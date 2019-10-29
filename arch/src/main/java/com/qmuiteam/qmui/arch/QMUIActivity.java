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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;

import static com.qmuiteam.qmui.arch.SwipeBackLayout.DRAG_DIRECTION_BOTTOM_TO_TOP;
import static com.qmuiteam.qmui.arch.SwipeBackLayout.DRAG_DIRECTION_LEFT_TO_RIGHT;
import static com.qmuiteam.qmui.arch.SwipeBackLayout.DRAG_DIRECTION_RIGHT_TO_LEFT;
import static com.qmuiteam.qmui.arch.SwipeBackLayout.DRAG_DIRECTION_TOP_TO_BOTTOM;
import static com.qmuiteam.qmui.arch.SwipeBackLayout.EDGE_BOTTOM;
import static com.qmuiteam.qmui.arch.SwipeBackLayout.EDGE_LEFT;
import static com.qmuiteam.qmui.arch.SwipeBackLayout.EDGE_RIGHT;
import static com.qmuiteam.qmui.arch.SwipeBackLayout.EDGE_TOP;

public class QMUIActivity extends InnerBaseActivity {
    private static final String TAG = "QMUIActivity";
    private SwipeBackLayout.ListenerRemover mListenerRemover;
    private SwipeBackgroundView mSwipeBackgroundView;
    private boolean mIsInSwipeBack = false;

