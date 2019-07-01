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
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.manager.QDDataManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class QDContinuousNestedScrollBaseFragment extends BaseFragment {
    @BindView(R.id.topbar) QMUITopBarLayout mTopBarLayout;
    @BindView(R.id.pull_to_refresh) QMUIPullRefreshLayout mPullRefreshLayout;
    @BindView(R.id.coordinator) QMUIContinuousNestedScrollLayout mCoordinatorLayout;

    private Bundle mSavedScrollInfo = new Bundle();

    @Override
    protected View onCreateView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_continuous_nested_scroll, null);
        ButterKnife.bind(this, view);
        initTopBar();
        initPullRefreshLayout();
        initCoordinatorLayout();
        mCoordinatorLayout.setDraggableScrollBarEnabled(true);
        return view;
    }

    private void initPullRefreshLayout(){
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
                }, 3000);
            }
        });
    }

    private void initTopBar() {
        mTopBarLayout.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        mTopBarLayout.setTitle(QDDataManager.getInstance().getName(this.getClass()));
        mTopBarLayout.addRightTextButton("scroll", QMUIViewHelper.generateViewId())
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showBottomSheet();
                    }
                });
    }

    protected abstract void initCoordinatorLayout();

    private void showBottomSheet() {
        new QMUIBottomSheet.BottomListSheetBuilder(getContext())
                .addItem("scrollToBottom")
                .addItem("scrollToTop")
                .addItem("scrollBottomViewToTop")
                .addItem("scrollBy 40dp")
                .addItem("scrollBy -40dp")
                .addItem("smoothScrollBy 100dp/1s")
                .addItem("smoothScrollBy -100dp/1s")
                .addItem("save current scroll info")
                .addItem("restore scroll info")
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        switch (position) {
                            case 0:
                                mCoordinatorLayout.scrollToBottom();
                                break;
                            case 1:
                                mCoordinatorLayout.scrollToTop();
                                break;
                            case 2:
                                mCoordinatorLayout.scrollBottomViewToTop();
                                break;
                            case 3:
                                mCoordinatorLayout.scrollBy(QMUIDisplayHelper.dp2px(getContext(), 40));
                                break;
                            case 4:
                                mCoordinatorLayout.scrollBy(QMUIDisplayHelper.dp2px(getContext(), -40));
                                break;
                            case 5:
                                mCoordinatorLayout.smoothScrollBy(QMUIDisplayHelper.dp2px(getContext(), 100), 1000);
                                break;
                            case 6:
                                mCoordinatorLayout.smoothScrollBy(QMUIDisplayHelper.dp2px(getContext(), -100), 1000);
                                break;
                            case 7:
                                mCoordinatorLayout.saveScrollInfo(mSavedScrollInfo);
                                break;
                            case 8:
                                mCoordinatorLayout.restoreScrollInfo(mSavedScrollInfo);
                        }
                        dialog.dismiss();
                    }
                })
                .build().show();
    }
}
