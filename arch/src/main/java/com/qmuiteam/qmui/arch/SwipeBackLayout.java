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

package com.qmuiteam.qmui.arch;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.util.QMUIViewOffsetHelper;
import com.qmuiteam.qmui.util.QMUIWindowInsetHelper;

import java.util.ArrayList;
import java.util.List;

import static com.qmuiteam.qmui.QMUIInterpolatorStaticHolder.QUNITIC_INTERPOLATOR;

/**
 * Created by cgspine on 2018/1/7.
 * <p>
 * modified from https://github.com/ikew0ng/SwipeBackLayout
 */


public class SwipeBackLayout extends FrameLayout {

    private static final int MIN_FLING_VELOCITY = 400; // dips per second
    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;
    private static final int FULL_ALPHA = 255;
    private static final float DEFAULT_SCROLL_THRESHOLD = 0.3f;
    private static final int BASE_SETTLE_DURATION = 256; // ms
    private static final int MAX_SETTLE_DURATION = 600; // ms


    public static final int DRAG_DIRECTION_NONE = 0;
    public static final int DRAG_DIRECTION_LEFT_TO_RIGHT = 1;
    public static final int DRAG_DIRECTION_RIGHT_TO_LEFT = 2;
    public static final int DRAG_DIRECTION_TOP_TO_BOTTOM = 3;
    public static final int DRAG_DIRECTION_BOTTOM_TO_TOP = 4;

    public static final int EDGE_LEFT = 1;
    public static final int EDGE_RIGHT = 2;
    public static final int EDGE_TOP = 4;
    public static final int EDGE_BOTTOM = 8;


    public static final int STATE_IDLE = 0;
    public static final int STATE_DRAGGING = 1;
    public static final int STATE_SETTLING = 2;

    public static final ViewMoveAction MOVE_VIEW_AUTO = new ViewMoveAuto();
    public static final ViewMoveAction MOVE_VIEW_LEFT_TO_RIGHT = new ViewMoveLeftToRight();
    public static final ViewMoveAction MOVE_VIEW_TOP_TO_BOTTOM = new ViewMoveTopToBottom();

    private float mScrollThreshold = DEFAULT_SCROLL_THRESHOLD;

    private View mContentView;
    private List<SwipeListener> mListeners;
    private Callback mCallback;
    private OnInsetsHandler mOnInsetsHandler;

    private Drawable mShadowLeft;
    private Drawable mShadowRight;
    private Drawable mShadowBottom;
    private Drawable mShadowTop;

    private float mScrimOpacity;
    private int mScrimColor = DEFAULT_SCRIM_COLOR;
    private VelocityTracker mVelocityTracker;
    private float mMaxVelocity;
    private float mMinVelocity;
    private OverScroller mScroller;
    private int mTouchSlop;

    private float mInitialMotionX;
    private float mInitialMotionY;
    private float mLastMotionX;
    private float mLastMotionY;

    private int mDragState = STATE_IDLE;
    private QMUIViewOffsetHelper mViewOffsetHelper;
    private ViewMoveAction mViewMoveAction = MOVE_VIEW_AUTO;

    private int mCurrentDragDirection = 0;
    private boolean mIsScrollOverValid = true;
    private boolean mEnableSwipeBack = true;


    public SwipeBackLayout(Context context) {
        this(context, null);
    }

