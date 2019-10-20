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


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.adaptor.QDRecyclerViewAdapter;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.model.QDItemDescription;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.lib.Group;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;

import butterknife.BindView;
import butterknife.ButterKnife;

/** 使用 {@link SnapHelper} 实现 {@link RecyclerView} 按页滚动。
 * Created by cgspine on 15/9/15.
 */

@Widget(group = Group.Lab, name = "用SnapHelper实现RecyclerView按页滚动", iconRes = R.mipmap.icon_grid_pager_layout_manager)
public class QDSnapHelperFragment extends BaseFragment {

    @BindView(R.id.topbar) QMUITopBarLayout mTopBar;
    @BindView(R.id.pagerWrap) ViewGroup mPagerWrap;

    RecyclerView mRecyclerView;
    LinearLayoutManager mPagerLayoutManager;
    QDRecyclerViewAdapter mRecyclerViewAdapter;
    SnapHelper mSnapHelper;

    private QDItemDescription mQDItemDescription;

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getContext()).inflate(R.layout.fragment_pagerlayoutmanager, null);
        ButterKnife.bind(this, root);

        mQDItemDescription = QDDataManager.getInstance().getDescription(this.getClass());
        initTopBar();

        mRecyclerView = new RecyclerView(getContext());
        mPagerLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mPagerLayoutManager);
        mRecyclerViewAdapter = new QDRecyclerViewAdapter();
        mRecyclerViewAdapter.setItemCount(10);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        mPagerWrap.addView(mRecyclerView);
        // PagerSnapHelper每次只能滚动一个item;用LinearSnapHelper则可以一次滚动多个，并最终保证定位
        // mSnapHelper = new LinearSnapHelper();
        mSnapHelper = new PagerSnapHelper();
        mSnapHelper.attachToRecyclerView(mRecyclerView);
        return root;
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        // 切换其他情况的按钮
        mTopBar.addRightImageButton(R.mipmap.icon_topbar_overflow, R.id.topbar_right_change_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetList();
            }
        });

        mTopBar.setTitle(mQDItemDescription.getName());
    }

    private void showBottomSheetList() {
        new QMUIBottomSheet.BottomListSheetBuilder(getActivity())
                .addItem("水平方向")
                .addItem("垂直方向")
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        dialog.dismiss();
                        switch (position) {
                            case 0:
                                mPagerLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                                break;
                            case 1:
                                mPagerLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                break;
                            default:
                                break;
                        }
                    }
                })
                .build()
                .show();
    }
}
