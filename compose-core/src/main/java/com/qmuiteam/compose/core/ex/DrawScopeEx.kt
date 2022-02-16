package com.qmuiteam.compose.core.ex

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qmuiteam.compose.core.ui.qmuiSeparatorColor

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