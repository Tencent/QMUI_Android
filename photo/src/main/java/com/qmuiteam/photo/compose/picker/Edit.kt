package com.qmuiteam.photo.compose.picker

import android.graphics.drawable.Drawable
import android.util.Log
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.consumeDownChange
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import androidx.core.view.WindowInsetsCompat
import com.qmuiteam.compose.core.R
import com.qmuiteam.compose.core.provider.QMUILocalWindowInsets
import com.qmuiteam.compose.core.provider.dp
import com.qmuiteam.photo.compose.QMUIGesturePhoto
import com.qmuiteam.photo.data.QMUIMediaPhotoVO
import kotlinx.coroutines.coroutineScope

private enum class QMUIPhotoPickerEditScene {
    normal, paint, text, clip
}

private class MutablePickerPhotoInfo(
    var drawable: Drawable?
)

private data class PickerPhotoLayoutInfo(var scale: Float, var rect: Rect)

sealed class PaintEdit {
    @Composable
    abstract fun Compose(size: Dp, selected: Boolean, onClick: () -> Unit)
}

class PaintMosaic(val level: Int) : PaintEdit() {

    @Composable
    override fun Compose(size: Dp, selected: Boolean, onClick: () -> Unit) {
        val ringWidth = with(LocalDensity.current) {
            2.dp.toPx()
        }
        Canvas(modifier = Modifier.size(size)) {
            drawCircle(
                Color.White,
                radius = this.size.minDimension / 2 - if (selected) 0f else ringWidth
            )
            drawCircle(
                Color.Black,
                radius = this.size.minDimension / 2 - ringWidth * 2
            )
        }
    }
}

class PaintGraffiti(val color: Color) : PaintEdit() {
    @Composable
    override fun Compose(size: Dp, selected: Boolean, onClick: () -> Unit) {
        val ringWidth = with(LocalDensity.current) {
            2.dp.toPx()
        }
        Canvas(modifier = Modifier
            .size(size)
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null
            ) {
                onClick()
            }) {
            drawCircle(
                Color.White,
                radius = this.size.minDimension / 2 - if (selected) 0f else ringWidth
            )
            drawCircle(
                color,
                radius = this.size.minDimension / 2 - ringWidth * 2
            )
        }
    }
}

@Composable
fun QMUIPhotoPickerEdit(
    onBackPressedDispatcher: OnBackPressedDispatcher,
    data: QMUIMediaPhotoVO,
    onBack: () -> Unit,
) {
    var scene by remember(data) {
        mutableStateOf(QMUIPhotoPickerEditScene.normal)
    }
    val photoInfo = remember(data) {
        MutablePickerPhotoInfo(null)
    }

    var photoLayoutInfo by remember(data) {
        mutableStateOf(PickerPhotoLayoutInfo(1f, Rect.Zero))
    }

    val editLayers = remember(data) {
        mutableStateListOf<EditLayer>()
    }


    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        QMUIGesturePhoto(
            containerWidth = maxWidth,
            containerHeight = maxHeight,
            imageRatio = data.model.ratio(),
            shouldTransitionEnter = false,
            shouldTransitionExit = false,
            isLongImage = data.photoProvider.isLongImage(),
            onBeginPullExit = {
                false
            },
            onTapExit = {

            }
        ) { _, scale, rect, onImageRatioEnsured ->
            photoLayoutInfo = PickerPhotoLayoutInfo(scale, rect)
            QMUIPhotoPickerEditPhotoContent(data) {
                photoInfo.drawable = it
                onImageRatioEnsured(it.intrinsicWidth.toFloat() / it.intrinsicHeight)
            }
        }

        QMUIPhotoEditHistoryList(photoLayoutInfo, editLayers)

        AnimatedVisibility(
            visible = scene == QMUIPhotoPickerEditScene.normal || scene == QMUIPhotoPickerEditScene.paint,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            QMUIPhotoPickerEditPaint(
                paintState = scene == QMUIPhotoPickerEditScene.paint,
                editLayers = editLayers,
                layoutInfo = photoLayoutInfo,
                onBack = onBack,
                onPaintClick = {
                    scene = if (it) QMUIPhotoPickerEditScene.paint else QMUIPhotoPickerEditScene.normal
                },
                onTextClick = {
                    scene = QMUIPhotoPickerEditScene.text
                },
                onClipClick = {
                    scene = QMUIPhotoPickerEditScene.clip
                },
                onFinishPaintLayer = {
                    editLayers.add(it)
                },
                onEnsureClick = {

                }
            )
        }
    }
}

