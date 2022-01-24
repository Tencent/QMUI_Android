package com.qmuiteam.photo.compose

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


@Composable
fun GesturePhoto(
    containerWidth: Dp,
    containerHeight: Dp,
    imageRatio: Float,
    isLongImage: Boolean,
    initRect: Rect? = null,
    transitionEnter: Boolean = false,
    transitionDurationMs: Int = 1000,
    onPress: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val layoutRatio = containerWidth / containerHeight
    val imageWidth: Dp
    val imageHeight: Dp
    when {
        imageRatio >= layoutRatio -> {
            imageWidth = containerWidth
            imageHeight = containerWidth / imageRatio
        }
        isLongImage -> {
            imageWidth = containerWidth
            imageHeight = containerHeight
        }
        else -> {
            imageWidth = containerHeight * imageRatio
            imageHeight = containerHeight
        }
    }

    var backgroundTargetAlpha by remember {
        mutableStateOf(1f)
    }

    val photoTargetNormalTranslateX = with(LocalDensity.current) {
        ((containerWidth - imageWidth) / 2f).toPx()
    }

    val photoTargetNormalTranslateY = with(LocalDensity.current) {
        ((containerHeight - imageHeight) / 2f).toPx()
    }

    var photoTargetScale by remember(containerWidth, containerHeight) { mutableStateOf(1f) }
    var photoTargetTranslateX by remember(containerWidth, containerHeight) { mutableStateOf(photoTargetNormalTranslateX) }
    var photoTargetTranslateY by remember(containerWidth, containerHeight) { mutableStateOf(photoTargetNormalTranslateY) }

    val containerWidthPx = with(LocalDensity.current) { containerWidth.toPx() }
    val containerHeightPx = with(LocalDensity.current) { containerHeight.toPx() }
    val imageWidthPx = with(LocalDensity.current) { imageWidth.toPx() }
    val imageHeightPx = with(LocalDensity.current) { imageHeight.toPx() }

    val transitionState = remember(containerWidth, containerHeight) {
        val canTransitionEnter = initRect != null && transitionEnter
        MutableTransitionState(!canTransitionEnter)
    }

    val scaleHandler: (Offset, Float) -> Unit = remember(containerWidth, containerHeight) {
        { center, scale ->
            var targetLeft = center.x + ((photoTargetTranslateX - center.x) * scale)
            var targetTop = center.y + ((photoTargetTranslateY - center.y) * scale)
            val targetWidth = imageWidthPx * photoTargetScale * scale
            val targetHeight = imageHeightPx * photoTargetScale * scale
            when {
                containerWidthPx > targetWidth -> {
                    targetLeft = (containerWidthPx - targetWidth) / 2
                }
                targetLeft > 0 -> {
                    targetLeft = 0f
                }
                targetLeft + targetWidth < containerWidthPx -> {
                    targetLeft = containerWidthPx - targetWidth
                }
            }

            when {
                containerHeightPx > targetHeight -> {
                    targetTop = (containerHeightPx - targetHeight) / 2
                }
                targetTop > 0 -> {
                    targetTop = 0f
                }
                targetTop + targetHeight < containerHeightPx -> {
                    targetTop = containerHeightPx - targetHeight
                }
            }
            photoTargetTranslateX = targetLeft
            photoTargetTranslateY = targetTop
            photoTargetScale *= scale
        }
    }

    transitionState.targetState = true
    val transition = updateTransition(transitionState = transitionState, label = "PhotoPager")
    Box(
        modifier = Modifier
            .width(containerWidth)
            .height(containerHeight)
    ) {
        PhotoBackgroundWithTransition(backgroundTargetAlpha, transition, transitionDurationMs){
            PhotoBackground(alpha = it)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput("${containerWidth}_${containerHeight}") {
                    coroutineScope {
                        launch {
                            detectTapGestures(
                                onPress = {
                                    onPress?.invoke()
                                },
                                onDoubleTap = {
                                    if (photoTargetScale == 1f) {
                                        var scale = 2f
                                        val alignScale = (containerWidth / imageWidth).coerceAtLeast((containerHeight / imageHeight))
                                        if (alignScale > 1.25 && alignScale < scale) {
                                            scale = alignScale
                                        }
                                        scaleHandler.invoke(it, scale)
                                    } else {
                                        photoTargetScale = 1f
                                        photoTargetTranslateX = photoTargetNormalTranslateX
                                        photoTargetTranslateY = photoTargetNormalTranslateY
                                    }
                                }
                            )
                        }

//                        launch {
//                            detectTransformGestures(true) { centroid: Offset, pan: Offset, zoom: Float, _: Float ->
//                                scaleHandler.invoke(centroid, zoom)
//                            }
//                        }
                    }
                }
        ) {

            if (initRect == null || initRect == Rect.Zero) {
                PhotoContentWithAlphaTransition(
                    scale = photoTargetScale,
                    translateX = photoTargetTranslateX,
                    translateY = photoTargetTranslateY,
                    transition = transition,
                    transitionDurationMs = transitionDurationMs
                ) { alpha, scale, translateX, translateY ->
                    PhotoTransformContent(imageWidth, imageHeight, alpha, scale, translateX, translateY, content)
                }
            } else {
                PhotoContentWithRectTransition(
                    imageWidth = imageWidthPx,
                    imageHeight = imageHeightPx,
                    initRect = initRect,
                    scale = photoTargetScale,
                    translateX = photoTargetTranslateX,
                    translateY = photoTargetTranslateY,
                    transition = transition,
                    transitionDurationMs = transitionDurationMs
                ) { scale, translateX, translateY ->
                    PhotoTransformContent(imageWidth, imageHeight, 1f, scale, translateX, translateY, content)
                }
            }
        }
    }
}

