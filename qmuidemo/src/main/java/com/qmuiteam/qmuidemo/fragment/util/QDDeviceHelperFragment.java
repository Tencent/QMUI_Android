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

package com.qmuiteam.qmuidemo.fragment.util;

import androidx.core.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;

import com.qmuiteam.qmui.util.QMUIDeviceHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.Group;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.model.QDItemDescription;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * {@link QMUIDeviceHelper} 的使用示例。
 * Created by Kayo on 2016/12/2.
 */

@Widget(group = Group.Helper, widgetClass = QMUIDeviceHelper.class, iconRes = R.mipmap.icon_grid_device_helper)
public class QDDeviceHelperFragment extends BaseFragment {

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

        initContent();

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

    private SpannableString getFormatItemValue(CharSequence value) {
        SpannableString result = new SpannableString(value);
        result.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.qmui_config_color_gray_5)), 0, value.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return result;
    }

    private void initContent() {
        String isTabletText = booleanToString(QMUIDeviceHelper.isTablet(getContext()));
        QMUIGroupListView.newSection(getContext())
                .addItemView(mGroupListView.createItemView(getString(R.string.deviceHelper_tablet_title)), null)
                .addItemView(mGroupListView.createItemView(getFormatItemValue(String.format("当前设备%1$s平板设备", isTabletText))), null)
                .addTo(mGroupListView);

        String isFlymeText = booleanToString(QMUIDeviceHelper.isFlyme());
        QMUIGroupListView.newSection(getContext())
                .addItemView(mGroupListView.createItemView(getString(R.string.deviceHelper_flyme_title)), null)
                .addItemView(mGroupListView.createItemView(getFormatItemValue(String.format("当前设备%1$s Flyme 系统", isFlymeText))), null)
                .addTo(mGroupListView);

        String isMiuiText = booleanToString(QMUIDeviceHelper.isMIUI());
        QMUIGroupListView.newSection(getContext())
                .addItemView(mGroupListView.createItemView(getString(R.string.deviceHelper_miui_title)), null)
                .addItemView(mGroupListView.createItemView(getFormatItemValue(String.format("当前设备%1$s MIUI 系统", isMiuiText))), null)
                .addTo(mGroupListView);

        String isMeizuText = booleanToString(QMUIDeviceHelper.isMeizu());
        QMUIGroupListView.newSection(getContext())
                .addItemView(mGroupListView.createItemView(getString(R.string.deviceHelper_meizu_title)), null)
                .addItemView(mGroupListView.createItemView(getFormatItemValue(String.format("当前设备%1$s魅族手机", isMeizuText))), null)
                .addTo(mGroupListView);
    }

    private String booleanToString(boolean b) {
        return b ? "是" : "不是";
    }
}
