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

package com.qmuiteam.qmui.widget.section;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.util.Log;
import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.List;

import static com.qmuiteam.qmui.widget.section.QMUISection.ITEM_INDEX_LOAD_AFTER;
import static com.qmuiteam.qmui.widget.section.QMUISection.ITEM_INDEX_LOAD_BEFORE;
import static com.qmuiteam.qmui.widget.section.QMUISection.ITEM_INDEX_SECTION_HEADER;

public class QMUISectionDiffCallback<H extends QMUISection.Model<H>, T extends QMUISection.Model<T>>
        extends DiffUtil.Callback {

    private ArrayList<QMUISection<H, T>> mOldList = new ArrayList<>();
    private ArrayList<QMUISection<H, T>> mNewList = new ArrayList<>();

    private SparseIntArray mOldSectionIndex = new SparseIntArray();
    private SparseIntArray mOldItemIndex = new SparseIntArray();

    private SparseIntArray mNewSectionIndex = new SparseIntArray();
    private SparseIntArray mNewItemIndex = new SparseIntArray();

    public QMUISectionDiffCallback(
            @Nullable List<QMUISection<H, T>> oldList,
            @Nullable List<QMUISection<H, T>> newList) {
        if (oldList != null) {
            mOldList.addAll(oldList);
        }

        if (newList != null) {
            mNewList.addAll(newList);
        }

        generateIndex(mOldList, mOldSectionIndex, mOldItemIndex);
        generateIndex(mNewList, mNewSectionIndex, mNewItemIndex);
    }

    public void cloneNewIndexTo(@NonNull SparseIntArray sectionIndex, @NonNull SparseIntArray itemIndex) {
        sectionIndex.clear();
        itemIndex.clear();
        for (int i = 0; i < mNewSectionIndex.size(); i++) {
            sectionIndex.append(mNewSectionIndex.keyAt(i), mNewSectionIndex.valueAt(i));
        }
        for (int i = 0; i < mNewItemIndex.size(); i++) {
            itemIndex.append(mNewItemIndex.keyAt(i), mNewItemIndex.valueAt(i));
        }
    }

    private void generateIndex(List<QMUISection<H, T>> list,
                               SparseIntArray sectionIndex, SparseIntArray itemIndex) {
        sectionIndex.clear();
        itemIndex.clear();
        int pos = 0;
        for (int i = 0; i < list.size(); i++) {
            QMUISection<H, T> section = list.get(i);
            if (section.isLocked()) {
                continue;
            }
            sectionIndex.append(pos, i);
            itemIndex.append(pos, ITEM_INDEX_SECTION_HEADER);
            pos++;
            if (section.isFold() || section.getItemCount() == 0) {
                continue;
            }
            if (section.isExistBeforeDataToLoad()) {
                sectionIndex.append(pos, i);
                itemIndex.append(pos, ITEM_INDEX_LOAD_BEFORE);
                pos++;
            }

            for (int j = 0; j < section.getItemCount(); j++) {
                sectionIndex.append(pos, i);
                itemIndex.append(pos, j);
                pos++;
            }

            if (section.isExistAfterDataToLoad()) {
                sectionIndex.append(pos, i);
                itemIndex.append(pos, ITEM_INDEX_LOAD_AFTER);
                pos++;
            }
        }
    }

    @Override
    public int getOldListSize() {
        return mOldSectionIndex.size();
    }

    @Override
    public int getNewListSize() {
        return mNewSectionIndex.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        int oldSectionIndex = mOldSectionIndex.get(oldItemPosition);
        int oldItemIndex = mOldItemIndex.get(oldItemPosition);

        int newSectionIndex = mNewSectionIndex.get(newItemPosition);
        int newItemIndex = mNewItemIndex.get(newItemPosition);

        QMUISection<H, T> oldModel = mOldList.get(oldSectionIndex);
        QMUISection<H, T> newModel = mNewList.get(newSectionIndex);

        if (!oldModel.getHeader().isSameItem(newModel.getHeader())) {
            return false;
        }

        if (oldItemIndex < 0 && oldItemIndex == newItemIndex) {
            return true;
        }

        if (oldItemIndex < 0 || newItemIndex < 0) {
            return false;
        }
        T oldItem = oldModel.getItemAt(oldItemIndex);
        T newItem = newModel.getItemAt(newItemIndex);

        return (oldItem == null && newItem == null) ||
                (oldItem != null && newItem != null && oldItem.isSameItem(newItem));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        int oldSectionIndex = mOldSectionIndex.get(oldItemPosition);
        int oldItemIndex = mOldItemIndex.get(oldItemPosition);

        int newSectionIndex = mNewSectionIndex.get(newItemPosition);
        int newItemIndex = mNewItemIndex.get(newItemPosition);

        QMUISection<H, T> oldModel = mOldList.get(oldSectionIndex);
        QMUISection<H, T> newModel = mNewList.get(newSectionIndex);

        if (oldItemIndex == ITEM_INDEX_SECTION_HEADER) {
            return oldModel.isFold() == newModel.isFold() &&
                    oldModel.getHeader().isSameContent(newModel.getHeader());
        }

        if (oldItemIndex == ITEM_INDEX_LOAD_BEFORE || oldItemIndex == ITEM_INDEX_LOAD_AFTER) {
            // forced to return false，so we can trigger to load more
            // in QMUIStickySectionAdapter.onViewAttachedToWindow
            return false;
        }

        T oldItem = oldModel.getItemAt(oldItemIndex);
        T newItem = newModel.getItemAt(newItemIndex);

        return (oldItem == null && newItem == null) ||
                (oldItem != null && newItem != null && oldItem.isSameContent(newItem));
    }
}
