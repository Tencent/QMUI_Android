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
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.widget.OverScroller;

import com.qmuiteam.qmui.widget.webview.QMUIWebView;

import androidx.core.view.NestedScrollingChild;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.ViewCompat;

import static androidx.customview.widget.ViewDragHelper.INVALID_POINTER;
import static com.qmuiteam.qmui.QMUIInterpolatorStaticHolder.QUNITIC_INTERPOLATOR;

public class QMUINestedWebView extends QMUIWebView implements NestedScrollingChild {
    private static final String TAG = "QMUINestedWebView";
    static final int MAX_SCROLL_DURATION = 2000;
    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_SETTLING = 2;

    private int mScrollState = SCROLL_STATE_IDLE;
    private int mScrollPointerId = INVALID_POINTER;
    private VelocityTracker mVelocityTracker;
    private int mLastTouchY;
    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private NestedScrollingChildHelper mChildHelper;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;
    final ViewFlinger mViewFlinger = new ViewFlinger();
    private Runnable mCheckFling;

    public QMUINestedWebView(Context context) {
        super(context);
        init(context);
    }

    public QMUINestedWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public QMUINestedWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        final ViewConfiguration vc = ViewConfiguration.get(context);
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        setOverScrollMode(OVER_SCROLL_NEVER);
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean returnValue = false;

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        boolean eventAddedToVelocityTracker = false;

        final MotionEvent vtev = MotionEvent.obtain(e);
        final int action = e.getActionMasked();
        final int actionIndex = e.getActionIndex();

