package com.qmuiteam.photo.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
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
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChangeConsumed
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue

@Composable
fun QMUIGesturePhoto(
    containerWidth: Dp,
    containerHeight: Dp,
    imageRatio: Float,
    isLongImage: Boolean,
    initRect: Rect? = null,
    transitionEnter: Boolean = false,
    transitionExit: Boolean = true,
    transitionDurationMs: Int = 360,
    maxScale: Float = 4f,
    onBeginPullExit: () -> Boolean,
    onExit: (afterTransition: Boolean) -> Unit,
    content: @Composable (transition: Transition<Boolean>) -> Unit
) {
    val layoutRatio = containerWidth / containerHeight
    val imageWidth: Dp
    val imageHeight: Dp
    when {
        imageRatio <= 0f -> {
            imageWidth = containerWidth
            imageHeight = containerHeight
        }
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
    var isGestureHandling by remember(containerWidth, containerHeight) {
        mutableStateOf(false)
    }

    var transitionTargetState by remember(containerWidth, containerHeight) { mutableStateOf(true) }
    val transitionState = remember(containerWidth, containerHeight) {
        MutableTransitionState(!transitionEnter)
    }

    val scaleHandler: (Offset, Float, Boolean) -> Unit = remember(containerWidth, containerHeight, maxScale) {
        lambda@{ center, scaleParam, edgeProtection ->
            var scale = scaleParam
            if (photoTargetScale * scaleParam > maxScale) {
                scale = maxScale / photoTargetScale
            }
            if (scale == 1f) {
                return@lambda
            }
            var targetLeft = center.x + ((photoTargetTranslateX - center.x) * scale)
            var targetTop = center.y + ((photoTargetTranslateY - center.y) * scale)
            val targetWidth = imageWidthPx * photoTargetScale * scale
            val targetHeight = imageHeightPx * photoTargetScale * scale

            if (edgeProtection) {
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
            }
            photoTargetTranslateX = targetLeft
            photoTargetTranslateY = targetTop
            photoTargetScale *= scale
        }
    }

    val reset: () -> Unit = remember(containerWidth, containerHeight) {
        {
            backgroundTargetAlpha = 1f
            photoTargetScale = 1f
            photoTargetTranslateX = photoTargetNormalTranslateX
            photoTargetTranslateY = photoTargetNormalTranslateY
        }
    }

    transitionState.targetState = transitionTargetState
    val transition = updateTransition(transitionState = transitionState, label = "PhotoPager")

    val nestedScrollConnection = remember {
        GestureNestScrollConnection()
    }

    Box(
        modifier = Modifier
            .width(containerWidth)
            .height(containerHeight)
    ) {
        PhotoBackgroundWithTransition(backgroundTargetAlpha, transition, transitionDurationMs) {
            PhotoBackground(alpha = it)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .pointerInput("${containerWidth}_${containerHeight}_${maxScale}_${transitionExit}_${onExit}_${onBeginPullExit}") {
                    coroutineScope {
                        launch {
                            detectTapGestures(
                                onTap = {
                                    if (transitionExit) {
                                        transitionTargetState = false
                                    } else {
                                        onExit(false)
                                    }
                                },
                                onDoubleTap = {
                                    if (photoTargetScale == 1f) {
                                        var scale = 2f
                                        val alignScale = (containerWidth / imageWidth).coerceAtLeast((containerHeight / imageHeight))
                                        if (alignScale > 1.25 && alignScale < scale) {
                                            scale = alignScale
                                        }
                                        scaleHandler.invoke(it, scale, true)
                                    } else {
                                        reset()
                                    }
                                }
                            )
                        }

                        launch {
                            forEachGesture {
                                awaitPointerEventScope {
                                    var zoom = 1f
                                    var pan = Offset.Zero
                                    val touchSlop = viewConfiguration.touchSlop
                                    var isZooming = false
                                    var isPanning = false
                                    var isExitPanning = false
                                    isGestureHandling = false
                                    awaitFirstDown(requireUnconsumed = false)
                                    nestedScrollConnection.canConsumeEvent = false
                                    nestedScrollConnection.isIntercepted = false
                                    do {
                                        val event = awaitPointerEvent()
                                        if (isZooming || isExitPanning) {
                                            nestedScrollConnection.isIntercepted = true
                                        }
                                        val needHandle = !nestedScrollConnection.canConsumeEvent && event.changes.any { it.positionChangeConsumed() }
                                        if (!needHandle) {
                                            val zoomChange = event.calculateZoom()
                                            val panChange = event.calculatePan()

                                            if (!isZooming && !isPanning) {
                                                zoom *= zoomChange
                                                pan += panChange

                                                val centroidSize = event.calculateCentroidSize(useCurrent = false)
                                                val zoomMotion = abs(1 - zoom) * centroidSize
                                                val panMotion = pan.getDistance()

                                                if (zoomMotion > touchSlop) {
                                                    isGestureHandling = true
                                                    isZooming = true
                                                } else if (panMotion > touchSlop) {
                                                    isPanning = true
                                                    isGestureHandling = true
                                                }
                                            }

                                            if (isZooming) {
                                                val centroid = event.calculateCentroid(useCurrent = false)
                                                if (zoomChange != 1f) {
                                                    scaleHandler(centroid, zoomChange, true)
                                                }
                                                event.changes.forEach {
                                                    if (it.positionChanged()) {
                                                        it.consumeAllChanges()
                                                    }
                                                }
                                            } else if (isPanning) {
                                                if (!isExitPanning) {
                                                    var xConsumed = false
                                                    var yConsumed = false
                                                    if (panChange != Offset.Zero) {
                                                        if (panChange.x > 0) {
                                                            if (photoTargetTranslateX < 0) {
                                                                photoTargetTranslateX = (photoTargetTranslateX + panChange.x).coerceAtMost(0f)
                                                                xConsumed = true
                                                            }
                                                        }
                                                        if (panChange.x < 0) {
                                                            val w = imageWidthPx * photoTargetScale
                                                            if (photoTargetTranslateX + w > containerWidthPx) {
                                                                photoTargetTranslateX =
                                                                    (photoTargetTranslateX + panChange.x).coerceAtLeast(containerWidthPx - w)
                                                                xConsumed = true
                                                            }
                                                        }

                                                        if (panChange.y > 0) {
                                                            if (photoTargetTranslateY < 0) {
                                                                photoTargetTranslateY = (photoTargetTranslateY + panChange.y).coerceAtMost(0f)
                                                                yConsumed = true
                                                            } else if (!xConsumed && panChange.y > panChange.x.absoluteValue) {
                                                                isExitPanning = photoTargetScale == 1f && onBeginPullExit()
                                                            }
                                                        }

                                                        if (panChange.y < 0) {
                                                            val h = imageHeightPx * photoTargetScale
                                                            if (photoTargetTranslateY + h > containerHeightPx) {
                                                                photoTargetTranslateY =
                                                                    (photoTargetTranslateY + panChange.y).coerceAtLeast(containerHeightPx - h)
                                                                yConsumed = true
                                                            }
                                                        }
                                                    }

                                                    if (xConsumed || yConsumed) {
                                                        event.changes.forEach {
                                                            if (it.positionChanged()) {
                                                                it.consumeAllChanges()
                                                            }
                                                        }
                                                    }
                                                }

                                                if (isExitPanning) {
                                                    val center = event.calculateCentroid(useCurrent = true)
                                                    val scaleChange = 1 - panChange.y / containerHeightPx / 2
                                                    val finalScale = (photoTargetScale * scaleChange)
                                                        .coerceAtLeast(0.5f)
                                                        .coerceAtMost(1f)
                                                    backgroundTargetAlpha = finalScale
                                                    photoTargetTranslateX += panChange.x
                                                    photoTargetTranslateY += panChange.y
                                                    scaleHandler(center, finalScale / photoTargetScale, false)
                                                    event.changes.forEach {
                                                        if (it.positionChanged()) {
                                                            it.consumeAllChanges()
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } while (event.changes.any { it.pressed })

                                    isGestureHandling = false
                                    if (isZooming) {
                                        if (photoTargetScale < 1f) {
                                            reset()
                                        }
                                    }

                                    if (isExitPanning) {
                                        if (photoTargetScale > 0.9f) {
                                            reset()
                                        } else {
                                            transitionTargetState = false
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        ) {

            if (initRect == null || initRect == Rect.Zero || imageRatio <= 0f) {
                PhotoContentWithAlphaTransition(
                    transition = transition,
                    transitionDurationMs = transitionDurationMs,
                    isGestureHandling = isGestureHandling,
                    scale = photoTargetScale,
                    translateX = photoTargetTranslateX,
                    translateY = photoTargetTranslateY
                ) { alpha, scale, translateX, translateY  ->
                    PhotoTransformContent(
                        alpha,
                        imageWidthPx,
                        imageHeightPx,
                        scale,
                        translateX,
                        translateY
                    ) {
                        content(transition)
                    }
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
                ) { w, h, scale, translateX, translateY ->
                    PhotoTransformContent(1f, w, h, scale, translateX, translateY) {
                        content(transition)
                    }
                }
            }
        }
    }


    if (!transitionState.currentState && !transitionState.targetState) {
        onExit(true)
    }
}

@Composable
fun PhotoBackgroundWithTransition(
    backgroundTargetAlpha: Float,
    transition: Transition<Boolean>,
    transitionDurationMs: Int,
    content: @Composable (alpha: Float) -> Unit
) {
    val alpha = transition.animateFloat(
        transitionSpec = { tween(durationMillis = transitionDurationMs) },
        label = "PhotoBackgroundWithTransition"
    ) {
        if (it) backgroundTargetAlpha else 0f
    }
    content(alpha.value)
}

@Composable
fun PhotoContentWithAlphaTransition(
    transition: Transition<Boolean>,
    transitionDurationMs: Int,
    isGestureHandling: Boolean,
    scale: Float,
    translateX: Float,
    translateY: Float,
    content: @Composable (alpha: Float, scale: Float, translateX: Float, translateY: Float) -> Unit
) {
    val alphaState = transition.animateFloat(
        transitionSpec = { tween(durationMillis = transitionDurationMs) },
        label = "PhotoContentWithAlphaTransition"
    ) {
        if (it) 1f else 0f
    }
    val duration = if(isGestureHandling) 0 else transitionDurationMs
    val scaleState = animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(durationMillis = duration)
    )
    val translateXState = animateFloatAsState(
        targetValue = translateX,
        animationSpec = tween(durationMillis = duration)
    )
    val translateYState = animateFloatAsState(
        targetValue = translateY,
        animationSpec = tween(durationMillis = duration)
    )
    content(alphaState.value, scaleState.value, translateXState.value, translateYState.value)
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
    content: @Composable (w: Float, h: Float, scale: Float, translateX: Float, translateY: Float) -> Unit
) {
    val rect = transition.animateRect(
        transitionSpec = { tween(durationMillis = transitionDurationMs) },
        label = "PhotoContentWithRectTransition"
    ) {
        if (it) Rect(translateX, translateY, translateX + imageWidth * scale, translateY + imageHeight * scale) else initRect
    }
    val isTransitionEnd = transition.currentState && transition.targetState
    val usedWidth = if(isTransitionEnd) imageWidth else rect.value.width
    val usedHeight = if(isTransitionEnd) imageHeight else rect.value.height
    val usedScale = if(isTransitionEnd) rect.value.width / imageWidth else 1f
    content(usedWidth, usedHeight, usedScale, rect.value.left, rect.value.top)
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
    alpha: Float,
    width: Float,
    height: Float,
    scale: Float,
    translateX: Float,
    translateY: Float,
    content: @Composable () -> Unit
) {
    val widthDp = with(LocalDensity.current) { width.toDp() }
    val heightDp = with(LocalDensity.current) { height.toDp() }
    Box(
        modifier = Modifier
            .width(widthDp)
            .height(heightDp)
            .graphicsLayer {
                this.transformOrigin = TransformOrigin(0f, 0f)
                this.translationX = translateX
                this.translationY = translateY
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
            }
    ) {
        content()
    }
}

internal class GestureNestScrollConnection : NestedScrollConnection {

    var isIntercepted: Boolean = false
    var canConsumeEvent: Boolean = false

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (isIntercepted) {
            return available
        }
        return super.onPreScroll(available, source)
    }

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        if (available.y > 0) {
            canConsumeEvent = true
        }
        return available
    }
}