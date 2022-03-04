package com.qmuiteam.photo.compose.picker

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal class TextEditLayer(
    val text: String,
    val color: Color,
    val reverse: Boolean,
) {
    @Composable
    fun Content(
        layoutInfo: PickerPhotoLayoutInfo,
        onEdit:() -> Unit,
        onDelete:()-> Unit
    ) {
        BoxWithConstraints {

        }
    }
}