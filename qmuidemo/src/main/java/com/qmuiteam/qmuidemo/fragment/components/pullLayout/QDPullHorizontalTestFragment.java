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

package com.qmuiteam.qmuidemo.fragment.components.pullLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.pullLayout.QMUIPullLayout;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.adaptor.QDRecyclerViewAdapter;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.base.BaseRecyclerAdapter;
import com.qmuiteam.qmuidemo.base.RecyclerViewHolder;
import com.qmuiteam.qmuidemo.lib.Group;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.model.QDItemDescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

@LatestVisitRecord
@Widget(group = Group.Other, name = "PullLayout: Horizontal Test")
public class QDPullHorizontalTestFragment extends BaseFragment {
    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.pull_layout)
    QMUIPullLayout mPullLayout;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private QDRecyclerViewAdapter mAdapter;

    private QDItemDescription mQDItemDescription;

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_pull_horizontal_test_layout, null);
        ButterKnife.bind(this, root);

        QDDataManager QDDataManager = com.qmuiteam.qmuidemo.manager.QDDataManager.getInstance();
        mQDItemDescription = QDDataManager.getDescription(this.getClass());
        initTopBar();
        initData();

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

    private void initData() {
        mPullLayout.setActionListener(new QMUIPullLayout.ActionListener() {
            @Override
            public void onActionTriggered(QMUIPullLayout.PullAction pullAction) {
                mPullLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullLayout.finishActionRun(pullAction);
                    }
                }, 1000);
            }
        });
        LinearLayoutManager layoutManager =  new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        new PagerSnapHelper().attachToRecyclerView(mRecyclerView);
        mAdapter = new QDRecyclerViewAdapter();
        mAdapter.setItemCount(10);
        mRecyclerView.setAdapter(mAdapter);
    }
}
