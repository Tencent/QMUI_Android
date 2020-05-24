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
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.qmuiteam.qmui.type.element.Element;
import com.qmuiteam.qmui.type.element.NextParagraphElement;

import java.util.ArrayList;
import java.util.List;

public class LineLayout {
    private int mMaxLines;
    private TextUtils.TruncateAt mEllipsize;
    private boolean mCalculateWholeLines;
    private TypeEnvironment mTypeEnvironment;
    private TypeModel mTypeModel;
    private List<Line> mLines = new ArrayList<>();
    private boolean mDropLastIfSpace;

    private LineLayout(Builder builder) {
        mTypeEnvironment = builder.mTypeEnvironment;
        mMaxLines = builder.mMaxLines;
        mEllipsize = builder.mEllipsize;
        mCalculateWholeLines = builder.mCalculateWholeLines;
        mDropLastIfSpace = builder.mDropLastIfSpace;
    }

    public void setTypeModel(TypeModel typeModel) {
        mTypeModel = typeModel;
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
                line.layout(mTypeEnvironment, mDropLastIfSpace, true);
                mLines.add(line);
                if (canInterrupt()) {
                    return;
                }
                y += line.getContentHeight() + mTypeEnvironment.getParagraphSpace();
                line = Line.acquire();
                line.init(0, y, mTypeEnvironment.getWidthLimit());
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
                    return;
                }
                y += line.getContentHeight() + mTypeEnvironment.getLineSpace();
                line = Line.acquire();
                line.init(0, y, mTypeEnvironment.getWidthLimit());
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
    }

    public int getMaxLayoutWidth(){
        int maxWidth = 0;
        for(Line line: mLines){
            maxWidth = Math.max(maxWidth, line.getLayoutWidth());
        }
        return maxWidth;
    }

    public int getContentHeight(){
        if(mLines.isEmpty()){
            return 0;
        }
        Line last = mLines.get(mLines.size() - 1);
        return last.getY() + last.getContentHeight();
    }

    public void draw(Canvas canvas) {
        mTypeEnvironment.clear();
        for(Line line: mLines){
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

    public static class Builder {
        private TypeEnvironment mTypeEnvironment;
        private int mMaxLines = Integer.MAX_VALUE;
        private TextUtils.TruncateAt mEllipsize;
        private boolean mCalculateWholeLines = true;
        private boolean mDropLastIfSpace = true;

        public Builder(@NonNull TypeEnvironment typeEnvironment) {
            mTypeEnvironment = typeEnvironment;
        }

        public Builder setMaxLines(int maxLines) {
            mMaxLines = maxLines;
            return this;
        }

        public Builder setCalculateWholeLines(boolean calculateWholeLines) {
            mCalculateWholeLines = calculateWholeLines;
            return this;
        }

        public Builder setEllipsize(TextUtils.TruncateAt ellipsize) {
            mEllipsize = ellipsize;
            return this;
        }

        public Builder setDropLastIfSpace(boolean dropLastIfSpace) {
            mDropLastIfSpace = dropLastIfSpace;
            return this;
        }

        public LineLayout build() {
            return new LineLayout(this);
        }
    }
}
