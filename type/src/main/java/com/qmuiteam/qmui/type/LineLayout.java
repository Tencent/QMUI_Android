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
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;

import com.qmuiteam.qmui.type.element.BreakWordLineElement;
import com.qmuiteam.qmui.type.element.CharOrPhraseElement;
import com.qmuiteam.qmui.type.element.IgnoreEffectElement;
import com.qmuiteam.qmui.type.element.Element;
import com.qmuiteam.qmui.type.element.NextParagraphElement;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class LineLayout {
    private int mMaxLines = Integer.MAX_VALUE;
    private TextUtils.TruncateAt mEllipsize;
    private boolean mCalculateWholeLines = true;
    private TypeEnvironment mTypeEnvironment;
    private TypeModel mTypeModel;
    private List<Line> mLines = new ArrayList<>();
    private boolean mDropLastIfSpace = true;
    private String mMoreText = null;
    private int mMoreTextColor = 0;
    private Typeface mMoreTextTypeface = null;
    private int mMoreUnderlineColor = Color.TRANSPARENT;
    private int mMoreBgColor = 0;
    private int mMoreUnderlineHeight = 0;
    private int mTotalLineCount = 0;

    public LineLayout(TypeEnvironment environment) {
        mTypeEnvironment = environment;
    }

    public LineLayout setMaxLines(int maxLines) {
        mMaxLines = maxLines;
        return this;
    }

    public LineLayout setEllipsize(TextUtils.TruncateAt ellipsize) {
        mEllipsize = ellipsize;
        return this;
    }

    public LineLayout setCalculateWholeLines(boolean calculateWholeLines) {
        mCalculateWholeLines = calculateWholeLines;
        return this;
    }

    public LineLayout setDropLastIfSpace(boolean dropLastIfSpace) {
        mDropLastIfSpace = dropLastIfSpace;
        return this;
    }

    public LineLayout setMoreText(String text, int color, Typeface typeface) {
        mMoreText = text;
        mMoreTextColor = color;
        mMoreTextTypeface = typeface;
        return this;
    }

    public LineLayout setMoreBackgroundColor(int color) {
        mMoreBgColor = color;
        return this;
    }

    public LineLayout setUnderline(int height, int color) {
        mMoreUnderlineHeight = height;
        mMoreUnderlineColor = color;
        return this;
    }

    public LineLayout setTypeModel(TypeModel typeModel) {
        mTypeModel = typeModel;
        return this;
    }

    public TypeModel getTypeModel() {
        return mTypeModel;
    }

    public void measureAndLayout() {
        mTypeEnvironment.clear();
        release();
        if (mTypeModel == null) {
            return;
        }
        Element element = mTypeModel.firstElement();
        if (element == null) {
            return;
        }
        Line line = Line.acquire();
        int y = 0;
        line.init(0, y, mTypeEnvironment.getWidthLimit());
        while (element != null) {
            element.measure(mTypeEnvironment);
            if (element instanceof NextParagraphElement) {
                line.add(element);
                line.layout(mTypeEnvironment, mDropLastIfSpace, false);
                mLines.add(line);
                if (canInterrupt()) {
                    return;
                }
                y += line.getContentHeight() + mTypeEnvironment.getParagraphSpace();
                line = createNewLine(y);
            } else if (line.getContentWidth() + element.getMeasureWidth() > mTypeEnvironment.getWidthLimit()) {
                if (mLines.size() == 0 && line.getSize() == 0) {
                    // the width is too small.
                    line.release();
                    return;
                }
                List<Element> back = line.handleWordBreak(mTypeEnvironment);
                line.layout(mTypeEnvironment, mDropLastIfSpace, false);
                mLines.add(line);
                if (canInterrupt()) {
                    handleEllipse(true);
                    return;
                }
                y += line.getContentHeight() + mTypeEnvironment.getLineSpace();
                line = createNewLine(y);
                if (back != null && !back.isEmpty()) {
                    for (Element el : back) {
                        line.add(el);
                    }
                }
                line.add(element);
            } else {
                line.add(element);
            }
            element = element.getNext();
        }
        if (line.getSize() > 0) {
            line.layout(mTypeEnvironment, mDropLastIfSpace, true);
            mLines.add(line);
        } else {
            line.release();
        }
        mTotalLineCount = mLines.size();
        handleEllipse(false);
    }

    private Line createNewLine(int y) {
        Line line = Line.acquire();
        line.init(0, y, mTypeEnvironment.getWidthLimit());
        return line;
    }

    private void handleEllipse(boolean fromInterrupt) {
        if (mLines.isEmpty() || mLines.size() < mMaxLines || (mLines.size() == mMaxLines && !fromInterrupt)) {
            return;
        }

        if (mEllipsize == TextUtils.TruncateAt.END) {
            handleEllipseEnd();
        } else if (mEllipsize == TextUtils.TruncateAt.START) {
            handleEllipseStart();
        } else if (mEllipsize == TextUtils.TruncateAt.MIDDLE) {
            handleEllipseMiddle();
        }
    }

    private void handleEllipseEnd() {
        for (int i = mLines.size() - 1; i >= mMaxLines; i--) {
            mLines.remove(mLines.get(i));
        }
        Line lastLine = mLines.get(mLines.size() - 1);
        int limitWidth = lastLine.getWidthLimit();
        Element ellipseElement = new CharOrPhraseElement("...", -1, -1);
        ellipseElement.addSingleEnvironmentUpdater(null, new EnvironmentUpdater() {
            @Override
            public void update(TypeEnvironment env) {
                env.clear();
            }
        });
        ellipseElement.measure(mTypeEnvironment);
        limitWidth -= ellipseElement.getMeasureWidth();

        Element moreElement = null;
        if (mMoreText != null && !mMoreText.isEmpty()) {
            moreElement = new CharOrPhraseElement(mMoreText, -1, -1);
            List<Integer> changeTypes = new ArrayList<>();
            changeTypes.add(TypeEnvironment.TYPE_TEXT_COLOR);
            changeTypes.add(TypeEnvironment.TYPE_BG_COLOR);
            changeTypes.add(TypeEnvironment.TYPE_TYPEFACE);
            changeTypes.add(TypeEnvironment.TYPE_BORDER_BOTTOM_COLOR);
            changeTypes.add(TypeEnvironment.TYPE_BORDER_BOTTOM_WIDTH);
            moreElement.addSingleEnvironmentUpdater(changeTypes, new EnvironmentUpdater() {
                @Override
                public void update(TypeEnvironment env) {
                    if (mMoreTextColor != 0) {
                        env.setTextColor(mMoreTextColor);
                    }
                    if (mMoreBgColor != 0) {
                        env.setBackgroundColor(mMoreBgColor);
                    }
                    if (mMoreTextTypeface != null) {
                        env.setTypeface(mMoreTextTypeface);
                    }

                    if (mMoreUnderlineHeight > 0) {
                        env.setBorderBottom(mMoreUnderlineHeight, mMoreUnderlineColor);
                    }
                }
            });
            moreElement.measure(mTypeEnvironment);
            limitWidth -= moreElement.getMeasureWidth();
        }

        int contentWidth = lastLine.getContentWidth();
        if (contentWidth < limitWidth) {
            lastLine.restoreVisibleChange();
        } else {
            List<Element> elements = lastLine.popAll();
            for (Element el : elements) {
                if (el.getMeasureWidth() <= limitWidth) {
                    lastLine.add(el);
                    limitWidth -= el.getMeasureWidth();
                } else {
                    break;
                }
            }
        }
        lastLine.add(ellipseElement);
        if (moreElement != null) {
            lastLine.add(moreElement);
        }
        lastLine.layout(mTypeEnvironment, mDropLastIfSpace, true);
    }

    private void handleEllipseStart() {
        mTypeEnvironment.clear();
        for (int i = mLines.size() - 1; i >= mMaxLines; i--) {
            mLines.remove(mLines.get(i));
        }
        Element ellipseElement = new CharOrPhraseElement("...", -1, -1);
        ellipseElement.addSingleEnvironmentUpdater(null, new EnvironmentUpdater() {
            @Override
            public void update(TypeEnvironment env) {
                env.clear();
            }
        });
        ellipseElement.measure(mTypeEnvironment);
        Queue<Element> elements = new LinkedList<>();
        elements.add(ellipseElement);
        for (int i = 0; i < mLines.size(); i++) {
            Line line = mLines.get(i);
            int limitWidth = line.getWidthLimit();
            elements.addAll(line.popAll());
            while (!elements.isEmpty()) {
                Element el = elements.peek();
                if (el != null) {
                    if (el instanceof NextParagraphElement) {
                        elements.poll();
                        line.add(el);
                        el.move(mTypeEnvironment);
                        break;
                    }

                    if (el instanceof BreakWordLineElement) {
                        elements.poll();
                        continue;
                    }
                    if (line.getContentWidth() + el.getMeasureWidth() <= limitWidth) {
                        elements.poll();
                        line.add(el);
                        el.move(mTypeEnvironment);

                    } else {
                        break;
                    }
                } else {
                    elements.poll();
                }
            }
            line.handleWordBreak(mTypeEnvironment);
            line.layout(mTypeEnvironment, mDropLastIfSpace, false);
            if (elements.isEmpty()) {
                return;
            }
        }
    }

    private void handleEllipseMiddle() {
        mTypeEnvironment.clear();
        List<Line> lines = new ArrayList<>(mLines);
        mLines.clear();
        Element ellipseElement = new CharOrPhraseElement("...", -1, -1);
        ellipseElement.measure(mTypeEnvironment);
        int ellipseLine = mMaxLines % 2 == 0 ? mMaxLines / 2 : (mMaxLines + 1) / 2;

        for (int i = 0; i < ellipseLine; i++) {
            mLines.add(lines.get(i));
        }
        Line handleLine = lines.get(ellipseLine - 1);
        int limitWidth = handleLine.getWidthLimit();
        Deque<Element> unHandled = new LinkedList<>(handleLine.popAll());
        while (!unHandled.isEmpty()) {
            Element el = unHandled.peek();
            if (el != null) {
                if (handleLine.getContentWidth() + el.getMeasureWidth() <= limitWidth / 2f - ellipseElement.getMeasureWidth() / 2) {
                    unHandled.poll();
                    handleLine.add(el);
                    el.move(mTypeEnvironment);
                } else {
                    break;
                }
            } else {
                unHandled.poll();
            }
        }
        ellipseElement.measure(mTypeEnvironment);
        handleLine.add(ellipseElement);

        int nextFullShowLine = lines.size() - mMaxLines + ellipseLine;
        int startLine = lines.size() - 1;
        // find the latest paragraph end line.
        for (int i = lines.size() - 2; i > nextFullShowLine; i--) {
            if (lines.get(i).isMiddleParagraphEndLine()) {
                startLine = i;
            }
        }
        for (int i = ellipseLine; i <= startLine; i++) {
            unHandled.addAll(lines.get(i).popAll());
        }

        for (int i = startLine; i >= nextFullShowLine; i--) {
            Line line = lines.get(i);
            while (!unHandled.isEmpty()) {
                Element element = unHandled.peekLast();
                if (element != null) {
                    if (element instanceof NextParagraphElement) {
                        unHandled.pollLast();
                        continue;
                    }
                    if (element instanceof BreakWordLineElement) {
                        unHandled.pollLast();
                        continue;
                    }
                    if (line.getContentWidth() + element.getMeasureWidth() <= line.getWidthLimit()) {
                        unHandled.pollLast();
                        line.addFirst(element);
                    } else {
                        break;
                    }
                } else {
                    unHandled.pollLast();
                }
            }
        }

        List<Element> toAdd = new LinkedList<>();
        int toAddWidth = 0;
        while (!unHandled.isEmpty()) {
            Element element = unHandled.peekLast();
            if (element != null) {
                if (element instanceof NextParagraphElement) {
                    unHandled.pollLast();
                    continue;
                }
                if (element instanceof BreakWordLineElement) {
                    unHandled.pollLast();
                    continue;
                }
                if (handleLine.getContentWidth() + toAddWidth + element.getMeasureWidth() <= handleLine.getWidthLimit()) {
                    unHandled.pollLast();
                    toAdd.add(0, element);
                    toAddWidth += element.getMeasureWidth();
                } else {
                    break;
                }
            } else {
                unHandled.pollLast();
            }
        }

        Element firstUnHandle = unHandled.peekFirst();
        Element lastUnHandle = unHandled.peekLast();
        Element effect = mTypeModel.getFirstEffect();
        if (firstUnHandle != null && lastUnHandle != null) {
            List<Element> ellipseEffect = new ArrayList<>();
            while (effect != null && effect.getIndex() <= lastUnHandle.getIndex()) {
                if (effect.getIndex() >= firstUnHandle.getIndex()) {
                    ellipseEffect.add(effect);
                }
                effect = effect.getNext();
            }
            if (ellipseEffect.size() > 0) {
                IgnoreEffectElement ignoreEffectElement = new IgnoreEffectElement(ellipseEffect);
                ignoreEffectElement.move(mTypeEnvironment);
                handleLine.add(ignoreEffectElement);
            }
        }
        for (Element el : toAdd) {
            el.move(mTypeEnvironment);
            handleLine.add(el);
        }
        handleLine.handleWordBreak(mTypeEnvironment);
        handleLine.layout(mTypeEnvironment, mDropLastIfSpace, ellipseLine == lines.size());
        int lastEnd = handleLine.getY() + handleLine.getContentHeight();
        for (int i = nextFullShowLine; i < lines.size(); i++) {
            Line line = lines.get(i);
            Line prev = lines.get(i - 1);
            if (prev.isMiddleParagraphEndLine()) {
                line.setY(lastEnd + mTypeEnvironment.getParagraphSpace());
            } else {
                line.setY(lastEnd + mTypeEnvironment.getLineSpace());
            }
            lastEnd = line.getY() + line.getContentHeight();
            line.move(mTypeEnvironment);
            line.handleWordBreak(mTypeEnvironment);
            line.layout(mTypeEnvironment, mDropLastIfSpace, i == lines.size() - 1);
            mLines.add(line);
        }
    }

    public int getMaxLayoutWidth() {
        int maxWidth = 0;
        for (Line line : mLines) {
            maxWidth = Math.max(maxWidth, line.getLayoutWidth());
        }
        return maxWidth;
    }

    public int getContentHeight() {
        if (mLines.isEmpty()) {
            return 0;
        }
        Line last = mLines.get(mLines.size() - 1);
        return last.getY() + last.getContentHeight();
    }

    public void draw(Canvas canvas) {
        mTypeEnvironment.clear();
        for (Line line : mLines) {
            line.draw(mTypeEnvironment, canvas);
        }
    }

    private boolean canInterrupt() {
        return mLines.size() == mMaxLines && !mCalculateWholeLines &&
                (mEllipsize == null || mEllipsize == TextUtils.TruncateAt.END);
    }

    public void release() {
        for (Line line : mLines) {
            line.release();
        }
        mLines.clear();
    }
}
