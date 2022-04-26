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
import android.util.Log
import android.view.MotionEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.ColorInt
import com.qmuiteam.qmui.type.LineLayout
import com.qmuiteam.qmui.type.TypeEnvironment
import com.qmuiteam.qmui.type.TypeModel
import com.qmuiteam.qmui.type.parser.PlainTextParser
import com.qmuiteam.qmui.type.parser.TextParser
import java.util.*

private const val TAG = "LineTypeView"

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

    private val touchSpanList = arrayListOf<TouchSpan>()
    private var currentTouchSpan: TouchSpan? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        environment.setMeasureLimit(widthSize - paddingLeft - paddingRight, heightSize - paddingTop - paddingBottom)
        lineLayout.measureAndLayout(environment, heightMode == MeasureSpec.EXACTLY)
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
                touchSpanList.clear()
                currentTouchSpan = null
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

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.text = text
        info.contentDescription = text
    }

    fun addClickEffect(
        start: Int, end: Int,
        textColorGetter: (isPressed: Boolean) -> Int,
        bgColorGetter: (isPressed: Boolean) -> Int,
        onClick: (start: Int, end: Int) -> Unit
    ): TypeModel.EffectRemover? {
        val types: MutableList<Int> = ArrayList()
        types.add(TypeEnvironment.TYPE_BG_COLOR)
        types.add(TypeEnvironment.TYPE_TEXT_COLOR)
        return unsafeAddClickEffect(start, end, types, { env, touchSpan ->
            env.textColor = textColorGetter.invoke(touchSpan.isPressed)
            env.backgroundColor = bgColorGetter.invoke(touchSpan.isPressed)
        }, onClick)
    }

    fun unsafeAddClickEffect(
        start: Int, end: Int,
        types: List<Int>, updater: (TypeEnvironment, touchSpan: TouchSpan) -> Unit, onClick: (Int, Int) -> Unit
    ): TypeModel.EffectRemover? {
        val typeModel = lineLayout.typeModel ?: return null
        val touchSpan = TouchSpan(start, end, onClick)
        val remover = typeModel.unsafeAddEffect(start, end, types) {
            updater.invoke(it, touchSpan)
        }
        touchSpanList.add(touchSpan)
        return TypeModel.EffectRemover {
            remover?.remove()
            touchSpanList.remove(touchSpan)
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val typeModel = lineLayout.typeModel ?: return super.onTouchEvent(event)
        if (touchSpanList.isEmpty()) {
            return super.onTouchEvent(event)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val current = currentTouchSpan
                if (current != null) {
                    Log.i(TAG, "the currentTouchSpan is not null when touch down.")
                    current.isPressed = false
                }
                val touchSpan = findCurrentTouchSpan(typeModel, event.x, event.y)
                if (touchSpan != null) {
                    touchSpan.isPressed = true
                    currentTouchSpan = touchSpan
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val current = currentTouchSpan
                if (current != null) {
                    if (!isSpanTouched(typeModel, current, event.x, event.y)) {
                        current.isPressed = false
                        val touchSpan = findCurrentTouchSpan(typeModel, event.x, event.y)
                        if (touchSpan != null) {
                            touchSpan.isPressed = true
                            currentTouchSpan = touchSpan
                        } else {
                            currentTouchSpan = null
                        }
                        invalidate()
                    }
                    return true
                } else {
                    val touchSpan = findCurrentTouchSpan(typeModel, event.x, event.y)
                    if (touchSpan != null) {
                        touchSpan.isPressed = true
                        currentTouchSpan = touchSpan
                        invalidate()
                        return true
                    }
                }
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                val current = currentTouchSpan
                if (current != null) {
                    currentTouchSpan = null
                    current.isPressed = false
                    if (event.action == MotionEvent.ACTION_UP) {
                        current.onClick.invoke(current.start, current.end)
                    }
                    invalidate()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun findCurrentTouchSpan(typeModel: TypeModel, x: Float, y: Float): TouchSpan? {
        for (i in 0 until touchSpanList.size) {
            val touchSpan = touchSpanList[i]
            if (isSpanTouched(typeModel, touchSpan, x, y)) {
                return touchSpan
            }
        }
        return null
    }

    private fun isSpanTouched(typeModel: TypeModel, touchSpan: TouchSpan, x: Float, y: Float): Boolean {
        val start = typeModel.getByPos(touchSpan.start) ?: return false
        val end = typeModel.getByPos(touchSpan.end) ?: return false
        if (start.y + paddingTop > y || end.y + paddingTop + end.measureHeight < y) {
            return false
        } else if (start.y == end.y) { // in one line
            return !(start.x + paddingLeft > x || end.x + paddingLeft < x)
        } else {
            // in muti line
            if (x < start.x + paddingLeft && y < start.y + start.measureHeight + paddingTop) {
                return false
            } else if (x > end.x + end.measureWidth + paddingLeft && y > end.y) {
                return false
            }
            return true
        }
    }

    class TouchSpan(val start: Int, val end: Int, val onClick: (Int, Int) -> Unit) {
        var isPressed: Boolean = false
            internal set
    }
}