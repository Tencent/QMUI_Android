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
import android.graphics.Color
import android.graphics.Typeface
import android.text.TextUtils.TruncateAt
import com.qmuiteam.qmui.type.element.*
import java.util.*

class LineLayout(private val mTypeEnvironment: TypeEnvironment) {
    private var mMaxLines = Int.MAX_VALUE
    private var mEllipsize: TruncateAt? = null
    private var mCalculateWholeLines = true
    private val mLines: MutableList<Line> = ArrayList()
    private var mDropLastIfSpace = true
    private var mMoreText: String? = null
    private var mMoreTextColor = 0
    private var mMoreTextTypeface: Typeface? = null
    private var mMoreUnderlineColor = Color.TRANSPARENT
    private var mMoreBgColor = 0
    private var mMoreUnderlineHeight = 0
    private var mTotalLineCount = 0

    var typeModel: TypeModel? = null
        private set

    fun setMaxLines(maxLines: Int): LineLayout {
        mMaxLines = maxLines
        return this
    }

    fun setEllipsize(ellipsize: TruncateAt?): LineLayout {
        mEllipsize = ellipsize
        return this
    }

    fun setCalculateWholeLines(calculateWholeLines: Boolean): LineLayout {
        mCalculateWholeLines = calculateWholeLines
        return this
    }

    fun setDropLastIfSpace(dropLastIfSpace: Boolean): LineLayout {
        mDropLastIfSpace = dropLastIfSpace
        return this
    }

    fun setMoreText(text: String?, color: Int, typeface: Typeface?): LineLayout {
        mMoreText = text
        mMoreTextColor = color
        mMoreTextTypeface = typeface
        return this
    }

    fun setMoreBackgroundColor(color: Int): LineLayout {
        mMoreBgColor = color
        return this
    }

    fun setUnderline(height: Int, color: Int): LineLayout {
        mMoreUnderlineHeight = height
        mMoreUnderlineColor = color
        return this
    }

    fun setTypeModel(typeModel: TypeModel?): LineLayout {
        this.typeModel = typeModel
        return this
    }

    fun measureAndLayout() {
        mTypeEnvironment.clear()
        release()
        if (typeModel == null) {
            return
        }
        var element: Element? = typeModel!!.firstElement() ?: return
        var line = Line.acquire()
        var y = 0
        line.init(0, y, mTypeEnvironment.widthLimit)
        while (element != null) {
            element.measure(mTypeEnvironment)
            if (element is NextParagraphElement) {
                line.add(element)
                line.layout(mTypeEnvironment, mDropLastIfSpace, false)
                mLines.add(line)
                if (canInterrupt()) {
                    return
                }
                y += line.contentHeight + mTypeEnvironment.paragraphSpace
                line = createNewLine(y)
            } else if (line.contentWidth + element.measureWidth > mTypeEnvironment.widthLimit) {
                if (mLines.size == 0 && line.size == 0) {
                    // the width is too small.
                    line.release()
                    return
                }
                val back = line.handleWordBreak(mTypeEnvironment)
                line.layout(mTypeEnvironment, mDropLastIfSpace, false)
                mLines.add(line)
                if (canInterrupt()) {
                    handleEllipse(true)
                    return
                }
                y += line.contentHeight + mTypeEnvironment.lineSpace
                line = createNewLine(y)
                if (back != null && !back.isEmpty()) {
                    for (el in back) {
                        line.add(el)
                    }
                }
                line.add(element)
            } else {
                line.add(element)
            }
            element = element.next
        }
        if (line.size > 0) {
            line.layout(mTypeEnvironment, mDropLastIfSpace, true)
            mLines.add(line)
        } else {
            line.release()
        }
        mTotalLineCount = mLines.size
        handleEllipse(false)
    }

    private fun createNewLine(y: Int): Line {
        val line = Line.acquire()
        line.init(0, y, mTypeEnvironment.widthLimit)
        return line
    }

    private fun handleEllipse(fromInterrupt: Boolean) {
        if (mLines.isEmpty() || mLines.size < mMaxLines || mLines.size == mMaxLines && !fromInterrupt) {
            return
        }
        if (mEllipsize == TruncateAt.END) {
            handleEllipseEnd()
        } else if (mEllipsize == TruncateAt.START) {
            handleEllipseStart()
        } else if (mEllipsize == TruncateAt.MIDDLE) {
            handleEllipseMiddle()
        }
    }

