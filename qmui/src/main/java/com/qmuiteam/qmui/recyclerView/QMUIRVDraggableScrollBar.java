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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.skin.IQMUISkinHandlerDecoration;
import com.qmuiteam.qmui.skin.QMUISkinHelper;
import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionLayout;

import org.jetbrains.annotations.NotNull;

public class QMUIRVDraggableScrollBar extends RecyclerView.ItemDecoration implements IQMUISkinHandlerDecoration, QMUIStickySectionLayout.DrawDecoration {
    private int[] STATE_PRESSED = new int[]{android.R.attr.state_pressed};
    private int[] STATE_NORMAL = new int[]{};
    private static final long DEFAULT_KEE_SHOW_DURATION = 800L;
    private static final long DEFAULT_TRANSITION_DURATION = 100L;

    RecyclerView mRecyclerView;
    QMUIStickySectionLayout mStickySectionLayout;
    private final int mStartMargin;
    private final int mEndMargin;
    private final int mInwardOffset;
    private final boolean mIsVerticalScroll;
    private final boolean mIsLocationInOppositeSide;
    private boolean mIsInDragging;
    private Drawable mScrollBarDrawable;
    private boolean mEnableScrollBarFadeInOut = false;
    private boolean mIsDraggable = true;
    private Callback mCallback;

    private long mKeepShownTime = DEFAULT_KEE_SHOW_DURATION;
    private long mTransitionDuration = DEFAULT_TRANSITION_DURATION;
    private long mStartTransitionTime = 0;
    private int mBeginAlpha = -1;
    private int mTargetAlpha = -1;
    private int mCurrentAlpha = 255;
    private float mPercent = 0f;
    private int mDragInnerStart = 0;
    private int mScrollBarSkinRes = 0;
    private int mScrollBarSkinTintColorRes = 0;

    public QMUIRVDraggableScrollBar(int startMargin,
                                    int endMargin,
                                    int inwardOffset,
                                    boolean isVerticalScroll,
                                    boolean isLocationInOppositeSide) {
        mStartMargin = startMargin;
        mEndMargin = endMargin;
        mInwardOffset = inwardOffset;
        mIsVerticalScroll = isVerticalScroll;
        mIsLocationInOppositeSide = isLocationInOppositeSide;
    }

    public QMUIRVDraggableScrollBar(int startMargin,
                                    int endMargin,
                                    int inwardOffset) {
        this(startMargin, endMargin, inwardOffset, true, false);
    }

    private Runnable mFadeScrollBarAction = new Runnable() {
        @Override
        public void run() {
            mTargetAlpha = 0;
            mBeginAlpha = mCurrentAlpha;
            mStartTransitionTime = System.currentTimeMillis();
            invalidate();
        }
    };

