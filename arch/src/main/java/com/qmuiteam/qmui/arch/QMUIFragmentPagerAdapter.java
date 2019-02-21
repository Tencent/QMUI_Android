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

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;

import com.qmuiteam.qmui.widget.QMUIPagerAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public abstract class QMUIFragmentPagerAdapter extends QMUIPagerAdapter {

    private final FragmentManager mFragmentManager;
    private FragmentTransaction mCurrentTransaction;
    private Fragment mCurrentPrimaryItem = null;

    public QMUIFragmentPagerAdapter(@NonNull FragmentManager fm) {
        mFragmentManager = fm;
    }

    public abstract QMUIFragment createFragment(int position);

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == ((Fragment) object).getView();
    }

    @SuppressLint("CommitTransaction")
    @Override
    @NonNull
    protected Object hydrate(@NonNull ViewGroup container, int position) {
        String name = makeFragmentName(container.getId(), position);
        if (mCurrentTransaction == null) {
            mCurrentTransaction = mFragmentManager.beginTransaction();
        }
        Fragment fragment = mFragmentManager.findFragmentByTag(name);
        if (fragment != null) {
            return fragment;
        }
        return createFragment(position);
    }

    @SuppressLint("CommitTransaction")
    @Override
    protected void populate(@NonNull ViewGroup container, @NonNull Object item, int position) {
        String name = makeFragmentName(container.getId(), position);
        if (mCurrentTransaction == null) {
            mCurrentTransaction = mFragmentManager.beginTransaction();
        }
        Fragment fragment = mFragmentManager.findFragmentByTag(name);
        if (fragment != null) {
            mCurrentTransaction.attach(fragment);
            if (fragment.getView() != null && fragment.getView().getWidth() == 0) {
                fragment.getView().requestLayout();
            }
        } else {
            fragment = (Fragment) item;
            mCurrentTransaction.add(container.getId(), fragment, name);
        }
        if (fragment != mCurrentPrimaryItem) {
            fragment.setMenuVisibility(false);
            fragment.setUserVisibleHint(false);
        }
    }

    @SuppressLint("CommitTransaction")
    @Override
    protected void destroy(@NonNull ViewGroup container, int position, @NonNull Object object) {
        if (mCurrentTransaction == null) {
            mCurrentTransaction = mFragmentManager.beginTransaction();
        }
        mCurrentTransaction.detach((Fragment) object);
    }

    @Override
    public void startUpdate(@NonNull ViewGroup container) {
        if (container.getId() == View.NO_ID) {
            throw new IllegalStateException("ViewPager with adapter " + this
                    + " requires a view id");
        }
    }

    @Override
    public void finishUpdate(@NonNull ViewGroup container) {
        if (mCurrentTransaction != null) {
            mCurrentTransaction.commitNowAllowingStateLoss();
            mCurrentTransaction = null;
        }
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        Fragment fragment = (Fragment) object;
        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
                mCurrentPrimaryItem.setUserVisibleHint(false);
            }
            fragment.setMenuVisibility(true);
            fragment.setUserVisibleHint(true);
            mCurrentPrimaryItem = fragment;
        }
    }

    private String makeFragmentName(int viewId, long id) {
        return "QMUIFragmentPagerAdapter:" + viewId + ":" + id;
    }
}