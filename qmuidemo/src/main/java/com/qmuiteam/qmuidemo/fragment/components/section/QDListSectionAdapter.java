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


package com.qmuiteam.qmuidemo.fragment.components.section;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.section.QMUISection;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionAdapter;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.model.SectionHeader;
import com.qmuiteam.qmuidemo.model.SectionItem;
import com.qmuiteam.qmuidemo.view.QDLoadingItemView;
import com.qmuiteam.qmuidemo.view.QDSectionHeaderView;

import static com.qmuiteam.qmui.widget.section.QMUISection.ITEM_INDEX_SECTION_HEADER;

public class QDListSectionAdapter extends QMUIStickySectionAdapter<SectionHeader, SectionItem, QMUIStickySectionAdapter.ViewHolder> {


    @Override
    protected void onBind(final ViewHolder holder, final int position, QMUISection<SectionHeader, SectionItem> section, int itemIndex) {
        if (itemIndex == ITEM_INDEX_SECTION_HEADER) {
            QDSectionHeaderView itemView = (QDSectionHeaderView) holder.itemView;
            itemView.render(section.getHeader(), section.isFold());
            itemView.getArrowView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.isForStickyHeader ? position : holder.getAdapterPosition();
                    toggleFold(pos, false);
                }
            });
        } else if (itemIndex >= 0) {
            ((TextView) holder.itemView).setText(section.getItemAt(itemIndex).getText());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        View view;
        Context context = viewGroup.getContext();
        if (type == ITEM_TYPE_SECTION_HEADER) {
            view = new QDSectionHeaderView(context);
        } else if (type == ITEM_TYPE_SECTION_ITEM) {
            int paddingHor = QMUIDisplayHelper.dp2px(context, 24);
            int paddingVer = QMUIDisplayHelper.dp2px(context, 16);
            TextView tv = new TextView(context);
            tv.setTextSize(14);
            tv.setBackgroundColor(ContextCompat.getColor(context, R.color.qmui_config_color_gray_9));
            tv.setTextColor(Color.DKGRAY);
            tv.setPadding(paddingHor, paddingVer, paddingHor, paddingVer);
            view = tv;
        } else if (type == ITEM_TYPE_SECTION_LOADING) {
            view = new QDLoadingItemView(context);
        } else {
            view = new View(viewGroup.getContext());
        }
        return new ViewHolder(view);
    }
}
