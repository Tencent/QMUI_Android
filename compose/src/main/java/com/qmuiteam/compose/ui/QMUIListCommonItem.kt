package com.qmuiteam.compose.ui

import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

interface QMUIItemAccessory {
    @Composable
    fun Compose()
}

class QMUIItemAccessoryChevron(val tint: Color) : QMUIItemAccessory {
    @Composable
    override fun Compose() {

    }
}

fun DrawScope.drawTopSeparator(color: Color = qmuiSeparatorColor, insetStart: Dp = 0.dp, insetEnd: Dp = 0.dp) {
    drawLine(
        color = color,
        start = Offset(insetStart.toPx(), 0f),
        end = Offset(size.width - insetEnd.toPx(), 0f),
        cap = StrokeCap.Square
    )
}

fun DrawScope.drawBottomSeparator(color: Color = qmuiSeparatorColor, insetStart: Dp = 0.dp, insetEnd: Dp = 0.dp) {
    drawLine(
        color = color,
        start = Offset(insetStart.toPx(), size.height),
        end = Offset(size.width - insetEnd.toPx(), size.height),
        cap = StrokeCap.Square
    )
}

fun DrawScope.drawLeftSeparator(color: Color = qmuiSeparatorColor, insetStart: Dp = 0.dp, insetEnd: Dp = 0.dp) {
    drawLine(
        color = color,
        start = Offset(0f, insetStart.toPx()),
        end = Offset(0f, size.height - insetEnd.toPx()),
        cap = StrokeCap.Square
    )
}

fun DrawScope.drawRightSeparator(color: Color = qmuiSeparatorColor, insetStart: Dp = 0.dp, insetEnd: Dp = 0.dp) {
    drawLine(
        color = color,
        start = Offset(size.width, insetStart.toPx()),
        end = Offset(size.width, size.height - insetEnd.toPx()),
        cap = StrokeCap.Square
    )
}

@Composable
fun QMUIItem(
    title: String,
    detail: String = "",
    background: Color = Color.Transparent,
    indication: Indication = rememberRipple(color = qmuiIndicationColor),
    titleFontSize: TextUnit = 16.sp,
    titleOnlyFontSize: TextUnit = 17.sp,
    titleColor: Color = qmuiTextMainColor,
    titleFontWeight: FontWeight = FontWeight.Medium,
    titleFontFamily: FontFamily? = null,
    titleLineHeight: TextUnit = 20.sp,
    detailFontSize: TextUnit = 12.sp,
    detailColor: Color = qmuiTextDescColor,
    detailFontWeight: FontWeight = FontWeight.Normal,
    detailFontFamily: FontFamily? = null,
    detailLineHeight: TextUnit = 17.sp,
    minHeight: Dp = 56.dp,
    paddingHor: Dp = qmuiCommonHorSpace,
    paddingVer: Dp = 12.dp,
    gapBetweenTitleAndDetail: Dp = 4.dp,
    accessory: QMUIItemAccessory? = null,
    onDrawOver: (DrawScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = minHeight)
        .background(background)
        .drawWithContent {
            drawContent()
            onDrawOver?.invoke(this)
        }
        .clickable(
            enabled = onClick != null,
            interactionSource = remember { MutableInteractionSource() },
            indication = indication
        ) {
            onClick?.invoke()
        }
        .padding(horizontal = paddingHor, vertical = paddingVer),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = titleColor,
                modifier = Modifier.fillMaxWidth(),
                fontSize = if (detail.isNotBlank()) titleFontSize else titleOnlyFontSize,
                fontWeight = titleFontWeight,
                fontFamily = titleFontFamily,
                lineHeight = titleLineHeight
            )
            if (detail.isNotBlank()) {
                Text(
                    text = detail,
                    color = detailColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = gapBetweenTitleAndDetail),
                    fontSize = detailFontSize,
                    fontWeight = detailFontWeight,
                    fontFamily = detailFontFamily,
                    lineHeight = detailLineHeight
                )
            }

        }
        accessory?.Compose()
    }
}