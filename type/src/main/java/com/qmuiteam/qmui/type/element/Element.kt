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
package com.qmuiteam.qmui.type.element

import android.graphics.Canvas
import android.graphics.Color
import com.qmuiteam.qmui.type.EnvironmentUpdater
import com.qmuiteam.qmui.type.TypeEnvironment
import java.util.*
import kotlin.collections.ArrayList

abstract class Element(val text: CharSequence, val index: Int, val start: Int) {
    companion object {
        const val VISIBLE = 0
        const val GONE = 1
        const val WORD_PART_WHOLE = 0
        const val WORD_PART_START = 1
        const val WORD_PART_MIDDLE = 2
        const val WORD_PART_END = 3
        const val LINE_BREAK_TYPE_NORMAL = 0
        const val LINE_BREAK_TYPE_NOT_START = 1
        const val LINE_BREAK_TYPE_NOT_END = 2
        const val LINE_BREAK_WORD_BREAK_ALLOWED = 3
        private val NOT_START_CHARS = charArrayOf(
                ',', '.', ';', ']', '>', ')', '?', '"', '\'', '!', ':', '}', '」',
                '，', '。', '；', '、', '】', '》', '）', '？', '”', '！', '：', '』')
        private val NOT_END_CHARS = charArrayOf(
                '(', '<', '[', '{', '“', '「', '『', '（', '《'
        )

        init {
            Arrays.sort(NOT_START_CHARS)
            Arrays.sort(NOT_END_CHARS)
        }
    }

    var prevEffect: Element? = null
        private set
    var nextEffect: Element? = null
        private set

    var wordPart = WORD_PART_WHOLE
    var lineBreakType = LINE_BREAK_TYPE_NORMAL
    var visible = VISIBLE


    var measureWidth = 0
        private set
    var measureHeight = 0
        private set
    var x = 0
    var y = 0
    var baseLine = 0
    var nextGapWidth = 0

    private var saveTypeList: MutableList<Int>? = null
    private var restoreTypeList: MutableList<Int>? = null
    private var environmentUpdaterList: MutableList<EnvironmentUpdater>? = null

    val length: Int = text.length

    private var _prev: Element? = null
    private var _next: Element? = null

    var next: Element?
        get() = _next
        set(element) {
            _next = element
            if (element != null) {
                element._prev = this
            }
        }
    var prev: Element?
        get() = _prev
        set(element) {
            _prev = element
            if (element != null) {
                element._next = this
            }
        }

    private val rightWithGap: Int
        get() = x + measureWidth + nextGapWidth

    init {
        if(text.length == 1){
            if (Arrays.binarySearch(NOT_START_CHARS, text[0]) >= 0) {
                lineBreakType = LINE_BREAK_TYPE_NOT_START
            } else if (Arrays.binarySearch(NOT_END_CHARS, text[0]) >= 0) {
                lineBreakType = LINE_BREAK_TYPE_NOT_END
            }
        }
    }

    fun insertEffectTo(head: Element): Element {
        if (head === this) {
            return head
        }
        if (index < head.index) {
            head.prevEffect = this
            nextEffect = head
            return this
        }
        var current: Element = head
        var next = head.nextEffect
        while (next != null) {
            if (next === this) {
                // already in list
                return head
            }
            if (next.index > index) {
                current.nextEffect = this
                next.prevEffect = this
                prevEffect = current
                nextEffect = next
                return head
            }
            current = next
            next = next.nextEffect
        }
        current.nextEffect = this
        prevEffect = current
        nextEffect = null
        return head
    }

    fun removeFromEffectListIfNeeded(head: Element?): Element? {
        if(head == null){
            return null
        }
        val noSaveType = saveTypeList.isNullOrEmpty()
        val noRestoreType = restoreTypeList.isNullOrEmpty()
        if (noSaveType && noRestoreType) {
            val prev = prevEffect
            val next = nextEffect
            if (prev != null) {
                prev.nextEffect = next
                prevEffect = null
            }
            if (next != null) {
                next.prevEffect = prev
                nextEffect = null
            }
            if (head === this) {
                return next
            }
        }
        return head
    }

    fun addSaveType(type: Int) {
        val list = saveTypeList ?:  ArrayList<Int>().also { saveTypeList = it}
        list.add(type)
    }

