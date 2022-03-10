package com.qmuiteam.photo.compose.picker

import android.graphics.drawable.Drawable
import androidx.activity.OnBackPressedCallback
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.consumeDownChange
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowInsetsCompat
import com.qmuiteam.compose.core.R
import com.qmuiteam.compose.core.helper.OnePx
import com.qmuiteam.compose.core.provider.QMUILocalWindowInsets
import com.qmuiteam.compose.core.provider.dp
import com.qmuiteam.photo.compose.QMUIGesturePhoto
import com.qmuiteam.photo.data.QMUIMediaPhotoVO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

private sealed class PickerEditScene

private object PickerEditSceneNormal : PickerEditScene()
private object PickerEditScenePaint : PickerEditScene()
private class PickerEditSceneText(val editLayer: TextEditLayer? = null) : PickerEditScene()
private class PickerEditSceneClip(val area: Rect) : PickerEditScene()

private class EditSceneHolder<T : PickerEditScene>(var scene: T? = null)

private class MutablePickerPhotoInfo(
    var drawable: Drawable?,
    var mosaicBitmapCache: MutableMap<Int, ImageBitmap> = mutableMapOf()
)

internal data class PickerPhotoLayoutInfo(val scale: Float, val rect: Rect)


@Composable
fun QMUIPhotoPickerEdit(
    onBackPressedDispatcher: OnBackPressedDispatcher,
    data: QMUIMediaPhotoVO,
    onBack: () -> Unit,
) {
    val sceneState = remember(data) {
        mutableStateOf<PickerEditScene>(PickerEditSceneNormal)
    }
    val scene = sceneState.value
    val photoInfo = remember(data) {
        MutablePickerPhotoInfo(null)
    }

    var photoLayoutInfo by remember(data) {
        mutableStateOf(PickerPhotoLayoutInfo(1f, Rect.Zero))
    }

    val paintEditLayers = remember(data) {
        mutableStateListOf<PaintEditLayer>()
    }

    val textEditLayers = remember(data) {
        mutableStateListOf<TextEditLayer>()
    }

    val config = QMUILocalPickerConfig.current

    var forceHideTools by remember {
        mutableStateOf(false)
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

            },
            onPress = {
                textEditLayers.forEach {
                    it.isFocusFlow.value = false
                }
            }
        ) { _, scale, rect, onImageRatioEnsured ->
            photoLayoutInfo = PickerPhotoLayoutInfo(scale, rect)
            QMUIPhotoPickerEditPhotoContent(data) {
                photoInfo.drawable = it
                onImageRatioEnsured(it.intrinsicWidth.toFloat() / it.intrinsicHeight)
            }
        }

        QMUIPhotoEditHistoryList(
            photoLayoutInfo,
            paintEditLayers,
            textEditLayers,
            onFocusLayer = { focusLayer ->
                textEditLayers.forEach {
                    if (it != focusLayer) {
                        it.isFocusFlow.value = false
                    }
                }
            },
            onEditTextLayer = {
                sceneState.value = PickerEditSceneText(it)
            },
            onDeleteTextLayer = {
                textEditLayers.remove(it)
            },
            onToggleDragging = {
                forceHideTools = it
            }
        )

        AnimatedVisibility(
            visible = scene == PickerEditSceneNormal || scene == PickerEditScenePaint,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            QMUIPhotoPickerEditPaintScreen(
                paintState = scene == PickerEditScenePaint,
                photoInfo = photoInfo,
                editLayers = paintEditLayers,
                layoutInfo = photoLayoutInfo,
                forceHideTools = forceHideTools,
                onBack = onBack,
                onPaintClick = {
                    sceneState.value = if (it) PickerEditScenePaint else PickerEditSceneNormal
                },
                onTextClick = {
                    sceneState.value = PickerEditSceneText()
                },
                onClipClick = {
                    sceneState.value = PickerEditSceneClip(Rect(Offset.Zero, photoLayoutInfo.rect.size))
                },
                onFinishPaintLayer = {
                    paintEditLayers.add(it)
                },
                onEnsureClick = {

                },
                onRevoke = {
                    paintEditLayers.removeLastOrNull()
                }
            )
        }

        AnimatedVisibility(
            visible = scene is PickerEditSceneText,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            // For exit animation
            val sceneHolder = remember {
                EditSceneHolder(scene as? PickerEditSceneText)
            }
            if (scene is PickerEditSceneText) {
                sceneHolder.scene = scene
            }
            val textScene = sceneHolder.scene
            if (textScene != null) {
                QMUIPhotoPickerEditTextScreen(
                    onBackPressedDispatcher,
                    photoLayoutInfo,
                    constraints,
                    textScene.editLayer,
                    textScene.editLayer?.color ?: config.textEditColorOptions[0].color,
                    textScene.editLayer?.reverse ?: false,
                    onCancel = {
                        sceneState.value = PickerEditSceneNormal
                    },
                    onFinishTextLayer = { toReplace, target ->
                        if (toReplace != null) {
                            val index = textEditLayers.indexOf(toReplace)
                            if (index >= 0) {
                                textEditLayers[index] = target
                            } else {
                                textEditLayers.add(target)
                            }
                        } else {
                            textEditLayers.add(target)
                        }
                        sceneState.value = PickerEditSceneNormal
                    }
                )
            }

        }
    }
}

