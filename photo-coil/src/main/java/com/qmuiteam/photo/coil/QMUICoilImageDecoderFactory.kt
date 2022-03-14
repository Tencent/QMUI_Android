package com.qmuiteam.photo.coil

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.unit.IntSize
import coil.ImageLoader
import coil.decode.BitmapFactoryDecoder
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.decode.ImageSource
import coil.fetch.SourceResult
import coil.request.Options
import coil.request.get
import coil.size.Scale
import coil.size.pxOrElse
import com.qmuiteam.photo.data.QMUIBitmapRegionHolderDrawable
import com.qmuiteam.photo.data.loadLongImage
import com.qmuiteam.photo.data.loadLongImageThumbnail
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class QMUICoilImageDecoderFactory(maxParallelism: Int = 4) : Decoder.Factory {

    companion object {
        val defaultInstance by lazy {
            QMUICoilImageDecoderFactory()
        }
    }

    private val parallelismLock = Semaphore(maxParallelism)

    override fun create(result: SourceResult, options: Options, imageLoader: ImageLoader): Decoder? {
        return if ((options.parameters["isLongImage"] as? Boolean) == true) {
            QMUICoilLongImageDecoder(result.source, options, parallelismLock)
        } else {
            BitmapFactoryDecoder(result.source, options, parallelismLock)
        }
    }
}


class QMUICoilLongImageDecoder(
    private val source: ImageSource,
    private val options: Options,
    private val parallelismLock: Semaphore = Semaphore(Int.MAX_VALUE)
) : Decoder {

    private val isThumb = options.parameters["isThumb"] == true

    override suspend fun decode(): DecodeResult = parallelismLock.withPermit {
        runInterruptible { decode(BitmapFactory.Options()) }
    }


    private fun decode(bmOptions: BitmapFactory.Options): DecodeResult {
        val ins = source.source().inputStream()
        val (width, height) = options.size
        val dstWidth = width.pxOrElse { -1 }
        val dstHeight = height.pxOrElse { -1 }
        if (isThumb) {
            val bm = loadLongImageThumbnail(ins, IntSize(dstWidth, dstHeight), bmOptions, options.scale == Scale.FIT)
            return DecodeResult(
                drawable = BitmapDrawable(options.context.resources, bm),
                isSampled = bmOptions.inSampleSize > 1
            )
        } else {
            val bitmapRegion = loadLongImage(
                ins,
                IntSize(dstWidth, dstHeight),
                bmOptions,
                options.scale == Scale.FIT,
                preloadCount = 2
            )
            return DecodeResult(
                drawable = QMUIBitmapRegionHolderDrawable(bitmapRegion),
                isSampled = bmOptions.inSampleSize > 1 || bmOptions.inScaled
            )
        }
    }
}