    private fun handleEllipseEnd() {
        for (i in mLines.size - 1 downTo mMaxLines) {
            mLines.remove(mLines[i])
        }
        val lastLine = mLines[mLines.size - 1]
        var limitWidth = lastLine.widthLimit
        val ellipseElement: Element = TextElement("...", -1, -1)
        ellipseElement.addSingleEnvironmentUpdater(null, object : EnvironmentUpdater {
            override fun update(env: TypeEnvironment) {
                env.clear()
            }
        })
        ellipseElement.measure(mTypeEnvironment)
        limitWidth = (limitWidth - ellipseElement.measureWidth).toInt()
        var moreElement: Element? = null
        if (mMoreText != null && !mMoreText!!.isEmpty()) {
            moreElement = TextElement(mMoreText!!, -1, -1)
            val changeTypes: MutableList<Int> = ArrayList()
            changeTypes.add(TypeEnvironment.TYPE_TEXT_COLOR)
            changeTypes.add(TypeEnvironment.TYPE_BG_COLOR)
            changeTypes.add(TypeEnvironment.TYPE_TYPEFACE)
            changeTypes.add(TypeEnvironment.TYPE_BORDER_BOTTOM_COLOR)
            changeTypes.add(TypeEnvironment.TYPE_BORDER_BOTTOM_WIDTH)
            moreElement.addSingleEnvironmentUpdater(changeTypes, object : EnvironmentUpdater {
                override fun update(env: TypeEnvironment) {
                    if (mMoreTextColor != 0) {
                        env.textColor = mMoreTextColor
                    }
                    if (mMoreBgColor != 0) {
                        env.backgroundColor = mMoreBgColor
                    }
                    if (mMoreTextTypeface != null) {
                        env.typeface = mMoreTextTypeface
                    }
                    if (mMoreUnderlineHeight > 0) {
                        env.setBorderBottom(mMoreUnderlineHeight, mMoreUnderlineColor)
                    }
                }
            })
            moreElement.measure(mTypeEnvironment)
            limitWidth = (limitWidth - moreElement.measureWidth).toInt()
        }
        val contentWidth = lastLine.contentWidth
        if (contentWidth < limitWidth) {
            lastLine.restoreVisibleChange()
        } else {
            val elements = lastLine.popAll()
            for (el in elements) {
                if (el.measureWidth <= limitWidth) {
                    lastLine.add(el)
                    limitWidth -= (limitWidth - el.measureWidth).toInt()
                } else {
                    break
                }
            }
        }
        lastLine.add(ellipseElement)
        if (moreElement != null) {
            lastLine.add(moreElement)
        }
        lastLine.layout(mTypeEnvironment, mDropLastIfSpace, true)
    }

    private fun handleEllipseStart() {
        mTypeEnvironment.clear()
        for (i in mLines.size - 1 downTo mMaxLines) {
            mLines.remove(mLines[i])
        }
        val ellipseElement: Element = TextElement("...", -1, -1)
        ellipseElement.addSingleEnvironmentUpdater(null, object : EnvironmentUpdater {
            override fun update(env: TypeEnvironment) {
                env.clear()
            }
        })
        ellipseElement.measure(mTypeEnvironment)
        val elements: Queue<Element> = LinkedList()
        elements.add(ellipseElement)
        for (i in mLines.indices) {
            val line = mLines[i]
            val limitWidth = line.widthLimit
            elements.addAll(line.popAll())
            while (!elements.isEmpty()) {
                val el = elements.peek()
                if (el != null) {
                    if (el is NextParagraphElement) {
                        elements.poll()
                        line.add(el)
                        el.move(mTypeEnvironment)
                        break
                    }
                    if (el is BreakWordLineElement) {
                        elements.poll()
                        continue
                    }
                    if (line.contentWidth + el.measureWidth <= limitWidth) {
                        elements.poll()
                        line.add(el)
                        el.move(mTypeEnvironment)
                    } else {
                        break
                    }
                } else {
                    elements.poll()
                }
            }
            line.handleWordBreak(mTypeEnvironment)
            line.layout(mTypeEnvironment, mDropLastIfSpace, false)
            if (elements.isEmpty()) {
                return
            }
        }
    }