@Composable
private fun QMUIPhotoPickerEditPaintScreen(
    paintState: Boolean,
    editLayers: List<PaintEditLayer>,
    photoInfo: MutablePickerPhotoInfo,
    layoutInfo: PickerPhotoLayoutInfo,
    forceHideTools: Boolean,
    onBack: () -> Unit,
    onPaintClick: (toPaint: Boolean) -> Unit,
    onTextClick: () -> Unit,
    onClipClick: () -> Unit,
    onFinishPaintLayer: (PaintEditLayer) -> Unit,
    onEnsureClick: () -> Unit,
    onRevoke: () -> Unit
) {
    val insets = QMUILocalWindowInsets.current.getInsetsIgnoringVisibility(
        WindowInsetsCompat.Type.navigationBars() or
                WindowInsetsCompat.Type.statusBars() or
                WindowInsetsCompat.Type.displayCutout()
    ).dp()

    val paintEditOptions = QMUILocalPickerConfig.current.editPaintOptions
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
                photoInfo,
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
            visible = showTools && !forceHideTools,
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
            visible = showTools && !forceHideTools,
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
            visible = showTools && !forceHideTools,
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
            visible = showTools && !forceHideTools,
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
            visible = showTools && paintState && !forceHideTools,
            modifier = Modifier.constrainAs(paintChooser) {
                width = Dimension.fillToConstraints
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(toolBar.top, 8.dp)
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

        AnimatedVisibility(
            visible = showTools && paintState && !forceHideTools,
            modifier = Modifier.constrainAs(createRef()) {
                end.linkTo(parent.end)
                bottom.linkTo(paintChooser.top)
            },
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CommonImageButton(
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp, end = insets.right + 16.dp, bottom = 16.dp),
                res = R.drawable.ic_qmui_topbar_back
            ) {
                onRevoke()
            }
        }
    }
}

@Composable
private fun QMUIPhotoPickerEditTextScreen(
    onBackPressedDispatcher: OnBackPressedDispatcher,
    photoLayoutInfo: PickerPhotoLayoutInfo,
    constraints: Constraints,
    editLayer: TextEditLayer?,
    color: Color,
    isReverse: Boolean,
    onCancel: () -> Unit,
    onFinishTextLayer: (toReplace: TextEditLayer?, target: TextEditLayer) -> Unit
) {
    DisposableEffect("") {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onCancel()
            }
        }
        onBackPressedDispatcher.addCallback(callback)
        object : DisposableEffectResult {
            override fun dispose() {
                callback.remove()
            }
        }
    }

    val insets = QMUILocalWindowInsets.current.getInsets(
        WindowInsetsCompat.Type.navigationBars() or
                WindowInsetsCompat.Type.ime() or
                WindowInsetsCompat.Type.statusBars() or
                WindowInsetsCompat.Type.displayCutout()
    ).dp()

    var input by remember(editLayer) {
        val text = editLayer?.text ?: ""
        mutableStateOf(TextFieldValue(text, TextRange(text.length)))
    }

    val config = QMUILocalPickerConfig.current

    var usedColor by remember(color) {
        mutableStateOf(color)
    }

    var usedReverse by remember(isReverse) {
        mutableStateOf(isReverse)
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ConstraintLayout(modifier = Modifier
        .fillMaxSize()
        .background(config.textEditMaskColor)
        .clickable(
            interactionSource = remember {
                MutableInteractionSource()
            },
            indication = null
        ) {
            if (input.text.isNotBlank()) {
                if (editLayer != null) {
                    onFinishTextLayer(
                        editLayer, TextEditLayer(
                            input.text,
                            editLayer.fontSize,
                            editLayer.center,
                            usedColor,
                            usedReverse,
                            editLayer.offsetFlow,
                            editLayer.scaleFlow,
                            editLayer.rotationFlow
                        )
                    )
                } else {
                    onFinishTextLayer(
                        null, TextEditLayer(
                            input.text,
                            config.textEditFontSize,
                            Offset(
                                (constraints.maxWidth / 2 - photoLayoutInfo.rect.left) / photoLayoutInfo.scale,
                                constraints.maxHeight / 2 - photoLayoutInfo.rect.top
                            ),
                            usedColor,
                            usedReverse,
                            scaleFlow = MutableStateFlow(1 / photoLayoutInfo.scale)
                        )
                    )
                }

            } else {
                onCancel()
            }
        }
        .padding(insets.left, insets.top, insets.right, insets.bottom)
    ) {
        val optionsId = createRef()
        QMUIPhotoPickerEditTextPaintOptions(
            config.textEditColorOptions,
            24.dp,
            usedColor,
            isReverse = usedReverse,
            modifier = Modifier.constrainAs(optionsId) {
                width = Dimension.fillToConstraints
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            },
            onSelect = {
                usedColor = it
            },
            onReverseClick = {
                usedReverse = it
            }
        )
        BasicTextField(
            value = input,
            onValueChange = {
                input = it
            },
            modifier = Modifier
                .padding(16.dp)
                .let {
                    if (usedReverse && input.text.isNotBlank()) {
                        it.background(color = usedColor, shape = RoundedCornerShape(10.dp))
                    } else {
                        it
                    }
                }
                .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 3.dp)
                .defaultMinSize(8.dp, 48.dp)
                .width(IntrinsicSize.Min)
                .focusRequester(focusRequester)
                .constrainAs(createRef()) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(optionsId.top)
                    top.linkTo(parent.top)
                },
            textStyle = TextStyle(
                color = if (usedReverse) {
                    if (usedColor == Color.White) Color.Black else Color.White
                } else usedColor,
                fontSize = config.textEditFontSize,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            ),
            cursorBrush = SolidColor(config.textCursorColor)
        )
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
    editLayers: List<PaintEditLayer>,
    textEditLayers: List<TextEditLayer>,
    onFocusLayer: (TextEditLayer) -> Unit,
    onEditTextLayer: (TextEditLayer) -> Unit,
    onDeleteTextLayer: (TextEditLayer) -> Unit,
    onToggleDragging: (Boolean) -> Unit
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
    textEditLayers.forEach {
        key(it) {
            it.Content(
                layoutInfo = layoutInfo,
                onFocus = {
                    onFocusLayer(it)
                },
                onToggleDragging = { isDragging ->
                    onToggleDragging(isDragging)
                },
                onEdit = {
                    onEditTextLayer(it)
                }) {
                onDeleteTextLayer(it)
            }
        }
    }
}