        if (action == MotionEvent.ACTION_DOWN) {
            flingScroll(0, 0);
            if (mCheckFling != null) {
                removeCallbacks(mCheckFling);
                mCheckFling = null;
            }
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mScrollPointerId = e.getPointerId(0);
                mLastTouchY = (int) (e.getY() + 0.5f);
                returnValue = super.onTouchEvent(e);
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mScrollPointerId = e.getPointerId(actionIndex);
                mLastTouchY = (int) (e.getY(actionIndex) + 0.5f);
                break;
            case MotionEvent.ACTION_MOVE:
                final int index = e.findPointerIndex(mScrollPointerId);
                if (index < 0) {
                    Log.e(TAG, "Error processing scroll; pointer index for id "
                            + mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                    return false;
                }
                final int y = (int) (e.getY(index) + 0.5f);
                int dy = mLastTouchY - y;
                mScrollConsumed[0] = 0;
                mScrollConsumed[1] = 0;
                if (dispatchNestedPreScroll(0, dy, mScrollConsumed, mScrollOffset)) {
                    dy -= mScrollConsumed[1];
                    Log.i(TAG, "mScrollOffset = " + mScrollOffset[1]);
                    vtev.offsetLocation(0, -mScrollOffset[1]);
                }
                // mLastTouchY record is always relative to QMUINestedWehView
                mLastTouchY = y - mScrollOffset[1];
                if (mScrollState != SCROLL_STATE_DRAGGING) {
                    setScrollState(SCROLL_STATE_DRAGGING);
                }
                if (mScrollState == SCROLL_STATE_DRAGGING) {
                    int oldY = getSafeScrollY();
                    Log.i(TAG, "vtev:" + vtev.getY());
                    returnValue = super.onTouchEvent(vtev);
                    int newScrollY = oldY;
                    if (dy > 0) {
                        newScrollY = Math.min(computeVerticalScrollRange() - getHeight(), oldY + dy);
                    } else if (dy < 0) {
                        newScrollY = Math.max(0, oldY + dy);
                    }
                    int consumed = newScrollY - oldY;
                    if (dispatchNestedScroll(0, consumed, 0,
                            dy - consumed, mScrollOffset)) {
                        vtev.offsetLocation(0, -mScrollOffset[1]);
                        mLastTouchY -= mScrollOffset[1];
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onPointerUp(e);
                break;
            case MotionEvent.ACTION_UP:
                returnValue = super.onTouchEvent(vtev);
                // cancel webview default fling
                flingScroll(0, 0);
                mVelocityTracker.addMovement(vtev);
                eventAddedToVelocityTracker = true;
                mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
                final float yvel = -mVelocityTracker.getYVelocity(mScrollPointerId);
                // must post, because stopNestedScroll will be invoked after onTouchEvent in dispatchEvent
                mCheckFling = new Runnable() {
                    @Override
                    public void run() {
                        mCheckFling = null;
                        if (!(yvel != 0 && fling((int) yvel))) {
                            setScrollState(SCROLL_STATE_IDLE);
                        }
                    }
                };
                ViewCompat.postOnAnimation(this, mCheckFling);
                mVelocityTracker.clear();
                break;
            case MotionEvent.ACTION_CANCEL:
                returnValue = super.onTouchEvent(vtev);
                stopNestedScroll();
                mVelocityTracker.clear();
                break;
        }
        if (!eventAddedToVelocityTracker) {
            mVelocityTracker.addMovement(vtev);
        }
        vtev.recycle();
        return returnValue;
    }

    private void onPointerUp(MotionEvent e) {
        final int actionIndex = e.getActionIndex();
        if (e.getPointerId(actionIndex) == mScrollPointerId) {
            // Pick a new pointer to pick up the slack.
            final int newIndex = actionIndex == 0 ? 1 : 0;
            mScrollPointerId = e.getPointerId(newIndex);
            mLastTouchY = (int) (e.getY(newIndex) + 0.5f);
        }
    }

    public boolean fling(int velocityY) {
        Log.i(TAG, "fling, v = " + velocityY);
        if (Math.abs(velocityY) < mMinFlingVelocity) {
            return false;
        }

        if (!dispatchNestedPreFling(0, velocityY)) {
            boolean canScroll;
            if (velocityY > 0) {
                canScroll = getSafeScrollY() < computeVerticalScrollRange() - getHeight();
            } else {
                canScroll = getSafeScrollY() >= 0;
            }
            dispatchNestedFling(0, velocityY, canScroll);

            startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);

            velocityY = Math.max(-mMaxFlingVelocity, Math.min(velocityY, mMaxFlingVelocity));
            mViewFlinger.fling(velocityY);
            return true;
        }
        return false;
    }

    void setScrollState(int state) {
        if (state == mScrollState) {
            return;
        }
        if (state != SCROLL_STATE_SETTLING) {
            mViewFlinger.stop();
        }
        mScrollState = state;
    }


    // Nested Scroll implements
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
        Log.i(TAG, "startNestedScroll");
        return mChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        Log.i(TAG, "stopNestedScroll");
        mChildHelper.stopNestedScroll();
        mViewFlinger.stop();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed,
                                        int[] offsetInWindow) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    /**
     * the result of getScrollY() may be larger than scrolling range
     *
     * @return
     */
    protected int getSafeScrollY() {
        int scrollY = getScrollY();
        int range = computeVerticalScrollRange();
        return Math.max(0, Math.min(scrollY, range - getHeight()));
    }

    @Override
    public void scrollTo(int x, int y) {
        if (x < 0) {
            x = 0;
        } else {
            int maxScrollX = computeHorizontalScrollRange() - getWidth();
            if (x > maxScrollX) {
                x = maxScrollX;
            }
        }

        if (y < 0) {
            y = 0;
        } else {
            int maxScrollY = computeVerticalScrollRange() - getHeight();
            if (y > maxScrollY) {
                y = maxScrollY;
            }
        }
        super.scrollTo(x, y);
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
                int consumedX = 0;
                int consumedY = 0;

                // Nested Pre Scroll
                mScrollConsumed[0] = 0;
                mScrollConsumed[1] = 0;
                Log.i(TAG, "unconsumedY = " + unconsumedY + ";1");
                if (dispatchNestedPreScroll(0, unconsumedY, mScrollConsumed, null)) {
                    unconsumedY -= mScrollConsumed[1];
                }
                if (unconsumedY != 0) {
                    Log.i(TAG, "unconsumedY = " + unconsumedY + ";2");
                    int oldY = getSafeScrollY();
                    scrollBy(0, unconsumedY);
                    int newY = getSafeScrollY();
                    consumedY = newY - oldY;
                    unconsumedY = unconsumedY - consumedY;
                    dispatchNestedScroll(consumedX, consumedY, 0, unconsumedY, null);
                }

                postOnAnimation();
            }

