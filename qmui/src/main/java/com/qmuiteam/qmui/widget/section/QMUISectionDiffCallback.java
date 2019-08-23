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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
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
        IndexGenerationInfo generationInfo = new IndexGenerationInfo(sectionIndex, itemIndex);
        if (list.isEmpty() || !list.get(0).isLocked()) {
            onGenerateCustomIndexBeforeSectionList(generationInfo, list);
        }

        for (int i = 0; i < list.size(); i++) {
            QMUISection<H, T> section = list.get(i);
            if (section.isLocked()) {
                continue;
            }
            generationInfo.appendIndex(i, ITEM_INDEX_SECTION_HEADER);
            if (section.isFold()) {
                continue;
            }
            onGenerateCustomIndexBeforeItemList(generationInfo, section, i);
            if (section.isExistBeforeDataToLoad()) {
                generationInfo.appendIndex(i, ITEM_INDEX_LOAD_BEFORE);
            }

            for (int j = 0; j < section.getItemCount(); j++) {
                generationInfo.appendIndex(i, j);
            }

            if (section.isExistAfterDataToLoad()) {
                generationInfo.appendIndex(i, ITEM_INDEX_LOAD_AFTER);
            }
            onGenerateCustomIndexAfterItemList(generationInfo, section, i);
        }
        if (list.isEmpty()) {
            onGenerateCustomIndexAfterSectionList(generationInfo, list);
        } else {
            QMUISection lastSection = list.get(list.size() - 1);
            if (!lastSection.isLocked() && (lastSection.isFold() || !lastSection.isExistAfterDataToLoad())) {
                onGenerateCustomIndexAfterSectionList(generationInfo, list);
            }
        }
    }

    /**
     * Subclasses overrides this method to add custom view before the beginning of the list, such as list header.
     * Use {@link IndexGenerationInfo#appendWholeListCustomIndex(int)} to add index info
     *
     * @param generationInfo call generationInfo.appendWholeListCustomIndex to collect index info
     * @param list           the whole list info
     */
    protected void onGenerateCustomIndexBeforeSectionList(IndexGenerationInfo generationInfo, List<QMUISection<H, T>> list) {

    }

    /**
     * Subclasses overrides this method to add custom view after the end of the list, such as list footer.
     * Use {@link IndexGenerationInfo#appendWholeListCustomIndex(int)} to add index info
     *
     * @param generationInfo call generationInfo.appendWholeListCustomIndex to collect index info
     * @param list           the whole list info
     */
    protected void onGenerateCustomIndexAfterSectionList(IndexGenerationInfo generationInfo, List<QMUISection<H, T>> list) {

    }

    /**
     * Subclasses overrides this method to add custom view before the beginning of the section content list
     * Use {@link IndexGenerationInfo#appendCustomIndex(int, int)} to add index info
     *
     * @param generationInfo call generationInfo.appendIndex to collect index info
     * @param section        section info
     * @param sectionIndex   section index info
     */
    protected void onGenerateCustomIndexBeforeItemList(IndexGenerationInfo generationInfo, QMUISection<H, T> section, int sectionIndex) {

    }

    /**
     * Subclasses overrides this method to add custom view before the end of the section content list
     * Use {@link IndexGenerationInfo#appendIndex(int, int)} to add index info
     *
     * @param generationInfo call generationInfo.appendCustomIndex to collect index info
     * @param section        section info
     * @param sectionIndex   section index info
     */
    protected void onGenerateCustomIndexAfterItemList(IndexGenerationInfo generationInfo, QMUISection<H, T> section, int sectionIndex) {

    }

    /**
     * Subclasses overrides this method to check whether two custom items have the same data
     * @param oldSection the old section in the old list
     * @param oldItemIndex the old item index in old section
     * @param newSection the new section in the new list
     * @param newItemIndex the new item index in new section
     * @return True if the contents of the items are the same or false if they are different.
     */
    protected boolean areCustomContentsTheSame(@Nullable QMUISection<H, T> oldSection, int oldItemIndex,
                                                   @Nullable QMUISection<H, T> newSection, int newItemIndex) {
        return false;
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

        if (oldSectionIndex < 0 || newSectionIndex < 0) {
            return oldSectionIndex == newSectionIndex && oldItemIndex == newItemIndex;
        }


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

        if (newSectionIndex < 0) {
            return areCustomContentsTheSame(null, oldItemIndex, null, newItemIndex);
        }

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

        if (QMUISection.isCustomItemIndex(oldItemIndex)) {
            return areCustomContentsTheSame(oldModel, oldItemIndex, newModel, newItemIndex);
        }

        T oldItem = oldModel.getItemAt(oldItemIndex);
        T newItem = newModel.getItemAt(newItemIndex);

        return (oldItem == null && newItem == null) ||
                (oldItem != null && newItem != null && oldItem.isSameContent(newItem));
    }

    public static class IndexGenerationInfo {
        private SparseIntArray sectionIndexArray;
        private SparseIntArray itemIndexArray;
        private int currentPosition;

        private IndexGenerationInfo(SparseIntArray sectionIndex, SparseIntArray itemIndex) {
            sectionIndexArray = sectionIndex;
            itemIndexArray = itemIndex;
            currentPosition = 0;
        }

        public final void appendCustomIndex(int sectionIndex, int itemIndex) {

            int offset = QMUISection.ITEM_INDEX_CUSTOM_OFFSET + itemIndex;
            if(!QMUISection.isCustomItemIndex(offset)){
                throw new IllegalArgumentException(
                        "Index conflicts with index used internally, please use negative number for custom item");
            }
            appendIndex(sectionIndex, offset);
        }

        private final void appendIndex(int sectionIndex, int itemIndex) {
            if (sectionIndex < 0) {
                throw new IllegalArgumentException("use appendWholeListCustomIndex for whole list");
            }
            sectionIndexArray.append(currentPosition, sectionIndex);
            itemIndexArray.append(currentPosition, itemIndex);
            currentPosition++;
        }

        public final void appendWholeListCustomIndex(int itemIndex) {
            int offset = QMUISection.ITEM_INDEX_CUSTOM_OFFSET + itemIndex;
            if(!QMUISection.isCustomItemIndex(offset)){
                throw new IllegalArgumentException(
                        "Index conflicts with index used internally, please use negative number for custom item");
            }
            appendWholeListIndex(offset);
        }

        private final void appendWholeListIndex(int itemIndex) {
            sectionIndexArray.append(currentPosition, QMUISection.SECTION_INDEX_UNKNOWN);
            itemIndexArray.append(currentPosition, itemIndex);
            currentPosition++;
        }
    }
}
