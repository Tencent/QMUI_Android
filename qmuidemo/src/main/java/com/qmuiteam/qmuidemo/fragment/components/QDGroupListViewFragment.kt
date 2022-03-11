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

package com.qmuiteam.qmuidemo.fragment.components;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.QMUILoadingView;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.model.QDItemDescription;

import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * {@link QMUIGroupListView} 的使用示例。
 * Created by Kayo on 2016/11/21.
 */

@Widget(widgetClass = QMUIGroupListView.class, iconRes = R.mipmap.icon_grid_group_list_view)
public class QDGroupListViewFragment extends BaseFragment {

    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.groupListView)
    QMUIGroupListView mGroupListView;

    private QDItemDescription mQDItemDescription;

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_grouplistview, null);
        ButterKnife.bind(this, root);

        mQDItemDescription = QDDataManager.getInstance().getDescription(this.getClass());
        initTopBar();

        initGroupListView();

        return root;
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        mTopBar.setTitle(mQDItemDescription.getName());
    }

    private void initGroupListView() {
        QMUICommonListItemView normalItem = mGroupListView.createItemView(
                ContextCompat.getDrawable(getContext(), R.mipmap.about_logo),
                "Item 1",
                null,
                QMUICommonListItemView.HORIZONTAL,
                QMUICommonListItemView.ACCESSORY_TYPE_NONE);
        normalItem.setOrientation(QMUICommonListItemView.VERTICAL);

        QMUICommonListItemView itemWithDetail = mGroupListView.createItemView(
                ContextCompat.getDrawable(getContext(), R.mipmap.example_image0),
                "Item 2",
                null,
                QMUICommonListItemView.HORIZONTAL,
                QMUICommonListItemView.ACCESSORY_TYPE_NONE);

        // 去除 icon 的 tintColor 换肤设置
        QMUICommonListItemView.SkinConfig skinConfig = new QMUICommonListItemView.SkinConfig();
        skinConfig.iconTintColorRes = 0;
        itemWithDetail.setSkinConfig(skinConfig);
        itemWithDetail.setDetailText("在右方的详细信息");

        QMUICommonListItemView itemWithDetailBelow = mGroupListView.createItemView("Item 3");
        itemWithDetailBelow.setOrientation(QMUICommonListItemView.VERTICAL);
        itemWithDetailBelow.setDetailText("在标题下方的详细信息");

        QMUICommonListItemView itemWithChevron = mGroupListView.createItemView("Item 4");
        itemWithChevron.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);

        QMUICommonListItemView itemWithSwitch = mGroupListView.createItemView("Item 5");
        itemWithSwitch.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_SWITCH);
        itemWithSwitch.getSwitch().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(getActivity(), "checked = " + isChecked, Toast.LENGTH_SHORT).show();
            }
        });

        QMUICommonListItemView itemWithDetailBelowWithChevron = mGroupListView.createItemView("Item 6");
        itemWithDetailBelowWithChevron.setOrientation(QMUICommonListItemView.VERTICAL);
        itemWithDetailBelowWithChevron.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
        itemWithDetailBelowWithChevron.setDetailText("在标题下方的详细信息");

        QMUICommonListItemView longTitleAndDetail = mGroupListView.createItemView(null,
                "标题有点长；标题有点长；标题有点长；标题有点长；标题有点长；标题有点长",
                "详细信息有点长; 详细信息有点长；详细信息有点长；详细信息有点长;详细信息有点长",
                QMUICommonListItemView.VERTICAL,
                QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        int paddingVer = QMUIDisplayHelper.dp2px(getContext(), 12);
        longTitleAndDetail.setPadding(longTitleAndDetail.getPaddingLeft(), paddingVer,
                longTitleAndDetail.getPaddingRight(), paddingVer);

        int height = QMUIResHelper.getAttrDimen(getContext(), com.qmuiteam.qmui.R.attr.qmui_list_item_height);

        QMUICommonListItemView itemWithDetailBelowWithChevronWithIcon = mGroupListView.createItemView(
                ContextCompat.getDrawable(getContext(), R.mipmap.about_logo),
                "Item 7",
                "在标题下方的详细信息",
                QMUICommonListItemView.VERTICAL,
                QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON,
                height);


        QMUICommonListItemView itemWithCustom = mGroupListView.createItemView("右方自定义 View");
        itemWithCustom.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CUSTOM);
        QMUILoadingView loadingView = new QMUILoadingView(getActivity());
        itemWithCustom.addAccessoryCustomView(loadingView);


        QMUICommonListItemView itemRedPoint1 = mGroupListView.createItemView(
                ContextCompat.getDrawable(getContext(), R.mipmap.about_logo),
                "红点显示在左边",
                "在标题下方的详细信息",
                QMUICommonListItemView.VERTICAL,
                QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON,
                height);
        itemRedPoint1.setTipPosition(QMUICommonListItemView.TIP_POSITION_LEFT);
        itemRedPoint1.showRedDot(true);

        QMUICommonListItemView itemRedPoint2 = mGroupListView.createItemView(
                ContextCompat.getDrawable(getContext(), R.mipmap.about_logo),
                "红点显示在右边",
                "在标题下方的详细信息",
                QMUICommonListItemView.VERTICAL,
                QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON,
                height);
        itemRedPoint2.setTipPosition(QMUICommonListItemView.TIP_POSITION_RIGHT);
        itemRedPoint2.showRedDot(true);

        QMUICommonListItemView itemRedPoint3 = mGroupListView.createItemView(
                ContextCompat.getDrawable(getContext(), R.mipmap.about_logo),
                "红点显示在左边",
                "在右方的详细信息",
                QMUICommonListItemView.HORIZONTAL,
                QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON,
                height);
        itemRedPoint3.setTipPosition(QMUICommonListItemView.TIP_POSITION_LEFT);
        itemRedPoint3.showRedDot(true);

        QMUICommonListItemView itemRedPoint4 = mGroupListView.createItemView(
                ContextCompat.getDrawable(getContext(), R.mipmap.about_logo),
                "红点显示在右边",
                "在右方的详细信息",
                QMUICommonListItemView.HORIZONTAL,
                QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON,
                height);
        itemRedPoint4.setTipPosition(QMUICommonListItemView.TIP_POSITION_RIGHT);
        itemRedPoint4.showRedDot(true);

        QMUICommonListItemView itemNew1 = mGroupListView.createItemView(
                ContextCompat.getDrawable(getContext(), R.mipmap.about_logo),
                "new 标识显示在左边",
                "在标题下方的详细信息",
                QMUICommonListItemView.VERTICAL,
                QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON,
                height);
        itemNew1.setTipPosition(QMUICommonListItemView.TIP_POSITION_LEFT);
        itemNew1.showNewTip(true);

        QMUICommonListItemView itemNew2 = mGroupListView.createItemView(
                ContextCompat.getDrawable(getContext(), R.mipmap.about_logo),
                "new 标识显示在右边",
                "在标题下方的详细信息",
                QMUICommonListItemView.VERTICAL,
                QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON,
                height);
        itemNew2.setTipPosition(QMUICommonListItemView.TIP_POSITION_RIGHT);
        itemNew2.showNewTip(true);

        QMUICommonListItemView itemNew3 = mGroupListView.createItemView(
                ContextCompat.getDrawable(getContext(), R.mipmap.about_logo),
                "new 标识显示在左边",
                "在右方的详细信息",
                QMUICommonListItemView.HORIZONTAL,
                QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON,
                height);
        itemNew3.setTipPosition(QMUICommonListItemView.TIP_POSITION_LEFT);
        itemNew3.showNewTip(true);

        QMUICommonListItemView itemNew4 = mGroupListView.createItemView(
                ContextCompat.getDrawable(getContext(), R.mipmap.about_logo),
                "new 标识显示在右边",
                "在右方的详细信息",
                QMUICommonListItemView.HORIZONTAL,
                QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON,
                height);
        itemNew4.setTipPosition(QMUICommonListItemView.TIP_POSITION_RIGHT);
        itemNew4.showNewTip(true);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v instanceof QMUICommonListItemView) {
                    CharSequence text = ((QMUICommonListItemView) v).getText();
                    Toast.makeText(getActivity(), text + " is Clicked", Toast.LENGTH_SHORT).show();
                    if (((QMUICommonListItemView) v).getAccessoryType() == QMUICommonListItemView.ACCESSORY_TYPE_SWITCH) {
                        ((QMUICommonListItemView) v).getSwitch().toggle();
                    }
                }
            }
        };

        int size = QMUIDisplayHelper.dp2px(getContext(), 20);
        QMUIGroupListView.newSection(getContext())
                .setTitle("Section 1: 默认提供的样式")
                .setDescription("Section 1 的描述")
                .setLeftIconSize(size, ViewGroup.LayoutParams.WRAP_CONTENT)
                .addItemView(normalItem, onClickListener)
                .addItemView(itemWithDetail, onClickListener)
                .addItemView(itemWithDetailBelow, onClickListener)
                .addItemView(itemWithChevron, onClickListener)
                .addItemView(itemWithSwitch, onClickListener)
                .addItemView(itemWithDetailBelowWithChevron, onClickListener)
                .addItemView(itemWithDetailBelowWithChevronWithIcon, onClickListener)
                .addItemView(longTitleAndDetail, onClickListener)
                .setMiddleSeparatorInset(QMUIDisplayHelper.dp2px(getContext(), 16), 0)
                .addTo(mGroupListView);

        QMUIGroupListView.newSection(getContext())
                .setTitle("Section 2: 自定义右侧 View/红点/new 提示")
                .setLeftIconSize(size, ViewGroup.LayoutParams.WRAP_CONTENT)
                .addItemView(itemWithCustom, onClickListener)
                .addItemView(itemRedPoint1, onClickListener)
                .addItemView(itemRedPoint2, onClickListener)
                .addItemView(itemRedPoint3, onClickListener)
                .addItemView(itemRedPoint4, onClickListener)
                .addItemView(itemNew1, onClickListener)
                .addItemView(itemNew2, onClickListener)
                .addItemView(itemNew3, onClickListener)
                .addItemView(itemNew4, onClickListener)
                .setOnlyShowStartEndSeparator(true)
                .addTo(mGroupListView);
    }
}
