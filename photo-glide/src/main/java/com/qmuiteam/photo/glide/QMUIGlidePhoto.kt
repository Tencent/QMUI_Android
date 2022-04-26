package com.qmuiteam.photo.glide

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.qmuiteam.photo.compose.BlankBox
import com.qmuiteam.photo.compose.QMUIBitmapRegionItem
import com.qmuiteam.photo.compose.QMUILocalPhotoConfig
import com.qmuiteam.photo.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
private fun GlideImage(
    uri: Uri,
    isLongImage: Boolean,
    isThumbImage: Boolean,
    isContainerDimenExactly: Boolean,
    onSuccess: ((PhotoResult) -> Unit)?,
    onError: (() -> Unit)?,
    contentDescription: String = "",
    contentScale: ContentScale = ContentScale.Fit,
    openBlankColor: Boolean = false
) {
    BoxWithConstraints(modifier = if (isContainerDimenExactly) Modifier.fillMaxSize() else Modifier) {
        val state = remember(uri) {
            mutableStateOf<Pair<Long, Drawable?>?>(null)
        }
        val context = LocalContext.current
        Log.i("cginetest", "1. $constraints")
        DisposableEffect(uri, isContainerDimenExactly, constraints.isZero,isLongImage, isThumbImage, contentScale) {
            val key = SystemClock.elapsedRealtime()
            val request = when {
                constraints.isZero ->  null
                isLongImage -> {
                    Glide.with(context).`as`(QMUILongGlidePhotoData::class.java).load(uri)
                        .downsample(DownsampleStrategy.CENTER_OUTSIDE)
                        .dontTransform()
                        .set(QMUI_PHOTO_IMG_IS_THUMB, isThumbImage)
                        .into(object : CustomTarget<QMUILongGlidePhotoData>(
                            constraints.maxWidth,
                            constraints.maxHeight
                        ) {

                            override fun onResourceReady(resource: QMUILongGlidePhotoData, transition: Transition<in QMUILongGlidePhotoData>?) {
                                state.value = key to resource.drawable
                                onSuccess?.invoke(PhotoResult(uri, resource.drawable))
                            }


                            override fun onLoadStarted(placeholder: Drawable?) {
                                if (placeholder != null || state.value?.first == key) {
                                    state.value = -1L to placeholder
                                }
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                if (state.value?.first == key) {
                                    state.value = -1L to placeholder
                                }
                            }

                            override fun onLoadFailed(errorDrawable: Drawable?) {
                                onError?.invoke()
                            }
                        })
                        .request
                }
                else -> {
                    Glide.with(context).load(uri)
                        .downsample(DownsampleStrategy.AT_LEAST)
                        .dontTransform()
                        .into(object : CustomTarget<Drawable>(
                            constraints.maxWidth,
                            constraints.maxHeight
                        ) {

                            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                state.value = key to resource
                                onSuccess?.invoke(PhotoResult(uri, resource))
                            }


                            override fun onLoadStarted(placeholder: Drawable?) {
                                if (placeholder != null || state.value?.first == key) {
                                    state.value = -1L to placeholder
                                }
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                if (state.value?.first == key) {
                                    state.value = -1L to placeholder
                                }
                            }

                            override fun onLoadFailed(errorDrawable: Drawable?) {
                                onError?.invoke()
                            }
                        })
                        .request
                }
            }

            onDispose {
                request?.clear()
            }
        }
        val currentDrawable = state.value?.second
        if (currentDrawable != null) {
            if (currentDrawable is QMUIBitmapRegionHolderDrawable) {
                LongImageContent(currentDrawable)
            } else {
                Image(
                    modifier = if (isContainerDimenExactly) {
                        Modifier.fillMaxSize()
                    } else Modifier,
                    contentDescription = contentDescription,
                    painter = BitmapPainter(currentDrawable.toBitmap().asImageBitmap()),
                    contentScale = contentScale,
                )
            }

        } else if (isContainerDimenExactly && openBlankColor) {
            BlankBox()
        }
    }
}

@Composable
private fun LongImageContent(drawable: QMUIBitmapRegionHolderDrawable) {
    val images by remember(drawable) {
        mutableStateOf(drawable.bitmapRegion.list)
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

open class QMUIGlideThumbPhoto(
    val uri: Uri,
    val isLongImage: Boolean,
    val openBlankColor: Boolean = true,
) : QMUIPhoto {
    @Composable
    override fun Compose(
        contentScale: ContentScale,
        isContainerDimenExactly: Boolean,
        onSuccess: ((PhotoResult) -> Unit)?,
        onError: ((Throwable) -> Unit)?
    ) {
        GlideImage(
            uri,
            isLongImage,
            true,
            isContainerDimenExactly,
            onSuccess,
            onError = {
                onError?.invoke(RuntimeException("glide failed to load thumb image."))
            },
            contentScale = contentScale,
            openBlankColor = openBlankColor
        )
    }
}


class QMUIGlidePhoto(
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
        GlideImage(
            uri,
            isLongImage,
            false,
            isContainerDimenExactly,
            onSuccess,
            onError = {
                onError?.invoke(RuntimeException("glide failed to load thumb image."))
            },
            contentScale = contentScale
        )
    }
}

open class QMUIGlidePhotoProvider(val uri: Uri, val thumbUrl: Uri, val ratio: Float) : QMUIPhotoProvider {

    companion object {
        const val META_URI_KEY = "meta_uri"
        const val META_THUMB_URI_KEY = "meta_thumb_uri"
        const val META_RATIO_KEY = "meta_ratio"
    }

    constructor(uri: Uri, ratio: Float): this(uri, uri, ratio)

    override fun thumbnail(openBlankColor: Boolean): QMUIPhoto? {
        return QMUIGlideThumbPhoto(thumbUrl, isLongImage(), openBlankColor)
    }

    override fun photo(): QMUIPhoto? {
        return QMUIGlidePhoto(uri, isLongImage())
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
            if(thumbUrl != uri){
                putParcelable(META_THUMB_URI_KEY, thumbUrl)
            }
            putFloat(META_RATIO_KEY, ratio)
        }
    }

    override fun recoverCls(): Class<out PhotoTransitionProviderRecover>? {
        return QMUIGlidePhotoTransitionProviderRecover::class.java
    }
}

class QMUIGlidePhotoTransitionProviderRecover : PhotoTransitionProviderRecover {
    override fun recover(bundle: Bundle): QMUIPhotoTransitionInfo? {
        val uri = bundle.getParcelable<Uri>(QMUIGlidePhotoProvider.META_URI_KEY) ?: return null
        val thumbUri = bundle.getParcelable<Uri>(QMUIGlidePhotoProvider.META_THUMB_URI_KEY) ?: uri
        val ratio = bundle.getFloat(QMUIGlidePhotoProvider.META_RATIO_KEY)
        return QMUIPhotoTransitionInfo(
            QMUIGlidePhotoProvider(uri, thumbUri, ratio),
            null,
            null,
            null
        )
    }
}