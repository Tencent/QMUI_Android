package com.qmuiteam.qmuidemo.fragment.lab

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.BitmapFactoryDecoder
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.decode.ImageSource
import coil.fetch.SourceResult
import coil.imageLoader
import coil.request.*
import coil.size.Scale
import coil.size.pxOrElse
import com.qmuiteam.compose.core.ui.QMUITopBarBackIconItem
import com.qmuiteam.compose.core.ui.QMUITopBarTextItem
import com.qmuiteam.compose.core.ui.QMUITopBarWithLazyScrollState
import com.qmuiteam.photo.activity.QMUIPhotoPickerActivity
import com.qmuiteam.photo.compose.QMUIPhotoThumbnailWithViewer
import com.qmuiteam.photo.data.*
import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord
import com.qmuiteam.qmuidemo.R
import com.qmuiteam.qmuidemo.activity.QDPhotoPickerActivity
import com.qmuiteam.qmuidemo.base.ComposeBaseFragment
import com.qmuiteam.qmuidemo.lib.annotation.Widget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

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
                rightItems = arrayListOf(
                    QMUITopBarTextItem("Pick a Picture") {
                        val activity = activity ?: return@QMUITopBarTextItem
                        val intent = QMUIPhotoPickerActivity.intentOf(activity, QDPhotoPickerActivity::class.java)
                        startActivity(intent)
                    }
                )
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
                            activity = requireActivity(),
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
                            activity = requireActivity(),
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
                            activity = requireActivity(),
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
                            activity = requireActivity(),
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
                            activity = requireActivity(),
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
                            activity = requireActivity(),
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
                            activity = requireActivity(),
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
                            activity = requireActivity(),
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
        isContainerDimenExactly: Boolean,
        onSuccess: ((PhotoResult) -> Unit)?,
        onError: ((Throwable) -> Unit)?
    ) {
        if (isLongImage) {
            LongImage(onSuccess, onError)
        } else {
            val context = LocalContext.current
            val model = remember(context, url, onSuccess, onError) {
                ImageRequest.Builder(context)
                    .data(url)
                    .allowHardware(false)
                    .listener(onError = { _, result ->
                        onError?.invoke(result.throwable)
                    }) { _, result ->
                        onSuccess?.invoke(PhotoResult(url, result.drawable))
                    }.build()
            }
            AsyncImage(
                model = model,
                contentDescription = "",
                contentScale = if (isContainerDimenExactly) contentScale else ContentScale.Inside,
                alignment = Alignment.Center,
                modifier = Modifier.let {
                    if (isContainerDimenExactly) {
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
        onSuccess: ((PhotoResult) -> Unit)?,
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
        onSuccess: ((PhotoResult) -> Unit)?,
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
                        onSuccess?.invoke(PhotoResult(url, result.drawable))
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
        isContainerDimenExactly: Boolean,
        onSuccess: ((PhotoResult) -> Unit)?,
        onError: ((Throwable) -> Unit)?
    ) {
        if (isLongImage) {
            LongImage(onSuccess, onError)
        } else {
            val context = LocalContext.current
            val model = remember(context, url, onSuccess, onError) {
                ImageRequest.Builder(context)
                    .data(url)
                    .allowHardware(false)
                    .listener(onError = { _, result ->
                        onError?.invoke(result.throwable)
                    }) { _, result ->
                        onSuccess?.invoke(PhotoResult(url, result.drawable))
                    }.build()
            }
            AsyncImage(
                model = model,
                contentDescription = "",
                contentScale = contentScale,
                alignment = Alignment.Center,
                modifier = Modifier.let {
                    if (isContainerDimenExactly) {
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
        onSuccess: ((PhotoResult) -> Unit)?,
        onError: ((Throwable) -> Unit)?
    ) {
        var images by remember {
            mutableStateOf(emptyList<QMUIBitmapRegionProvider>())
        }
        val context = LocalContext.current
        LaunchedEffect(key1 = "") {
            val result = withContext(Dispatchers.IO) {
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .setParameter("isLongImage", true)
                    .build()
                context.imageLoader.execute(request)
            }
            if (result is SuccessResult) {
                (result.drawable as? LongImageDrawableHolder)?.bitmapRegion?.let {
                    images = it.list
                }
                onSuccess?.invoke(PhotoResult(url, result.drawable))
            } else if (result is ErrorResult) {
                onError?.invoke(result.throwable)
            }
        }
        if (images.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(images) { image ->
                    BoxWithConstraints() {
                        val width = constraints.maxWidth
                        val height = width * image.height / image.width
                        val heightDp = with(LocalDensity.current) {
                            height.toDp()
                        }
                        LongImageItem(image, maxWidth, heightDp)
                    }
                }
            }
        }
    }
}


@Composable
fun LongImageItem(bmRegion: QMUIBitmapRegionProvider, w: Dp, h: Dp) {
    var bitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }
    LaunchedEffect(key1 = bmRegion) {
        withContext(Dispatchers.IO) {
            bitmap = bmRegion.loader.load()
        }
    }
    Box(modifier = Modifier.size(w, h)) {
        val bm = bitmap
        if (bm != null) {
            Image(
                painter = BitmapPainter(bm.asImageBitmap()),
                contentDescription = "",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxSize()
            )
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
        return ratio > 0 && ratio < 0.2f
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
                drawable = LongImageDrawableHolder(bitmapRegion),
                isSampled = bmOptions.inSampleSize > 1 || bmOptions.inScaled
            )
        }
    }
}

class LongImageDrawableHolder(val bitmapRegion: QMUIBitmapRegion) : Drawable() {

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