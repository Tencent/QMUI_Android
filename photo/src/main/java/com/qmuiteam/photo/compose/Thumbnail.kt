package com.qmuiteam.photo.compose

import android.graphics.drawable.Drawable
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.qmuiteam.photo.activity.QMUIPhotoViewerActivity
import com.qmuiteam.photo.data.QMUIPhoto
import com.qmuiteam.photo.data.QMUIPhotoProvider
import com.qmuiteam.photo.data.QMUIPhotoTransition

class QMUIPhotoThumbnailConfig(
    val singleSquireImageWidthRatio: Float = 0.5f,
    val singleWideImageMaxWidthRatio: Float = 0.667f,
    val singleHighImageDefaultWidthRatio: Float = 0.5f,
    val singleHighImageMaxHeight: Dp = 320.dp,
    val singleLongImageWidthRatio: Float = 0.5f,
    val isLongImageIfRatioLessThan: Float = 0.2f,
    val longImageShowTopRatio: Float = 0.25f,
    val averageIfTwoImage: Boolean = true,
    val horGap: Dp = 5.dp,
    val verGap: Dp = 5.dp,
    val alphaWhenPressed: Float = 0.5f
)

val qmuiDefaultPhotoThumbnailConfig = QMUIPhotoThumbnailConfig()

@Composable
private fun QMUIPhotoThumbnailItem(
    thumb: QMUIPhoto?,
    width: Dp,
    height: Dp,
    alphaWhenPressed: Float,
    isContainerFixed: Boolean,
    isLongImage: Boolean,
    onLayout: (offset: Offset, size: IntSize) -> Unit,
    onPhotoLoaded: (Drawable) -> Unit,
    click: (() -> Unit)?,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    Box(modifier = Modifier
        .width(width)
        .height(height)
        .let {
            if (click != null) {
                it
                    .clickable(interactionSource, null) {
                        click.invoke()
                    }
                    .alpha(if (isPressed.value) alphaWhenPressed else 1f)
            } else {
                it
            }
        }
        .onGloballyPositioned {
            onLayout(it.positionInWindow(), it.size)
        }
    ) {
        thumb?.Compose(
            contentScale = ContentScale.Crop,
            isContainerFixed = isContainerFixed,
            isLongImage = isLongImage,
            onSuccess = {
                onPhotoLoaded(it)
            },
            onError = null
        )
    }
}


@Composable
fun QMUIPhotoThumbnailWithViewer(
    activity: ComponentActivity,
    images: List<QMUIPhotoProvider>,
    config: QMUIPhotoThumbnailConfig = remember { qmuiDefaultPhotoThumbnailConfig }
){
    QMUIPhotoThumbnail(images, config){ list, index ->
        val intent = QMUIPhotoViewerActivity.intentOf(activity, QMUIPhotoViewerActivity::class.java, list, index)
        activity.startActivity(intent)
        activity.overridePendingTransition(0, 0)
    }
}

