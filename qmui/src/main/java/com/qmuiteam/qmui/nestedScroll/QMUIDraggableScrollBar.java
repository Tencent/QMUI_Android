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

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qmuiteam.qmui.nestedScroll;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUILangHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

public class QMUIDraggableScrollBar extends View {

    private int[] STATE_PRESSED = new int[]{android.R.attr.state_pressed};
    private int[] STATE_NORMAL = new int[]{};

    private Drawable mDragDrawable;
    private int mKeepShownTime = 800;
    private int mTransitionDuration = 100;
    private long mStartTransitionTime = 0;
    private float mCurrentAlpha = 0f;
    private float mPercent = 0f;
    private Runnable mDelayInvalidateRunnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };
    private boolean mIsInDragging = false;
    private Callback mCallback;
    private int mDrawableDrawTop = -1;
    private float mDragInnerTop = 0;
    private int mAdjustDistanceProtection = QMUIDisplayHelper.dp2px(getContext(), 20);
    private int mAdjustMaxDistanceOnce = QMUIDisplayHelper.dp2px(getContext(), 4);
    private boolean enableFadeInAndOut = true;

    public QMUIDraggableScrollBar(Context context) {
        this(context, (AttributeSet) null);
    }

    public QMUIDraggableScrollBar(Context context, @NonNull Drawable dragDrawable) {
        this(context, (AttributeSet) null);
        mDragDrawable = dragDrawable.mutate();
    }

    public QMUIDraggableScrollBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void setKeepShownTime(int keepShownTime) {
        mKeepShownTime = keepShownTime;
    }

    public void setTransitionDuration(int transitionDuration) {
        mTransitionDuration = transitionDuration;
    }

    public void setEnableFadeInAndOut(boolean enableFadeInAndOut) {
        this.enableFadeInAndOut = enableFadeInAndOut;
    }

    public boolean isEnableFadeInAndOut() {
        return enableFadeInAndOut;
    }

    public void setDragDrawable(Drawable dragDrawable) {
        mDragDrawable = dragDrawable.mutate();
        invalidate();
    }

    public void setPercent(float percent) {
        if(!mIsInDragging){
            setPercentInternal(percent);
        }
    }

    private void setPercentInternal(float percent){
        mPercent = percent;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable drawable = mDragDrawable;
        if (drawable == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(
                drawable.getIntrinsicWidth(), MeasureSpec.EXACTLY), heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Drawable drawable = mDragDrawable;
        if (drawable == null) {
            return super.onTouchEvent(event);
        }
        int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();
        if (action == MotionEvent.ACTION_DOWN) {
            mIsInDragging = false;
            if (mCurrentAlpha > 0 && x > getWidth() - drawable.getIntrinsicWidth()
                    && y >= mDrawableDrawTop && y <= mDrawableDrawTop + drawable.getIntrinsicHeight()) {
                mDragInnerTop = y - mDrawableDrawTop;
                getParent().requestDisallowInterceptTouchEvent(true);
                mIsInDragging = true;
                if(mCallback != null){
                    mCallback.onDragStarted();
                    mDragDrawable.setState(STATE_PRESSED);
                }
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (mIsInDragging) {
                getParent().requestDisallowInterceptTouchEvent(true);
                onDragging(drawable, y);
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (mIsInDragging) {
                mIsInDragging = false;
                onDragging(drawable, y);
                if(mCallback != null){
                    mCallback.onDragEnd();
                    mDragDrawable.setState(STATE_NORMAL);
                }
            }
        }
        return mIsInDragging;
    }

    private void onDragging(Drawable drawable, float currentY) {
        float percent = (currentY - getScrollBarTopMargin() - mDragInnerTop) / (getHeight() - getScrollBarBottomMargin() - getScrollBarTopMargin() - drawable.getIntrinsicHeight());
        percent = QMUILangHelper.constrain(percent, 0f, 1f);
        if (mCallback != null) {
            mCallback.onDragToPercent(percent);
        }
        setPercentInternal(percent);
    }

    public void awakenScrollBar() {
        if (mDragDrawable == null) {
            mDragDrawable = ContextCompat.getDrawable(getContext(), R.drawable.qmui_icon_scroll_bar);
        }
        long current = System.currentTimeMillis();
        if (current - mStartTransitionTime > mTransitionDuration) {
            mStartTransitionTime = current - mTransitionDuration;
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Drawable drawable = mDragDrawable;
        if (drawable == null) {
            return;
        }
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        if (drawableWidth <= 0 || drawableHeight <= 0) {
            return;
        }

        int needInvalidate = -1;
        if(enableFadeInAndOut){
            long timeAfterStartShow = System.currentTimeMillis() - mStartTransitionTime;
            long timeAfterEndShow;
            if (timeAfterStartShow < mTransitionDuration) {
                // in show animation
                mCurrentAlpha = timeAfterStartShow * 1f / mTransitionDuration;
                needInvalidate = 0;
            } else if (timeAfterStartShow - mTransitionDuration < mKeepShownTime) {
                // keep show
                mCurrentAlpha = 1f;
                needInvalidate = (int) (mKeepShownTime - (timeAfterStartShow - mTransitionDuration));
            } else if ((timeAfterEndShow = timeAfterStartShow - mTransitionDuration - mKeepShownTime)
                    < mTransitionDuration) {
                // in hide animation
                mCurrentAlpha = 1 - timeAfterEndShow * 1f / mTransitionDuration;
                needInvalidate = 0;
            } else {
                mCurrentAlpha = 0f;
            }
            if (mCurrentAlpha <= 0f) {
                return;
            }
        }else{
            mCurrentAlpha = 1f;
        }
        drawable.setAlpha((int) (mCurrentAlpha * 255));

        int totalHeight = getHeight() - getScrollBarTopMargin() - getScrollBarBottomMargin();
        int totalWidth = getWidth();
        int top = getScrollBarTopMargin() + (int) ((totalHeight - drawableHeight) * mPercent);
        int left = totalWidth - drawableWidth;
        if (!mIsInDragging && mDrawableDrawTop > 0) {
            int moveDistance = top - mDrawableDrawTop;
            if (moveDistance > mAdjustMaxDistanceOnce && moveDistance < mAdjustDistanceProtection) {
                top = mDrawableDrawTop + mAdjustMaxDistanceOnce;
                needInvalidate = 0;
            } else if (moveDistance < -mAdjustMaxDistanceOnce && moveDistance > -mAdjustDistanceProtection) {
                top = mDrawableDrawTop - mAdjustMaxDistanceOnce;
                needInvalidate = 0;
            }
        }
        drawable.setBounds(0, 0, drawableWidth, drawableHeight);
        canvas.save();
        canvas.translate(left, top);
        drawable.draw(canvas);
        canvas.restore();
        mDrawableDrawTop = top;

        if (needInvalidate == 0) {
            invalidate();
        } else if (needInvalidate > 0) {
            ViewCompat.postOnAnimationDelayed(this, mDelayInvalidateRunnable, needInvalidate);
        }
    }

    protected int getScrollBarTopMargin() {
        return 0;
    }

    protected int getScrollBarBottomMargin() {
        return 0;
    }

    interface Callback {
        void onDragStarted();
        void onDragToPercent(float percent);
        void onDragEnd();
    }
}