@Composable
private fun QMUIPhotoPickerEditPaint(
    paintState: Boolean,
    editLayers: List<EditLayer>,
    layoutInfo: PickerPhotoLayoutInfo,
    onBack: () -> Unit,
    onPaintClick: (toPaint: Boolean) -> Unit,
    onTextClick: () -> Unit,
    onClipClick: () -> Unit,
    onFinishPaintLayer: (PaintEditLayer) -> Unit,
    onEnsureClick: () -> Unit
) {
    val insets = QMUILocalWindowInsets.current.getInsetsIgnoringVisibility(
        WindowInsetsCompat.Type.navigationBars() or
                WindowInsetsCompat.Type.statusBars() or
                WindowInsetsCompat.Type.displayCutout()
    ).dp()

    val paintEditOptions = QMUILocalPickerConfig.current.paintEditOptions
    var paintEditCurrentIndex by remember {
        mutableStateOf(4)
    }

    if (paintEditCurrentIndex >= paintEditOptions.size) {
        paintEditCurrentIndex = paintEditOptions.size - 1
    }

    var showTools by remember {
        mutableStateOf(true)
    }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {

        Box(modifier = Modifier
            .fillMaxSize()
            .constrainAs(createRef()) {
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
                visibility = if (paintState) Visibility.Visible else Visibility.Gone
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }) {
            QMUIPhotoPaintCanvas(
                paintEditOptions[paintEditCurrentIndex],
                layoutInfo,
                editLayers,
                onTouchBegin = {
                    showTools = false
                },
                onTouchEnd = {
                    showTools = true
                    onFinishPaintLayer(it)
                }
            )
        }

        AnimatedVisibility(
            visible = showTools,
            modifier = Modifier.constrainAs(createRef()) {
                width = Dimension.fillToConstraints
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
            },
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(insets.top + 60.dp)
                    .background(brush = Brush.verticalGradient(listOf(Color.Black.copy(0.2f), Color.Transparent)))
            )
        }

        AnimatedVisibility(
            visible = showTools,
            modifier = Modifier.constrainAs(createRef()) {
                width = Dimension.fillToConstraints
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            },
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(insets.bottom + 150.dp)
                    .background(brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.2f))))
            )
        }

        AnimatedVisibility(
            visible = showTools,
            modifier = Modifier.constrainAs(createRef()) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
            },
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CommonImageButton(
                modifier = Modifier
                    .padding(start = insets.left + 16.dp, top = insets.top + 16.dp, end = 16.dp, bottom = 16.dp),
                res = R.drawable.ic_qmui_topbar_back
            ) {
                onBack()
            }
        }

        val (toolBar, paintChooser) = createRefs()

        AnimatedVisibility(
            visible = showTools,
            modifier = Modifier.constrainAs(toolBar) {
                width = Dimension.fillToConstraints
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            },
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            QMUIPhotoPickerEditToolBar(
                modifier = Modifier.padding(bottom = insets.bottom, start = insets.left, end = insets.right),
                isPaintState = paintState,
                onPaintClick = onPaintClick,
                onTextClick = onTextClick,
                onClipClick = onClipClick,
                onEnsureClick = onEnsureClick
            )
        }

        AnimatedVisibility(
            visible = showTools && paintState,
            modifier = Modifier.constrainAs(paintChooser) {
                width = Dimension.fillToConstraints
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(toolBar.top)
            },
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            QMUIPhotoPickerEditPaintOptions(
                paintEditOptions,
                24.dp,
                paintEditCurrentIndex
            ) {
                paintEditCurrentIndex = it
            }
        }
    }
}

@Composable
fun QMUIPhotoPickerEditPhotoContent(
    data: QMUIMediaPhotoVO,
    onSuccess: (Drawable) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val photo = remember(data) {
            data.photoProvider.photo()
        }

        photo?.Compose(
            contentScale = ContentScale.Fit,
            isContainerDimenExactly = true,
            onSuccess = {
                if (it.drawable.intrinsicWidth > 0 && it.drawable.intrinsicHeight > 0) {
                    onSuccess(it.drawable)
                }
            },
            onError = null
        )
    }
}

@Composable
private fun QMUIPhotoEditHistoryList(
    layoutInfo: PickerPhotoLayoutInfo,
    editLayers: List<EditLayer>
) {
    if (layoutInfo.rect == Rect.Zero) {
        return
    }
    val (w, h) = with(LocalDensity.current) {
        arrayOf(
            layoutInfo.rect.width.toDp(),
            layoutInfo.rect.height.toDp()
        )
    }
    Canvas(modifier = Modifier
        .width(w / layoutInfo.scale)
        .height(h / layoutInfo.scale)
        .graphicsLayer {
            this.transformOrigin = TransformOrigin(0f, 0f)
            this.translationX = layoutInfo.rect.left
            this.translationY = layoutInfo.rect.top
            this.scaleX = layoutInfo.scale
            this.scaleY = layoutInfo.scale
            this.clip = true
        }) {
        editLayers.forEach {
            with(it) {
                draw()
            }
        }
    }
}

