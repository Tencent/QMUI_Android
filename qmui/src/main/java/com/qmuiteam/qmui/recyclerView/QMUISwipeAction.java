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

package com.qmuiteam.qmui.recyclerView;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

public class QMUISwipeAction {
    final String mText;
    Drawable mIcon;
    int mTextSize;
    Typeface mTypeface;
    int mSwipeDirectionMiniSize;
    int mIconTextGap;
    int mTextColor;
    int mTextColorAttr = 0;
    int mBackgroundColor;
    int mBackgroundColorAttr = 0;
    int mIconAttr = 0;
    boolean mUseIconTint = false;
    int mPaddingStartEnd = 0;


    // inner use for layout and draw
    Paint paint;
    float contentWidth;
    float contentHeight;



    private QMUISwipeAction(String text, int textColor, int textSize, Typeface typeface,
                            int textColorAttr, Drawable icon, int iconAttr, boolean useIconTint,
                            int iconTextGap, int backgroundColor, int backgroundColorAttr,
                            int paddingStartEnd, int swipeDirectionMiniSize) {
        mText = text;
        mTextColor = textColor;
        mTextSize = textSize;
        mTypeface = typeface;
        mTextColorAttr = textColorAttr;
        mIcon = icon;
        mIconAttr = iconAttr;
        mUseIconTint = useIconTint;
        mIconTextGap = iconTextGap;
        mBackgroundColor = backgroundColor;
        mBackgroundColorAttr = backgroundColorAttr;
        mPaddingStartEnd = paddingStartEnd;
        mSwipeDirectionMiniSize = swipeDirectionMiniSize;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTypeface(typeface);
        paint.setTextSize(textSize);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        contentWidth = 0;
        contentHeight = fontMetrics.descent - fontMetrics.ascent;
        if(icon != null){
            contentWidth += icon.getIntrinsicWidth();
            contentHeight = Math.max(contentHeight, icon.getIntrinsicHeight());
        }
        if(text != null && text.length() > 0){
            contentWidth += paint.measureText(text);
            if(icon != null){
                contentWidth += iconTextGap;
            }
        }

    }

    public String getText() {
        return mText;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public int getTextSize() {
        return mTextSize;
    }

    public Typeface getTypeface() {
        return mTypeface;
    }

    public int getTextColorAttr() {
        return mTextColorAttr;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public int getIconAttr() {
        return mIconAttr;
    }

    public boolean isUseIconTint() {
        return mUseIconTint;
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public int getBackgroundColorAttr() {
        return mBackgroundColorAttr;
    }

    public int getPaddingStartEnd() {
        return mPaddingStartEnd;
    }

    public int getIconTextGap() {
        return mIconTextGap;
    }

    public int getSwipeDirectionMiniSize() {
        return mSwipeDirectionMiniSize;
    }
    
    public static class ActionBuilder{
        String mText;
        Drawable mIcon;
        int mTextSize;
        Typeface mTypeface;
        int mSwipeDirectionMiniSize;
        int mIconTextGap;
        int mTextColor;
        int mTextColorAttr = 0;
        int mBackgroundColor;
        int mBackgroundColorAttr = 0;
        int mIconAttr = 0;
        boolean mUseIconTint = false;
        int mPaddingStartEnd = 0;
        
        public ActionBuilder text(String text){
            mText = text;
            return this;
        }

        public ActionBuilder textSize(int textSize) {
            mTextSize = textSize;
            return this;
        }

        public ActionBuilder textColor(int textColor) {
            mTextColor = textColor;
            return this;
        }

        public ActionBuilder typeface(Typeface typeface) {
            mTypeface = typeface;
            return this;
        }

        public ActionBuilder textColorAttr(int textColorAttr) {
            mTextColorAttr = textColorAttr;
            return this;
        }

        public ActionBuilder icon(@Nullable Drawable drawable) {
            mIcon = drawable == null ? null : drawable.mutate();
            return this;
        }

        public ActionBuilder iconAttr(int iconAttr) {
            mIconAttr = iconAttr;
            return this;
        }

        public ActionBuilder useIconTint(boolean useIconTint) {
            mUseIconTint = useIconTint;
            return this;
        }

        public ActionBuilder backgroundColor(int backgroundColor) {
            mBackgroundColor = backgroundColor;
            return this;
        }

        public ActionBuilder backgroundColorAttr(int backgroundColorAttr) {
            mBackgroundColorAttr = backgroundColorAttr;
            return this;
        }

        public ActionBuilder paddingStartEnd(int paddingStartEnd) {
            mPaddingStartEnd = paddingStartEnd;
            return this;
        }

        public ActionBuilder iconTextGap(int iconTextGap) {
            mIconTextGap = iconTextGap;
            return this;
        }

        public ActionBuilder swipeDirectionMinSize(int minSize) {
            mSwipeDirectionMiniSize = minSize;
            return this;
        }

        public QMUISwipeAction build(){
            return new QMUISwipeAction(mText,
                    mTextColor, mTextSize, mTypeface, mTextColorAttr,
                    mIcon, mIconAttr, mUseIconTint, mIconTextGap,
                    mBackgroundColor, mBackgroundColorAttr,
                    mPaddingStartEnd, mSwipeDirectionMiniSize);
        }
    }
}
