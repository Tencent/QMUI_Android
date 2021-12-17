package com.qmuiteam.compose.modal

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DefaultDialogPaddingHor = 20.dp

@Composable
fun QMUIDialog(
    modal: QMUIModal,
    horEdge: Dp = 20.dp,
    verEdge: Dp = 20.dp,
    widthLimit: Dp = 360.dp,
    radius: Dp = 12.dp,
    background: Color = Color.White,
    content: @Composable (QMUIModal) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horEdge, vertical = verEdge),
        contentAlignment = Alignment.Center
    ) {
        var modifier = if (widthLimit < maxWidth) {
            Modifier.width(widthLimit)
        } else {
            Modifier.fillMaxWidth()
        }
        if (radius > 0.dp) {
            modifier = modifier.clip(RoundedCornerShape(radius))
        }
        modifier = modifier
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { }
        Box(modifier = modifier) {
            content(modal)
        }
    }
}

@Composable
fun QMUIDialogTitle(
    text: String,
    fontSize: TextUnit = 15.sp,
    textAlign: TextAlign? = null,
    color: Color = Color.Black,
    fontWeight: FontWeight? = FontWeight.Bold,
    fontFamily: FontFamily? = null,
    maxLines: Int = Int.MAX_VALUE,
    lineHeight: TextUnit = 20.sp,
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 24.dp,
                start = DefaultDialogPaddingHor,
                end = DefaultDialogPaddingHor,
            ),
        textAlign = textAlign,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        maxLines = maxLines,
        lineHeight = lineHeight
    )
}

@Composable
fun QMUIDialogMessageContent(
    text: String,
    fontSize: TextUnit = 14.sp,
    textAlign: TextAlign? = null,
    color: Color = Color.Black,
    fontWeight: FontWeight? = FontWeight.Bold,
    fontFamily: FontFamily? = null,
    maxLines: Int = Int.MAX_VALUE,
    lineHeight: TextUnit = 16.sp,
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = DefaultDialogPaddingHor,
                end = DefaultDialogPaddingHor,
                top = 16.dp,
                bottom = 24.dp
            ),
        textAlign = textAlign,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        maxLines = maxLines,
        lineHeight = lineHeight
    )
}


fun View.qmuiDialog(
    mask: Color = DefaultMaskColor,
    systemCancellable: Boolean = true,
    maskCancellable: Boolean = true,
    modalHostProvider: ModalHostProvider = DefaultModalHostProvider,
    horEdge: Dp = 20.dp,
    verEdge: Dp = 20.dp,
    widthLimit: Dp = 360.dp,
    radius: Dp = 12.dp,
    background: Color = Color.White,
    content: @Composable (QMUIModal) -> Unit
): QMUIModal {
    return qmuiModal(mask, systemCancellable, maskCancellable, modalHostProvider) { modal ->
        QMUIDialog(modal, horEdge, verEdge, widthLimit, radius, background, content)
    }
}

fun View.qmuiStillDialog(
    mask: Color = DefaultMaskColor,
    systemCancellable: Boolean = true,
    maskCancellable: Boolean = true,
    modalHostProvider: ModalHostProvider = DefaultModalHostProvider,
    horEdge: Dp = 20.dp,
    verEdge: Dp = 20.dp,
    widthLimit: Dp = 360.dp,
    radius: Dp = 12.dp,
    background: Color = Color.White,
    content: @Composable (QMUIModal) -> Unit
): QMUIModal {
    return qmuiStillModal(mask, systemCancellable, maskCancellable, modalHostProvider) { modal ->
        QMUIDialog(modal, horEdge, verEdge, widthLimit, radius, background, content)
    }
}