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

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.viewpager2.widget.ViewPager2;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.tab.QMUITabBuilder;
import com.qmuiteam.qmui.widget.tab.QMUITabIndicator;
import com.qmuiteam.qmui.widget.tab.QMUITabSegment;
import com.qmuiteam.qmui.widget.tab.QMUITabSegment2;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.adaptor.QDRecyclerViewAdapter;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.Group;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.model.QDItemDescription;

import butterknife.BindView;
import butterknife.ButterKnife;


@Widget(group = Group.Other, name = "ViewPager2: 内容自适应，超过父容器则滚动")
public class QDTabSegment2ScrollableModeFragment extends BaseFragment {
    @SuppressWarnings("FieldCanBeLocal")
    private final int TAB_COUNT = 10;

    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.tabSegment)
    QMUITabSegment2 mTabSegment;
    @BindView(R.id.contentViewPager)
    ViewPager2 mContentViewPager;

    private ContentPage mDestPage = ContentPage.Item1;
    private QDItemDescription mQDItemDescription;
    private int mCurrentItemCount = TAB_COUNT;
    private QDRecyclerViewAdapter mPageAdapter;

    @Override
    protected View onCreateView() {
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_tab_viewpager2_layout, null);
        ButterKnife.bind(this, rootView);

        mQDItemDescription = QDDataManager.getInstance().getDescription(this.getClass());
        initTopBar();
        initTabAndPager();

        return rootView;
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        mTopBar.setTitle(mQDItemDescription.getName());
        mTopBar.addRightTextButton("reduce tab", QMUIViewHelper.generateViewId())
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reduceTabCount();
                    }
                });
    }

    private void initTabAndPager() {
        mPageAdapter = new QDRecyclerViewAdapter();
        mPageAdapter.setItemCount(mCurrentItemCount);
        mContentViewPager.setAdapter(mPageAdapter);
        mContentViewPager.setCurrentItem(mDestPage.getPosition(), false);
        QMUITabBuilder tabBuilder = mTabSegment.tabBuilder();
        for (int i = 0; i < mCurrentItemCount; i++) {
            mTabSegment.addTab(tabBuilder.setText("Item " + (i + 1)).build(getContext()));
        }
        int space = QMUIDisplayHelper.dp2px(getContext(), 16);
        mTabSegment.setIndicator(new QMUITabIndicator(
                QMUIDisplayHelper.dp2px(getContext(), 2), false, true));
        mTabSegment.setMode(QMUITabSegment.MODE_SCROLLABLE);
        mTabSegment.setItemSpaceInScrollMode(space);
        mTabSegment.setupWithViewPager(mContentViewPager);
        mTabSegment.setPadding(space, 0, space, 0);
        mTabSegment.addOnTabSelectedListener(new QMUITabSegment.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int index) {
                Toast.makeText(getContext(), "select index " + index, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTabUnselected(int index) {
                Toast.makeText(getContext(), "unSelect index " + index, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTabReselected(int index) {
                Toast.makeText(getContext(), "reSelect index " + index, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDoubleTap(int index) {
                Toast.makeText(getContext(), "double tap index " + index, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reduceTabCount() {
        if (mCurrentItemCount <= 1) {
            Toast.makeText(getContext(), "Only the last one, don't reduce it anymore!!!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mCurrentItemCount--;
        mPageAdapter.setItemCount(mCurrentItemCount);
        mPageAdapter.notifyDataSetChanged();
        mTabSegment.reset();
        QMUITabBuilder tabBuilder = mTabSegment.tabBuilder();
        for (int i = 0; i < mCurrentItemCount; i++) {
            mTabSegment.addTab(tabBuilder.setText("Item " + (i + 1)).build(getContext()));
        }
        mTabSegment.notifyDataChanged();
    }

    public enum ContentPage {
        Item1(0),
        Item2(1),
        Item3(2),
        Item4(3),
        Item5(4),
        Item6(5),
        Item7(6),
        Item8(7),
        Item9(8),
        Item10(9);
        private final int position;

        ContentPage(int pos) {
            position = pos;
        }

        public static ContentPage getPage(int position) {
            switch (position) {
                case 0:
                    return Item1;
                case 1:
                    return Item2;
                case 2:
                    return Item3;
                case 3:
                    return Item4;
                case 4:
                    return Item5;
                case 5:
                    return Item6;
                case 6:
                    return Item7;
                case 7:
                    return Item8;
                case 8:
                    return Item9;
                case 9:
                    return Item10;
                default:
                    return Item1;
            }
        }

        public int getPosition() {
            return position;
        }
    }
}
