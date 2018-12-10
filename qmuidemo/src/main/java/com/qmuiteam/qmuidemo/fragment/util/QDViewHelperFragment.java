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

import android.view.LayoutInflater;
import android.view.View;

import com.qmuiteam.qmui.util.QMUIViewHelper;
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
 * {@link QMUIViewHelper} 内各种方法的使用示例。
 * Created by Kayo on 2017/02/04.
 */

@Widget(group = Group.Helper, widgetClass = QMUIViewHelper.class, iconRes = R.mipmap.icon_grid_view_helper)
public class QDViewHelperFragment extends BaseFragment {

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

        initContentView();

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

    private void initContentView() {
        QMUIGroupListView.newSection(getContext())
                .setTitle("背景动画")
                .addItemView(mGroupListView.createItemView(QDDataManager.getInstance().getName(QDViewHelperBackgroundAnimationBlinkFragment.class)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QDViewHelperBackgroundAnimationBlinkFragment fragment = new QDViewHelperBackgroundAnimationBlinkFragment();
                        startFragment(fragment);
                    }
                })
                .addItemView(mGroupListView.createItemView(QDDataManager.getInstance().getName(QDViewHelperBackgroundAnimationFullFragment.class)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QDViewHelperBackgroundAnimationFullFragment fragment = new QDViewHelperBackgroundAnimationFullFragment();
                        startFragment(fragment);
                    }
                })
                .addTo(mGroupListView);

        QMUIGroupListView.newSection(getContext())
                .setTitle("进退场动画")
                .addItemView(mGroupListView.createItemView(QDDataManager.getInstance().getName(QDViewHelperAnimationFadeFragment.class)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QDViewHelperAnimationFadeFragment fragment = new QDViewHelperAnimationFadeFragment();
                        startFragment(fragment);
                    }
                })
                .addItemView(mGroupListView.createItemView(QDDataManager.getInstance().getName(QDViewHelperAnimationSlideFragment.class)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QDViewHelperAnimationSlideFragment fragment = new QDViewHelperAnimationSlideFragment();
                        startFragment(fragment);
                    }
                })
                .addTo(mGroupListView);
    }

}
