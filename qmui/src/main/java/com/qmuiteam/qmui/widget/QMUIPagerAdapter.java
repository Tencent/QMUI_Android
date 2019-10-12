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

import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

/**
 * @author cginechen
 * @date 2017-09-13
 */

public abstract class QMUIPagerAdapter extends PagerAdapter {
    private SparseArray<Object> mScrapItems = new SparseArray<>();

    public QMUIPagerAdapter() {
    }


    /**
     * Hydrating an object is taking an object that exists in memory,
     * that doesn't yet contain any domain data ("real" data),
     * and then populating it with domain data.
     */
    @NonNull
    protected abstract Object hydrate(@NonNull ViewGroup container, int position);

    protected abstract void populate(@NonNull ViewGroup container, @NonNull Object item, int position);

    protected abstract void destroy(@NonNull ViewGroup container, int position, @NonNull Object object);

    @Override
    @NonNull
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Object item = mScrapItems.get(position);
        if (item == null) {
            item = hydrate(container, position);
            mScrapItems.put(position, item);
        }
        populate(container, item, position);
        return item;
    }

    @Override
    public final void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        destroy(container, position, object);

    }

    /**
     * sometimes you may need to perform some operations on all items,
     * such as perform cleanup when the ViewPager is destroyed
     * once the action return true, then do not handle remain items
     *
     * @param action
     */
    public void each(@NonNull Action action) {
        int size = mScrapItems.size();
        for (int i = 0; i < size; i++) {
            Object item = mScrapItems.valueAt(i);
            if (action.call(item)) {
                break;
            }
        }
    }

    public interface Action {
        /**
         * @return true to intercept forEach
         */
        boolean call(Object item);
    }
}