    public SwipeBackLayout(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.SwipeBackLayoutStyle);
    }

    public SwipeBackLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwipeBackLayout, defStyle,
                R.style.SwipeBackLayout);

        int shadowLeft = a.getResourceId(R.styleable.SwipeBackLayout_shadow_left,
                R.drawable.shadow_left);
        int shadowRight = a.getResourceId(R.styleable.SwipeBackLayout_shadow_right,
                R.drawable.shadow_right);
        int shadowBottom = a.getResourceId(R.styleable.SwipeBackLayout_shadow_bottom,
                R.drawable.shadow_bottom);
        int shadowTop = a.getResourceId(R.styleable.SwipeBackLayout_shadow_top,
                R.drawable.shadow_top);
        setShadow(shadowLeft, EDGE_LEFT);
        setShadow(shadowRight, EDGE_RIGHT);
        setShadow(shadowBottom, EDGE_BOTTOM);
        setShadow(shadowTop, EDGE_TOP);
        a.recycle();
        final float density = getResources().getDisplayMetrics().density;
        final float minVel = MIN_FLING_VELOCITY * density;
        final ViewConfiguration vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop();
        mMaxVelocity = vc.getScaledMaximumFlingVelocity();
        mMinVelocity = minVel;
        mScroller = new OverScroller(context, QUNITIC_INTERPOLATOR);
        QMUIWindowInsetHelper.setOnApplyWindowInsetsListener(this, new androidx.core.view.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                int insetsType = mOnInsetsHandler != null ? mOnInsetsHandler.getInsetsType() : 0;
                if(insetsType != 0){
                    Insets toUsed = insets.getInsets(insetsType);
                    v.setPadding(toUsed.left, toUsed.top, toUsed.right, toUsed.bottom);
                }else{
                    v.setPadding(0, 0, 0, 0);
                }
                return insets;
            }
        }, false);
    }

    public void setEnableSwipeBack(boolean enableSwipeBack) {
        mEnableSwipeBack = enableSwipeBack;
    }

    public boolean isEnableSwipeBack() {
        return mEnableSwipeBack;
    }

    private final Runnable mSetIdleRunnable = new Runnable() {
        @Override
        public void run() {
            setDragState(STATE_IDLE);
        }
    };

    /**
     * Set up contentView which will be moved by user gesture
     *
     * @param view
     */
    private void setContentView(View view) {
        mContentView = view;
        mViewOffsetHelper = new QMUIViewOffsetHelper(view);
    }

    public void setViewMoveAction(@NonNull ViewMoveAction viewMoveAction) {
        mViewMoveAction = viewMoveAction;
    }

    public View getContentView() {
        return mContentView;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * Set a color to use for the scrim that obscures primary content while a
     * drawer is open.
     *
     * @param color Color to use in 0xAARRGGBB format.
     */
    public void setScrimColor(int color) {
        mScrimColor = color;
        invalidate();
    }

    /**
     * Add a callback to be invoked when a swipe event is sent to this view.
     *
     * @param listener the swipe listener to attach to this view
     */
    public ListenerRemover addSwipeListener(final SwipeListener listener) {
        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }
        mListeners.add(listener);
        return new ListenerRemover() {
            @Override
            public void remove() {
                mListeners.remove(listener);
            }
        };
    }

    /**
     * Removes a listener from the set of listeners
     *
     * @param listener
     */
    public void removeSwipeListener(SwipeListener listener) {
        if (mListeners == null) {
            return;
        }
        mListeners.remove(listener);
    }

    public void clearSwipeListeners() {
        if (mListeners == null) {
            return;
        }
        mListeners.clear();
        mListeners = null;
    }

    public void setOnInsetsHandler(OnInsetsHandler insetsHandler) {
        mOnInsetsHandler = insetsHandler;
    }

    /**
     * Set scroll threshold, we will close the activity, when scrollPercent over
     * this value
     *
     * @param threshold
     */
    public void setScrollThresHold(float threshold) {
        if (threshold >= 1.0f || threshold <= 0) {
            throw new IllegalArgumentException("Threshold value should be between 0 and 1.0");
        }
        mScrollThreshold = threshold;
    }

    /**
     * Set a drawable used for edge shadow.
     *
     * @param shadow   Drawable to use
     * @param edgeFlag Combination of edge flags describing the edge to set
     */
    public void setShadow(Drawable shadow, int edgeFlag) {
        if ((edgeFlag & EDGE_LEFT) != 0) {
            mShadowLeft = shadow;
        } else if ((edgeFlag & EDGE_RIGHT) != 0) {
            mShadowRight = shadow;
        } else if ((edgeFlag & EDGE_BOTTOM) != 0) {
            mShadowBottom = shadow;
        } else if ((edgeFlag & EDGE_TOP) != 0) {
            mShadowTop = shadow;
        }
        invalidate();
    }

    /**
     * Set a drawable used for edge shadow.
     *
     * @param resId    Resource of drawable to use
     * @param edgeFlag Combination of edge flags describing the edge to set
     * @see #EDGE_LEFT
     * @see #EDGE_RIGHT
     * @see #EDGE_BOTTOM
     */
    public void setShadow(int resId, int edgeFlag) {
        setShadow(getResources().getDrawable(resId), edgeFlag);
    }

    void setDragState(int state) {
        removeCallbacks(mSetIdleRunnable);
        if (mDragState != state) {
            mDragState = state;
            onViewDragStateChanged(mDragState);
        }
    }

    private boolean isTouchInContentView(float x, float y) {
        return x >= mContentView.getLeft() && x < mContentView.getRight()
                && y >= mContentView.getTop() && y < mContentView.getBottom();
    }


    private int selectDragDirection(float x, float y) {
        final float dx = x - mInitialMotionX;
        final float dy = y - mInitialMotionY;
        mCurrentDragDirection = mCallback == null ? DRAG_DIRECTION_NONE :
                mCallback.getDragDirection(this, mViewMoveAction, mInitialMotionX, mInitialMotionY, dx, dy, mTouchSlop);
        if(mCurrentDragDirection != DRAG_DIRECTION_NONE){
            mInitialMotionX = mLastMotionX = x;
            mInitialMotionY = mLastMotionY = y;
            onSwipeBackBegin();
            requestParentDisallowInterceptTouchEvent(true);
            setDragState(STATE_DRAGGING);
        }
        return mCurrentDragDirection;
    }

    private float getTouchMoveDelta(float x, float y) {
        if (mCurrentDragDirection == DRAG_DIRECTION_LEFT_TO_RIGHT ||
                mCurrentDragDirection == DRAG_DIRECTION_RIGHT_TO_LEFT) {
            return x - mLastMotionX;
        } else {
            return y - mLastMotionY;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(!mEnableSwipeBack){
            cancel();
            return false;
        }

        final int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            cancel();
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);


        final float x = ev.getX();
        final float y = ev.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mInitialMotionX = mLastMotionX = x;
                mInitialMotionY = mLastMotionY = y;
                if (mDragState == STATE_SETTLING) {
                    if (isTouchInContentView(x, y)) {
                        requestParentDisallowInterceptTouchEvent(true);
                        setDragState(STATE_DRAGGING);
                    }
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mDragState == STATE_IDLE) {
                    selectDragDirection(x, y);
                } else if (mDragState == STATE_DRAGGING) {
                    mViewMoveAction.move(this, mContentView, mViewOffsetHelper,
                            mCurrentDragDirection, getTouchMoveDelta(x, y));
                    onScroll();
                } else {
                    if (isTouchInContentView(x, y)) {
                        requestParentDisallowInterceptTouchEvent(true);
                        setDragState(STATE_DRAGGING);
                    }
                }
                mLastMotionX = x;
                mLastMotionY = y;
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                cancel();
                break;
            }
        }

        return mDragState == STATE_DRAGGING;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(!mEnableSwipeBack){
            cancel();
            return false;
        }

        final int action = ev.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            cancel();
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        final float x = ev.getX();
        final float y = ev.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mInitialMotionX = mLastMotionX = x;
                mInitialMotionY = mLastMotionY = y;
                if (mDragState == STATE_SETTLING) {
                    if (isTouchInContentView(x, y)) {
                        requestParentDisallowInterceptTouchEvent(true);
                        setDragState(STATE_DRAGGING);
                    }
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mDragState == STATE_IDLE) {
                    selectDragDirection(x, y);
                } else if (mDragState == STATE_DRAGGING) {
                    mViewMoveAction.move(this, mContentView, mViewOffsetHelper,
                            mCurrentDragDirection, getTouchMoveDelta(x, y));
                    onScroll();
                } else {
                    if (isTouchInContentView(x, y)) {
                        requestParentDisallowInterceptTouchEvent(true);
                        setDragState(STATE_DRAGGING);
                    }
                }
                mLastMotionX = x;
                mLastMotionY = y;
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (mDragState == STATE_DRAGGING) {
                    releaseViewForPointerUp();
                }
                cancel();
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                if (mDragState == STATE_DRAGGING) {
                    settleContentViewAt(0, 0,
                            (int) mVelocityTracker.getXVelocity(),
                            (int) mVelocityTracker.getYVelocity());
                }
                cancel();
                break;
            }
        }
        return true;
    }

    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
        final ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    private void releaseViewForPointerUp() {
        mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
        int moveEdge = mViewMoveAction.getEdge(mCurrentDragDirection);
        float v;
        if(mCurrentDragDirection == DRAG_DIRECTION_LEFT_TO_RIGHT ||
                mCurrentDragDirection == DRAG_DIRECTION_RIGHT_TO_LEFT){
            v = clampMag(mVelocityTracker.getXVelocity(), mMinVelocity, mMaxVelocity);
        }else{
            v = clampMag(mVelocityTracker.getYVelocity(), mMinVelocity, mMaxVelocity);
        }

        if (moveEdge == EDGE_LEFT || moveEdge == EDGE_RIGHT) {
            int target = mViewMoveAction.getSettleTarget(this, mContentView,
                    v, mCurrentDragDirection, mScrollThreshold);
            settleContentViewAt(target, 0, (int) v, 0);
        } else {
            int target = mViewMoveAction.getSettleTarget(this, mContentView,
                    v, mCurrentDragDirection, mScrollThreshold);
            settleContentViewAt(0, target, 0, (int) v);
        }
    }

    /**
     * Settle the captured view at the given (left, top) position.
     *
     * @param finalLeft Target left position for the captured view
     * @param finalTop  Target top position for the captured view
     * @param xvel      Horizontal velocity
     * @param yvel      Vertical velocity
     * @return true if animation should continue through {@link #continueSettling(boolean)} calls
     */
    private boolean settleContentViewAt(int finalLeft, int finalTop, int xvel, int yvel) {
        final int startLeft = mContentView.getLeft();
        final int startTop = mContentView.getTop();
        final int dx = finalLeft - startLeft;
        final int dy = finalTop - startTop;

        if (dx == 0 && dy == 0) {
            // Nothing to do. Send callbacks, be done.
            mScroller.abortAnimation();
            setDragState(STATE_IDLE);
            return false;
        }

        final int duration = computeSettleDuration(dx, dy, xvel, yvel);
        mScroller.startScroll(startLeft, startTop, dx, dy, duration);

        setDragState(STATE_SETTLING);
        invalidate();
        return true;
    }

    public boolean continueSettling(boolean deferCallbacks) {
        if (mDragState == STATE_SETTLING) {
            boolean keepGoing = mScroller.computeScrollOffset();
            final int x = mScroller.getCurrX();
            final int y = mScroller.getCurrY();
            mViewOffsetHelper.setOffset(
                    x - mViewOffsetHelper.getLayoutLeft(),
                    y - mViewOffsetHelper.getLayoutTop());
            onScroll();

            if (keepGoing && x == mScroller.getFinalX() && y == mScroller.getFinalY()) {
                // Close enough. The interpolator/scroller might think we're still moving
                // but the user sure doesn't.
                mScroller.abortAnimation();
                keepGoing = false;
            }

            if (!keepGoing) {
                if (deferCallbacks) {
                    post(mSetIdleRunnable);
                } else {
                    setDragState(STATE_IDLE);
                }
            }
        }

        return mDragState == STATE_SETTLING;
    }

    private int computeSettleDuration(int dx, int dy, int xvel, int yvel) {
        xvel = clampMag(xvel, (int) mMinVelocity, (int) mMaxVelocity);
        yvel = clampMag(yvel, (int) mMinVelocity, (int) mMaxVelocity);
        final int absDx = Math.abs(dx);
        final int absDy = Math.abs(dy);
        final int absXVel = Math.abs(xvel);
        final int absYVel = Math.abs(yvel);
        final int addedVel = absXVel + absYVel;
        final int addedDistance = absDx + absDy;

        final float xweight = xvel != 0 ? (float) absXVel / addedVel :
                (float) absDx / addedDistance;
        final float yweight = yvel != 0 ? (float) absYVel / addedVel :
                (float) absDy / addedDistance;

        int range = mViewMoveAction.getDragRange(this, mCurrentDragDirection);
        int xduration = computeAxisDuration(dx, xvel, range);
        int yduration = computeAxisDuration(dy, yvel, range);

        return (int) (xduration * xweight + yduration * yweight);
    }

    private int computeAxisDuration(int delta, int velocity, int motionRange) {
        if (delta == 0) {
            return 0;
        }

        final int width = getWidth();
        final int halfWidth = width / 2;
        final float distanceRatio = Math.min(1f, (float) Math.abs(delta) / width);
        final float distance = halfWidth + halfWidth
                * distanceInfluenceForSnapDuration(distanceRatio);

        int duration;
        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        } else if (motionRange != 0) {
            final float range = (float) Math.abs(delta) / motionRange;
            duration = (int) ((range + 1) * BASE_SETTLE_DURATION);
        } else {
            duration = BASE_SETTLE_DURATION;
        }
        return Math.min(duration, MAX_SETTLE_DURATION);
    }

    private float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * (float) Math.PI / 2.0f;
        return (float) Math.sin(f);
    }

    /**
     * Clamp the magnitude of value for absMin and absMax.
     * If the value is below the minimum, it will be clamped to zero.
     * If the value is above the maximum, it will be clamped to the maximum.
     *
     * @param value  Value to clamp
     * @param absMin Absolute value of the minimum significant value to return
     * @param absMax Absolute value of the maximum value to return
     * @return The clamped value with the same sign as <code>value</code>
     */
    private int clampMag(int value, int absMin, int absMax) {
        final int absValue = Math.abs(value);
        if (absValue < absMin) return 0;
        if (absValue > absMax) return value > 0 ? absMax : -absMax;
        return value;
    }

    /**
     * Clamp the magnitude of value for absMin and absMax.
     * If the value is below the minimum, it will be clamped to zero.
     * If the value is above the maximum, it will be clamped to the maximum.
     *
     * @param value  Value to clamp
     * @param absMin Absolute value of the minimum significant value to return
     * @param absMax Absolute value of the maximum value to return
     * @return The clamped value with the same sign as <code>value</code>
     */
    private float clampMag(float value, float absMin, float absMax) {
        final float absValue = Math.abs(value);
        if (absValue < absMin) return 0;
        if (absValue > absMax) return value > 0 ? absMax : -absMax;
        return value;
    }

    public void cancel() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mViewOffsetHelper != null) {
            mViewOffsetHelper.onViewLayout();
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        final boolean drawContent = child == mContentView;

        boolean ret = super.drawChild(canvas, child, drawingTime);
        if (mScrimOpacity > 0 && drawContent
                && mDragState != STATE_IDLE) {
            drawShadow(canvas, child);
            drawScrim(canvas, child);
        }
        return ret;
    }


    private void drawScrim(Canvas canvas, View child) {
        final int baseAlpha = (mScrimColor & 0xff000000) >>> 24;
        final int alpha = (int) (baseAlpha * mScrimOpacity);
        final int color = alpha << 24 | (mScrimColor & 0xffffff);
        int movingEdge = mViewMoveAction.getEdge(mCurrentDragDirection);
        if ((movingEdge & EDGE_LEFT) != 0) {
            canvas.clipRect(0, 0, child.getLeft(), getHeight());
        } else if ((movingEdge & EDGE_RIGHT) != 0) {
            canvas.clipRect(child.getRight(), 0, getRight(), getHeight());
        } else if ((movingEdge & EDGE_BOTTOM) != 0) {
            canvas.clipRect(0, child.getBottom(), getRight(), getHeight());
        } else if ((movingEdge & EDGE_TOP) != 0) {
            canvas.clipRect(0, 0, getRight(), child.getTop());
        }
        canvas.drawColor(color);
    }

    private void drawShadow(Canvas canvas, View child) {


        int movingEdge = mViewMoveAction.getEdge(mCurrentDragDirection);
        if ((movingEdge & EDGE_LEFT) != 0) {
            mShadowLeft.setBounds(child.getLeft() - mShadowLeft.getIntrinsicWidth(),
                    child.getTop(), child.getLeft(), child.getBottom());
            mShadowLeft.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
            mShadowLeft.draw(canvas);
        } else if ((movingEdge & EDGE_RIGHT) != 0) {
            mShadowRight.setBounds(child.getRight(), child.getTop(),
                    child.getRight() + mShadowRight.getIntrinsicWidth(), child.getBottom());
            mShadowRight.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
            mShadowRight.draw(canvas);
        } else if ((movingEdge & EDGE_BOTTOM) != 0) {
            mShadowBottom.setBounds(child.getLeft(), child.getBottom(), child.getRight(),
                    child.getBottom() + mShadowBottom.getIntrinsicHeight());
            mShadowBottom.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
            mShadowBottom.draw(canvas);
        } else if ((movingEdge & EDGE_TOP) != 0) {
            mShadowTop.setBounds(child.getLeft(), child.getTop() - mShadowTop.getIntrinsicHeight(),
                    child.getRight(), child.getTop());
            mShadowTop.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
            mShadowTop.draw(canvas);
        }
    }

    @Override
    public void computeScroll() {
        if (continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void onSwipeBackBegin() {
        mIsScrollOverValid = true;
        mScrimOpacity = 1 - mViewMoveAction.getCurrentPercent(this, mContentView, mCurrentDragDirection);
        if (mListeners != null && !mListeners.isEmpty()) {
            for (SwipeListener listener : mListeners) {
                listener.onSwipeBackBegin(mCurrentDragDirection, mViewMoveAction.getEdge(mCurrentDragDirection));
            }
        }
        invalidate();
    }

    private void onScroll() {
        float scrollPercent = mViewMoveAction.getCurrentPercent(this, mContentView, mCurrentDragDirection);
        mScrimOpacity = 1 - mViewMoveAction.getCurrentPercent(this, mContentView, mCurrentDragDirection);
        if (scrollPercent < mScrollThreshold && !mIsScrollOverValid) {
            mIsScrollOverValid = true;
        }
        if (mDragState == STATE_DRAGGING && mIsScrollOverValid &&
                scrollPercent >= mScrollThreshold) {
            mIsScrollOverValid = false;
            onScrollOverThreshold();
        }
        if (mListeners != null && !mListeners.isEmpty()) {
            for (SwipeListener listener : mListeners) {
                listener.onScroll(mCurrentDragDirection, mViewMoveAction.getEdge(mCurrentDragDirection), scrollPercent);
            }
        }
        invalidate();
    }

    private void onScrollOverThreshold() {
        if (mListeners != null && !mListeners.isEmpty()) {
            for (SwipeListener listener : mListeners) {
                listener.onScrollOverThreshold();
            }
        }
    }

    private void onViewDragStateChanged(int dragState) {
        if (mListeners != null && !mListeners.isEmpty()) {
            for (SwipeListener listener : mListeners) {
                listener.onScrollStateChange(dragState,
                        mViewMoveAction.getCurrentPercent(this, mContentView, mCurrentDragDirection));
            }
        }
    }

    public void resetOffset(){
        if(mViewOffsetHelper != null){
            mViewOffsetHelper.setOffset(0, 0);
        }
    }

    public static SwipeBackLayout wrap(View child, ViewMoveAction viewMoveAction, Callback callback) {
        SwipeBackLayout wrapper = new SwipeBackLayout(child.getContext());
        wrapper.addView(child, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        wrapper.setContentView(child);
        wrapper.setViewMoveAction(viewMoveAction);
        wrapper.setCallback(callback);
        return wrapper;
    }

    public static SwipeBackLayout wrap(Context context, int childRes, ViewMoveAction viewMoveAction, Callback callback) {
        SwipeBackLayout wrapper = new SwipeBackLayout(context);
        View child = LayoutInflater.from(context).inflate(childRes, wrapper, false);
        wrapper.addView(child, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        wrapper.setContentView(child);
        wrapper.setCallback(callback);
        wrapper.setViewMoveAction(viewMoveAction);
        return wrapper;
    }

    public static void translateInSwipeBack(View view, int edgeFlag, int targetOffset){
        if (edgeFlag == EDGE_BOTTOM) {
            view.setTranslationY(targetOffset);
            view.setTranslationX(0);
        } else if (edgeFlag == EDGE_RIGHT) {
            view.setTranslationY(0);
            view.setTranslationX(targetOffset);
        } else if(edgeFlag == EDGE_LEFT){
            view.setTranslationY(0);
            view.setTranslationX(-targetOffset);
        }else{
            view.setTranslationY(-targetOffset);
            view.setTranslationX(0);
        }
    }

    public float getXFraction() {
        int width = getWidth();
        if(width == 0){
            ViewParent parent = getParent();
            if(parent instanceof ViewGroup){
                width = ((ViewGroup)parent).getWidth();
            }
        }
        return (width == 0) ? 0 : getX() / (float) width;
    }

    public void setXFraction(float xFraction) {
        int width = getWidth();
        if(width == 0){
            ViewParent parent = getParent();
            if(parent instanceof ViewGroup){
                width = ((ViewGroup)parent).getWidth();
            }
        }
        setX((width > 0) ? (xFraction * width) : 0);
    }

    public float getYFraction() {
        int height = getHeight();
        if(height == 0){
            ViewParent parent = getParent();
            if(parent instanceof ViewGroup){
                height = ((ViewGroup)parent).getHeight();
            }
        }
        return (height == 0) ? 0 : getY() / (float) height;
    }

    public void setYFraction(float yFraction) {
        int height = getHeight();
        if(height == 0){
            ViewParent parent = getParent();
            if(parent instanceof ViewGroup){
                height = ((ViewGroup)parent).getHeight();
            }
        }
        setY((height > 0) ? (yFraction * height) : 0);
    }

    public interface Callback {
        int getDragDirection(SwipeBackLayout swipeBackLayout, ViewMoveAction moveAction,
                             float downX, float downY, float dx, float dy, float touchSlop);
    }

    public interface ViewMoveAction {
        float getCurrentPercent(@NonNull SwipeBackLayout swipeBackLayout,
                                @NonNull View contentView, int dragDirection);

        int getDragRange(@NonNull SwipeBackLayout swipeBackLayout, int dragDirection);

        int getSettleTarget(@NonNull SwipeBackLayout swipeBackLayout,
                            @NonNull View contentView,
                            float v, int dragDirection, float scrollThreshold);

        int getEdge(int dragDirection);

        void move(@NonNull SwipeBackLayout swipeBackLayout,
                  @NonNull View contentView,
                  @NonNull QMUIViewOffsetHelper offsetHelper,
                  int dragDirection, float delta);
    }

    public interface ListenerRemover {
        void remove();
    }

    public interface SwipeListener {
        /**
         * Invoke when state change
         *
         * @param state         flag to describe scroll state
         * @param scrollPercent scroll percent of this view
         * @see #STATE_IDLE
         * @see #STATE_DRAGGING
         * @see #STATE_SETTLING
         */
        void onScrollStateChange(int state, float scrollPercent);

        /**
         * Invoke when scrolling
         *
         * @param moveEdge      flag to describe edge
         * @param scrollPercent scroll percent of this view
         */
        void onScroll(int dragDirection, int moveEdge, float scrollPercent);

        /**
         * Invoke when swipe back begin.
         */
        void onSwipeBackBegin(int dragDirection, int moveEdge);

        /**
         * Invoke when scroll percent over the threshold for the first time
         */
        void onScrollOverThreshold();
    }

    public interface OnInsetsHandler {
        @WindowInsetsCompat.Type.InsetsType
        int getInsetsType();
    }

    public static class ViewMoveAuto implements ViewMoveAction {

        private boolean isHor(int dragDirection){
            return dragDirection == DRAG_DIRECTION_RIGHT_TO_LEFT ||
                    dragDirection == DRAG_DIRECTION_LEFT_TO_RIGHT;
        }

        @Override
        public float getCurrentPercent(@NonNull SwipeBackLayout swipeBackLayout,
                                       @NonNull View contentView, int dragDirection) {
            float percent;
            if(isHor(dragDirection)){
                percent = Math.abs(contentView.getLeft() * 1f / swipeBackLayout.getWidth());
            }else{
                percent = Math.abs(contentView.getTop() * 1f / swipeBackLayout.getHeight());
            }
            return QMUILangHelper.constrain(percent, 0f, 1f);
        }

        @Override
        public int getDragRange(@NonNull SwipeBackLayout swipeBackLayout, int dragDirection) {
            if(isHor(dragDirection)){
                return swipeBackLayout.getWidth();
            }
            return swipeBackLayout.getHeight();
        }

        @Override
        public int getSettleTarget(@NonNull SwipeBackLayout swipeBackLayout,
                                   @NonNull View contentView,
                                   float v, int dragDirection, float scrollThreshold) {
            if(dragDirection == DRAG_DIRECTION_LEFT_TO_RIGHT){
                if (v > 0 ||
                        (v == 0 && getCurrentPercent(swipeBackLayout, contentView, dragDirection) > scrollThreshold)) {
                    return swipeBackLayout.getWidth();
                }
            }else if(dragDirection == DRAG_DIRECTION_RIGHT_TO_LEFT){
                if (v < 0 ||
                        (v == 0 && getCurrentPercent(swipeBackLayout, contentView, dragDirection) > scrollThreshold)) {
                    return -swipeBackLayout.getWidth();
                }
            }else if(dragDirection == DRAG_DIRECTION_TOP_TO_BOTTOM){
                if (v > 0 ||
                        (v == 0 && getCurrentPercent(swipeBackLayout, contentView, dragDirection) > scrollThreshold)) {
                    return swipeBackLayout.getHeight();
                }
            }else{
                if (v < 0 ||
                        (v == 0 && getCurrentPercent(swipeBackLayout, contentView, dragDirection) > scrollThreshold)) {
                    return -swipeBackLayout.getHeight();
                }
            }

            return 0;
        }

        @Override
        public int getEdge(int dragDirection) {
            if(dragDirection == DRAG_DIRECTION_LEFT_TO_RIGHT){
                return EDGE_LEFT;
            }else if(dragDirection == DRAG_DIRECTION_RIGHT_TO_LEFT){
                return EDGE_RIGHT;
            }else if(dragDirection == DRAG_DIRECTION_TOP_TO_BOTTOM){
                return EDGE_TOP;
            }else{
                return EDGE_BOTTOM;
            }
        }

        @Override
        public void move(@NonNull SwipeBackLayout swipeBackLayout,
                         @NonNull View contentView,
                         @NonNull QMUIViewOffsetHelper offsetHelper,
                         int dragDirection, float delta) {
            if(dragDirection == DRAG_DIRECTION_LEFT_TO_RIGHT){
                int target = (int) (offsetHelper.getLeftAndRightOffset() + delta);
                target = QMUILangHelper.constrain(target, 0, swipeBackLayout.getWidth());
                offsetHelper.setLeftAndRightOffset(target);
            }else if(dragDirection == DRAG_DIRECTION_RIGHT_TO_LEFT){
                int target = (int) (offsetHelper.getLeftAndRightOffset() + delta);
                target = QMUILangHelper.constrain(target, -swipeBackLayout.getWidth(),0);
                offsetHelper.setLeftAndRightOffset(target);
            }else if(dragDirection == DRAG_DIRECTION_TOP_TO_BOTTOM){
                int target = (int) (offsetHelper.getTopAndBottomOffset() + delta);
                target = QMUILangHelper.constrain(target, 0, swipeBackLayout.getHeight());
                offsetHelper.setTopAndBottomOffset(target);
            }else{
                int target = (int) (offsetHelper.getTopAndBottomOffset() + delta);
                target = QMUILangHelper.constrain(target, -swipeBackLayout.getHeight(),0);
                offsetHelper.setTopAndBottomOffset(target);
            }

        }
    }

    public static class ViewMoveLeftToRight implements ViewMoveAction {

        @Override
        public float getCurrentPercent(@NonNull SwipeBackLayout swipeBackLayout,
                                       @NonNull View contentView, int dragDirection) {
            return QMUILangHelper.constrain(
                    contentView.getLeft() * 1f / swipeBackLayout.getWidth(), 0f, 1f);
        }

        @Override
        public int getDragRange(@NonNull SwipeBackLayout swipeBackLayout, int dragDirection) {
            return swipeBackLayout.getWidth();
        }

        @Override
        public int getSettleTarget(@NonNull SwipeBackLayout swipeBackLayout,
                                   @NonNull View contentView,
                                   float v, int dragDirection, float scrollThreshold) {
            if (v > 0 ||
                    (v == 0 && getCurrentPercent(swipeBackLayout, contentView, dragDirection) > scrollThreshold)) {
                return swipeBackLayout.getWidth();
            }
            return 0;
        }

        @Override
        public int getEdge(int dragDirection) {
            return EDGE_LEFT;
        }

        @Override
        public void move(@NonNull SwipeBackLayout swipeBackLayout,
                         @NonNull View contentView,
                         @NonNull QMUIViewOffsetHelper offsetHelper, int dragDirection, float delta) {
            if (dragDirection == DRAG_DIRECTION_BOTTOM_TO_TOP ||
                    dragDirection == DRAG_DIRECTION_TOP_TO_BOTTOM) {
                delta = delta * swipeBackLayout.getWidth() / swipeBackLayout.getHeight();
            }
            int target = (int) (offsetHelper.getLeftAndRightOffset() + delta);
            target = QMUILangHelper.constrain(target, 0, swipeBackLayout.getWidth());
            offsetHelper.setLeftAndRightOffset(target);
        }
    }

    public static class ViewMoveTopToBottom implements ViewMoveAction {

        @Override
        public float getCurrentPercent(@NonNull SwipeBackLayout swipeBackLayout,
                                       @NonNull View contentView, int dragDirection) {
            return QMUILangHelper.constrain(
                    contentView.getTop() * 1f / swipeBackLayout.getHeight(), 0f, 1f);
        }

        @Override
        public int getDragRange(@NonNull SwipeBackLayout swipeBackLayout, int dragDirection) {
            return swipeBackLayout.getHeight();
        }

        @Override
        public int getSettleTarget(@NonNull SwipeBackLayout swipeBackLayout,
                                   @NonNull View contentView,
                                   float v, int dragDirection, float scrollThreshold) {
            if (v > 0 ||
                    (v == 0 && getCurrentPercent(swipeBackLayout, contentView, dragDirection) > scrollThreshold)) {
                return swipeBackLayout.getHeight();
            }
            return 0;
        }

        @Override
        public int getEdge(int dragDirection) {
            return EDGE_TOP;
        }

        @Override
        public void move(@NonNull SwipeBackLayout swipeBackLayout,
                         @NonNull View contentView,
                         @NonNull QMUIViewOffsetHelper offsetHelper, int dragDirection, float delta) {
            if (dragDirection == DRAG_DIRECTION_LEFT_TO_RIGHT ||
                    dragDirection == DRAG_DIRECTION_RIGHT_TO_LEFT) {
                delta = delta * swipeBackLayout.getHeight() / swipeBackLayout.getWidth();
            }
            int target = (int) (offsetHelper.getTopAndBottomOffset() + delta);
            target = QMUILangHelper.constrain(target, 0, swipeBackLayout.getHeight());
            offsetHelper.setTopAndBottomOffset(target);
        }
    }
}
