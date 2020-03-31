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

package com.qmuiteam.qmui.widget.tab;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import java.lang.ref.WeakReference;


/**
 * 在 {@link QMUIBasicTabSegment} 的基础上添加与 {@link ViewPager2} 的联动使用
 */
public class QMUITabSegment2 extends QMUIBasicTabSegment {

    private static final String TAG = "QMUITabSegment";

    /**
     * the scrollState of ViewPager
     */
    private int mViewPagerScrollState = ViewPager2.SCROLL_STATE_IDLE;


    private ViewPager2 mViewPager;
    private ViewPager2.OnPageChangeCallback mOnPageChangeListener;
    private OnTabSelectedListener mViewPagerSelectedListener;


    public QMUITabSegment2(Context context) {
        super(context);
    }

    public QMUITabSegment2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QMUITabSegment2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected boolean needPreventEvent() {
        return mViewPagerScrollState != ViewPager2.SCROLL_STATE_IDLE;
    }


    /**
     * associate QMUITabSegment2 with a {@link ViewPager2}
     *
     * @param viewPager the ViewPager2 to associate
     */
    public void setupWithViewPager(@Nullable final ViewPager2 viewPager) {
        if (mViewPager != null) {
            if (mOnPageChangeListener != null) {
                mViewPager.unregisterOnPageChangeCallback(mOnPageChangeListener);
            }
        }

        if (mViewPagerSelectedListener != null) {
            removeOnTabSelectedListener(mViewPagerSelectedListener);
            mViewPagerSelectedListener = null;
        }

        if (viewPager != null) {
            mViewPager = viewPager;
            if (mOnPageChangeListener == null) {
                mOnPageChangeListener = new TabLayoutOnPageChangeListener(this);
            }
            viewPager.registerOnPageChangeCallback(mOnPageChangeListener);

            mViewPagerSelectedListener = new ViewPagerOnTabSelectedListener(viewPager);
            addOnTabSelectedListener(mViewPagerSelectedListener);

            final int curItem = mViewPager.getCurrentItem();
            selectTab(curItem, true, false);
        } else {
            mViewPager = null;
        }
    }


    private void setViewPagerScrollState(int state) {
        mViewPagerScrollState = state;
        if (mViewPagerScrollState == ViewPager2.SCROLL_STATE_IDLE) {
            if (mPendingSelectedIndex != NO_POSITION && mSelectAnimator == null) {
                selectTab(mPendingSelectedIndex, true, false);
                mPendingSelectedIndex = NO_POSITION;
            }
        }
    }


    public static class TabLayoutOnPageChangeListener extends ViewPager2.OnPageChangeCallback {
        private final WeakReference<QMUITabSegment2> mTabSegmentRef;

        public TabLayoutOnPageChangeListener(QMUITabSegment2 tabSegment) {
            mTabSegmentRef = new WeakReference<>(tabSegment);
        }

        @Override
        public void onPageScrollStateChanged(final int state) {
            final QMUITabSegment2 tabSegment = mTabSegmentRef.get();
            if (tabSegment != null) {
                tabSegment.setViewPagerScrollState(state);
            }

        }

        @Override
        public void onPageScrolled(final int position, final float positionOffset,
                                   final int positionOffsetPixels) {
            final QMUITabSegment2 tabSegment = mTabSegmentRef.get();
            if (tabSegment != null) {
                tabSegment.updateIndicatorPosition(position, positionOffset);
            }
        }

        @Override
        public void onPageSelected(final int position) {
            final QMUITabSegment2 tabSegment = mTabSegmentRef.get();
            if (tabSegment != null && tabSegment.mPendingSelectedIndex != NO_POSITION) {
                tabSegment.mPendingSelectedIndex = position;
                return;
            }
            if (tabSegment != null && tabSegment.getSelectedIndex() != position
                    && position < tabSegment.getTabCount()) {
                tabSegment.selectTab(position, true, false);
            }
        }
    }

    private static class ViewPagerOnTabSelectedListener implements OnTabSelectedListener {
        private final ViewPager2 mViewPager;

        public ViewPagerOnTabSelectedListener(ViewPager2 viewPager) {
            mViewPager = viewPager;
        }

        @Override
        public void onTabSelected(int index) {
            mViewPager.setCurrentItem(index, false);
        }

        @Override
        public void onTabUnselected(int index) {
        }

        @Override
        public void onTabReselected(int index) {
        }

        @Override
        public void onDoubleTap(int index) {

        }
    }
}
