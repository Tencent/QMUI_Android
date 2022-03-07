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
package com.qmuiteam.qmui.type.parser

import com.qmuiteam.qmui.type.element.Element

object ParserHelper {

    fun isEnglishLetterOrNumber(c: Char): Boolean {
        return c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9'
    }

    fun handleWordPart(c: Char, prev: Element?, curr: Element, wordBreakChecker: (c: Char) -> Boolean) {
        if (isEnglishLetterOrNumber(c)) {
            if (prev == null || prev.wordPart == Element.WORD_PART_WHOLE || prev.wordPart == Element.WORD_PART_END) {
                curr.wordPart = Element.WORD_PART_START
            } else {
                curr.wordPart = Element.WORD_PART_MIDDLE
                if (wordBreakChecker(c)) {
                    curr.lineBreakType = Element.LINE_BREAK_WORD_BREAK_ALLOWED
                }
            }
        } else {
            if (prev != null && prev.wordPart == Element.WORD_PART_MIDDLE) {
                prev.wordPart = Element.WORD_PART_END
                prev.lineBreakType = Element.LINE_BREAK_TYPE_NORMAL
            }
            curr.wordPart = Element.WORD_PART_WHOLE
        }
    }

    fun handleUnionIfNeeded(text: CharSequence, i: Int): Int {
        val unicode = Character.codePointAt(text, i)
        var charCount = Character.charCount(unicode)
        var next = i + charCount
        while (next < text.length) {
            val nextUnicode = Character.codePointAt(text, next)
            val type = Character.getType(nextUnicode)
            if (type == Character.NON_SPACING_MARK.toInt()) {
                val nextCharCount = Character.charCount(nextUnicode)
                charCount += nextCharCount
                next += nextCharCount
            } else {
                break
            }
        }
        return charCount
    }
}