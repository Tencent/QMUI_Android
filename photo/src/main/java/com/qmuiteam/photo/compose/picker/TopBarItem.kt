package com.qmuiteam.photo.compose.picker

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qmuiteam.compose.core.ui.QMUITopBarItem
import kotlinx.coroutines.flow.StateFlow

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
            horizontalArrangement = Arrangement.Absolute.spacedBy(5.dp)
        ) {
            Text(
                text,
                fontSize = 17.sp,
                color = textColor,
                modifier = Modifier.padding(bottom = 1.dp)
            )
            QMUIPhotoPickerBucketToggleArrow(iconBgColor, iconColor, isFocusFlow)
        }
    }
}

class QMUIPhotoSendTopBarItem(
    private val canSendSelf: Boolean,
    private val text: String,
    private val maxSelectCount: Int,
    private val selectCountFlow: StateFlow<Int>,
    private val onClick: () -> Unit
) : QMUITopBarItem {
    @Composable
    override fun Compose(topBarHeight: Dp) {
        val selectCount by selectCountFlow.collectAsState()
        CommonButton(
            enabled = selectCount > 0 || canSendSelf,
            text = if (selectCount > 0) "$text($selectCount/$maxSelectCount)" else text,
            onClick = onClick
        )
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
                .width(8.dp)
                .height(4.dp)
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