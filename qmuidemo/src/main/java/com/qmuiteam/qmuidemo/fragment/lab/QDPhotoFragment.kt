package com.qmuiteam.qmuidemo.fragment.lab

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.*
import coil.fetch.SourceResult
import coil.imageLoader
import coil.request.*
import coil.size.Scale
import coil.size.pxOrElse
import com.qmuiteam.compose.ui.QMUITopBarBackIconItem
import com.qmuiteam.compose.ui.QMUITopBarWithLazyScrollState
import com.qmuiteam.photo.compose.QMUIPhotoThumbnailWithViewer
import com.qmuiteam.photo.data.PhotoTransitionProviderRecover
import com.qmuiteam.photo.data.QMUIPhoto
import com.qmuiteam.photo.data.QMUIPhotoProvider
import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord
import com.qmuiteam.qmuidemo.R
import com.qmuiteam.qmuidemo.base.ComposeBaseFragment
import com.qmuiteam.qmuidemo.lib.annotation.Widget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Widget(name = "QMUI Photo", iconRes = R.mipmap.icon_grid_in_progress)
@LatestVisitRecord
class QDPhotoFragment : ComposeBaseFragment() {

    @Composable
    override fun PageContent() {
        Column(modifier = Modifier.fillMaxSize()) {
            val scrollState = rememberLazyListState()
            QMUITopBarWithLazyScrollState(
                scrollState = scrollState,
                title = "QMUIPhoto",
                leftItems = arrayListOf(
                    QMUITopBarBackIconItem {
                        popBackStack()
                    }
                ),
            )
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White),
                contentPadding = PaddingValues(start = 44.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        QMUIPhotoThumbnailWithViewer(
                            requireActivity(),
                            images = listOf(
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9979/31y68oGufDGL3zQ6TT.jpg",
                                    1f
                                )
                            )
                        )
                    }

                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        QMUIPhotoThumbnailWithViewer(
                            requireActivity(),
                            images = listOf(
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png",
                                    1.379f
                                )
                            )
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        QMUIPhotoThumbnailWithViewer(
                            requireActivity(),
                            images = listOf(
                                CoilPhotoProvider(
                                    "file:///android_asset/test.png",
                                    0.0125f
                                )
                            )
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        QMUIPhotoThumbnailWithViewer(
                            requireActivity(),
                            images = listOf(
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg",
                                    0.749f
                                )
                            )
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        QMUIPhotoThumbnailWithViewer(
                            requireActivity(),
                            images = listOf(
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg",
                                    0.749f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png",
                                    1.379f
                                )
                            )
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        QMUIPhotoThumbnailWithViewer(
                            requireActivity(),
                            images = listOf(
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg",
                                    0.749f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png",
                                    1.379f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9979/31y68oGufDGL3zQ6TT.jpg",
                                    1f
                                )
                            )
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        QMUIPhotoThumbnailWithViewer(
                            requireActivity(),
                            images = listOf(
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg",
                                    0.749f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png",
                                    1.379f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9979/31y68oGufDGL3zQ6TT.jpg",
                                    1f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg",
                                    0.749f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png",
                                    1.379f
                                ),
                            )
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        QMUIPhotoThumbnailWithViewer(
                            requireActivity(),
                            images = listOf(
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg",
                                    0.749f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png",
                                    1.379f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9979/31y68oGufDGL3zQ6TT.jpg",
                                    1f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg",
                                    0.749f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png",
                                    1.379f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg",
                                    0.749f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png",
                                    1.379f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg",
                                    0.749f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9979/31y68oGufDGL3zQ6TT.jpg",
                                    1f
                                ),
                            )
                        )
                    }
                }
            }
        }
    }
}


