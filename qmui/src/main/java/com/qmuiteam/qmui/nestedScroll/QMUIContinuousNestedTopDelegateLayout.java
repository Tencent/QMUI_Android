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
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.util.QMUIViewOffsetHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingChild2;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.NestedScrollingParent2;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;

public class QMUIContinuousNestedTopDelegateLayout extends FrameLayout implements
        NestedScrollingChild2, NestedScrollingParent2, IQMUIContinuousNestedTopView {

    public static final String KEY_SCROLL_INFO_OFFSET = "@qmui_scroll_info_top_dl_offset";

    private OnScrollNotifier mScrollNotifier;
    private View mHeaderView;
    private IQMUIContinuousNestedTopView mDelegateView;
    private View mFooterView;
    private QMUIViewOffsetHelper mHeaderViewOffsetHelper;
    private QMUIViewOffsetHelper mDelegateViewOffsetHelper;
    private QMUIViewOffsetHelper mFooterViewOffsetHelper;
    private int mOffsetCurrent = 0;
    private int mOffsetRange = 0;
    private final NestedScrollingParentHelper mParentHelper;
    private final NestedScrollingChildHelper mChildHelper;
    private Runnable mCheckLayoutAction = new Runnable() {
        @Override
        public void run() {
            checkLayout();
        }
    };

    public QMUIContinuousNestedTopDelegateLayout(@NonNull Context context) {
        this(context, null);
    }

    public QMUIContinuousNestedTopDelegateLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QMUIContinuousNestedTopDelegateLayout(@NonNull Context context,
                                                 @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mParentHelper = new NestedScrollingParentHelper(this);
        mChildHelper = new NestedScrollingChildHelper(this);

        ViewCompat.setNestedScrollingEnabled(this, true);
        setClipToPadding(false);
    }

    public void setHeaderView(@NonNull View headerView) {
        mHeaderView = headerView;
        mHeaderViewOffsetHelper = new QMUIViewOffsetHelper(headerView);
        addView(headerView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    public void setDelegateView(@NonNull IQMUIContinuousNestedTopView delegateView) {
        if (!(delegateView instanceof View)) {
            throw new IllegalArgumentException("delegateView must be a instance of View");
        }
        if (mDelegateView != null) {
            mDelegateView.injectScrollNotifier(null);
        }
        mDelegateView = delegateView;
        View view = (View) delegateView;
        mDelegateViewOffsetHelper = new QMUIViewOffsetHelper(view);
        // WRAP_CONTENT, the height will be handled by QMUIContinuousNestedTopAreaBehavior
        addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void setFooterView(@NonNull View footerView) {
        mFooterView = footerView;
        mFooterViewOffsetHelper = new QMUIViewOffsetHelper(footerView);
        addView(footerView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        int anchorHeight = getPaddingTop();
        if (mHeaderView != null) {
            mHeaderView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(h, MeasureSpec.UNSPECIFIED));
            anchorHeight += mHeaderView.getMeasuredHeight();
        }
        if (mDelegateView != null) {
            View delegateView = (View) mDelegateView;
            delegateView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(h, MeasureSpec.AT_MOST));
            anchorHeight += delegateView.getMeasuredHeight();
        }

        if (mFooterView != null) {
            mFooterView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(h, MeasureSpec.UNSPECIFIED));
            anchorHeight += mFooterView.getMeasuredHeight();
        }

        anchorHeight += getPaddingBottom();
        if (anchorHeight < h) {
            setMeasuredDimension(w, anchorHeight);
        } else {
            setMeasuredDimension(w, h);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int w = right - left, h = bottom - top;
        int anchorTop = getPaddingTop();
        int viewHeight;
        if (mHeaderView != null) {
            viewHeight = mHeaderView.getMeasuredHeight();
            mHeaderView.layout(0, anchorTop, w, anchorTop + viewHeight);
            anchorTop += viewHeight;
        }

        if (mDelegateView != null) {
            View view = (View) mDelegateView;
            viewHeight = view.getMeasuredHeight();
            view.layout(0, anchorTop, w, anchorTop + viewHeight);
            anchorTop += viewHeight;
        }

        if (mFooterView != null) {
            viewHeight = mFooterView.getMeasuredHeight();
            mFooterView.layout(0, anchorTop, w, anchorTop + viewHeight);
            anchorTop += viewHeight;
        }
        anchorTop += getPaddingBottom();

        mOffsetRange = Math.max(0, anchorTop - h);

        if (mHeaderViewOffsetHelper != null) {
            mHeaderViewOffsetHelper.onViewLayout();
            mOffsetCurrent = -mHeaderViewOffsetHelper.getTopAndBottomOffset();
        }

        if (mDelegateViewOffsetHelper != null) {
            mDelegateViewOffsetHelper.onViewLayout();
            mOffsetCurrent = -mDelegateViewOffsetHelper.getTopAndBottomOffset();
        }

        if (mFooterViewOffsetHelper != null) {
            mFooterViewOffsetHelper.onViewLayout();
            mOffsetCurrent = -mFooterViewOffsetHelper.getTopAndBottomOffset();
        }

        if(mOffsetCurrent > mOffsetRange){
            offsetTo(mOffsetRange);
        }
        postCheckLayout();
    }

    public void postCheckLayout() {
        removeCallbacks(mCheckLayoutAction);
        post(mCheckLayoutAction);
    }

    public void checkLayout() {
        if (mHeaderView == null && mFooterView == null) {
            return;
        }
        if (mDelegateView == null) {
            return;
        }
        int headerOffsetRange = getContainerHeaderOffsetRange();
        int delegateCurrentScroll = mDelegateView.getCurrentScroll();
        int delegateScrollRange = mDelegateView.getScrollOffsetRange();
        if (delegateCurrentScroll > 0 && mHeaderView != null && mOffsetCurrent < headerOffsetRange) {
            int over = headerOffsetRange - mOffsetCurrent;
            if (over >= delegateCurrentScroll) {
                mDelegateView.consumeScroll(Integer.MIN_VALUE);
                offsetTo(mOffsetCurrent + delegateCurrentScroll);
            } else {
                mDelegateView.consumeScroll(-over);
                offsetTo(headerOffsetRange);
            }

        }

        if (mOffsetCurrent > headerOffsetRange && delegateCurrentScroll < delegateScrollRange
                && mFooterView != null) {
            int over = mOffsetCurrent - headerOffsetRange;
            int delegateRemain = delegateScrollRange - delegateCurrentScroll;
            if (over >= delegateRemain) {
                mDelegateView.consumeScroll(Integer.MAX_VALUE);
                offsetTo(headerOffsetRange + over - delegateRemain);
            } else {
                mDelegateView.consumeScroll(over);
                offsetTo(headerOffsetRange);
            }
        }

    }

    private void offsetTo(int targetOffsetCurrent) {
        mOffsetCurrent = targetOffsetCurrent;
        if (mHeaderViewOffsetHelper != null) {
            mHeaderViewOffsetHelper.setTopAndBottomOffset(-targetOffsetCurrent);
        }

        if (mDelegateViewOffsetHelper != null) {
            mDelegateViewOffsetHelper.setTopAndBottomOffset(-targetOffsetCurrent);
        }

        if (mFooterViewOffsetHelper != null) {
            mFooterViewOffsetHelper.setTopAndBottomOffset(-targetOffsetCurrent);
        }
        if (mScrollNotifier != null) {
            mScrollNotifier.notify(getCurrentScroll(), getScrollOffsetRange());
        }
    }

    public IQMUIContinuousNestedTopView getDelegateView() {
        return mDelegateView;
    }

    public View getHeaderView() {
        return mHeaderView;
    }

    public View getFooterView() {
        return mFooterView;
    }

    public int getContainerOffsetCurrent() {
        return mOffsetCurrent;
    }

    public int getContainerOffsetRange() {
        return mOffsetRange;
    }

    public int getContainerHeaderOffsetRange() {
        if (mOffsetRange == 0 || mHeaderView == null) {
            return 0;
        }
        int maxHeight = getPaddingTop() + mHeaderView.getHeight();
        return Math.min(maxHeight, mOffsetRange);
    }

    @Override
    public int consumeScroll(int dyUnconsumed) {
        if (mOffsetRange <= 0) {
            if (mDelegateView != null) {
                return mDelegateView.consumeScroll(dyUnconsumed);
            }
            return dyUnconsumed;
        }

        if (dyUnconsumed > 0) {
            if (mDelegateView == null) {
                if (dyUnconsumed == Integer.MAX_VALUE) {
                    offsetTo(mOffsetRange);
                } else if (mOffsetCurrent + dyUnconsumed <= mOffsetRange) {
                    offsetTo(mOffsetCurrent + dyUnconsumed);
                    return 0;
                } else if (mOffsetCurrent < mOffsetRange) {
                    dyUnconsumed -= mOffsetRange - mOffsetCurrent;
                    offsetTo(mOffsetRange);

                }
                return dyUnconsumed;
            } else {
                int beforeRange = Math.min(mOffsetRange,
                        getPaddingTop() + (mHeaderView == null ? 0 : mHeaderView.getHeight()));
                if (dyUnconsumed == Integer.MAX_VALUE) {
                    offsetTo(beforeRange);
                } else if (mOffsetCurrent + dyUnconsumed <= beforeRange) {
                    offsetTo(mOffsetCurrent + dyUnconsumed);
                    return 0;
                } else if (mOffsetCurrent < beforeRange) {
                    dyUnconsumed -= beforeRange - mOffsetCurrent;
                    offsetTo(beforeRange);
                }
                dyUnconsumed = mDelegateView.consumeScroll(dyUnconsumed);
                if (dyUnconsumed <= 0) {
                    return dyUnconsumed;
                }
                if (dyUnconsumed == Integer.MAX_VALUE) {
                    offsetTo(mOffsetRange);
                } else if (mOffsetCurrent + dyUnconsumed <= mOffsetRange) {
                    offsetTo(mOffsetCurrent + dyUnconsumed);
                    return 0;
                } else {
                    dyUnconsumed -= mOffsetRange - mOffsetCurrent;
                    offsetTo(mOffsetRange);
                    return dyUnconsumed;
                }
            }
        } else if (dyUnconsumed < 0) {
            if (mDelegateView == null) {
                if (dyUnconsumed == Integer.MIN_VALUE) {
                    offsetTo(0);
                } else if (mOffsetCurrent + dyUnconsumed >= 0) {
                    offsetTo(mOffsetCurrent + dyUnconsumed);
                    return 0;
                } else if (mOffsetCurrent > 0) {
                    dyUnconsumed += mOffsetCurrent;
                    offsetTo(0);
                }
                return dyUnconsumed;
            }
            int afterRange = Math.max(0,
                    mOffsetRange - getPaddingBottom() - (mFooterView == null ? 0 : mFooterView.getHeight()));
            if (dyUnconsumed == Integer.MIN_VALUE) {
                offsetTo(afterRange);
            } else if (mOffsetCurrent + dyUnconsumed > afterRange) {
                offsetTo(mOffsetCurrent + dyUnconsumed);
                return 0;
            } else if (mOffsetCurrent > afterRange) {
                dyUnconsumed += mOffsetCurrent - afterRange;
                offsetTo(afterRange);
            }
            dyUnconsumed = mDelegateView.consumeScroll(dyUnconsumed);
            if (dyUnconsumed >= 0) {
                return dyUnconsumed;
            }
            if (dyUnconsumed == Integer.MIN_VALUE) {
                offsetTo(0);
            } else if (mOffsetCurrent + dyUnconsumed > 0) {
                offsetTo(mOffsetCurrent + dyUnconsumed);
                return 0;
            } else if (mOffsetCurrent > 0) {
                dyUnconsumed += mOffsetCurrent;
                offsetTo(0);
            }
        }
        return dyUnconsumed;
    }

    @Override
    public int getCurrentScroll() {
        int currentOffset = mOffsetCurrent;
        if (mDelegateView != null) {
            currentOffset += mDelegateView.getCurrentScroll();
        }
        return currentOffset;
    }

    @Override
    public int getScrollOffsetRange() {
        int scrollRange = mOffsetRange;
        if (mDelegateView != null) {
            scrollRange += mDelegateView.getScrollOffsetRange();
        }
        return scrollRange;
    }

    @Override
    public void injectScrollNotifier(final OnScrollNotifier notifier) {
        mScrollNotifier = notifier;
        if (mDelegateView != null) {
            mDelegateView.injectScrollNotifier(new OnScrollNotifier() {
                @Override
                public void notify(int innerOffset, int innerRange) {
                    notifier.notify(getCurrentScroll(), getScrollOffsetRange());
                }

                @Override
                public void onScrollStateChange(View view, int newScrollState) {

                }
            });
        }
    }

    @Override
    public void saveScrollInfo(@NonNull Bundle bundle) {
        bundle.putInt(KEY_SCROLL_INFO_OFFSET, -mOffsetCurrent);
        if (mDelegateView != null) {
            mDelegateView.saveScrollInfo(bundle);
        }
    }

    @Override
    public void restoreScrollInfo(@NonNull Bundle bundle) {
        int offset = bundle.getInt(KEY_SCROLL_INFO_OFFSET, 0);
        offsetTo(QMUILangHelper.constrain(-offset, 0, getContainerOffsetRange()));
        if (mDelegateView != null) {
            mDelegateView.restoreScrollInfo(bundle);
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
        int consumed = 0;
        if (dyUnconsumed > 0) {
            if (mOffsetCurrent + dyUnconsumed <= mOffsetRange) {
                consumed = dyUnconsumed;
                offsetTo(mOffsetCurrent + dyUnconsumed);
            } else if (mOffsetCurrent <= mOffsetRange) {
                consumed = mOffsetRange - mOffsetCurrent;
                offsetTo(mOffsetRange);
            }
        } else if (dyUnconsumed < 0) {
            if (mOffsetCurrent + dyUnconsumed >= 0) {
                consumed = dyUnconsumed;
                offsetTo(mOffsetCurrent + dyUnconsumed);
            } else if (mOffsetCurrent >= 0) {
                consumed = -mOffsetCurrent;
                offsetTo(0);
            }
        }
        dispatchNestedScroll(0, dyConsumed + consumed, 0,
                dyUnconsumed - consumed, null, type);
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed,
                                  int type) {
        dispatchNestedPreScroll(dx, dy, consumed, null, type);
        int unconsumed = dy - consumed[1];
        if (unconsumed > 0) {
            int topMargin = Math.min(mOffsetRange, getPaddingTop() + (mHeaderView == null ? 0 : mHeaderView.getHeight()));
            if (mOffsetCurrent + unconsumed <= topMargin) {
                offsetTo(mOffsetCurrent + unconsumed);
                consumed[1] += unconsumed;
            } else if (mOffsetCurrent < topMargin) {
                consumed[1] += topMargin - mOffsetCurrent;
                offsetTo(topMargin);
            }
        } else if (unconsumed < 0) {
            int bottomMargin = getPaddingBottom() + (mFooterView != null ? mFooterView.getHeight() : 0);
            int b = mOffsetRange - bottomMargin;
            if (mOffsetCurrent + unconsumed >= b) {
                offsetTo(mOffsetCurrent + unconsumed);
                consumed[1] += unconsumed;
            } else if (mOffsetCurrent > b) {
                consumed[1] += b - mOffsetCurrent;
                offsetTo(b);
            }
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
}