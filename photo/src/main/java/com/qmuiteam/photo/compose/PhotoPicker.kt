package com.qmuiteam.photo.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

class QMUIPhotoPickerConfig(
    val backgroundColor: Color = Color(0xFF333333),
    val loadingColor: Color = Color(0xFFFFFFFF),
    val tipTextColor: Color = Color(0xFFFFFFFF),
)

val qmuiPhotoPickerDefaultConfig by lazy { QMUIPhotoPickerConfig() }
val QMUILocalPickerConfig = staticCompositionLocalOf { qmuiPhotoPickerDefaultConfig }

@Composable
fun QMUIDefaultPickerConfigProvider(content: @Composable () -> Unit) {
    CompositionLocalProvider(QMUILocalPickerConfig provides qmuiPhotoPickerDefaultConfig) {
        content()
    }
}