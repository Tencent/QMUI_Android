package com.qmuiteam.qmui.nestedScroll;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUILangHelper;

public class QMUIDraggableScrollBar extends View {

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
    private int mDrawableDrawTop = 0;
    private int mMoveMaxDistanceOnceIfNotDrag = QMUIDisplayHelper.dp2px(getContext(), 4);

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

    public void setDragDrawable(Drawable dragDrawable) {
        mDragDrawable = dragDrawable.mutate();
        invalidate();
    }

    public void setPercent(float percent) {
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
                getParent().requestDisallowInterceptTouchEvent(true);
                mIsInDragging = true;
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
            }
        }
        return mIsInDragging;
    }

    private void onDragging(Drawable drawable, float currentY) {
        float percent = currentY / (getHeight() - drawable.getIntrinsicHeight());
        percent = QMUILangHelper.constrain(percent, 0f, 1f);
        if (mCallback != null) {
            mCallback.onDragToPercent(percent);
        }
        setPercent(percent);
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
        long timeAfterStartShow = System.currentTimeMillis() - mStartTransitionTime;
        Log.i("cgine", "timeAfterStartShow = " + timeAfterStartShow);
        long timeAfterEndShow;
        int needInvalidate = -1;
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
        drawable.setAlpha((int) (mCurrentAlpha * 255));
        int totalHeight = getHeight() - getScrollBarTopMargin() - getScrollBarBottomMargin();
        int totalWidth = getWidth();
        int top = getScrollBarTopMargin() + (int) ((totalHeight - drawableHeight) * mPercent);
        int left = totalWidth - drawableWidth;
        if(!mIsInDragging){
            int moveDistance = top - mDrawableDrawTop;
            if(moveDistance > mMoveMaxDistanceOnceIfNotDrag){
                top = mDrawableDrawTop + mMoveMaxDistanceOnceIfNotDrag;
                needInvalidate = 0;
            }else if(moveDistance < -mMoveMaxDistanceOnceIfNotDrag){
                top = mDrawableDrawTop - mMoveMaxDistanceOnceIfNotDrag;
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

    protected int getScrollBarTopMargin(){
        return 0;
    }

    protected int getScrollBarBottomMargin(){
        return 0;
    }

    interface Callback {
        void onDragToPercent(float percent);
    }
}
