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
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.section.QMUISection;
import com.qmuiteam.qmui.widget.section.QMUISectionDiffCallback;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionAdapter;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.model.SectionHeader;
import com.qmuiteam.qmuidemo.model.SectionItem;
import com.qmuiteam.qmuidemo.view.QDLoadingItemView;
import com.qmuiteam.qmuidemo.view.QDSectionHeaderView;

import java.util.List;

import static com.qmuiteam.qmui.widget.section.QMUISection.ITEM_INDEX_SECTION_HEADER;

public class QDListWithDecorationSectionAdapter extends QMUIStickySectionAdapter<SectionHeader, SectionItem, QMUIStickySectionAdapter.ViewHolder> {

    public static final int ITEM_INDEX_LIST_HEADER = QMUISection.ITEM_INDEX_DECORATION_START;
    public static final int ITEM_INDEX_LIST_FOOTER = ITEM_INDEX_LIST_HEADER + QMUISection.ITEM_INDEX_NEXT_DIRECTION;
    public static final int ITEM_INDEX_SECTION_TIP_START = ITEM_INDEX_LIST_FOOTER + QMUISection.ITEM_INDEX_NEXT_DIRECTION;
    public static final int ITEM_INDEX_SECTION_TIP_END = ITEM_INDEX_SECTION_TIP_START + QMUISection.ITEM_INDEX_NEXT_DIRECTION;

    public static final int ITEM_TYPE_LIST_HEADER = ITEM_TYPE_CUSTOM_START;
    public static final int ITEM_TYPE_LIST_FOOTER = ITEM_TYPE_LIST_HEADER + ITEM_TYPE_NEXT_DIRECTION;
    public static final int ITEM_TYPE_SECTION_TIP_START = ITEM_TYPE_LIST_FOOTER + ITEM_TYPE_NEXT_DIRECTION;
    public static final int ITEM_TYPE_SECTION_TIP_END = ITEM_TYPE_SECTION_TIP_START + ITEM_TYPE_NEXT_DIRECTION;


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
        } else if (type == ITEM_TYPE_LIST_HEADER) {
            ImageView iv = new ImageView(context);
            iv.setImageResource(R.mipmap.example_image2);
            view = iv;
        } else if (type == ITEM_TYPE_LIST_FOOTER) {
            TextView tv = new TextView(context);
            tv.setTextSize(12);
            tv.setBackgroundColor(ContextCompat.getColor(context, R.color.qmui_config_color_gray_9));
            tv.setTextColor(Color.DKGRAY);
            tv.setText(R.string.sticky_section_decoration_list_footer);
            tv.setGravity(Gravity.CENTER);
            int paddingVer = QMUIDisplayHelper.dp2px(context, 16);
            tv.setPadding(0, paddingVer, 0, paddingVer);
            view = tv;
        } else if (type == ITEM_TYPE_SECTION_TIP_START) {
            TextView tv = new TextView(context);
            tv.setTextSize(12);
            tv.setBackgroundColor(ContextCompat.getColor(context, R.color.qmui_config_color_gray_9));
            tv.setTextColor(Color.DKGRAY);
            tv.setText(R.string.sticky_section_decoration_section_top_tip);
            tv.setGravity(Gravity.CENTER);
            int paddingVer = QMUIDisplayHelper.dp2px(context, 16);
            tv.setPadding(0, paddingVer, 0, paddingVer);
            view = tv;
        } else if (type == ITEM_TYPE_SECTION_TIP_END) {
            TextView tv = new TextView(context);
            tv.setTextSize(12);
            tv.setBackgroundColor(ContextCompat.getColor(context, R.color.qmui_config_color_gray_9));
            tv.setTextColor(Color.DKGRAY);
            tv.setText(R.string.sticky_section_decoration_section_bottom_tip);
            tv.setGravity(Gravity.CENTER);
            int paddingVer = QMUIDisplayHelper.dp2px(context, 16);
            tv.setPadding(0, paddingVer, 0, paddingVer);
            view = tv;
        } else {
            view = new View(viewGroup.getContext());
        }
        return new ViewHolder(view);
    }

    @Override
    protected int getMoreItemViewType(int itemIndex, int position) {
        if (itemIndex == ITEM_INDEX_LIST_HEADER) {
            return ITEM_TYPE_LIST_HEADER;
        } else if (itemIndex == ITEM_INDEX_LIST_FOOTER) {
            return ITEM_TYPE_LIST_FOOTER;
        } else if (itemIndex == ITEM_INDEX_SECTION_TIP_START) {
            return ITEM_TYPE_SECTION_TIP_START;
        } else if (itemIndex == ITEM_INDEX_SECTION_TIP_END) {
            return ITEM_TYPE_SECTION_TIP_END;
        }
        return super.getMoreItemViewType(itemIndex, position);
    }

    @Override
    protected QMUISectionDiffCallback<SectionHeader, SectionItem> createDiffCallback(
            List<QMUISection<SectionHeader, SectionItem>> lastData,
            List<QMUISection<SectionHeader, SectionItem>> currentData) {
        return new QMUISectionDiffCallback<SectionHeader, SectionItem>(lastData, currentData) {

            @Override
            protected void onGenerateDecorationIndexBeforeSectionList(IndexGenerationInfo generationInfo, List<QMUISection<SectionHeader, SectionItem>> list) {
                generationInfo.appendWholeListDecorationIndex(ITEM_INDEX_LIST_HEADER);
            }

            @Override
            protected void onGenerateDecorationIndexAfterSectionList(IndexGenerationInfo generationInfo, List<QMUISection<SectionHeader, SectionItem>> list) {
                generationInfo.appendWholeListDecorationIndex(ITEM_INDEX_LIST_FOOTER);
            }

            @Override
            protected void onGenerateDecorationIndexBeforeItemList(IndexGenerationInfo generationInfo,
                                                                   QMUISection<SectionHeader, SectionItem> section,
                                                                   int sectionIndex) {
                if (!section.isExistBeforeDataToLoad()) {
                    generationInfo.appendIndex(sectionIndex, ITEM_INDEX_SECTION_TIP_START);
                }
            }

            @Override
            protected void onGenerateDecorationIndexAfterItemList(IndexGenerationInfo generationInfo,
                                                                  QMUISection<SectionHeader, SectionItem> section,
                                                                  int sectionIndex) {
                if (!section.isExistAfterDataToLoad()) {
                    generationInfo.appendIndex(sectionIndex, ITEM_INDEX_SECTION_TIP_END);
                }
            }

            @Override
            protected boolean areDecorationContentsTheSame(@Nullable QMUISection<SectionHeader, SectionItem> oldSection, int oldItemIndex, @Nullable QMUISection<SectionHeader, SectionItem> newSection, int newItemIndex) {
                return true;
            }
        };
    }
}
