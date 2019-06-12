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

package com.qmuiteam.qmuidemo.fragment.lab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.qmuiteam.qmui.nestedScroll.QMUIContinuousNestedScrollLayout;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.Group;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;

@Widget(group = Group.Lab,
        widgetClass = QMUIContinuousNestedScrollLayout.class,
        iconRes = R.mipmap.icon_grid_continuous_nest_scroll,
        docUrl ="https://github.com/Tencent/QMUI_Android/wiki/QMUIContinuousNestedScrollLayout")
public class QDContinuousNestedScrollFragment extends BaseFragment {

    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.groupListView)
    QMUIGroupListView mGroupListView;

    private QDDataManager mQDDataManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQDDataManager = QDDataManager.getInstance();
    }

    @Override
    protected View onCreateView() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_grouplistview, null);
        ButterKnife.bind(this, view);
        initTopBar();
        initGroupListView();
        return view;
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        mTopBar.setTitle(mQDDataManager.getName(this.getClass()));
    }

    private void initGroupListView() {
        QMUIGroupListView.newSection(getContext())
                .addItemView(mGroupListView.createItemView(mQDDataManager.getName(
                        QDContinuousNestedScroll1Fragment.class)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QDContinuousNestedScroll1Fragment fragment = new QDContinuousNestedScroll1Fragment();
                        startFragment(fragment);
                    }
                })
                .addItemView(mGroupListView.createItemView(mQDDataManager.getName(
                        QDContinuousNestedScroll2Fragment.class)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QDContinuousNestedScroll2Fragment fragment = new QDContinuousNestedScroll2Fragment();
                        startFragment(fragment);
                    }
                })
                .addItemView(mGroupListView.createItemView(mQDDataManager.getName(
                        QDContinuousNestedScroll3Fragment.class)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QDContinuousNestedScroll3Fragment fragment = new QDContinuousNestedScroll3Fragment();
                        startFragment(fragment);
                    }
                })
                .addItemView(mGroupListView.createItemView(mQDDataManager.getName(
                        QDContinuousNestedScroll4Fragment.class)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QDContinuousNestedScroll4Fragment fragment = new QDContinuousNestedScroll4Fragment();
                        startFragment(fragment);
                    }
                })
                .addItemView(mGroupListView.createItemView(mQDDataManager.getName(
                        QDContinuousNestedScroll5Fragment.class)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QDContinuousNestedScroll5Fragment fragment = new QDContinuousNestedScroll5Fragment();
                        startFragment(fragment);
                    }
                })
                .addItemView(mGroupListView.createItemView(mQDDataManager.getName(
                        QDContinuousNestedScroll6Fragment.class)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QDContinuousNestedScroll6Fragment fragment = new QDContinuousNestedScroll6Fragment();
                        startFragment(fragment);
                    }
                })
                .addItemView(mGroupListView.createItemView(mQDDataManager.getName(
                        QDContinuousNestedScroll7Fragment.class)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QDContinuousNestedScroll7Fragment fragment = new QDContinuousNestedScroll7Fragment();
                        startFragment(fragment);
                    }
                })
                .addItemView(mGroupListView.createItemView(mQDDataManager.getName(
                        QDContinuousNestedScroll8Fragment.class)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QDContinuousNestedScroll8Fragment fragment = new QDContinuousNestedScroll8Fragment();
                        startFragment(fragment);
                    }
                })
                .addTo(mGroupListView);
    }
}
