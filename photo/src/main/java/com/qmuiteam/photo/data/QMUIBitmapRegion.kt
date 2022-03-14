package com.qmuiteam.photo.data

import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.LruCache
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min


fun interface QMUIBitmapRegionLoader {
    suspend fun load(): Bitmap?
}

class QMUIBitmapRegionProvider(
    val width: Int,
    val height: Int,
    val loader: QMUIBitmapRegionLoader
)

class QMUIAlreadyBitmapRegionLoader(private val bm: Bitmap) : QMUIBitmapRegionLoader {
    override suspend fun load(): Bitmap {
        return bm
    }
}

private class QMUICacheBitmapRegionLoader(
    private val origin: QMUIBitmapRegionLoader,
    private val cacheStatistic: QMUIBitmapRegionCacheStatistic
) : QMUIBitmapRegionLoader {

    @Volatile
    private var cache: Bitmap? = null
    private val mutex = Mutex()

    override suspend fun load(): Bitmap? {
        val localCache = cache
        if (localCache != null) {
            return localCache
        }
        return mutex.withLock {
            if (cache != null) {
                return cache
            }
            origin.load().also {
                cache = it
                cacheStatistic.doWhenLoaded(this)
            }
        }
    }

    suspend fun releaseCache() {
        mutex.withLock {
            cache = null
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
    cacheTimeoutForLazyLoad: Long = 1000,
    cacheCountForLazyLoad: Int = 5
): QMUIBitmapRegion {
    val cacheStatistic = QMUIBitmapRegionCacheStatistic(cacheTimeoutForLazyLoad, cacheCountForLazyLoad)
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

                    private val mutex = Mutex()

                    override suspend fun load(): Bitmap? {
                        return mutex.withLock {
                            regionDecoder.decodeRegion(Rect(0, finalTop, w, bottom), options)
                        }
                    }

                }
                ret.add(
                    QMUIBitmapRegionProvider(
                        w, bottom - finalTop, if (cacheStatistic.canCache()) {
                            QMUICacheBitmapRegionLoader(loader, cacheStatistic)
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

private class QMUIBitmapRegionCacheStatistic(
    val cacheTimeoutForLazyLoad: Long,
    val cacheCountForLazyLoad: Int
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val cacheJobs = object : LruCache<QMUICacheBitmapRegionLoader, Job>(cacheCountForLazyLoad) {
        override fun entryRemoved(evicted: Boolean, key: QMUICacheBitmapRegionLoader?, oldValue: Job?, newValue: Job?) {
            super.entryRemoved(evicted, key, oldValue, newValue)
            if (newValue == null) {
                key?.let {
                    scope.launch {
                        it.releaseCache()
                    }
                }
            } else {
                oldValue?.cancel()
            }
        }
    }

    fun doWhenLoaded(loader: QMUICacheBitmapRegionLoader) {
        val job = scope.launch {
            delay(cacheTimeoutForLazyLoad)
            cacheJobs.remove(loader)
        }
        cacheJobs.put(loader, job)
    }

    fun canCache(): Boolean {
        return cacheTimeoutForLazyLoad > 0 && cacheCountForLazyLoad > 0
    }
}


class QMUIBitmapRegionHolderDrawable(val bitmapRegion: QMUIBitmapRegion) : Drawable() {

    override fun getIntrinsicHeight(): Int {
        return bitmapRegion.height
    }

    override fun getIntrinsicWidth(): Int {
        return bitmapRegion.width
    }

    override fun draw(canvas: Canvas) {

    }

    override fun setAlpha(alpha: Int) {

    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }
}