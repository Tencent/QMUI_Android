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
package com.qmuiteam.qmui.type

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import android.util.SparseArray
import java.util.*

class TypeEnvironment {
    companion object {
        private const val TAG = "TypeEnvironment"
        const val TYPE_TEXT_COLOR = -1
        const val TYPE_BG_COLOR = -2
        const val TYPE_TYPEFACE = -3
        const val TYPE_TEXT_SIZE = -4
        const val TYPE_ALIGNMENT = -5
        const val TYPE_LINE_SPACE = -6
        const val TYPE_PARAGRAPH_SPACE = -7
        const val TYPE_BORDER_TOP_WIDTH = -8
        const val TYPE_BORDER_TOP_COLOR = -9
        const val TYPE_BORDER_RIGHT_WIDTH = -10
        const val TYPE_BORDER_RIGHT_COLOR = -11
        const val TYPE_BORDER_BOTTOM_WIDTH = -12
        const val TYPE_BORDER_BOTTOM_COLOR = -13
        const val TYPE_BORDER_LEFT_WIDTH = -14
        const val TYPE_BORDER_LEFT_COLOR = -15
        const val TYPE_BORDER_PAINT = -16
        const val TYPE_LINE_HEIGHT = -17

        val DEFAULT_LAST_LINE_JUSTIFY_MAX_WIDTH = (Resources.getSystem().displayMetrics.density * 36).toInt()
    }

    enum class Alignment {
        LEFT, RIGHT, CENTER, JUSTIFY
    }

    var widthLimit = 0
        private set
    var heightLimit = 0
        private set


    var alignment: Alignment = Alignment.JUSTIFY

    var lastLineJustifyMaxWidth = DEFAULT_LAST_LINE_JUSTIFY_MAX_WIDTH

    val paint = Paint().apply {
        isAntiAlias = true
        textSize = Resources.getSystem().displayMetrics.scaledDensity * 14f
    }
    val bgPaint = Paint().apply {
        isAntiAlias = true
    }
    private val mCustomProp: SparseArray<Any?> = SparseArray()
    private val mStack = SparseArray<Stack<Any?>>()


    var lineSpace = 0

    var lineHeight = -1

    var paragraphSpace: Int = 0
        get() = field.coerceAtLeast(lineSpace)

    var typeface: Typeface? = null
        set(value) {
            field = value
            paint.typeface = value
        }

