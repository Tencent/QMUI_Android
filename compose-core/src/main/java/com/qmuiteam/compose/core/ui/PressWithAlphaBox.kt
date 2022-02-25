package com.qmuiteam.compose.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

@Composable
fun PressWithAlphaBox(
    modifier: Modifier = Modifier,
    enable: Boolean = true,
    pressAlpha: Float = 0.5f,
    disableAlpha: Float = 0.5f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    Box(modifier = Modifier
        .alpha(if (!enable) disableAlpha else if (isPressed.value) pressAlpha else 1f)
        .clickable(enabled = enable, interactionSource = interactionSource, indication = null) {
            onClick?.invoke()
        }
        .then(modifier),
        content = content
    )

}