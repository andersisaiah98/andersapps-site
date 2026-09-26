package app.jscookbook.core.data.sync

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.jscookbook.core.data.db.JsCookBookDatabase
import app.jscookbook.core.data.db.PhotoEntity
import app.jscookbook.core.data.db.RecipeEntity
import app.jscookbook.core.data.repository.CategoryRepository
import app.jscookbook.core.data.repository.Clock
import app.jscookbook.core.data.repository.CurrentCookbook
import app.jscookbook.core.data.repository.RecipeRepository
import app.jscookbook.core.data.repository.newId
import app.jscookbook.core.model.Photo
import app.jscookbook.core.model.RecipeDraft
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LocalSyncTest {

    private lateinit var db: JsCookBookDatabase
    private var now = 1_000L
    private val clock = Clock { now }
    private lateinit var cookbook: CurrentCookbook
    private lateinit var recipes: RecipeRepository
    private lateinit var categories: CategoryRepository
    private lateinit var sync: LocalSync

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), JsCookBookDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        cookbook = CurrentCookbook(db.cookbookDao(), db.syncDao(), clock)
        recipes = RecipeRepository(db, cookbook, clock)
        categories = CategoryRepository(db.categoryDao(), cookbook, clock)
        sync = LocalSync(db, cookbook, clock)
    }

    @After
    fun tearDown() = db.close()

    private suspend fun saveRecipe(id: String = newId(), title: String = "Soup", photos: List<Photo> = emptyList()): String {
        recipes.save(RecipeDraft(id = id, title = title, photos = photos))
        return id
    }

    /** The row as the server would send it back. */
    private suspend fun remoteRecipe(id: String, title: String, updatedAt: Long): JsonObject {
        val local = db.recipeDao().get(id) ?: RecipeEntity(
            id = id, cookbookId = cookbook.id(), title = title, type = null, description = "", servings = "",
            prepMinutes = null, cookMinutes = null, sourceUrl = "", tags = "", isFavorite = false, rating = null,
            notes = "", imageSource = "FALLBACK", createdAt = 1, updatedAt = updatedAt,
        )
        val json = SyncJson.encodeToJsonElement(RecipeEntity.serializer(), local.copy(title = title, updatedAt = updatedAt)).jsonObject
        return JsonObject(json + ("server_updated_at" to JsonPrimitive(999_999L)))
    }

    @Test
    fun localEditsArePendingUntilPushed() = runTest {
        val id = saveRecipe()
        val pending = sync.pendingPush(SyncTables.recipes)
        assertEquals(listOf(id), pending.map { it.key })
        // Phone-only fields never go to the server; nulls do (so a restore clears deleted_at).
        val json = pending.single().json
        assertFalse("dirty" in json)
        assertTrue("deleted_at" in json)
        assertEquals(1_000L, json["updated_at"]!!.jsonPrimitive.long)

        sync.markPushed(SyncTables.recipes, pending)
        assertTrue(sync.pendingPush(SyncTables.recipes).isEmpty())
        sync.markPushed(SyncTables.cookbooks, sync.pendingPush(SyncTables.cookbooks))
        assertEquals(0, sync.observePendingCount().first())
    }

    @Test
    fun rowEditedDuringPushStaysDirty() = runTest {
        val id = saveRecipe()
        val pending = sync.pendingPush(SyncTables.recipes)
        now = 2_000L
        recipes.setFavorite(id, true) // lands while the push is in flight
        sync.markPushed(SyncTables.recipes, pending)
        assertEquals(listOf(2_000L), sync.pendingPush(SyncTables.recipes).map { it.updatedAt })
    }

    @Test
    fun softDeletesAreDirtyAndSync() = runTest {
        val id = saveRecipe()
        sync.markPushed(SyncTables.recipes, sync.pendingPush(SyncTables.recipes))
        now = 3_000L
        recipes.delete(id)
        val json = sync.pendingPush(SyncTables.recipes).single().json
        assertEquals(3_000L, json["deleted_at"]!!.jsonPrimitive.long)
    }

    @Test
    fun newerRemoteEditReplacesUnpushedLocalEdit() = runTest {
        val id = saveRecipe(title = "Mine") // updated_at 1000, dirty
        val applied = sync.applyRemote(SyncTables.recipes, listOf(remoteRecipe(id, "Theirs", updatedAt = 1_500)))
        assertEquals(1, applied)
        val row = db.recipeDao().get(id)!!
        assertEquals("Theirs", row.title)
        assertFalse(row.dirty)
        assertTrue(sync.pendingPush(SyncTables.recipes).isEmpty())
    }

    @Test
    fun newerUnpushedLocalEditSurvivesOlderRemote() = runTest {
        now = 5_000L
        val id = saveRecipe(title = "Mine")
        val applied = sync.applyRemote(SyncTables.recipes, listOf(remoteRecipe(id, "Theirs", updatedAt = 4_000)))
        assertEquals(0, applied)
        assertEquals("Mine", db.recipeDao().get(id)!!.title)
        assertEquals(listOf(id), sync.pendingPush(SyncTables.recipes).map { it.key })
    }

    @Test
    fun staleEchoDoesNotOverwriteSyncedRow() = runTest {
        now = 5_000L
        val id = saveRecipe(title = "Current")
        sync.markPushed(SyncTables.recipes, sync.pendingPush(SyncTables.recipes))
        assertEquals(0, sync.applyRemote(SyncTables.recipes, listOf(remoteRecipe(id, "Old", updatedAt = 4_000))))
        assertEquals("Current", db.recipeDao().get(id)!!.title)
    }

    @Test
    fun rowsFromTheOtherPhoneAppear() = runTest {
        val id = newId()
        assertEquals(1, sync.applyRemote(SyncTables.recipes, listOf(remoteRecipe(id, "From Julia's phone", updatedAt = 10))))
        assertEquals(listOf("From Julia's phone"), recipes.observeRecipes().first().map { it.title })
        assertTrue(sync.pendingPush(SyncTables.recipes).isEmpty())
    }

    @Test
    fun photosWaitForUploadAndKeepTheirLocalFiles() = runTest {
        val photo = Photo("p1", "/files/p1.jpg", "/files/thumbs/p1.jpg", 100, 80, true, "")
        val id = saveRecipe(photos = listOf(photo))
        // Not uploaded yet: the row is held back so the other phone never sees a photo without a file.
        assertTrue(sync.pendingPush(SyncTables.photos).isEmpty())
        assertEquals(listOf("p1"), sync.photosToUpload("photos").map { it.id })

        sync.setStoragePaths("photos", "p1", "cb/p1.jpg", "cb/thumbs/p1.jpg")
        val pending = sync.pendingPush(SyncTables.photos)
        assertEquals("cb/p1.jpg", pending.single().json["storage_path"]!!.jsonPrimitive.content)
        assertFalse("local_path" in pending.single().json)
        sync.markPushed(SyncTables.photos, pending)

        // The server's copy (a caption edit from the other phone) has no local paths; ours are kept.
        val remote = SyncJson.encodeToJsonElement(
            PhotoEntity.serializer(),
            db.syncDao().photos(listOf("p1")).single().copy(caption = "Plated", updatedAt = 9_000),
        ).jsonObject
        sync.applyRemote(SyncTables.photos, listOf(remote))
        val row = db.syncDao().photos(listOf("p1")).single()
        assertEquals("Plated", row.caption)
        assertEquals("/files/p1.jpg", row.localPath)
        assertEquals(id, row.recipeId)
    }

    @Test
    fun photoFromOtherPhoneIsQueuedForDownload() = runTest {
        val id = saveRecipe()
        val remote = SyncJson.encodeToJsonElement(
            PhotoEntity.serializer(),
            PhotoEntity(
                id = "p9", recipeId = id, cookbookId = cookbook.id(), storagePath = "cb/p9.jpg",
                thumbnailStoragePath = "cb/thumbs/p9.jpg", width = 10, height = 10, isCover = true, caption = "",
                position = 0, createdAt = 1, updatedAt = 1,
            ),
        ).jsonObject
        sync.applyRemote(SyncTables.photos, listOf(remote))
        assertEquals(listOf("p9"), sync.photosToDownload("photos").map { it.id })

        sync.setLocalPaths("photos", "p9", "/files/p9.jpg", "/files/thumbs/p9.jpg")
        assertTrue(sync.photosToDownload("photos").isEmpty())
        assertFalse(db.syncDao().photos(listOf("p9")).single().dirty)
    }

    @Test
    fun joiningAHouseholdMovesLocalRecipesIntoIt() = runTest {
        val oldId = cookbook.id()
        val category = categories.save(null, "Soups", "Pot", "terracotta")
        val recipe = saveRecipe()
        sync.markPushed(SyncTables.recipes, sync.pendingPush(SyncTables.recipes))

        sync.adoptCookbook("household")

        assertEquals("household", cookbook.id())
        assertEquals("household", cookbook.observeId().first())
        assertEquals("household", sync.linkedCookbook())
        assertEquals("household", db.recipeDao().get(recipe)!!.cookbookId)
        assertEquals("household", db.categoryDao().get(category)!!.cookbookId)
        assertNull(db.cookbookDao().get(oldId))
        // Moved rows go up on the next push, and the screens still show them.
        assertEquals(listOf(recipe), sync.pendingPush(SyncTables.recipes).map { it.key })
        assertEquals(1, recipes.observeRecipes().first().size)
        // The placeholder cook book loses to the real one from the server.
        assertTrue(sync.pendingPush(SyncTables.cookbooks).isEmpty())
    }

    @Test
    fun cursorsArePerTableAndClearedOnUnlink() = runTest {
        sync.setCursor(SyncTables.recipes, "cb", 42)
        assertEquals(42L, sync.cursor(SyncTables.recipes, "cb"))
        assertEquals(0L, sync.cursor(SyncTables.steps, "cb"))
        sync.linkCurrentCookbook()
        sync.unlink()
        assertEquals(0L, sync.cursor(SyncTables.recipes, "cb"))
        assertNull(sync.linkedCookbook())
    }
}
