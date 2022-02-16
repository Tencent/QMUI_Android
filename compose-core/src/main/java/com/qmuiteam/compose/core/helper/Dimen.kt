package com.qmuiteam.compose.core.helper

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun OnePx(): Dp {
    return (1 / LocalDensity.current.density).dp
}