    private final RecyclerView.OnItemTouchListener mOnItemTouchListener = new RecyclerView.OnItemTouchListener() {

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            if (!mIsDraggable || mScrollBarDrawable == null || !needDrawScrollBar(rv)) {
                return false;
            }
            int action = e.getAction();
            final int x = (int) e.getX();
            final int y = (int) e.getY();
            if (action == MotionEvent.ACTION_DOWN) {
                Rect bounds = mScrollBarDrawable.getBounds();
                if (mCurrentAlpha > 0 && bounds.contains(x, y)) {
                    startDrag();
                    mDragInnerStart = mIsVerticalScroll ? y - bounds.top : x - bounds.left;
                }
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (mIsInDragging) {
                    onDragging(rv, mScrollBarDrawable, x, y);
                }
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                if (mIsInDragging) {
                    onDragging(rv, mScrollBarDrawable, x, y);
                    endDrag();
                }
            }
            return mIsInDragging;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            if (!mIsDraggable || mScrollBarDrawable == null || !needDrawScrollBar(rv)) {
                return;
            }
            int action = e.getAction();
            final int x = (int) e.getX();
            final int y = (int) e.getY();
            if (action == MotionEvent.ACTION_DOWN) {
                Rect bounds = mScrollBarDrawable.getBounds();
                if (mCurrentAlpha > 0 && bounds.contains(x, y)) {
                    startDrag();
                    mDragInnerStart = mIsVerticalScroll ? y - bounds.top : x - bounds.left;
                }
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (mIsInDragging) {
                    onDragging(rv, mScrollBarDrawable, x, y);
                }
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                if (mIsInDragging) {
                    onDragging(rv, mScrollBarDrawable, x, y);
                    endDrag();
                }
            }
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            if (disallowIntercept && mIsInDragging) {
                endDrag();
            }
        }
    };

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {

        private int mPrevStatus = RecyclerView.SCROLL_STATE_IDLE;

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            if (mEnableScrollBarFadeInOut) {
                if (mPrevStatus == RecyclerView.SCROLL_STATE_IDLE && newState != RecyclerView.SCROLL_STATE_IDLE) {
                    mStartTransitionTime = System.currentTimeMillis();
                    mBeginAlpha = mCurrentAlpha;
                    mTargetAlpha = 255;
                    invalidate();
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    recyclerView.postDelayed(mFadeScrollBarAction, mKeepShownTime);
                }
            }
            mPrevStatus = newState;
        }
    };

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    private void invalidate() {
        if (mStickySectionLayout != null) {
            mStickySectionLayout.invalidate();
        } else if (mRecyclerView != null) {
            mRecyclerView.invalidate();
        }
    }

    public void setScrollBarDrawable(@Nullable Drawable scrollBarDrawable) {
        mScrollBarDrawable = scrollBarDrawable;
        if (scrollBarDrawable != null) {
            scrollBarDrawable.setState(mIsInDragging ? STATE_PRESSED : STATE_NORMAL);
        }
        if (mRecyclerView != null) {
            QMUISkinHelper.refreshRVItemDecoration(mRecyclerView, this);
        }
        invalidate();
    }

    public void setScrollBarSkinRes(int scrollBarSkinRes) {
        mScrollBarSkinRes = scrollBarSkinRes;
        if (mRecyclerView != null) {
            QMUISkinHelper.refreshRVItemDecoration(mRecyclerView, this);
        }
        invalidate();
    }

    public void setScrollBarSkinTintColorRes(int colorRes) {
        mScrollBarSkinTintColorRes = colorRes;
        if (mRecyclerView != null) {
            QMUISkinHelper.refreshRVItemDecoration(mRecyclerView, this);
        }
        invalidate();
    }

    public void setDraggable(boolean draggable) {
        mIsDraggable = draggable;
    }

    public boolean isDraggable() {
        return mIsDraggable;
    }

    public void setEnableScrollBarFadeInOut(boolean enableScrollBarFadeInOut) {
        if (mEnableScrollBarFadeInOut != enableScrollBarFadeInOut) {
            mEnableScrollBarFadeInOut = enableScrollBarFadeInOut;
            if (!mEnableScrollBarFadeInOut) {
                mBeginAlpha = -1;
                mTargetAlpha = -1;
                mCurrentAlpha = 255;
            } else {
                if (mRecyclerView != null) {
                    if (mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                        mCurrentAlpha = 0;
                    }
                } else {
                    mCurrentAlpha = 0;
                }
            }
            invalidate();
        }
    }

    public boolean isEnableScrollBarFadeInOut() {
        return mEnableScrollBarFadeInOut;
    }

    private void commonAttachToRecyclerView(@Nullable RecyclerView recyclerView) {
        if (mRecyclerView == recyclerView) {
            return; // nothing to do
        }
        if (mRecyclerView != null) {
            destroyCallbacks();
        }
        mRecyclerView = recyclerView;
        if (recyclerView != null) {
            setupCallbacks();
            QMUISkinHelper.refreshRVItemDecoration(recyclerView, this);
        }
    }

    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) {
        if (mStickySectionLayout != null) {
            mStickySectionLayout.removeDrawDecoration(this);
            mStickySectionLayout = null;
        }
        commonAttachToRecyclerView(recyclerView);
    }

    public void attachToStickSectionLayout(@Nullable QMUIStickySectionLayout stickySectionLayout) {
        if (mStickySectionLayout == stickySectionLayout) {
            return; // nothing to do
        }
        if (mStickySectionLayout != null) {
            mStickySectionLayout.removeDrawDecoration(this);
        }
        mStickySectionLayout = stickySectionLayout;
        if (stickySectionLayout != null) {
            stickySectionLayout.addDrawDecoration(this);
            commonAttachToRecyclerView(stickySectionLayout.getRecyclerView());
        }
    }

    private void setupCallbacks() {
        mRecyclerView.addItemDecoration(this);
        mRecyclerView.addOnItemTouchListener(mOnItemTouchListener);
        mRecyclerView.addOnScrollListener(mScrollListener);
    }

    private void destroyCallbacks() {
        mRecyclerView.removeItemDecoration(this);
        mRecyclerView.removeOnItemTouchListener(mOnItemTouchListener);
        mRecyclerView.removeCallbacks(mFadeScrollBarAction);
        mRecyclerView.removeOnScrollListener(mScrollListener);
    }

    private void startDrag() {
        mIsInDragging = true;
        if (mScrollBarDrawable != null) {
            mScrollBarDrawable.setState(STATE_PRESSED);
        }
        if (mCallback != null) {
            mCallback.onDragStarted();
        }
        if (mRecyclerView != null) {
            mRecyclerView.removeCallbacks(mFadeScrollBarAction);
        }
        invalidate();
    }

    private void endDrag() {
        mIsInDragging = false;
        if (mScrollBarDrawable != null) {
            mScrollBarDrawable.setState(STATE_NORMAL);
        }
        if (mCallback != null) {
            mCallback.onDragEnd();
        }
        invalidate();
    }

    private void onDragging(RecyclerView recyclerView, Drawable drawable, int x, int y) {
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        int usefulSpace = getUsefulSpace(recyclerView) - (mIsVerticalScroll ? drawableHeight : drawableWidth);
        int useValue = mIsVerticalScroll ? y : x;
        float percent = (useValue - mStartMargin - mDragInnerStart) * 1f / usefulSpace;
        percent = QMUILangHelper.constrain(percent, 0f, 1f);
        if (mCallback != null) {
            mCallback.onDragToPercent(percent);
        }
        mPercent = percent;

        if (percent <= 0) {
            recyclerView.scrollToPosition(0);
        } else if (percent >= 1f) {
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            if (adapter != null) {
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        } else {
            int range = getScrollRange(recyclerView);
            int offset = getCurrentOffset(recyclerView);
            int delta = (int) (range * mPercent - offset);
            if (mIsVerticalScroll) {
                recyclerView.scrollBy(0, delta);
            } else {
                recyclerView.scrollBy(delta, 0);
            }
        }
        invalidate();
    }


    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (mStickySectionLayout == null) {
            drawScrollBar(c, parent);
        }
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull QMUIStickySectionLayout parent) {

    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull QMUIStickySectionLayout parent) {
        if (mRecyclerView != null) {
            drawScrollBar(c, mRecyclerView);
        }
    }

    private void drawScrollBar(@NonNull Canvas c, @NonNull RecyclerView recyclerView) {
        Drawable drawable = ensureScrollBar(recyclerView.getContext());
        if (drawable == null || !needDrawScrollBar(recyclerView)) {
            return;
        }

        if (mTargetAlpha != -1 && mBeginAlpha != -1) {
            long transitionTime = System.currentTimeMillis() - mStartTransitionTime;
            long duration = mTransitionDuration * Math.abs(mTargetAlpha - mBeginAlpha) / 255;
            if (transitionTime >= duration) {
                mCurrentAlpha = mTargetAlpha;
                mTargetAlpha = -1;
                mBeginAlpha = -1;
            } else {
                mCurrentAlpha = (int) (mBeginAlpha + (mTargetAlpha - mBeginAlpha) * transitionTime * 1f / duration);
                recyclerView.postInvalidateOnAnimation();
            }
        }

        drawable.setAlpha(mCurrentAlpha);

        if (!mIsInDragging) {
            mPercent = calculatePercent(recyclerView);
        }
        setScrollBarBounds(recyclerView, drawable);
        drawable.draw(c);
    }

    private int getUsefulSpace(@NonNull RecyclerView recyclerView) {
        if (mIsVerticalScroll) {
            return recyclerView.getHeight() - mStartMargin - mEndMargin;
        }
        return recyclerView.getWidth() - mStartMargin - mEndMargin;
    }

    private boolean needDrawScrollBar(RecyclerView recyclerView){
        if(mIsVerticalScroll){
            return recyclerView.canScrollVertically(-1) || recyclerView.canScrollVertically(1);
        }
        return recyclerView.canScrollHorizontally(-1) || recyclerView.canScrollHorizontally(1);
    }

    private void setScrollBarBounds(@NonNull RecyclerView recyclerView, @NonNull Drawable drawable) {
        int usefulSpace = getUsefulSpace(recyclerView);
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        int left, top;
        if (mIsVerticalScroll) {
            top = (int) ((usefulSpace - drawableHeight) * mPercent);
            left = mIsLocationInOppositeSide ? mInwardOffset : (recyclerView.getWidth() - drawableWidth - mInwardOffset);

        } else {
            left = (int) ((usefulSpace - drawableWidth) * mPercent);
            top = mIsLocationInOppositeSide ? mInwardOffset : (recyclerView.getHeight() - drawableHeight - mInwardOffset);
        }
        drawable.setBounds(left, top, left + drawableWidth, top + drawableHeight);
    }

    private int getScrollRange(@NonNull RecyclerView recyclerView) {
        if (mIsVerticalScroll) {
            return recyclerView.computeVerticalScrollRange() - recyclerView.getHeight();
        } else {
            return recyclerView.computeHorizontalScrollRange() - recyclerView.getWidth();
        }
    }

    private int getCurrentOffset(@NonNull RecyclerView recyclerView) {
        if (mIsVerticalScroll) {
            return recyclerView.computeVerticalScrollOffset();
        }
        return recyclerView.computeHorizontalScrollOffset();
    }

    private float calculatePercent(@NonNull RecyclerView recyclerView) {
        return QMUILangHelper.constrain(getCurrentOffset(recyclerView) * 1f / getScrollRange(recyclerView), 0f, 1f);
    }

    public Drawable ensureScrollBar(Context context) {
        if (mScrollBarDrawable == null) {
            setScrollBarDrawable(
                    ContextCompat.getDrawable(context, R.drawable.qmui_icon_scroll_bar));
        }
        return mScrollBarDrawable;
    }

    @Override
    public void handle(@NotNull @NonNull RecyclerView recyclerView,
                       @NotNull @NonNull QMUISkinManager manager,
                       int skinIndex,
                       @NotNull @NonNull Resources.Theme theme) {
        if (mScrollBarSkinRes != 0) {
            mScrollBarDrawable = QMUIResHelper.getAttrDrawable(
                    recyclerView.getContext(), theme, mScrollBarSkinRes);
        } else if (mScrollBarSkinTintColorRes != 0 && mScrollBarDrawable != null) {
            DrawableCompat.setTintList(mScrollBarDrawable,
                    QMUIResHelper.getAttrColorStateList(
                            recyclerView.getContext(), theme, mScrollBarSkinTintColorRes));
        }
        invalidate();
    }

    interface Callback {
        void onDragStarted();

        void onDragToPercent(float percent);

        void onDragEnd();
    }
}
