package com.qmuiteam.photo.compose.picker

import android.graphics.drawable.Drawable
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.view.WindowInsetsCompat
import com.qmuiteam.compose.core.R
import com.qmuiteam.compose.core.provider.QMUILocalWindowInsets
import com.qmuiteam.compose.core.provider.dp
import com.qmuiteam.photo.compose.QMUIGesturePhoto
import com.qmuiteam.photo.data.QMUIMediaPhotoVO

private enum class QMUIPhotoPickerEditScene {
    normal, paint, text, clip
}

private class QMUIPhotoPickerPhotoInfo(
    var scale: Float,
    var rect: Rect?,
    var drawable: Drawable?,
)

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
        QMUIPhotoPickerPhotoInfo(1f, null, null)
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
            photoInfo.scale = scale
            photoInfo.rect = rect
            QMUIPhotoPickerEditPhotoContent(data) {
                photoInfo.drawable = it
                onImageRatioEnsured(it.intrinsicWidth.toFloat() / it.intrinsicHeight)
            }
        }
        AnimatedVisibility(
            visible = scene == QMUIPhotoPickerEditScene.normal || scene == QMUIPhotoPickerEditScene.paint,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            QMUIPhotoPickerEditPaint(
                paintState = scene == QMUIPhotoPickerEditScene.paint,
                photoInfo = photoInfo,
                onBack = onBack,
                onPaintClick = {
                    scene = if(it) QMUIPhotoPickerEditScene.paint else QMUIPhotoPickerEditScene.normal
                },
                onTextClick = {
                    scene = QMUIPhotoPickerEditScene.text
                },
                onClipClick = {
                    scene = QMUIPhotoPickerEditScene.clip
                },
                onEnsureClick = {
                    // TODO
                }
            )
        }
    }
}

@Composable
private fun QMUIPhotoPickerEditPaint(
    paintState: Boolean,
    photoInfo: QMUIPhotoPickerPhotoInfo,
    onBack: () -> Unit,
    onPaintClick: (toPaint: Boolean) -> Unit,
    onTextClick: () -> Unit,
    onClipClick: () -> Unit,
    onEnsureClick: () -> Unit
) {
    val insets = QMUILocalWindowInsets.current.getInsetsIgnoringVisibility(
        WindowInsetsCompat.Type.navigationBars() or
                WindowInsetsCompat.Type.statusBars() or
                WindowInsetsCompat.Type.displayCutout()
    ).dp()

    var showTools by remember {
        mutableStateOf(true)
    }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
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
            Text("TODO", color = Color.White)
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
internal fun QMUIPhotoPickerEditPaintChooser(){

}

@Composable
internal fun QMUIPhotoPickerEditToolBar(
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
        constrain(horChain){
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
            modifier = Modifier.constrainAs(ensure){
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                end.linkTo(parent.end, 16.dp)
            }
        )
    }
}