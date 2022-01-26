package com.qmuiteam.photo.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.Size
import android.view.View
import android.view.WindowManager
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
        e.printStackTrace()
        null
    }
}

fun getWindowSize(context: Context): Size {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val windowMetrics = wm.currentWindowMetrics
        Size(windowMetrics.bounds.width(), windowMetrics.bounds.height())
    } else {
        val displayMetrics = context.resources.displayMetrics
        Size(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }
}