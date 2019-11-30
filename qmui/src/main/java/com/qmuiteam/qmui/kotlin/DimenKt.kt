package com.qmuiteam.qmui.kotlin

import android.content.Context
import android.view.View
import androidx.annotation.DimenRes
import androidx.fragment.app.Fragment

fun Context.dip(value: Int): Int = (value * resources.displayMetrics.density).toInt()
fun Context.dip(value: Float): Int = (value * resources.displayMetrics.density).toInt()
fun Context.sp(value: Int): Int = (value * resources.displayMetrics.scaledDensity).toInt()
fun Context.sp(value: Float): Int = (value * resources.displayMetrics.scaledDensity).toInt()
fun Context.px2dp(px: Int): Float = px.toFloat() / resources.displayMetrics.density
fun Context.px2sp(px: Int): Float = px.toFloat() / resources.displayMetrics.scaledDensity
fun Context.dimen(@DimenRes resource: Int): Int = resources.getDimensionPixelSize(resource)

fun View.dip(value: Int): Int = context.dip(value)
fun View.dip(value: Float): Int = context.dip(value)
fun View.sp(value: Int): Int = context.sp(value)
fun View.sp(value: Float): Int = context.sp(value)
fun View.px2dp(px: Int): Float = context.px2dp(px)
fun View.px2sp(px: Int): Float = context.px2sp(px)
fun View.dimen(@DimenRes resource: Int): Int = context.dimen(resource)

// must be called after attached.
fun Fragment.dip(value: Int): Int = context!!.dip(value)
fun Fragment.dip(value: Float): Int = context!!.dip(value)
fun Fragment.sp(value: Int): Int = context!!.sp(value)
fun Fragment.sp(value: Float): Int = context!!.sp(value)
fun Fragment.px2dp(px: Int): Float = context!!.px2dp(px)
fun Fragment.px2sp(px: Int): Float = context!!.px2sp(px)
fun Fragment.dimen(@DimenRes resource: Int): Int = context!!.dimen(resource)