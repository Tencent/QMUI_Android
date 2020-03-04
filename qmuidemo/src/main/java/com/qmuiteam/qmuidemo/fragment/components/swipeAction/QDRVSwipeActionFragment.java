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

package com.qmuiteam.qmuidemo.fragment.components.swipeAction;

import android.view.LayoutInflater;
import android.view.View;

import com.qmuiteam.qmui.recyclerView.QMUIRVItemSwipeAction;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;
import com.qmuiteam.qmui.widget.pullLayout.QMUIPullLayout;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.fragment.components.pullLayout.QDPullHorizontalTestFragment;
import com.qmuiteam.qmuidemo.fragment.components.pullLayout.QDPullRefreshAndLoadMoreTestFragment;
import com.qmuiteam.qmuidemo.fragment.components.pullLayout.QDPullVerticalTestFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.model.QDItemDescription;

import butterknife.BindView;
import butterknife.ButterKnife;

@Widget(widgetClass = QMUIRVItemSwipeAction.class, iconRes = R.mipmap.icon_grid_rv_item_swipe_action)
public class QDRVSwipeActionFragment extends BaseFragment {
    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.groupListView)
    QMUIGroupListView mGroupListView;

    private QDDataManager mQDDataManager;
    private QDItemDescription mQDItemDescription;

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_grouplistview, null);
        ButterKnife.bind(this, root);

        mQDDataManager = QDDataManager.getInstance();
        mQDItemDescription = mQDDataManager.getDescription(this.getClass());
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
        QMUIGroupListView.newSection(getContext())
                .addItemView(mGroupListView.createItemView(mQDDataManager.getName(
                        QDRVSwipeMutiActionFragment.class)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QDRVSwipeMutiActionFragment fragment = new QDRVSwipeMutiActionFragment();
                        startFragment(fragment);
                    }
                })
                .addItemView(mGroupListView.createItemView(mQDDataManager.getName(
                        QDRVSwipeMutiActionOnlyIconFragment.class)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QDRVSwipeMutiActionOnlyIconFragment fragment = new QDRVSwipeMutiActionOnlyIconFragment();
                        startFragment(fragment);
                    }
                })
                .addItemView(mGroupListView.createItemView(mQDDataManager.getName(
                        QDRVSwipeMutiActionWithIconFragment.class)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QDRVSwipeMutiActionWithIconFragment fragment = new QDRVSwipeMutiActionWithIconFragment();
                        startFragment(fragment);
                    }
                })
                .addItemView(mGroupListView.createItemView(mQDDataManager.getName(
                        QDRVSwipeSingleDeleteActionFragment.class)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QDRVSwipeSingleDeleteActionFragment fragment = new QDRVSwipeSingleDeleteActionFragment();
                        startFragment(fragment);
                    }
                })
                .addItemView(mGroupListView.createItemView(mQDDataManager.getName(
                        QDRVSwipeDeleteWithNoActionFragment.class)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QDRVSwipeDeleteWithNoActionFragment fragment = new QDRVSwipeDeleteWithNoActionFragment();
                        startFragment(fragment);
                    }
                })
                .addItemView(mGroupListView.createItemView(mQDDataManager.getName(
                        QDRVSwipeUpDeleteFragment.class)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QDRVSwipeUpDeleteFragment fragment = new QDRVSwipeUpDeleteFragment();
                        startFragment(fragment);
                    }
                })
                .addTo(mGroupListView);


    }
}
