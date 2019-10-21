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

package com.qmuiteam.qmui.skin;

import android.database.DataSetObserver;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class DelegateViewPagerAdapter extends PagerAdapter {
    private PagerAdapter origin;
    private String prefix;
    private HashMap<Object, Integer> mChildBindInfo = new HashMap<>();
    private QMUISkinMaker skinMaker;

    DelegateViewPagerAdapter(QMUISkinMaker skinMaker, PagerAdapter origin, String prefix) {
        this.skinMaker = skinMaker;
        this.origin = origin;
        this.prefix = prefix;
    }

    @Override
    public int getCount() {
        return origin.getCount();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return origin.isViewFromObject(view, object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Object item = origin.instantiateItem(container, position);
        int id = skinMaker.bindViewPagerChild(origin, item, prefix);
        mChildBindInfo.put(item, id);
        return item;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        origin.destroyItem(container, position, object);
        Integer bindId = mChildBindInfo.remove(object);
        if (bindId != null) {
            skinMaker.unBind(bindId);
        }
    }

    @Override
    public void restoreState(Parcelable bundle, ClassLoader classLoader) {
        origin.restoreState(bundle, classLoader);
    }

    @Override
    public Parcelable saveState() {
        return origin.saveState();
    }

    @Override
    public void startUpdate(@NonNull ViewGroup container) {
        origin.startUpdate(container);
    }

    @Override
    public void finishUpdate(@NonNull ViewGroup container) {
        origin.finishUpdate(container);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return origin.getPageTitle(position);
    }

    @Override
    public float getPageWidth(int position) {
        return origin.getPageWidth(position);
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        origin.setPrimaryItem(container, position, object);
    }

    @Override
    public void unregisterDataSetObserver(@NonNull DataSetObserver observer) {
        origin.unregisterDataSetObserver(observer);
    }

    @Override
    public void registerDataSetObserver(@NonNull DataSetObserver observer) {
        origin.registerDataSetObserver(observer);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        origin.notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return origin.getItemPosition(object);
    }
}