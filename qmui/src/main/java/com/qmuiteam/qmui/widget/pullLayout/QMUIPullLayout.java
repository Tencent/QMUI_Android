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

package com.qmuiteam.qmui.widget.pullLayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.OverScroller;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.qmuiteam.qmui.Beta;
import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.util.QMUIViewOffsetHelper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.qmuiteam.qmui.QMUIInterpolatorStaticHolder.QUNITIC_INTERPOLATOR;

@Beta
public class QMUIPullLayout extends FrameLayout implements NestedScrollingParent3 {
    public static final float DEFAULT_PULL_RATE = 0.45f;
    public static final float DEFAULT_FLING_FRACTION = 0.002f;
    public static final float DEFAULT_SCROLL_SPEED_PER_PIXEL = 1.5f;
    public static final int DEFAULT_MIN_SCROLL_DURATION = 300;
    public static final int PULL_EDGE_LEFT = 0x01;
    public static final int PULL_EDGE_TOP = 0x02;
    public static final int PULL_EDGE_RIGHT = 0x04;
    public static final int PULL_EDGE_BOTTOM = 0x08;
    public static final int PUL_EDGE_ALL = PULL_EDGE_LEFT | PULL_EDGE_TOP | PULL_EDGE_RIGHT | PULL_EDGE_BOTTOM;

    private static final int STATE_IDLE = 0;
    private static final int STATE_PULLING = 1;
    private static final int STATE_SETTLING_TO_TRIGGER_OFFSET = 2;
    private static final int STATE_TRIGGERING= 3;
    private static final int STATE_SETTLING_TO_INIT_OFFSET = 4;
    private static final int STATE_SETTLING_DELIVER = 5;
    private static final int STATE_SETTLING_FLING = 6;

    @IntDef({PULL_EDGE_LEFT, PULL_EDGE_TOP, PULL_EDGE_RIGHT, PULL_EDGE_BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PullEdge {
    }


    private int mEnabledEdges;
    private View mTargetView;
    private QMUIViewOffsetHelper mTargetOffsetHelper;
    private PullAction mLeftPullAction = null;
    private PullAction mTopPullAction = null;
    private PullAction mRightPullAction = null;
    private PullAction mBottomPullAction = null;
    private ActionListener mActionListener;

    // Array to be used for calls from v2 version of onNestedScroll to v3 version of onNestedScroll.
    // This only exist to prevent GC and object instantiation costs that are present before API 21.
    private final int[] mNestedScrollingV2ConsumedCompat = new int[2];
    private StopTargetViewFlingImpl mStopTargetViewFlingImpl = DefaultStopTargetViewFlingImpl.getInstance();
    private Runnable mStopTargetFlingRunnable = null;
    private OverScroller mScroller;
    private float mNestedPreFlingVelocityScaleDown = 10;
    private int mMinScrollDuration = DEFAULT_MIN_SCROLL_DURATION;
    private int mState = STATE_IDLE;

    private final NestedScrollingParentHelper mNestedScrollingParentHelper;

    public QMUIPullLayout(@NonNull Context context) {
        this(context, null);
    }

    public QMUIPullLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.QMUIPullLayoutStyle);
    }

