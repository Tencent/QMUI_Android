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

package com.qmuiteam.qmuidemo.fragment.components.viewpager;

import android.view.LayoutInflater;
import android.view.View;

import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.QMUIViewPager;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.model.QDItemDescription;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author cginechen
 * @date 2017-09-13
 */

@Widget(widgetClass = QMUIViewPager.class, iconRes = R.mipmap.icon_grid_pager_layout_manager)
public class QDViewPagerFragment extends BaseFragment {
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
                        QDFitSystemWindowViewPagerFragment.class)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QDFitSystemWindowViewPagerFragment fragment = new QDFitSystemWindowViewPagerFragment();
                        startFragment(fragment);
                    }
                })
                .addItemView(mGroupListView.createItemView(mQDDataManager.getName(
                        QDLoopViewPagerFragment.class)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QDLoopViewPagerFragment fragment = new QDLoopViewPagerFragment();
                        startFragment(fragment);
                    }
                })
                .addTo(mGroupListView);
    }
}
