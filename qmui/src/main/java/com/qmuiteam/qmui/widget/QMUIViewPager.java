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

package com.qmuiteam.qmui.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.os.Build;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.qmuiteam.qmui.util.QMUIWindowInsetHelper;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * @author cginechen
 * @date 2017-09-13
 */

public class QMUIViewPager extends ViewPager implements IWindowInsetLayout {
    private static final int DEFAULT_INFINITE_RATIO = 100;

    private boolean mIsSwipeable = true;
    private boolean mIsInMeasure = false;
    private QMUIWindowInsetHelper mQMUIWindowInsetHelper;
    private boolean mEnableLoop = false;
    private int mInfiniteRatio = DEFAULT_INFINITE_RATIO;

    public QMUIViewPager(Context context) {
        this(context, null);
    }

    public QMUIViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mQMUIWindowInsetHelper = new QMUIWindowInsetHelper(this, this);
    }




    public void setSwipeable(boolean enable) {
        mIsSwipeable = enable;
    }

    public int getInfiniteRatio() {
        return mInfiniteRatio;
    }

    public void setInfiniteRatio(int infiniteRatio) {
        mInfiniteRatio = infiniteRatio;
    }

    public boolean isEnableLoop() {
        return mEnableLoop;
    }

    public void setEnableLoop(boolean enableLoop) {
        if (mEnableLoop != enableLoop) {
            mEnableLoop = enableLoop;
            if (getAdapter() != null) {
                getAdapter().notifyDataSetChanged();
            }
        }

    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        ViewCompat.requestApplyInsets(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mIsSwipeable && super.onTouchEvent(ev);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mIsSwipeable && super.onInterceptTouchEvent(ev);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mIsInMeasure = true;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mIsInMeasure = false;
    }

    public boolean isInMeasure() {
        return mIsInMeasure;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected boolean fitSystemWindows(Rect insets) {
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            return applySystemWindowInsets19(insets);
        }
        return super.fitSystemWindows(insets);
    }

    @Override
    public boolean applySystemWindowInsets19(Rect insets) {
        return mQMUIWindowInsetHelper.defaultApplySystemWindowInsets19(this, insets);
    }

    @Override
    public boolean applySystemWindowInsets21(Object insets) {
        return mQMUIWindowInsetHelper.defaultApplySystemWindowInsets21(this, insets);
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        if (adapter instanceof QMUIPagerAdapter) {
            super.setAdapter(new WrapperPagerAdapter((QMUIPagerAdapter) adapter));
        } else {
            super.setAdapter(adapter);
        }
    }

    class WrapperPagerAdapter extends PagerAdapter {
        private QMUIPagerAdapter mAdapter;

        public WrapperPagerAdapter(QMUIPagerAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public int getCount() {
            int count = mAdapter.getCount();
            if (mEnableLoop && count > 3) {
                count *= mInfiniteRatio;
            }
            return count;
        }

        @Override
        @NonNull
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            int realPosition = position;
            if (mEnableLoop && mAdapter.getCount() != 0) {
                realPosition = position % mAdapter.getCount();
            }
            return mAdapter.instantiateItem(container, realPosition);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            int realPosition = position;
            if (mEnableLoop && mAdapter.getCount() != 0) {
                realPosition = position % mAdapter.getCount();
            }
            mAdapter.destroyItem(container, realPosition, object);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return mAdapter.isViewFromObject(view, object);
        }


        @Override
        public void restoreState(Parcelable bundle, ClassLoader classLoader) {
            mAdapter.restoreState(bundle, classLoader);
        }

        @Override
        public Parcelable saveState() {
            return mAdapter.saveState();
        }

        @Override
        public void startUpdate(@NonNull ViewGroup container) {
            mAdapter.startUpdate(container);
        }

        @Override
        public void finishUpdate(@NonNull ViewGroup container) {
            mAdapter.finishUpdate(container);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            int virtualPosition = position % mAdapter.getCount();
            return mAdapter.getPageTitle(virtualPosition);
        }

        @Override
        public float getPageWidth(int position) {
            return mAdapter.getPageWidth(position);
        }

        @Override
        public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            mAdapter.setPrimaryItem(container, position, object);
        }

        @Override
        public void unregisterDataSetObserver(@NonNull DataSetObserver observer) {
            mAdapter.unregisterDataSetObserver(observer);
        }

        @Override
        public void registerDataSetObserver(@NonNull DataSetObserver observer) {
            mAdapter.registerDataSetObserver(observer);
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return mAdapter.getItemPosition(object);
        }
    }
}