@Composable
private fun QMUIPhotoPaintCanvas(
    editPaint: EditPaint,
    photoInfo: MutablePickerPhotoInfo,
    layoutInfo: PickerPhotoLayoutInfo,
    editLayers: List<PaintEditLayer>,
    onTouchBegin: () -> Unit,
    onTouchEnd: (PaintEditLayer) -> Unit
) {
    val drawable = photoInfo.drawable ?: return
    val (w, h) = with(LocalDensity.current) {
        arrayOf(
            layoutInfo.rect.width.toDp(),
            layoutInfo.rect.height.toDp()
        )
    }

    val graffitiStrokeWidth = with(LocalDensity.current) {
        QMUILocalPickerConfig.current.graffitiPaintStrokeWidth.toPx()
    }
    val mosaicStrokeWidth = with(LocalDensity.current) {
        QMUILocalPickerConfig.current.mosaicPaintStrokeWidth.toPx()
    }
    val currentLayerState = remember(editLayers, editPaint, layoutInfo, drawable) {
        val layer = when (editPaint) {
            is ColorEditPaint -> {
                GraffitiEditLayer(Path(), editPaint.color, graffitiStrokeWidth / layoutInfo.scale)
            }
            is MosaicEditPaint -> {
                val image = photoInfo.mosaicBitmapCache[editPaint.scaleLevel] ?: drawable.toBitmap(
                    drawable.intrinsicWidth / editPaint.scaleLevel,
                    drawable.intrinsicHeight / editPaint.scaleLevel
                ).asImageBitmap().also {
                    photoInfo.mosaicBitmapCache[editPaint.scaleLevel] = it
                }
                MosaicEditLayer(
                    path = Path(),
                    image = image,
                    strokeWidth = mosaicStrokeWidth
                )
            }
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
            .pointerInput(editLayers, editPaint, layoutInfo) {
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
    editPaint: List<EditPaint>,
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
        editPaint.forEachIndexed { index, paintEdit ->
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

@Composable
private fun QMUIPhotoPickerEditTextPaintOptions(
    editPaint: List<ColorEditPaint>,
    size: Dp,
    color: Color,
    isReverse: Boolean,
    modifier: Modifier,
    onReverseClick: (isReverse: Boolean) -> Unit,
    onSelect: (Color) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        CommonImageButton(
            res = R.drawable.ic_qmui_mark,
            modifier = Modifier.padding(16.dp),
        ) {
            onReverseClick(!isReverse)
        }

        Box(
            modifier = Modifier
                .width(OnePx())
                .height(size + 8.dp)
                .background(QMUILocalPickerConfig.current.commonSeparatorColor)
        )

        Row(
            modifier = Modifier
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            editPaint.forEach { paintEdit ->
                paintEdit.Compose(size = size, selected = paintEdit.color == color) {
                    onSelect(paintEdit.color)
                }
            }
        }
    }

}