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
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import static com.qmuiteam.qmui.widget.section.QMUISection.ITEM_INDEX_CUSTOM_OFFSET;
import static com.qmuiteam.qmui.widget.section.QMUISection.ITEM_INDEX_LOAD_AFTER;
import static com.qmuiteam.qmui.widget.section.QMUISection.ITEM_INDEX_LOAD_BEFORE;
import static com.qmuiteam.qmui.widget.section.QMUISection.ITEM_INDEX_SECTION_HEADER;

public abstract class QMUIStickySectionAdapter<
        H extends QMUISection.Model<H>, T extends QMUISection.Model<T>, VH extends QMUIStickySectionAdapter.ViewHolder> extends RecyclerView.Adapter<VH> {
    private static final String TAG = "StickySectionAdapter";
    public static final int ITEM_TYPE_UNKNOWN = -1;
    public static final int ITEM_TYPE_SECTION_HEADER = 0;
    public static final int ITEM_TYPE_SECTION_ITEM = 1;
    public static final int ITEM_TYPE_SECTION_LOADING = 2;
    public static final int ITEM_TYPE_CUSTOM_OFFSET = 1000;

    private List<QMUISection<H, T>> mBackupData = new ArrayList<>();
    private List<QMUISection<H, T>> mCurrentData = new ArrayList<>();

    private SparseIntArray mSectionIndex = new SparseIntArray();
    private SparseIntArray mItemIndex = new SparseIntArray();
    private ArrayList<QMUISection<H, T>> mLoadingBeforeSections = new ArrayList<>(2);
    private ArrayList<QMUISection<H, T>> mLoadingAfterSections = new ArrayList<>(2);

    private Callback<H, T> mCallback;
    private ViewCallback mViewCallback;

    /**
     * see {@link #setData(List, boolean, boolean)}
     *
     * @param data section list
     */
    public final void setData(@Nullable List<QMUISection<H, T>> data) {
        setData(data, true);
    }

    /**
     * see {@link #setData(List, boolean, boolean)}
     *
     * @param data section list
     * @param onlyMutateState This is used to backup for next diff. True to use shallow copy, false tp use deep copy.
     */
    public final void setData(@Nullable List<QMUISection<H, T>> data, boolean onlyMutateState){
        setData(data, onlyMutateState, true);
    }

    /**
     * set the new data to the adapter, this will trigger diff between new data and old data.
     * you should pay attention to the state of your data in memory. if new data and old data
     * reference to the same data in memory, the diff will fail. This is why the parameter
     * onlyMutateState exists:
     * if onlyMutateState == true, shallow copy is used to backup for next diff. You must sure H, T in memory is
     * different between old data and new data
     * if onlyMutateState == false, deep copy is used to backup for next diff. It's safe, but it will consume
     * unnecessary performance if your new data is different in memory.
     *
     * @param data            section list
     * @param onlyMutateState This is used to backup for next diff. True to use shallow copy, false tp use deep copy.
     * @param checkLock       check section lock
     */
    public final void setData(@Nullable List<QMUISection<H, T>> data, boolean onlyMutateState, boolean checkLock) {
        mLoadingBeforeSections.clear();
        mLoadingAfterSections.clear();
        mCurrentData.clear();
        if (data != null) {
            mCurrentData.addAll(data);
        }
        beforeDiffInSet(mBackupData, mCurrentData);
        if(!mCurrentData.isEmpty() && checkLock){
            lock(mCurrentData.get(0));
        }
        diff(true, onlyMutateState);
    }

    /**
     * Subclasses override this method to fill some info to new section list if need.
     * For example, assume the user expand some section by click event, these action while
     * modify old section list, but the new section list knows nothing for user action.
     * so this method is a chance to synchronize some info from old section list.
     *
     * @param oldData old section list
     * @param newData new section list
     */
    protected void beforeDiffInSet(List<QMUISection<H, T>> oldData, List<QMUISection<H, T>> newData) {

    }

    /**
     *
     * @param data              section list
     * @param onlyMutateState   this is used to backup for next diff. True to use shallow copy, false tp use deep copy.
     */
    public final void setDataWithoutDiff(@Nullable List<QMUISection<H, T>> data, boolean onlyMutateState){
        setDataWithoutDiff(data, onlyMutateState, true);
    }

    /**
     * same as {@link #setData(List, boolean)}, but do't use {@link DiffUtil},
     * use {@link #notifyDataSetChanged()} directly.
     *
     * @param data            section list
     * @param onlyMutateState this is used to backup for next diff. True to use shallow copy, false tp use deep copy.
     * @param checkLock       check section lock
     */
    public final void setDataWithoutDiff(@Nullable List<QMUISection<H, T>> data, boolean onlyMutateState, boolean checkLock) {
        mLoadingBeforeSections.clear();
        mLoadingAfterSections.clear();
        mCurrentData.clear();
        if (data != null) {
            mCurrentData.addAll(data);
        }
        if(checkLock && !mCurrentData.isEmpty()){
            lock(mCurrentData.get(0));
        }
        // only used to generate index info
        QMUISectionDiffCallback callback = createDiffCallback(mBackupData, mCurrentData);
        callback.cloneNewIndexTo(mSectionIndex, mItemIndex);
        notifyDataSetChanged();
        mBackupData.clear();
        for (QMUISection<H, T> section : mCurrentData) {
            mBackupData.add(onlyMutateState ? section.mutate() : section.cloneForDiff());
        }
    }

    private void diff(boolean newDataSet, boolean onlyMutateState) {
        QMUISectionDiffCallback callback = createDiffCallback(mBackupData, mCurrentData);
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


    /**
     * section data is not changed, only custom item index may changed, so we also need to regenerate index
     */
    public void refreshCustomData() {
        QMUISectionDiffCallback callback = createDiffCallback(mBackupData, mCurrentData);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(callback, false);
        callback.cloneNewIndexTo(mSectionIndex, mItemIndex);
        diffResult.dispatchUpdatesTo(this);
    }

    protected QMUISectionDiffCallback<H, T> createDiffCallback(
            List<QMUISection<H, T>> lastData, List<QMUISection<H, T>> currentData) {
        return new QMUISectionDiffCallback<>(lastData, currentData);
    }

    public void setCallback(Callback<H, T> callback) {
        mCallback = callback;
    }

    void setViewCallback(ViewCallback viewCallback) {
        mViewCallback = viewCallback;
    }


    public int getItemIndex(int position) {
        if (position < 0 || position >= mItemIndex.size()) {
            return QMUISection.ITEM_INDEX_UNKNOWN;
        }
        return mItemIndex.get(position);
    }

    public int getSectionIndex(int position) {
        if (position < 0 || position >= mSectionIndex.size()) {
            return QMUISection.SECTION_INDEX_UNKNOWN;
        }
        return mSectionIndex.get(position);
    }

    @Nullable
    public QMUISection<H, T> getSection(int position) {
        if (position < 0 || position >= mSectionIndex.size()) {
            return null;
        }
        int sectionIndex = mSectionIndex.get(position);
        if (sectionIndex < 0 || sectionIndex >= mCurrentData.size()) {
            return null;
        }
        return mCurrentData.get(sectionIndex);
    }

    @Nullable
    public QMUISection<H, T> getSectionDirectly(int index) {
        if (index < 0 || index >= mCurrentData.size()) {
            return null;
        }
        return mCurrentData.get(index);
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

        if (isLoadBefore) {
            mLoadingBeforeSections.remove(section);
        } else {
            mLoadingAfterSections.remove(section);
        }

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
        if (index < 0 || index >= mCurrentData.size()) {
            return;
        }
        section.setLocked(false);
        lockBefore(index - 1, lockPrevious);
        lockAfter(index + 1, lockAfter);
    }

    private void lockBefore(int current, boolean needLock) {
        while (current >= 0) {
            QMUISection<H, T> section = mCurrentData.get(current);
            if (needLock) {
                section.setLocked(true);
            } else {
                section.setLocked(false);
                needLock = !section.isFold() && section.isExistBeforeDataToLoad()
                        && !section.isErrorToLoadBefore();
            }
            current--;
        }
    }

    private void lockAfter(int current, boolean needLock) {
        while (current < mCurrentData.size()) {
            QMUISection<H, T> section = mCurrentData.get(current);
            if (needLock) {
                section.setLocked(true);
            } else {
                section.setLocked(false);
                needLock = !section.isFold() && section.isExistAfterDataToLoad()
                        && !section.isErrorToLoadAfter();
            }
            current++;
        }
    }


    /**
     * scroll to special section header
     *
     * @param targetSection
     * @param scrollToTop   True to scroll to recyclerView Top, false to scroll to visible area.
     */
    public void scrollToSectionHeader(@NonNull QMUISection<H, T> targetSection, boolean scrollToTop) {
        if (mViewCallback == null) {
            return;
        }
        for (int i = 0; i < mCurrentData.size(); i++) {
            QMUISection<H, T> section = mCurrentData.get(i);
            if (targetSection.getHeader().isSameItem(section.getHeader())) {
                if (section.isLocked()) {
                    lock(section);
                    diff(false, true);
                    safeScrollToSection(section, scrollToTop);
                } else {
                    safeScrollToSection(section, scrollToTop);
                }
                return;
            }
        }

    }


    private void safeScrollToSection(@NonNull QMUISection<H, T> targetSection, boolean scrollToTop) {
        for (int i = 0; i < mSectionIndex.size(); i++) {
            int position = mSectionIndex.keyAt(i);
            int sectionIndex = mSectionIndex.valueAt(i);
            if (sectionIndex < 0 || sectionIndex >= mCurrentData.size()) {
                continue;
            }
            int itemIndex = mItemIndex.get(position);
            if (itemIndex == ITEM_INDEX_SECTION_HEADER) {
                QMUISection<H, T> temp = mCurrentData.get(sectionIndex);
                if (temp.getHeader().isSameItem(targetSection.getHeader())) {
                    mViewCallback.scrollToPosition(position, true, scrollToTop);
                    return;
                }
            }
        }
    }


    /**
     * scroll to special section item
     *
     * @param targetSection section info. if your items are not repeated in different section,
     *                      you can use null for this method.
     * @param targetItem    item info
     * @param scrollToTop   True to scroll to recyclerView Top, false to scroll to visible area.
     */
    public void scrollToSectionItem(@Nullable QMUISection<H, T> targetSection, @NonNull T targetItem, boolean scrollToTop) {
        if (mViewCallback == null) {
            return;
        }
        // can not trust mItemIndex, maybe the section owned this item is folded
        // if this happened, we should unfold the section
        for (int i = 0; i < mCurrentData.size(); i++) {
            QMUISection<H, T> section = mCurrentData.get(i);
            if ((targetSection == null && section.existItem(targetItem)) || targetSection == section) {
                if (section.isFold() || section.isLocked()) {
                    // unlock this section
                    section.setFold(false);
                    lock(section);
                    diff(false, true);
                    safeScrollToSectionItem(section, targetItem, scrollToTop);
                } else {
                    safeScrollToSectionItem(section, targetItem, scrollToTop);
                }
                return;
            }
        }
    }

    private void safeScrollToSectionItem(@NonNull QMUISection<H, T> targetSection, @NonNull T item, boolean scrollToTop) {
        for (int i = 0; i < mItemIndex.size(); i++) {
            int position = mItemIndex.keyAt(i);
            int itemIndex = mItemIndex.valueAt(i);
            if (itemIndex < 0) {
                continue;
            }
            QMUISection<H, T> section = getSection(position);
            if (section != targetSection) {
                continue;
            }
            if (section.getItemAt(itemIndex).isSameItem(item)) {
                mViewCallback.scrollToPosition(position, false, scrollToTop);
                return;
            }
        }
    }

    /**
     * only for custom item
     *
     * @param sectionIndex
     * @param customItemIndex
     * @param unFoldTargetSection
     * @return
     */
    public int findCustomPosition(int sectionIndex, int customItemIndex, boolean unFoldTargetSection) {
        int itemIndex = QMUISection.ITEM_INDEX_CUSTOM_OFFSET + customItemIndex;
        return findPosition(sectionIndex, itemIndex, unFoldTargetSection);
    }

    /**
     * find position by sectionIndex and itemIndex
     *
     * @param sectionIndex
     * @param itemIndex
     * @param unFoldTargetSection
     * @return
     */
    public int findPosition(int sectionIndex, int itemIndex, boolean unFoldTargetSection) {
        if (unFoldTargetSection && sectionIndex >= 0) {
            QMUISection<H, T> section = mCurrentData.get(sectionIndex);
            if (section != null && section.isFold()) {
                section.setFold(false);
                lock(section);
                diff(false, true);
            }
        }
        for (int i = 0; i < getItemCount(); i++) {
            if (mSectionIndex.get(i) != sectionIndex) {
                continue;
            }
            if (mItemIndex.get(i) == itemIndex) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    /**
     * find position by positionFinder
     *
     * @param positionFinder
     * @param unFoldTargetSection
     * @return
     */
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
    public final int getItemCount() {
        return mItemIndex.size();
    }

    @NonNull
    @Override
    public final VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        if (type == ITEM_TYPE_SECTION_HEADER) {
            return onCreateSectionHeaderViewHolder(viewGroup);
        } else if (type == ITEM_TYPE_SECTION_ITEM) {
            return onCreateSectionItemViewHolder(viewGroup);
        } else if (type == ITEM_TYPE_SECTION_LOADING) {
            return onCreateSectionLoadingViewHolder(viewGroup);
        } else {
            return onCreateCustomItemViewHolder(viewGroup, type - ITEM_TYPE_CUSTOM_OFFSET);
        }
    }

    @Override
    public final void onBindViewHolder(@NonNull final VH vh, int position) {
        final int stickyPosition = position;
        QMUISection<H, T> section = getSection(position);
        int itemIndex = getItemIndex(position);
        if (itemIndex == ITEM_INDEX_SECTION_HEADER) {
            onBindSectionHeader(vh, position, section);
        } else if (itemIndex >= 0) {
            onBindSectionItem(vh, position, section, itemIndex);
        } else if (itemIndex == ITEM_INDEX_LOAD_BEFORE || itemIndex == ITEM_INDEX_LOAD_AFTER) {
            onBindSectionLoadingItem(vh, position, section, itemIndex == ITEM_INDEX_LOAD_BEFORE);
        } else {
            onBindCustomItem(vh, position, section, itemIndex - QMUISection.ITEM_INDEX_CUSTOM_OFFSET);
        }
        if (itemIndex == ITEM_INDEX_LOAD_AFTER) {
            vh.isLoadBefore = false;
        } else if (itemIndex == ITEM_INDEX_LOAD_BEFORE) {
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

    @NonNull
    protected abstract VH onCreateSectionHeaderViewHolder(@NonNull ViewGroup viewGroup);

    @NonNull
    protected abstract VH onCreateSectionItemViewHolder(@NonNull ViewGroup viewGroup);

    @NonNull
    protected abstract VH onCreateSectionLoadingViewHolder(@NonNull ViewGroup viewGroup);

    @NonNull
    protected abstract VH onCreateCustomItemViewHolder(@NonNull ViewGroup viewGroup, int type);


    protected void onBindSectionHeader(VH holder, int position, QMUISection<H, T> section) {

    }

    protected void onBindSectionItem(VH holder, int position, QMUISection<H, T> section, int itemIndex) {

    }

    protected void onBindSectionLoadingItem(VH holder, int position, QMUISection<H, T> section, boolean loadingBefore) {

    }

    protected void onBindCustomItem(VH holder, int position, @Nullable QMUISection<H, T> section, int itemIndex) {

    }


    @Override
    public final int getItemViewType(int position) {
        int itemIndex = getItemIndex(position);
        if (itemIndex == QMUISection.ITEM_INDEX_UNKNOWN) {
            // QMUIStickySectionItemDecoration uses findFirstVisibleItemPosition to get the layout position
            // it may be exceed the adapter position range if layout is not updated in time
            Log.e(TAG, "the item index is undefined, you may need to check your data if not called by QMUIStickySectionItemDecoration.");
            return ITEM_TYPE_UNKNOWN;
        }
        if (itemIndex == ITEM_INDEX_SECTION_HEADER) {
            return ITEM_TYPE_SECTION_HEADER;
        } else if (itemIndex == ITEM_INDEX_LOAD_BEFORE || itemIndex == ITEM_INDEX_LOAD_AFTER) {
            return ITEM_TYPE_SECTION_LOADING;
        } else if (itemIndex >= 0) {
            return ITEM_TYPE_SECTION_ITEM;
        } else {
            return ITEM_TYPE_CUSTOM_OFFSET + getCustomItemViewType(itemIndex - ITEM_INDEX_CUSTOM_OFFSET, position);
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull VH holder) {
        if (holder.getItemViewType() == ITEM_TYPE_SECTION_LOADING && mCallback != null) {
            if (!holder.isLoadError) {
                QMUISection<H, T> section = getSection(holder.getAdapterPosition());
                if (section != null) {
                    if (holder.isLoadBefore) {
                        if (mLoadingBeforeSections.contains(section)) {
                            return;
                        }
                        mLoadingBeforeSections.add(section);
                        mCallback.loadMore(section, true);
                    } else {
                        if (mLoadingAfterSections.contains(section)) {
                            return;
                        }
                        mLoadingAfterSections.add(section);
                        mCallback.loadMore(section, false);
                    }

                }
            }
        }
    }

    protected int getCustomItemViewType(int itemIndex, int position) {
        return ITEM_TYPE_UNKNOWN;
    }

    public interface Callback<H extends QMUISection.Model<H>, T extends QMUISection.Model<T>> {
        void loadMore(QMUISection<H, T> section, boolean loadMoreBefore);

        void onItemClick(ViewHolder holder, int position);

        boolean onItemLongClick(ViewHolder holder, int position);
    }

    public interface ViewCallback {
        void scrollToPosition(int position, boolean isSectionHeader, boolean scrollToTop);

        @Nullable
        RecyclerView.ViewHolder findViewHolderForAdapterPosition(int position);

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
