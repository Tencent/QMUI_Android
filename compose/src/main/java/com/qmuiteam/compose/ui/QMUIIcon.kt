package com.qmuiteam.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.qmuiteam.compose.R

@Composable
fun QMUIChevronIcon(tint: Color? = null) {
    Image(
        painter = painterResource(id = R.drawable.ic_qmui_chevron),
        contentDescription = "",
        colorFilter = tint?.let { ColorFilter.tint(it) }
    )
}

enum class CheckStatus {
    none, partial, checked
}

@Composable
fun QMUICheckBox(
    status: CheckStatus = CheckStatus.none,
    isEnabled: Boolean = true,
    tint: Color?
) {
    val icon = when (status) {
        CheckStatus.checked -> R.drawable.ic_qmui_checkbox_checked
        CheckStatus.partial -> R.drawable.ic_qmui_checkbox_partial
        else -> R.drawable.ic_qmui_checkbox_normal
    }
    Image(
        painter = painterResource(id = icon),
        contentDescription = "",
        colorFilter = tint?.let { ColorFilter.tint(it) },
        modifier = Modifier.let {
            if (isEnabled) {
                it
            } else {
                it.alpha(0.5f)
            }
        }
    )
}

@Composable
fun QMUIMarkIcon(tint: Color? = null) {
    Image(
        painter = painterResource(id = R.drawable.ic_qmui_mark),
        contentDescription = "",
        colorFilter = tint?.let { ColorFilter.tint(it) }
    )
}