    fun removeSaveType(type: Int) {
        val list = saveTypeList ?: return
        for (i in list.indices) {
            if (list[i] == type) {
                list.removeAt(i)
                break
            }
        }
    }

    fun addRestoreType(type: Int) {
        val list = restoreTypeList ?:  ArrayList<Int>().also { restoreTypeList = it}
        list.add(type)
    }

    fun removeStoreType(type: Int) {
        val list = restoreTypeList ?: return
        for (i in list.indices) {
            if (list[i] == type) {
                list.removeAt(i)
                break
            }
        }
    }

    fun hasEnvironmentUpdater(): Boolean {
        return (environmentUpdaterList?.size ?: 0) > 0
    }

    fun hasSaveType(): Boolean {
        return !saveTypeList.isNullOrEmpty()
    }

    fun hasRestoreType(): Boolean {
        return !restoreTypeList.isNullOrEmpty()
    }

    fun addEnvironmentUpdater(environmentUpdater: EnvironmentUpdater) {
        val list = environmentUpdaterList ?: ArrayList<EnvironmentUpdater>().also { environmentUpdaterList = it }
        list.add(environmentUpdater)
    }

    fun removeEnvironmentUpdater(environmentUpdater: EnvironmentUpdater) {
        val list = environmentUpdaterList ?: return
        list.remove(environmentUpdater)
    }

    fun addSingleEnvironmentUpdater(changedTypes: List<Int>?, environmentUpdater: EnvironmentUpdater) {
        if (changedTypes != null) {
            for (type in changedTypes) {
                addSaveType(type)
                addRestoreType(type)
            }
        }
        addEnvironmentUpdater(environmentUpdater)
    }

    fun move(environment: TypeEnvironment) {
        if (hasEnvironmentUpdater()) {
            updateEnv(environment)
            restoreEnv(environment)
        }
    }

    override fun toString(): String {
        return text.toString()
    }


    protected fun setMeasureDimen(measureWidth: Int, measureHeight: Int, baseline: Int) {
        this.measureWidth = measureWidth
        this.measureHeight = measureHeight
        baseLine = baseline
    }


    fun measure(env: TypeEnvironment) {
        updateEnv(env)
        onMeasure(env)
        restoreEnv(env)
    }

    fun draw(env: TypeEnvironment, canvas: Canvas) {
        updateEnv(env)
        if (visible == VISIBLE) {
            onDraw(env, canvas)
        }
        restoreEnv(env)
    }

    fun updateEnv(env: TypeEnvironment) {
        saveTypeList?.forEach {
            env.save(it)
        }
        environmentUpdaterList?.forEach {
            it.update(env)
        }
    }

    fun restoreEnv(env: TypeEnvironment) {
        restoreTypeList?.forEach {
            env.restore(it)
        }
    }

    protected abstract fun onMeasure(env: TypeEnvironment)
    protected abstract fun onDraw(env: TypeEnvironment, canvas: Canvas)
    protected fun drawBg(env: TypeEnvironment, canvas: Canvas) {
        if (env.backgroundColor != Color.TRANSPARENT) {
            canvas.drawRect(x.toFloat(), y.toFloat(), rightWithGap.toFloat(), (y + measureHeight).toFloat(), env.bgPaint)
        }
    }

    protected fun drawBorder(env: TypeEnvironment, canvas: Canvas) {
        val paint = env.borderPaint
        if (env.borderLeftWidth > 0) {
            paint.color = env.borderLeftColor
            canvas.drawRect(x.toFloat(), y.toFloat(), (x + env.borderLeftWidth).toFloat(), (y + measureHeight).toFloat(), paint)
        }
        if (env.borderTopWidth > 0) {
            paint.color = env.borderTopColor
            canvas.drawRect(x.toFloat(), y.toFloat(), rightWithGap.toFloat(), (y + env.borderTopWidth).toFloat(), paint)
        }
        if (env.borderRightWidth > 0) {
            paint.color = env.borderRightColor
            canvas.drawRect((x + measureWidth - env.borderRightWidth).toFloat(), y.toFloat(),
                    (x + measureWidth).toFloat(), (y + measureHeight).toFloat(), paint)
        }
        if (env.borderBottomWidth > 0) {
            paint.color = env.borderBottomColor
            canvas.drawRect(x.toFloat(), (y + measureHeight - env.borderBottomWidth).toFloat(),
                    rightWithGap.toFloat(), (y + measureHeight).toFloat(), paint)
        }
    }
}