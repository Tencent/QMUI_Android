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

import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;
import com.qmuiteam.qmui.widget.section.QMUISection;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionLayout;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.model.SectionItem;
import com.qmuiteam.qmuidemo.model.SectionHeader;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

@Widget(widgetClass = QMUIStickySectionLayout.class, iconRes = R.mipmap.icon_grid_in_progress)
public class QDSectionLayoutFragment extends BaseFragment {

    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.pull_to_refresh)
    QMUIPullRefreshLayout mPullRefreshLayout;
    @BindView(R.id.section_layout)
    QMUIStickySectionLayout mSectionLayout;

    private RecyclerView.LayoutManager mLayoutManager;
    private QDSectionAdapter mAdapter;


    @Override
    protected View onCreateView() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_section_layout, null);
        ButterKnife.bind(this, view);
        initTopBar();
        initRefreshLayout();
        initStickyLayout();
        initData();
        return view;
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        mTopBar.setTitle(QDDataManager.getInstance().getDescription(this.getClass()).getName());
    }

    private void initRefreshLayout() {
        mPullRefreshLayout.setChildScrollUpCallback(new QMUIPullRefreshLayout.OnChildScrollUpCallback() {
            @Override
            public boolean canChildScrollUp(QMUIPullRefreshLayout parent, @Nullable View child) {
                return QMUIPullRefreshLayout.defaultCanScrollUp(mSectionLayout.getRecyclerView());
            }
        });
        mPullRefreshLayout.setOnPullListener(new QMUIPullRefreshLayout.OnPullListener() {
            @Override
            public void onMoveTarget(int offset) {

            }

            @Override
            public void onMoveRefreshView(int offset) {

            }

            @Override
            public void onRefresh() {
                mPullRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullRefreshLayout.finishRefresh();
                    }
                }, 2000);
            }
        });
    }

    private void initStickyLayout() {
        mLayoutManager = new LinearLayoutManager(getContext()) {
            @Override
            public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                return new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        };
        mSectionLayout.getRecyclerView().setLayoutManager(mLayoutManager);
    }

    private void initData() {
        mAdapter = new QDSectionAdapter();
        mSectionLayout.setAdapterWithStickyDecoration(mAdapter);
        ArrayList<QMUISection<SectionHeader, SectionItem>> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(createSection("header " + i, i % 2 == 0));
        }
        mAdapter.setData(list);
    }

    private QMUISection<SectionHeader, SectionItem> createSection(String headerText, boolean isFold) {
        SectionHeader header = new SectionHeader(headerText);
        ArrayList<SectionItem> contents = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            contents.add(new SectionItem("item " + i));
        }
        return new QMUISection<>(header, contents, isFold);
    }
}