    public QMUIPullLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs,
                R.styleable.QMUIPullLayout, defStyleAttr, 0);
        mEnabledEdges = array.getInt(R.styleable.QMUIPullLayout_qmui_pull_enable_edge, PUL_EDGE_ALL);
        array.recycle();
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mScroller = new OverScroller(context, QUNITIC_INTERPOLATOR);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        boolean isTargetSet = false;
        int edgesSet = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (lp.isTarget) {
                if (isTargetSet) {
                    throw new RuntimeException(
                            "More than one view in xml are marked by qmui_is_target = true.");
                }
                isTargetSet = true;
                setTargetView(view);
            } else {
                if ((edgesSet & lp.edge) != 0) {
                    String text = "";
                    if (lp.edge == PULL_EDGE_LEFT) {
                        text = "left";
                    } else if (lp.edge == PULL_EDGE_TOP) {
                        text = "top";
                    } else if (lp.edge == PULL_EDGE_RIGHT) {
                        text = "right";
                    } else if (lp.edge == PULL_EDGE_BOTTOM) {
                        text = "bottom";
                    }
                    throw new RuntimeException("More than one view in xml marked by qmui_layout_edge = " + text);
                }
                edgesSet |= lp.edge;
                setActionView(view, lp);
            }
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (mScroller.isFinished()) {
                if(mState == STATE_SETTLING_TO_INIT_OFFSET){
                    mState = STATE_IDLE;
                    return;
                }
                if(mState == STATE_TRIGGERING){
                    return;
                }

                if(mState == STATE_SETTLING_FLING){
                    checkScrollToTargetOffsetOrInitOffset(false);
                    return;
                }

                if(mState == STATE_SETTLING_TO_TRIGGER_OFFSET){
                    mState = STATE_TRIGGERING;
                    if (mLeftPullAction != null && isEdgeEnabled(PULL_EDGE_LEFT)) {
                        if (mScroller.getFinalX() == mLeftPullAction.getTargetTriggerOffset()) {
                            onActionTriggered(mLeftPullAction);
                        }
                    }
                    if (mRightPullAction != null && isEdgeEnabled(PULL_EDGE_RIGHT)) {
                        if (mScroller.getFinalX() == -mRightPullAction.getTargetTriggerOffset()) {
                            onActionTriggered(mRightPullAction);
                        }
                    }

                    if (mTopPullAction != null && isEdgeEnabled(PULL_EDGE_TOP)) {
                        if (mScroller.getFinalY() == mTopPullAction.getTargetTriggerOffset()) {
                            onActionTriggered(mTopPullAction);
                        }
                    }
                    if (mBottomPullAction != null && isEdgeEnabled(PULL_EDGE_BOTTOM)) {
                        if (mScroller.getFinalY() == -mBottomPullAction.getTargetTriggerOffset()) {
                            onActionTriggered(mBottomPullAction);
                        }
                    }
                    setHorOffsetToTargetOffsetHelper(mScroller.getCurrX());
                    setVerOffsetToTargetOffsetHelper(mScroller.getCurrY());
                }
            }else{
                setHorOffsetToTargetOffsetHelper(mScroller.getCurrX());
                setVerOffsetToTargetOffsetHelper(mScroller.getCurrY());
                postInvalidateOnAnimation();
            }
        }
    }

    public void setStopTargetViewFlingImpl(@NonNull StopTargetViewFlingImpl stopTargetViewFlingImpl) {
        mStopTargetViewFlingImpl = stopTargetViewFlingImpl;
    }

    public void setMinScrollDuration(int minScrollDuration) {
        mMinScrollDuration = minScrollDuration;
    }

    public void setTargetView(@NonNull View view) {
        if (view.getParent() != this) {
            throw new RuntimeException("Target already exists other parent view.");
        }
        if (view.getParent() == null) {
            LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            addView(view, lp);
        }
        innerSetTargetView(view);
    }

    private void innerSetTargetView(@NonNull View view) {
        mTargetView = view;
        mTargetOffsetHelper = new QMUIViewOffsetHelper(view);
    }

    public void setActionView(View view, LayoutParams lp) {
        PullActionBuilder builder = new PullActionBuilder(view, lp.edge)
                .canOverPull(lp.canOverPull)
                .pullRate(lp.pullRate)
                .needReceiveFlingFromTargetView(lp.needReceiveFlingFromTarget)
                .receivedFlingFraction(lp.receivedFlingFraction)
                .scrollSpeedPerPixel(lp.scrollSpeedPerPixel)
                .targetTriggerOffset(lp.targetTriggerOffset)
                .triggerUntilScrollToTriggerOffset(lp.triggerUntilScrollToTriggerOffset)
                .scrollToTriggerOffsetAfterTouchUp(lp.scrollToTriggerOffsetAfterTouchUp)
                .actionInitOffset(lp.actionInitOffset);
        view.setLayoutParams(lp);
        setActionView(builder);
    }

    public void setActionView(@NonNull PullActionBuilder builder) {
        if (builder.mActionView.getParent() != this) {
            throw new RuntimeException("Action view already exists other parent view.");
        }
        if (builder.mActionView.getParent() == null) {
            ViewGroup.LayoutParams lp = builder.mActionView.getLayoutParams();
            if (lp == null) {
                lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            addView(builder.mActionView, lp);
        }
        if (builder.mPullEdge == PULL_EDGE_LEFT) {
            mLeftPullAction = builder.build();
        } else if (builder.mPullEdge == PULL_EDGE_TOP) {
            mTopPullAction = builder.build();
        } else if (builder.mPullEdge == PULL_EDGE_RIGHT) {
            mRightPullAction = builder.build();
        } else if (builder.mPullEdge == PULL_EDGE_BOTTOM) {
            mBottomPullAction = builder.build();
        }
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public void setEnabledEdges(int enabledEdges) {
        mEnabledEdges = enabledEdges;
    }

    public boolean isEdgeEnabled(@PullEdge int edge) {
        return (mEnabledEdges & edge) == edge && getPullAction(edge) != null;
    }

    @Nullable
    private PullAction getPullAction(@PullEdge int edge) {
        if (edge == PULL_EDGE_LEFT) {
            return mLeftPullAction;
        } else if (edge == PULL_EDGE_TOP) {
            return mTopPullAction;
        } else if (edge == PULL_EDGE_RIGHT) {
            return mRightPullAction;
        } else if (edge == PULL_EDGE_BOTTOM) {
            return mBottomPullAction;
        }
        return null;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int w = r - l;
        int h = b - t;
        if (mTargetView != null) {
            mTargetView.layout(0, 0, w, h);
            mTargetOffsetHelper.onViewLayout();
        }

        if (mLeftPullAction != null) {
            View view = mLeftPullAction.mActionView;
            int vw = view.getMeasuredWidth(), vh = view.getMeasuredHeight(), vc = (h - vh) / 2;
            view.layout(-vw, vc, 0, vc + vh);
            mLeftPullAction.mViewOffsetHelper.onViewLayout();
        }

        if (mTopPullAction != null) {
            View view = mTopPullAction.mActionView;
            int vw = view.getMeasuredWidth(), vh = view.getMeasuredHeight(), vc = (w - vw) / 2;
            view.layout(vc, -vh, vc + vw, 0);
            mTopPullAction.mViewOffsetHelper.onViewLayout();
        }

        if (mRightPullAction != null) {
            View view = mRightPullAction.mActionView;
            int vw = view.getMeasuredWidth(), vh = view.getMeasuredHeight(), vc = (h - vh) / 2;
            view.layout(w, vc, w + vw, vc + vh);
            mRightPullAction.mViewOffsetHelper.onViewLayout();
        }

        if (mBottomPullAction != null) {
            View view = mBottomPullAction.mActionView;
            int vw = view.getMeasuredWidth(), vh = view.getMeasuredHeight(), vc = (w - vw) / 2;
            view.layout(vc, h, vc + vw, h + vh);
            mBottomPullAction.mViewOffsetHelper.onViewLayout();
        }
    }

    public void setNestedPreFlingVelocityScaleDown(float nestedPreFlingVelocityScaleDown) {
        mNestedPreFlingVelocityScaleDown = nestedPreFlingVelocityScaleDown;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        return mTargetView == target && (axes == ViewCompat.SCROLL_AXIS_HORIZONTAL && (isEdgeEnabled(PULL_EDGE_LEFT) || isEdgeEnabled(PULL_EDGE_RIGHT))) ||
                (axes == ViewCompat.SCROLL_AXIS_VERTICAL && (isEdgeEnabled(PULL_EDGE_TOP) || isEdgeEnabled(PULL_EDGE_BOTTOM)));
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return onStartNestedScroll(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        if(type == ViewCompat.TYPE_TOUCH){
            removeStopTargetFlingRunnable();
            mScroller.abortAnimation();
            mState = STATE_PULLING;
        }
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        onNestedScrollAccepted(child, target, axes, ViewCompat.TYPE_TOUCH);
    }


    @Override
    public void onNestedPreScroll(@NonNull final View target, int dx, int dy, @NonNull int[] consumed, int type) {
        int originDx = dx, originDy = dy;
        dy = checkEdgeTopScrollDown(dy, consumed, type);
        dy = checkEdgeBottomScrollDown(dy, consumed, type);
        dy = checkEdgeTopScrollUp(dy, consumed, type);
        dy = checkEdgeBottomScrollUp(dy, consumed, type);

        dx = checkEdgeLeftScrollRight(dx, consumed, type);
        dx = checkEdgeRightScrollRight(dx, consumed, type);
        dx = checkEdgeLeftScrollLeft(dx, consumed, type);
        dx = checkEdgeRightScrollLeft(dx, consumed, type);

        if(originDx == dx && originDy == dy && mState == STATE_SETTLING_DELIVER){
            checkStopTargetFling(target, dx, dy, type);
        }
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        onNestedPreScroll(target, dx, dy, consumed, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
        int originDxUnconsumed = dxUnconsumed, originDyUnconsumed = dyUnconsumed;
        dyUnconsumed = checkEdgeTopScrollDown(dyUnconsumed, consumed, type);
        dyUnconsumed = checkEdgeBottomScrollDown(dyUnconsumed, consumed, type);
        dyUnconsumed = checkEdgeTopScrollUp(dyUnconsumed, consumed, type);
        dyUnconsumed = checkEdgeBottomScrollUp(dyUnconsumed, consumed, type);

        dxUnconsumed = checkEdgeLeftScrollRight(dxUnconsumed, consumed, type);
        dxUnconsumed = checkEdgeRightScrollRight(dxUnconsumed, consumed, type);
        dxUnconsumed = checkEdgeLeftScrollLeft(dxUnconsumed, consumed, type);
        dxUnconsumed = checkEdgeRightScrollLeft(dxUnconsumed, consumed, type);
        if(dyUnconsumed == originDyUnconsumed && dxUnconsumed == originDxUnconsumed && mState == STATE_SETTLING_DELIVER){
            checkStopTargetFling(target, dxUnconsumed, dyUnconsumed, type);
        }
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, mNestedScrollingV2ConsumedCompat);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        int hOffset = mTargetOffsetHelper.getLeftAndRightOffset();
        int vOffset = mTargetOffsetHelper.getTopAndBottomOffset();

        // if the targetView is RecyclerView and we set OnFlingListener for RecyclerView.
        // then the targetView can not deliver fling consume to NestedScrollParent
        // so we intercept the fling if the target view can not consume the fling.
        if(mLeftPullAction != null && isEdgeEnabled(PULL_EDGE_LEFT)){
            if(velocityX  < 0 && !mTargetView.canScrollHorizontally(-1)){
                mState = STATE_SETTLING_FLING;
                velocityX /= mNestedPreFlingVelocityScaleDown;
                int maxX = mLeftPullAction.isCanOverPull() ? Integer.MAX_VALUE : mLeftPullAction.getTargetTriggerOffset();
                mScroller.fling(hOffset, vOffset, (int) -velocityX, 0, 0,  maxX, vOffset, vOffset);
                postInvalidateOnAnimation();
                return true;
            }else if(velocityX > 0 && hOffset > 0){
                mState = STATE_SETTLING_TO_INIT_OFFSET;
                mScroller.startScroll(hOffset, vOffset, -hOffset, 0, scrollDuration(mLeftPullAction,hOffset));
                postInvalidateOnAnimation();
                return true;
            }
        }

        if(mRightPullAction != null && isEdgeEnabled(PULL_EDGE_RIGHT)){
            if(velocityX > 0 && !mTargetView.canScrollHorizontally(1)){
                mState = STATE_SETTLING_FLING;
                velocityX /= mNestedPreFlingVelocityScaleDown;
                int minX = mRightPullAction.isCanOverPull() ? Integer.MIN_VALUE : -mRightPullAction.getTargetTriggerOffset();
                mScroller.fling(hOffset, vOffset, (int) -velocityX, 0,  minX, 0, vOffset, vOffset);
                postInvalidateOnAnimation();
                return true;
            }else if(velocityX < 0 && hOffset < 0){
                mState = STATE_SETTLING_TO_INIT_OFFSET;
                mScroller.startScroll(hOffset, vOffset, -hOffset, 0, scrollDuration(mRightPullAction, hOffset));
                postInvalidateOnAnimation();
                return true;
            }
        }

        if(mTopPullAction != null && isEdgeEnabled(PULL_EDGE_TOP)){
            if(velocityY  < 0 && !mTargetView.canScrollVertically(-1)){
                mState = STATE_SETTLING_FLING;
                velocityY /= mNestedPreFlingVelocityScaleDown;
                int maxY = mTopPullAction.isCanOverPull() ? Integer.MAX_VALUE : mTopPullAction.getTargetTriggerOffset();
                mScroller.fling(hOffset, vOffset, 0, (int) -velocityY, hOffset,  hOffset, 0, maxY);
                postInvalidateOnAnimation();
                return true;
            }else if(velocityY > 0 && vOffset > 0){
                mState = STATE_SETTLING_TO_INIT_OFFSET;
                mScroller.startScroll(hOffset, vOffset, 0, -vOffset, scrollDuration(mTopPullAction, vOffset));
                postInvalidateOnAnimation();
                return true;
            }
        }

        if(mBottomPullAction != null && isEdgeEnabled(PULL_EDGE_BOTTOM)){
            if(velocityY > 0 && !mTargetView.canScrollVertically(1)){
                mState = STATE_SETTLING_FLING;
                velocityY /= mNestedPreFlingVelocityScaleDown;
                int minY =  mBottomPullAction.isCanOverPull() ? Integer.MIN_VALUE : -mBottomPullAction.getTargetTriggerOffset();
                mScroller.fling(hOffset, vOffset, 0, (int) -velocityY, hOffset, hOffset,  minY, 0);
                postInvalidateOnAnimation();
                return true;
            }else if(velocityY < 0 && vOffset < 0){
                mState = STATE_SETTLING_TO_INIT_OFFSET;
                mScroller.startScroll(hOffset, vOffset, 0, -vOffset, scrollDuration(mBottomPullAction, vOffset));
                postInvalidateOnAnimation();
                return true;
            }
        }
        mState = STATE_SETTLING_DELIVER;
        return super.onNestedPreFling(target, velocityX, velocityY);
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        if(mState == STATE_PULLING){
            checkScrollToTargetOffsetOrInitOffset(false);
        }else if(mState == STATE_SETTLING_DELIVER && type != ViewCompat.TYPE_TOUCH){
            removeStopTargetFlingRunnable();
            checkScrollToTargetOffsetOrInitOffset(false);
        }
    }

    private int scrollDuration(PullAction pullAction, int delta){
        return Math.max(mMinScrollDuration, Math.abs((int) (pullAction.mScrollSpeedPerPixel * delta)));
    }

    private void onActionTriggered(PullAction pullAction) {
        if(pullAction.mIsActionRunning){
            return;
        }
        pullAction.mIsActionRunning = true;
        if(mActionListener != null){
            mActionListener.onActionTriggered(pullAction);
        }
        if(pullAction.mActionView instanceof ActionPullWatcherView){
            ((ActionPullWatcherView)pullAction.mActionView).onActionTriggered();
        }
    }

    public void finishActionRun(@NonNull PullAction pullAction){
        finishActionRun(pullAction, true);
    }

    public void finishActionRun(@NonNull PullAction pullAction, boolean animate){
        if(pullAction != getPullAction(pullAction.mPullEdge)){
            return;
        }
        pullAction.mIsActionRunning = false;
        if(pullAction.mActionView instanceof ActionPullWatcherView){
            ((ActionPullWatcherView)pullAction.mActionView).onActionFinished();
        }
        if(mState == STATE_PULLING){
            return;
        }
        if(!animate){
            mState = STATE_IDLE;
            setVerOffsetToTargetOffsetHelper(0);
            setHorOffsetToTargetOffsetHelper(0);
            return;
        }
        mState = STATE_SETTLING_TO_INIT_OFFSET;
        @PullEdge int pullEdge = pullAction.getPullEdge();
        int vOffset = mTargetOffsetHelper.getTopAndBottomOffset();
        int hOffset = mTargetOffsetHelper.getLeftAndRightOffset();
        if(pullEdge == PULL_EDGE_TOP && mTopPullAction != null && vOffset > 0){
            mScroller.startScroll(hOffset, vOffset, 0, -vOffset, scrollDuration(mTopPullAction, vOffset));
            postInvalidateOnAnimation();
        }else if(pullEdge == PULL_EDGE_BOTTOM && mBottomPullAction != null && vOffset < 0){
            mScroller.startScroll(hOffset, vOffset, 0, -vOffset, scrollDuration(mBottomPullAction, vOffset));
            postInvalidateOnAnimation();
        }else if(pullEdge == PULL_EDGE_LEFT && mLeftPullAction != null && hOffset > 0){
            mScroller.startScroll(hOffset, vOffset, -hOffset, 0, scrollDuration(mLeftPullAction, hOffset));
            postInvalidateOnAnimation();
        }else if(pullEdge == PULL_EDGE_RIGHT && mRightPullAction != null && hOffset < 0){
            mScroller.startScroll(hOffset, vOffset, -hOffset, 0, scrollDuration(mRightPullAction, hOffset));
            postInvalidateOnAnimation();
        }
    }

    private void checkScrollToTargetOffsetOrInitOffset(boolean forceInit) {
        if (mTargetView == null) {
            return;
        }
        mScroller.abortAnimation();
        int hOffset = mTargetOffsetHelper.getLeftAndRightOffset();
        int vOffset = mTargetOffsetHelper.getTopAndBottomOffset();
        int hTarget = 0, vTarget = 0;
        if (mLeftPullAction != null && isEdgeEnabled(PULL_EDGE_LEFT) && hOffset > 0) {
            mState = STATE_SETTLING_TO_INIT_OFFSET;
            if(!forceInit){
                int targetOffset = mLeftPullAction.getTargetTriggerOffset();
                if(hOffset == targetOffset){
                    onActionTriggered(mLeftPullAction);
                    return;
                }
                if(hOffset > targetOffset){
                    if(!mLeftPullAction.mScrollToTriggerOffsetAfterTouchUp){
                        mState = STATE_TRIGGERING;
                        onActionTriggered(mLeftPullAction);
                        return;
                    }
                    if(!mLeftPullAction.mTriggerUntilScrollToTriggerOffset){
                        mState = STATE_TRIGGERING;
                        onActionTriggered(mLeftPullAction);
                    }else{
                        mState = STATE_SETTLING_TO_TRIGGER_OFFSET;
                    }
                    hTarget = targetOffset;
                }
            }
            int dx = hTarget - hOffset;
            mScroller.startScroll(hOffset, vOffset, dx, 0, scrollDuration(mLeftPullAction, dx));
            postInvalidateOnAnimation();
            return;
        }

        if(mRightPullAction != null && isEdgeEnabled(PULL_EDGE_RIGHT) && hOffset < 0){
            mState = STATE_SETTLING_TO_INIT_OFFSET;
            if(!forceInit){
                int targetOffset = mRightPullAction.getTargetTriggerOffset();
                if (hOffset == -targetOffset) {
                    mState = STATE_TRIGGERING;
                    onActionTriggered(mRightPullAction);
                    return;
                }
                if(hOffset < -targetOffset){
                    if(!mRightPullAction.mScrollToTriggerOffsetAfterTouchUp){
                        mState = STATE_TRIGGERING;
                        onActionTriggered(mRightPullAction);
                        return;
                    }

                    if(!mRightPullAction.mTriggerUntilScrollToTriggerOffset){
                        mState = STATE_TRIGGERING;
                        onActionTriggered(mRightPullAction);
                    }else{
                        mState = STATE_SETTLING_TO_TRIGGER_OFFSET;
                    }
                    hTarget = -targetOffset;
                }
            }
            int dx = hTarget - hOffset;
            mScroller.startScroll(hOffset, vOffset, dx, 0,scrollDuration(mRightPullAction, dx));
            postInvalidateOnAnimation();
            return;
        }

        if (mTopPullAction != null && isEdgeEnabled(PULL_EDGE_TOP) && vOffset > 0) {
            mState = STATE_SETTLING_TO_INIT_OFFSET;
            if(!forceInit){
                int targetOffset = mTopPullAction.getTargetTriggerOffset();
                if(vOffset == targetOffset){
                    mState = STATE_TRIGGERING;
                    onActionTriggered(mTopPullAction);
                    return;
                }
                if(vOffset > targetOffset){
                    if(!mTopPullAction.mScrollToTriggerOffsetAfterTouchUp){
                        mState = STATE_TRIGGERING;
                        onActionTriggered(mTopPullAction);
                        return;
                    }

                    if(!mTopPullAction.mTriggerUntilScrollToTriggerOffset){
                        mState = STATE_TRIGGERING;
                        onActionTriggered(mTopPullAction);
                    }else{
                        mState = STATE_SETTLING_TO_TRIGGER_OFFSET;
                    }
                    vTarget = targetOffset;
                }
            }
            int dy = vTarget - vOffset;
            mScroller.startScroll(hOffset, vOffset, hOffset, dy, scrollDuration(mTopPullAction, dy));
            postInvalidateOnAnimation();
            return;
        }

        if (mBottomPullAction != null && isEdgeEnabled(PULL_EDGE_BOTTOM) && vOffset < 0) {
            mState = STATE_SETTLING_TO_INIT_OFFSET;
            if(!forceInit){
                int targetOffset = mBottomPullAction.getTargetTriggerOffset();
                if(vOffset == -targetOffset){
                    onActionTriggered(mBottomPullAction);
                    return;
                }
                if(vOffset < -targetOffset){
                    if(!mBottomPullAction.mScrollToTriggerOffsetAfterTouchUp){
                        mState = STATE_TRIGGERING;
                        onActionTriggered(mBottomPullAction);
                        return;
                    }

                    if(!mBottomPullAction.mTriggerUntilScrollToTriggerOffset){
                        mState = STATE_TRIGGERING;
                        onActionTriggered(mBottomPullAction);
                    }else{
                        mState = STATE_SETTLING_TO_TRIGGER_OFFSET;
                    }
                    vTarget = -targetOffset;
                }
            }
            int dy = vTarget - vOffset;
            mScroller.startScroll(hOffset, vOffset, hOffset, dy, scrollDuration(mBottomPullAction, dy));
            postInvalidateOnAnimation();
            return;
        }

        mState = STATE_IDLE;
    }

    private void removeStopTargetFlingRunnable() {
        if (mStopTargetFlingRunnable != null) {
            removeCallbacks(mStopTargetFlingRunnable);
            mStopTargetFlingRunnable = null;
        }
    }

    private void checkStopTargetFling(final View targetView, int dx, int dy, int type) {
        if (mStopTargetFlingRunnable != null || type == ViewCompat.TYPE_TOUCH) {
            return;
        }
        if ((dy < 0 && !mTargetView.canScrollVertically(-1)) ||
                (dy > 0 && !mTargetView.canScrollVertically(1)) ||
                (dx < 0 && !mTargetView.canScrollHorizontally(-1)) ||
                (dx > 0 && !mTargetView.canScrollHorizontally(1))) {
            mStopTargetFlingRunnable = new Runnable() {
                @Override
                public void run() {
                    mStopTargetViewFlingImpl.stopFling(targetView);
                    mStopTargetFlingRunnable = null;
                    checkScrollToTargetOffsetOrInitOffset(false);
                }
            };
            post(mStopTargetFlingRunnable);
        }
    }

    private void setHorOffsetToTargetOffsetHelper(int hOffset) {
        mTargetOffsetHelper.setLeftAndRightOffset(hOffset);
        onTargetViewLeftAndRightOffsetChanged(hOffset);
        if (mLeftPullAction != null) {
            mLeftPullAction.onTargetMoved(hOffset);
            if(mLeftPullAction.mActionView instanceof ActionPullWatcherView){
                ((ActionPullWatcherView)mLeftPullAction.mActionView).onPull(mLeftPullAction, hOffset);
            }

        }
        if (mRightPullAction != null) {
            mRightPullAction.onTargetMoved(-hOffset);
            if(mRightPullAction.mActionView instanceof ActionPullWatcherView){
                ((ActionPullWatcherView)mRightPullAction.mActionView).onPull(mRightPullAction, -hOffset);
            }
        }
    }

    private void setVerOffsetToTargetOffsetHelper(int vOffset) {
        mTargetOffsetHelper.setTopAndBottomOffset(vOffset);
        onTargetViewTopAndBottomOffsetChanged(vOffset);
        if (mTopPullAction != null) {
            mTopPullAction.onTargetMoved(vOffset);
            if(mTopPullAction.mActionView instanceof ActionPullWatcherView){
                ((ActionPullWatcherView)mTopPullAction.mActionView).onPull(mTopPullAction, vOffset);
            }

        }
        if (mBottomPullAction != null) {
            mBottomPullAction.onTargetMoved(-vOffset);
            if(mBottomPullAction.mActionView instanceof ActionPullWatcherView){
                ((ActionPullWatcherView)mBottomPullAction.mActionView).onPull(mBottomPullAction, -vOffset);
            }
        }
    }

    protected void onTargetViewTopAndBottomOffsetChanged(int vOffset){

    }

    protected void onTargetViewLeftAndRightOffsetChanged(int hOffset){

    }

    private int checkEdgeTopScrollDown(int dy, int[] consumed, int type) {
        int vOffset = mTargetOffsetHelper.getTopAndBottomOffset();
        if (dy > 0 && isEdgeEnabled(PULL_EDGE_TOP) && vOffset > 0) {
            float pullRate = type == ViewCompat.TYPE_TOUCH ?  mTopPullAction.getPullRate() : 1f;
            int ry = (int) (dy * pullRate);
            if(ry == 0){
                return dy;
            }
            if (vOffset >= ry) {
                consumed[1] += dy;
                vOffset -= ry;
                dy = 0;
            } else {
                int yConsumed = (int) (vOffset / pullRate);
                consumed[1] += yConsumed;
                dy -= yConsumed;
                vOffset = 0;
            }
            setVerOffsetToTargetOffsetHelper(vOffset);
        }
        return dy;
    }

    private int checkEdgeTopScrollUp(int dy, int[] consumed, int type) {
        if (dy < 0 && isEdgeEnabled(PULL_EDGE_TOP) && !mTargetView.canScrollVertically(-1) &&
                (type == ViewCompat.TYPE_TOUCH || mTopPullAction.mNeedReceiveFlingFromTargetView)) {
            int vOffset = mTargetOffsetHelper.getTopAndBottomOffset();
            float pullRate = type == ViewCompat.TYPE_TOUCH ? mTopPullAction.getPullRate(): mTopPullAction.getFlingRate(vOffset);
            int ry = (int) (dy * pullRate);
            if(ry == 0){
                return dy;
            }
            if (mTopPullAction.mCanOverPull || -ry <= mTopPullAction.getTargetTriggerOffset() - vOffset) {
                vOffset -= ry;
                consumed[1] += dy;
                dy = 0;
            } else {
                int yConsumed = (int) ((vOffset - mTopPullAction.getTargetTriggerOffset()) / pullRate);
                consumed[1] += yConsumed;
                dy -= yConsumed;
                vOffset = mBottomPullAction.getTargetTriggerOffset();
            }
            setVerOffsetToTargetOffsetHelper(vOffset);
        }
        return dy;
    }

    private int checkEdgeBottomScrollDown(int dy, int[] consumed, int type) {
        if (dy > 0 && isEdgeEnabled(PULL_EDGE_BOTTOM) && !mTargetView.canScrollVertically(1) &&
                (type == ViewCompat.TYPE_TOUCH || mBottomPullAction.mNeedReceiveFlingFromTargetView)) {
            int vOffset = mTargetOffsetHelper.getTopAndBottomOffset();
            float pullRate =type == ViewCompat.TYPE_TOUCH ? mBottomPullAction.getPullRate(): mBottomPullAction.getFlingRate(-vOffset);
            int ry = (int) (dy * pullRate);
            if(ry == 0){
                return dy;
            }
            if (mBottomPullAction.mCanOverPull || vOffset - ry >= -mBottomPullAction.getTargetTriggerOffset()) {
                vOffset -= ry;
                consumed[1] += dy;
                dy = 0;
            } else {
                int yConsumed = (int) ((-mBottomPullAction.getTargetTriggerOffset() - vOffset) / pullRate);
                consumed[1] += yConsumed;
                dy -= yConsumed;
                vOffset = -mBottomPullAction.getTargetTriggerOffset();
            }
            setVerOffsetToTargetOffsetHelper(vOffset);
        }
        return dy;
    }

    private int checkEdgeBottomScrollUp(int dy, int[] consumed, int type) {
        int vOffset = mTargetOffsetHelper.getTopAndBottomOffset();
        if (dy < 0 && isEdgeEnabled(PULL_EDGE_BOTTOM) && vOffset < 0) {
            float pullRate = type == ViewCompat.TYPE_TOUCH ?  mBottomPullAction.getPullRate() : 1f;
            int ry = (int) (dy * pullRate);
            if(ry == 0){
                return dy;
            }
            if (vOffset <= ry) {
                consumed[1] += dy;
                vOffset -= ry;
                dy = 0;
            } else {
                int yConsumed = (int) (vOffset / pullRate);
                consumed[1] += yConsumed;
                dy -= yConsumed;
                vOffset = 0;
            }
            setVerOffsetToTargetOffsetHelper(vOffset);
        }
        return dy;
    }

    private int checkEdgeLeftScrollRight(int dx, int[] consumed, int type) {
        int hOffset = mTargetOffsetHelper.getLeftAndRightOffset();
        if (dx > 0 && isEdgeEnabled(PULL_EDGE_LEFT) && hOffset > 0) {
            float pullRate = type == ViewCompat.TYPE_TOUCH ? mLeftPullAction.getPullRate(): 1f;
            int rx = (int) (dx * pullRate);
            if(rx == 0){
                return dx;
            }
            if (hOffset >= rx) {
                consumed[0] += dx;
                hOffset -= rx;
                dx = 0;
            } else {
                int xConsumed = (int) (hOffset / pullRate);
                consumed[0] += xConsumed;
                dx -= xConsumed;
                hOffset = 0;
            }
            setHorOffsetToTargetOffsetHelper(hOffset);
        }
        return dx;
    }

    private int checkEdgeLeftScrollLeft(int dx, int[] consumed, int type) {
        int hOffset = mTargetOffsetHelper.getLeftAndRightOffset();
        if (dx < 0 && isEdgeEnabled(PULL_EDGE_LEFT) && !mTargetView.canScrollHorizontally(-1) &&
                (type == ViewCompat.TYPE_TOUCH || mLeftPullAction.mNeedReceiveFlingFromTargetView)) {
            float pullRate =type == ViewCompat.TYPE_TOUCH ? mLeftPullAction.getPullRate(): mLeftPullAction.getFlingRate(hOffset);
            int rx = (int) (dx * pullRate);
            if(rx == 0){
                return dx;
            }
            if (mLeftPullAction.mCanOverPull || -rx <= mLeftPullAction.getTargetTriggerOffset() - hOffset) {
                hOffset -= rx;
                consumed[0] += dx;
                dx = 0;
            } else {
                int xConsumed = (int) ((hOffset - mLeftPullAction.getTargetTriggerOffset()) / pullRate);
                consumed[0] += xConsumed;
                dx -= xConsumed;
                hOffset = mLeftPullAction.getTargetTriggerOffset();
            }
            setHorOffsetToTargetOffsetHelper(hOffset);
        }
        return dx;
    }

    private int checkEdgeRightScrollRight(int dx, int[] consumed, int type) {
        if (dx > 0 && isEdgeEnabled(PULL_EDGE_RIGHT) && !mTargetView.canScrollHorizontally(1) &&
                (type == ViewCompat.TYPE_TOUCH || mRightPullAction.mNeedReceiveFlingFromTargetView)) {
            int hOffset = mTargetOffsetHelper.getLeftAndRightOffset();
            float pullRate = type == ViewCompat.TYPE_TOUCH ? mRightPullAction.getPullRate(): mRightPullAction.getFlingRate(-hOffset);
            int rx = (int) (dx * pullRate);
            if(rx == 0){
                return dx;
            }
            if (mRightPullAction.mCanOverPull || hOffset - rx >= -mRightPullAction.getTargetTriggerOffset()) {
                hOffset -= rx;
                consumed[0] += dx;
                dx = 0;
            } else {
                int xConsumed = (int) ((-mRightPullAction.getTargetTriggerOffset() - hOffset) / pullRate);
                consumed[0] += xConsumed;
                dx -= xConsumed;
                hOffset = -mRightPullAction.getTargetTriggerOffset();
            }
            setHorOffsetToTargetOffsetHelper(hOffset);
        }
        return dx;
    }

    private int checkEdgeRightScrollLeft(int dx, int[] consumed, int type) {
        int hOffset = mTargetOffsetHelper.getLeftAndRightOffset();
        if (dx < 0 && isEdgeEnabled(PULL_EDGE_RIGHT) && hOffset < 0) {
            float pullRate = type == ViewCompat.TYPE_TOUCH ? mRightPullAction.getPullRate(): 1f;
            int rx = (int) (dx * pullRate);
            if(rx == 0){
                return dx;
            }
            if (hOffset <= dx) {
                consumed[0] += dx;
                hOffset -= rx;
                dx = 0;
            } else {
                int xConsumed = (int) (hOffset / pullRate);
                consumed[0] += xConsumed;
                dx -= xConsumed;
                hOffset = 0;
            }
            setHorOffsetToTargetOffsetHelper(hOffset);
        }
        return dx;
    }

    @Override
    protected FrameLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        return new LayoutParams(lp);
    }

    @Override
    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected FrameLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams && super.checkLayoutParams(p);
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {
        public boolean isTarget = false;
        public int edge = PULL_EDGE_TOP;
        public int targetTriggerOffset = ViewGroup.LayoutParams.WRAP_CONTENT;
        public boolean canOverPull = false;
        public float pullRate = DEFAULT_PULL_RATE;
        public boolean needReceiveFlingFromTarget = true;
        public float receivedFlingFraction = DEFAULT_FLING_FRACTION;
        public int actionInitOffset = 0;
        public float scrollSpeedPerPixel = DEFAULT_SCROLL_SPEED_PER_PIXEL;
        public boolean triggerUntilScrollToTriggerOffset = false;
        public boolean scrollToTriggerOffsetAfterTouchUp = true;


        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            final TypedArray a = c.obtainStyledAttributes(attrs,
                    R.styleable.QMUIPullLayout_Layout);
            isTarget = a.getBoolean(R.styleable.QMUIPullLayout_Layout_qmui_is_target, false);
            if (!isTarget) {
                edge = a.getInteger(R.styleable.QMUIPullLayout_Layout_qmui_pull_edge, PULL_EDGE_TOP);
                try {
                    targetTriggerOffset = a.getDimensionPixelSize(
                            R.styleable.QMUIPullLayout_Layout_qmui_target_view_trigger_offset,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                } catch (Exception ignore) {
                    int intValue = a.getInt(R.styleable.QMUIPullLayout_Layout_qmui_target_view_trigger_offset, ViewGroup.LayoutParams.WRAP_CONTENT);
                    if (intValue == ViewGroup.LayoutParams.WRAP_CONTENT) {
                        targetTriggerOffset = ViewGroup.LayoutParams.WRAP_CONTENT;
                    }
                }

                canOverPull = a.getBoolean(
                        R.styleable.QMUIPullLayout_Layout_qmui_can_over_pull, false);
                pullRate = a.getFloat(
                        R.styleable.QMUIPullLayout_Layout_qmui_pull_rate, pullRate);
                needReceiveFlingFromTarget = a.getBoolean(
                        R.styleable.QMUIPullLayout_Layout_qmui_need_receive_fling_from_target_view, true);
                receivedFlingFraction = a.getFloat(
                        R.styleable.QMUIPullLayout_Layout_qmui_received_fling_fraction, receivedFlingFraction);
                actionInitOffset = a.getDimensionPixelSize(R.styleable.QMUIPullLayout_Layout_qmui_action_view_init_offset, 0);
                scrollSpeedPerPixel = a.getFloat(R.styleable.QMUIPullLayout_Layout_qmui_scroll_speed_per_pixel, scrollSpeedPerPixel);
                triggerUntilScrollToTriggerOffset = a.getBoolean(R.styleable.QMUIPullLayout_Layout_qmui_trigger_until_scroll_to_trigger_offset, false);
                scrollToTriggerOffsetAfterTouchUp = a.getBoolean(R.styleable.QMUIPullLayout_Layout_qmui_scroll_to_trigger_offset_after_touch_up, true);
            }
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }
    }

    public final static class PullAction {
        @NonNull
        private final View mActionView;
        private final int mTargetTriggerOffset;
        private final boolean mCanOverPull;
        private final float mPullRate;
        private final float mReceivedFlingFraction;
        private final int mActionInitOffset;
        private final int mPullEdge;
        private final float mScrollSpeedPerPixel;
        private final boolean mNeedReceiveFlingFromTargetView;
        private final boolean mTriggerUntilScrollToTriggerOffset;
        private final boolean mScrollToTriggerOffsetAfterTouchUp;
        private final QMUIViewOffsetHelper mViewOffsetHelper;
        private final ActionViewOffsetCalculator mActionViewOffsetCalculator;

        private boolean mIsActionRunning = false;

        PullAction(@NonNull View actionView,
                   int targetOffset,
                   boolean isTargetCanOverPull,
                   float targetPullRate,
                   int actionInitOffset,
                   int pullEdge,
                   float scrollSpeedPerPixel,
                   boolean needReceiveFlingFromTargetView,
                   float receivedFlingFraction,
                   boolean triggerUntilScrollToTriggerOffset,
                   boolean scrollToTriggerOffsetAfterTouchUp,
                   ActionViewOffsetCalculator calculator) {
            mActionView = actionView;
            mTargetTriggerOffset = targetOffset;
            mCanOverPull = isTargetCanOverPull;
            mPullRate = targetPullRate;
            mNeedReceiveFlingFromTargetView = needReceiveFlingFromTargetView;
            mReceivedFlingFraction = receivedFlingFraction;
            mActionInitOffset = actionInitOffset;
            mScrollSpeedPerPixel = scrollSpeedPerPixel;
            mPullEdge = pullEdge;
            mTriggerUntilScrollToTriggerOffset = triggerUntilScrollToTriggerOffset;
            mScrollToTriggerOffsetAfterTouchUp = scrollToTriggerOffsetAfterTouchUp;
            mActionViewOffsetCalculator = calculator;

            mViewOffsetHelper = new QMUIViewOffsetHelper(actionView);
            updateOffset(actionInitOffset);
        }

        public int getActionPullSize() {
            if (mPullEdge == PULL_EDGE_TOP || mPullEdge == PULL_EDGE_BOTTOM) {
                return mActionView.getHeight();
            }
            return mActionView.getWidth();
        }

        public int getActionInitOffset() {
            return mActionInitOffset;
        }

        public int getTargetTriggerOffset() {
            if (mTargetTriggerOffset == ViewGroup.LayoutParams.WRAP_CONTENT) {
                return getActionPullSize() - getActionInitOffset() * 2;
            }
            return mTargetTriggerOffset;
        }

        public float getScrollSpeedPerPixel() {
            return mScrollSpeedPerPixel;
        }

        public float getPullRate() {
            return mPullRate;
        }

        public boolean isNeedReceiveFlingFromTargetView() {
            return mNeedReceiveFlingFromTargetView;
        }

        public boolean isScrollToTriggerOffsetAfterTouchUp() {
            return mScrollToTriggerOffsetAfterTouchUp;
        }

        public boolean isTriggerUntilScrollToTriggerOffset() {
            return mTriggerUntilScrollToTriggerOffset;
        }

        public float getFlingRate(int currentTargetOffset){
            return Math.min(mPullRate, Math.max(mPullRate - (currentTargetOffset - getTargetTriggerOffset()) * mReceivedFlingFraction, 0));
        }

        public boolean isCanOverPull() {
            return mCanOverPull;
        }

        public int getPullEdge() {
            return mPullEdge;
        }

        void updateOffset(int offset) {
            if (mPullEdge == PULL_EDGE_LEFT) {
                mViewOffsetHelper.setLeftAndRightOffset(offset);
            } else if (mPullEdge == PULL_EDGE_TOP) {
                mViewOffsetHelper.setTopAndBottomOffset(offset);
            } else if (mPullEdge == PULL_EDGE_RIGHT) {
                mViewOffsetHelper.setLeftAndRightOffset(-offset);
            } else {
                mViewOffsetHelper.setTopAndBottomOffset(-offset);
            }
        }

        void onTargetMoved(int targetOffset) {
            updateOffset(
                    mActionViewOffsetCalculator.calculateOffset(this, targetOffset));
        }
    }

    public static class PullActionBuilder {
        @NonNull
        private final View mActionView;
        private int mTargetTriggerOffset = ViewGroup.LayoutParams.WRAP_CONTENT;
        private boolean mCanOverPull;
        private float mPullRate = DEFAULT_PULL_RATE;
        private boolean mNeedReceiveFlingFromTargetView = true;
        private float mReceivedFlingFraction = DEFAULT_FLING_FRACTION;
        private int mActionInitOffset;
        private float mScrollSpeedPerPixel = DEFAULT_SCROLL_SPEED_PER_PIXEL;
        @PullEdge
        private int mPullEdge;
        private ActionViewOffsetCalculator mActionViewOffsetCalculator;
        private boolean mTriggerUntilScrollToTriggerOffset = false;
        private boolean mScrollToTriggerOffsetAfterTouchUp = true;

        public PullActionBuilder(@NonNull View actionView, @PullEdge int pullEdge) {
            mActionView = actionView;
            mPullEdge = pullEdge;
        }

        public PullActionBuilder triggerUntilScrollToTriggerOffset(boolean triggerUntilScrollToTriggerOffset){
            mTriggerUntilScrollToTriggerOffset = triggerUntilScrollToTriggerOffset;
            return this;
        }

        public PullActionBuilder scrollToTriggerOffsetAfterTouchUp(boolean scrollToTriggerOffsetAfterTouchUp){
            mScrollToTriggerOffsetAfterTouchUp = scrollToTriggerOffsetAfterTouchUp;
            return this;
        }

        public PullActionBuilder targetTriggerOffset(int offset) {
            mTargetTriggerOffset = offset;
            return this;
        }

        public PullActionBuilder canOverPull(boolean canOverPull) {
            mCanOverPull = canOverPull;
            return this;
        }

        public PullActionBuilder receivedFlingFraction(float fraction) {
            mReceivedFlingFraction = fraction;
            return this;
        }

        public PullActionBuilder needReceiveFlingFromTargetView(boolean needReceive) {
            mNeedReceiveFlingFromTargetView = needReceive;
            return this;
        }

        public PullActionBuilder pullRate(float rate){
            mPullRate = rate;
            return this;
        }

        public PullActionBuilder scrollSpeedPerPixel(float scrollSpeedPerPixel){
            mScrollSpeedPerPixel = scrollSpeedPerPixel;
            return this;
        }

        public PullActionBuilder actionInitOffset(int initOffset) {
            mActionInitOffset = initOffset;
            return this;
        }

        public PullActionBuilder actionViewOffsetCalculator(ActionViewOffsetCalculator calculator) {
            mActionViewOffsetCalculator = calculator;
            return this;
        }


        PullAction build() {
            if (mActionViewOffsetCalculator == null) {
                mActionViewOffsetCalculator = new QMUIAlwaysFollowOffsetCalculator();
            }
            return new PullAction(mActionView,
                    mTargetTriggerOffset,
                    mCanOverPull,
                    mPullRate,
                    mActionInitOffset,
                    mPullEdge,
                    mScrollSpeedPerPixel,
                    mNeedReceiveFlingFromTargetView,
                    mReceivedFlingFraction,
                    mTriggerUntilScrollToTriggerOffset,
                    mScrollToTriggerOffsetAfterTouchUp,
                    mActionViewOffsetCalculator);
        }
    }

    public interface ActionViewOffsetCalculator {
        int calculateOffset(PullAction pullAction, int targetOffset);
    }

    public interface  ActionPullWatcherView {
        void onPull(PullAction pullAction, int currentTargetOffset);
        void onActionTriggered();
        void onActionFinished();
    }

    public interface StopTargetViewFlingImpl {
        void stopFling(View view);
    }

    public static class DefaultStopTargetViewFlingImpl implements StopTargetViewFlingImpl {

        private static DefaultStopTargetViewFlingImpl sInstance;

        public static DefaultStopTargetViewFlingImpl getInstance() {
            if (sInstance == null) {
                sInstance = new DefaultStopTargetViewFlingImpl();
            }
            return sInstance;
        }

        private DefaultStopTargetViewFlingImpl() {

        }

        @Override
        public void stopFling(View view) {
            if (view instanceof RecyclerView) {
                ((RecyclerView) view).stopScroll();
            }
        }
    }

    public interface ActionListener {
        void onActionTriggered(@NonNull PullAction pullAction);
    }
}
