package com.qmuiteam.qmui.type.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.util.putAll
import androidx.core.util.valueIterator
import com.qmuiteam.qmui.type.TypeModel
import com.qmuiteam.qmui.type.element.Element
import com.qmuiteam.qmui.type.element.EmojiElement
import com.qmuiteam.qmui.type.parser.EmojiTextParser

open class EmojiEditText(
    context: Context,
    attributeSet: AttributeSet? = null
) : AppCompatEditText(context, attributeSet) {

    companion object {
        const val EmojiOriginSize = -1
    }


    private var emojiModel: EmojiModel? = null
    private var isTextFirstSet: Boolean = false
    private var isTextManualSetting: Boolean = false

    var emojiSize: Int = EmojiOriginSize
        set(value) {
            if(field != value){
                field = value
                invalidate()
            }
        }

    var textParser: EmojiTextParser? = null
        set(value) {
            if (field != value) {
                field = value
                if (isTextFirstSet) {
                    setText(text, BufferType.EDITABLE)
                }
            }
        }

    private val textWatcher = object : TextWatcher {

        private val pendingRemoveRange = mutableListOf<Pair<Int, Int>>()
        private var isPendingRemoving = false
        private val correctingEmoji = ArrayList<Emoji>()

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            if (isPendingRemoving || isTextManualSetting) {
                return
            }
            val model = emojiModel
            if (s != null && model != null && count > 0) {
                val end = start + count
                var current = start
                while (current < end) {
                    val emoji = model.getEmoji(current)
                    if (emoji != null) {
                        var shouldBreak = false
                        if(model.removeEmoji(emoji)){
                            emojiModel = null
                            shouldBreak = true
                        }
                        if (emoji.start < start) {
                            pendingRemoveRange.add(emoji.start to start)
                        }
                        val emojiEnd = emoji.start + emoji.text.length
                        if (emojiEnd > end) {
                            val offset = count - after
                            // remove first.
                            pendingRemoveRange.add(0, (end - offset) to (emojiEnd - offset))
                        }
                        if(shouldBreak){
                            break;
                        }
                        current = emoji.start + emoji.text.length
                    } else {
                        current++
                    }
                }
            }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if(isTextManualSetting){
                return
            }

            val offset = count - before
            emojiModel?.map?.let { map ->
                map.valueIterator().asSequence().mapTo(correctingEmoji) { emoji ->
                    if(emoji.start >= start){
                        emoji.start += offset
                    }
                    emoji
                }
                map.clear()
                correctingEmoji.forEach {
                    map.put(it.start, it)
                }
                correctingEmoji.clear()
            }

            if (isPendingRemoving) {
                return
            }

            if (count > 0) {
                val insertModel = textParser?.parse(s!!.subSequence(start, start + count))?.toEmojiModel(start)
                if (insertModel != null) {
                    val model = emojiModel
                    if (model != null) {
                        model.merge(insertModel)
                    } else {
                        emojiModel = insertModel
                    }
                }
            }

        }

        override fun afterTextChanged(s: Editable?) {
            if (isPendingRemoving || isTextManualSetting) {
                return
            }
            isPendingRemoving = true
            pendingRemoveRange.forEach {
                s?.delete(it.first, it.second)
            }
            pendingRemoveRange.clear()
            s?.getSpans(0, s.length, EmojiSpan::class.java)?.forEach {
                s.removeSpan(it)
            }
            emojiModel?.map?.valueIterator()?.forEach {
                s?.setSpan(it.span, it.start, it.start + it.text.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            }

            fixSelection()
            isPendingRemoving = false
            Log.i("cginetest",
                "afterTextChanged end: $s, $isPendingRemoving, $isPendingRemoving")
        }

    }

    init {
        super.addTextChangedListener(textWatcher)
    }

    fun replaceSelection(toInsert: CharSequence) {
        val origin = text
        if (origin == null) {
            setText(toInsert)
        } else {
            if(selectionStart < 0 || selectionEnd < 0){
                setSelection(origin.length, origin.length)
            }
            if(selectionStart == selectionEnd){
                origin.insert(selectionEnd, toInsert)
            }else{
                var fixStart = selectionStart
                emojiModel?.getEmoji(selectionStart)?.let {
                    val end = it.start + it.text.length
                    if(selectionStart > it.start && selectionStart == end - 1){
                        fixStart = end
                    }
                }
                origin.replace(fixStart, selectionEnd, toInsert)
            }
        }
    }

    fun delete() {
        val origin = text ?: return
        if (selectionStart != selectionEnd) {
            origin.replace(selectionStart, selectionEnd, "")
        } else if (selectionStart > 0) {
            origin.delete(selectionStart - 1, selectionEnd)
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        isTextFirstSet = true
        val model = textParser?.parse(text)?.toEmojiModel(0)
        emojiModel = model
        val spannable = if (text is Spannable) {
            val spans = text.getSpans(0, text.length, EmojiSpan::class.java)
            for (span in spans) {
                text.removeSpan(span)
            }
            text
        } else {
            SpannableString(text ?: "")
        }
        model?.map?.valueIterator()?.forEach {
            spannable.setSpan(it.span, it.start, it.start + it.text.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        isTextManualSetting = true
        super.setText(spannable, type)
        fixSelection()
        isTextManualSetting = false
    }

    private fun fixSelection() {
        val model = emojiModel ?: return
        if (selectionStart == selectionEnd) {
            val emoji = model.getEmoji(selectionStart) ?: return
            if (selectionStart > emoji.start && selectionStart < (emoji.start + emoji.text.length)) {
                setSelection(emoji.start + emoji.text.length)
            }
        } else {
            var fixStart = selectionStart
            var fixEnd = selectionEnd
            val start = model.getEmoji(selectionStart)
            if (start != null && selectionStart > start.start &&
                selectionStart < (start.start + start.text.length)) {
                fixStart = start.start
            }
            val end = model.getEmoji(selectionEnd)
            if (end != null && selectionEnd > end.start && selectionEnd < (end.start + end.text.length)) {
                fixEnd = end.start + end.text.length
            }
            setSelection(fixStart, fixEnd)
        }

    }

    inner class Emoji(val span: EmojiSpan, val text: CharSequence, var start: Int) {
        var prev: Emoji? = null
        var next: Emoji? = null
    }

    inner class EmojiModel(
        val map: SparseArray<Emoji>,
        var begin: Emoji,
        var end: Emoji
    ) {
        fun merge(other: EmojiModel) {
            map.putAll(other.map)
            when {
                other.begin.start > end.start -> {
                    end.next = other.begin
                    other.begin.prev = end
                    end = other.end
                }
                other.end.start < begin.start -> {
                    other.end.next = begin
                    begin.prev = other.end
                    begin = other.begin
                }
                else -> {
                    var next: Emoji?
                    var otherNext: Emoji?
                    val newBegin: Emoji = if (begin.start < other.begin.start) {
                        next = begin.next
                        otherNext = other.begin
                        begin
                    } else {
                        otherNext = other.begin.next
                        next = begin
                        other.begin
                    }
                    var cur = newBegin
                    while (next != null || otherNext != null) {
                        when {
                            next == null -> {
                                cur.next = otherNext
                                otherNext!!.prev = cur
                                begin = newBegin
                                end = other.end
                                return
                            }
                            otherNext == null -> {
                                cur.next = next
                                next.prev = cur
                                begin = newBegin
                                return
                            }
                            next.start < otherNext.start -> {
                                cur.next = next
                                next.prev = cur
                                cur = next
                                next = next.next
                            }
                            else -> {
                                cur.next = otherNext
                                otherNext.prev = cur
                                cur = otherNext
                                otherNext = otherNext.next
                            }
                        }
                    }
                }
            }
        }

        fun removeEmoji(emoji: Emoji): Boolean{
            map.remove(emoji.start)
            if(emoji == begin){
                begin = emoji.next ?: return true
                begin.prev = null
                return false
            }

            if(emoji == end){
                end = emoji.prev ?: return true
                end.next = null
                return false
            }
            val prev = emoji.prev
            val next = emoji.next
            prev?.next = next
            next?.prev = prev
            return false
        }

        fun getEmoji(pos: Int): Emoji? {
            if (begin.start > pos) {
                return null
            }
            if (end.start + end.text.length <= pos) {
                return null
            }
            var lo = 0
            var hi: Int = map.size() - 1

            while (lo <= hi) {
                val mid = lo + hi ushr 1
                val midVal = map.valueAt(mid)
                when {
                    midVal.start + midVal.text.length <= pos -> {
                        lo = mid + 1
                    }
                    midVal.start > pos -> {
                        hi = mid - 1
                    }
                    else -> {
                        return midVal
                    }
                }
            }
            return null

        }
    }

    fun TypeModel.toEmojiModel(offset: Int): EmojiModel? {
        var node: Element? = firstElement()
        val map = SparseArray<Emoji>()
        var begin: Emoji? = null
        var end: Emoji? = null
        while (node != null) {
            if (node is EmojiElement) {
                val next = Emoji(EmojiSpan(node.drawable.apply {
                    setBounds(0, 0, intrinsicWidth, intrinsicHeight)
                }), node.text, offset + node.start)
                map.put(offset + node.start, next)
                if (begin == null) {
                    begin = next
                    end = next
                } else {
                    next.prev = end
                    end!!.next = next
                    end = next
                }
            }
            node = node.next
        }
        if (begin == null) {
            return null
        }
        return EmojiModel(map, begin, end!!)
    }

    inner class EmojiSpan(drawable: Drawable) : ImageSpan(drawable) {
        override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
            val rect = drawable.bounds
            rect.left = 0
            rect.top = 0
            rect.right = if(emojiSize == EmojiOriginSize) drawable.intrinsicWidth else emojiSize
            rect.bottom = if(emojiSize == EmojiOriginSize) drawable.intrinsicHeight else emojiSize
            drawable.bounds = rect
            if (fm != null) {
                val fontMetricsHeight = fm.descent - fm.ascent
                if(fontMetricsHeight < rect.bottom){
                    val ratio = rect.bottom.toFloat() / fontMetricsHeight.toFloat()
                    fm.ascent = (fm.ascent * ratio).toInt()
                    fm.descent = (fm.descent * ratio).toInt()
                    fm.top = fm.ascent
                    fm.bottom = fm.descent
                }
            }

            return rect.right
        }

        override fun draw(
            canvas: Canvas, text: CharSequence?, start: Int, end: Int,
            x: Float, top: Int, y: Int, bottom: Int, paint: Paint
        ) {
            canvas.save()
            val rect = drawable.bounds
            val fontMetricsInt = paint.fontMetricsInt
            val fontTop = y + fontMetricsInt.top
            val fontMetricsHeight = fontMetricsInt.bottom - fontMetricsInt.top
            val iconHeight = rect.height()
            val iconTop = fontTop + (fontMetricsHeight - iconHeight) / 2
            canvas.translate(x, iconTop.toFloat())
            drawable.draw(canvas)
            canvas.restore()
        }
    }
}