package com.qmuiteam.photo.compose.picker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.qmuiteam.compose.core.ui.QMUITopBar
import com.qmuiteam.compose.core.ui.QMUITopBarBackIconItem
import com.qmuiteam.photo.compose.QMUIGesturePhoto
import com.qmuiteam.photo.data.PhotoLoadStatus
import com.qmuiteam.photo.data.QMUIMediaPhotoVO

@OptIn(ExperimentalPagerApi::class)
@Composable
fun QMUIPhotoPickerPreview(
    pagerState: PagerState,
    data: List<QMUIMediaPhotoVO>,
    loading: @Composable BoxScope.() -> Unit,
    loadingFailed: @Composable BoxScope.() -> Unit,
    onTap: () -> Unit
) {

    HorizontalPager(
        count = data.size,
        state = pagerState
    ) { page ->
        val item = data[page]
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            QMUIGesturePhoto(
                containerWidth = maxWidth,
                containerHeight = maxHeight,
                imageRatio = item.model.ratio(),
                shouldTransitionEnter = false,
                shouldTransitionExit = false,
                isLongImage = item.photoProvider.isLongImage(),
                onBeginPullExit = {
                    false
                },
                onTapExit = {
                    onTap()
                }
            ) { _, _, _, onImageRatioEnsured ->
                QMUIPhotoPickerPreviewItemContent(item, onImageRatioEnsured, loadingFailed, loading)
            }
        }
    }
}

@Composable
private fun QMUIPhotoPickerPreviewItemContent(
    item: QMUIMediaPhotoVO,
    onImageRatioEnsured: (Float) -> Unit,
    loading: @Composable BoxScope.() -> Unit,
    loadingFailed: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val photo = remember(item) {
            item.photoProvider.photo()
        }

        var loadStatus by remember {
            mutableStateOf(PhotoLoadStatus.loading)
        }

        photo?.Compose(
            contentScale = ContentScale.Fit,
            isContainerDimenExactly = true,
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

        if (loadStatus == PhotoLoadStatus.loading) {
            loading()
        } else if (loadStatus == PhotoLoadStatus.failed) {
            loadingFailed()
        }
    }
}