@Composable
private fun QMUIPhotoPaintCanvas(
    paintEdit: PaintEdit,
    layoutInfo: PickerPhotoLayoutInfo,
    editLayers: List<EditLayer>,
    onTouchBegin: () -> Unit,
    onTouchEnd: (PaintEditLayer) -> Unit
) {
    val (w, h) = with(LocalDensity.current) {
        arrayOf(
            layoutInfo.rect.width.toDp(),
            layoutInfo.rect.height.toDp()
        )
    }

    val strokeWidth = with(LocalDensity.current) {
        QMUILocalPickerConfig.current.paintEditStrokeWidth.toPx()
    }
    val currentLayerState = remember(editLayers, paintEdit, layoutInfo) {
        val layer = if (paintEdit is PaintGraffiti) {
            GraffitiEditLayer(Path(), paintEdit.color, strokeWidth / layoutInfo.scale)
        } else {
            MosaicEditLayer(Path())
        }
        mutableStateOf(layer, neverEqualPolicy())
    }

    val currentLayer = currentLayerState.value

    Canvas(modifier = Modifier
        .width(w / layoutInfo.scale)
        .height(h / layoutInfo.scale)
        .graphicsLayer {
            this.transformOrigin = TransformOrigin(0f, 0f)
            this.translationX = layoutInfo.rect.left
            this.translationY = layoutInfo.rect.top
            this.scaleX = layoutInfo.scale
            this.scaleY = layoutInfo.scale
            this.clip = true
        }) {
        with(currentLayer) {
            draw()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(editLayers, paintEdit, layoutInfo) {
                coroutineScope {
                    forEachGesture {
                        awaitPointerEventScope {
                            val down = awaitFirstDown(requireUnconsumed = true)
                            down.consumeDownChange()
                            currentLayer.path.moveTo(
                                (down.position.x - layoutInfo.rect.left) / layoutInfo.scale,
                                (down.position.y - layoutInfo.rect.top) / layoutInfo.scale
                            )
                            onTouchBegin()
                            do {
                                val event = awaitPointerEvent()
                                val change = event.changes.find { it.id.value == down.id.value }
                                if (change != null) {
                                    change.consumePositionChange()
                                    currentLayer.path.lineTo(
                                        (change.position.x - layoutInfo.rect.left) / layoutInfo.scale,
                                        (change.position.y - layoutInfo.rect.top) / layoutInfo.scale
                                    )
                                    currentLayerState.value = currentLayer
                                }

                            } while (change == null || change.pressed)
                            onTouchEnd(currentLayer)
                        }
                    }
                }
            }
    )
}


@Composable
private fun QMUIPhotoPickerEditPaintOptions(
    paintEdit: List<PaintEdit>,
    size: Dp,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        paintEdit.forEachIndexed { index, paintEdit ->
            paintEdit.Compose(size = size, selected = index == selectedIndex) {
                onSelect(index)
            }
        }
    }
}

@Composable
private fun QMUIPhotoPickerEditToolBar(
    modifier: Modifier,
    isPaintState: Boolean,
    onPaintClick: (toPaint: Boolean) -> Unit,
    onTextClick: () -> Unit,
    onClipClick: () -> Unit,
    onEnsureClick: () -> Unit
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        val (paint, text, clip, ensure) = createRefs()
        val horChain = createHorizontalChain(paint, text, clip, chainStyle = ChainStyle.Packed(0f))
        constrain(horChain) {
            start.linkTo(parent.start, 16.dp)
            end.linkTo(ensure.start)
        }
        CommonImageButton(
            modifier = Modifier
                .padding(10.dp)
                .constrainAs(paint) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
            res = R.drawable.ic_qmui_checkbox_checked,
            checked = isPaintState
        ) {
            onPaintClick(!isPaintState)
        }
        CommonImageButton(
            modifier = Modifier
                .padding(10.dp)
                .constrainAs(text) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
            res = R.drawable.ic_qmui_checkbox_checked,
        ) {
            onTextClick()
        }
        CommonImageButton(
            modifier = Modifier
                .padding(10.dp)
                .constrainAs(clip) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
            res = R.drawable.ic_qmui_checkbox_checked,
        ) {
            onClipClick()
        }
        CommonButton(
            enabled = true,
            text = "确定",
            onClick = onEnsureClick,
            modifier = Modifier.constrainAs(ensure) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                end.linkTo(parent.end, 16.dp)
            }
        )
    }
}