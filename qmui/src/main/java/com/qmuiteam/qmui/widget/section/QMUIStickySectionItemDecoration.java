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

import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;

/**
 * Created by cgspine on 2018/1/20.
 */

public class QMUIStickySectionItemDecoration<VH extends QMUIStickySectionAdapter.ViewHolder>
        extends RecyclerView.ItemDecoration {

    private Callback<VH> mCallback;
    private VH mStickyHeaderViewHolder;
    private int mStickyHeaderViewPosition = RecyclerView.NO_POSITION;
    private WeakReference<ViewGroup> mWeakSectionContainer;
    private int mTargetTop = 0;

    public QMUIStickySectionItemDecoration(ViewGroup sectionContainer, @NonNull Callback<VH> callback) {
        mCallback = callback;
        mWeakSectionContainer = new WeakReference<>(sectionContainer);

        mCallback.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                mStickyHeaderViewPosition = RecyclerView.NO_POSITION;
                mCallback.invalidate();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if (positionStart <= mStickyHeaderViewPosition) {
                    mStickyHeaderViewPosition = RecyclerView.NO_POSITION;
                    mCallback.invalidate();
                }
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                if (fromPosition == mStickyHeaderViewPosition ||
                        toPosition == mStickyHeaderViewPosition) {
                    mStickyHeaderViewPosition = RecyclerView.NO_POSITION;
                    mCallback.invalidate();
                }
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                // stickyViewHolder should update when the adapter updates relative view holder
                super.onItemRangeChanged(positionStart, itemCount);
                if (mStickyHeaderViewPosition >= positionStart
                        && mStickyHeaderViewPosition < positionStart + itemCount
                        && mStickyHeaderViewHolder != null
                        && mWeakSectionContainer.get() != null) {
                    mStickyHeaderViewPosition = RecyclerView.NO_POSITION;
                    mCallback.invalidate();
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                if (mStickyHeaderViewPosition >= positionStart
                        && mStickyHeaderViewPosition < positionStart + itemCount) {
                    mStickyHeaderViewPosition = RecyclerView.NO_POSITION;
                    setHeaderVisibility(false);
                }
            }
        });
    }

    private void setHeaderVisibility(boolean visibility) {
        ViewGroup sectionContainer = mWeakSectionContainer.get();
        if (sectionContainer == null) {
            return;
        }
        sectionContainer.setVisibility(visibility ? View.VISIBLE : View.GONE);
        mCallback.onHeaderVisibilityChanged(visibility);
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent,
                           @NonNull RecyclerView.State state) {


        ViewGroup sectionContainer = mWeakSectionContainer.get();
        if (sectionContainer == null || parent.getChildCount() == 0) {
            return;
        }

        RecyclerView.Adapter adapter = parent.getAdapter();
        if (adapter == null) {
            return;
        }

        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (!(layoutManager instanceof LinearLayoutManager)) {
            setHeaderVisibility(false);
            return;
        }
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
        int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

        if (firstVisibleItemPosition == RecyclerView.NO_POSITION) {
            setHeaderVisibility(false);
            return;
        }

        int headerPos = mCallback.getRelativeStickyItemPosition(firstVisibleItemPosition);
        if (headerPos == RecyclerView.NO_POSITION) {
            setHeaderVisibility(false);
            return;
        }
        int itemType = mCallback.getItemViewType(headerPos);
        if (itemType == QMUIStickySectionAdapter.ITEM_TYPE_UNKNOWN) {
            setHeaderVisibility(false);
            return;
        }
        if (mStickyHeaderViewHolder == null || mStickyHeaderViewHolder.getItemViewType() != itemType) {
            mStickyHeaderViewHolder = createStickyViewHolder(parent, headerPos, itemType);
        }

        if (mStickyHeaderViewPosition != headerPos) {
            mStickyHeaderViewPosition = headerPos;
            bindStickyViewHolder(sectionContainer, mStickyHeaderViewHolder, headerPos);
        }

        setHeaderVisibility(true);

        int contactPoint = sectionContainer.getHeight() - 1;
        final View childInContact = parent.findChildViewUnder(parent.getWidth() / 2, contactPoint);
        if (childInContact == null) {
            mTargetTop = parent.getTop();
            ViewCompat.offsetTopAndBottom(sectionContainer, mTargetTop - sectionContainer.getTop());
            return;
        }

        if (mCallback.isHeaderItem(parent.getChildAdapterPosition(childInContact))) {
            mTargetTop = childInContact.getTop() + parent.getTop() - sectionContainer.getHeight();
            ViewCompat.offsetTopAndBottom(sectionContainer, mTargetTop - sectionContainer.getTop());
            return;
        }

        mTargetTop = parent.getTop();
        ViewCompat.offsetTopAndBottom(sectionContainer, mTargetTop - sectionContainer.getTop());
    }

    public int getTargetTop() {
        return mTargetTop;
    }


    private VH createStickyViewHolder(RecyclerView recyclerView, int position, int itemType) {
        VH vh = mCallback.createViewHolder(recyclerView, itemType);
        vh.isForStickyHeader = true;
        return vh;
    }

    private void bindStickyViewHolder(ViewGroup sectionContainer, VH viewHolder, int position) {
        mCallback.bindViewHolder(viewHolder, position);
        sectionContainer.removeAllViews();
        sectionContainer.addView(viewHolder.itemView);
    }


    public interface Callback<ViewHolder extends QMUIStickySectionAdapter.ViewHolder> {
        /**
         * @param pos adapterPosition
         * @return sticky section header position
         */
        int getRelativeStickyItemPosition(int pos);


        boolean isHeaderItem(int pos);

        ViewHolder createViewHolder(ViewGroup parent, int viewType);

        void bindViewHolder(ViewHolder holder, int position);

        int getItemViewType(int position);

        void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer);

        void onHeaderVisibilityChanged(boolean visible);

        void invalidate();
    }
}
