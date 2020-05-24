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

package com.qmuiteam.qmui.type;

import android.graphics.Canvas;

import androidx.core.util.Pools;

import com.qmuiteam.qmui.type.element.BreakWordLineElement;
import com.qmuiteam.qmui.type.element.CharElement;
import com.qmuiteam.qmui.type.element.Element;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Line {
    private static Pools.Pool<Line> sLinePool = new Pools.SimplePool<>(16);

    public static Line acquire() {
        Line line = sLinePool.acquire();
        if (line == null) {
            line = new Line();
        }
        return line;
    }

    private Line() {

    }

    private int mX;
    private int mY;
    private int mWidthLimit;
    private int mContentWidth;
    private int mContentHeight;
    private int mLayoutWidth;
    private List<Element> mElements = new ArrayList<>();

    public void init(int x, int y, int widthLimit) {
        mX = x;
        mY = y;
        mWidthLimit = widthLimit;
    }

    public void add(Element element) {
        element.setVisible(Element.VISIBLE);
        mElements.add(element);
        mContentWidth += element.getMeasureWidth();
        mContentHeight = (int) Math.max(mContentHeight, element.getMeasureHeight());
    }

    public int getSize() {
        return mElements.size();
    }

    public int getContentWidth() {
        return mContentWidth;
    }

    public int getLayoutWidth() {
        return mLayoutWidth;
    }

    public int getContentHeight() {
        return mContentHeight;
    }

    public int getX() {
        return mX;
    }

    public int getY() {
        return mY;
    }

    public List<Element> handleWordBreak(TypeEnvironment environment) {
        if (mElements.size() == 0) {
            return null;
        }
        int lastIndex = mElements.size() - 1;
        Element last = mElements.get(lastIndex);
        if (last.getWordPart() == Element.WORD_PART_WHOLE || last.hasEnvironmentUpdater()) {
            return null;
        }
        List<Element> back = new LinkedList<>();
        back.add(last);
        mElements.remove(lastIndex);
        lastIndex--;
        int min = Math.max(0, lastIndex - 20); // try 20 letter.
        boolean find = false;
        while (lastIndex > min) {
            Element el = mElements.get(lastIndex);

            if (el.getWordPart() == Element.WORD_PART_WHOLE) {
                find = true;
                break;
            } else if (el.isCanBreakWord()) {
                BreakWordLineElement b = new BreakWordLineElement();
                b.measure(environment);
                add(b);
                find = true;
                break;
            } else if (el.hasEnvironmentUpdater()) {
                // give up
                mElements.addAll(back);
                return null;
            } else {
                back.add(0, el);
                mElements.remove(lastIndex);
                lastIndex--;
            }
        }

        if (!find) {
            // give up
            mElements.addAll(back);
            return null;
        }

        if (back.isEmpty()) {
            return null;
        }

        for (Element el : back) {
            mContentWidth -= el.getMeasureWidth();
        }
        return back;
    }

    private void hideLastIfSpaceIfNeeded(boolean dropLastIfSpace){
        Element last = mElements.get(mElements.size() - 1);
        if (dropLastIfSpace && last instanceof CharElement && last.getChar() == ' ') {
            last.setVisible(Element.GONE);
            mContentWidth -= last.getMeasureWidth();
        }
    }

    private int calculateGapCount(){
        int ret = 0;
        for(int i = 1; i < mElements.size(); i++){
            Element el = mElements.get(i);
            if(el.getVisible() != Element.GONE &&
                    (el.getWordPart() == Element.WORD_PART_WHOLE ||
                    el.getWordPart() == Element.WORD_PART_START)){
                ret ++;
            }
        }
        return ret;
    }

    public void layout(TypeEnvironment env, boolean dropLastIfSpace, boolean isParagraphLast) {
        if (mElements.isEmpty()) {
            return;
        }
        hideLastIfSpaceIfNeeded(dropLastIfSpace);
        mLayoutWidth = mContentWidth;
        TypeEnvironment.Alignment alignment = env.getAlignment();
        float start = mX;
        float addSpace = 0;
        if (alignment == TypeEnvironment.Alignment.RIGHT) {
            start = mX + mWidthLimit - mContentWidth;
        } else if (alignment == TypeEnvironment.Alignment.CENTER) {
            start = mX + (mWidthLimit - mContentWidth) / 2f;
        } else if (alignment == TypeEnvironment.Alignment.JUSTIFY) {
            float remain = mWidthLimit - mContentWidth;
            if (!isParagraphLast || remain < env.getLastLineJustifyMaxWidth()) {
                int gapCount = calculateGapCount();
                if(gapCount > 0){
                    addSpace = remain / gapCount;
                    mLayoutWidth = mWidthLimit;
                }
            }
        }
        float x = start;
        for (int i = 0; i < mElements.size(); i++) {
            Element el = mElements.get(i);
            if (i > 0 && (el.getWordPart() == Element.WORD_PART_WHOLE
                    || el.getWordPart() == Element.WORD_PART_START)) {
                x += addSpace;
            }
            el.setX((int) x);
            x += el.getMeasureWidth();
            el.setY(mY + (mContentHeight - el.getMeasureHeight()) / 2f);
        }
    }

    public void draw(TypeEnvironment env, Canvas canvas) {
        for (Element element : mElements) {
            element.draw(env, canvas);
        }
    }

    public void release() {
        mX = 0;
        mY = 0;
        mWidthLimit = 0;
        mContentWidth = 0;
        mContentHeight = 0;
        mLayoutWidth = 0;
        mElements.clear();
        sLinePool.release(this);
    }
}
