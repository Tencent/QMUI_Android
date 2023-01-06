package com.qmuiteam.photo.compose.picker

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qmuiteam.compose.core.ui.QMUITopBarItem
import com.qmuiteam.compose.core.ui.qmuiPrimaryColor
import kotlinx.coroutines.flow.StateFlow

data class QMUIPhotoPickerConfig(
    var editable: Boolean = true,
    var primaryColor: Color = qmuiPrimaryColor,
    var commonTextButtonTextColor: Color = Color.White,
    var commonSeparatorColor: Color = Color.White.copy(alpha = 0.3f),
    var commonIconNormalTintColor: Color = Color.White.copy(0.9f),
    var commonIconCheckedTintColor: Color = primaryColor,
    var commonIconCheckedTextColor: Color = Color.White.copy(alpha = 0.6f),

    var commonButtonNormalTextColor: Color = Color.White,
    var commonButtonNormalBgColor: Color = primaryColor,
    var commonButtonDisabledTextColor: Color = Color.White.copy(alpha = 0.3f),
    var commonButtonDisableBgColor: Color = Color.White.copy(alpha = 0.15f),
    var commonButtonPressBgColor: Color = primaryColor.copy(alpha = 0.8f),
    var commonButtonPressedTextColor: Color = commonButtonNormalTextColor,

    var topBarBgColor: Color = Color(0xFF222222),
    var toolBarBgColor: Color = topBarBgColor,

    var topBarBucketFactory: (
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
    var topBarSendFactory: (
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

    var screenBgColor: Color = Color(0xFF333333),
    var loadingColor: Color = Color.White,
    var tipTextColor: Color = Color.White,

    var gridPreferredSize: Dp = 80.dp,
    var gridGap: Dp = 2.dp,
    var gridBorderColor: Color = Color.White.copy(alpha = 0.15f),

    var bucketChooserMaskColor: Color = Color.Black.copy(alpha = 0.36f),
    var bucketChooserBgColor: Color = topBarBgColor,
    var bucketChooserIndicationColor: Color = Color.White.copy(alpha = 0.2f),
    var bucketChooserMainTextColor: Color = Color.White,
    var bucketChooserCountTextColor: Color = Color.White.copy(alpha = 0.64f),

    var editPaintOptions: List<EditPaint> = listOf(
        MosaicEditPaint(16),
        MosaicEditPaint(50),
        ColorEditPaint(Color.White),
        ColorEditPaint(Color.Black),
        ColorEditPaint(Color.Red),
        ColorEditPaint(Color.Yellow),
        ColorEditPaint(Color.Green),
        ColorEditPaint(Color.Blue),
        ColorEditPaint(Color.Magenta)
    ),
    var graffitiPaintStrokeWidth: Dp = 5.dp,
    var mosaicPaintStrokeWidth: Dp = 20.dp,

    var textEditMaskColor:Color = Color.Black.copy(0.5f),
    var textEditColorOptions: List<ColorEditPaint> = listOf(
        ColorEditPaint(Color.White),
        ColorEditPaint(Color.Black),
        ColorEditPaint(Color.Red),
        ColorEditPaint(Color.Yellow),
        ColorEditPaint(Color.Green),
        ColorEditPaint(Color.Blue),
        ColorEditPaint(Color.Magenta)
    ),
    var textEditFontSize: TextUnit = 30.sp,
    var textEditLineSpace: TextUnit = 3.sp,
    var textCursorColor: Color = primaryColor,

    var editLayerDeleteAreaNormalBgColor: Color = Color.Black.copy(alpha = 0.3f),
    var editLayerDeleteAreaNormalFocusColor: Color = Color.Red.copy(alpha = 0.6f),
    var photoNotPickMaskColor: Color,
)

val lightConfig by lazy { QMUIPhotoPickerConfig(photoNotPickMaskColor = Color(0x80FFFFFF)) }
val darkConfig by lazy { QMUIPhotoPickerConfig(photoNotPickMaskColor = Color(0x80000000)) }

val qmuiPhotoPickerDefaultConfig by lazy { lightConfig }

val QMUILocalPickerConfig = staticCompositionLocalOf { qmuiPhotoPickerDefaultConfig }

@Composable
fun QMUIDefaultPickerConfigProvider(
    darkTheme: Boolean = isSystemInDarkTheme(),
    configSet: (QMUIPhotoPickerConfig) -> Unit = {},
    content: @Composable () -> Unit
) {
    val config = if (darkTheme) {
        darkConfig
    } else {
        lightConfig
    }.apply {
        configSet(this)
    }

    CompositionLocalProvider(QMUILocalPickerConfig provides config) {
        content()
    }
}