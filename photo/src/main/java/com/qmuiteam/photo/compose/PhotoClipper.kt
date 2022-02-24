/*
 * Tencent is pleased to support the open source community by making QMUI_Android available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qmuiteam.photo.compose

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.qmuiteam.photo.data.PhotoLoadStatus
import com.qmuiteam.photo.data.QMUIPhotoProvider

private class ClipperPhotoInfo(
    var scale: Float = 1f,
    var rect: Rect? = null,
    var drawable: Drawable? = null,
    var clipArea: Rect
)

val DefaultClipFocusAreaSquareCenter = Rect.Zero

@Composable
fun QMUIPhotoClipper(
    photoProvider: QMUIPhotoProvider,
    maskColor: Color = Color.Black.copy(0.64f),
    clipFocusArea: Rect = DefaultClipFocusAreaSquareCenter,
    drawClipFocusArea: DrawScope.(Rect) -> Unit = { area ->
        drawCircle(
            Color.Black,
            radius = area.size.minDimension / 2,
            center = area.center,
            blendMode = BlendMode.DstOut
        )
    },
    bitmapClipper: (origin: Bitmap, clipArea: Rect, scale: Float) -> Bitmap? = { origin, clipArea, scale ->
        val matrix = Matrix()
        matrix.postScale(scale, scale)
        Bitmap.createBitmap(
            origin,
            clipArea.left.toInt(),
            clipArea.top.toInt(),
            clipArea.width.toInt(),
            clipArea.height.toInt(),
            matrix,
            false
        )
    },
    operateContent: @Composable BoxWithConstraintsScope.(doClip: () -> Bitmap?) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val focusArea = if (clipFocusArea == DefaultClipFocusAreaSquareCenter) {
            val size = (constraints.maxWidth.coerceAtMost(constraints.maxHeight)).toFloat()
            val left = (constraints.maxWidth - size) / 2
            val top = (constraints.maxHeight - size) / 2
            Rect(left, top, left + size, top + size)
        } else {
            clipFocusArea
        }

        val photoInfo = remember(photoProvider) {
            ClipperPhotoInfo(clipArea = focusArea)
        }.apply {
            clipArea = focusArea
        }

        val doClip = remember(photoInfo) {
            val func: () -> Bitmap? = lambda@{
                val origin = photoInfo.drawable?.toBitmap() ?: return@lambda null
                val rect = photoInfo.rect ?: return@lambda null
                val scale = rect.width / origin.width
                val clipRect = photoInfo.clipArea.translate(Offset(-rect.left, -rect.top))
                val imageArea = Rect(
                    clipRect.left / scale,
                    clipRect.top / scale,
                    clipRect.right / scale,
                    clipRect.bottom / scale
                )
                bitmapClipper(origin, imageArea, scale)
            }
            func
        }

        QMUIGesturePhoto(
            containerWidth = maxWidth,
            containerHeight = maxHeight,
            imageRatio = photoProvider.ratio(),
            isLongImage = photoProvider.isLongImage(),
            shouldTransitionExit = false,
            panEdgeProtection = focusArea,
            onBeginPullExit = { false },
            onTapExit = {}
        ) { _, scale, rect, onImageRatioEnsured ->
            photoInfo.scale = scale
            photoInfo.rect = rect
            QMUIPhotoContent(photoProvider) {
                photoInfo.drawable = it
                if (it.intrinsicWidth > 0 && it.intrinsicHeight > 0) {
                    onImageRatioEnsured(it.intrinsicWidth.toFloat() / it.intrinsicHeight)
                }
            }
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawContext.canvas.withSaveLayer(Rect(Offset.Zero, drawContext.size), Paint()) {
                drawRect(maskColor)
                drawClipFocusArea(focusArea)
            }
        }
        operateContent(doClip)
    }
}

@Composable
fun BoxScope.QMUIPhotoContent(
    photoProvider: QMUIPhotoProvider,
    onSuccess: (Drawable) -> Unit
) {
    var loadStatus by remember {
        mutableStateOf(PhotoLoadStatus.loading)
    }
    val photo = remember(photoProvider) {
        photoProvider.photo()
    }
    photo?.Compose(
        contentScale = ContentScale.Fit,
        isContainerDimenExactly = true,
        onSuccess = {
            loadStatus = PhotoLoadStatus.success
            onSuccess.invoke(it.drawable)
        },
        onError = {
            loadStatus = PhotoLoadStatus.failed
        })

    if (loadStatus == PhotoLoadStatus.loading) {
        Box(modifier = Modifier.align(Alignment.Center)) {
            QMUIPhotoLoading(size = 48.dp)
        }
    }
}