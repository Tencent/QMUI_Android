package com.qmuiteam.photo.compose.picker

import android.graphics.Typeface
import android.text.TextPaint
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChangeConsumed
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import com.qmuiteam.compose.core.R
import com.qmuiteam.compose.core.provider.QMUILocalWindowInsets
import com.qmuiteam.compose.core.provider.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.absoluteValue

internal class TextEditLayer(
    val text: String,
    val fontSize: TextUnit,
    val center: Offset,
    val color: Color,
    val reverse: Boolean,
    val offsetFlow: MutableStateFlow<Offset> = MutableStateFlow(Offset.Zero),
    val scaleFlow: MutableStateFlow<Float> = MutableStateFlow(1f),
    val rotationFlow: MutableStateFlow<Float> = MutableStateFlow(0f)
) {

    val isFocusFlow = MutableStateFlow(false)

    private val textColor = if (reverse) {
        (if (color == Color.White) Color.Black else Color.White)
    } else color

    private val paint = TextPaint().apply {
        typeface = Typeface.DEFAULT_BOLD
        color = textColor.toArgb()
        setShadowLayer(0f, 2f, 2f, textColor.copy(0.4f).toArgb())
    }


    @Composable
    private fun TextLayout(
        modifier: Modifier,
        lineSpace: Float,
        paddingHor: Float,
        paddingVer: Float,
        fontSize: Float,
        isFocus: Boolean
    ) {
        val cornerRadius = with(LocalDensity.current) {
            10.dp.toPx()
        }

        val focusPointSize = with(LocalDensity.current) {
            6.dp.toPx()
        }
        val focusLineWidth = with(LocalDensity.current) {
            2.dp.toPx()
        }

        Canvas(modifier = modifier) {

            val rectTopLeftOffset = Offset(focusPointSize / 2, focusPointSize / 2)
            val rectSize = Size(size.width - focusPointSize, size.height - focusPointSize)

            if (reverse) {
                drawRoundRect(
                    color,
                    topLeft = rectTopLeftOffset,
                    size = rectSize,
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
            }
            paint.textSize = fontSize
            drawIntoCanvas {
                val fontHeight = paint.descent() - paint.ascent()
                var baseLine = paddingVer - paint.ascent()
                var start = 0
                while (start < text.length) {
                    val count = paint.breakText(
                        text, start, text.length,
                        false,
                        size.width - paddingHor * 2,
                        null
                    )
                    val end = start + count
                    val contentWidth = paint.measureText(text, start, end)
                    it.nativeCanvas.drawText(text, start, end, (size.width - contentWidth) / 2, baseLine, paint)
                    baseLine += fontHeight + lineSpace
                    start = end
                }
            }

            if (isFocus) {
                drawRect(
                    Color.White,
                    topLeft = rectTopLeftOffset,
                    size = rectSize,
                    style = Stroke(focusLineWidth)
                )
                val focusSize = Size(focusPointSize, focusPointSize)
                drawRect(
                    Color.White,
                    topLeft = Offset.Zero,
                    size = focusSize
                )
                drawRect(
                    Color.White,
                    topLeft = Offset(size.width - focusPointSize, 0f),
                    size = focusSize
                )
                drawRect(
                    Color.White,
                    topLeft = Offset(0f, size.height - focusPointSize),
                    size = focusSize
                )
                drawRect(
                    Color.White,
                    topLeft = Offset(size.width - focusPointSize, size.height - focusPointSize),
                    size = focusSize
                )
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun Content(
        layoutInfo: PickerPhotoLayoutInfo,
        onFocus: () -> Unit,
        onEdit: () -> Unit,
        onToggleDragging: (Boolean) -> Unit,
        onDelete: () -> Unit
    ) {
        val currentOffset by offsetFlow.collectAsState()
        val currentRotation by rotationFlow.collectAsState()
        val currentScale by scaleFlow.collectAsState()

        val lineSpace = with(LocalDensity.current) {
            QMUILocalPickerConfig.current.textEditLineSpace.toPx()
        }

        val fontSizePx = with(LocalDensity.current) {
            fontSize.toPx()
        }

        val paddingHor = with(LocalDensity.current) {
            16.dp.toPx()
        }

        val paddingVer = with(LocalDensity.current) {
            8.dp.toPx()
        }

        val isFocus by isFocusFlow.collectAsState()

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val (contentWidth, contentHeight) = remember(constraints.maxWidth, constraints.maxHeight, fontSizePx) {
                paint.textSize = fontSizePx
                val textConstraintMaxWidth = constraints.maxWidth - paddingHor * 4
                val fontHeight = paint.descent() - paint.ascent()
                var start = 0
                var textMaxWidth = 0f
                var lineCount = 0
                while (start < text.length) {
                    val count = paint.breakText(
                        text, start, text.length,
                        false,
                        textConstraintMaxWidth,
                        null
                    )
                    val end = start + count
                    val contentWidth = paint.measureText(text, start, end)
                    textMaxWidth = textMaxWidth.coerceAtLeast(contentWidth)
                    lineCount++
                    start = end
                }
                arrayOf(
                    textMaxWidth + paddingHor * 2,
                    lineCount * (fontHeight + lineSpace) - lineSpace + paddingVer * 2
                )
            }
            val contentWidthDp = with(LocalDensity.current) {
                contentWidth.toDp()
            }
            val contentHeightDp = with(LocalDensity.current) {
                contentHeight.toDp()
            }

            val start = with(LocalDensity.current) {
                (center.x - contentWidth / 2).toDp()
            }

            val top = with(LocalDensity.current) {
                (center.y - contentHeight / 2).toDp()
            }

            val dragInfo = remember {
                MutableDragInfo()
            }

            var isDragging by remember {
                mutableStateOf(false)
            }

            var isInDeleteArea by remember {
                mutableStateOf(false)
            }

            TextLayout(
                modifier = Modifier
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0f, 0f)
                        scaleX = layoutInfo.scale
                        scaleY = layoutInfo.scale
                        translationX = layoutInfo.rect.left
                        translationY = layoutInfo.rect.top
                    }
                    .padding(start = start, top = top)
                    .width(contentWidthDp)
                    .height(contentHeightDp)
                    .onGloballyPositioned {
                        dragInfo.editLayerCenter = it.positionInWindow() + Offset(it.size.width / 2f, it.size.height / 2f)
                    }
                    .graphicsLayer {
                        translationX = currentOffset.x
                        translationY = currentOffset.y
                        scaleX = currentScale
                        scaleY = currentScale
                        rotationZ = currentRotation
                    }
                    .pointerInput("") {
                        coroutineScope {

                            launch {
                                detectTapGestures(
                                    onTap = {
                                        if (isFocusFlow.value) {
                                            onEdit()
                                        } else {
                                            isFocusFlow.value = true
                                            onFocus()
                                        }
                                    },
                                )
                            }
                            launch {
                                forEachGesture {
                                    awaitPointerEventScope {
                                        var rotation = 0f
                                        var zoom = 1f
                                        var pan = Offset.Zero
                                        var pastTouchSlop = false
                                        val touchSlop = viewConfiguration.touchSlop

                                        awaitFirstDown(requireUnconsumed = false)
                                        do {
                                            val event = awaitPointerEvent()
                                            val canceled = event.changes.any { it.positionChangeConsumed() }
                                            if (!canceled) {
                                                val zoomChange = event.calculateZoom()
                                                val rotationChange = event.calculateRotation()
                                                val panChange = event.calculatePan()

                                                if (!pastTouchSlop) {
                                                    zoom *= zoomChange
                                                    rotation += rotationChange
                                                    pan += panChange

                                                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                                                    val zoomMotion = abs(1 - zoom) * centroidSize
                                                    val rotationMotion = abs(rotation * PI.toFloat() * centroidSize / 180f)
                                                    val panMotion = pan.getDistance()

                                                    if (zoomMotion > touchSlop ||
                                                        rotationMotion > touchSlop ||
                                                        panMotion > touchSlop
                                                    ) {
                                                        pastTouchSlop = true
                                                    }
                                                }

                                                if (pastTouchSlop) {
                                                    if (rotationChange != 0f ||
                                                        zoomChange != 1f ||
                                                        panChange != Offset.Zero
                                                    ) {
                                                        if(panChange != Offset.Zero){
                                                            if(!isDragging){
                                                                isDragging = true
                                                                onToggleDragging(true)
                                                            }
                                                        }
                                                        offsetFlow.value = offsetFlow.value + panChange
                                                        scaleFlow.value = scaleFlow.value * zoomChange
                                                        rotationFlow.value = rotationFlow.value + rotationChange
                                                        if (isDragging) {
                                                            isInDeleteArea = dragInfo.isInDeleteArea(offsetFlow.value)
                                                        }
                                                    }
                                                    event.changes.forEach {
                                                        if (it.positionChanged()) {
                                                            it.consumeAllChanges()
                                                        }
                                                    }
                                                }
                                            }
                                        } while (!canceled && event.changes.any { it.pressed })
                                        if (isDragging) {
                                            if (isInDeleteArea) {
                                                onDelete()
                                            }
                                        }
                                        isInDeleteArea = false
                                        isDragging = false
                                        onToggleDragging(false)
                                    }
                                }
                            }
                        }
                    },
                lineSpace = lineSpace,
                paddingHor = paddingHor,
                paddingVer = paddingVer,
                fontSize = fontSizePx,
                isFocus = isFocus
            )

            AnimatedVisibility(
                visible = isDragging,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                DeleteArea(isInDeleteArea) { offset, size ->
                    dragInfo.deleteAreaOffset = offset
                    dragInfo.deleteAreaSize = size
                }
            }
        }
    }

    @Composable
    private fun DeleteArea(
        isFocusing: Boolean,
        onPlaced: (offset: Offset, size: IntSize) -> Unit
    ) {
        val insets = QMUILocalWindowInsets.current.getInsetsIgnoringVisibility(
            WindowInsetsCompat.Type.navigationBars()
        ).dp()
        val config = QMUILocalPickerConfig.current
        Column(modifier = Modifier
            .padding(bottom = insets.bottom + 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .onGloballyPositioned {
                onPlaced(it.positionInWindow(), it.size)
            }
            .background(if (isFocusing) config.editLayerDeleteAreaNormalFocusColor else config.editLayerDeleteAreaNormalBgColor)
            .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(
                    id = if (isFocusing) {
                        R.drawable.ic_qmui_checkbox_checked
                    } else R.drawable.ic_qmui_checkbox_partial
                ),
                contentDescription = "",
                colorFilter = ColorFilter.tint(Color.White)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isFocusing) "松手即可删除" else "拖动到此处删除",
                color = Color.White,
                fontSize = 15.sp
            )
        }
    }
}

private class MutableDragInfo(
    var deleteAreaOffset: Offset = Offset.Zero,
    var deleteAreaSize: IntSize = IntSize.Zero,
    var editLayerCenter: Offset = Offset.Zero
) {
    fun isInDeleteArea(offset: Offset): Boolean {
        val windowOffset = editLayerCenter + offset
        return windowOffset.x > deleteAreaOffset.x &&
                windowOffset.x < deleteAreaOffset.x + deleteAreaSize.width &&
                windowOffset.y > deleteAreaOffset.y &&
                windowOffset.y < deleteAreaOffset.y + deleteAreaSize.height
    }
}