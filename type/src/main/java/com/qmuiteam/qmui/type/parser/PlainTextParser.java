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

package com.qmuiteam.qmui.type.parser;

import com.qmuiteam.qmui.type.TypeModel;
import com.qmuiteam.qmui.type.element.CharOrPhraseElement;
import com.qmuiteam.qmui.type.element.Element;
import com.qmuiteam.qmui.type.element.NextParagraphElement;

import java.util.HashMap;

public class PlainTextParser implements TextParser {
    @Override
    public TypeModel parse(CharSequence text) {
        if (text.length() == 0) {
            return null;
        }
        HashMap<Integer, Element> map = new HashMap<>(text.length());
        Element first = null, last = null, tmp;
        int index = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                tmp = new NextParagraphElement(c, null, index, i);
            } else if (c == '\r') {
                if (i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    tmp = new NextParagraphElement('\u0000', "\r\n", index, i);
                    i++;
                } else {
                    tmp = new NextParagraphElement(c, null, index, i);
                }
            } else {
                tmp = new CharOrPhraseElement(c, index, i);
            }

            ParserHelper.handleWordPart(c, last, tmp);

            index++;
            if (first == null) {
                first = tmp;
                last = tmp;
            } else {
                last.setNext(tmp);
                last = tmp;
            }
            map.put(tmp.getIndex(), tmp);
        }
        return new TypeModel(text, map, first, last, null);
    }
}