class CoilThumbPhoto(val url: String, val isLongImage: Boolean) : QMUIPhoto {
    @Composable
    override fun Compose(
        contentScale: ContentScale,
        isContainerFixed: Boolean,
        onSuccess: ((Drawable) -> Unit)?,
        onError: ((Throwable) -> Unit)?
    ) {
        if (isLongImage) {
            LongImage(onSuccess, onError)
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .allowHardware(false)
                    .listener(onError = { _, result ->
                        onError?.invoke(result.throwable)
                    }) { _, result ->
                        onSuccess?.invoke(result.drawable)
                    }.build(),
                contentDescription = "",
                contentScale = contentScale,
                alignment = Alignment.Center,
                modifier = Modifier.let {
                    if (isContainerFixed) {
                        it.fillMaxSize()
                    } else {
                        it
                    }
                }
            )
        }

    }

    @Composable
    fun LongImage(
        onSuccess: ((Drawable) -> Unit)?,
        onError: ((Throwable) -> Unit)?
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val request = ImageRequest.Builder(LocalContext.current)
                .setParameter("isThumb", true)
                .setParameter("isLongImage", true)
                .data(url)
                .scale(Scale.FILL)
                .size(constraints.maxWidth, constraints.maxHeight)
                .build()
            LongImageContent(request, onSuccess, onError)
        }

    }

    @Composable
    fun LongImageContent(
        request: ImageRequest,
        onSuccess: ((Drawable) -> Unit)?,
        onError: ((Throwable) -> Unit)?
    ) {
        val imageLoader = LocalContext.current.imageLoader
        var bitmap by remember("") {
            mutableStateOf<Bitmap?>(null)
        }
        LaunchedEffect("") {
            withContext(Dispatchers.IO) {
                val result = imageLoader.execute(request)
                if (result is SuccessResult) {
                    bitmap = result.drawable.toBitmap()
                    withContext(Dispatchers.Main) {
                        onSuccess?.invoke(result.drawable)
                    }
                } else if (result is ErrorResult) {
                    withContext(Dispatchers.Main) {
                        onError?.invoke(result.throwable)
                    }
                }
            }
        }
        val bm = bitmap
        if (bm != null) {
            Image(
                painter = BitmapPainter(bm.asImageBitmap()),
                contentDescription = "",
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxSize()
            )
        }

    }
}

class CoilPhoto(val url: String, val isLongImage: Boolean) : QMUIPhoto {

    @Composable
    override fun Compose(
        contentScale: ContentScale,
        isContainerFixed: Boolean,
        onSuccess: ((Drawable) -> Unit)?,
        onError: ((Throwable) -> Unit)?
    ) {
        if (isLongImage) {
            LongImage(onSuccess, onError)
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .allowHardware(false)
                    .listener(onError = { _, result ->
                        onError?.invoke(result.throwable)
                    }) { _, result ->
                        onSuccess?.invoke(result.drawable)
                    }.build(),
                contentDescription = "",
                contentScale = contentScale,
                alignment = Alignment.Center,
                modifier = Modifier.let {
                    if (isContainerFixed) {
                        it.fillMaxSize()
                    } else {
                        it
                    }
                }
            )
        }
    }

    @Composable
    fun LongImage(
        onSuccess: ((Drawable) -> Unit)?,
        onError: ((Throwable) -> Unit)?
    ) {
        var images by remember {
            mutableStateOf(emptyList<Bitmap>())
        }
        val context = LocalContext.current
        LaunchedEffect("") {
            launch {
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .setParameter("isLongImage", true)
                    .build()
                val result = context.imageLoader.execute(request)
                if (result is SuccessResult) {
                    (result.drawable as? LongImageDrawableHolder)?.bms?.let {
                        images = it
                    }
                    onSuccess?.invoke(result.drawable)
                } else if (result is ErrorResult) {
                    onError?.invoke(result.throwable)
                }
            }
        }
        if (images.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(images) { image ->
                    Image(
                        painter = BitmapPainter(image.asImageBitmap()),
                        contentDescription = "",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                }
            }
        }
    }

}

class CoilPhotoProvider(val url: String, val ratio: Float) : QMUIPhotoProvider {

    override fun thumbnail(): QMUIPhoto? {
        return CoilThumbPhoto(url, isLongImage())
    }

    override fun photo(): QMUIPhoto? {
        return CoilPhoto(url, isLongImage())
    }

    override fun ratio(): Float {
        return ratio
    }

    override fun isLongImage(): Boolean {
        return ratio < 0.2f
    }

    override fun meta(): Bundle? {
        return null
    }

    override fun recoverCls(): Class<in PhotoTransitionProviderRecover>? {
        return null
    }
}


class ImageDecoderFactory(val maxParallelism: Int = 4) : Decoder.Factory {

    private val parallelismLock = Semaphore(maxParallelism)

