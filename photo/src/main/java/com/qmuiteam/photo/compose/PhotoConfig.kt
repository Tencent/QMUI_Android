package com.qmuiteam.photo.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

class QMUIPhotoConfig(
    val blankColor: Color = Color.LightGray
)

val qmuiPhotoDefaultConfig by lazy { QMUIPhotoConfig() }
val QMUILocalPhotoConfig = staticCompositionLocalOf { qmuiPhotoDefaultConfig }


@Composable
fun QMUIDefaultPhotoConfigProvider(content: @Composable () -> Unit) {
    CompositionLocalProvider(QMUILocalPhotoConfig provides qmuiPhotoDefaultConfig) {
        content()
    }
}


@Composable
fun BlankBox() {
    val blankColor = QMUILocalPhotoConfig.current.blankColor
    if (blankColor != Color.Transparent) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(blankColor)
        )
    }
}