package com.qmuiteam.photo.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.os.Build
import androidx.compose.ui.unit.IntSize
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min


fun interface QMUIBitmapRegionLoader {
    fun load(): Bitmap?
}

class QMUIBitmapRegionProvider(
    val width: Int,
    val height: Int,
    val loader: QMUIBitmapRegionLoader
)

class QMUIAlreadyBitmapRegionLoader(private val bm: Bitmap) : QMUIBitmapRegionLoader {
    override fun load(): Bitmap {
        return bm
    }
}

class QMUICacheBitmapRegionLoader(private val origin: QMUIBitmapRegionLoader) : QMUIBitmapRegionLoader {
    @Volatile
    private var cache: Bitmap? = null

    override fun load(): Bitmap? {
        if (cache != null) {
            return cache
        }
        synchronized(this) {
            if (cache != null) {
                return cache
            }
            cache = origin.load()
            return cache
        }
    }
}

class QMUIBitmapRegion(val width: Int, val height: Int, val list: List<QMUIBitmapRegionProvider>)


/**
 * fit:
 *  if ture, fit the image to the dst so that both dimensions (width and height) of the image will be equal to or less than the dst
 *  if false, fill the image in the dst such that both dimensions (width and height) of the image will be equal to or larger than the dst
 */
fun loadLongImageThumbnail(
    ins: InputStream,
    preferredSize: IntSize,
    options: BitmapFactory.Options,
    fit: Boolean = false,
): Bitmap? {
    return loadLongImage(ins, preferredSize, options, fit) { regionDecoder ->
        val w = regionDecoder.width
        val h = regionDecoder.height
        val pageHeight = if (preferredSize.width > 0 && preferredSize.height > 0) {
            (w * preferredSize.height / preferredSize.width).coerceAtMost(w * 5).coerceAtMost(h)
        } else {
            (5 * w).coerceAtMost(h)
        }
        regionDecoder.decodeRegion(Rect(0, 0, w, pageHeight), options)
    }
}

/**
 * fit:
 *  if ture, fit the image to the dst so that both dimensions (width and height) of the image will be equal to or less than the dst
 *  if false, fill the image in the dst such that both dimensions (width and height) of the image will be equal to or larger than the dst
 */
fun loadLongImage(
    ins: InputStream,
    preferredSize: IntSize,
    options: BitmapFactory.Options,
    fit: Boolean = false,
    preloadCount: Int = Int.MAX_VALUE,
    cacheForLazyLoad: Boolean = false
): QMUIBitmapRegion {

    return loadLongImage(ins, preferredSize, options, fit) { regionDecoder ->
        val w = regionDecoder.width
        val h = regionDecoder.height
        val pageHeight = if (preferredSize.width > 0 && preferredSize.height > 0) {
            (w * preferredSize.height / preferredSize.width).coerceAtMost(w * 5).coerceAtMost(h)
        } else {
            (5 * w).coerceAtMost(h)
        }

        val ret = arrayListOf<QMUIBitmapRegionProvider>()
        var top = 0
        var i = 0
        while (top < h) {
            val bottom = (top + pageHeight).coerceAtMost(h)
            if (i < preloadCount) {
                val bm = regionDecoder.decodeRegion(Rect(0, top, w, bottom), options)
                ret.add(QMUIBitmapRegionProvider(bm.width, bm.height, QMUIAlreadyBitmapRegionLoader(bm)))
            } else {
                val finalTop = top
                val loader = object : QMUIBitmapRegionLoader {
                    override fun load(): Bitmap? {
                        synchronized(this) {
                            return regionDecoder.decodeRegion(Rect(0, finalTop, w, bottom), options)
                        }
                    }

                }
                ret.add(
                    QMUIBitmapRegionProvider(
                        w, bottom - finalTop, if (cacheForLazyLoad) {
                            QMUICacheBitmapRegionLoader(loader)
                        } else {
                            loader
                        }
                    )
                )
            }
            top = bottom
            i++
        }

        QMUIBitmapRegion(w, h, ret)
    }
}


private fun <T> loadLongImage(
    ins: InputStream,
    preferredSize: IntSize,
    options: BitmapFactory.Options,
    fit: Boolean = false,
    handler: (BitmapRegionDecoder) -> T
): T {
    // Read the image's dimensions.
    options.inJustDecodeBounds = true
    val bufferedIns = ins.buffered()
    bufferedIns.mark(Int.MAX_VALUE)
    BitmapFactory.decodeStream(bufferedIns, null, options)
    options.inJustDecodeBounds = false
    bufferedIns.reset()

    options.inMutable = false

    if (options.outWidth > 0 && options.outHeight > 0) {
        val dstWidth = if (preferredSize.width <= 0) options.outWidth else preferredSize.width
        val dstHeight = if (preferredSize.height <= 0) options.outHeight else preferredSize.height
        options.inSampleSize = calculateInSampleSize(
            srcWidth = options.outWidth,
            srcHeight = options.outHeight,
            dstWidth = dstWidth,
            dstHeight = dstHeight,
            fit = fit
        )
    } else {
        options.inSampleSize = 1
    }

    val regionDecoder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        BitmapRegionDecoder.newInstance(bufferedIns)
    } else {
        BitmapRegionDecoder.newInstance(bufferedIns, false)
    }
    checkNotNull(regionDecoder) { "BitmapRegionDecoder newInstance failed." }
    return handler(regionDecoder)
}

private fun calculateInSampleSize(
    srcWidth: Int,
    srcHeight: Int,
    dstWidth: Int,
    dstHeight: Int,
    fit: Boolean = false
): Int {
    val widthInSampleSize = Integer.highestOneBit(srcWidth / dstWidth)
    val heightInSampleSize = Integer.highestOneBit(srcHeight / dstHeight)
    return if (fit) {
        max(widthInSampleSize, heightInSampleSize).coerceAtLeast(1)
    } else {
        min(widthInSampleSize, heightInSampleSize).coerceAtLeast(1)
    }
}