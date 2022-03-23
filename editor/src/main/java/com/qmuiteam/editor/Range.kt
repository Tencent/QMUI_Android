package com.qmuiteam.editor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange

internal class MutableRange<T>(
    var item: T,
    var start: Int,
    var end: Int,
    var tag: String
) {

    fun modifyByInsert(insertPos: Int, appendIfAtEnd: Boolean) {
        if (start == end) {
            if (insertPos < start) {
                start++
                end++
            } else if (insertPos == start) {
                end++
            }
        } else {
            if (insertPos < start) {
                start++
                end++
            } else if (insertPos < end || (appendIfAtEnd && insertPos == end)) {
                end++
            }
        }
    }

    fun modifyByDelete(deletePos: Int): Boolean {
        if (start == end) {
            if (deletePos < start - 1) {
                start--
                end--
            } else if (deletePos == start - 1) {
                start--
                end--
                return true
            }
        }
        if (deletePos < start) {
            start--
            end--
        } else if (deletePos < end) {
            end--
        }
        return false
    }

    fun isCursorContained(cursorPos: Int): Boolean{
        return if(start == end){
            start == cursorPos
        } else {
            cursorPos in (start + 1) until end
        }
    }
}

fun <T> AnnotatedString.Range<T>.isCursorContained(cursorPos: Int): Boolean{
    return if(start == end){
        start == cursorPos
    } else {
        cursorPos in (start + 1)..end
    }
}