package com.qmuiteam.photo.util

import android.graphics.Bitmap
import com.qmuiteam.compose.core.helper.QMUILog
import java.io.ByteArrayOutputStream

fun interface BitmapCompressMaxSizeStrategy {
    fun get(ratio: Float): Int
}

val DefaultBitmapCompressMaxSizeStrategy = BitmapCompressMaxSizeStrategy {
    if (it < 0.33 || it > 3) {
        1024 * 1024 * 8
    } else {
        1024 * 1024 * 2
    }
}

fun Bitmap.compressByShortEdgeWidthAndByteSize(
    shortEdgeMaxWidth: Int = 1200,
    byteMaxSizeStrategy: BitmapCompressMaxSizeStrategy = DefaultBitmapCompressMaxSizeStrategy,
    compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    compressQuality: Int = 80
): ByteArray {
    val ratio = width.toFloat() / height
    var bitmap = this
    try {
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

    val output = ByteArrayOutputStream()
    bitmap.compress(compressFormat, compressQuality, output)
    val byteMaxSize = byteMaxSizeStrategy.get(ratio)
    var nextQuality = compressQuality - 10
    while (nextQuality >= 20 && output.toByteArray().size > byteMaxSize) {
        output.reset()
        bitmap.compress(compressFormat, nextQuality, output)
        nextQuality -= 10
    }
    return output.toByteArray()
}