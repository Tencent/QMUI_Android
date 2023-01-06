package com.qmuiteam.photo.compose.picker

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import com.qmuiteam.compose.core.ex.drawTopSeparator
import com.qmuiteam.compose.core.helper.OnePx
import com.qmuiteam.compose.core.provider.QMUILocalWindowInsets
import com.qmuiteam.compose.core.provider.dp
import com.qmuiteam.photo.data.QMUIMediaModel
import com.qmuiteam.photo.data.QMUIMediaPhotoVO
import kotlinx.coroutines.flow.StateFlow
import java.lang.StringBuilder

class QMUIPhotoPickerGridRowData(val key: String, val list: List<QMUIMediaPhotoVO>)

private fun convertToRowData(data: List<QMUIMediaPhotoVO>, rowCount: Int): List<QMUIPhotoPickerGridRowData>{
    val ret = mutableListOf<QMUIPhotoPickerGridRowData>()
    var list = mutableListOf<QMUIMediaPhotoVO>()
    val keySb = StringBuilder()
    data.forEach {
        keySb.append(it.model.uri)
        list.add(it)
        if(list.size == rowCount){
            ret.add(QMUIPhotoPickerGridRowData(keySb.toString(), list))
            list = mutableListOf()
            keySb.clear()
        }
    }
    if(list.isNotEmpty()){
        ret.add(QMUIPhotoPickerGridRowData(keySb.toString(), list))
    }
    return ret
}

@Composable
fun QMUIPhotoPickerGrid(
    data: List<QMUIMediaPhotoVO>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    pickedItems: List<Long>,
    onPickItem: (toPick: Boolean, model: QMUIMediaPhotoVO) -> Unit,
    onPreview: (model: QMUIMediaModel) -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val config = QMUILocalPickerConfig.current
        val gap = config.gridGap
        val rowCount = remember(maxWidth, config) {
            val preferredSize = config.gridPreferredSize
            ((maxWidth + gap) / (preferredSize + gap)).toInt().coerceAtLeast(2)
        }
        val cellSize = remember(maxWidth, gap, rowCount) {
            ((maxWidth + gap) / rowCount) - gap
        }

        val rowData = remember(data, rowCount) {
            convertToRowData(data, rowCount)
        }
        // TODO use LazyVerticalGrid for a replacement
        LazyColumn(
            state = state,
            verticalArrangement = Arrangement.Absolute.spacedBy(gap)
        ) {
            items(rowData, key = { it.key }){ item ->
                QMUIPhotoPickerGridRow(item, cellSize, gap, pickedItems, onPickItem, onPreview)
            }
        }
    }
}

@Composable
private fun QMUIPhotoPickerGridRow(
    data: QMUIPhotoPickerGridRowData,
    cellSize: Dp,
    gap: Dp,
    pickedItems: List<Long>,
    onPickItem: (toPick: Boolean, model: QMUIMediaPhotoVO) -> Unit,
    onPreview: (model: QMUIMediaModel) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Absolute.spacedBy(gap),
    ) {
        for(i in 0 until data.list.size){
            QMUIPhotoPickerGridCell(
                data = data.list[i],
                cellSize = cellSize,
                pickedItems = pickedItems,
                onPickItem = onPickItem,
                onPreview = onPreview
            )
        }
    }
}

@Composable
private fun QMUIPhotoPickerGridCell(
    data: QMUIMediaPhotoVO,
    cellSize: Dp,
    pickedItems: List<Long>,
    onPickItem: (toPick: Boolean, model: QMUIMediaPhotoVO) -> Unit,
    onPreview: (model: QMUIMediaModel) -> Unit
) {
    val pickedIndex = remember(pickedItems) {
        pickedItems.indexOfFirst {
            it == data.model.id
        }
    }
    Box(
        modifier = Modifier
            .size(cellSize)
            .border(OnePx(), QMUILocalPickerConfig.current.gridBorderColor)
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null,
                enabled = true
            ) {
                onPreview.invoke(data.model)
            }
    ) {
        val thumbnail = remember(data) {
            data.photoProvider.thumbnail(true)
        }
        thumbnail?.Compose(
            contentScale = ContentScale.Crop,
            isContainerDimenExactly = true,
            onSuccess = null,
            onError = null
        )

        QMUIPhotoPickerGridCellMask(pickedIndex)

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clickable {
                    onPickItem(pickedIndex < 0, data)
                }
                .padding(4.dp)
                .size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            QMUIPhotoPickCheckBox(pickedIndex)
        }
    }
}

@Composable
fun QMUIPhotoPickerGridCellMask(pickedIndex: Int){
    val maskAlpha = animateFloatAsState(targetValue = if(pickedIndex >= 0) 0.36f else 0.15f)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = maskAlpha.value))
    )
}

@Composable
fun QMUIPhotoPickerGridToolBar(
    modifier: Modifier = Modifier,
    enableOrigin: Boolean,
    pickedItems: List<Long>,
    isOriginOpenFlow: StateFlow<Boolean>,
    onToggleOrigin: (toOpen: Boolean) -> Unit,
    onPreview: () -> Unit
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
        CommonTextButton(
            modifier = Modifier.align(Alignment.CenterStart),
            enable = pickedItems.isNotEmpty(),
            text = "预览",
            onClick = onPreview
        )

        if(enableOrigin){
            OriginOpenButton(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp)
                    .align(Alignment.Center),
                isOriginOpenFlow = isOriginOpenFlow,
                onToggleOrigin = onToggleOrigin
            )
        }
    }
}