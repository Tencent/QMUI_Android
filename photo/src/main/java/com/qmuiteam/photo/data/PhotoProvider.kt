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
import android.graphics.Rect
import android.os.Bundle
import androidx.compose.runtime.Composable

class PhotoViewerData(
    val list: List<PhotoProvider>,
    val index: Int,
    val background: Bitmap?
)

interface Photo {

    @Composable
    fun Compose()
}

interface PhotoProvider {
    suspend fun thumbnail(): Photo?
    suspend fun photo(): Photo?
    fun transitionStart(): Rect?
    fun transitionEnd(): Rect?

    // used to recover PhotoProvider from arguments if activity recreate.
    fun meta(): Bundle?
    fun recoverCls(): Class<in PhotoProviderRecover>?
}

class LossPhotoProvider private constructor(): PhotoProvider {

    companion object {
        val instance by lazy {
            LossPhotoProvider()
        }
    }

    override suspend fun thumbnail(): Photo? {
        return null
    }

    override suspend fun photo(): Photo? {
        return null
    }

    override fun transitionStart(): Rect? {
        return null
    }

    override fun transitionEnd(): Rect? {
        return null
    }

    override fun meta(): Bundle? {
        return null
    }

    override fun recoverCls(): Class<in PhotoProviderRecover>? {
        return null
    }

}

interface PhotoProviderRecover {
    fun recover(bundle: Bundle): PhotoProvider?
}


class ImageItem(
    val url: String,
    val thumbnailUrl: String?,
    val thumbnail: Bitmap?
)