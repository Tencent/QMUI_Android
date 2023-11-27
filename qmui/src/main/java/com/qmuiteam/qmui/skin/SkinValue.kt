package com.qmuiteam.qmui.skin

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.qmuiteam.qmui.util.QMUIResHelper

interface SkinValue {

    fun getFloat(context: Context, attr: Int): Float
    fun getColor(context: Context, attr: Int): Int
    fun getColorStateList(context: Context, attr: Int): ColorStateList?
    fun getDrawable(context: Context, attr: Int): Drawable?
}

class ThemeSkinValue(val theme: Resources.Theme): SkinValue {

    override fun getFloat(context: Context, attr: Int): Float {
        return QMUIResHelper.getAttrFloatValue(theme, attr)
    }

    override fun getColorStateList(context: Context, attr: Int): ColorStateList? {
        return QMUIResHelper.getAttrColorStateList(context, theme, attr)
    }
    override fun getColor(context: Context, attr: Int): Int {
        return QMUIResHelper.getAttrColor(theme, attr)
    }

    override fun getDrawable(context: Context, attr: Int): Drawable? {
        return QMUIResHelper.getAttrDrawable(context, theme, attr)
    }
}

class RemoteSkinValue(val theme: Resources.Theme?, val map: Map<String, Any>): SkinValue {
    override fun getColor(context: Context, attr: Int): Int {
        return kotlin.runCatching {
            val name = context.resources.getResourceEntryName(attr)
            map[name] as Int
        }.getOrNull() ?: QMUIResHelper.getAttrColor(theme, attr)

    }

    override fun getColorStateList(context: Context, attr: Int): ColorStateList? {
        return kotlin.runCatching {
            val name = context.resources.getResourceEntryName(attr)
            // 可以选用其它构造器, 看自己怎么自定义
            ColorStateList.valueOf(map[name] as Int)
        }.getOrNull() ?:  QMUIResHelper.getAttrColorStateList(context, theme, attr)
    }

    override fun getFloat(context: Context, attr: Int): Float {
        return kotlin.runCatching {
            val name = context.resources.getResourceEntryName(attr)
            map[name] as Float
        }.getOrNull() ?: QMUIResHelper.getAttrFloatValue(theme, attr)
    }

    override fun getDrawable(context: Context, attr: Int): Drawable? {
        return kotlin.runCatching {
            val name = context.resources.getResourceEntryName(attr)
            val bitmap = BitmapFactory.decodeFile(map[name] as String)
            BitmapDrawable(context.resources, bitmap)
        }.getOrNull() ?: QMUIResHelper.getAttrDrawable(context, theme, attr)
    }
}