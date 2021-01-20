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

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.util.SparseArray;

import com.qmuiteam.qmui.type.element.Element;

import java.util.Stack;

public class TypeEnvironment {
    private static final String TAG = "TypeEnvironment";
    public static final int TYPE_TEXT_COLOR = -1;
    public static final int TYPE_BG_COLOR = -2;
    public static final int TYPE_TYPEFACE = -3;
    public static final int TYPE_TEXT_SIZE = -4;
    public static final int TYPE_ALIGNMENT = -5;
    public static final int TYPE_LINE_SPACE = -6;
    public static final int TYPE_PARAGRAPH_SPACE = -7;
    public static final int TYPE_BORDER_TOP_WIDTH = -8;
    public static final int TYPE_BORDER_TOP_COLOR = -9;
    public static final int TYPE_BORDER_RIGHT_WIDTH = -10;
    public static final int TYPE_BORDER_RIGHT_COLOR = -11;
    public static final int TYPE_BORDER_BOTTOM_WIDTH = -12;
    public static final int TYPE_BORDER_BOTTOM_COLOR = -13;
    public static final int TYPE_BORDER_LEFT_WIDTH = -14;
    public static final int TYPE_BORDER_LEFT_COLOR = -15;
    public static final int TYPE_BORDER_PAINT = -16;

    public enum Alignment {
        LEFT,
        RIGHT,
        CENTER,
        JUSTIFY
    }

    private int mWidthLimit;
    private int mHeightLimit;

    private Typeface mTypeface;
    private float mTextSize;
    private int mLineSpace;
    private int mParagraphSpace;
    private Alignment mAlignment = Alignment.JUSTIFY;
    private int mLastLineJustifyMaxWidth = (int) (Resources.getSystem().getDisplayMetrics().density * 36);

    private int mTextColor = Color.BLACK;
    private int mBackgroundColor = Color.TRANSPARENT;

    private Paint mPaint = new Paint();
    private Paint mBgPaint = new Paint();

    private SparseArray<Object> mCustomProp = new SparseArray<>();

    private SparseArray<Stack<Object>> mStack = new SparseArray<>();
    private Element mLastRunElement = null;


    public TypeEnvironment() {
        mPaint.setAntiAlias(true);
        mBgPaint.setAntiAlias(true);
    }


    public void setMeasureLimit(int widthLimit, int heightLimit) {
        mWidthLimit = widthLimit;
        mHeightLimit = heightLimit;
    }

    public int getWidthLimit() {
        return mWidthLimit;
    }

    public int getHeightLimit() {
        return mHeightLimit;
    }

    public void setLastLineJustifyMaxWidth(int lastLineJustifyMaxWidth) {
        mLastLineJustifyMaxWidth = lastLineJustifyMaxWidth;
    }

    public int getLastLineJustifyMaxWidth() {
        return mLastLineJustifyMaxWidth;
    }

    public void setTypeface(Typeface typeface) {
        mTypeface = typeface;
        mPaint.setTypeface(typeface);
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
        mPaint.setColor(textColor);
    }

    public void setTextSize(float textSize) {
        mTextSize = textSize;
        mPaint.setTextSize(textSize);
    }

    public void setBackgroundColor(int backgroundColor) {
        mBackgroundColor = backgroundColor;
        mBgPaint.setColor(backgroundColor);
    }

    public void setAlignment(Alignment alignment) {
        mAlignment = alignment;
    }

    public void setLineSpace(int lineSpace) {
        mLineSpace = lineSpace;
    }

    public void setParagraphSpace(int paragraphSpace) {
        mParagraphSpace = paragraphSpace;
    }

    public int getParagraphSpace() {
        return Math.max(mParagraphSpace, mLineSpace);
    }

    public Alignment getAlignment() {
        return mAlignment;
    }

    public int getLineSpace() {
        return mLineSpace;
    }

