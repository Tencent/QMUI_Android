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

package com.qmuiteam.qmui.nestedScroll;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.OverScroller;

import com.qmuiteam.qmui.layout.QMUIFrameLayout;
import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.util.QMUIViewOffsetHelper;

import androidx.annotation.NonNull;
import androidx.core.view.NestedScrollingChild;
import androidx.core.view.NestedScrollingChild2;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.NestedScrollingParent2;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;

import static com.qmuiteam.qmui.QMUIInterpolatorStaticHolder.QUNITIC_INTERPOLATOR;


public abstract class QMUIContinuousNestedBottomDelegateLayout extends QMUIFrameLayout implements
        NestedScrollingChild2, NestedScrollingParent2, IQMUIContinuousNestedBottomView {
    public static final String KEY_SCROLL_INFO_OFFSET = "@qmui_scroll_info_bottom_dl_offset";

    private final NestedScrollingParentHelper mParentHelper;
    private final NestedScrollingChildHelper mChildHelper;
    private View mHeaderView;
    private View mContentView;
    private QMUIViewOffsetHelper mHeaderViewOffsetHelper;
    private QMUIViewOffsetHelper mContentViewOffsetHelper;
    private IQMUIContinuousNestedBottomView.OnScrollNotifier mOnScrollNotifier;

    private static final int INVALID_POINTER = -1;
    private boolean isBeingDragged;
    private int activePointerId = INVALID_POINTER;
    private int lastMotionY;
    private int touchSlop = -1;
    private VelocityTracker velocityTracker;
    private final ViewFlinger mViewFlinger;
    private final int[] mScrollConsumed = new int[2];
    private final int[] mScrollOffset = new int[2];
    private Rect mTempRect = new Rect();
    private int mNestedOffsetY = 0;
    private Runnable mCheckLayoutAction = new Runnable() {
        @Override
        public void run() {
            checkLayout();
        }
    };

    public QMUIContinuousNestedBottomDelegateLayout(Context context) {
        this(context, null);
    }

    public QMUIContinuousNestedBottomDelegateLayout(Context context, AttributeSet attrs) {
        this(context, null, 0);
    }

    public QMUIContinuousNestedBottomDelegateLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mParentHelper = new NestedScrollingParentHelper(this);
        mChildHelper = new NestedScrollingChildHelper(this);

        ViewCompat.setNestedScrollingEnabled(this, true);
        mHeaderView = onCreateHeaderView();
        mContentView = onCreateContentView();
        if (!(mContentView instanceof IQMUIContinuousNestedBottomView)) {
            throw new IllegalStateException("the view create by onCreateContentView() " +
                    "should implement from IQMUIContinuousNestedBottomView");
        }
        addView(mHeaderView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, getHeaderHeightLayoutParam()));
        addView(mContentView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mHeaderViewOffsetHelper = new QMUIViewOffsetHelper(mHeaderView);
        mContentViewOffsetHelper = new QMUIViewOffsetHelper(mContentView);
        mViewFlinger = new ViewFlinger();
    }

    public View getHeaderView() {
        return mHeaderView;
    }

    public View getContentView() {
        return mContentView;
    }

    public int getOffsetCurrent() {
        return -mHeaderViewOffsetHelper.getTopAndBottomOffset();
    }

    public int getOffsetRange() {
        return -getMiniOffset();
    }

    private int getMiniOffset() {
        IQMUIContinuousNestedBottomView b = (IQMUIContinuousNestedBottomView) mContentView;
        int contentHeight = b.getContentHeight();
        FrameLayout.LayoutParams headerLp = (LayoutParams) mHeaderView.getLayoutParams();
        int minOffset = -mHeaderView.getHeight() - headerLp.bottomMargin + getHeaderStickyHeight();
        if (contentHeight != IQMUIContinuousNestedBottomView.HEIGHT_IS_ENOUGH_TO_SCROLL) {
            minOffset += mContentView.getHeight() - contentHeight;
            minOffset = Math.min(minOffset, 0);
        }
        return minOffset;
    }

    @Override
    public int getContentHeight() {
        IQMUIContinuousNestedBottomView b = (IQMUIContinuousNestedBottomView) mContentView;
        int bc = b.getContentHeight();
        if (bc == IQMUIContinuousNestedBottomView.HEIGHT_IS_ENOUGH_TO_SCROLL || bc > mContentView.getHeight()) {
            return IQMUIContinuousNestedBottomView.HEIGHT_IS_ENOUGH_TO_SCROLL;
        }
        int bottomMargin = getContentBottomMargin();
        if (bc + mHeaderView.getHeight() + bottomMargin > getHeight()) {
            return IQMUIContinuousNestedBottomView.HEIGHT_IS_ENOUGH_TO_SCROLL;
        }
        return mHeaderView.getHeight() + bc + bottomMargin;
    }

    @NonNull
    protected abstract View onCreateHeaderView();

    @NonNull
    protected abstract View onCreateContentView();

    protected int getHeaderStickyHeight() {
        return 0;
    }


    protected int getHeaderHeightLayoutParam() {
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    protected int getContentBottomMargin() {
        return 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        mContentView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(
                heightSize - getHeaderStickyHeight() - getContentBottomMargin(),
                MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mHeaderView.layout(0, 0, mHeaderView.getMeasuredWidth(),
                mHeaderView.getMeasuredHeight());


        int contentTop = mHeaderView.getBottom();
        mContentView.layout(0, contentTop, mContentView.getMeasuredWidth(),
                contentTop + mContentView.getMeasuredHeight());

        mHeaderViewOffsetHelper.onViewLayout();
        mContentViewOffsetHelper.onViewLayout();
        postCheckLayout();
    }

    public void postCheckLayout() {
        removeCallbacks(mCheckLayoutAction);
        post(mCheckLayoutAction);
    }

    public void checkLayout() {
        int offsetCurrent = getOffsetCurrent();
        int offsetRange = getOffsetRange();
        IQMUIContinuousNestedBottomView bottomView = (IQMUIContinuousNestedBottomView) mContentView;
        if (offsetCurrent < offsetRange && bottomView.getCurrentScroll() > 0) {
            bottomView.consumeScroll(Integer.MIN_VALUE);
        }
    }

    private int offsetBy(int dyUnConsumed) {
        int canConsume = 0;


        FrameLayout.LayoutParams headerLp = (LayoutParams) mHeaderView.getLayoutParams();
        int minOffset = getMiniOffset();
        if (dyUnConsumed > 0) {
            canConsume = Math.min(mHeaderView.getTop() - minOffset, dyUnConsumed);
        } else if (dyUnConsumed < 0) {
            canConsume = Math.max(mHeaderView.getTop() - headerLp.topMargin, dyUnConsumed);
        }
        if (canConsume != 0) {
            mHeaderViewOffsetHelper.setTopAndBottomOffset(mHeaderViewOffsetHelper.getTopAndBottomOffset() - canConsume);
            mContentViewOffsetHelper.setTopAndBottomOffset(mContentViewOffsetHelper.getTopAndBottomOffset() - canConsume);
        }
        mOnScrollNotifier.notify(-mHeaderViewOffsetHelper.getTopAndBottomOffset(),
                mHeaderView.getHeight() + ((IQMUIContinuousNestedBottomView) mContentView).getScrollOffsetRange());
        return dyUnConsumed - canConsume;
    }


    @Override
    public void consumeScroll(int dy) {
        if (dy == Integer.MAX_VALUE) {
            offsetBy(dy);
            ((IQMUIContinuousNestedBottomView) mContentView).consumeScroll(Integer.MAX_VALUE);
            return;
        } else if (dy == Integer.MIN_VALUE) {
            ((IQMUIContinuousNestedBottomView) mContentView).consumeScroll(Integer.MIN_VALUE);
            offsetBy(dy);
            return;
        }
        ((IQMUIContinuousNestedBottomView) mContentView).consumeScroll(dy);
    }

    @Override
    public void smoothScrollYBy(int dy, int duration) {
        ((IQMUIContinuousNestedBottomView) mContentView).smoothScrollYBy(dy, duration);
    }

    @Override
    public void stopScroll() {
        ((IQMUIContinuousNestedBottomView) mContentView).stopScroll();
    }

    @Override
    public int getCurrentScroll() {
        return -mHeaderViewOffsetHelper.getTopAndBottomOffset() +
                ((IQMUIContinuousNestedBottomView) mContentView).getCurrentScroll();
    }

    @Override
    public int getScrollOffsetRange() {
        if (getContentHeight() != HEIGHT_IS_ENOUGH_TO_SCROLL) {
            return 0;
        }
        return mHeaderView.getHeight() - getHeaderStickyHeight() +
                ((IQMUIContinuousNestedBottomView) mContentView).getScrollOffsetRange();
    }

    @Override
    public void injectScrollNotifier(final OnScrollNotifier notifier) {
        mOnScrollNotifier = notifier;
        if (mContentView instanceof IQMUIContinuousNestedBottomView) {
            ((IQMUIContinuousNestedBottomView) mContentView).injectScrollNotifier(new OnScrollNotifier() {
                @Override
                public void notify(int innerOffset, int innerRange) {
                    notifier.notify(innerOffset - mHeaderView.getTop(),
                            innerRange + mHeaderView.getHeight());
                }

                @Override
                public void onScrollStateChange(View view, int newScrollState) {
                    notifier.onScrollStateChange(view, newScrollState);
                }
            });
        }
    }

    @Override
    public void saveScrollInfo(@NonNull Bundle bundle) {
        bundle.putInt(KEY_SCROLL_INFO_OFFSET, mHeaderViewOffsetHelper.getTopAndBottomOffset());
        if (mContentView != null) {
            ((IQMUIContinuousNestedBottomView) mContentView).saveScrollInfo(bundle);
        }
    }

    @Override
    public void restoreScrollInfo(@NonNull Bundle bundle) {
        int offset = bundle.getInt(KEY_SCROLL_INFO_OFFSET, 0);
        offset = QMUILangHelper.constrain(offset, getMiniOffset(), 0);
        mHeaderViewOffsetHelper.setTopAndBottomOffset(offset);
        mContentViewOffsetHelper.setTopAndBottomOffset(offset);
        if (mContentView != null) {
            ((IQMUIContinuousNestedBottomView) mContentView).restoreScrollInfo(bundle);
        }
    }

    // NestedScrollingChild2

    @Override
    public boolean startNestedScroll(int axes, int type) {
        return mChildHelper.startNestedScroll(axes, type);
    }

    @Override
    public void stopNestedScroll(int type) {
        mChildHelper.stopNestedScroll(type);
    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return mChildHelper.hasNestedScrollingParent(type);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow, int type) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow, type);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow,
                                           int type) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
    }

    // NestedScrollingChild

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return startNestedScroll(axes, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void stopNestedScroll() {
        stopNestedScroll(ViewCompat.TYPE_TOUCH);
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return hasNestedScrollingParent(ViewCompat.TYPE_TOUCH);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        return dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    // NestedScrollingParent2

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes,
                                       int type) {
        return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes,
                                       int type) {
        mParentHelper.onNestedScrollAccepted(child, target, axes, type);
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, type);
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        mParentHelper.onStopNestedScroll(target, type);
        stopNestedScroll(type);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed,
                               int dyUnconsumed, int type) {
        int remain = offsetBy(dyUnconsumed);
        dispatchNestedScroll(0, dyUnconsumed - remain, 0, remain, null,
                type);
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed,
                                  int type) {
        dispatchNestedPreScroll(dx, dy, consumed, null, type);
        int unconsumed = dy - consumed[1];
        if (unconsumed > 0) {
            consumed[1] += unconsumed - offsetBy(unconsumed);
        }
    }

    // NestedScrollingParent

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return onStartNestedScroll(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        onNestedScrollAccepted(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onStopNestedScroll(View target) {
        onStopNestedScroll(target, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed,
                               int dyUnconsumed) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        onNestedPreScroll(target, dx, dy, consumed, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        if (!consumed) {
            mViewFlinger.fling((int) velocityY);
            return true;
        }
        return false;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public int getNestedScrollAxes() {
        return mParentHelper.getNestedScrollAxes();
    }


    private boolean isPointInHeaderBounds(int x, int y) {
        QMUIViewHelper.getDescendantRect(this, mHeaderView, mTempRect);
        return mTempRect.contains(x, y);
    }

    private void ensureVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (touchSlop < 0) {
            touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        }
        final int action = ev.getAction();

        if (action == MotionEvent.ACTION_MOVE && isBeingDragged) {
            return true;
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                mViewFlinger.stop();
                isBeingDragged = false;
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();
                if (isPointInHeaderBounds(x, y)) {
                    lastMotionY = y;
                    this.activePointerId = ev.getPointerId(0);
                    startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH);
                }
                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN: {
                final int actionIndex = ev.getActionIndex();
                return actionIndex != 0 &&
                        !isPointInHeaderBounds((int) ev.getX(), (int) ev.getY())
                        && isPointInHeaderBounds((int) ev.getX(actionIndex), (int) ev.getY(actionIndex));
            }

            case MotionEvent.ACTION_MOVE: {
                final int activePointerId = this.activePointerId;
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }
                final int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex == -1) {
                    break;
                }

                final int y = (int) ev.getY(pointerIndex);
                final int yDiff = Math.abs(y - lastMotionY);
                if (yDiff > touchSlop) {
                    isBeingDragged = true;
                    lastMotionY = y;
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                isBeingDragged = false;
                this.activePointerId = INVALID_POINTER;
                stopNestedScroll(ViewCompat.TYPE_TOUCH);
                break;
            }
        }

        return isBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (touchSlop < 0) {
            touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        }

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mNestedOffsetY = 0;
        }

        final MotionEvent vtev = MotionEvent.obtain(ev);
        vtev.offsetLocation(0, mNestedOffsetY);
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                mViewFlinger.stop();
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();

                if (isPointInHeaderBounds(x, y)) {
                    lastMotionY = y;
                    activePointerId = ev.getPointerId(0);
                    ensureVelocityTracker();
                    startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH);
                } else {
                    return false;
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int activePointerIndex = ev.findPointerIndex(activePointerId);
                if (activePointerIndex == -1) {
                    return false;
                }

                final int y = (int) ev.getY(activePointerIndex);
                int dy = lastMotionY - y;

                if (!isBeingDragged && Math.abs(dy) > touchSlop) {
                    isBeingDragged = true;
                    if (dy > 0) {
                        dy -= touchSlop;
                    } else {
                        dy += touchSlop;
                    }
                }

                if (isBeingDragged) {
                    lastMotionY = y;
                    if (dy < 0 && ((IQMUIContinuousNestedBottomView) mContentView).getCurrentScroll() > 0) {
                        // the content view can scroll up, prevent drag
                        return true;
                    }
                    mScrollConsumed[0] = 0;
                    mScrollConsumed[1] = 0;
                    if (dispatchNestedPreScroll(0, dy, mScrollConsumed, mScrollOffset)) {
                        dy -= mScrollConsumed[1];
                        lastMotionY = y - mScrollOffset[1];
                        vtev.offsetLocation(0, mScrollOffset[1]);
                        mNestedOffsetY += mScrollOffset[1];
                    }
                    int unconsumed = offsetBy(dy);
                    if (dispatchNestedScroll(0, dy - unconsumed, 0, unconsumed, mScrollOffset, ViewCompat.TYPE_TOUCH)) {
                        lastMotionY = y - mScrollOffset[1];
                        vtev.offsetLocation(0, mScrollOffset[1]);
                        mNestedOffsetY += mScrollOffset[1];
                    }
                }
                break;
            }

            case MotionEvent.ACTION_UP:
                if (velocityTracker != null) {
                    velocityTracker.addMovement(vtev);
                    velocityTracker.computeCurrentVelocity(1000);
                    int yvel = -(int) (velocityTracker.getYVelocity(activePointerId) + 0.5f);
                    mViewFlinger.fling(yvel);
                }
                // $FALLTHROUGH
            case MotionEvent.ACTION_CANCEL: {
                isBeingDragged = false;
                activePointerId = INVALID_POINTER;
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                stopNestedScroll(ViewCompat.TYPE_TOUCH);
                break;
            }
        }

        if (velocityTracker != null) {
            velocityTracker.addMovement(vtev);
        }

        vtev.recycle();

        return true;
    }

    class ViewFlinger implements Runnable {
        private int mLastFlingY;
        OverScroller mOverScroller;
        Interpolator mInterpolator = QUNITIC_INTERPOLATOR;

        // When set to true, postOnAnimation callbacks are delayed until the run method completes
        private boolean mEatRunOnAnimationRequest = false;

        // Tracks if postAnimationCallback should be re-attached when it is done
        private boolean mReSchedulePostAnimationCallback = false;

        ViewFlinger() {
            mOverScroller = new OverScroller(getContext(), QUNITIC_INTERPOLATOR);
        }

        @Override
        public void run() {
            mReSchedulePostAnimationCallback = false;
            mEatRunOnAnimationRequest = true;

            // Keep a local reference so that if it is changed during onAnimation method, it won't
            // cause unexpected behaviors
            final OverScroller scroller = mOverScroller;
            if (scroller.computeScrollOffset()) {
                final int y = scroller.getCurrY();
                int unconsumedY = y - mLastFlingY;
                mLastFlingY = y;
                IQMUIContinuousNestedBottomView bottomView = (IQMUIContinuousNestedBottomView) mContentView;
                boolean canScroll = unconsumedY <= 0 || bottomView.getCurrentScroll() < bottomView.getScrollOffsetRange();
                if(canScroll){
                    if (!mChildHelper.hasNestedScrollingParent(ViewCompat.TYPE_NON_TOUCH)) {
                        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH);
                    }
                    consumeScroll(unconsumedY);
                    postOnAnimation();
                }else{
                    stop();
                }
            }

            mEatRunOnAnimationRequest = false;
            if (mReSchedulePostAnimationCallback) {
                internalPostOnAnimation();
            } else {
                stopNestedScroll(ViewCompat.TYPE_NON_TOUCH);
            }
        }

        void postOnAnimation() {
            if (mEatRunOnAnimationRequest) {
                mReSchedulePostAnimationCallback = true;
            } else {
                internalPostOnAnimation();
            }
        }

        private void internalPostOnAnimation() {
            removeCallbacks(this);
            ViewCompat.postOnAnimation(QMUIContinuousNestedBottomDelegateLayout.this, this);

        }

        public void fling(int velocityY) {
            startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH);
            mLastFlingY = 0;
            // Because you can't define a custom interpolator for flinging, we should make sure we
            // reset ourselves back to the teh default interpolator in case a different call
            // changed our interpolator.
            if (mInterpolator != QUNITIC_INTERPOLATOR) {
                mInterpolator = QUNITIC_INTERPOLATOR;
                mOverScroller = new OverScroller(getContext(), QUNITIC_INTERPOLATOR);
            }
            mOverScroller.fling(0, 0, 0, velocityY,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            postOnAnimation();
        }


        public void stop() {
            removeCallbacks(this);
            mOverScroller.abortAnimation();
        }
    }
}
