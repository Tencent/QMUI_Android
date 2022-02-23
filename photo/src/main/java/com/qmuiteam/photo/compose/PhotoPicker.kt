package com.qmuiteam.photo.compose

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qmuiteam.compose.core.helper.OnePx
import com.qmuiteam.compose.core.ui.CheckStatus
import com.qmuiteam.compose.core.ui.QMUICheckBox
import com.qmuiteam.compose.core.ui.QMUITopBarItem
import com.qmuiteam.compose.core.ui.qmuiPrimaryColor
import com.qmuiteam.photo.data.QMUIMediaModel
import com.qmuiteam.photo.data.QMUIMediaPhotoVO
import kotlinx.coroutines.flow.StateFlow
import java.lang.StringBuilder

class QMUIPhotoPickerConfig(
    val topBarBgColor: Color = Color(0xFF222222),
    val topBarBucketFactory: (
        textFlow: StateFlow<String>,
        isFocusFlow: StateFlow<Boolean>,
        onClick: () -> Unit
    ) -> QMUITopBarItem = { textFlow, isFocusFlow, onClick ->
        QMUIPhotoPickerBucketTopBarItem(
            bgColor = Color.White.copy(alpha = 0.15f),
            textColor = Color.White,
            iconBgColor = Color.White.copy(alpha = 0.72f),
            iconColor = Color(0xFF333333),
            textFlow = textFlow,
            isFocusFlow = isFocusFlow,
            onClick = onClick
        )
    },
    val topBarSendFactory: (
        maxSelectCount: Int,
        selectCountFlow: StateFlow<Int>,
        onClick: () -> Unit
    ) -> QMUITopBarItem = { maxSelectCount, selectCountFlow, onClick ->
        QMUIPhotoSendTopBarItem(
            normalTextColor = Color.White,
            disableTextColor = Color.White.copy(alpha = 0.3f),
            normalBgColor = qmuiPrimaryColor,
            pressBgColor = qmuiPrimaryColor.copy(alpha = 0.8f),
            disableBgColor = Color.White.copy(alpha = 0.15f),
            text = "发送",
            maxSelectCount = maxSelectCount,
            selectCountFlow = selectCountFlow,
            onClick = onClick
        )
    },

    val screenBgColor: Color = Color(0xFF333333),
    val loadingColor: Color = Color.White,
    val tipTextColor: Color = Color.White,

    val gridPreferredSize: Dp = 80.dp,
    val gridGap: Dp = 2.dp,
    val gridBorderColor: Color = Color.White.copy(alpha = 0.15f),

    val checkBoxNormalTintColor: Color = Color.White,
    val checkBoxCheckedBgColor: Color = qmuiPrimaryColor,
    val checkBoxCheckedTextColor: Color = Color.White
)

val qmuiPhotoPickerDefaultConfig by lazy { QMUIPhotoPickerConfig() }
val QMUILocalPickerConfig = staticCompositionLocalOf { qmuiPhotoPickerDefaultConfig }

@Composable
fun QMUIDefaultPickerConfigProvider(content: @Composable () -> Unit) {
    CompositionLocalProvider(QMUILocalPickerConfig provides qmuiPhotoPickerDefaultConfig) {
        content()
    }
}


class QMUIPhotoPickerBucketTopBarItem(
    private val bgColor: Color,
    private val textColor: Color,
    private val iconBgColor: Color,
    private val iconColor: Color,
    private val textFlow: StateFlow<String>,
    private val isFocusFlow: StateFlow<Boolean>,
    private val onClick: () -> Unit
) : QMUITopBarItem {

    @Composable
    override fun Compose(topBarHeight: Dp) {
        val text by textFlow.collectAsState()
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(bgColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = true,
                ) {
                    onClick()
                }
                .padding(start = 12.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(4.dp)
        ) {
            Text(
                text,
                fontSize = 17.sp,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
            QMUIPhotoPickerBucketToggleArrow(iconBgColor, iconColor, isFocusFlow)
        }
    }
}

class QMUIPhotoSendTopBarItem(
    private val normalTextColor: Color,
    private val disableTextColor: Color,
    private val normalBgColor: Color,
    private val pressBgColor: Color,
    private val disableBgColor: Color,
    private val text: String,
    private val maxSelectCount: Int,
    private val selectCountFlow: StateFlow<Int>,
    private val onClick: () -> Unit
) : QMUITopBarItem {
    @Composable
    override fun Compose(topBarHeight: Dp) {
        val selectCount by selectCountFlow.collectAsState()
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed = interactionSource.collectIsPressedAsState()
        val usedBgColor = when {
            selectCount == 0 -> disableBgColor
            isPressed.value -> pressBgColor
            else -> normalBgColor
        }
        val usedTextColor = if (selectCount > 0) normalTextColor else disableTextColor
        val usedText = if (selectCount > 0) "$text($selectCount/$maxSelectCount)" else text
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(usedBgColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = selectCount > 0
                ) {
                    onClick()
                }
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = usedText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = usedTextColor
            )
        }
    }
}

@Composable
fun QMUIPhotoPickerBucketToggleArrow(
    bgColor: Color,
    iconColor: Color,
    isFocusFlow: StateFlow<Boolean>
) {
    val isFocus by isFocusFlow.collectAsState()
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        val strokeWidth = with(LocalDensity.current) {
            1.6.dp.toPx()
        }
        val transition = updateTransition(targetState = isFocus, "QMUIPhotoPickerBucketToggleArrow")
        val rotate = transition.animateFloat(
            transitionSpec = { tween(durationMillis = 300) },
            label = "QMUIPhotoPickerBucketToggleArrowFocus"
        ) {
            if (it) 180f else 0f
        }
        Canvas(
            modifier = Modifier
                .width(10.dp)
                .height(5.dp)
                .rotate(rotate.value)
        ) {

            drawPath(Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width / 2, size.height)
                lineTo(size.width, 0f)
            }, iconColor, style = Stroke(strokeWidth))
        }
    }
}

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
    return ret
}

@Composable
fun QMUIPhotoPickerGrid(
    data: List<QMUIMediaPhotoVO>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    pickedItems: List<Long>,
    onPickItem: (toPick: Boolean, model: QMUIMediaModel) -> Unit,
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
            verticalArrangement = spacedBy(gap)
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
    onPickItem: (toPick: Boolean, model: QMUIMediaModel) -> Unit,
    onPreview: (model: QMUIMediaModel) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = spacedBy(gap),
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
    onPickItem: (toPick: Boolean, model: QMUIMediaModel) -> Unit,
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
        data.photoProvider.thumbnail()?.Compose(
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
                    onPickItem(pickedIndex < 0, data.model)
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QMUIPhotoPickCheckBox(pickIndex: Int){
    val config = QMUILocalPickerConfig.current
    val strokeWidth = with(LocalDensity.current){
        2.dp.toPx()
    }
    AnimatedVisibility(
        visible = pickIndex < 0,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()){
            drawCircle(
                color = config.checkBoxNormalTintColor,
                radius = (size.minDimension - strokeWidth) / 2.0f,
                style = Stroke(strokeWidth)
            )
        }
    }
    AnimatedVisibility(
        visible = pickIndex >= 0,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(config.checkBoxCheckedBgColor),
            contentAlignment = Alignment.Center
        ){
            if(transition.targetState != EnterExitState.PostExit){
                Text(
                    text = "${pickIndex + 1}",
                    color = config.checkBoxCheckedTextColor,
                    fontSize = 12.sp
                )
            }
        }
    }
}