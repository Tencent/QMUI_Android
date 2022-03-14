package com.qmuiteam.photo.glide

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.unit.IntSize
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.Option
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.module.LibraryGlideModule
import com.qmuiteam.photo.data.QMUIBitmapRegionHolderDrawable
import com.qmuiteam.photo.data.loadLongImage
import com.qmuiteam.photo.data.loadLongImageThumbnail
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer


val QMUI_PHOTO_IMG_IS_THUMB = Option.memory("com.qmuiteam.photo.isThumb", false)


class QMUILongGlidePhotoData(
    val drawable: Drawable
)

@GlideModule
class QMUIGlideModule : LibraryGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(
            Registry.BUCKET_BITMAP,
            InputStream::class.java,
            QMUILongGlidePhotoData::class.java,
            object : ResourceDecoder<InputStream, QMUILongGlidePhotoData> {
                override fun handles(source: InputStream, options: Options): Boolean {
                    return true
                }

                override fun decode(source: InputStream, width: Int, height: Int, options: Options): Resource<QMUILongGlidePhotoData> {
                    return doDecode(context, source, width, height, options)
                }

            })
    }
}

private fun doDecode(
    context: Context,
    source: InputStream,
    width: Int,
    height: Int,
    options: Options
): Resource<QMUILongGlidePhotoData> {
    val isThumb = options.get(QMUI_PHOTO_IMG_IS_THUMB) == true
    val bmOptions = BitmapFactory.Options()
    if (isThumb) {
        val bm = loadLongImageThumbnail(
            source,
            IntSize(width, height),
            bmOptions,
            options.get(DownsampleStrategy.OPTION) == DownsampleStrategy.CENTER_INSIDE
        )
        val drawable = BitmapDrawable(context.resources, bm)
        return SimpleResource(QMUILongGlidePhotoData(drawable))
    } else {
        val bitmapRegion = loadLongImage(
            source,
            IntSize(width, height),
            bmOptions,
            options.get(DownsampleStrategy.OPTION) == DownsampleStrategy.CENTER_INSIDE,
            preloadCount = 2
        )
        val drawable = QMUIBitmapRegionHolderDrawable(bitmapRegion)
        return SimpleResource(QMUILongGlidePhotoData(drawable))
    }
}

private class ByteBufferInputStream(val buf: ByteBuffer) : InputStream() {
    @Throws(IOException::class)
    override fun read(): Int {
        return if (!buf.hasRemaining()) {
            -1
        } else buf.get().toInt() and 0xFF
    }

    @Throws(IOException::class)
    override fun read(bytes: ByteArray, off: Int, len: Int): Int {
        if (!buf.hasRemaining()) {
            return -1
        }
        val toRead = len.coerceAtMost(buf.remaining())
        buf.get(bytes, off, toRead)
        return toRead
    }
}