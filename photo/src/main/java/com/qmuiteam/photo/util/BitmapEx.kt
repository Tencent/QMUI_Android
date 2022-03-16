package com.qmuiteam.photo.util

import android.content.Context
import android.graphics.Bitmap
import com.qmuiteam.compose.core.helper.QMUILog
import java.io.*


val DefaultBitmapCompressMaxSizeStrategy: (Bitmap) -> Int = {
    val ratio = it.width.toFloat() / it.height
    if (ratio < 0.33 || ratio > 3) {
        1024 * 1024 * 8
    } else {
        1024 * 1024 * 2
    }
}

val DefaultBitmapCompressCanUseMemoryStorage: (Bitmap) -> Boolean = {
    it.width * it.height < 1080 * 1920
}

abstract class BitmapCompressResult internal constructor(
    val compressFormat: Bitmap.CompressFormat,
    val compressQuality: Int
) {
    abstract fun inputStream(): InputStream?
}

internal class BitmapCompressStreamResult(
    compressFormat: Bitmap.CompressFormat,
    compressQuality: Int,
    private val stream: BitmapCompressStream
): BitmapCompressResult(compressFormat, compressQuality){

    override fun inputStream(): InputStream? {
        return stream.inputStream()
    }
}

fun Bitmap.compressByShortEdgeWidthAndByteSize(
    context: Context,
    shortEdgeMaxWidth: Int = 1200,
    byteMaxSizeStrategy: (Bitmap) -> Int = DefaultBitmapCompressMaxSizeStrategy,
    canUseMemoryStorage: (Bitmap) -> Boolean = DefaultBitmapCompressCanUseMemoryStorage,
    compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    compressQuality: Int = 80,
): BitmapCompressResult? {

    var bitmap = this
    try {
        val ratio = width.toFloat() / height
        if (width <= height) {
            if (width > shortEdgeMaxWidth) {
                bitmap = Bitmap.createScaledBitmap(this, shortEdgeMaxWidth, (shortEdgeMaxWidth / ratio).toInt(), false)
            }
        } else {
            if (height > shortEdgeMaxWidth) {
                bitmap = Bitmap.createScaledBitmap(this, (shortEdgeMaxWidth * ratio).toInt(), shortEdgeMaxWidth, false)
            }
        }
    } catch (ignored: OutOfMemoryError) {
        QMUILog.w(
            "compressByShortEdgeWidthAndByteSize",
            "createScaledBitmap failed: shortEdgeMaxWidth = $shortEdgeMaxWidth, width = $width; height = $height"
        )
    }

    val byteMaxSize = byteMaxSizeStrategy(this)
    val useMemoryStorage = canUseMemoryStorage(this)

    val stream: BitmapCompressStream = if (useMemoryStorage) BitmapCompressMemoryStream() else BitmapCompressFileStream(context.cacheDir)
    var currentQuality = compressQuality
    var nextQuality = currentQuality
    var failCount = 0
    var succes: Boolean
    do {
        stream.reset()
        currentQuality = nextQuality
        succes = try {
            stream.outputStream().use {
                bitmap.compress(compressFormat, currentQuality, it)
            }
        } catch (e: Throwable) {
            QMUILog.w(
                "compressByShortEdgeWidthAndByteSize",
                "compress bitmap failed(compressFormat = $compressFormat; quality = $nextQuality, failCount = $failCount).", e
            )
            false
        }
        if (succes) {
            nextQuality -= 10
            failCount = 0
        } else {
            nextQuality -= 5
            failCount++
        }
    } while ((!succes && failCount < 2 && nextQuality >= 20) || (succes && nextQuality >= 20 && stream.size() > byteMaxSize))
    if (!succes) {
        return null
    }
    return BitmapCompressStreamResult(compressFormat, currentQuality, stream)
}

internal interface BitmapCompressStream {

    fun reset()

    fun size(): Int

    fun outputStream(): OutputStream

    fun inputStream(): InputStream?
}

internal class BitmapCompressMemoryStream : BitmapCompressStream {

    private val output = ByteArrayOutputStream()

    override fun reset() {
        output.reset()
    }

    override fun size(): Int {
        return output.size()
    }

    override fun outputStream(): OutputStream {
        return output
    }

    override fun inputStream(): InputStream {
        return ByteArrayInputStream(output.toByteArray())
    }

}

internal class BitmapCompressFileStream(val cacheDir: File) : BitmapCompressStream {

    private var file: File? = null

    override fun reset() {
        file?.delete()
        file = File(cacheDir, "qmui-bm-${System.nanoTime()}")
    }

    override fun size(): Int {
        return file?.length()?.toInt() ?: 0
    }

    override fun outputStream(): OutputStream {
        return file!!.outputStream().buffered()
    }

    override fun inputStream(): InputStream? {
        return file?.inputStream()?.buffered()
    }
}
