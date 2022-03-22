package com.qmuiteam.editor

import androidx.compose.ui.text.input.TextFieldValue
import java.util.*

enum class WordEditAction {
    insert, delete, repace
}


data class WordEditPoint(
    val action: WordEditAction,
    val oldIndex: Int,
    val newIndex: Int
)

class WordEditResult(
    val dis: Int,
    val list: List<WordEditPoint>
)

private class WordEditRecordNode(val point: WordEditPoint) {

    var prev: WordEditRecordNode? = null
}

private class WordEditRecord(
    var dis: Int
) {
    var node: WordEditRecordNode? = null
}

fun wordEdit(oldTextFieldValue: TextFieldValue, newTextFieldValue: TextFieldValue): WordEditResult {
    val oldText = oldTextFieldValue.text
    val newText = newTextFieldValue.text
    if(oldText.length <= 20 || newText.length <= 20){
        return wordEdit(oldText, newText)
    }

    var prefixCheckLength = 10
    var prefix = (oldTextFieldValue.selection.start - prefixCheckLength)
        .coerceAtMost(newTextFieldValue.selection.start - prefixCheckLength)
        .coerceAtLeast(0)
    while (prefix > 0){
        if(oldText.substring(0, prefix) == newText.substring(0, prefix)){
            break
        }
        prefixCheckLength *= 2
        prefix = (prefix - prefixCheckLength).coerceAtLeast(0)
    }

    var suffixCheckLength = 10
    var suffix = (oldText.length - oldTextFieldValue.selection.end - suffixCheckLength)
        .coerceAtMost(newText.length - newTextFieldValue.selection.end - suffixCheckLength)
        .coerceAtLeast(0)
    while (suffix > 0){
        if(oldText.substring(oldText.length - suffix) == newText.substring(newText.length - suffix)){
            break
        }
        suffixCheckLength *= 2
        suffix = (suffix - suffixCheckLength).coerceAtLeast(0)
    }
    if(prefix == 0 && suffix == 0){
        return wordEdit(oldText, newText)
    }
    return wordEdit(
        oldText.substring(prefix, oldText.length - suffix),
        newText.substring(prefix, newText.length - suffix)
    )
}

fun wordEdit(oldText: String, newText: String, shift: Int = 0): WordEditResult {
    val array = arrayOfNulls<WordEditRecord>(oldText.length + 1)
    val next = arrayOfNulls<WordEditRecord>(oldText.length + 1)
    for (j in array.indices) {
        array[j] = WordEditRecord(j).apply {
            if (j > 0) {
                node = WordEditRecordNode(
                    WordEditPoint(
                        WordEditAction.delete,
                        shift + j - 1,
                        -1
                    )
                ).apply {
                    prev = array[j - 1]!!.node
                }
            }
        }
    }
    for (i in newText.indices) {
        for (j in array.indices) {
            val columnLast = array[j]!!
            if (j == 0) {
                next[j] = WordEditRecord(columnLast.dis + 1).apply {
                    node = WordEditRecordNode(
                        WordEditPoint(WordEditAction.insert, shift + j - 1, shift + i)
                    ).apply {
                        prev = columnLast.node
                    }
                }
            } else {
                val path1 = WordEditRecord(columnLast.dis + 1).apply {
                    node = WordEditRecordNode(
                        WordEditPoint(WordEditAction.insert, shift + j - 1, shift + i)
                    ).apply {
                        prev = columnLast.node
                    }
                }

                val rowLast = next[j - 1]!!
                val path2 = WordEditRecord(rowLast.dis + 1).apply {
                    node = WordEditRecordNode(
                        WordEditPoint(WordEditAction.delete, shift + j - 1, -1)
                    ).apply {
                        prev = rowLast.node
                    }
                }

                val diagonalLast = array[j - 1]!!
                val path3 = if (newText[i] == oldText[j - 1]) {
                    diagonalLast
                } else {
                    WordEditRecord(diagonalLast.dis + 1).apply {
                        node = WordEditRecordNode(
                            WordEditPoint(
                                WordEditAction.repace,
                                j - 1,
                                i
                            )
                        ).apply {
                            prev = diagonalLast.node
                        }
                    }
                }

                var minPath = path1
                if (path2.dis < minPath.dis) {
                    minPath = path2
                }

                if (path3.dis < minPath.dis) {
                    minPath = path3
                }
                next[j] = minPath
            }
        }
        for (j in array.indices) {
            array[j] = next[j]
        }
    }
    val ret = array[array.size - 1]!!
    val list = LinkedList<WordEditPoint>()
    var node = ret.node
    while (node != null) {
        list.addFirst(node!!.point)
        node = node?.prev
    }
    return WordEditResult(ret.dis, list)
}