    override fun create(result: SourceResult, options: Options, imageLoader: ImageLoader): Decoder? {
        return if ((options.parameters["isLongImage"] as? Boolean) == true) {
            LongImageDecoder(result.source, options, parallelismLock)
        } else {
            BitmapFactoryDecoder(result.source, options, parallelismLock)
        }
    }
}

class LongImageDecoder(
    private val source: ImageSource,
    private val options: Options,
    private val parallelismLock: Semaphore = Semaphore(Int.MAX_VALUE)
) : Decoder {

    private val isThumb = options.parameters["isThumb"] == true

    override suspend fun decode(): DecodeResult = parallelismLock.withPermit {
        runInterruptible { BitmapFactory.Options().decode() }
    }


    private fun BitmapFactory.Options.decode(): DecodeResult {
        val bufferedSource = source.source()

        // Read the image's dimensions.
        inJustDecodeBounds = true
        BitmapFactory.decodeStream(bufferedSource.peek().inputStream(), null, this)
        inJustDecodeBounds = false



        inPreferredConfig = Bitmap.Config.ARGB_8888
        inPremultiplied = options.premultipliedAlpha

        if (Build.VERSION.SDK_INT >= 26 && options.colorSpace != null) {
            inPreferredColorSpace = options.colorSpace
        }

        // Always create immutable bitmaps as they have performance benefits.
        inMutable = false

        if (outWidth > 0 && outHeight > 0) {
            val (width, height) = options.size
            val dstWidth = width.pxOrElse { outWidth }
            val dstHeight = height.pxOrElse { outHeight }
            inSampleSize = DecodeUtils.calculateInSampleSize(
                srcWidth = outWidth,
                srcHeight = outHeight,
                dstWidth = dstWidth,
                dstHeight = dstHeight,
                scale = options.scale
            )

            // Calculate the image's density scaling multiple.
            var scale = DecodeUtils.computeSizeMultiplier(
                srcWidth = outWidth / inSampleSize.toDouble(),
                srcHeight = outHeight / inSampleSize.toDouble(),
                dstWidth = dstWidth.toDouble(),
                dstHeight = dstHeight.toDouble(),
                scale = options.scale
            )

            // Only upscale the image if the options require an exact size.
            if (options.allowInexactSize) {
                scale = scale.coerceAtMost(1.0)
            }

            inScaled = scale != 1.0
            if (inScaled) {
                if (scale > 1) {
                    // Upscale
                    inDensity = (Int.MAX_VALUE / scale).roundToInt()
                    inTargetDensity = Int.MAX_VALUE
                } else {
                    // Downscale
                    inDensity = Int.MAX_VALUE
                    inTargetDensity = (Int.MAX_VALUE * scale).roundToInt()
                }
            }
        } else {
            // This occurs if there was an error decoding the image's size.
            inSampleSize = 1
            inScaled = false
        }

        val regionDecoder = BitmapRegionDecoder.newInstance(bufferedSource.inputStream(), false)
        checkNotNull(regionDecoder) {
            "BitmapRegionDecoder newInstance failed."
        }
        val w = regionDecoder.width
        val h = regionDecoder.height
        if (isThumb) {
            val outBitmap = regionDecoder.decodeRegion(Rect(0, 0, w, w * 3), this)

            // Fix the incorrect density created by overloading inDensity/inTargetDensity.
            outBitmap.density = options.context.resources.displayMetrics.densityDpi

            return DecodeResult(
                drawable = BitmapDrawable(options.context.resources, outBitmap),
                isSampled = inSampleSize > 1 || inScaled
            )
        } else {
            val ret = arrayListOf<Bitmap>()
            var start = 0
            while (start < h) {
                val end = (start + w * 3).coerceAtMost(h)
                val outBitmap = regionDecoder.decodeRegion(Rect(0, start, w, end), this)

                // Fix the incorrect density created by overloading inDensity/inTargetDensity.
                outBitmap.density = options.context.resources.displayMetrics.densityDpi
                ret.add(outBitmap)
                start = end
            }
            return DecodeResult(
                drawable = LongImageDrawableHolder(ret, w, h),
                isSampled = inSampleSize > 1 || inScaled
            )
        }
    }
}

class LongImageDrawableHolder(val bms: List<Bitmap>, val w: Int, val h: Int) : Drawable() {

    override fun getIntrinsicHeight(): Int {
        return h
    }

    override fun getIntrinsicWidth(): Int {
        return w
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