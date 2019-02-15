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

import java.util.ArrayList;
import java.util.List;

public class QMUISection<H extends QMUISection.Model<H>, T extends QMUISection.Model<T>> {
    public static final int SECTION_INDEX_UNKNOWN = -1;
    public static final int ITEM_INDEX_UNKNOWN = -1;
    public static final int ITEM_INDEX_SECTION_HEADER = -2;
    public static final int ITEM_INDEX_LOAD_BEFORE = -3;
    public static final int ITEM_INDEX_LOAD_AFTER = -4;
    /**
     * if add internal index, we should update this item
     */
    public static final int ITEM_INDEX_INTERNAL_END = -4;
    /**
     * offset custom index to reduce conflict with internal index
     */
    public static final int ITEM_INDEX_CUSTOM_OFFSET = -1000;

    private H mHeader;
    private ArrayList<T> mItemList;
    private boolean mIsFold;
    private boolean mIsLocked;
    private boolean mExistBeforeDataToLoad;
    private boolean mExistAfterDataToLoad;
    private boolean mIsErrorToLoadBefore = false;
    private boolean mIsErrorToLoadAfter = false;


    public QMUISection(@NonNull H header, @Nullable List<T> itemList) {
        this(header, itemList, false);
    }

    public QMUISection(@NonNull H header, @Nullable List<T> itemList, boolean isFold) {
        this(header, itemList, isFold, false, false, false);
    }


    public QMUISection(@NonNull H header, @Nullable List<T> itemList, boolean isFold,
                       boolean isLocked, boolean existBeforeDataToLoad, boolean existAfterDataToLoad) {
        mHeader = header;
        mItemList = new ArrayList<>();
        if (itemList != null) {
            mItemList.addAll(itemList);
        }
        mIsFold = isFold;
        mIsLocked = isLocked;
        mExistBeforeDataToLoad = existBeforeDataToLoad;
        mExistAfterDataToLoad = existAfterDataToLoad;
    }

    public H getHeader() {
        return mHeader;
    }

    public boolean isFold() {
        return mIsFold;
    }

    public void setFold(boolean fold) {
        mIsFold = fold;
    }

    public boolean isLocked() {
        return mIsLocked;
    }

    public void setLocked(boolean locked) {
        mIsLocked = locked;
    }

    public boolean isExistBeforeDataToLoad() {
        return mExistBeforeDataToLoad;
    }

    public void setExistBeforeDataToLoad(boolean existBeforeDataToLoad) {
        mExistBeforeDataToLoad = existBeforeDataToLoad;
    }

    public boolean isExistAfterDataToLoad() {
        return mExistAfterDataToLoad;
    }

    public void setExistAfterDataToLoad(boolean existAfterDataToLoad) {
        mExistAfterDataToLoad = existAfterDataToLoad;
    }

    public boolean isErrorToLoadBefore() {
        return mIsErrorToLoadBefore;
    }

    public void setErrorToLoadBefore(boolean errorToLoadBefore) {
        mIsErrorToLoadBefore = errorToLoadBefore;
    }

    public boolean isErrorToLoadAfter() {
        return mIsErrorToLoadAfter;
    }

    public void setErrorToLoadAfter(boolean errorToLoadAfter) {
        mIsErrorToLoadAfter = errorToLoadAfter;
    }

    public int getItemCount() {
        return mItemList.size();
    }

    public T getItemAt(int index){
        if (index < 0 || index >= mItemList.size()) {
            return null;
        }
        return mItemList.get(index);
    }

    public boolean existItem(T item){
        return mItemList.contains(item);
    }

    public void finishLoadMore(@Nullable List<T> data, boolean isLoadBefore, boolean existMoreData){
        if(isLoadBefore){
            if(data != null){
                mItemList.addAll(0, data);
            }
            mExistBeforeDataToLoad = existMoreData;

        }else{
            if(data != null){
                mItemList.addAll(data);
            }
            mExistAfterDataToLoad = existMoreData;
        }
    }

    public void cloneStatusTo(QMUISection<H, T> other) {
        other.mExistBeforeDataToLoad = mExistBeforeDataToLoad;
        other.mExistAfterDataToLoad = mExistAfterDataToLoad;
        other.mIsFold = mIsFold;
        other.mIsLocked = mIsLocked;
        other.mIsErrorToLoadBefore = mIsErrorToLoadBefore;
        other.mIsErrorToLoadAfter = mIsErrorToLoadAfter;
    }

    public QMUISection<H, T> mutate(){
        QMUISection<H, T> section = new QMUISection<>(mHeader, mItemList,
                mIsFold, mIsLocked, mExistBeforeDataToLoad, mExistAfterDataToLoad);
        section.mIsErrorToLoadBefore = mIsErrorToLoadBefore;
        section.mIsErrorToLoadAfter = mIsErrorToLoadAfter;
        return section;
    }

    public QMUISection<H, T> cloneForDiff() {
        ArrayList<T> newList = new ArrayList<>();
        for (T item : mItemList) {
            newList.add(item.cloneForDiff());
        }
        QMUISection<H, T> section = new QMUISection<>(mHeader.cloneForDiff(), newList,
                mIsFold, mIsLocked, mExistBeforeDataToLoad, mExistAfterDataToLoad);
        section.mIsErrorToLoadBefore = mIsErrorToLoadBefore;
        section.mIsErrorToLoadAfter = mIsErrorToLoadAfter;
        return section;
    }

    public static final boolean isCustomItemIndex(int index){
        return index < ITEM_INDEX_INTERNAL_END;
    }

    public interface Model<T> {
        /**
         * Called by QMUISection to clone this model for next diff if the adapter data is mutable.
         * you just need clone the fields needed for diff
         *
         * @return another instance of T
         */
        T cloneForDiff();

        /**
         * Called by QMUIDiffCallback decide whether two object represent the same T.
         * For example, if your items have unique ids, this method should check their id equality.
         *
         * @param other the object to compare
         * @return True if the two items represent the same object or false if they are different.
         */
        boolean isSameItem(T other);

        /**
         * Called by the QMUIDiffCallback when it wants to check whether two items have the same data.
         * QMUIDiffCallback uses this information to detect if the contents of an item has changed.
         *
         * @param other the object to compare
         * @return True if the contents of the items are the same or false if they are different.
         */
        boolean isSameContent(T other);
    }
}
