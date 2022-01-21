package com.qmuiteam.photo.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView

fun View.asBitmap(): Bitmap? {
    if (this is ImageView) {
        val drawable = drawable
        if (drawable != null && drawable is BitmapDrawable) {
            return drawable.bitmap
        }
    }
    return try {
        clearFocus()
        val bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas()
        canvas.setBitmap(bm)
        canvas.save()
        draw(canvas)
        canvas.restore()
        canvas.setBitmap(null)
        bm
    } catch (e: Throwable) {
        null
    }
}