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

import android.animation.ValueAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qmuiteam.qmui.widget.QMUICollapsingTopBarLayout;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.adaptor.QDRecyclerViewAdapter;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author cginechen
 * @date 2017-09-02
 */

@Widget(widgetClass = QMUICollapsingTopBarLayout.class, iconRes = R.mipmap.icon_grid_collapse_top_bar)
public class QDCollapsingTopBarLayoutFragment extends BaseFragment {
    private static final String TAG = "CollapsingTopBarLayout";

    QDRecyclerViewAdapter mRecyclerViewAdapter;
    LinearLayoutManager mPagerLayoutManager;


    @BindView(R.id.recyclerView) RecyclerView mRecyclerView;
    @BindView(R.id.collapsing_topbar_layout) QMUICollapsingTopBarLayout mCollapsingTopBarLayout;
    @BindView(R.id.topbar) QMUITopBar mTopBar;

    @Override
    protected View onCreateView() {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_collapsing_topbar_layout, null);
        ButterKnife.bind(this, rootView);
        initTopBar();
        mPagerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mPagerLayoutManager);
        mRecyclerViewAdapter = new QDRecyclerViewAdapter();
        mRecyclerViewAdapter.setItemCount(10);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        mCollapsingTopBarLayout.setScrimUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Log.i(TAG, "scrim: " + animation.getAnimatedValue());
            }
        });

        mCollapsingTopBarLayout.addOnOffsetUpdateListener(new QMUICollapsingTopBarLayout.OnOffsetUpdateListener() {
            @Override
            public void onOffsetChanged(QMUICollapsingTopBarLayout layout, int offset, float expandFraction) {
                Log.i(TAG, "offset = " + offset + "; expandFraction = " + expandFraction);
            }
        });

        return rootView;
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        mCollapsingTopBarLayout.setTitle(QDDataManager.getInstance().getDescription(this.getClass()).getName());
    }
}
