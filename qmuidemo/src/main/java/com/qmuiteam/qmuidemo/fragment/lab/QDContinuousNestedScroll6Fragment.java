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

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.qmuiteam.qmui.nestedScroll.QMUIContinuousNestedBottomAreaBehavior;
import com.qmuiteam.qmui.nestedScroll.QMUIContinuousNestedBottomRecyclerView;
import com.qmuiteam.qmui.nestedScroll.QMUIContinuousNestedTopAreaBehavior;
import com.qmuiteam.qmui.nestedScroll.QMUIContinuousNestedTopLinearLayout;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmuidemo.base.BaseRecyclerAdapter;
import com.qmuiteam.qmuidemo.base.RecyclerViewHolder;
import com.qmuiteam.qmuidemo.lib.Group;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

@Widget(group = Group.Other, name = "linearLayout + recyclerview")
public class QDContinuousNestedScroll6Fragment extends QDContinuousNestedScrollBaseFragment {

    private QMUIContinuousNestedTopLinearLayout mTopLinearLayout;
    private RecyclerView mRecyclerView;
    private BaseRecyclerAdapter<String> mAdapter;

    @Override
    protected void initCoordinatorLayout() {
        mTopLinearLayout = new QMUIContinuousNestedTopLinearLayout(getContext());
        mTopLinearLayout.setBackgroundColor(Color.LTGRAY);
        mTopLinearLayout.setOrientation(LinearLayout.VERTICAL);


        AppCompatTextView firstView = new AppCompatTextView(getContext()) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(
                        QMUIDisplayHelper.dp2px(getContext(), 1000), View.MeasureSpec.EXACTLY
                ));
            }
        };
        firstView.setTextSize(17);
        firstView.setBackgroundColor(Color.DKGRAY);
        firstView.setTextColor(Color.WHITE);
        firstView.setText("This is Top firstView");
        firstView.setGravity(Gravity.CENTER);
        mTopLinearLayout.addView(firstView);

        AppCompatTextView secondView = new AppCompatTextView(getContext()) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(
                        QMUIDisplayHelper.dp2px(getContext(), 1000), MeasureSpec.EXACTLY
                ));
            }
        };
        secondView.setTextSize(17);
        secondView.setBackgroundColor(Color.GRAY);
        secondView.setTextColor(Color.WHITE);
        secondView.setGravity(Gravity.CENTER);
        secondView.setText("This is secondView");
        mTopLinearLayout.addView(secondView);


        int matchParent = ViewGroup.LayoutParams.MATCH_PARENT;
        CoordinatorLayout.LayoutParams topLp = new CoordinatorLayout.LayoutParams(
                matchParent, ViewGroup.LayoutParams.WRAP_CONTENT);
        topLp.setBehavior(new QMUIContinuousNestedTopAreaBehavior(getContext()));
        mCoordinatorLayout.setTopAreaView(mTopLinearLayout, topLp);

        mRecyclerView = new QMUIContinuousNestedBottomRecyclerView(getContext());
        CoordinatorLayout.LayoutParams recyclerViewLp = new CoordinatorLayout.LayoutParams(
                matchParent, matchParent);
        recyclerViewLp.setBehavior(new QMUIContinuousNestedBottomAreaBehavior());
        mCoordinatorLayout.setBottomAreaView(mRecyclerView, recyclerViewLp);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override
            public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });

        mAdapter = new BaseRecyclerAdapter<String>(getContext(), null) {
            @Override
            public int getItemLayoutId(int viewType) {
                return android.R.layout.simple_list_item_1;
            }

            @Override
            public void bindData(RecyclerViewHolder holder, int position, String item) {
                holder.setText(android.R.id.text1, item);
            }
        };
        mAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int pos) {
                Toast.makeText(getContext(), "click position=" + pos, Toast.LENGTH_SHORT).show();
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        onDataLoaded();
    }

    private void onDataLoaded() {
        List<String> data = new ArrayList<>(Arrays.asList("Helps", "Maintain", "Liver",
                "Health", "Function", "Supports", "Healthy", "Fat", "Metabolism", "Nuturally",
                "Bracket", "Refrigerator", "Bathtub", "Wardrobe", "Comb", "Apron", "Carpet",
                "Bolster", "Pillow", "Cushion"));
        Collections.shuffle(data);
        mAdapter.setData(data);
    }
}