@Composable
fun QMUIPhotoThumbnail(
    images: List<QMUIPhotoProvider>,
    config: QMUIPhotoThumbnailConfig = remember { qmuiDefaultPhotoThumbnailConfig },
    onClick: ((images: List<QMUIPhotoTransition>, index: Int) -> Unit)? = null
) {
    if (images.size < 0) {
        return
    }
    val renderInfo = remember {
        Array(images.size) {
            QMUIPhotoTransition(images[it], null, null, null)
        }
    }
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (images.size == 1) {
            val image = images[0]
            val thumb = image.thumbnail()
            if (thumb != null) {
                val ratio = image.ratio()
                when {
                    ratio <= 0 -> {
                        QMUIPhotoThumbnailItem(
                            thumb,
                            Dp.Unspecified,
                            Dp.Unspecified,
                            config.alphaWhenPressed,
                            isContainerFixed = false,
                            isLongImage = false,
                            onLayout = { offset, size ->
                                renderInfo[0].offsetInWindow = offset
                                renderInfo[0].size = size
                            },
                            onPhotoLoaded = {
                                renderInfo[0].photo = it
                            },
                            click = if (onClick != null) {
                                {
                                    onClick.invoke(renderInfo.toList(), 0)
                                }
                            } else null
                        )
                    }
                    ratio == 1f -> {
                        val wh = maxWidth * config.singleSquireImageWidthRatio
                        QMUIPhotoThumbnailItem(
                            thumb,
                            wh,
                            wh,
                            config.alphaWhenPressed,
                            isContainerFixed = true,
                            isLongImage = false,
                            onLayout = { offset, size ->
                                renderInfo[0].offsetInWindow = offset
                                renderInfo[0].size = size
                            },
                            onPhotoLoaded = {
                                renderInfo[0].photo = it
                            },
                            click = if (onClick != null) {
                                {
                                    onClick.invoke(renderInfo.toList(), 0)
                                }
                            } else null
                        )
                    }
                    ratio > 1f -> {
                        val width = maxWidth * config.singleWideImageMaxWidthRatio
                        val height = width / ratio
                        QMUIPhotoThumbnailItem(
                            thumb,
                            width,
                            height,
                            config.alphaWhenPressed,
                            isContainerFixed = true,
                            isLongImage = false,
                            onLayout = { offset, size ->
                                renderInfo[0].offsetInWindow = offset
                                renderInfo[0].size = size
                            },
                            onPhotoLoaded = {
                                renderInfo[0].photo = it
                            },
                            click = if (onClick != null) {
                                {
                                    onClick.invoke(renderInfo.toList(), 0)
                                }
                            } else null
                        )
                    }
                    ratio <= config.isLongImageIfRatioLessThan -> {
                        val width = maxWidth * config.singleLongImageWidthRatio
                        val height = width / config.longImageShowTopRatio
                        QMUIPhotoThumbnailItem(
                            thumb,
                            width,
                            height,
                            config.alphaWhenPressed,
                            isContainerFixed = true,
                            isLongImage = true,
                            onLayout = { offset, size ->
                                renderInfo[0].offsetInWindow = offset
                                renderInfo[0].size = size
                            },
                            onPhotoLoaded = {
                                renderInfo[0].photo = it
                            },
                            click = if (onClick != null) {
                                {
                                    onClick.invoke(renderInfo.toList(), 0)
                                }
                            } else null
                        )
                    }
                    else -> {
                        var width = maxWidth * config.singleHighImageDefaultWidthRatio
                        var height = width / ratio
                        if (height > config.singleHighImageMaxHeight) {
                            height = config.singleHighImageMaxHeight
                            width = height * ratio
                        }
                        QMUIPhotoThumbnailItem(
                            thumb,
                            width,
                            height,
                            config.alphaWhenPressed,
                            isContainerFixed = true,
                            isLongImage = false,
                            onLayout = { offset, size ->
                                renderInfo[0].offsetInWindow = offset
                                renderInfo[0].size = size
                            },
                            onPhotoLoaded = {
                                renderInfo[0].photo = it
                            },
                            click = if (onClick != null) {
                                {
                                    onClick.invoke(renderInfo.toList(), 0)
                                }
                            } else null
                        )
                    }
                }
            }
        } else if (images.size == 2 && config.averageIfTwoImage) {
            RowImages(images, renderInfo, config, maxWidth, 2, 0, onClick)
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                for (i in 0 until (images.size / 3 + if (images.size % 3 > 0) 1 else 0).coerceAtMost(
                    3
                )) {
                    if (i > 0) {
                        Spacer(modifier = Modifier.height(config.verGap))
                    }
                    RowImages(
                        images,
                        renderInfo,
                        config,
                        this@BoxWithConstraints.maxWidth,
                        3,
                        i * 3,
                        onClick
                    )
                }
            }
        }
    }
}

@Composable
fun RowImages(
    images: List<QMUIPhotoProvider>,
    renderInfo: Array<QMUIPhotoTransition>,
    config: QMUIPhotoThumbnailConfig,
    containerWidth: Dp,
    rowCount: Int,
    startIndex: Int,
    onClick: ((images: List<QMUIPhotoTransition>, index: Int) -> Unit)?
) {
    val wh = (containerWidth - config.horGap * (rowCount - 1)) / rowCount
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(wh)
    ) {
        for (i in startIndex until (startIndex + rowCount).coerceAtMost(images.size)) {
            if (i != startIndex) {
                Spacer(modifier = Modifier.width(config.horGap))
            }
            val image = images[i]
            val ratio = image.ratio()
            val isLongImage = ratio > 0f && ratio < config.isLongImageIfRatioLessThan
            QMUIPhotoThumbnailItem(
                image.thumbnail(),
                wh,
                wh,
                config.alphaWhenPressed,
                isContainerFixed = true,
                isLongImage = isLongImage,
                onLayout = { offset, size ->
                    renderInfo[i].offsetInWindow = offset
                    renderInfo[i].size = size
                },
                onPhotoLoaded = {
                    renderInfo[i].photo = it
                },
                click = if (onClick != null) {
                    {
                        onClick.invoke(renderInfo.toList(), i)
                    }
                } else null
            )
        }
    }
}