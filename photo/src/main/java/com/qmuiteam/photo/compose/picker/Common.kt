package com.qmuiteam.photo.compose.picker

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qmuiteam.compose.core.ui.CheckStatus
import com.qmuiteam.compose.core.ui.PressWithAlphaBox
import com.qmuiteam.compose.core.ui.QMUICheckBox
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QMUIPhotoPickCheckBox(pickIndex: Int) {
    val config = QMUILocalPickerConfig.current
    val strokeWidth = with(LocalDensity.current) {
        2.dp.toPx()
    }
    AnimatedVisibility(
        visible = pickIndex < 0,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = config.commonIconNormalTintColor,
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
                .background(config.commonIconCheckedTintColor),
            contentAlignment = Alignment.Center
        ) {
            if (transition.targetState != EnterExitState.PostExit) {
                Text(
                    text = "${pickIndex + 1}",
                    color = config.commonIconCheckedTextColor,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun QMUIPhotoPickRadio(
    checked: Boolean,
    ratioSize: Dp = 18.dp,
    strokeWidthDp: Dp = 1.6.dp
) {
    Box(modifier = Modifier.size(ratioSize)) {
        val strokeWidth = with(LocalDensity.current) {
            strokeWidthDp.toPx()
        }
        val config = QMUILocalPickerConfig.current
        AnimatedVisibility(
            visible = !checked,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Canvas(modifier = Modifier.size(ratioSize)) {
                drawCircle(
                    color = config.commonIconNormalTintColor,
                    radius = (size.minDimension - strokeWidth) / 2.0f,
                    style = Stroke(strokeWidth)
                )
            }
        }
        AnimatedVisibility(
            visible = checked,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Canvas(modifier = Modifier.size(ratioSize)) {
                drawCircle(
                    color = config.commonIconCheckedTintColor,
                    radius = (size.minDimension - strokeWidth) / 2.0f,
                    style = Stroke(strokeWidth)
                )

                drawCircle(
                    color = config.commonIconCheckedTintColor,
                    radius = (size.minDimension - strokeWidth * 4) / 2.0f,
                )
            }
        }
    }
}

@Composable
fun OriginOpenButton(
    modifier: Modifier = Modifier,
    isOriginOpenFlow: StateFlow<Boolean>,
    onToggleOrigin: (toOpen: Boolean) -> Unit,
) {
    val isOriginOpen by isOriginOpenFlow.collectAsState()
    Row(
        modifier = modifier.clickable(
            interactionSource = remember {
                MutableInteractionSource()
            },
            indication = null
        ) {
            onToggleOrigin.invoke(!isOriginOpen)
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.spacedBy(5.dp)
    ) {
        QMUIPhotoPickRadio(isOriginOpen)
        Text(
            "原图",
            fontSize = 17.sp,
            color = QMUILocalPickerConfig.current.commonTextButtonTextColor
        )
    }
}

@Composable
fun PickCurrentCheckButton(
    modifier: Modifier = Modifier,
    isPicked: Boolean,
    onPicked: (toPick: Boolean) -> Unit,
) {
    val config = QMUILocalPickerConfig.current
    Row(
        modifier = modifier.clickable(
            interactionSource = remember {
                MutableInteractionSource()
            },
            indication = null
        ) {
            onPicked.invoke(!isPicked)
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.spacedBy(5.dp)
    ) {
        QMUICheckBox(
            size = 18.dp,
            status = if (isPicked) CheckStatus.checked else CheckStatus.none,
            tint = if (isPicked) config.commonIconCheckedTintColor else config.commonIconNormalTintColor,
            background = if (isPicked) config.commonIconNormalTintColor else Color.Transparent,
        )
        Text(
            "选择",
            fontSize = 17.sp,
            color = QMUILocalPickerConfig.current.commonTextButtonTextColor
        )
    }
}


@Composable
internal fun CommonTextButton(
    modifier: Modifier,
    enable: Boolean,
    text: String,
    onClick: () -> Unit
) {
    PressWithAlphaBox(
        enable = enable,
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 16.dp)
            .then(modifier),
        onClick = {
            onClick()
        }
    ) {
        Text(
            text,
            fontSize = 17.sp,
            color = QMUILocalPickerConfig.current.commonTextButtonTextColor,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
internal fun CommonImageButton(
    modifier: Modifier = Modifier,
    res: Int,
    enabled: Boolean = true,
    checked: Boolean = false,
    onClick: () -> Unit
){
    PressWithAlphaBox(
        modifier = modifier,
        enable = enabled,
        onClick = {
            onClick()
        }
    ) {
        val config = QMUILocalPickerConfig.current
        Image(
            painter = painterResource(res),
            contentDescription = "",
            colorFilter = ColorFilter.tint(if(checked) config.commonIconCheckedTintColor else config.commonIconNormalTintColor),
            contentScale = ContentScale.Inside
        )
    }
}

@Composable
internal fun CommonButton(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    text: String,
    onClick: () -> Unit
) {
    val config = QMUILocalPickerConfig.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val bgColor = when {
        !enabled -> config.commonButtonDisableBgColor
        isPressed.value -> config.commonButtonPressBgColor
        else -> config.commonButtonNormalBgColor
    }
    val textColor = when {
        !enabled -> config.commonButtonDisabledTextColor
        isPressed.value -> config.commonButtonPressedTextColor
        else -> config.commonButtonNormalTextColor
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                onClick()
            }
            .padding(start = 10.dp, end = 10.dp, top = 3.dp, bottom = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 17.sp,
            color = textColor
        )
    }
}