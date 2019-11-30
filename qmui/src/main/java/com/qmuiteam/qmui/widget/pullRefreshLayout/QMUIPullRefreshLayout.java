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

package com.qmuiteam.qmui.widget.pullRefreshLayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Scroller;

import com.qmuiteam.qmui.BuildConfig;
import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.nestedScroll.QMUIContinuousNestedScrollLayout;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.skin.defaultAttr.IQMUISkinDefaultAttrProvider;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.collection.SimpleArrayMap;
import androidx.core.content.ContextCompat;
import androidx.core.view.NestedScrollingParent;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

/**
 * 下拉刷新控件, 作为容器，下拉时会将子 View 下移, 并拉出 RefreshView（表示正在刷新的 View）
 * <ul>
 * <li>可通过继承并覆写 {@link #createRefreshView()} 方法实现自己的 RefreshView</li>
 * <li>可通过 {@link #setRefreshOffsetCalculator(RefreshOffsetCalculator)} 自己决定在下拉过程中 RefreshView 的位置</li>
 * <li>可在 xml 中使用 {@link com.qmuiteam.qmui.R.styleable#QMUIPullRefreshLayout} 这些属性或在 Java 设置对应的属性决定子View的开始位置、触发刷新的位置等值</li>
 * </ul>
 *
 * @author cginechen
 * @date 2016-12-11
 */
public class QMUIPullRefreshLayout extends ViewGroup implements NestedScrollingParent {
    private static final String TAG = "QMUIPullRefreshLayout";
    private static final int INVALID_POINTER = -1;
    private static final int FLAG_NEED_SCROLL_TO_INIT_POSITION = 1;
    private static final int FLAG_NEED_SCROLL_TO_REFRESH_POSITION = 1 << 1;
    private static final int FLAG_NEED_DO_REFRESH = 1 << 2;
    private static final int FLAG_NEED_DELIVER_VELOCITY = 1 << 3;
    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    boolean mIsRefreshing = false;
    private View mTargetView;
    private IRefreshView mIRefreshView;
    private View mRefreshView;
    private int mRefreshZIndex = -1;
    private int mSystemTouchSlop;
    private int mTouchSlop;
    private OnPullListener mListener;
    private OnChildScrollUpCallback mChildScrollUpCallback;
    /**
     * RefreshView的初始offset
     */
    private int mRefreshInitOffset;
    /**
     * 刷新时RefreshView的offset
     */
    private int mRefreshEndOffset;
    /**
     * RefreshView当前offset
     */
    private int mRefreshCurrentOffset;
    /**
     * 是否自动根据RefreshView的高度计算RefreshView的初始位置
     */
    private boolean mAutoCalculateRefreshInitOffset = true;
    /**
     * 是否自动根据TargetView在刷新时的位置计算RefreshView的结束位置
     */
    private boolean mAutoCalculateRefreshEndOffset = true;
    /**
     * 自动让TargetView的刷新位置与RefreshView高度相等
     */
    private boolean mEqualTargetRefreshOffsetToRefreshViewHeight = false;
    /**
     * 当拖拽超过超过mAutoScrollToRefreshMinOffset时，自动滚动到刷新位置并触发刷新
     * mAutoScrollToRefreshMinOffset == - 1表示要mAutoScrollToRefreshMinOffset>=mTargetRefreshOffset
     */
    private int mAutoScrollToRefreshMinOffset = -1;
    /**
     * TargetView(ListView或者ScrollView等)的初始位置
     */
    private int mTargetInitOffset;
    /**
     * 下拉时 TargetView（ListView 或者 ScrollView 等）当前的位置。
     */
    private int mTargetCurrentOffset;
    /**
     * 刷新时TargetView(ListView或者ScrollView等)的位置
     */
    private int mTargetRefreshOffset;
    private boolean mDisableNestScrollImpl = false;
    private boolean mEnableOverPull = true;
    private boolean mNestedScrollInProgress;
    private int mActivePointerId = INVALID_POINTER;
    private boolean mIsDragging;
    private float mInitialDownY;
    private float mInitialDownX;
    @SuppressWarnings("FieldCanBeLocal") private float mInitialMotionY;
    private float mLastMotionY;
    private float mDragRate = 0.65f;
    private RefreshOffsetCalculator mRefreshOffsetCalculator;
    private VelocityTracker mVelocityTracker;
    private float mMaxVelocity;
    private float mMiniVelocity;
    private Scroller mScroller;
    private int mScrollFlag = 0;
    private boolean mNestScrollDurationRefreshing = false;
    private Runnable mPendingRefreshDirectlyAction = null;
    private boolean mSafeDisallowInterceptTouchEvent = false;


    public QMUIPullRefreshLayout(Context context) {
        this(context, null);
    }

