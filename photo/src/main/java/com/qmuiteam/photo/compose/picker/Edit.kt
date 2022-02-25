package com.qmuiteam.photo.compose.picker

import android.graphics.drawable.Drawable
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import com.qmuiteam.compose.core.R
import com.qmuiteam.compose.core.provider.QMUILocalWindowInsets
import com.qmuiteam.compose.core.provider.dp
import com.qmuiteam.compose.core.ui.PressWithAlphaBox
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
                scene == QMUIPhotoPickerEditScene.paint,
                photoInfo,
                onBack = onBack
            )
        }
    }
}

@Composable
private fun BoxScope.QMUIPhotoPickerEditPaint(
    paintState: Boolean,
    photoInfo: QMUIPhotoPickerPhotoInfo,
    onBack: () -> Unit
) {
    val insets = QMUILocalWindowInsets.current.getInsetsIgnoringVisibility(
        WindowInsetsCompat.Type.navigationBars() or
                WindowInsetsCompat.Type.statusBars() or
                WindowInsetsCompat.Type.displayCutout()
    ).dp()

    var showTools by remember {
        mutableStateOf(true)
    }

    AnimatedVisibility(
        visible = showTools,
        modifier = Modifier.align(Alignment.TopStart),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        PressWithAlphaBox(
            modifier = Modifier
                .padding(start = insets.left + 16.dp, top = insets.top + 16.dp, end = 16.dp, bottom = 16.dp),
            enable = true,
            onClick = {
                onBack()
            }
        ) {
            Image(
                painter = painterResource(R.drawable.ic_qmui_topbar_back),
                contentDescription = "",
                colorFilter = ColorFilter.tint(QMUILocalPickerConfig.current.commonIconButtonTintColor),
                contentScale = ContentScale.Inside
            )
        }
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(start = insets.left, end = insets.bottom, bottom = insets.bottom)
    ) {
        AnimatedVisibility(
            visible = showTools && paintState,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text("TODO")
        }

        AnimatedVisibility(
            visible = showTools,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text("TODO")
        }
    }
}

@Composable
private fun QMUIPhotoPickerEditPhotoContent(
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