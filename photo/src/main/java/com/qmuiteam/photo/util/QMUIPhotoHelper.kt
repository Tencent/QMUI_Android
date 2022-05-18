package com.qmuiteam.photo.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


object QMUIPhotoHelper {

    private const val TAG = "QMUIPhotoHelper"

    fun saveToStore(
        context: Context,
        bitmap: Bitmap,
        nameWithoutSuffix: String,
        dirName: String = Environment.DIRECTORY_PICTURES,
        compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        compressQuality: Int = 100
    ): Uri? {
        val suffix = when (compressFormat) {
            Bitmap.CompressFormat.JPEG -> ".jpeg"
            Bitmap.CompressFormat.PNG -> ".png"
            else -> ".webp"
        }
        val mime = when (compressFormat) {
            Bitmap.CompressFormat.JPEG -> "image/jpeg"
            Bitmap.CompressFormat.PNG -> "image/png"
            else -> "image/webp"
        }
        return saveToStore(context, "$nameWithoutSuffix$suffix", mime, dirName) {
            bitmap.compress(compressFormat, compressQuality, it)
        }
    }

    fun saveToStore(
        context: Context,
        name: String,
        mimeType: String,
        dirName: String = Environment.DIRECTORY_PICTURES,
        writer: (OutputStream) -> Unit
    ): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis())
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, dirName)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        var stream: OutputStream? = null
        var uri: Uri? = null
        try {
            uri = context.contentResolver.insert(contentUri, contentValues)
            if (uri == null) {
                throw IOException("Failed to create new MediaStore record.")
            }
            stream = context.contentResolver.openOutputStream(uri)
            if (stream == null) {
                throw IOException("Failed to get output stream.")
            }
            writer.invoke(stream)
            contentValues.clear()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
            }
            return uri
        } catch (e: Throwable) {
            Log.i(TAG, "saveToStore failed.", e)
            if (uri != null) {
                context.contentResolver.delete(uri, null, null)
            }
        } finally {
            stream?.close()
        }
        return null
    }

    fun compressByShortEdgeWidthAndByteSize(
        context: Context,
        originProvider: (Context) -> InputStream?,
        shortEdgeMaxWidth: Int = 1200,
        byteMaxSizeStrategy: (Bitmap) -> Int = DefaultBitmapCompressMaxSizeStrategy,
        canUseMemoryStorage: (Bitmap) -> Boolean = DefaultBitmapCompressCanUseMemoryStorage,
        compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        compressQuality: Int = 80
    ): BitmapCompressResult? {
        val applicationContext = context.applicationContext
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        var inputStream = originProvider(applicationContext) ?: return null
        inputStream.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        val imageHeight = options.outHeight
        val imageWidth = options.outWidth
        if (imageWidth <= imageHeight) {
            if (imageWidth > shortEdgeMaxWidth) {
                options.inSampleSize = Integer.highestOneBit(imageWidth / shortEdgeMaxWidth)
            }
        } else {
            if (imageHeight > shortEdgeMaxWidth) {
                options.inSampleSize = Integer.highestOneBit(imageHeight / shortEdgeMaxWidth)
            }
        }
        options.inJustDecodeBounds = false
        inputStream = originProvider(applicationContext) ?: return null
        val bitmap = inputStream.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return object : BitmapCompressResult(compressFormat, -1, -1, -1) {
            override fun inputStream(): InputStream? {
                return originProvider(applicationContext)
            }

        }
        return bitmap.compressByShortEdgeWidthAndByteSize(
            context,
            shortEdgeMaxWidth,
            byteMaxSizeStrategy,
            canUseMemoryStorage,
            compressFormat,
            compressQuality
        )
    }
}