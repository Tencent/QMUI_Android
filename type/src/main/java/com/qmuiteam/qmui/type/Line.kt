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

import android.graphics.Canvas
import androidx.core.util.Pools
import com.qmuiteam.qmui.type.TypeEnvironment
import com.qmuiteam.qmui.type.element.BreakWordLineElement
import com.qmuiteam.qmui.type.element.Element
import com.qmuiteam.qmui.type.element.NextParagraphElement
import com.qmuiteam.qmui.type.element.TextElement
import java.util.*

class Line private constructor() {
    companion object {
        private val sLinePool: Pools.Pool<Line> = Pools.SimplePool(16)

        fun acquire(): Line {
            var line = sLinePool.acquire()
            if (line == null) {
                line = Line()
            }
            return line
        }
    }

    var x = 0
    var y = 0
    var widthLimit = 0
        private set
    var contentWidth = 0
        private set
    var contentHeight = 0
        private set
    var layoutWidth = 0
        private set
    private val mElements = LinkedList<Element>()
    private var mVisibleChanged: HashMap<Element, Int>? = null


    val size: Int
        get() = mElements.size

    fun init(x: Int, y: Int, widthLimit: Int) {
        this.x = x
        this.y = y
        this.widthLimit = widthLimit
    }

    fun add(element: Element) {
        mElements.add(element)
        contentWidth += element.measureWidth
        contentHeight = contentHeight.coerceAtLeast(element.measureHeight)
    }

    fun addFirst(element: Element) {
        mElements.add(0, element)
        contentWidth += element.measureWidth
        contentHeight = contentHeight.coerceAtLeast(element.measureHeight)
    }

    fun first(): Element? {
        return if (mElements.isEmpty()) null else mElements[0]
    }

    fun move(environment: TypeEnvironment?) {
        for (el in mElements) {
            el.move(environment!!)
        }
    }

    fun handleWordBreak(environment: TypeEnvironment?): List<Element>? {
        if (mElements.size == 0) {
            return null
        }
        var lastIndex = mElements.size - 1
        val last = mElements[lastIndex]
        val next = last.next
        val back: MutableList<Element> = LinkedList()
        if (last.wordPart == Element.WORD_PART_WHOLE) {
            if (last.lineBreakType == Element.LINE_BREAK_TYPE_NOT_END ||
                    next != null && next.lineBreakType == Element.LINE_BREAK_TYPE_NOT_START) {
                mElements.removeAt(lastIndex)
                back.add(last)
            }
        } else if (last.wordPart == Element.WORD_PART_END && next != null && next.lineBreakType != Element.LINE_BREAK_TYPE_NOT_START) {
            // do nothing
        } else if (last.wordPart == Element.WORD_PART_START) {
            mElements.removeAt(lastIndex)
            back.add(last)
        } else {
            back.add(last)
            mElements.removeAt(lastIndex)
            lastIndex--
            val min = Math.max(0, lastIndex - 30) // try 30 letter.
            var find = false
            while (lastIndex > min) {
                val el = mElements[lastIndex]
                if (el.wordPart == Element.WORD_PART_WHOLE || el.wordPart == Element.WORD_PART_END) {
                    find = true
                    break
                } else if (el.lineBreakType == Element.LINE_BREAK_WORD_BREAK_ALLOWED) {
                    // TODO what if environment had changed after break? the measure may be wrong
                    val b = BreakWordLineElement()
                    b.measure(environment!!)
                    add(b)
                    find = true
                    break
                } else {
                    back.add(0, el)
                    mElements.removeAt(lastIndex)
                    lastIndex--
                }
            }
            if (!find) {
                // give up
                mElements.addAll(back)
                return null
            }
        }
        if (back.isEmpty()) {
            return null
        }
        for (el in back) {
            contentWidth = (contentWidth -el.measureWidth).toInt()
        }
        return back
    }

    private fun hideLastIfSpaceIfNeeded(dropLastIfSpace: Boolean): Boolean {
        val last = mElements[mElements.size - 1]
        if (dropLastIfSpace && last is TextElement && last.length == 1 && last.text[0] == ' ' && last.visible != Element.GONE) {
            changeVisibleInner(last, Element.GONE)
            contentWidth = (contentWidth - last.measureWidth).toInt()
            return true
        }
        return false
    }

    private fun changeVisibleInner(element: Element, visible: Int) {
        val oldVal = element.visible
        if (visible == oldVal) {
            return
        }
        if (mVisibleChanged == null) {
            mVisibleChanged = HashMap()
        }
        mVisibleChanged!![element] = oldVal
        element.visible = visible
    }

    private fun calculateGapCount(): Int {
        var ret = 0
        for (i in 1 until mElements.size) {
            val el = mElements[i]
            if (el.visible != Element.GONE &&
                    (el.wordPart == Element.WORD_PART_WHOLE ||
                            el.wordPart == Element.WORD_PART_START)) {
                ret++
            }
        }
        return ret
    }

    val isMiddleParagraphEndLine: Boolean
        get() = !mElements.isEmpty() && mElements[mElements.size - 1] is NextParagraphElement

    fun layout(env: TypeEnvironment, dropLastIfSpace: Boolean, isEnd: Boolean) {
        if (mElements.isEmpty()) {
            return
        }
        hideLastIfSpaceIfNeeded(dropLastIfSpace)
        layoutWidth = contentWidth
        val alignment = env.alignment
        var start = x
        var addSpace = 0
        if (alignment === TypeEnvironment.Alignment.RIGHT) {
            start = x + widthLimit - contentWidth
        } else if (alignment === TypeEnvironment.Alignment.CENTER) {
            start = x + (widthLimit - contentWidth) / 2
        } else if (alignment === TypeEnvironment.Alignment.JUSTIFY) {
            val remain = widthLimit - contentWidth
            if (!(isEnd || isMiddleParagraphEndLine) || remain < env.lastLineJustifyMaxWidth) {
                val gapCount = calculateGapCount()
                if (gapCount > 0) {
                    addSpace = remain / gapCount
                    layoutWidth = widthLimit
                }
            }
        }
        var x = start
        for (i in mElements.indices) {
            val el = mElements[i]
            if (i > 0 && (el.wordPart == Element.WORD_PART_WHOLE
                            || el.wordPart == Element.WORD_PART_START)) {
                x += addSpace
                mElements[i - 1].nextGapWidth = addSpace
            }
            el.x = x
            x += el.measureWidth
            el.y = y + (contentHeight - el.measureHeight) / 2
        }
    }

    fun draw(env: TypeEnvironment, canvas: Canvas) {
        for (element in mElements) {
            element.draw(env, canvas)
        }
    }

    fun restoreVisibleChange() {
        if (mVisibleChanged != null) {
            for (entry in mVisibleChanged!!.keys) {
                val visible = mVisibleChanged!![entry]
                if (visible != null) {
                    entry.visible = visible
                }
            }
            mVisibleChanged!!.clear()
        }
    }

    fun popAll(): List<Element> {
        val elements: List<Element> = ArrayList(mElements)
        mElements.clear()
        restoreVisibleChange()
        contentWidth = 0
        contentHeight = 0
        layoutWidth = 0
        return elements
    }

    fun clear() {
        mElements.clear()
        restoreVisibleChange()
        contentWidth = 0
        contentHeight = 0
        layoutWidth = 0
    }

    fun release() {
        x = 0
        y = 0
        widthLimit = 0
        contentWidth = 0
        contentHeight = 0
        layoutWidth = 0
        mElements.clear()
        restoreVisibleChange()
        sLinePool.release(this)
    }
}