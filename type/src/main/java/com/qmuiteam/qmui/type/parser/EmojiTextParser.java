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

import android.graphics.drawable.Drawable;

import com.qmuiteam.qmui.type.TypeModel;
import com.qmuiteam.qmui.type.element.CharOrPhraseElement;
import com.qmuiteam.qmui.type.element.DrawableElement;
import com.qmuiteam.qmui.type.element.Element;
import com.qmuiteam.qmui.type.element.EmojiElement;
import com.qmuiteam.qmui.type.element.NextParagraphElement;

import java.util.HashMap;

public class EmojiTextParser implements TextParser {

    private final EmojiResourceProvider mEmojiProvider;

    public EmojiTextParser(EmojiResourceProvider provider) {
        mEmojiProvider = provider;
    }

    @Override
    public TypeModel parse(CharSequence text) {
        int size = text.length();
        if (size == 0) {
            return null;
        }
        HashMap<Integer, Element> map = new HashMap<>(text.length());
        Element first = null, last = null, tmp = null;
        int index = 0;
        for (int i = 0; i < size; i++) {
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
            } else if (c == '[') {
                int j = i + 1;
                boolean find = false;
                int end = Math.min(i + 30, size);
                while (j < end) {
                    if (text.charAt(j) == ']') {
                        CharSequence sub = text.subSequence(i, j + 1);
                        Drawable emoji = mEmojiProvider.queryForDrawable(sub);
                        if (emoji != null) {
                            tmp = new EmojiElement(emoji, '\u0000', sub, index, i);
                            i = j;
                            find = true;
                            break;
                        }
                    }
                    j++;
                }
                if (!find) {
                    tmp = new CharOrPhraseElement(c, index, i);
                }
            } else {
                boolean handled = false;
                Drawable emoji = mEmojiProvider.queryForDrawable(c);
                if (emoji != null) {
                    handled = true;
                    tmp = new DrawableElement(emoji, c, null, index, i);
                }

                if (!handled) {
                    int unicode = Character.codePointAt(text, i);
                    int codeCount = Character.charCount(unicode);
                    emoji = mEmojiProvider.queryForDrawable(unicode);
                    if (emoji != null) {
                        handled = true;
                        tmp = new DrawableElement(emoji, c, text.subSequence(i, i + codeCount), index, i);
                        i += codeCount - 1;
                    }

                    int nextStart = i + codeCount;
                    if (!handled && nextStart < size) {
                        int nextUnicode = Character.codePointAt(text, nextStart);
                        emoji = mEmojiProvider.queryForDrawable(unicode, nextUnicode);
                        if (emoji != null) {
                            handled = true;
                            int nextCodeCount = Character.charCount(nextUnicode);
                            tmp = new DrawableElement(emoji, c, text.subSequence(i, nextStart + nextCodeCount), index, i);
                            i = nextStart + nextCodeCount - 1;
                        }
                    }
                }

                if (!handled) {
                    tmp = new CharOrPhraseElement(c, index, i);
                }
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
