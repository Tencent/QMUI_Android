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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.util.QMUIViewOffsetHelper;

public class QMUIContinuousNestedTopDelegateLayout extends FrameLayout implements IQMUIContinuousNestedTopView {

    private OnScrollNotifier mScrollNotifier;
    private View mHeaderView;
    private IQMUIContinuousNestedTopView mDelegateView;
    private View mFooterView;
    private QMUIViewOffsetHelper mHeaderViewOffsetHelper;
    private QMUIViewOffsetHelper mDelegateViewOffsetHelper;
    private QMUIViewOffsetHelper mFooterViewOffsetHelper;
    private int mOffsetCurrent = 0;
    private int mOffsetRange = 0;

    public QMUIContinuousNestedTopDelegateLayout(@NonNull Context context) {
        super(context);
    }

    public QMUIContinuousNestedTopDelegateLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public QMUIContinuousNestedTopDelegateLayout(@NonNull Context context,
                                                 @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        int anchorHeight = 0;
        if (mHeaderView != null) {
            anchorHeight += mHeaderView.getMeasuredHeight();
        }
        if (mDelegateView != null) {
            View delegateView = (View) mDelegateView;
            anchorHeight += delegateView.getMeasuredHeight();
        }

        if (mFooterView != null) {
            anchorHeight += mFooterView.getMeasuredHeight();
        }
        if (anchorHeight < h) {
            setMeasuredDimension(w, anchorHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int w = right - left, h = bottom - top;
        int anchorTop = 0;
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
            mScrollNotifier.notify(getCurrentScroll(), getScrollRange());
        }
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
                int beforeRange = mHeaderView == null ? 0 : mHeaderView.getHeight();
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
            int afterRange = mOffsetRange - (mFooterView == null ? 0 : mFooterView.getHeight());
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
    public int getScrollRange() {
        int scrollRange = mOffsetRange;
        if (mDelegateView != null) {
            scrollRange += mDelegateView.getScrollRange();
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
                    notifier.notify(getCurrentScroll(), getScrollRange());
                }
            });
        }
    }
}