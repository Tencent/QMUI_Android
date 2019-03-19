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
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import static com.qmuiteam.qmui.QMUIInterpolatorStaticHolder.QUNITIC_INTERPOLATOR;

public class QMUIContinuousNestedTopAreaBehavior extends QMUIViewOffsetBehavior<View> {

    private static final int INVALID_POINTER = -1;

    private final ViewFlinger mViewFlinger;
    private final int[] mScrollConsumed = new int[2];

    private boolean isBeingDragged;
    private int activePointerId = INVALID_POINTER;
    private int lastMotionY;
    private int touchSlop = -1;
    private VelocityTracker velocityTracker;
    private Callback mCallback;

    public QMUIContinuousNestedTopAreaBehavior(Context context) {
        this(context, null);
    }


    public QMUIContinuousNestedTopAreaBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mViewFlinger = new ViewFlinger(context);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent,
                                         @NonNull View child, @NonNull MotionEvent ev) {
        if (touchSlop < 0) {
            touchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
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
                if (parent.isPointInChildBounds(child, x, y)) {
                    lastMotionY = y;
                    this.activePointerId = ev.getPointerId(0);
                    ensureVelocityTracker();
                }
                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN: {
                final int actionIndex = ev.getActionIndex();
                return actionIndex != 0 &&
                        !parent.isPointInChildBounds(child, (int) ev.getX(), (int) ev.getY())
                        && parent.isPointInChildBounds(
                        child, (int) ev.getX(actionIndex), (int) ev.getY(actionIndex));
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
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                break;
            }
        }

        if (velocityTracker != null) {
            velocityTracker.addMovement(ev);
        }

        return isBeingDragged;
    }

    @Override
    public boolean onTouchEvent(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull MotionEvent ev) {
        if (touchSlop < 0) {
            touchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
        }
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                mViewFlinger.stop();
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();

                if (parent.isPointInChildBounds(child, x, y)) {
                    lastMotionY = y;
                    activePointerId = ev.getPointerId(0);
                    ensureVelocityTracker();
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
                    scroll(parent, child, dy);
                }
                break;
            }

            case MotionEvent.ACTION_UP:
                if (velocityTracker != null) {
                    velocityTracker.addMovement(ev);
                    velocityTracker.computeCurrentVelocity(1000);
                    int yvel = -(int) (velocityTracker.getYVelocity(activePointerId) + 0.5f);
                    mViewFlinger.fling(parent, child, yvel);
                }
                // $FALLTHROUGH
            case MotionEvent.ACTION_CANCEL: {
                isBeingDragged = false;
                activePointerId = INVALID_POINTER;
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                break;
            }
        }

        if (velocityTracker != null) {
            velocityTracker.addMovement(ev);
        }

        return true;
    }

    private void scroll(@NonNull CoordinatorLayout parent, @NonNull View child, int dy) {
        mScrollConsumed[0] = 0;
        mScrollConsumed[1] = 0;
        onNestedPreScroll(parent, child, child, 0, dy, mScrollConsumed, ViewCompat.TYPE_TOUCH);
        int unConsumed = dy - mScrollConsumed[1];
        if (child instanceof IQMUIContinuousNestedTopView) {
            unConsumed = ((IQMUIContinuousNestedTopView) child).consumeScroll(unConsumed);
        }
        onNestedScroll(parent, child, child, 0, dy - unConsumed,
                0, unConsumed, ViewCompat.TYPE_TOUCH, mScrollConsumed);

    }

    private void ensureVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
    }

    @Override
    public boolean onMeasureChild(@NonNull CoordinatorLayout parent, @NonNull View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        final int childLpHeight = child.getLayoutParams().height;
        if (childLpHeight == ViewGroup.LayoutParams.MATCH_PARENT) {
            int availableHeight = View.MeasureSpec.getSize(parentHeightMeasureSpec);
            if (availableHeight == 0) {
                // If the measure spec doesn't specify a size, use the current height
                availableHeight = parent.getHeight();
            }
            final int heightMeasureSpec =
                    View.MeasureSpec.makeMeasureSpec(availableHeight, View.MeasureSpec.AT_MOST);

            parent.onMeasureChild(
                    child, parentWidthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);

            return true;
        }
        return false;
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout parent, @NonNull View child,
                                  @NonNull View target, int dx, int dy,
                                  @NonNull int[] consumed, int type) {
        if (target == child) {
            // both target view and child view is top view
            if (dy < 0) {
                View bottomView = findBottomView(parent);
                if (bottomView != null) {
                    if (child.getTop() <= dy) {
                        setTopAndBottomOffset(child.getTop() - dy - getLayoutTop());
                        consumed[1] += dy;
                    } else if (child.getTop() < 0) {
                        int top = child.getTop();
                        setTopAndBottomOffset(0 - getLayoutTop());
                        consumed[1] += top;
                    }
                }
            }
        } else {
            if (dy > 0) {
                // child is topView, target is bottomView
                if (target instanceof IQMUIContinuousNestedBottomView) {
                    int contentHeight = ((IQMUIContinuousNestedBottomView) target).getContentHeight();
                    int minOffset = -child.getHeight();
                    if (contentHeight != IQMUIContinuousNestedBottomView.HEIGHT_IS_ENOUGH_TO_SCROLL) {
                        minOffset = parent.getHeight() - contentHeight - child.getHeight();
                    }
                    if (child.getTop() - dy >= minOffset)
                        if (child.getTop() - dy >= minOffset) {
                            setTopAndBottomOffset(child.getTop() - dy - getLayoutTop());
                            consumed[1] += dy;
                        } else if (child.getTop() > minOffset) {
                            int distance = child.getTop() - minOffset;
                            setTopAndBottomOffset(minOffset);
                            consumed[1] += distance;
                        }
                }
            }
        }
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout parent, @NonNull View child,
                               @NonNull View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed,
                               int type, @NonNull int[] consumed) {
        if (target == child) {
            // both target view and child view is top view
            if (dyUnconsumed > 0) {
                View bottomView = findBottomView(parent);
                if (bottomView != null) {
                    int contentHeight = ((IQMUIContinuousNestedBottomView) bottomView).getContentHeight();
                    int minBottom = parent.getHeight();
                    boolean canContentScroll = true;
                    if (contentHeight != IQMUIContinuousNestedBottomView.HEIGHT_IS_ENOUGH_TO_SCROLL) {
                        minBottom = parent.getHeight() + parent.getHeight() - contentHeight;
                        canContentScroll = false;
                    }
                    if (bottomView.getBottom() - minBottom > dyUnconsumed) {
                        setTopAndBottomOffset(target.getTop() - dyUnconsumed - getLayoutTop());
                    } else if (bottomView.getBottom() - minBottom > 0) {
                        int moveDistance = bottomView.getBottom() - minBottom;
                        setTopAndBottomOffset(target.getTop() - moveDistance - getLayoutTop());
                        if (canContentScroll) {
                            ((IQMUIContinuousNestedBottomView) bottomView).consumeScroll(dyUnconsumed - moveDistance);
                        }
                    } else if (canContentScroll) {
                        ((IQMUIContinuousNestedBottomView) bottomView).consumeScroll(dyUnconsumed);
                    }
                }
            }
        } else {
            // child is topView, target is bottomView
            if (dyUnconsumed < 0) {
                if (child.getTop() <= dyUnconsumed) {
                    setTopAndBottomOffset(child.getTop() - dyUnconsumed - getLayoutTop());
                    consumed[1] += dyUnconsumed;
                } else if (child.getTop() < 0) {
                    int top = child.getTop();
                    setTopAndBottomOffset(0 - getLayoutTop());
                    int innerUnConsumed = ((IQMUIContinuousNestedTopView) child).consumeScroll(dyUnconsumed - top);
                    consumed[1] += dyUnconsumed - innerUnConsumed;
                } else if (child instanceof IQMUIContinuousNestedTopView) {
                    consumed[1] += dyUnconsumed - ((IQMUIContinuousNestedTopView) child).consumeScroll(dyUnconsumed);
                }
            }
        }
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull View child, @NonNull View directTargetChild,
                                       @NonNull View target, int axes, int type) {
        return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    private View findBottomView(CoordinatorLayout parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof IQMUIContinuousNestedBottomView) {
                return child;
            }
        }
        return null;
    }


    class ViewFlinger implements Runnable {
        private int mLastFlingY;
        OverScroller mOverScroller;
        Interpolator mInterpolator = QUNITIC_INTERPOLATOR;

        // When set to true, postOnAnimation callbacks are delayed until the run method completes
        private boolean mEatRunOnAnimationRequest = false;

        // Tracks if postAnimationCallback should be re-attached when it is done
        private boolean mReSchedulePostAnimationCallback = false;

        private CoordinatorLayout mCurrentParent;
        private View mCurrentChild;

        ViewFlinger(Context context) {
            mOverScroller = new OverScroller(context, QUNITIC_INTERPOLATOR);
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
                if (mCurrentParent != null && mCurrentChild != null) {
                    scroll(mCurrentParent, mCurrentChild, unconsumedY);
                    postOnAnimation();
                }
            }

            mEatRunOnAnimationRequest = false;
            if (mReSchedulePostAnimationCallback) {
                internalPostOnAnimation();
            } else {
                mCurrentChild = null;
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
            if (mCurrentChild != null) {
                mCurrentParent.removeCallbacks(this);
                ViewCompat.postOnAnimation(mCurrentChild, this);
            }

        }

        public void fling(CoordinatorLayout parent, View child, int velocityY) {
            mCurrentParent = parent;
            mCurrentChild = child;
            mLastFlingY = 0;
            // Because you can't define a custom interpolator for flinging, we should make sure we
            // reset ourselves back to the teh default interpolator in case a different call
            // changed our interpolator.
            if (mInterpolator != QUNITIC_INTERPOLATOR) {
                mInterpolator = QUNITIC_INTERPOLATOR;
                mOverScroller = new OverScroller(mCurrentParent.getContext(), QUNITIC_INTERPOLATOR);
            }
            mOverScroller.fling(0, 0, 0, velocityY,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            postOnAnimation();
        }


        public void stop() {
            if (mCurrentChild != null) {
                mCurrentChild.removeCallbacks(this);
            }
            mOverScroller.abortAnimation();
            mCurrentChild = null;
            mCurrentParent = null;
        }
    }

    @Override
    public boolean setTopAndBottomOffset(int offset) {
        boolean ret = super.setTopAndBottomOffset(offset);
        if (mCallback != null) {
            mCallback.onTopAreaOffset(offset);
        }
        return ret;
    }

    public interface Callback {
        void onTopAreaOffset(int offset);
    }
}