    private fun handleEllipseMiddle() {
        mTypeEnvironment.clear()
        val lines: List<Line> = ArrayList(mLines)
        mLines.clear()
        val ellipseElement: Element = TextElement("...", -1, -1)
        ellipseElement.measure(mTypeEnvironment)
        val ellipseLine = if (mMaxLines % 2 == 0) mMaxLines / 2 else (mMaxLines + 1) / 2
        for (i in 0 until ellipseLine) {
            mLines.add(lines[i])
        }
        val handleLine = lines[ellipseLine - 1]
        val limitWidth = handleLine.widthLimit
        val unHandled: Deque<Element> = LinkedList(handleLine.popAll())
        while (!unHandled.isEmpty()) {
            val el = unHandled.peek()
            if (el != null) {
                if (handleLine.contentWidth + el.measureWidth <= limitWidth / 2f - ellipseElement.measureWidth / 2) {
                    unHandled.poll()
                    handleLine.add(el)
                    el.move(mTypeEnvironment)
                } else {
                    break
                }
            } else {
                unHandled.poll()
            }
        }
        ellipseElement.measure(mTypeEnvironment)
        handleLine.add(ellipseElement)
        val nextFullShowLine = lines.size - mMaxLines + ellipseLine
        var startLine = lines.size - 1
        // find the latest paragraph end line.
        for (i in lines.size - 2 downTo nextFullShowLine + 1) {
            if (lines[i].isMiddleParagraphEndLine) {
                startLine = i
            }
        }
        for (i in ellipseLine..startLine) {
            unHandled.addAll(lines[i].popAll())
        }
        for (i in startLine downTo nextFullShowLine) {
            val line = lines[i]
            while (!unHandled.isEmpty()) {
                val element = unHandled.peekLast()
                if (element != null) {
                    if (element is NextParagraphElement) {
                        unHandled.pollLast()
                        continue
                    }
                    if (element is BreakWordLineElement) {
                        unHandled.pollLast()
                        continue
                    }
                    if (line.contentWidth + element.measureWidth <= line.widthLimit) {
                        unHandled.pollLast()
                        line.addFirst(element)
                    } else {
                        break
                    }
                } else {
                    unHandled.pollLast()
                }
            }
        }
        val toAdd = LinkedList<Element>()
        var toAddWidth = 0
        while (!unHandled.isEmpty()) {
            val element = unHandled.peekLast()
            if (element != null) {
                if (element is NextParagraphElement) {
                    unHandled.pollLast()
                    continue
                }
                if (element is BreakWordLineElement) {
                    unHandled.pollLast()
                    continue
                }
                if (handleLine.contentWidth + toAddWidth + element.measureWidth <= handleLine.widthLimit) {
                    unHandled.pollLast()
                    toAdd.add(0, element)
                    toAddWidth = (toAddWidth + element.measureWidth).toInt()
                } else {
                    break
                }
            } else {
                unHandled.pollLast()
            }
        }
        val firstUnHandle = unHandled.peekFirst()
        val lastUnHandle = unHandled.peekLast()
        var effect = typeModel!!.firstEffect
        if (firstUnHandle != null && lastUnHandle != null) {
            val ellipseEffect: MutableList<Element> = ArrayList()
            while (effect != null && effect.index <= lastUnHandle.index) {
                if (effect.index >= firstUnHandle.index) {
                    ellipseEffect.add(effect)
                }
                effect = effect.next
            }
            if (ellipseEffect.size > 0) {
                val ignoreEffectElement = IgnoreEffectElement(ellipseEffect)
                ignoreEffectElement.move(mTypeEnvironment)
                handleLine.add(ignoreEffectElement)
            }
        }
        for (el in toAdd) {
            el.move(mTypeEnvironment)
            handleLine.add(el)
        }
        handleLine.handleWordBreak(mTypeEnvironment)
        handleLine.layout(mTypeEnvironment, mDropLastIfSpace, ellipseLine == lines.size)
        var lastEnd = handleLine.y + handleLine.contentHeight
        for (i in nextFullShowLine until lines.size) {
            val line = lines[i]
            val prev = lines[i - 1]
            if (prev.isMiddleParagraphEndLine) {
                line.y = lastEnd + mTypeEnvironment.paragraphSpace
            } else {
                line.y = lastEnd + mTypeEnvironment.lineSpace
            }
            lastEnd = line.y + line.contentHeight
            line.move(mTypeEnvironment)
            line.handleWordBreak(mTypeEnvironment)
            line.layout(mTypeEnvironment, mDropLastIfSpace, i == lines.size - 1)
            mLines.add(line)
        }
    }

    val maxLayoutWidth: Int
        get() {
            var maxWidth = 0
            for (line in mLines) {
                maxWidth = Math.max(maxWidth, line.layoutWidth)
            }
            return maxWidth
        }
    val contentHeight: Int
        get() {
            if (mLines.isEmpty()) {
                return 0
            }
            val last = mLines[mLines.size - 1]
            return last.y + last.contentHeight
        }

    fun draw(canvas: Canvas) {
        mTypeEnvironment.clear()
        for (line in mLines) {
            line.draw(mTypeEnvironment, canvas)
        }
    }

    private fun canInterrupt(): Boolean {
        return mLines.size == mMaxLines && !mCalculateWholeLines &&
                (mEllipsize == null || mEllipsize == TruncateAt.END)
    }

    fun release() {
        for (line in mLines) {
            line.release()
        }
        mLines.clear()
    }
}