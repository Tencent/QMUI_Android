package com.qmuiteam.photo.compose.picker

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

interface EditLayer {
    fun DrawScope.draw()
    fun drawToBitmap()
}

sealed class PaintEditLayer(val path: Path) : EditLayer

class GraffitiEditLayer(
    path: Path,
    val color: Color,
    val strokeWidth: Float
) : PaintEditLayer(path) {

    override fun DrawScope.draw() {
        drawPath(
            path,
            color = color,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }

    override fun drawToBitmap() {

    }
}

class MosaicEditLayer(path: Path) : PaintEditLayer(path) {
    override fun DrawScope.draw() {

    }

    override fun drawToBitmap() {

    }
}