    var textSize: Float = paint.textSize
        set(value) {
            field = value
            paint.textSize = value
        }
    var textColor: Int = Color.BLACK
        set(value) {
            field = value
            paint.color = value
        }
    var backgroundColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            bgPaint.color = value
        }

    fun setCustomProp(type: Int, value: Any?) {
        mCustomProp.put(type, value)
    }

    fun setBorderTop(width: Int, color: Int) {
        setCustomProp(TYPE_BORDER_TOP_WIDTH, width)
        setCustomProp(TYPE_BORDER_TOP_COLOR, color)
    }

    val borderTopWidth: Int
        get() = getIntCustomProp(TYPE_BORDER_TOP_WIDTH)
    val borderTopColor: Int
        get() = getIntCustomProp(TYPE_BORDER_TOP_COLOR)

    fun setBorderRight(width: Int, color: Int) {
        setCustomProp(TYPE_BORDER_RIGHT_WIDTH, width)
        setCustomProp(TYPE_BORDER_RIGHT_COLOR, color)
    }

    val borderRightWidth: Int
        get() = getIntCustomProp(TYPE_BORDER_RIGHT_WIDTH)
    val borderRightColor: Int
        get() = getIntCustomProp(TYPE_BORDER_RIGHT_COLOR)

    fun setBorderBottom(width: Int, color: Int) {
        setCustomProp(TYPE_BORDER_BOTTOM_WIDTH, width)
        setCustomProp(TYPE_BORDER_BOTTOM_COLOR, color)
    }

    val borderBottomWidth: Int
        get() = getIntCustomProp(TYPE_BORDER_BOTTOM_WIDTH)
    val borderBottomColor: Int
        get() = getIntCustomProp(TYPE_BORDER_BOTTOM_COLOR)

    fun setBorderLeft(width: Int, color: Int) {
        setCustomProp(TYPE_BORDER_LEFT_WIDTH, width)
        setCustomProp(TYPE_BORDER_LEFT_COLOR, color)
    }

    val borderLeftWidth: Int
        get() = getIntCustomProp(TYPE_BORDER_LEFT_WIDTH)
    val borderLeftColor: Int
        get() = getIntCustomProp(TYPE_BORDER_LEFT_COLOR)
    val borderPaint: Paint
        get() {
            val obj = getCustomProp(TYPE_BORDER_PAINT)
            val paint: Paint
            if (obj == null) {
                paint = Paint()
                paint.isAntiAlias = true
                setCustomProp(TYPE_BORDER_PAINT, paint)
            } else {
                paint = obj as Paint
            }
            return paint
        }

    fun getCustomProp(type: Int): Any? {
        return mCustomProp[type]
    }

    fun getIntCustomProp(type: Int): Int {
        val obj = mCustomProp[type]
        return if (obj !is Int) {
            0
        } else obj
    }

    fun setMeasureLimit(widthLimit: Int, heightLimit: Int) {
        this.widthLimit = widthLimit
        this.heightLimit = heightLimit
    }

    fun snapshot(): TypeEnvironment {
        val env = TypeEnvironment()
        env.setMeasureLimit(widthLimit, heightLimit)
        env.alignment = alignment
        env.lineSpace = lineSpace
        env.lineHeight = lineHeight
        env.paragraphSpace = paragraphSpace
        env.textSize = textSize
        env.typeface = typeface
        env.textColor = textColor
        env.backgroundColor = backgroundColor
        for (i in 0 until mStack.size()) {
            env.mStack.put(mStack.keyAt(i), mStack.valueAt(i).clone() as Stack<Any?>)
        }
        for (i in 0 until mCustomProp.size()) {
            env.setCustomProp(mCustomProp.keyAt(i), mCustomProp.valueAt(i))
        }
        return env
    }

    fun save(type: Int) {
        var stack = mStack[type]
        if (stack == null) {
            stack = Stack()
            mStack.put(type, stack)
        }
        if (type == TYPE_TEXT_COLOR) {
            stack.push(textColor)
        } else if (type == TYPE_BG_COLOR) {
            stack.push(backgroundColor)
        } else if (type == TYPE_TYPEFACE) {
            stack.push(typeface)
        } else if (type == TYPE_TEXT_SIZE) {
            stack.push(textSize)
        } else if (type == TYPE_ALIGNMENT) {
            stack.push(alignment)
        } else if (type == TYPE_LINE_SPACE) {
            stack.push(lineSpace)
        } else if (type == TYPE_PARAGRAPH_SPACE) {
            stack.push(paragraphSpace)
        } else if(type == TYPE_LINE_HEIGHT){
            stack.push(lineHeight)
        } else{
            stack.push(mCustomProp[type])
        }
    }

    fun restore(type: Int) {
        val stack = mStack[type]
        if (stack == null || stack.isEmpty()) {
            Log.d(TAG, "restore (type = $type)with a empty stack.")
            return
        }
        val v = stack.pop()
        restore(type, v)
    }

    private fun restore(type: Int, v: Any?) {
        if (type == TYPE_TEXT_COLOR) {
            textColor = v as Int
        } else if (type == TYPE_BG_COLOR) {
            backgroundColor = v as Int
        } else if (type == TYPE_TYPEFACE) {
            typeface = v as? Typeface
        } else if (type == TYPE_TEXT_SIZE) {
            textSize = v as Float
        } else if (type == TYPE_ALIGNMENT) {
            alignment = v as Alignment
        } else if (type == TYPE_LINE_SPACE) {
            lineSpace = v as Int
        } else if (type == TYPE_PARAGRAPH_SPACE) {
            paragraphSpace = v as Int
        }else if (type == TYPE_LINE_HEIGHT) {
            lineHeight = v as Int
        } else {
            setCustomProp(type, v)
        }
    }

    fun clear() {
        for (i in 0 until mStack.size()) {
            val stack = mStack.valueAt(i)
            if (stack != null && stack.size > 0) {
                while (stack.size > 1) {
                    stack.pop()
                }
                restore(mStack.keyAt(i), stack.pop())
            }
        }
    }

    fun isRunning(): Boolean{
        for (i in 0 until mStack.size()) {
            val stack = mStack.valueAt(i)
            if(stack.size > 0){
                return true
            }
        }
        return false
    }
}