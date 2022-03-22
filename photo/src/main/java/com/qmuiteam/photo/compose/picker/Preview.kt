package com.qmuiteam.photo.compose.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.qmuiteam.compose.core.ex.drawTopSeparator
import com.qmuiteam.compose.core.provider.QMUILocalWindowInsets
import com.qmuiteam.compose.core.provider.dp
import com.qmuiteam.photo.compose.QMUIGesturePhoto
import com.qmuiteam.photo.data.PhotoLoadStatus
import com.qmuiteam.photo.data.QMUIMediaPhotoVO
import kotlinx.coroutines.flow.StateFlow

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

    val photo = remember(item) {
        item.photoProvider.photo()
    }

    var loadStatus by remember {
        mutableStateOf(PhotoLoadStatus.loading)
    }

    Box(modifier = Modifier.fillMaxSize()) {

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

@Composable
fun QMUIPhotoPickerPreviewPickedItems(
    data: List<QMUIMediaPhotoVO>,
    pickedItems: List<Long>,
    currentId: Long,
    onClick: (QMUIMediaPhotoVO) -> Unit
) {
    if (pickedItems.isNotEmpty()) {
        val list = remember(data, pickedItems) {
            data.filter { pickedItems.contains(it.model.id) }
        }
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(QMUILocalPickerConfig.current.toolBarBgColor),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(5.dp),
            contentPadding = PaddingValues(horizontal = 5.dp)
        ) {
            items(list, { it.model.id }) {
                QMUIPhotoPickerPreviewPickedItem(it, it.model.id == currentId, onClick)
            }
        }
    }
}

@Composable
private fun QMUIPhotoPickerPreviewPickedItem(
    item: QMUIMediaPhotoVO,
    isCurrent: Boolean,
    onClick: (QMUIMediaPhotoVO) -> Unit
) {
    val thumb = remember(item) {
        item.photoProvider.thumbnail(true)
    }
    Box(modifier = Modifier
        .size(50.dp)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            onClick(item)
        }
        .let {
            if (isCurrent) {
                it.border(2.dp, QMUILocalPickerConfig.current.commonIconCheckedTintColor)
            } else {
                it
            }
        }
    ) {
        thumb?.Compose(
            contentScale = ContentScale.Crop,
            isContainerDimenExactly = true,
            onSuccess = null,
            onError = null
        )
    }
}


@Composable
fun QMUIPhotoPickerPreviewToolBar(
    modifier: Modifier = Modifier,
    current: QMUIMediaPhotoVO,
    isCurrentPicked: Boolean,
    enableOrigin: Boolean,
    isOriginOpenFlow: StateFlow<Boolean>,
    onToggleOrigin: (toOpen: Boolean) -> Unit,
    onEdit: () -> Unit,
    onToggleSelect: (toSelect: Boolean) -> Unit
) {
    val insets = QMUILocalWindowInsets.current.getInsetsIgnoringVisibility(
        WindowInsetsCompat.Type.navigationBars()
    ).dp()
    val config = QMUILocalPickerConfig.current
    Box(modifier = modifier
        .background(config.toolBarBgColor)
        .padding(bottom = insets.bottom)
        .height(44.dp)
        .drawBehind {
            drawTopSeparator(config.commonSeparatorColor)
        }
    ) {
        if (current.model.editable && config.editable) {
            CommonTextButton(
                modifier = Modifier.align(Alignment.CenterStart),
                enable = true,
                text = "编辑",
                onClick = onEdit
            )
        }

        if (enableOrigin) {
            OriginOpenButton(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp)
                    .align(Alignment.Center),
                isOriginOpenFlow = isOriginOpenFlow,
                onToggleOrigin = onToggleOrigin
            )
        }

        PickCurrentCheckButton(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterEnd),
            isPicked = isCurrentPicked,
            onPicked = onToggleSelect
        )
    }
}