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

package com.qmuiteam.qmui.widget.dialog;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class QMUIBottomSheetListAdapter extends RecyclerView.Adapter<QMUIBottomSheetListAdapter.VH> {

    public static final int ITEM_TYPE_HEADER = 1;
    public static final int ITEM_TYPE_FOOTER = 2;
    public static final int ITEM_TYPE_NORMAL = 3;

    @Nullable
    private View mHeaderView;
    @Nullable
    private View mFooterView;
    private List<QMUIBottomSheetListItemModel> mData = new ArrayList<>();
    private final boolean mNeedMark;
    private final boolean mGravityCenter;
    private int mCheckedIndex = -1;
    private OnItemClickListener mOnItemClickListener;

    public QMUIBottomSheetListAdapter(boolean needMark, boolean gravityCenter){
        mNeedMark = needMark;
        mGravityCenter = gravityCenter;
    }

    public void setCheckedIndex(int checkedIndex) {
        mCheckedIndex = checkedIndex;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setData(@Nullable View headerView,
                        @Nullable View footerView,
                        List<QMUIBottomSheetListItemModel> data) {
        mHeaderView = headerView;
        mFooterView = footerView;
        mData.clear();
        if (data != null) {
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if(mHeaderView != null){
            if(position == 0){
                return ITEM_TYPE_HEADER;
            }
        }
        if(position == getItemCount() - 1){
            if(mFooterView != null){
                return ITEM_TYPE_FOOTER;
            }
        }
        return ITEM_TYPE_NORMAL;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_TYPE_HEADER){
            return new VH(mHeaderView);
        }else if(viewType == ITEM_TYPE_FOOTER){
            return new VH(mFooterView);
        }
        final VH vh = new VH(new QMUIBottomSheetListItemView(
                parent.getContext(), mNeedMark, mGravityCenter));
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null){
                    int adapterPosition = vh.getAdapterPosition();
                    int dataPos = mHeaderView != null ? adapterPosition - 1 : adapterPosition;
                    mOnItemClickListener.onClick(vh, dataPos, mData.get(dataPos));
                }
            }
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        if(holder.getItemViewType() != ITEM_TYPE_NORMAL){
            return;
        }
        if(mHeaderView != null){
            position--;
        }
        QMUIBottomSheetListItemModel itemModel = mData.get(position);
        QMUIBottomSheetListItemView itemView = (QMUIBottomSheetListItemView) holder.itemView;
        itemView.render(itemModel, position == mCheckedIndex);
    }

    @Override
    public int getItemCount() {
        return mData.size() + (mHeaderView != null ? 1 : 0) + (mFooterView != null ? 1 : 0);
    }

    static class VH extends RecyclerView.ViewHolder {

        public VH(@NonNull View itemView) {
            super(itemView);
        }
    }

    interface OnItemClickListener {
        void onClick(VH vh, int dataPos, QMUIBottomSheetListItemModel model);
    }
}
