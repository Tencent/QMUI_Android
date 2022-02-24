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
package com.qmuiteam.photo.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Transition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.qmuiteam.photo.R
import com.qmuiteam.photo.compose.QMUIGesturePhoto
import com.qmuiteam.photo.compose.QMUIPhotoLoading
import com.qmuiteam.photo.data.*
import com.qmuiteam.photo.util.asBitmap
import kotlinx.coroutines.flow.MutableStateFlow

private const val PHOTO_CURRENT_INDEX = "qmui_photo_current_index"
private const val PHOTO_TRANSITION_DELIVERY_KEY = "qmui_photo_transition_delivery"
private const val PHOTO_COUNT = "qmui_photo_count"
private const val PHOTO_META_KEY_PREFIX = "qmui_photo_meta_"
private const val PHOTO_PROVIDER_RECOVER_CLASS_KEY_PREFIX = "qmui_photo_provider_recover_cls_"

open class QMUIPhotoViewerActivity : AppCompatActivity() {

    companion object {

        fun intentOf(
            activity: ComponentActivity,
            cls: Class<out QMUIPhotoViewerActivity>,
            list: List<QMUIPhotoTransitionInfo>,
            index: Int
        ): Intent {
            val data = PhotoViewerData(list, index, activity.window.decorView.asBitmap())
            val intent = Intent(activity, cls)
            intent.putExtra(PHOTO_TRANSITION_DELIVERY_KEY, QMUIPhotoTransitionDelivery.put(data))
            intent.putExtra(PHOTO_CURRENT_INDEX, index)
            intent.putExtra(PHOTO_COUNT, list.size)
            list.forEachIndexed { i, transition ->
                val meta = transition.photoProvider.meta()
                val recoverCls = transition.photoProvider.recoverCls()
                if (meta != null && recoverCls != null) {
                    intent.putExtra("${PHOTO_META_KEY_PREFIX}${i}", meta)
                    intent.putExtra(
                        "${PHOTO_PROVIDER_RECOVER_CLASS_KEY_PREFIX}${i}",
                        recoverCls.name
                    )
                }
            }
            return intent
        }
    }