    private SwipeBackLayout.SwipeListener mSwipeListener = new SwipeBackLayout.SwipeListener() {

        @Override
        public void onScrollStateChange(int state, float scrollPercent) {
            Log.i(TAG, "SwipeListener:onScrollStateChange: state = " + state + " ;scrollPercent = " + scrollPercent);
            mIsInSwipeBack = state != SwipeBackLayout.STATE_IDLE;
            if (state == SwipeBackLayout.STATE_IDLE) {
                if (mSwipeBackgroundView != null) {
                    if (scrollPercent <= 0.0F) {
                        mSwipeBackgroundView.unBind();
                        mSwipeBackgroundView = null;
                    } else if (scrollPercent >= 1.0F) {
                        // unBind mSwipeBackgroundView until onDestroy
                        finish();
                        int exitAnim = mSwipeBackgroundView.hasChildWindow() ?
                                R.anim.swipe_back_exit_still : R.anim.swipe_back_exit;
                        overridePendingTransition(R.anim.swipe_back_enter, exitAnim);
                    }
                }
            }
        }

        @Override
        public void onScroll(int dragDirection, int movingEdge, float scrollPercent) {
            if (mSwipeBackgroundView != null) {
                scrollPercent = Math.max(0f, Math.min(1f, scrollPercent));
                int targetOffset = (int) (Math.abs(backViewInitOffset(
                        QMUIActivity.this, dragDirection, movingEdge)) * (1 - scrollPercent));
                SwipeBackLayout.offsetInSwipeBack(mSwipeBackgroundView, movingEdge, targetOffset);
            }
        }

        @Override
        public void onSwipeBackBegin(int dragDirection, int moveEdge) {
            Log.i(TAG, "SwipeListener:onSwipeBackBegin: moveEdge = " + moveEdge);
            onDragStart();
            ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
            if (decorView != null) {
                Activity prevActivity = QMUISwipeBackActivityManager.getInstance()
                        .getPenultimateActivity(QMUIActivity.this);
                if (decorView.getChildAt(0) instanceof SwipeBackgroundView) {
                    mSwipeBackgroundView = (SwipeBackgroundView) decorView.getChildAt(0);
                } else {
                    mSwipeBackgroundView = new SwipeBackgroundView(QMUIActivity.this);
                    decorView.addView(mSwipeBackgroundView, 0, new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                }
                mSwipeBackgroundView.bind(prevActivity,
                        QMUIActivity.this, restoreSubWindowWhenDragBack());
                SwipeBackLayout.offsetInSwipeBack(mSwipeBackgroundView, moveEdge,
                        Math.abs(backViewInitOffset(decorView.getContext(), dragDirection, moveEdge)));
            }
        }

        @Override
        public void onScrollOverThreshold() {
            Log.i(TAG, "SwipeListener:onEdgeTouch:onScrollOverThreshold");
        }
    };
    private SwipeBackLayout.Callback mSwipeCallback = new SwipeBackLayout.Callback() {
        @Override
        public boolean canSwipeBack(SwipeBackLayout layout, int dragDirection, int moveEdge) {
            return QMUISwipeBackActivityManager.getInstance().canSwipeBack() && canDragBack();
        }

        @Override
        public boolean shouldBeginDrag(SwipeBackLayout swipeBackLayout,
                                       float downX, float downY, int direction) {
            return QMUIActivity.this.shouldBeginDrag(swipeBackLayout, downX, downY, direction);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(newSwipeBackLayout(view));
    }

    @Override
    public void setContentView(int layoutResID) {
        SwipeBackLayout swipeBackLayout = SwipeBackLayout.wrap(this,
                layoutResID, dragBackDirection(), dragViewMoveAction(), mSwipeCallback);
        if (translucentFull()) {
            swipeBackLayout.getContentView().setFitsSystemWindows(false);
        } else {
            swipeBackLayout.getContentView().setFitsSystemWindows(true);
        }
        mListenerRemover = swipeBackLayout.addSwipeListener(mSwipeListener);
        super.setContentView(swipeBackLayout);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(newSwipeBackLayout(view), params);
    }

    private View newSwipeBackLayout(View view) {
        if (translucentFull()) {
            view.setFitsSystemWindows(false);
        } else {
            view.setFitsSystemWindows(true);
        }
        final SwipeBackLayout swipeBackLayout = SwipeBackLayout.wrap(
                view, dragBackDirection(), dragViewMoveAction(), mSwipeCallback);
        mListenerRemover = swipeBackLayout.addSwipeListener(mSwipeListener);
        return swipeBackLayout;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListenerRemover != null) {
            mListenerRemover.remove();
        }
        if (mSwipeBackgroundView != null) {
            mSwipeBackgroundView.unBind();
            mSwipeBackgroundView = null;
        }
    }

    /**
     * final this method, if need override this method, use doOnBackPressed as an alternative
     */
    @Override
    public final void onBackPressed() {
        if (!mIsInSwipeBack) {
            doOnBackPressed();
        }
    }

    protected void doOnBackPressed() {
        super.onBackPressed();
    }

    public boolean isInSwipeBack() {
        return mIsInSwipeBack;
    }

    /**
     * disable or enable drag back
     *
     * @return if true open dragBack, otherwise close dragBack
     * @deprecated Use {@link #canDragBack(Context, int, int)}
     */
    @Deprecated
    protected boolean canDragBack() {
        return true;
    }


    protected boolean canDragBack(Context context, int dragDirection, int moveEdge) {
        return canDragBack();
    }

    /**
     * @return the init offset for backView for Parallax scrolling
     * @deprecated Use {@link #backViewInitOffset(Context, int, int)}
     */
    @Deprecated
    protected int backViewInitOffset() {
        return 0;
    }

    protected int backViewInitOffset(Context context, int dragDirection, int moveEdge) {
        return backViewInitOffset();
    }

    protected boolean shouldBeginDrag(SwipeBackLayout swipeBackLayout,
                                      float downX, float downY, int dragDirection){
        int edgeSize = QMUIDisplayHelper.dp2px(swipeBackLayout.getContext(), 20);
        if(dragDirection == DRAG_DIRECTION_LEFT_TO_RIGHT){
            return downX < edgeSize;
        }else if(dragDirection == DRAG_DIRECTION_RIGHT_TO_LEFT){
            return downX > swipeBackLayout.getWidth() - edgeSize;
        }else if(dragDirection == DRAG_DIRECTION_TOP_TO_BOTTOM){
            return downY < edgeSize;
        }else if(dragDirection == DRAG_DIRECTION_BOTTOM_TO_TOP){
            return downY > swipeBackLayout.getHeight() - edgeSize;
        }
        return true;
    }

    /**
     * called when drag back started.
     */
    protected void onDragStart() {

    }


    /**
     * @return
     * @deprecated Use {@link #dragBackDirection()}
     */
    @Deprecated
    protected int dragBackEdge() {
        return EDGE_LEFT;
    }

    protected int dragBackDirection() {
        int oldEdge = dragBackEdge();
        if (oldEdge == EDGE_RIGHT) {
            return SwipeBackLayout.DRAG_DIRECTION_RIGHT_TO_LEFT;
        } else if (oldEdge == EDGE_TOP) {
            return SwipeBackLayout.DRAG_DIRECTION_TOP_TO_BOTTOM;
        } else if (oldEdge == EDGE_BOTTOM) {
            return SwipeBackLayout.DRAG_DIRECTION_BOTTOM_TO_TOP;
        }
        return SwipeBackLayout.DRAG_DIRECTION_LEFT_TO_RIGHT;
    }

    protected SwipeBackLayout.ViewMoveAction dragViewMoveAction() {
        return SwipeBackLayout.MOVE_VIEW_AUTO;
    }

    /**
     * Immersive processing
     *
     * @return if true, the area under status bar belongs to content; otherwise it belongs to padding
     */
    protected boolean translucentFull() {
        return false;
    }

    /**
     * restore sub window(e.g dialog) when drag back to previous activity
     *
     * @return
     */
    protected boolean restoreSubWindowWhenDragBack() {
        return true;
    }

    /**
     * When finishing last activity, let activity have a chance to start a new Activity
     *
     * @return Intent to start a new Activity
     */

    public Intent onLastActivityFinish() {
        return null;
    }

    @Override
    public void finish() {
        if (!QMUISwipeBackActivityManager.getInstance().canSwipeBack()) {
            Intent intent = onLastActivityFinish();
            if (intent != null) {
                startActivity(intent);
            }
        }
        super.finish();
    }
}
