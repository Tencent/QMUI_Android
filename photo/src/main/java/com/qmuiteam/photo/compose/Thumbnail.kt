package com.qmuiteam.photo.compose

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.qmuiteam.photo.data.*
import com.qmuiteam.photo.util.getWindowSize
import kotlinx.coroutines.launch

const val SINGLE_HIGH_IMAGE_MINI_SCREEN_HEIGHT_RATIO = -1F

class QMUIPhotoThumbnailConfig(
    val singleSquireImageWidthRatio: Float = 0.5f,
    val singleWideImageMaxWidthRatio: Float = 0.667f,
    val singleHighImageDefaultWidthRatio: Float = 0.5f,
    val singleHighImageMiniHeightRatio: Float = SINGLE_HIGH_IMAGE_MINI_SCREEN_HEIGHT_RATIO,
    val singleLongImageWidthRatio: Float = 0.5f,
    val averageIfTwoImage: Boolean = true,
    val horGap: Dp = 5.dp,
    val verGap: Dp = 5.dp,
    val alphaWhenPressed: Float = 1f
)

val qmuiDefaultPhotoThumbnailConfig = QMUIPhotoThumbnailConfig()

@Composable
private fun QMUIPhotoThumbnailItem(
    thumb: QMUIPhoto?,
    width: Dp,
    height: Dp,
    alphaWhenPressed: Float,
    isContainerDimenExactly: Boolean,
    onLayout: (offset: Offset, size: IntSize) -> Unit,
    onPhotoLoaded: (PhotoResult) -> Unit,
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
            contentScale = if (isContainerDimenExactly) ContentScale.Crop else ContentScale.Fit,
            isContainerDimenExactly = isContainerDimenExactly,
            onSuccess = {
                onPhotoLoaded(it)
            },
            onError = null
        )
    }
}


@Composable
fun QMUIPhotoThumbnailWithViewer(
    targetActivity: Class<out QMUIPhotoViewerActivity> = QMUIPhotoViewerActivity::class.java,
    activity: ComponentActivity,
    images: List<QMUIPhotoProvider>,
    config: QMUIPhotoThumbnailConfig = remember { qmuiDefaultPhotoThumbnailConfig }
) {
    QMUIPhotoThumbnail(images, config) { list, index ->
        val intent = QMUIPhotoViewerActivity.intentOf(activity, targetActivity, list, index)
        activity.startActivity(intent)
        activity.overridePendingTransition(0, 0)
    }
}

@Composable
fun QMUIPhotoThumbnail(
    images: List<QMUIPhotoProvider>,
    config: QMUIPhotoThumbnailConfig = remember { qmuiDefaultPhotoThumbnailConfig },
    onClick: ((images: List<QMUIPhotoTransitionInfo>, index: Int) -> Unit)? = null
) {
    if (images.size < 0) {
        return
    }
    val renderInfo = remember(images) {
        Array(images.size) {
            QMUIPhotoTransitionInfo(images[it], null, null, null)
        }
    }
    val context = LocalContext.current
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (images.size == 1) {
            val image = images[0]
            val thumb = remember(image) {
                image.thumbnail(true)
            }
            if (thumb != null) {
                val ratio = image.ratio()
                when {
                    ratio <= 0 -> {
                        QMUIPhotoThumbnailItem(
                            thumb,
                            Dp.Unspecified,
                            Dp.Unspecified,
                            config.alphaWhenPressed,
                            isContainerDimenExactly = false,
                            onLayout = { offset, size ->
                                renderInfo[0].offsetInWindow = offset
                                renderInfo[0].size = size
                            },
                            onPhotoLoaded = {
                                renderInfo[0].photo = it.drawable
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
                            isContainerDimenExactly = true,
                            onLayout = { offset, size ->
                                renderInfo[0].offsetInWindow = offset
                                renderInfo[0].size = size
                            },
                            onPhotoLoaded = {
                                renderInfo[0].photo = it.drawable
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
                            isContainerDimenExactly = true,
                            onLayout = { offset, size ->
                                renderInfo[0].offsetInWindow = offset
                                renderInfo[0].size = size
                            },
                            onPhotoLoaded = {
                                renderInfo[0].photo = it.drawable
                            },
                            click = if (onClick != null) {
                                {
                                    onClick.invoke(renderInfo.toList(), 0)
                                }
                            } else null
                        )
                    }
                    image.isLongImage() -> {
                        val width = maxWidth * config.singleLongImageWidthRatio
                        val heightRatio = if (config.singleHighImageMiniHeightRatio == SINGLE_HIGH_IMAGE_MINI_SCREEN_HEIGHT_RATIO) {
                            val windowSize = getWindowSize(context)
                            windowSize.width * 1f / windowSize.height
                        } else {
                            config.singleHighImageMiniHeightRatio
                        }
                        val height = width / heightRatio
                        QMUIPhotoThumbnailItem(
                            thumb,
                            width,
                            height,
                            config.alphaWhenPressed,
                            isContainerDimenExactly = true,
                            onLayout = { offset, size ->
                                renderInfo[0].offsetInWindow = offset
                                renderInfo[0].size = size
                            },
                            onPhotoLoaded = {
                                renderInfo[0].photo = it.drawable
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
                        val heightMiniRatio = if (config.singleHighImageMiniHeightRatio == SINGLE_HIGH_IMAGE_MINI_SCREEN_HEIGHT_RATIO) {
                            val windowSize = getWindowSize(context)
                            windowSize.width * 1f / windowSize.height
                        } else {
                            config.singleHighImageMiniHeightRatio
                        }
                        if (ratio < heightMiniRatio) {
                            height = width * heightMiniRatio
                            width = height * ratio
                        }
                        QMUIPhotoThumbnailItem(
                            thumb,
                            width,
                            height,
                            config.alphaWhenPressed,
                            isContainerDimenExactly = true,
                            onLayout = { offset, size ->
                                renderInfo[0].offsetInWindow = offset
                                renderInfo[0].size = size
                            },
                            onPhotoLoaded = {
                                renderInfo[0].photo = it.drawable
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
    renderInfo: Array<QMUIPhotoTransitionInfo>,
    config: QMUIPhotoThumbnailConfig,
    containerWidth: Dp,
    rowCount: Int,
    startIndex: Int,
    onClick: ((images: List<QMUIPhotoTransitionInfo>, index: Int) -> Unit)?
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
            QMUIPhotoThumbnailItem(
                remember(image) {
                    image.thumbnail(true)
                },
                wh,
                wh,
                config.alphaWhenPressed,
                isContainerDimenExactly = true,
                onLayout = { offset, size ->
                    renderInfo[i].offsetInWindow = offset
                    renderInfo[i].size = size
                },
                onPhotoLoaded = {
                    renderInfo[i].photo = it.drawable
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