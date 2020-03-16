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

import android.animation.TimeInterpolator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import com.qmuiteam.qmui.QMUIInterpolatorStaticHolder;

public class QMUISwipeAction {
    final String mText;
    Drawable mIcon;
    int mTextSize;
    Typeface mTypeface;
    int mSwipeDirectionMiniSize;
    int mIconTextGap;
    int mTextColor;
    int mTextColorAttr;
    int mBackgroundColor;
    int mBackgroundColorAttr;
    int mIconAttr;
    boolean mUseIconTint;
    int mPaddingStartEnd;
    int mOrientation;
    boolean mReverseDrawOrder;
    TimeInterpolator mSwipeMoveInterpolator;
    int mSwipePxPerMS;


    // inner use for layout and draw
    Paint paint;
    float contentWidth;
    float contentHeight;


    private QMUISwipeAction(ActionBuilder builder) {
        mText = builder.mText != null && builder.mText.length() > 0 ? builder.mText : null;
        mTextColor = builder.mTextColor;
        mTextSize = builder.mTextSize;
        mTypeface = builder.mTypeface;
        mTextColorAttr = builder.mTextColorAttr;
        mIcon = builder.mIcon;
        mIconAttr = builder.mIconAttr;
        mUseIconTint = builder.mUseIconTint;
        mIconTextGap = builder.mIconTextGap;
        mBackgroundColor = builder.mBackgroundColor;
        mBackgroundColorAttr = builder.mBackgroundColorAttr;
        mPaddingStartEnd = builder.mPaddingStartEnd;
        mSwipeDirectionMiniSize = builder.mSwipeDirectionMiniSize;
        mOrientation = builder.mOrientation;
        mReverseDrawOrder = builder.mReverseDrawOrder;
        mSwipeMoveInterpolator = builder.mSwipeMoveInterpolator;
        mSwipePxPerMS = builder.mSwipePxPerMS;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTypeface(mTypeface);
        paint.setTextSize(mTextSize);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        if (mIcon != null && mText != null) {
            mIcon.setBounds(0, 0, mIcon.getIntrinsicWidth(), mIcon.getIntrinsicHeight());
            if (mOrientation == ActionBuilder.HORIZONTAL) {
                contentWidth = mIcon.getIntrinsicWidth() + mIconTextGap + paint.measureText(mText);
                contentHeight = Math.max(fontMetrics.descent - fontMetrics.ascent, mIcon.getIntrinsicHeight());
            } else {
                contentWidth = Math.max(mIcon.getIntrinsicWidth(), paint.measureText(mText));
                contentHeight = fontMetrics.descent - fontMetrics.ascent + mIconTextGap + mIcon.getIntrinsicHeight();
            }
        } else if (mIcon != null) {
            mIcon.setBounds(0, 0, mIcon.getIntrinsicWidth(), mIcon.getIntrinsicHeight());
            contentWidth = mIcon.getIntrinsicWidth();
            contentHeight = mIcon.getIntrinsicHeight();
        } else if (mText != null) {
            contentWidth = paint.measureText(mText);
            contentHeight = fontMetrics.descent - fontMetrics.ascent;
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

    public int getOrientation() {
        return mOrientation;
    }

    protected void draw(Canvas canvas) {
        if (mText != null && mIcon != null) {
            if (mOrientation == ActionBuilder.HORIZONTAL) {
                if (mReverseDrawOrder) {
                    canvas.drawText(mText, 0,
                            (contentHeight - paint.descent() + paint.ascent()) / 2 - paint.ascent(),
                            paint);
                    canvas.save();
                    canvas.translate(contentWidth - mIcon.getIntrinsicWidth(), (contentHeight - mIcon.getIntrinsicHeight()) / 2);
                    mIcon.draw(canvas);
                    canvas.restore();
                } else {
                    canvas.save();
                    canvas.translate(0, (contentHeight - mIcon.getIntrinsicHeight()) / 2);
                    mIcon.draw(canvas);
                    canvas.restore();
                    canvas.drawText(mText,
                            mIcon.getIntrinsicWidth() + mIconTextGap,
                            (contentHeight - paint.descent() + paint.ascent()) / 2 - paint.ascent(),
                            paint);
                }

            } else {
                float textWidth = paint.measureText(mText);
                if (mReverseDrawOrder) {
                    canvas.drawText(mText, (contentWidth - textWidth) / 2, -paint.ascent(), paint);
                    canvas.save();
                    canvas.translate(
                            (contentWidth - mIcon.getIntrinsicWidth()) / 2,
                            contentHeight - mIcon.getIntrinsicHeight());
                    mIcon.draw(canvas);
                    canvas.restore();
                } else {
                    canvas.save();
                    canvas.translate((contentWidth - mIcon.getIntrinsicWidth()) / 2, 0);
                    mIcon.draw(canvas);
                    canvas.restore();
                    canvas.drawText(mText, (contentWidth - textWidth) / 2, contentHeight - paint.descent(), paint);
                }
            }
        } else if (mIcon != null) {
            mIcon.draw(canvas);
        } else if (mText != null) {
            canvas.drawText(mText, 0, -paint.ascent(), paint);
        }

    }

    public static class ActionBuilder {
        public static final int VERTICAL = 1;
        public static final int HORIZONTAL = 2;
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
        int mOrientation = VERTICAL;
        boolean mReverseDrawOrder = false;
        TimeInterpolator mSwipeMoveInterpolator = QMUIInterpolatorStaticHolder.ACCELERATE_INTERPOLATOR;
        int mSwipePxPerMS = 2;

        public ActionBuilder text(String text) {
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

        public ActionBuilder orientation(int orientation) {
            mOrientation = orientation;
            return this;
        }

        public ActionBuilder reverseDrawOrder(boolean reverse) {
            mReverseDrawOrder = reverse;
            return this;
        }

        public ActionBuilder swipeMoveInterpolator(TimeInterpolator interpolator) {
            mSwipeMoveInterpolator = interpolator;
            return this;
        }

        public ActionBuilder swipePxPerMS(int swipePxPerMS){
            mSwipePxPerMS = swipePxPerMS;
            return this;
        }

        public QMUISwipeAction build() {
            return new QMUISwipeAction(this);
        }
    }
}
