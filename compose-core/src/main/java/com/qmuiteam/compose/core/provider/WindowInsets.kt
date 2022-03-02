package com.qmuiteam.compose.core.provider

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.qmuiteam.compose.core.R

val QMUILocalWindowInsets = staticCompositionLocalOf { WindowInsetsCompat.CONSUMED }

@Composable
fun QMUIWindowInsetsProvider(content: @Composable () -> Unit) {
    val view = LocalView.current
    val windowInsets = remember(view) {
        mutableStateOf(view.getTag(R.id.qmui_window_inset_cache) as? WindowInsetsCompat ?: WindowInsetsCompat.CONSUMED)
    }
    LaunchedEffect(view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, OnApplyWindowInsetsListener { _, insets ->
            windowInsets.value = insets
            view.setTag(R.id.qmui_window_inset_cache, insets)
            return@OnApplyWindowInsetsListener insets
        })
        view.requestApplyInsets()
    }
    CompositionLocalProvider(QMUILocalWindowInsets provides windowInsets.value) {
        content()
    }
}

data class DpInsets(val left: Dp, val top: Dp, val right: Dp, val bottom: Dp) {
    companion object {
        val NONE = DpInsets(0.dp, 0.dp, 0.dp, 0.dp)
    }
}

@Composable
fun Insets.dp(): DpInsets {
    if (this == Insets.NONE) {
        return DpInsets.NONE
    }
    return with(LocalDensity.current) {
        DpInsets(
            (left / density).dp,
            (top / density).dp,
            (right / density).dp,
            (bottom / density).dp
        )
    }
}