    private val viewModel by viewModels<QMUIPhotoViewerViewModel>()
    private val transitionTargetFlow = MutableStateFlow(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView)?.let {
            it.hide(WindowInsetsCompat.Type.statusBars())
            it.isAppearanceLightNavigationBars = false
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                window.navigationBarDividerColor = android.graphics.Color.TRANSPARENT
                window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        onBackPressedDispatcher.addCallback(object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                transitionTargetFlow.value = false
            }
        })

        setContent {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                val data = viewModel.data
                if (data == null || data.list.isEmpty()) {
                    Text(text = "没有图片数据")
                } else {
                    viewModel.data?.background?.let {
                        Image(
                            painter = BitmapPainter(it.asImageBitmap()),
                            contentDescription = "",
                            contentScale = ContentScale.FillWidth,
                            alignment = Alignment.TopCenter,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    PhotoViewer(list = data.list, index = data.index)
                }
            }
        }
    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    protected open fun PhotoViewer(list: List<QMUIPhotoTransitionInfo>, index: Int) {
        val pagerState = rememberPagerState(index)
        HorizontalPager(
            count = list.size,
            state = pagerState
        ) { page ->
            val item = list[page]
            val initRect = item.photoRect()
            val transitionTarget = if(pagerState.currentPage == page){
                transitionTargetFlow.collectAsState().value
            } else true
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                QMUIGesturePhoto(
                    containerWidth = maxWidth,
                    containerHeight = maxHeight,
                    imageRatio = item.ratio(),
                    isLongImage = item.photoProvider.isLongImage(),
                    initRect = initRect,
                    shouldTransitionEnter = page == index,
                    transitionTarget = transitionTarget,
                    onBeginPullExit = {
                        true
                    },
                    onTapExit = {
                        if (it) {
                            finish()
                            overridePendingTransition(0, 0)
                        } else {
                            finish()
                            overridePendingTransition(0, R.anim.scale_exit)
                        }
                    }
                ) { transition, _, _, onImageRatioEnsured ->
                    PhotoContent(
                        transition = transition,
                        photoTransitionInfo = item,
                        onImageRatioEnsured = onImageRatioEnsured
                    )
                }
            }
        }
    }

    @Composable
    protected open fun PhotoContent(
        transition: Transition<Boolean>,
        photoTransitionInfo: QMUIPhotoTransitionInfo,
        onImageRatioEnsured: (Float) -> Unit
    ) {

        val thumb = remember(photoTransitionInfo) { photoTransitionInfo.photoProvider.thumbnail() }

        var loadStatus by remember {
            mutableStateOf(PhotoLoadStatus.loading)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            PhotoItem(photoTransitionInfo,
                onSuccess = {
                    if (it.drawable.intrinsicWidth > 0 && it.drawable.intrinsicHeight > 0) {
                        onImageRatioEnsured(it.drawable.intrinsicWidth.toFloat() / it.drawable.intrinsicHeight)
                    }
                    loadStatus = PhotoLoadStatus.success
                },
                onError = {
                    loadStatus = PhotoLoadStatus.failed
                }
            )

            if (loadStatus != PhotoLoadStatus.success || !transition.currentState || !transition.targetState) {
                val transitionPhoto = photoTransitionInfo.photo
                val contentScale = when {
                    photoTransitionInfo.photoProvider.isLongImage() -> {
                        ContentScale.FillWidth
                    }
                    photoTransitionInfo.ratio() > 0f -> {
                        ContentScale.Crop
                    }
                    else -> ContentScale.Fit
                }
                if (transitionPhoto != null) {
                    Image(
                        painter = BitmapPainter(transitionPhoto.toBitmap().asImageBitmap()),
                        contentDescription = "",
                        alignment = if (photoTransitionInfo.photoProvider.isLongImage()) Alignment.TopCenter else Alignment.Center,
                        contentScale = contentScale,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    thumb?.Compose(
                        contentScale = contentScale,
                        isContainerDimenExactly = true,
                        onSuccess = null,
                        onError = null
                    )
                }
            }

            if (loadStatus == PhotoLoadStatus.loading) {
                Loading()
            } else if (loadStatus == PhotoLoadStatus.failed) {
                LoadingFailed()
            }
        }
    }

    @Composable
    private fun PhotoItem(
        photoTransitionInfo: QMUIPhotoTransitionInfo,
        onSuccess: ((PhotoResult) -> Unit)?,
        onError: ((Throwable) -> Unit)? = null
    ) {
        val photo = remember(photoTransitionInfo) {
            photoTransitionInfo.photoProvider.photo()
        }
        photo?.Compose(
            contentScale = ContentScale.Fit,
            isContainerDimenExactly = true,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    @Composable
    protected open fun BoxScope.Loading() {
        Box(modifier = Modifier.align(Alignment.Center)) {
            QMUIPhotoLoading(size = 48.dp)
        }
    }

    @Composable
    protected open fun BoxScope.LoadingFailed() {
        // do nothing default, users should handle load fail / reload in Photo
    }
}


class QMUIPhotoViewerViewModel(val state: SavedStateHandle) : ViewModel() {

    val enterIndex = state.get<Int>(PHOTO_CURRENT_INDEX) ?: 0
    val data: PhotoViewerData?

    private val transitionDeliverKey = state.get<Long>(PHOTO_TRANSITION_DELIVERY_KEY) ?: -1

    init {
        val transitionDeliverData = QMUIPhotoTransitionDelivery.getAndRemove(transitionDeliverKey)
        data = if (transitionDeliverData != null) {
            transitionDeliverData
        } else {
            val count = state.get<Int>(PHOTO_COUNT) ?: 0
            if (count > 0) {
                val list = arrayListOf<QMUIPhotoTransitionInfo>()
                for (i in 0 until count) {
                    try {
                        val meta = state.get<Bundle>("${PHOTO_META_KEY_PREFIX}${i}")
                        val clsName =
                            state.get<String>("${PHOTO_PROVIDER_RECOVER_CLASS_KEY_PREFIX}${i}")
                        if (meta == null || clsName.isNullOrBlank()) {
                            list.add(lossPhotoTransitionInfo)
                        } else {
                            val cls = Class.forName(clsName)
                            val recover = cls.newInstance() as PhotoTransitionProviderRecover
                            list.add(recover.recover(meta) ?: lossPhotoTransitionInfo)
                        }

                    } catch (e: Throwable) {
                        list.add(lossPhotoTransitionInfo)
                    }
                }
                PhotoViewerData(list, enterIndex, null)
            } else {
                null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        QMUIPhotoTransitionDelivery.remove(transitionDeliverKey)
    }
}
