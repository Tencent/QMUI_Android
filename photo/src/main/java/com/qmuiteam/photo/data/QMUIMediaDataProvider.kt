package com.qmuiteam.photo.data

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.qmuiteam.compose.core.helper.QMUILog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

const val QMUIMediaPhotoBucketAllId = "__all__"
const val QMUIMediaPhotoBucketAllName = "最近项目"

open class QMUIMediaModel(
    val id: Long,
    val uri: Uri,
    var width: Int,
    var height: Int,
    val rotation: Int,
    val name: String,
    val modifyTimeSec: Long,
    val bucketId: String,
    val bucketName: String,
    val editable: Boolean
) {
    fun ratio(): Float {
        if(height <= 0 || width <= 0){
            return -1f
        }
        if(rotation == 90 || rotation == 270){
            return height.toFloat() / width
        }
        return width.toFloat() / height
    }
}

class QMUIMediaPhotoBucket(
    val id: String,
    val name: String,
    val list: List<QMUIMediaModel>
)

class QMUIMediaPhotoBucketVO(
    val id: String,
    val name: String,
    val list: List<QMUIMediaPhotoVO>
)

class QMUIMediaPhotoVO(
    val model: QMUIMediaModel,
    val photoProvider: QMUIPhotoProvider
)

interface QMUIMediaPhotoProviderFactory {
    fun factory(model: QMUIMediaModel): QMUIPhotoProvider
}

interface QMUIMediaDataProvider {
    suspend fun provide(context: Context, supportedMimeTypes: Array<String>): List<QMUIMediaPhotoBucket>
}

class QMUIMediaImagesProvider : QMUIMediaDataProvider {

    companion object {

        private const val TAG = "QMUIMediaDataProvider"

        val DEFAULT_SUPPORT_MIMETYPES = arrayOf(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/heic",
            "image/heif"
        )

        private val COLUMNS = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.ORIENTATION,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )
    }

    override suspend fun provide(context: Context, supportedMimeTypes: Array<String>): List<QMUIMediaPhotoBucket> {
        return withContext(Dispatchers.IO) {
            val selection = if (supportedMimeTypes.isEmpty()) {
                null
            } else {
                val sb = StringBuilder()
                sb.append(MediaStore.Images.Media.MIME_TYPE)
                sb.append(" IN (")
                supportedMimeTypes.forEachIndexed { index, s ->
                    if (index != 0) {
                        sb.append(",")
                    }
                    sb.append("'")
                    sb.append(s)
                    sb.append("'")

                }
                sb.append(")")
                sb.toString()
            }
            val list = mutableListOf<QMUIMediaModel>()
            context.applicationContext.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                COLUMNS,
                selection,
                null,
                "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        try {
                            val path = cursor.readString(MediaStore.Images.Media.DATA)
                            val id = cursor.readLong(MediaStore.Images.Media._ID)
                            val w = cursor.readInt(MediaStore.Images.Media.WIDTH)
                            val h = cursor.readInt(MediaStore.Images.Media.HEIGHT)
                            val o = cursor.readInt(MediaStore.Images.Media.ORIENTATION)
                            val isRotated = o == 90 || o == 270
                            list.add(
                                QMUIMediaModel(
                                    id,
                                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id),
                                    if(isRotated) h else w,
                                    if(isRotated) w else h,
                                    cursor.readInt(MediaStore.Images.Media.ORIENTATION),
                                    cursor.readString(MediaStore.Images.Media.DISPLAY_NAME),
                                    cursor.readLong(MediaStore.Images.Media.DATE_MODIFIED),
                                    cursor.readString(MediaStore.Images.Media.BUCKET_ID),
                                    (cursor.readString(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)).let {
                                        it.ifEmpty { File(path).parent ?: "" }
                                    },
                                    true
                                )
                            )
                        } catch (e: Exception) {
                            QMUILog.e(TAG, "read image data from cursor failed.", e)
                        }
                    } while (cursor.moveToNext())
                }
            }
            val buckets = mutableListOf<MutableMediaPhotoBucket>()
            val defaultPhotoBucket = MutableMediaPhotoBucket(QMUIMediaPhotoBucketAllId, QMUIMediaPhotoBucketAllName)
            buckets.add(defaultPhotoBucket)
            list.forEach { model ->
                defaultPhotoBucket.list.add(model)
                if(model.name.isNotBlank()){
                    val bucket = buckets.find { it.id == model.bucketId} ?:MutableMediaPhotoBucket(model.bucketId, model.bucketName).also {
                        buckets.add(it)
                    }
                    bucket.list.add(model)
                }
            }

            buckets.map {
                QMUIMediaPhotoBucket(it.id, it.name, it.list)
            }
        }
    }

    private class MutableMediaPhotoBucket(
        val id: String,
        val name: String
    ){
        val list: MutableList<QMUIMediaModel> = mutableListOf()
    }

}


private fun <T> Cursor.getColumnIndexAndDoAction(columnName: String, block: (Int) -> T): T? {
    return try {
        getColumnIndexOrThrow(columnName).let {
            if (it < 0) null else block(it)
        }
    } catch (e: Throwable) {
        QMUILog.e("QMUIMediaDataProvider", "getColumnIndex for $columnName failed.", e)
        null
    }
}

fun Cursor.readLong(columnName: String): Long = getColumnIndexAndDoAction(columnName) { getLongOrNull(it) } ?: 0
fun Cursor.readString(columnName: String): String = getColumnIndexAndDoAction(columnName) { getStringOrNull(it) } ?: ""
fun Cursor.readInt(columnName: String): Int = getColumnIndexAndDoAction(columnName) { getIntOrNull(it) } ?: 0
