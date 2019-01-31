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
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import static com.qmuiteam.qmui.widget.section.QMUISection.ITEM_INDEX_LOAD_AFTER;
import static com.qmuiteam.qmui.widget.section.QMUISection.ITEM_INDEX_LOAD_BEFORE;
import static com.qmuiteam.qmui.widget.section.QMUISection.ITEM_INDEX_SECTION_HEADER;

public abstract class QMUIStickySectionAdapter<
        H extends QMUISection.Model<H>, T extends QMUISection.Model<T>, VH extends QMUIStickySectionAdapter.ViewHolder> extends RecyclerView.Adapter<VH> {

    private static final String TAG = "StickySectionAdapter";
    public static final int ITEM_TYPE_SECTION_HEADER = 0;
    public static final int ITEM_TYPE_SECTION_ITEM = 1;
    public static final int ITEM_TYPE_SECTION_LOADING = 2;

    private List<QMUISection<H, T>> mBackupData = new ArrayList<>();
    private List<QMUISection<H, T>> mCurrentData = new ArrayList<>();

    private SparseIntArray mSectionIndex = new SparseIntArray();
    private SparseIntArray mItemIndex = new SparseIntArray();

    private Callback<H, T> mCallback;
    private ViewCallback mViewCallback;

    public final void setData(@Nullable List<QMUISection<H, T>> data) {
        setData(data, true);
    }

    public final void setData(@Nullable List<QMUISection<H, T>> data, boolean onlyMutateState) {
        mCurrentData.clear();
        if (data != null) {
            mCurrentData.addAll(data);
        }
        onBeforeDataDiff(mBackupData, mCurrentData);
        diff(true, onlyMutateState);
    }

    protected void onBeforeDataDiff(List<QMUISection<H, T>> currentData, List<QMUISection<H, T>> newData) {

    }

    private void diff(boolean newDataSet, boolean onlyMutateState) {
        QMUISectionDiffCallback callback = new QMUISectionDiffCallback<>(mBackupData, mCurrentData);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(callback, false);
        callback.cloneNewIndexTo(mSectionIndex, mItemIndex);
        diffResult.dispatchUpdatesTo(this);

        if (newDataSet || mBackupData.size() != mCurrentData.size()) {
            mBackupData.clear();
            for (QMUISection<H, T> section : mCurrentData) {
                mBackupData.add(onlyMutateState ? section.mutate() : section.cloneForDiff());
            }
        } else {
            //only status change, so we only copy statuses to mBackupData
            for (int i = 0; i < mCurrentData.size(); i++) {
                mCurrentData.get(i).cloneStatusTo(mBackupData.get(i));
            }
        }
    }

    public void setCallback(Callback<H, T> callback) {
        mCallback = callback;
    }

    void setViewCallback(ViewCallback viewCallback) {
        mViewCallback = viewCallback;
    }

    public int getItemIndex(int position) {
        if (position < 0 || position > mItemIndex.size()) {
            return QMUISection.ITEM_INDEX_UNDEFINED;
        }
        return mItemIndex.get(position);
    }

    public int getSectionIndex(int position) {
        if (position < 0 || position > mSectionIndex.size()) {
            return QMUISection.ITEM_INDEX_UNDEFINED;
        }
        return mSectionIndex.get(position);
    }

    @Nullable
    public QMUISection<H, T> getSection(int position) {
        if (position < 0 || position > mSectionIndex.size()) {
            return null;
        }
        int sectionIndex = mSectionIndex.get(position);
        if (sectionIndex < 0 || sectionIndex >= mCurrentData.size()) {
            return null;
        }
        return mCurrentData.get(sectionIndex);
    }

    public boolean isSectionFold(int position) {
        QMUISection<H, T> section = getSection(position);
        if (section == null) {
            return false;
        }
        return section.isFold();
    }

    @Nullable
    public T getSectionItem(int position) {
        int itemIndex = getItemIndex(position);
        if (itemIndex < 0) {
            return null;
        }
        QMUISection<H, T> section = getSection(position);
        if (section == null) {
            return null;
        }
        return section.getItemAt(itemIndex);
    }


    public void finishLoadMore(QMUISection<H, T> section, List<T> itemList,
                               boolean isLoadBefore, boolean existMoreData) {

        if (mCurrentData.indexOf(section) < 0) {
            return;
        }

        // if load before, we should focus first item in section. otherwise the new data will
        // wash current items down
        if (isLoadBefore && !section.isFold()) {
            for (int i = 0; i < mItemIndex.size(); i++) {
                int position = mItemIndex.keyAt(i);
                int itemIndex = mItemIndex.valueAt(i);
                if (itemIndex == 0 && section == getSection(position)) {
                    RecyclerView.ViewHolder focusViewHolder = mViewCallback == null ? null :
                            mViewCallback.findViewHolderForAdapterPosition(position);
                    if (focusViewHolder != null) {
                        mViewCallback.requestChildFocus(focusViewHolder.itemView);
                    }
                    break;
                }

            }
        }

        section.finishLoadMore(itemList, isLoadBefore, existMoreData);
        lock(section);

        diff(true, true);
    }

    /**
     * lock section if needed, so we can stop scroll when in loadMore
     *
     * @param section
     */
    private void lock(QMUISection<H, T> section) {
        boolean lockPrevious = !section.isFold() && section.isExistBeforeDataToLoad()
                && !section.isErrorToLoadBefore();
        boolean lockAfter = !section.isFold() && section.isExistAfterDataToLoad()
                && !section.isErrorToLoadAfter();

        int index = mCurrentData.indexOf(section);
        if (index < 0) {
            return;
        }
        section.setLocked(false);
        for (int i = 0; i < mCurrentData.size(); i++) {
            if (i < index) {
                mCurrentData.get(i).setLocked(lockPrevious);
            } else if (i > index) {
                mCurrentData.get(i).setLocked(lockAfter);
            }
        }
    }


    public void scrollToSectionHeader(QMUISection<H, T> section, boolean scrollToTop) {
        if (mViewCallback == null) {
            return;
        }
        for (int i = 0; i < mSectionIndex.size(); i++) {
            int position = mSectionIndex.keyAt(i);
            int sectionIndex = mSectionIndex.valueAt(i);
            if (sectionIndex < 0 || sectionIndex >= mCurrentData.size()) {
                continue;
            }
            int itemIndex = mItemIndex.get(position);
            if (itemIndex == ITEM_INDEX_SECTION_HEADER) {
                QMUISection<H, T> temp = mCurrentData.get(sectionIndex);
                if (temp.getHeader().isSameItem(section.getHeader())) {
                    mViewCallback.scrollToPosition(position, true, scrollToTop);
                    return;
                }
            }
        }
    }


    public void scrollToSectionItem(@NonNull T item, boolean scrollToTop) {
        if (mViewCallback == null) {
            return;
        }
        // can not trust mItemIndex, maybe the section owned this item is folded
        // if this happened, we should unfold the section
        for (int i = 0; i < mCurrentData.size(); i++) {
            QMUISection<H, T> section = mCurrentData.get(i);
            if (section.existItem(item)) {
                if (section.isFold()) {
                    // unlock this section
                    section.setFold(false);
                    lock(section);
                    diff(false, true);
                    safeScrollToSectionItem(item, scrollToTop);
                } else {
                    safeScrollToSectionItem(item, scrollToTop);
                }
                return;
            }
        }
    }

    private void safeScrollToSectionItem(@NonNull T item,  boolean scrollToTop) {
        for (int i = 0; i < mItemIndex.size(); i++) {
            int position = mItemIndex.keyAt(i);
            int itemIndex = mItemIndex.valueAt(i);
            if (itemIndex < 0) {
                continue;
            }
            QMUISection<H, T> section = getSection(position);
            if (section == null) {
                continue;
            }
            if (section.getItemAt(itemIndex).isSameItem(item)) {
                mViewCallback.scrollToPosition(position, false, scrollToTop);
                return;
            }
        }
    }

    public int findPosition(PositionFinder<H, T> positionFinder, boolean unFoldTargetSection) {
        if (!unFoldTargetSection) {
            for (int i = 0; i < getItemCount(); i++) {
                QMUISection<H, T> section = getSection(i);
                if (section == null) {
                    continue;
                }
                int itemIndex = getItemIndex(i);
                if (itemIndex == ITEM_INDEX_SECTION_HEADER) {
                    if (positionFinder.find(section, null)) {
                        return i;
                    }
                } else if (itemIndex >= 0) {
                    if (positionFinder.find(section, section.getItemAt(itemIndex))) {
                        return i;
                    }
                }
            }
            return RecyclerView.NO_POSITION;
        }
        QMUISection<H, T> targetSection = null;
        T targetItem = null;
        loop:
        for (int i = 0; i < mCurrentData.size(); i++) {
            QMUISection<H, T> section = mCurrentData.get(i);
            if (positionFinder.find(section, null)) {
                targetSection = section;
                break;
            }
            for (int j = 0; j < section.getItemCount(); j++) {
                if (positionFinder.find(section, section.getItemAt(j))) {
                    targetSection = section;
                    targetItem = section.getItemAt(j);
                    boolean isFold = section.isFold();
                    if (isFold) {
                        section.setFold(false);
                        lock(section);
                        diff(false, true);
                    }
                    break loop;
                }
            }
        }
        for (int i = 0; i < getItemCount(); i++) {
            QMUISection<H, T> section = getSection(i);
            if (section != targetSection) {
                continue;
            }
            int itemIndex = getItemIndex(i);
            if (itemIndex == ITEM_INDEX_SECTION_HEADER && targetItem == null) {
                return i;
            } else if (itemIndex >= 0) {
                if (section.getItemAt(itemIndex).isSameItem(targetItem)) {
                    return i;
                }
            }
        }
        return RecyclerView.NO_POSITION;
    }

    public void toggleFold(int position, boolean scrollToTop) {
        QMUISection<H, T> section = getSection(position);
        if (section == null) {
            return;
        }
        section.setFold(!section.isFold());
        lock(section);
        diff(false, true);
        if (scrollToTop && !section.isFold() && mViewCallback != null) {
            for (int i = 0; i < mSectionIndex.size(); i++) {
                int pos = mSectionIndex.keyAt(i);
                int itemIndex = getItemIndex(pos);
                if (itemIndex == ITEM_INDEX_SECTION_HEADER && getSection(pos) == section) {
                    mViewCallback.scrollToPosition(pos, true, true);
                    return;
                }
            }
        }
    }


    public int getRelativeStickyPosition(int position) {
        while (getItemViewType(position) != ITEM_TYPE_SECTION_HEADER) {
            position--;
            if (position < 0) {
                return RecyclerView.NO_POSITION;
            }
        }
        return position;
    }

    @Override
    public int getItemCount() {
        return mItemIndex.size();
    }


    @Override
    public void onBindViewHolder(@NonNull final VH vh, int position) {
        final int stickyPosition = position;
        QMUISection<H, T> section = getSection(position);
        int itemIndex = getItemIndex(position);
        onBind(vh, position, section, itemIndex);
        if(itemIndex == ITEM_INDEX_LOAD_AFTER){
            vh.isLoadBefore = false;
        }else if(itemIndex == ITEM_INDEX_LOAD_BEFORE){
            vh.isLoadBefore = true;
        }
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = vh.isForStickyHeader ? stickyPosition : vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && mCallback != null) {
                    mCallback.onItemClick(vh, pos);
                }
            }
        });
        vh.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int pos = vh.isForStickyHeader ? stickyPosition : vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && mCallback != null) {
                    return mCallback.onItemLongClick(vh, pos);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        int itemIndex = getItemIndex(position);
        if (itemIndex == QMUISection.ITEM_INDEX_UNDEFINED) {
            throw new RuntimeException("the item index is undefined, something is wrong in your data.");
        }
        if (itemIndex == ITEM_INDEX_SECTION_HEADER) {
            return ITEM_TYPE_SECTION_HEADER;
        } else if (itemIndex == ITEM_INDEX_LOAD_BEFORE || itemIndex == ITEM_INDEX_LOAD_AFTER) {
            return ITEM_TYPE_SECTION_LOADING;
        } else {
            return ITEM_TYPE_SECTION_ITEM;
        }

    }

    @Override
    public void onViewAttachedToWindow(@NonNull VH holder) {
        if (holder.getItemViewType() == ITEM_TYPE_SECTION_LOADING && mCallback != null) {
            if (!holder.isLoadError) {
                QMUISection<H, T> section = getSection(holder.getAdapterPosition());
                if (section != null) {
                    mCallback.loadMore(section, holder.isLoadBefore);
                }
            }
        }
    }

    protected abstract void onBind(VH holder, int position, QMUISection<H, T> section, int itemIndex);


    public interface Callback<H extends QMUISection.Model<H>, T extends QMUISection.Model<T>> {
        void loadMore(QMUISection<H, T> section, boolean loadMoreBefore);

        void onItemClick(ViewHolder holder, int position);

        boolean onItemLongClick(ViewHolder holder, int position);
    }

    public interface ViewCallback {
        void scrollToPosition(int position, boolean isSectionHeader, boolean scrollToTop);

        @Nullable RecyclerView.ViewHolder findViewHolderForAdapterPosition(int position);

        void requestChildFocus(View view);
    }

    public interface PositionFinder<H extends QMUISection.Model<H>, T extends QMUISection.Model<T>> {
        /**
         * if item == null, indicate this call for header.
         *
         * @param section
         * @param item
         * @return
         */
        boolean find(@NonNull QMUISection<H, T> section, @Nullable T item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public boolean isLoadError = false;
        public boolean isLoadBefore = false;
        public boolean isForStickyHeader = false;

        public ViewHolder(View itemView) {
            super(itemView);
        }

    }
}
