package com.qmuiteam.photo.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qmuiteam.photo.data.QMUIMediaModel
import com.qmuiteam.photo.data.QMUIMediaPhotoVO

class QMUIPhotoPickerConfig(
    val backgroundColor: Color = Color(0xFF333333),
    val loadingColor: Color = Color(0xFFFFFFFF),
    val tipTextColor: Color = Color(0xFFFFFFFF),

    val gridPreferredSize: Dp = 80.dp,
    val gridGap: Dp = 5.dp
)

val qmuiPhotoPickerDefaultConfig by lazy { QMUIPhotoPickerConfig() }
val QMUILocalPickerConfig = staticCompositionLocalOf { qmuiPhotoPickerDefaultConfig }

@Composable
fun QMUIDefaultPickerConfigProvider(content: @Composable () -> Unit) {
    CompositionLocalProvider(QMUILocalPickerConfig provides qmuiPhotoPickerDefaultConfig) {
        content()
    }
}


@Composable
fun QMUIPhotoPickerGrid(
    data: List<QMUIMediaModel>,
    photoContent: @Composable (QMUIMediaModel) -> Unit
){
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val preferredSize = QMUILocalPickerConfig.current.gridPreferredSize
        val gap = QMUILocalPickerConfig.current.gridGap
        val rowCount = ((maxWidth + gap) / (preferredSize + gap)).toInt().coerceAtLeast(2)
        val columnCount = (data.size / rowCount) + if(data.size % rowCount > 0) 1 else 0
        val realSize = ((maxWidth + gap) / rowCount) - gap
        LazyColumn {
            items(
                count = columnCount,
                key = { index ->
                    val dataIndex = index * rowCount
                    val endIndex = (dataIndex + rowCount).coerceAtMost(data.size)
                    data.subList(dataIndex, endIndex).joinToString("-") { it.uri.toString() }
                }
            ){

            }
        }
    }
}

@Composable
private fun QMUIPhotoPickerGridRow(
    data: List<QMUIMediaPhotoVO>,
    rowCount: Int,
    columnIndex: Int,
    cellSize: Dp
){
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
    ){
        for(i in 0 until rowCount){
            Box(modifier = Modifier.size(cellSize)){
                val realIndex = columnIndex * rowCount + i
            }
        }
    }
}