    public float getTextSize() {
        return mTextSize;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public Paint getBgPaint() {
        return mBgPaint;
    }

    public Paint getPaint() {
        return mPaint;
    }

    public void setCustomProp(int type, Object value) {
        mCustomProp.put(type, value);
    }

    public void setBorderTop(int width, int color) {
        setCustomProp(TYPE_BORDER_TOP_WIDTH, width);
        setCustomProp(TYPE_BORDER_TOP_COLOR, color);
    }

    public int getBorderTopWidth() {
        return getIntCustomProp(TYPE_BORDER_TOP_WIDTH);
    }

    public int getBorderTopColor() {
        return getIntCustomProp(TYPE_BORDER_TOP_COLOR);
    }

    public void setBorderRight(int width, int color) {
        setCustomProp(TYPE_BORDER_RIGHT_WIDTH, width);
        setCustomProp(TYPE_BORDER_RIGHT_COLOR, color);
    }

    public int getBorderRightWidth() {
        return getIntCustomProp(TYPE_BORDER_RIGHT_WIDTH);
    }

    public int getBorderRightColor() {
        return getIntCustomProp(TYPE_BORDER_RIGHT_COLOR);
    }

    public void setBorderBottom(int width, int color) {
        setCustomProp(TYPE_BORDER_BOTTOM_WIDTH, width);
        setCustomProp(TYPE_BORDER_BOTTOM_COLOR, color);
    }

    public int getBorderBottomWidth() {
        return getIntCustomProp(TYPE_BORDER_BOTTOM_WIDTH);
    }

    public int getBorderBottomColor() {
        return getIntCustomProp(TYPE_BORDER_BOTTOM_COLOR);
    }

    public void setBorderLeft(int width, int color) {
        setCustomProp(TYPE_BORDER_LEFT_WIDTH, width);
        setCustomProp(TYPE_BORDER_LEFT_COLOR, color);
    }

    public int getBorderLeftWidth() {
        return getIntCustomProp(TYPE_BORDER_LEFT_WIDTH);
    }

    public int getBorderLeftColor() {
        return getIntCustomProp(TYPE_BORDER_LEFT_COLOR);
    }

    public Paint getBorderPaint() {
        Object obj = getCustomProp(TYPE_BORDER_PAINT);
        Paint paint;
        if (obj == null) {
            paint = new Paint();
            paint.setAntiAlias(true);
            setCustomProp(TYPE_BORDER_PAINT, paint);
        } else {
            paint = (Paint) obj;
        }
        return paint;
    }

    public Object getCustomProp(int type) {
        return mCustomProp.get(type);
    }

    public int getIntCustomProp(int type) {
        Object obj = mCustomProp.get(type);
        if (!(obj instanceof Integer)) {
            return 0;
        }
        return (int) obj;
    }

    public TypeEnvironment snapshot() {
        TypeEnvironment env = new TypeEnvironment();
        env.setMeasureLimit(mWidthLimit, mHeightLimit);
        env.setAlignment(mAlignment);
        env.setLineSpace(mLineSpace);
        env.setParagraphSpace(mParagraphSpace);
        env.setTextSize(mTextSize);
        env.setTypeface(mTypeface);

        env.setTextColor(mTextColor);
        env.setBackgroundColor(mBackgroundColor);
        for(int i =0; i< mStack.size(); i++){
            env.mStack.put(mStack.keyAt(i), (Stack<Object>) mStack.valueAt(i).clone());
        }

        if (mCustomProp != null) {
            for (int i = 0; i < mCustomProp.size(); i++) {
                env.setCustomProp(mCustomProp.keyAt(i), mCustomProp.valueAt(i));
            }
        }
        return env;
    }

    void setLastRunElement(Element lastRunElement) {
        mLastRunElement = lastRunElement;
    }

    Element getLastRunElement() {
        return mLastRunElement;
    }

    public void save(int type) {
        Stack<Object> stack = mStack.get(type);
        if (stack == null) {
            stack = new Stack<>();
            mStack.put(type, stack);
        }
        if (type == TYPE_TEXT_COLOR) {
            stack.push(mTextColor);
        } else if (type == TYPE_BG_COLOR) {
            stack.push(mBackgroundColor);
        } else if (type == TYPE_TYPEFACE) {
            stack.push(mTypeface);
        } else if (type == TYPE_TEXT_SIZE) {
            stack.push(mTextSize);
        } else if (type == TYPE_ALIGNMENT) {
            stack.push(mAlignment);
        } else if (type == TYPE_LINE_SPACE) {
            stack.push(mLineSpace);
        } else if (type == TYPE_PARAGRAPH_SPACE) {
            stack.push(mParagraphSpace);
        } else {
            stack.push(mCustomProp.get(type));
        }
    }

    public void restore(int type) {
        Stack<Object> stack = mStack.get(type);
        if (stack == null || stack.isEmpty()) {
            Log.d(TAG, "restore (type = " + type + ")with a empty stack.");
            return;
        }
        Object v = stack.pop();
        restore(type, v);
    }

    private void restore(int type, Object v){
        if (type == TYPE_TEXT_COLOR) {
            setTextColor((Integer) v);
        } else if (type == TYPE_BG_COLOR) {
            setBackgroundColor((Integer) v);
        } else if (type == TYPE_TYPEFACE) {
            setTypeface((Typeface) v);
        } else if (type == TYPE_TEXT_SIZE) {
            setTextSize((Float) v);
        } else if (type == TYPE_ALIGNMENT) {
            setAlignment((Alignment) v);
        } else if (type == TYPE_LINE_SPACE) {
            setLineSpace((Integer) v);
        } else if (type == TYPE_PARAGRAPH_SPACE) {
            setParagraphSpace((Integer) v);
        } else {
            setCustomProp(type, v);
        }
    }

    public void clear() {
        for (int i = 0; i < mStack.size(); i++) {
            Stack<Object> stack = mStack.valueAt(i);
            if (stack != null && stack.size() > 0) {
                while (stack.size() > 1){
                    stack.pop();
                }
                restore(mStack.keyAt(i), stack.pop());
            }
        }
    }
}
