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

import com.qmuiteam.qmui.type.TypeModel
import com.qmuiteam.qmui.type.element.*
import java.util.*

class EmojiTextParser(private val emojiProvider: EmojiResourceProvider) : TextParser {

    override fun parse(text: CharSequence?): TypeModel? {
        if(text == null || text.isEmpty()){
            return null
        }

        val size = text.length
        val map = HashMap<Int, Element>(size)
        var first: Element? = null
        var last: Element? = null
        var tmp: Element? = null
        var index = 0
        var i = 0
        while (i < size) {
            val c = text[i]
            if (c == '\n') {
                tmp = NextParagraphElement(text.subSequence(i, i + 1), index, i)
            } else if (c == '\r') {
                if (i + 1 < text.length && text[i + 1] == '\n') {
                    tmp = NextParagraphElement(text.subSequence(i, i + 2), index, i)
                    i++
                } else {
                    tmp = NextParagraphElement(text.subSequence(i, i + 1), index, i)
                }
            } else if (c == '[') {
                var j = i + 1
                var find = false
                val end = Math.min(i + 30, size)
                while (j < end) {
                    if (text[j] == ']') {
                        val sub = text.subSequence(i, j + 1)
                        val emoji = emojiProvider.queryForDrawable(sub)
                        if (emoji != null) {
                            tmp = EmojiElement(emoji, text.subSequence(i, j + 1), index, i)
                            i = j
                            find = true
                            break
                        }
                    }
                    j++
                }
                if (!find) {
                    tmp = TextElement(text.subSequence(i, i + 1), index, i)
                }
            } else {
                var handled = false
                var emoji = emojiProvider.queryForDrawable(c)
                if (emoji != null) {
                    handled = true
                    tmp = DrawableElement(emoji, text.subSequence(i, i + 1), index, i)
                }
                if (!handled) {
                    val unicode = Character.codePointAt(text, i)
                    val codeCount = Character.charCount(unicode)
                    emoji = emojiProvider.queryForDrawable(unicode)
                    if (emoji != null) {
                        handled = true
                        tmp = DrawableElement(emoji, text.subSequence(i, i + codeCount), index, i)
                        i += codeCount - 1
                    }
                    val nextStart = i + codeCount
                    if (!handled && nextStart < size) {
                        val nextUnicode = Character.codePointAt(text, nextStart)
                        emoji = emojiProvider.queryForDrawable(unicode, nextUnicode)
                        if (emoji != null) {
                            handled = true
                            val nextCodeCount = Character.charCount(nextUnicode)
                            tmp = DrawableElement(emoji, text.subSequence(i, nextStart + nextCodeCount), index, i)
                            i = nextStart + nextCodeCount - 1
                        }
                    }
                }
                if (!handled) {
                    tmp = TextElement(text.subSequence(i, i + 1), index, i)
                }
            }
            ParserHelper.handleWordPart(c, last, tmp!!)
            index++
            if (first == null) {
                first = tmp
                last = tmp
            } else {
                last!!.next = tmp
                last = tmp
            }
            map[tmp.index] = tmp
            i++
        }
        return TypeModel(text, map, first!!, last!!)
    }
}