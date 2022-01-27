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
package com.qmuiteam.photo.data

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize

class PhotoViewerData(
    val list: List<QMUIPhotoTransitionInfo>,
    val index: Int,
    val background: Bitmap?
)

interface QMUIPhoto {

    @Composable
    fun Compose(
        contentScale: ContentScale,
        isContainerFixed: Boolean,
        onSuccess: ((Drawable) -> Unit)?,
        onError: ((Throwable) -> Unit)?
    )
}

interface QMUIPhotoProvider {
    fun thumbnail(): QMUIPhoto?
    fun photo(): QMUIPhoto?
    fun ratio(): Float = -1f
    fun isLongImage(): Boolean = false

    fun meta(): Bundle?
    fun recoverCls(): Class<in PhotoTransitionProviderRecover>?
}

class QMUIPhotoTransitionInfo(
    val photoProvider: QMUIPhotoProvider,
    var offsetInWindow: Offset?,
    var size: IntSize?,
    var photo: Drawable?
) {
    fun photoRect(): Rect? {
        val offset = offsetInWindow
        val size = size?.toSize()
        if (offset == null || size == null || size.width == 0f || size.height == 0f) {
            return null
        }
        return Rect(offset, size)
    }
}

val lossPhotoProvider = object : QMUIPhotoProvider {
    override fun thumbnail(): QMUIPhoto? {
        return null
    }

    override fun photo(): QMUIPhoto? {
        return null
    }

    override fun meta(): Bundle? {
        return null
    }

    override fun recoverCls(): Class<in PhotoTransitionProviderRecover>? {
        return null
    }
}

val lossPhotoTransitionInfo = QMUIPhotoTransitionInfo(lossPhotoProvider, null, null, null)


interface PhotoTransitionProviderRecover {
    fun recover(bundle: Bundle): QMUIPhotoTransitionInfo?
}


class ImageItem(
    val url: String,
    val thumbnailUrl: String?,
    val thumbnail: Bitmap?
)