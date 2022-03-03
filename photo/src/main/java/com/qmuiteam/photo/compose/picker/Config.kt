package com.qmuiteam.photo.compose.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qmuiteam.compose.core.ui.QMUITopBarItem
import com.qmuiteam.compose.core.ui.qmuiPrimaryColor
import kotlinx.coroutines.flow.StateFlow

class QMUIPhotoPickerConfig(
    val commonTextButtonTextColor: Color = Color.White,
    val commonSeparatorColor: Color = Color.White.copy(alpha = 0.3f),
    val commonIconNormalTintColor: Color = Color.White.copy(0.9f),
    val commonIconCheckedTintColor: Color = qmuiPrimaryColor,
    val commonIconCheckedTextColor: Color = Color.White.copy(alpha = 0.6f),

    val commonButtonNormalTextColor: Color = Color.White,
    val commonButtonNormalBgColor: Color = qmuiPrimaryColor,
    val commonButtonDisabledTextColor: Color = Color.White.copy(alpha = 0.3f),
    val commonButtonDisableBgColor: Color = Color.White.copy(alpha = 0.15f),
    val commonButtonPressBgColor: Color = qmuiPrimaryColor.copy(alpha = 0.8f),
    val commonButtonPressedTextColor: Color = commonButtonNormalTextColor,

    val topBarBgColor: Color = Color(0xFF222222),
    val toolBarBgColor: Color = topBarBgColor,

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
        canSendSelf: Boolean,
        maxSelectCount: Int,
        selectCountFlow: StateFlow<Int>,
        onClick: () -> Unit
    ) -> QMUITopBarItem = { canSendSelf, maxSelectCount, selectCountFlow, onClick ->
        QMUIPhotoSendTopBarItem(
            text = "发送",
            canSendSelf = canSendSelf,
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

    val bucketChooserMaskColor: Color = Color.Black.copy(alpha = 0.36f),
    val bucketChooserBgColor: Color = topBarBgColor,
    val bucketChooserIndicationColor: Color = Color.White.copy(alpha = 0.2f),
    val bucketChooserMainTextColor: Color = Color.White,
    val bucketChooserCountTextColor: Color = Color.White.copy(alpha = 0.64f),

    val paintEditOptions: List<PaintEdit> = listOf(
        PaintMosaic(2),
        PaintMosaic(1),
        PaintGraffiti(Color.White),
        PaintGraffiti(Color.Black),
        PaintGraffiti(Color.Red),
        PaintGraffiti(Color.Yellow),
        PaintGraffiti(Color.Green),
        PaintGraffiti(Color.Blue),
        PaintGraffiti(Color.Magenta)
    ),
    val paintEditStrokeWidth: Dp = 5.dp
)

val qmuiPhotoPickerDefaultConfig by lazy { QMUIPhotoPickerConfig() }
val QMUILocalPickerConfig = staticCompositionLocalOf { qmuiPhotoPickerDefaultConfig }

@Composable
fun QMUIDefaultPickerConfigProvider(content: @Composable () -> Unit) {
    CompositionLocalProvider(QMUILocalPickerConfig provides qmuiPhotoPickerDefaultConfig) {
        content()
    }
}