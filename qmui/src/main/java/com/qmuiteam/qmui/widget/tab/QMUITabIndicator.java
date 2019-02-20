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

package com.qmuiteam.qmui.widget.tab;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.qmuiteam.qmui.util.QMUIDrawableHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class QMUITabIndicator {

    /**
     * the height of indicator
     */
    private int mIndicatorHeight;
    /**
     * is indicator layout in top of QMUITabSegment?
     */
    private boolean mIndicatorTop = false;
    /**
     * use a drawable to present the indicator
     */
    private @Nullable Drawable mIndicatorDrawable;
    /**
     * the width of indicator changed when toggle to different tab
     */
    private boolean mIsIndicatorWidthFollowContent = true;

    /**
     * indicator rect, draw directly
     */
    private Rect mIndicatorRect = null;

    /**
     * indicator paint, draw directly
     */
    private Paint mIndicatorPaint = null;

    public QMUITabIndicator(int indicatorHeight, boolean indicatorTop,
                            boolean isIndicatorWidthFollowContent) {
        mIndicatorHeight = indicatorHeight;
        mIndicatorTop = indicatorTop;
        mIsIndicatorWidthFollowContent = isIndicatorWidthFollowContent;
    }

    public QMUITabIndicator(@NonNull Drawable drawable, boolean indicatorTop,
                            boolean isIndicatorWidthFollowContent) {
        mIndicatorDrawable = drawable;
        mIndicatorHeight = drawable.getIntrinsicHeight();
        mIndicatorTop = indicatorTop;
        mIsIndicatorWidthFollowContent = isIndicatorWidthFollowContent;
    }

    public boolean isIndicatorWidthFollowContent() {
        return mIsIndicatorWidthFollowContent;
    }

    public boolean isIndicatorTop() {
        return mIndicatorTop;
    }

    protected void updateInfo(int left, int width, int color) {
        if (mIndicatorRect == null) {
            mIndicatorRect = new Rect(left, 0,
                    left + width, 0);
        } else {
            mIndicatorRect.left = left;
            mIndicatorRect.right = left + width;
        }

        if (mIndicatorDrawable != null) {
            QMUIDrawableHelper.setDrawableTintColor(mIndicatorDrawable, color);
        } else {
            if (mIndicatorPaint == null) {
                mIndicatorPaint = new Paint();
                mIndicatorPaint.setStyle(Paint.Style.FILL);
            }
            mIndicatorPaint.setColor(color);
        }
    }

    protected void draw(Canvas canvas, int viewTop, int viewBottom) {
        if (mIndicatorRect != null) {
            if (mIndicatorTop) {
                mIndicatorRect.top = viewTop;
                mIndicatorRect.bottom = mIndicatorRect.top + mIndicatorHeight;
            } else {
                mIndicatorRect.bottom = viewBottom;
                mIndicatorRect.top = mIndicatorRect.bottom - mIndicatorHeight;
            }
            if (mIndicatorDrawable != null) {
                mIndicatorDrawable.setBounds(mIndicatorRect);
                mIndicatorDrawable.draw(canvas);
            } else {
                canvas.drawRect(mIndicatorRect, mIndicatorPaint);
            }
        }
    }
}