@Composable
fun PhotoBackgroundWithTransition(
    backgroundTargetAlpha: Float,
    transition: Transition<Boolean>,
    transitionDurationMs: Int,
    content: @Composable (alpha: Float) -> Unit
) {
    if (transition.currentState) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(backgroundTargetAlpha)
                .background(Color.Black)
        )
    } else {
        val alpha = transition.animateFloat(
            transitionSpec = { tween(durationMillis = transitionDurationMs) },
            label = "PhotoBackgroundWithTransition"
        ) {
            if (it) backgroundTargetAlpha else 0f
        }
        content(alpha.value)
    }
}

@Composable
fun PhotoContentWithAlphaTransition(
    scale: Float,
    translateX: Float,
    translateY: Float,
    transition: Transition<Boolean>,
    transitionDurationMs: Int,
    content: @Composable (alpha: Float, scale: Float, translateX: Float, translateY: Float) -> Unit
) {
    val alpha = transition.animateFloat(
        transitionSpec = { tween(durationMillis = transitionDurationMs) },
        label = "PhotoContentWithAlphaTransition"
    ) {
        if (it) 1f else 0f
    }
    content(alpha.value, scale, translateX, translateY)
}

@Composable
fun PhotoContentWithRectTransition(
    imageWidth: Float,
    imageHeight: Float,
    initRect: Rect,
    scale: Float,
    translateX: Float,
    translateY: Float,
    transition: Transition<Boolean>,
    transitionDurationMs: Int,
    content: @Composable (scale: Float, translateX: Float, translateY: Float) -> Unit
) {
    val rect = transition.animateRect(
        transitionSpec = { tween(durationMillis = transitionDurationMs) },
        label = "PhotoContentWithRectTransition"
    ) {
        if (it) Rect(translateX, translateY, translateX + imageWidth * scale, translateY + imageHeight * scale) else initRect
    }
    content(rect.value.width / imageWidth, rect.value.left, rect.value.top)
}

@Composable
fun PhotoBackground(
    alpha: Float
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .background(Color.Black)
    )
}

@Composable
fun PhotoTransformContent(
    imageWidth: Dp,
    imageHeight: Dp,
    alpha: Float,
    scale: Float,
    translateX: Float,
    translateY: Float,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .width(imageWidth)
            .height(imageHeight)
            .graphicsLayer {
                this.transformOrigin = TransformOrigin(0f, 0f)
                this.scaleX = scale
                this.scaleY = scale
                this.translationX = translateX
                this.translationY = translateY
                this.alpha = alpha
            }
    ) {
        content()
    }
}