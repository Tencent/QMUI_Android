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
import com.qmuiteam.qmui.type.element.Element
import com.qmuiteam.qmui.type.element.NextParagraphElement
import com.qmuiteam.qmui.type.element.TextElement
import com.qmuiteam.qmui.type.parser.ParserHelper.handleWordPart
import java.util.*

class PlainTextParser : TextParser {

    companion object {
        val instance by lazy {
            PlainTextParser()
        }
    }

    override fun parse(text: CharSequence?): TypeModel? {
        if (text == null || text.isEmpty()) {
            return null
        }
        val size = text.length
        val map = HashMap<Int, Element>(size)
        var first: Element? = null
        var last: Element? = null
        var tmp: Element
        var index = 0
        var i = 0
        while (i < size) {
            val c = text[i]
            if (c == '\n') {
                tmp = NextParagraphElement(text.subSequence(i, i+1), index, i)
            } else if (c == '\r') {
                if (i + 1 < text.length && text[i + 1] == '\n') {
                    tmp = NextParagraphElement(text.subSequence(i, i+2), index, i)
                    i++
                } else {
                    tmp = NextParagraphElement(text.subSequence(i, i+1), index, i)
                }
            } else {
                tmp = TextElement(text.subSequence(i, i+1), index, i)
            }
            handleWordPart(c, last, tmp)
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