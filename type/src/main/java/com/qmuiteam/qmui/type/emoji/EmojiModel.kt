package com.qmuiteam.qmui.type.emoji

import android.text.SpannableString
import android.text.Spanned
import android.util.SparseArray
import androidx.core.util.putAll
import com.qmuiteam.qmui.type.TypeModel
import com.qmuiteam.qmui.type.element.Element
import com.qmuiteam.qmui.type.element.EmojiElement

class Emoji(val span: EmojiSpan, val text: CharSequence, var start: Int) {
    var prev: Emoji? = null
    var next: Emoji? = null
}

class EmojiModel(
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

fun TypeModel.toEmojiModel(offset: Int, emojiSizeGetter: () -> Int): EmojiModel? {
    var node: Element? = firstElement()
    val map = SparseArray<Emoji>()
    var begin: Emoji? = null
    var end: Emoji? = null
    while (node != null) {
        if (node is EmojiElement) {
            val next = Emoji(EmojiSpan(node.drawable.apply {
                setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            }, emojiSizeGetter), node.text, offset + node.start)
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

fun TypeModel.toSpannableString(emojiSizeGetter: () -> Int): SpannableString{
    val ss = (origin as? SpannableString)?.apply {
        getSpans(0, length, EmojiSpan::class.java)?.forEach {  span ->
            removeSpan(span)
        }
    } ?:  SpannableString(origin)
    var cur: Element? = firstElement()
    while (cur != null){
        if(cur is EmojiElement){
            ss.setSpan(
                EmojiSpan(cur.drawable, emojiSizeGetter),
                cur.start,
                cur.start + cur.text.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
        cur = cur.next
    }
    return ss
}