    public QMUIPullRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.QMUIPullRefreshLayoutStyle);
    }

    public QMUIPullRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);

        final ViewConfiguration vc = ViewConfiguration.get(context);
        mMaxVelocity = vc.getScaledMaximumFlingVelocity();
        mMiniVelocity = vc.getScaledMinimumFlingVelocity();
        mSystemTouchSlop = vc.getScaledTouchSlop();
        mTouchSlop = QMUIDisplayHelper.px2dp(context, mSystemTouchSlop); //系统的值是8dp,如何配置？

        mScroller = new Scroller(getContext());
        mScroller.setFriction(getScrollerFriction());

        addRefreshView();
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);

        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);

        TypedArray array = context.obtainStyledAttributes(attrs,
                R.styleable.QMUIPullRefreshLayout, defStyleAttr, 0);

        try {
            mRefreshInitOffset = array.getDimensionPixelSize(
                    R.styleable.QMUIPullRefreshLayout_qmui_refresh_init_offset, Integer.MIN_VALUE);
            mRefreshEndOffset = array.getDimensionPixelSize(
                    R.styleable.QMUIPullRefreshLayout_qmui_refresh_end_offset, Integer.MIN_VALUE);
            mTargetInitOffset = array.getDimensionPixelSize(
                    R.styleable.QMUIPullRefreshLayout_qmui_target_init_offset, 0);
            mTargetRefreshOffset = array.getDimensionPixelSize(
                    R.styleable.QMUIPullRefreshLayout_qmui_target_refresh_offset,
                    QMUIDisplayHelper.dp2px(getContext(), 72));
            mAutoCalculateRefreshInitOffset = mRefreshInitOffset == Integer.MIN_VALUE ||
                    array.getBoolean(R.styleable.QMUIPullRefreshLayout_qmui_auto_calculate_refresh_init_offset, false);
            mAutoCalculateRefreshEndOffset = mRefreshEndOffset == Integer.MIN_VALUE ||
                    array.getBoolean(R.styleable.QMUIPullRefreshLayout_qmui_auto_calculate_refresh_end_offset, false);
            mEqualTargetRefreshOffsetToRefreshViewHeight = array.getBoolean(R.styleable.QMUIPullRefreshLayout_qmui_equal_target_refresh_offset_to_refresh_view_height, false);
        } finally {
            array.recycle();
        }
        mRefreshCurrentOffset = mRefreshInitOffset;
        mTargetCurrentOffset = mTargetInitOffset;
    }

    public static boolean defaultCanScrollUp(View view) {
        if (view == null) {
            return false;
        }
        if (view instanceof QMUIContinuousNestedScrollLayout) {
            QMUIContinuousNestedScrollLayout layout = (QMUIContinuousNestedScrollLayout) view;
            return layout.getCurrentScroll() > 0;
        }

        if (view instanceof QMUIStickySectionLayout) {
            QMUIStickySectionLayout layout = (QMUIStickySectionLayout) view;
            return defaultCanScrollUp(layout.getRecyclerView());
        }

        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(view, -1) || view.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(view, -1);
        }
    }

    public void setOnPullListener(OnPullListener listener) {
        mListener = listener;
    }

    public void setDisableNestScrollImpl(boolean disableNestScrollImpl) {
        mDisableNestScrollImpl = disableNestScrollImpl;
    }

    public void setDragRate(float dragRate) {
        // have no idea to change drag rate for nest scroll
        mDisableNestScrollImpl = true;
        mDragRate = dragRate;
    }

    public void setChildScrollUpCallback(OnChildScrollUpCallback childScrollUpCallback) {
        mChildScrollUpCallback = childScrollUpCallback;
    }

    protected float getScrollerFriction() {
        return ViewConfiguration.getScrollFriction();
    }

    public void setAutoScrollToRefreshMinOffset(int autoScrollToRefreshMinOffset) {
        mAutoScrollToRefreshMinOffset = autoScrollToRefreshMinOffset;
    }

    /**
     * 覆盖该方法以实现自己的 RefreshView。
     *
     * @return 自定义的 RefreshView, 注意该 View 必须实现 {@link IRefreshView} 接口
     */
    protected View createRefreshView() {
        return new RefreshView(getContext());
    }

    private void addRefreshView() {
        if (mRefreshView == null) {
            mRefreshView = createRefreshView();
        }
        if (!(mRefreshView instanceof IRefreshView)) {
            throw new RuntimeException("refreshView must be a instance of IRefreshView");
        }
        mIRefreshView = (IRefreshView) mRefreshView;
        if (mRefreshView.getLayoutParams() == null) {
            mRefreshView.setLayoutParams(new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        }
        addView(mRefreshView);
    }

    /**
     * 设置在下拉过程中 RefreshView 的偏移量
     */
    public void setRefreshOffsetCalculator(RefreshOffsetCalculator refreshOffsetCalculator) {
        mRefreshOffsetCalculator = refreshOffsetCalculator;
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (mRefreshZIndex < 0) {
            return i;
        }
        // 最后才绘制mRefreshView
        if (i == mRefreshZIndex) {
            return childCount - 1;
        }
        if (i > mRefreshZIndex) {
            return i - 1;
        }
        return i;
    }

    /**
     * child view call, to ensure disallowInterceptTouchEvent make sense
     * <p>
     * how to optimize this...
     */
    public void openSafeDisallowInterceptTouchEvent() {
        mSafeDisallowInterceptTouchEvent = true;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {

        if (mSafeDisallowInterceptTouchEvent) {
            super.requestDisallowInterceptTouchEvent(b);
            mSafeDisallowInterceptTouchEvent = false;
        }

        // if this is a List < L or another view that doesn't support nested
        // scrolling, ignore this request so that the vertical scroll event
        // isn't stolen
        //noinspection StatementWithEmptyBody
        if ((android.os.Build.VERSION.SDK_INT < 21 && mTargetView instanceof AbsListView)
                || (mTargetView != null && !ViewCompat.isNestedScrollingEnabled(mTargetView))) {
            // Nope.
        } else {
            super.requestDisallowInterceptTouchEvent(b);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ensureTargetView();
        if (mTargetView == null) {
            Log.d(TAG, "onMeasure: mTargetView == null");
            return;
        }
        int targetMeasureWidthSpec = MeasureSpec.makeMeasureSpec(
                getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY);
        int targetMeasureHeightSpec = MeasureSpec.makeMeasureSpec(
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY);
        mTargetView.measure(targetMeasureWidthSpec, targetMeasureHeightSpec);
        measureChild(mRefreshView, widthMeasureSpec, heightMeasureSpec);
        mRefreshZIndex = -1;
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) == mRefreshView) {
                mRefreshZIndex = i;
                break;
            }
        }

        int refreshViewHeight = mRefreshView.getMeasuredHeight();
        if (mAutoCalculateRefreshInitOffset) {
            if (mRefreshInitOffset != -refreshViewHeight) {
                mRefreshInitOffset = -refreshViewHeight;
                mRefreshCurrentOffset = mRefreshInitOffset;
            }

        }
        if (mEqualTargetRefreshOffsetToRefreshViewHeight) {
            mTargetRefreshOffset = refreshViewHeight;
        }
        if (mAutoCalculateRefreshEndOffset) {
            mRefreshEndOffset = (mTargetRefreshOffset - refreshViewHeight) / 2;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (getChildCount() == 0) {
            return;
        }
        ensureTargetView();
        if (mTargetView == null) {
            Log.d(TAG, "onLayout: mTargetView == null");
            return;
        }

        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        mTargetView.layout(childLeft, childTop + mTargetCurrentOffset,
                childLeft + childWidth, childTop + childHeight + mTargetCurrentOffset);
        int refreshViewWidth = mRefreshView.getMeasuredWidth();
        int refreshViewHeight = mRefreshView.getMeasuredHeight();
        mRefreshView.layout((width / 2 - refreshViewWidth / 2), mRefreshCurrentOffset,
                (width / 2 + refreshViewWidth / 2), mRefreshCurrentOffset + refreshViewHeight);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTargetView();

        final int action = ev.getAction();
        int pointerIndex;

        if (!isEnabled() || canChildScrollUp() || mNestedScrollInProgress) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "fast end onIntercept: isEnabled = " + isEnabled() + "; canChildScrollUp = "
                        + canChildScrollUp() + " ; mNestedScrollInProgress = " + mNestedScrollInProgress);
            }
            return false;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mIsDragging = false;
                mActivePointerId = ev.getPointerId(0);
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                mInitialDownX = ev.getX(pointerIndex);
                mInitialDownY = ev.getY(pointerIndex);
                break;

            case MotionEvent.ACTION_MOVE:
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }

                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);
                startDragging(x, y);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsDragging = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsDragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        int pointerIndex;

        if (!isEnabled() || canChildScrollUp() || mNestedScrollInProgress) {
            Log.d(TAG, "fast end onTouchEvent: isEnabled = " + isEnabled() + "; canChildScrollUp = "
                    + canChildScrollUp() + " ; mNestedScrollInProgress = " + mNestedScrollInProgress);
            return false;
        }

        acquireVelocityTracker(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mIsDragging = false;
                mScrollFlag = 0;
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mActivePointerId = ev.getPointerId(0);
                break;

            case MotionEvent.ACTION_MOVE: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(TAG, "onTouchEvent Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);
                startDragging(x, y);

                if (mIsDragging) {
                    float dy = (y - mLastMotionY) * mDragRate;
                    if (dy >= 0) {
                        moveTargetView(dy, true);
                    } else {
                        int move = moveTargetView(dy, true);
                        float delta = Math.abs(dy) - Math.abs(move);
                        if (delta > 0) {
                            // 重新dispatch一次down事件，使得列表可以继续滚动
                            ev.setAction(MotionEvent.ACTION_DOWN);
                            // 立刻dispatch一个大于mSystemTouchSlop的move事件，防止触发TargetView
                            float offsetLoc = mSystemTouchSlop + 1;
                            if (delta > offsetLoc) {
                                offsetLoc = delta;
                            }
                            ev.offsetLocation(0, offsetLoc);
                            super.dispatchTouchEvent(ev);
                            ev.setAction(action);
                            // 再dispatch一次move事件，消耗掉所有dy
                            ev.offsetLocation(0, -offsetLoc);
                            super.dispatchTouchEvent(ev);
                        }
                    }
                    mLastMotionY = y;
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                pointerIndex = ev.getActionIndex();
                if (pointerIndex < 0) {
                    Log.e(TAG, "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                    return false;
                }
                mActivePointerId = ev.getPointerId(pointerIndex);
                break;
            }

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }

                if (mIsDragging) {
                    mIsDragging = false;
                    mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                    float vy = mVelocityTracker.getYVelocity(mActivePointerId);
                    if (Math.abs(vy) < mMiniVelocity) {
                        vy = 0;
                    }
                    finishPull((int) vy);
                }
                mActivePointerId = INVALID_POINTER;
                releaseVelocityTracker();
                return false;
            }
            case MotionEvent.ACTION_CANCEL:
                releaseVelocityTracker();
                return false;
        }

        return true;
    }

    private void ensureTargetView() {
        if (mTargetView == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (!view.equals(mRefreshView)) {
                    onSureTargetView(view);
                    mTargetView = view;
                    break;
                }
            }
        }
        if (mTargetView != null && mPendingRefreshDirectlyAction != null) {
            Runnable runnable = mPendingRefreshDirectlyAction;
            mPendingRefreshDirectlyAction = null;
            runnable.run();
        }
    }

    /**
     * 确定TargetView, 提供机会给子类来做一些初始化的操作
     */
    protected void onSureTargetView(View targetView) {

    }

    protected void onFinishPull(int vy, int refreshInitOffset, int refreshEndOffset, int refreshViewHeight,
                                int targetCurrentOffset, int targetInitOffset, int targetRefreshOffset) {

    }

    private void finishPull(int vy) {
        info("finishPull: vy = " + vy + " ; mTargetCurrentOffset = " + mTargetCurrentOffset +
                " ; mTargetRefreshOffset = " + mTargetRefreshOffset + " ; mTargetInitOffset = " + mTargetInitOffset +
                " ; mScroller.isFinished() = " + mScroller.isFinished());
        int miniVy = vy / 1000; // 向下拖拽时， 速度不能太大
        onFinishPull(miniVy, mRefreshInitOffset, mRefreshEndOffset, mRefreshView.getHeight(),
                mTargetCurrentOffset, mTargetInitOffset, mTargetRefreshOffset);
        if (mTargetCurrentOffset >= mTargetRefreshOffset) {
            if (miniVy > 0) {
                mScrollFlag = FLAG_NEED_SCROLL_TO_REFRESH_POSITION | FLAG_NEED_DO_REFRESH;
                mScroller.fling(0, mTargetCurrentOffset, 0, miniVy,
                        0, 0, mTargetInitOffset, Integer.MAX_VALUE);
                invalidate();
            } else if (miniVy < 0) {
                mScroller.fling(0, mTargetCurrentOffset, 0, vy,
                        0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
                if (mScroller.getFinalY() < mTargetInitOffset) {
                    mScrollFlag = FLAG_NEED_DELIVER_VELOCITY;
                } else if (mScroller.getFinalY() < mTargetRefreshOffset) {
                    int dy = mTargetInitOffset - mTargetCurrentOffset;
                    mScroller.startScroll(0, mTargetCurrentOffset, 0, dy);
                } else if (mScroller.getFinalY() == mTargetRefreshOffset) {
                    mScrollFlag = FLAG_NEED_DO_REFRESH;
                } else {
                    mScroller.startScroll(0, mTargetCurrentOffset, 0, mTargetRefreshOffset - mTargetCurrentOffset);
                    mScrollFlag = FLAG_NEED_DO_REFRESH;
                }
                invalidate();
            } else {
                if (mTargetCurrentOffset > mTargetRefreshOffset) {
                    mScroller.startScroll(0, mTargetCurrentOffset, 0, mTargetRefreshOffset - mTargetCurrentOffset);
                }
                mScrollFlag = FLAG_NEED_DO_REFRESH;
                invalidate();
            }
        } else {
            if (miniVy > 0) {
                mScroller.fling(0, mTargetCurrentOffset, 0, miniVy, 0, 0, mTargetInitOffset, Integer.MAX_VALUE);
                if (mScroller.getFinalY() > mTargetRefreshOffset) {
                    mScrollFlag = FLAG_NEED_SCROLL_TO_REFRESH_POSITION | FLAG_NEED_DO_REFRESH;
                } else if (mAutoScrollToRefreshMinOffset >= 0 && mScroller.getFinalY() > mAutoScrollToRefreshMinOffset) {
                    mScroller.startScroll(0, mTargetCurrentOffset, 0, mTargetRefreshOffset - mTargetCurrentOffset);
                    mScrollFlag = FLAG_NEED_DO_REFRESH;
                } else {
                    mScrollFlag = FLAG_NEED_SCROLL_TO_INIT_POSITION;
                }
                invalidate();
            } else if (miniVy < 0) {
                mScrollFlag = 0;
                mScroller.fling(0, mTargetCurrentOffset, 0, vy, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
                if (mScroller.getFinalY() < mTargetInitOffset) {
                    mScrollFlag = FLAG_NEED_DELIVER_VELOCITY;
                } else {
                    mScroller.startScroll(0, mTargetCurrentOffset, 0, mTargetInitOffset - mTargetCurrentOffset);
                    mScrollFlag = 0;
                }
                invalidate();
            } else {
                if (mTargetCurrentOffset == mTargetInitOffset) {
                    return;
                }
                if (mAutoScrollToRefreshMinOffset >= 0 && mTargetCurrentOffset >= mAutoScrollToRefreshMinOffset) {
                    mScroller.startScroll(0, mTargetCurrentOffset, 0, mTargetRefreshOffset - mTargetCurrentOffset);
                    mScrollFlag = FLAG_NEED_DO_REFRESH;
                } else {
                    mScroller.startScroll(0, mTargetCurrentOffset, 0, mTargetInitOffset - mTargetCurrentOffset);
                    mScrollFlag = 0;
                }
                invalidate();
            }
        }
    }

    protected void onRefresh() {
        if (mIsRefreshing) {
            return;
        }
        mIsRefreshing = true;
        mIRefreshView.doRefresh();
        if (mListener != null) {
            mListener.onRefresh();
        }
    }

    public void finishRefresh() {
        mIsRefreshing = false;
        mIRefreshView.stop();
        mScrollFlag = FLAG_NEED_SCROLL_TO_INIT_POSITION;
        mScroller.forceFinished(true);
        invalidate();
    }

    public void setToRefreshDirectly() {
        setToRefreshDirectly(0);
    }

    public void setToRefreshDirectly(final long delay) {
        if (mTargetView != null) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    setTargetViewToTop(mTargetView);
                    onRefresh();
                    mScrollFlag = FLAG_NEED_SCROLL_TO_REFRESH_POSITION;
                    invalidate();
                }
            }, delay);

        } else {
            mPendingRefreshDirectlyAction = new Runnable() {
                @Override
                public void run() {
                    setToRefreshDirectly(delay);
                }
            };
        }
    }


    public void setEnableOverPull(boolean enableOverPull) {
        mEnableOverPull = enableOverPull;
    }

    protected void setTargetViewToTop(View targetView) {
        if (targetView instanceof RecyclerView) {
            ((RecyclerView) targetView).scrollToPosition(0);
        } else if (targetView instanceof AbsListView) {
            AbsListView listView = (AbsListView) targetView;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                listView.setSelectionFromTop(0, 0);
            } else {
                listView.setSelection(0);
            }
        } else {
            targetView.scrollTo(0, 0);
        }
    }


    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = ev.getActionIndex();
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    public void reset() {
        moveTargetViewTo(mTargetInitOffset, false);
        mIRefreshView.stop();
        mIsRefreshing = false;
        mScroller.forceFinished(true);
        mScrollFlag = 0;
    }

    protected void startDragging(float x, float y) {
        final float dx = x - mInitialDownX;
        final float dy = y - mInitialDownY;
        boolean isYDrag = isYDrag(dx, dy);
        if (isYDrag && (dy > mTouchSlop || (dy < -mTouchSlop && mTargetCurrentOffset > mTargetInitOffset)) && !mIsDragging) {
            mInitialMotionY = mInitialDownY + mTouchSlop;
            mLastMotionY = mInitialMotionY;
            mIsDragging = true;
        }
    }

    protected boolean isYDrag(float dx, float dy) {
        return Math.abs(dy) > Math.abs(dx);
    }

    public boolean isDragging() {
        return mIsDragging;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        reset();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            reset();
            invalidate();
        }
    }

    public boolean canChildScrollUp() {
        if (mChildScrollUpCallback != null) {
            return mChildScrollUpCallback.canChildScrollUp(this, mTargetView);
        }
        return defaultCanScrollUp(mTargetView);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        info("onStartNestedScroll: nestedScrollAxes = " + nestedScrollAxes);
        return !mDisableNestScrollImpl && isEnabled() && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        info("onNestedScrollAccepted: axes = " + axes);
        mScroller.abortAnimation();
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        mNestedScrollInProgress = true;
        mIsDragging = true;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        info("onNestedPreScroll: dx = " + dx + " ; dy = " + dy);
        int parentCanConsume = mTargetCurrentOffset - mTargetInitOffset;
        if (dy > 0 && parentCanConsume > 0) {
            if (dy >= parentCanConsume) {
                consumed[1] = parentCanConsume;
                moveTargetViewTo(mTargetInitOffset, true);
            } else {
                consumed[1] = dy;
                moveTargetView(-dy, true);
            }
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        info("onNestedScroll: dxConsumed = " + dxConsumed + " ; dyConsumed = " + dyConsumed +
                " ; dxUnconsumed = " + dxUnconsumed + " ; dyUnconsumed = " + dyUnconsumed);
        if (dyUnconsumed < 0 && !canChildScrollUp() && mScroller.isFinished() && mScrollFlag == 0) {
            moveTargetView(-dyUnconsumed, true);
        }
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onStopNestedScroll(View child) {
        info("onStopNestedScroll: mNestedScrollInProgress = " + mNestedScrollInProgress);
        mNestedScrollingParentHelper.onStopNestedScroll(child);
        if (mNestedScrollInProgress) {
            mNestedScrollInProgress = false;
            mIsDragging = false;
            if (!mNestScrollDurationRefreshing) {
                finishPull(0);
            }

        }
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        info("onNestedPreFling: mTargetCurrentOffset = " + mTargetCurrentOffset +
                " ; velocityX = " + velocityX + " ; velocityY = " + velocityY);
        if (mTargetCurrentOffset > mTargetInitOffset) {
            mNestedScrollInProgress = false;
            mIsDragging = false;
            if (!mNestScrollDurationRefreshing) {
                finishPull((int) -velocityY);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        try {
            return super.onNestedFling(target, velocityX, velocityY, consumed);
        } catch (Throwable e) {
            // android 24及以上ViewGroup会继续往上派发， 23以及以下直接返回false
            // 低于5.0的机器和RecyclerView配合工作时，部分机型会调用这个方法，但是ViewGroup并没有实现这个方法，会报错，这里catch一下
        }
        return false;
    }

    private int moveTargetView(float dy, boolean isDragging) {
        int target = (int) (mTargetCurrentOffset + dy);
        return moveTargetViewTo(target, isDragging);
    }

    private int moveTargetViewTo(int target, boolean isDragging) {
        return moveTargetViewTo(target, isDragging, false);
    }

    private int moveTargetViewTo(int target, boolean isDragging, boolean calculateAnyWay) {
        target = calculateTargetOffset(target, mTargetInitOffset, mTargetRefreshOffset, mEnableOverPull);
        int offset = 0;
        if (target != mTargetCurrentOffset || calculateAnyWay) {
            offset = target - mTargetCurrentOffset;
            ViewCompat.offsetTopAndBottom(mTargetView, offset);
            mTargetCurrentOffset = target;
            int total = mTargetRefreshOffset - mTargetInitOffset;
            if (isDragging) {
                mIRefreshView.onPull(Math.min(mTargetCurrentOffset - mTargetInitOffset, total), total,
                        mTargetCurrentOffset - mTargetRefreshOffset);
            }
            onMoveTargetView(mTargetCurrentOffset);
            if (mListener != null) {
                mListener.onMoveTarget(mTargetCurrentOffset);
            }

            if (mRefreshOffsetCalculator == null) {
                mRefreshOffsetCalculator = new QMUIDefaultRefreshOffsetCalculator();
            }
            int newRefreshOffset = mRefreshOffsetCalculator.calculateRefreshOffset(mRefreshInitOffset, mRefreshEndOffset, mRefreshView.getHeight(),
                    mTargetCurrentOffset, mTargetInitOffset, mTargetRefreshOffset);
            if (newRefreshOffset != mRefreshCurrentOffset) {
                ViewCompat.offsetTopAndBottom(mRefreshView, newRefreshOffset - mRefreshCurrentOffset);
                mRefreshCurrentOffset = newRefreshOffset;
                onMoveRefreshView(mRefreshCurrentOffset);
                if (mListener != null) {
                    mListener.onMoveRefreshView(mRefreshCurrentOffset);
                }
            }
        }
        return offset;
    }

    protected int calculateTargetOffset(int target, int targetInitOffset, int targetRefreshOffset, boolean enableOverPull) {
        target = Math.max(target, targetInitOffset);
        if (!enableOverPull) {
            target = Math.min(target, targetRefreshOffset);
        }
        return target;
    }

    private void acquireVelocityTracker(final MotionEvent event) {
        if (null == mVelocityTracker) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    private void releaseVelocityTracker() {
        if (null != mVelocityTracker) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    public int getRefreshInitOffset() {
        return mRefreshInitOffset;
    }

    public int getRefreshEndOffset() {
        return mRefreshEndOffset;
    }

    public int getTargetInitOffset() {
        return mTargetInitOffset;
    }

    public int getTargetRefreshOffset() {
        return mTargetRefreshOffset;
    }

    public void setTargetRefreshOffset(int targetRefreshOffset) {
        mEqualTargetRefreshOffsetToRefreshViewHeight = false;
        mTargetRefreshOffset = targetRefreshOffset;
    }

    public View getTargetView() {
        return mTargetView;
    }

    protected void onMoveTargetView(int offset) {

    }

    protected void onMoveRefreshView(int offset) {

    }


    private boolean hasFlag(int flag) {
        return (mScrollFlag & flag) == flag;
    }

    private void removeFlag(int flag) {
        mScrollFlag = mScrollFlag & ~flag;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int offsetY = mScroller.getCurrY();
            moveTargetViewTo(offsetY, false);
            if (offsetY <= 0 && hasFlag(FLAG_NEED_DELIVER_VELOCITY)) {
                deliverVelocity();
                mScroller.forceFinished(true);
            }
            invalidate();
        } else if (hasFlag(FLAG_NEED_SCROLL_TO_INIT_POSITION)) {
            removeFlag(FLAG_NEED_SCROLL_TO_INIT_POSITION);
            if (mTargetCurrentOffset != mTargetInitOffset) {
                mScroller.startScroll(0, mTargetCurrentOffset, 0, mTargetInitOffset - mTargetCurrentOffset);
            }
            invalidate();
        } else if (hasFlag(FLAG_NEED_SCROLL_TO_REFRESH_POSITION)) {
            removeFlag(FLAG_NEED_SCROLL_TO_REFRESH_POSITION);
            if (mTargetCurrentOffset != mTargetRefreshOffset) {
                mScroller.startScroll(0, mTargetCurrentOffset, 0, mTargetRefreshOffset - mTargetCurrentOffset);
            } else {
                moveTargetViewTo(mTargetRefreshOffset, false, true);
            }
            invalidate();
        } else if (hasFlag(FLAG_NEED_DO_REFRESH)) {
            removeFlag(FLAG_NEED_DO_REFRESH);
            onRefresh();
            moveTargetViewTo(mTargetRefreshOffset, false, true);
        } else {
            deliverVelocity();
        }
    }

    private void deliverVelocity() {
        if (hasFlag(FLAG_NEED_DELIVER_VELOCITY)) {
            removeFlag(FLAG_NEED_DELIVER_VELOCITY);
            if (mScroller.getCurrVelocity() > mMiniVelocity) {
                info("deliver velocity: " + mScroller.getCurrVelocity());
                // if there is a velocity, pass it on
                if (mTargetView instanceof RecyclerView) {
                    ((RecyclerView) mTargetView).fling(0, (int) mScroller.getCurrVelocity());
                } else if (mTargetView instanceof AbsListView && android.os.Build.VERSION.SDK_INT >= 21) {
                    ((AbsListView) mTargetView).fling((int) mScroller.getCurrVelocity());
                }
            }
        }
    }

    private void info(String msg) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, msg);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mNestScrollDurationRefreshing = mIsRefreshing || (mScrollFlag & FLAG_NEED_DO_REFRESH) != 0;
        } else if (mNestScrollDurationRefreshing) {
            if (action == MotionEvent.ACTION_MOVE) {
                if (!mIsRefreshing && mScroller.isFinished() && mScrollFlag == 0) {
                    // 这里必须要 dispatch 一次 down 事件，否则不能触发 NestScroll，具体可参考 RecyclerView
                    // down 过程中会触发 onStopNestedScroll，mNestScrollDurationRefreshing 必须在之后
                    // 置为false，否则会触发 finishPull
                    ev.offsetLocation(0, -mSystemTouchSlop - 1);
                    ev.setAction(MotionEvent.ACTION_DOWN);
                    super.dispatchTouchEvent(ev);
                    mNestScrollDurationRefreshing = false;
                    ev.setAction(action);
                    // offset touch slop, 避免触发点击事件
                    ev.offsetLocation(0, mSystemTouchSlop + 1);
                }
            } else {
                mNestScrollDurationRefreshing = false;
            }
        }

        return super.dispatchTouchEvent(ev);
    }

    public interface OnPullListener {

        void onMoveTarget(int offset);

        void onMoveRefreshView(int offset);

        void onRefresh();
    }


    public interface OnChildScrollUpCallback {
        boolean canChildScrollUp(QMUIPullRefreshLayout parent, @Nullable View child);
    }

    public interface RefreshOffsetCalculator {

        /**
         * 通过 targetView 的当前位置、targetView 的初始和刷新位置以及 refreshView 的初始与结束位置计算 RefreshView 的位置。
         *
         * @param refreshInitOffset   RefreshView 的初始 offset。
         * @param refreshEndOffset    刷新时 RefreshView 的 offset。
         * @param refreshViewHeight   RerfreshView 的高度
         * @param targetCurrentOffset 下拉时 TargetView（ListView 或者 ScrollView 等）当前的位置。
         * @param targetInitOffset    TargetView（ListView 或者 ScrollView 等）的初始位置。
         * @param targetRefreshOffset 刷新时 TargetView（ListView 或者 ScrollView等）的位置。
         * @return RefreshView 当前的位置。
         */
        int calculateRefreshOffset(int refreshInitOffset, int refreshEndOffset, int refreshViewHeight,
                                   int targetCurrentOffset, int targetInitOffset, int targetRefreshOffset);
    }

    public interface IRefreshView {
        void stop();

        void doRefresh();

        void onPull(int offset, int total, int overPull);
    }

    public static class RefreshView extends AppCompatImageView implements IRefreshView, IQMUISkinDefaultAttrProvider {
        private static final int MAX_ALPHA = 255;
        private static final float TRIM_RATE = 0.85f;
        private static final float TRIM_OFFSET = 0.4f;

        static final int CIRCLE_DIAMETER = 40;
        static final int CIRCLE_DIAMETER_LARGE = 56;

        private CircularProgressDrawable mProgress;
        private int mCircleDiameter;

        private static SimpleArrayMap<String, Integer> sDefaultSkinAttrs;

        static {
            sDefaultSkinAttrs = new SimpleArrayMap<>(4);
            sDefaultSkinAttrs.put(QMUISkinValueBuilder.TINT_COLOR, R.attr.qmui_skin_support_pull_refresh_view_color);
        }

        public RefreshView(Context context) {
            super(context);
            mProgress = new CircularProgressDrawable(context);
            setColorSchemeColors(QMUIResHelper.getAttrColor(
                    context, R.attr.qmui_skin_support_pull_refresh_view_color));
            mProgress.setStyle(CircularProgressDrawable.LARGE);
            mProgress.setAlpha(MAX_ALPHA);
            mProgress.setArrowScale(0.8f);
            setImageDrawable(mProgress);
            final DisplayMetrics metrics = getResources().getDisplayMetrics();
            mCircleDiameter = (int) (CIRCLE_DIAMETER * metrics.density);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(mCircleDiameter, mCircleDiameter);
        }

        @Override
        public void onPull(int offset, int total, int overPull) {
            if (mProgress.isRunning()) {
                return;
            }
            float end = TRIM_RATE * offset / total;
            float rotate = TRIM_OFFSET * offset / total;
            if (overPull > 0) {
                rotate += TRIM_OFFSET * overPull / total;
            }
            mProgress.setArrowEnabled(true);
            mProgress.setStartEndTrim(0, end);
            mProgress.setProgressRotation(rotate);
        }

        public void setSize(@CircularProgressDrawable.ProgressDrawableSize int size) {
            if (size != CircularProgressDrawable.LARGE && size != CircularProgressDrawable.DEFAULT) {
                return;
            }
            final DisplayMetrics metrics = getResources().getDisplayMetrics();
            if (size == CircularProgressDrawable.LARGE) {
                mCircleDiameter = (int) (CIRCLE_DIAMETER_LARGE * metrics.density);
            } else {
                mCircleDiameter = (int) (CIRCLE_DIAMETER * metrics.density);
            }
            // force the bounds of the progress circle inside the circle view to
            // update by setting it to null before updating its size and then
            // re-setting it
            setImageDrawable(null);
            mProgress.setStyle(size);
            setImageDrawable(mProgress);
        }

        public void stop() {
            mProgress.stop();
        }

        public void doRefresh() {
            mProgress.start();
        }

        public void setColorSchemeResources(@ColorRes int... colorResIds) {
            final Context context = getContext();
            int[] colorRes = new int[colorResIds.length];
            for (int i = 0; i < colorResIds.length; i++) {
                colorRes[i] = ContextCompat.getColor(context, colorResIds[i]);
            }
            setColorSchemeColors(colorRes);
        }

        public void setColorSchemeColors(@ColorInt int... colors) {
            mProgress.setColorSchemeColors(colors);
        }

        @Override
        public SimpleArrayMap<String, Integer> getDefaultSkinAttrs() {
            return sDefaultSkinAttrs;
        }
    }
}