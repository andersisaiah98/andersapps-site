package app.jscookbook.core.data.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import app.jscookbook.core.data.repository.newId
import app.jscookbook.core.model.Photo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Photos live in app-private storage: a full image resized on the device to at most 2048 px on
 * the long edge, and a small thumbnail for grids. Both are JPEGs. Sync uploads them to Storage.
 */
@Singleton
class PhotoStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val photoDir get() = File(context.filesDir, "photos").apply { mkdirs() }
    private val thumbDir get() = File(photoDir, "thumbs").apply { mkdirs() }

    /** Where the camera writes before [import] (shared with the camera app via FileProvider). */
    fun newCaptureFile(): File =
        File(context.cacheDir, "camera").apply { mkdirs() }.let { File(it, "${newId()}.jpg") }

    /** Decodes, resizes and stores the image at [uri]. EXIF rotation is applied by the decoder. */
    suspend fun import(uri: Uri): Photo = withContext(Dispatchers.IO) {
        val id = newId()
        val full = decode(uri, MaxEdge)
        val photoFile = File(photoDir, "$id.jpg")
        val thumbFile = File(thumbDir, "$id.jpg")
        try {
            photoFile.outputStream().use { full.compress(Bitmap.CompressFormat.JPEG, 88, it) }
            val thumb = scaleDown(full, ThumbEdge)
            thumbFile.outputStream().use { thumb.compress(Bitmap.CompressFormat.JPEG, 82, it) }
            if (thumb !== full) thumb.recycle()
            Photo(
                id = id,
                localPath = photoFile.absolutePath,
                thumbnailPath = thumbFile.absolutePath,
                width = full.width,
                height = full.height,
                isCover = false,
                caption = "",
            )
        } finally {
            full.recycle()
        }
    }

    /** Removes the files of a photo that was never saved (e.g. the editor was discarded). */
    suspend fun discard(photo: Photo) = withContext(Dispatchers.IO) {
        photo.localPath?.let { File(it).delete() }
        photo.thumbnailPath?.let { File(it).delete() }
        Unit
    }

    /** Stores a photo that synced from the other phone. Returns the (full, thumbnail) paths. */
    suspend fun saveDownloaded(id: String, full: ByteArray, thumbnail: ByteArray): Pair<String, String> =
        withContext(Dispatchers.IO) {
            val photoFile = File(photoDir, "$id.jpg")
            val thumbFile = File(thumbDir, "$id.jpg")
            // Write to temp names first so a half-written file is never shown.
            File(photoDir, "$id.jpg.part").apply { writeBytes(full); renameTo(photoFile) }
            File(thumbDir, "$id.jpg.part").apply { writeBytes(thumbnail); renameTo(thumbFile) }
            photoFile.absolutePath to thumbFile.absolutePath
        }

    suspend fun read(path: String): ByteArray = withContext(Dispatchers.IO) { File(path).readBytes() }

    private fun decode(uri: Uri, maxEdge: Int): Bitmap {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val longEdge = max(info.size.width, info.size.height)
            if (longEdge > maxEdge) {
                val scale = maxEdge.toFloat() / longEdge
                decoder.setTargetSize(
                    (info.size.width * scale).roundToInt().coerceAtLeast(1),
                    (info.size.height * scale).roundToInt().coerceAtLeast(1),
                )
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }

    private fun scaleDown(bitmap: Bitmap, maxEdge: Int): Bitmap {
        val longEdge = max(bitmap.width, bitmap.height)
        if (longEdge <= maxEdge) return bitmap
        val scale = maxEdge.toFloat() / longEdge
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).roundToInt().coerceAtLeast(1),
            (bitmap.height * scale).roundToInt().coerceAtLeast(1),
            true,
        )
    }

    private companion object {
        const val MaxEdge = 2048
        const val ThumbEdge = 512
    }
}
