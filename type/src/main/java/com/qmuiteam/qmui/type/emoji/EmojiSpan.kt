package com.qmuiteam.qmui.type.emoji

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan
import com.qmuiteam.qmui.type.view.EmojiEditText

class EmojiSpan(drawable: Drawable, val emojiSizeGetter: () -> Int) : ImageSpan(drawable) {
    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        val emojiSize = emojiSizeGetter()
        val rect = drawable.bounds
        rect.left = 0
        rect.top = 0
        rect.right = if(emojiSize == EmojiEditText.EmojiOriginSize) drawable.intrinsicWidth else emojiSize
        rect.bottom = if(emojiSize == EmojiEditText.EmojiOriginSize) drawable.intrinsicHeight else emojiSize
        drawable.bounds = rect
        if (fm != null) {
            val fontMetricsHeight = fm.descent - fm.ascent
            if(fontMetricsHeight < rect.bottom){
                val ratio = rect.bottom.toFloat() / fontMetricsHeight.toFloat()
                fm.ascent = (fm.ascent * ratio).toInt()
                fm.descent = (fm.descent * ratio).toInt()
                fm.top = fm.ascent
                fm.bottom = fm.descent
            }
        }

        return rect.right
    }

    override fun draw(
        canvas: Canvas, text: CharSequence?, start: Int, end: Int,
        x: Float, top: Int, y: Int, bottom: Int, paint: Paint
    ) {
        canvas.save()
        val rect = drawable.bounds
        val fontMetricsInt = paint.fontMetricsInt
        val fontTop = y + fontMetricsInt.top
        val fontMetricsHeight = fontMetricsInt.bottom - fontMetricsInt.top
        val iconHeight = rect.height()
        val iconTop = fontTop + (fontMetricsHeight - iconHeight) / 2
        canvas.translate(x, iconTop.toFloat())
        drawable.draw(canvas)
        canvas.restore()
    }
}