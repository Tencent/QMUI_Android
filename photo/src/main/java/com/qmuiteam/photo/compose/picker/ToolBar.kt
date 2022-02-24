package com.qmuiteam.photo.compose.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import com.qmuiteam.compose.core.ex.drawTopSeparator
import com.qmuiteam.compose.core.provider.QMUILocalWindowInsets
import com.qmuiteam.compose.core.provider.dp
import com.qmuiteam.compose.core.ui.CheckStatus
import com.qmuiteam.compose.core.ui.PressWithAlphaBox
import com.qmuiteam.compose.core.ui.QMUICheckBox
import kotlinx.coroutines.flow.StateFlow

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
            status = if(isPicked) CheckStatus.checked else CheckStatus.none,
            tint = if(isPicked) config.commonCheckIconCheckedTintColor else config.commonCheckIconNormalTintColor,
            background = if(isPicked) config.commonCheckIconNormalTintColor else Color.Transparent,
        )
        Text(
            "选择",
            fontSize = 17.sp,
            color = QMUILocalPickerConfig.current.commonTextButtonTextColor
        )
    }
}

@Composable
fun CommonTextButton(
    modifier: Modifier,
    enable: Boolean,
    text: String,
    onClick: () -> Unit
){
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
fun QMUIPhotoPickerGridPageToolBar(
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


@Composable
fun QMUIPhotoPickerPreviewToolBar(
    modifier: Modifier = Modifier,
    isCurrentPicked: Boolean,
    enableOrigin: Boolean,
    isOriginOpenFlow: StateFlow<Boolean>,
    onToggleOrigin: (toOpen: Boolean) -> Unit,
    onEdit: () -> Unit,
    onToggleSelect: (toSelect: Boolean) -> Unit
){
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
            enable = true,
            text = "预览",
            onClick = onEdit
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