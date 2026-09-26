package app.jscookbook.core.sync

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.jscookbook.core.data.db.JsCookBookDatabase
import app.jscookbook.core.data.photo.PhotoStore
import app.jscookbook.core.data.repository.Clock
import app.jscookbook.core.data.repository.CurrentCookbook
import app.jscookbook.core.data.repository.RecipeRepository
import app.jscookbook.core.data.repository.newId
import app.jscookbook.core.data.sync.LocalSync
import app.jscookbook.core.model.Photo
import app.jscookbook.core.model.RecipeDraft
import app.jscookbook.core.model.toDraft
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/** An in-memory stand-in for Supabase with the same rules as the SQL migration. */
private class FakeServer : SyncRemote {
    val tables = mutableMapOf<String, MutableMap<String, JsonObject>>()
    val files = mutableMapOf<String, ByteArray>()
    private var clock = 1_000_000L
    var online = true

    override fun isReady() = online

    override suspend fun upsert(table: String, conflictColumns: String, rows: List<JsonObject>) {
        check(online) { "offline" }
        val stored = tables.getOrPut(table) { mutableMapOf() }
        rows.forEach { row ->
            val key = conflictColumns.split(",").joinToString(":") { row[it]!!.jsonPrimitive.content }
            val old = stored[key]
            // The sync_stamp trigger: an older updated_at never overwrites a newer one.
            if (old != null && row.long("updated_at") < old.long("updated_at")) return@forEach
            stored[key] = JsonObject(row + ("server_updated_at" to JsonPrimitive(++clock)))
        }
    }

    override suspend fun fetchSince(table: String, cookbookId: String, since: Long, offset: Long, limit: Int): List<JsonObject> {
        check(online) { "offline" }
        val idColumn = if (table == "cookbooks") "id" else "cookbook_id"
        return tables[table].orEmpty().values
            .filter { it[idColumn]!!.jsonPrimitive.content == cookbookId && it.long("server_updated_at") >= since }
            .sortedBy { it.long("server_updated_at") }
            .drop(offset.toInt()).take(limit)
    }

    override suspend fun upload(path: String, bytes: ByteArray) {
        check(online) { "offline" }
        files[path] = bytes
    }

    override suspend fun download(path: String): ByteArray = checkNotNull(files[path]) { "no file $path" }

    private fun JsonObject.long(name: String) = this[name]!!.jsonPrimitive.long
}

private class Phone(context: Context, server: SyncRemote, clock: Clock, filesDir: String) {
    val db = Room.inMemoryDatabaseBuilder(context, JsCookBookDatabase::class.java).allowMainThreadQueries().build()
    val cookbook = CurrentCookbook(db.cookbookDao(), db.syncDao(), clock)
    val recipes = RecipeRepository(db, cookbook, clock)
    val local = LocalSync(db, cookbook, clock)
    val photoStore = PhotoStore(object : android.content.ContextWrapper(context) {
        override fun getFilesDir() = File(context.filesDir, filesDir).apply { mkdirs() }
    })
    val engine = SyncEngine(server, local, photoStore, clock)

    suspend fun titles() = recipes.observeRecipes().first().map { it.title }.sorted()
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TwoPhoneSyncTest {

    private var now = 1_000L
    private val clock = Clock { now }
    private val server = FakeServer()
    private lateinit var isaiah: Phone
    private lateinit var julia: Phone

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        isaiah = Phone(context, server, clock, "isaiah")
        julia = Phone(context, server, clock, "julia")
    }

    @After
    fun tearDown() {
        isaiah.db.close()
        julia.db.close()
    }

    /** Isaiah shares his cook book, Julia joins it. */
    private suspend fun household() {
        isaiah.local.linkCurrentCookbook()
        julia.local.adoptCookbook(isaiah.cookbook.id())
    }

