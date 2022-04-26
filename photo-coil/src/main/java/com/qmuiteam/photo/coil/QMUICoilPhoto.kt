package com.qmuiteam.photo.coil

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.compose.AsyncImageContent
import coil.compose.AsyncImagePainter
import coil.imageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Scale
import com.qmuiteam.photo.compose.BlankBox
import com.qmuiteam.photo.compose.QMUIBitmapRegionItem
import com.qmuiteam.photo.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class QMUICoilThumbPhoto(
    val uri: Uri,
    val isLongImage: Boolean,
    val openBlankColor: Boolean
) : QMUIPhoto {
    @Composable
    override fun Compose(
        contentScale: ContentScale,
        isContainerDimenExactly: Boolean,
        onSuccess: ((PhotoResult) -> Unit)?,
        onError: ((Throwable) -> Unit)?
    ) {
        if (isLongImage) {
            LongImage(onSuccess, onError, openBlankColor)
        } else {
            val context = LocalContext.current
            val model = remember(context, uri, onSuccess, onError) {
                ImageRequest.Builder(context)
                    .data(uri)
                    .allowHardware(false)
                    .crossfade(true)
                    .decoderFactory(QMUICoilImageDecoderFactory.defaultInstance)
                    .listener(onError = { _, result ->
                        onError?.invoke(result.throwable)
                    }) { _, result ->
                        onSuccess?.invoke(PhotoResult(uri, result.drawable))
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
            ) { state ->
                if (state == AsyncImagePainter.State.Empty || state is AsyncImagePainter.State.Loading) {
                    if (isContainerDimenExactly && openBlankColor) {
                        BlankBox()
                    }
                } else {
                    AsyncImageContent()
                }
            }
        }

    }

    @Composable
    fun LongImage(
        onSuccess: ((PhotoResult) -> Unit)?,
        onError: ((Throwable) -> Unit)?,
        openBlankColor: Boolean
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val request = ImageRequest.Builder(LocalContext.current)
                .allowHardware(false)
                .setParameter("isThumb", true)
                .setParameter("isLongImage", true)
                .crossfade(true)
                .decoderFactory(QMUICoilImageDecoderFactory.defaultInstance)
                .data(uri)
                .scale(Scale.FILL)
                .size(constraints.maxWidth, constraints.maxHeight)
                .build()
            LongImageContent(request, onSuccess, onError, openBlankColor)
        }

    }

    @Composable
    fun LongImageContent(
        request: ImageRequest,
        onSuccess: ((PhotoResult) -> Unit)?,
        onError: ((Throwable) -> Unit)?,
        openBlankColor: Boolean
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
                        onSuccess?.invoke(PhotoResult(uri, result.drawable))
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
        } else if (openBlankColor) {
            BlankBox()
        }

    }
}


class QMUICoilPhoto(
    val uri: Uri,
    val isLongImage: Boolean
) : QMUIPhoto {

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
            val model = remember(context, uri, onSuccess, onError) {
                ImageRequest.Builder(context)
                    .data(uri)
                    .allowHardware(false)
                    .crossfade(true)
                    .decoderFactory(QMUICoilImageDecoderFactory.defaultInstance)
                    .listener(onError = { _, result ->
                        onError?.invoke(result.throwable)
                    }) { _, result ->
                        onSuccess?.invoke(PhotoResult(uri, result.drawable))
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
                    .data(uri)
                    .crossfade(true)
                    .setParameter("isLongImage", true)
                    .decoderFactory(QMUICoilImageDecoderFactory.defaultInstance)
                    .build()
                context.imageLoader.execute(request)
            }
            if (result is SuccessResult) {
                (result.drawable as? QMUIBitmapRegionHolderDrawable)?.bitmapRegion?.let {
                    images = it.list
                }
                onSuccess?.invoke(PhotoResult(uri, result.drawable))
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
                        QMUIBitmapRegionItem(image, maxWidth, heightDp)
                    }
                }
            }
        }
    }
}


open class QMUICoilPhotoProvider(
    val uri: Uri,
    val thumbUri: Uri,
    val ratio: Float
) : QMUIPhotoProvider {

    companion object {
        const val META_URI_KEY = "meta_uri"
        const val META_THUMB_URI_KEY = "meta_thumb_uri"
        const val META_RATIO_KEY = "meta_ratio"
    }

    constructor(uri: Uri, ratio: Float) : this(uri, uri, ratio)


    override fun thumbnail(openBlankColor: Boolean): QMUIPhoto? {
        return QMUICoilThumbPhoto(thumbUri, isLongImage(), openBlankColor)
    }

    override fun photo(): QMUIPhoto? {
        return QMUICoilPhoto(uri, isLongImage())
    }

    override fun ratio(): Float {
        return ratio
    }

    override fun isLongImage(): Boolean {
        return ratio > 0 && ratio < 0.2f
    }

    override fun meta(): Bundle? {
        return Bundle().apply {
            putParcelable(META_URI_KEY, uri)
            if(thumbUri != uri){
                putParcelable(META_THUMB_URI_KEY, thumbUri)
            }
            putParcelable(META_THUMB_URI_KEY, thumbUri)
            putFloat(META_RATIO_KEY, ratio)
        }
    }

    override fun recoverCls(): Class<out PhotoTransitionProviderRecover>? {
        return QMUICoilPhotoTransitionProviderRecover::class.java
    }
}

class QMUICoilPhotoTransitionProviderRecover : PhotoTransitionProviderRecover {
    override fun recover(bundle: Bundle): QMUIPhotoTransitionInfo? {
        val uri = bundle.getParcelable<Uri>(QMUICoilPhotoProvider.META_URI_KEY) ?: return null
        val thumbUri = bundle.getParcelable<Uri>(QMUICoilPhotoProvider.META_THUMB_URI_KEY) ?: uri
        val ratio = bundle.getFloat(QMUICoilPhotoProvider.META_RATIO_KEY)
        return QMUIPhotoTransitionInfo(
            QMUICoilPhotoProvider(uri, thumbUri, ratio),
            null,
            null,
            null
        )
    }
}