            mEatRunOnAnimationRequest = false;
            if (mReSchedulePostAnimationCallback) {
                internalPostOnAnimation();
            } else {
                setScrollState(SCROLL_STATE_IDLE);
                stopNestedScroll();
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
            ViewCompat.postOnAnimation(QMUINestedWebView.this, this);
        }

        public void fling(int velocityY) {
            setScrollState(SCROLL_STATE_SETTLING);
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
            flingScroll(0, 0);
        }

        public void smoothScrollBy(int dx, int dy) {
            smoothScrollBy(dx, dy, 0, 0);
        }

        public void smoothScrollBy(int dx, int dy, int vx, int vy) {
            smoothScrollBy(dx, dy, computeScrollDuration(dx, dy, vx, vy));
        }

        private float distanceInfluenceForSnapDuration(float f) {
            f -= 0.5f; // center the values about 0.
            f *= 0.3f * (float) Math.PI / 2.0f;
            return (float) Math.sin(f);
        }

        private int computeScrollDuration(int dx, int dy, int vx, int vy) {
            final int absDx = Math.abs(dx);
            final int absDy = Math.abs(dy);
            final boolean horizontal = absDx > absDy;
            final int velocity = (int) Math.sqrt(vx * vx + vy * vy);
            final int delta = (int) Math.sqrt(dx * dx + dy * dy);
            final int containerSize = horizontal ? getWidth() : getHeight();
            final int halfContainerSize = containerSize / 2;
            final float distanceRatio = Math.min(1.f, 1.f * delta / containerSize);
            final float distance = halfContainerSize + halfContainerSize
                    * distanceInfluenceForSnapDuration(distanceRatio);

            final int duration;
            if (velocity > 0) {
                duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
            } else {
                float absDelta = (float) (horizontal ? absDx : absDy);
                duration = (int) (((absDelta / containerSize) + 1) * 300);
            }
            return Math.min(duration, MAX_SCROLL_DURATION);
        }

        public void smoothScrollBy(int dx, int dy, int duration) {
            smoothScrollBy(dx, dy, duration, QUNITIC_INTERPOLATOR);
        }

        public void smoothScrollBy(int dx, int dy, Interpolator interpolator) {
            smoothScrollBy(dx, dy, computeScrollDuration(dx, dy, 0, 0),
                    interpolator == null ? QUNITIC_INTERPOLATOR : interpolator);
        }

        public void smoothScrollBy(int dx, int dy, int duration, Interpolator interpolator) {
            if (mInterpolator != interpolator) {
                mInterpolator = interpolator;
                mOverScroller = new OverScroller(getContext(), interpolator);
            }
            setScrollState(SCROLL_STATE_SETTLING);
            mLastFlingY = 0;
            mOverScroller.startScroll(0, 0, dx, dy, duration);
            if (Build.VERSION.SDK_INT < 23) {
                // b/64931938 before API 23, startScroll() does not reset getCurX()/getCurY()
                // to start values, which causes fillRemainingScrollValues() put in obsolete values
                // for LayoutManager.onLayoutChildren().
                mOverScroller.computeScrollOffset();
            }
            postOnAnimation();
        }

        public void stop() {
            removeCallbacks(this);
            mOverScroller.abortAnimation();
        }

    }
}
