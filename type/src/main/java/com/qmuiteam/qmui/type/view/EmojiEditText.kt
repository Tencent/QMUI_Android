package com.qmuiteam.qmui.type.view

import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.util.valueIterator
import com.qmuiteam.qmui.type.emoji.Emoji
import com.qmuiteam.qmui.type.emoji.EmojiModel
import com.qmuiteam.qmui.type.emoji.EmojiSpan
import com.qmuiteam.qmui.type.emoji.toEmojiModel
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
            if (field != value) {
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
                        if (model.removeEmoji(emoji)) {
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
                        if (shouldBreak) {
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
            if (isTextManualSetting) {
                return
            }

            val offset = count - before
            emojiModel?.map?.let { map ->
                map.valueIterator().asSequence().mapTo(correctingEmoji) { emoji ->
                    if (emoji.start >= start) {
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
                val insertModel =
                    textParser?.parse(s!!.subSequence(start, start + count))?.toEmojiModel(start){ emojiSize }
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
                s?.setSpan(
                    it.span,
                    it.start,
                    it.start + it.text.length,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
            }

            fixSelection()
            isPendingRemoving = false
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
            if (selectionStart < 0 || selectionEnd < 0) {
                setSelection(origin.length, origin.length)
            }
            if (selectionStart == selectionEnd) {
                origin.insert(selectionEnd, toInsert)
            } else {
                var fixStart = selectionStart
                emojiModel?.getEmoji(selectionStart)?.let {
                    val end = it.start + it.text.length
                    if (selectionStart > it.start && selectionStart == end - 1) {
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
        val model = textParser?.parse(text)?.toEmojiModel(0) { emojiSize }
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
            spannable.setSpan(
                it.span,
                it.start,
                it.start + it.text.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
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
                selectionStart < (start.start + start.text.length)
            ) {
                fixStart = start.start
            }
            val end = model.getEmoji(selectionEnd)
            if (end != null && selectionEnd > end.start && selectionEnd < (end.start + end.text.length)) {
                fixEnd = end.start + end.text.length
            }
            setSelection(fixStart, fixEnd)
        }

    }
}