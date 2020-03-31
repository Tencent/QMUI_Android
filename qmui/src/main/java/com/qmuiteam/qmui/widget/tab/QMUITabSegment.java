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
import android.database.DataSetObserver;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.lang.ref.WeakReference;


/**
 * 在 {@link QMUIBasicTabSegment} 的基础上添加与 {@link ViewPager} 的联动使用
 */
public class QMUITabSegment extends QMUIBasicTabSegment {

    private static final String TAG = "QMUITabSegment";

    /**
     * the scrollState of ViewPager
     */
    private int mViewPagerScrollState = ViewPager.SCROLL_STATE_IDLE;


    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private DataSetObserver mPagerAdapterObserver;
    private ViewPager.OnPageChangeListener mOnPageChangeListener;
    private OnTabSelectedListener mViewPagerSelectedListener;
    private AdapterChangeListener mAdapterChangeListener;


    public QMUITabSegment(Context context) {
        super(context);
    }

    public QMUITabSegment(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QMUITabSegment(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected boolean needPreventEvent() {
        return mViewPagerScrollState != ViewPager.SCROLL_STATE_IDLE;
    }

    @Override
    public void notifyDataChanged() {
        super.notifyDataChanged();
        populateFromPagerAdapter(false);
    }

    public void setupWithViewPager(@Nullable ViewPager viewPager) {
        setupWithViewPager(viewPager, true);
    }

    public void setupWithViewPager(@Nullable ViewPager viewPager, boolean useAdapterTitle) {
        setupWithViewPager(viewPager, useAdapterTitle, true);
    }

    /**
     * associate QMUITabSegment with a {@link ViewPager}
     *
     * @param viewPager       the ViewPager to associate
     * @param useAdapterTitle populate the tab with viewPager.adapter.getTitle
     * @param autoRefresh     refresh QMUITabSegment when viewPager.adapter changed.
     */
    public void setupWithViewPager(@Nullable final ViewPager viewPager, boolean useAdapterTitle, boolean autoRefresh) {
        if (mViewPager != null) {
            // If we've already been setup with a ViewPager, remove us from it
            if (mOnPageChangeListener != null) {
                mViewPager.removeOnPageChangeListener(mOnPageChangeListener);
            }

            if (mAdapterChangeListener != null) {
                mViewPager.removeOnAdapterChangeListener(mAdapterChangeListener);
            }
        }

        if (mViewPagerSelectedListener != null) {
            // If we already have a tab selected listener for the ViewPager, remove it
            removeOnTabSelectedListener(mViewPagerSelectedListener);
            mViewPagerSelectedListener = null;
        }

        if (viewPager != null) {
            mViewPager = viewPager;

            // Add our custom OnPageChangeListener to the ViewPager
            if (mOnPageChangeListener == null) {
                mOnPageChangeListener = new TabLayoutOnPageChangeListener(this);
            }
            viewPager.addOnPageChangeListener(mOnPageChangeListener);

            // Now we'll add a tab selected listener to set ViewPager's current item
            mViewPagerSelectedListener = new ViewPagerOnTabSelectedListener(viewPager);
            addOnTabSelectedListener(mViewPagerSelectedListener);

            final PagerAdapter adapter = viewPager.getAdapter();
            if (adapter != null) {
                // Now we'll populate ourselves from the pager adapter, adding an observer if
                // autoRefresh is enabled
                setPagerAdapter(adapter, useAdapterTitle, autoRefresh);
            }

            // Add a listener so that we're notified of any adapter changes
            if (mAdapterChangeListener == null) {
                mAdapterChangeListener = new AdapterChangeListener(useAdapterTitle);
            }
            mAdapterChangeListener.setAutoRefresh(autoRefresh);
            viewPager.addOnAdapterChangeListener(mAdapterChangeListener);
        } else {
            // We've been given a null ViewPager so we need to clear out the internal state,
            // listeners and observers
            mViewPager = null;
            setPagerAdapter(null, false, false);
        }
    }


    private void setViewPagerScrollState(int state) {
        mViewPagerScrollState = state;
        if (mViewPagerScrollState == ViewPager.SCROLL_STATE_IDLE) {
            if (mPendingSelectedIndex != NO_POSITION && mSelectAnimator == null) {
                selectTab(mPendingSelectedIndex, true, false);
                mPendingSelectedIndex = NO_POSITION;
            }
        }
    }


    void populateFromPagerAdapter(boolean useAdapterTitle) {
        if (mPagerAdapter == null) {
            if (useAdapterTitle) {
                reset();
            }
            return;
        }
        final int adapterCount = mPagerAdapter.getCount();
        if (useAdapterTitle) {
            reset();
            for (int i = 0; i < adapterCount; i++) {
                addTab(mTabBuilder.setText(mPagerAdapter.getPageTitle(i)).build(getContext()));
            }
            super.notifyDataChanged();
        }

        if (mViewPager != null && adapterCount > 0) {
            final int curItem = mViewPager.getCurrentItem();
            selectTab(curItem, true, false);
        }
    }


    void setPagerAdapter(@Nullable final PagerAdapter adapter, boolean useAdapterTitle, final boolean addObserver) {
        if (mPagerAdapter != null && mPagerAdapterObserver != null) {
            // If we already have a PagerAdapter, unregister our observer
            mPagerAdapter.unregisterDataSetObserver(mPagerAdapterObserver);
        }

        mPagerAdapter = adapter;

        if (addObserver && adapter != null) {
            // Register our observer on the new adapter
            if (mPagerAdapterObserver == null) {
                mPagerAdapterObserver = new PagerAdapterObserver(useAdapterTitle);
            }
            adapter.registerDataSetObserver(mPagerAdapterObserver);
        }

        // Finally make sure we reflect the new adapter
        populateFromPagerAdapter(useAdapterTitle);
    }

    public static class TabLayoutOnPageChangeListener implements ViewPager.OnPageChangeListener {
        private final WeakReference<QMUITabSegment> mTabSegmentRef;

        public TabLayoutOnPageChangeListener(QMUITabSegment tabSegment) {
            mTabSegmentRef = new WeakReference<>(tabSegment);
        }

        @Override
        public void onPageScrollStateChanged(final int state) {
            final QMUITabSegment tabSegment = mTabSegmentRef.get();
            if (tabSegment != null) {
                tabSegment.setViewPagerScrollState(state);
            }

        }

        @Override
        public void onPageScrolled(final int position, final float positionOffset,
                                   final int positionOffsetPixels) {
            final QMUITabSegment tabSegment = mTabSegmentRef.get();
            if (tabSegment != null) {
                tabSegment.updateIndicatorPosition(position, positionOffset);
            }
        }

        @Override
        public void onPageSelected(final int position) {
            final QMUITabSegment tabSegment = mTabSegmentRef.get();
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
        private final ViewPager mViewPager;

        public ViewPagerOnTabSelectedListener(ViewPager viewPager) {
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

    private class AdapterChangeListener implements ViewPager.OnAdapterChangeListener {
        private boolean mAutoRefresh;
        private final boolean mUseAdapterTitle;

        AdapterChangeListener(boolean useAdapterTitle) {
            mUseAdapterTitle = useAdapterTitle;
        }

        @Override
        public void onAdapterChanged(@NonNull ViewPager viewPager,
                                     @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
            if (mViewPager == viewPager) {
                setPagerAdapter(newAdapter, mUseAdapterTitle, mAutoRefresh);
            }
        }

        void setAutoRefresh(boolean autoRefresh) {
            mAutoRefresh = autoRefresh;
        }
    }


    private class PagerAdapterObserver extends DataSetObserver {
        private final boolean mUseAdapterTitle;

        PagerAdapterObserver(boolean useAdapterTitle) {
            mUseAdapterTitle = useAdapterTitle;
        }

        @Override
        public void onChanged() {
            populateFromPagerAdapter(mUseAdapterTitle);
        }

        @Override
        public void onInvalidated() {
            populateFromPagerAdapter(mUseAdapterTitle);
        }
    }

    /**
     * Please use QMUIBasicTabSegment.OnTabClickListener for a replacement
     */
    @Deprecated
    public interface OnTabClickListener extends QMUIBasicTabSegment.OnTabClickListener{

    }

    /**
     * Please use QMUIBasicTabSegment.OnTabSelectedListener for a replacement
     */
    @Deprecated
    public interface OnTabSelectedListener extends QMUIBasicTabSegment.OnTabSelectedListener{

    }
}