    @Test
    fun editOnOnePhoneAppearsOnTheOther() = runTest {
        household()
        val id = newId()
        isaiah.recipes.save(RecipeDraft(id = id, title = "Tomato Soup"))
        assertEquals(SyncResult.Success, isaiah.engine.sync())
        assertEquals(SyncResult.Success, julia.engine.sync())
        assertEquals(listOf("Tomato Soup"), julia.titles())

        now = 2_000L
        julia.recipes.save(julia.recipes.observeRecipe(id).first()!!.toDraft().copy(title = "Roasted Tomato Soup"))
        julia.engine.sync()
        isaiah.engine.sync()
        assertEquals(listOf("Roasted Tomato Soup"), isaiah.titles())
    }

    @Test
    fun deletesSync() = runTest {
        household()
        val id = newId()
        isaiah.recipes.save(RecipeDraft(id = id, title = "Chili"))
        isaiah.engine.sync(); julia.engine.sync()
        now = 2_000L
        julia.recipes.delete(id)
        julia.engine.sync(); isaiah.engine.sync()
        assertTrue(isaiah.titles().isEmpty())
    }

    @Test
    fun recipesAlreadyOnTheJoiningPhoneAreKept() = runTest {
        julia.recipes.save(RecipeDraft(id = newId(), title = "Julia's Bread"))
        isaiah.recipes.save(RecipeDraft(id = newId(), title = "Isaiah's Pasta"))
        household()
        julia.engine.sync(); isaiah.engine.sync(); julia.engine.sync()
        assertEquals(listOf("Isaiah's Pasta", "Julia's Bread"), isaiah.titles())
        assertEquals(listOf("Isaiah's Pasta", "Julia's Bread"), julia.titles())
    }

    @Test
    fun offlineConflictResolvesToLatestEditOnBothPhones() = runTest {
        household()
        val id = newId()
        isaiah.recipes.save(RecipeDraft(id = id, title = "Curry"))
        isaiah.engine.sync(); julia.engine.sync()

        // Both edit while offline; Julia's edit is the later one.
        server.online = false
        now = 5_000L
        isaiah.recipes.save(isaiah.recipes.observeRecipe(id).first()!!.toDraft().copy(title = "Curry (Isaiah)"))
        now = 6_000L
        julia.recipes.save(julia.recipes.observeRecipe(id).first()!!.toDraft().copy(title = "Curry (Julia)"))
        assertEquals(SyncResult.Skipped, isaiah.engine.sync())
        server.online = true

        // Julia's phone reconnects first, then Isaiah's (whose older edit must not win).
        julia.engine.sync(); isaiah.engine.sync(); julia.engine.sync()
        assertEquals(listOf("Curry (Julia)"), isaiah.titles())
        assertEquals(listOf("Curry (Julia)"), julia.titles())
        assertEquals(0, isaiah.local.observePendingCount().first())
    }

    @Test
    fun photosUploadAndDownload() = runTest {
        household()
        val file = File(isaiah.photoStore.newCaptureFile().parentFile, "p1.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val thumb = File(file.parentFile, "p1-thumb.jpg").apply { writeBytes(byteArrayOf(4)) }
        val id = newId()
        isaiah.recipes.save(
            RecipeDraft(id = id, title = "Pie", photos = listOf(Photo("p1", file.path, thumb.path, 3, 1, true, ""))),
        )
        isaiah.engine.sync()
        val cookbookId = isaiah.cookbook.id()
        assertNotNull(server.files["$cookbookId/p1.jpg"])
        assertNotNull(server.files["$cookbookId/thumbs/p1.jpg"])

        julia.engine.sync()
        val photo = julia.recipes.observeRecipe(id).first()!!.photos.single()
        assertEquals(listOf<Byte>(1, 2, 3), File(photo.localPath!!).readBytes().toList())
        assertEquals(listOf<Byte>(4), File(photo.thumbnailPath!!).readBytes().toList())
    }

    @Test
    fun manyRecipesSyncInOnePass() = runTest {
        household()
        repeat(30) { isaiah.recipes.save(RecipeDraft(id = newId(), title = "Recipe %02d".format(it))) }
        isaiah.engine.sync()
        julia.engine.sync()
        assertEquals(30, julia.titles().size)
    }
}
