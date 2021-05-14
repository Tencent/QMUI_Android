/*
 * Tencent is pleased to support the open source community by making QMUI_Android available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qmuiteam.qmui.type.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.text.TextUtils
import android.util.AttributeSet
import androidx.annotation.ColorInt
import com.qmuiteam.qmui.type.LineLayout
import com.qmuiteam.qmui.type.TypeModel
import com.qmuiteam.qmui.type.parser.PlainTextParser
import com.qmuiteam.qmui.type.parser.TextParser

open class LineTypeView : BaseTypeView {

    val lineLayout = LineLayout()

    var textParser: TextParser = PlainTextParser.instance
        set(value) {
            if (field != value) {
                field = value
                lineLayout.typeModel = value.parse(text)
                requestLayout()
            }
        }

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        environment.setMeasureLimit(widthSize - paddingLeft - paddingRight, heightSize - paddingTop - paddingBottom)
        lineLayout.measureAndLayout(environment)
        val usedWidth = if (widthMode == MeasureSpec.AT_MOST) {
            lineLayout.maxLayoutWidth + paddingLeft + paddingRight
        } else widthSize
        val usedHeight = if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            lineLayout.contentHeight + paddingTop + paddingBottom
        } else heightSize
        setMeasuredDimension(usedWidth, usedHeight)
    }

    var text: CharSequence? = null
        set(value) {
            if (field != value) {
                field = value
                lineLayout.typeModel = textParser.parse(value)
                requestLayout()
            }
        }

    var ellipsized: TextUtils.TruncateAt?
        get() = lineLayout.ellipsize
        set(value) {
            if (lineLayout.ellipsize != value) {
                lineLayout.ellipsize = value
                requestLayout()
            }
        }

    var maxLines: Int
        get() = lineLayout.maxLines
        set(value) {
            if (lineLayout.maxLines != value) {
                lineLayout.maxLines = value
                requestLayout()
            }
        }


    fun addBgEffect(start: Int, end: Int, @ColorInt color: Int): TypeModel.EffectRemover? {
        val typeModel = lineLayout.typeModel ?: return null
        val remover = typeModel.addBgEffect(start, end, color) ?: return null
        invalidate()
        return remover
    }

    fun addTextColorEffect(start: Int, end: Int, @ColorInt color: Int): TypeModel.EffectRemover? {
        val typeModel = lineLayout.typeModel ?: return null
        val remover = typeModel.addTextColorEffect(start, end, color) ?: return null
        invalidate()
        return remover
    }

    fun addUnderLineEffect(start: Int, end: Int, @ColorInt color: Int, height: Int): TypeModel.EffectRemover? {
        val typeModel = lineLayout.typeModel ?: return null
        val remover = typeModel.addUnderLineEffect(start, end, color, height) ?: return null
        invalidate()
        return remover
    }

    fun addTypefaceEffect(start: Int, end: Int, typeface: Typeface): TypeModel.EffectRemover? {
        val typeModel = lineLayout.typeModel ?: return null
        val remover = typeModel.addTypefaceEffect(start, end, typeface) ?: return null
        requestLayout()
        return remover
    }

    fun addTextSizeEffect(start: Int, end: Int, textSize: Float): TypeModel.EffectRemover? {
        val typeModel = lineLayout.typeModel ?: return null
        val remover = typeModel.addTextSizeEffect(start, end, textSize) ?: return null
        requestLayout()
        return remover
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        lineLayout.draw(canvas, environment)
        canvas.restore()
    }
}