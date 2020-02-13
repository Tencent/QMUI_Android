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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.qmuiteam.qmui.recyclerView.QMUIRVDraggableScrollBar;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;
import com.qmuiteam.qmui.widget.section.QMUISection;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionAdapter;
import com.qmuiteam.qmui.widget.section.QMUIStickySectionLayout;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.model.SectionHeader;
import com.qmuiteam.qmuidemo.model.SectionItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class QDBaseSectionLayoutFragment extends BaseFragment {

    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.pull_to_refresh)
    QMUIPullRefreshLayout mPullRefreshLayout;
    @BindView(R.id.section_layout)
    QMUIStickySectionLayout mSectionLayout;

    private RecyclerView.LayoutManager mLayoutManager;
    protected QMUIStickySectionAdapter<SectionHeader, SectionItem, QMUIStickySectionAdapter.ViewHolder> mAdapter;

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

        mTopBar.addRightImageButton(R.mipmap.icon_topbar_overflow, R.id.topbar_right_change_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showBottomSheet();
                    }
                });
    }

    private void initRefreshLayout() {
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

    protected void initStickyLayout() {
        mLayoutManager = createLayoutManager();
        mSectionLayout.setLayoutManager(mLayoutManager);
        QMUIRVDraggableScrollBar scrollBar = new QMUIRVDraggableScrollBar(0, 0, 0);
        scrollBar.setEnableScrollBarFadeInOut(false);
        scrollBar.attachToStickSectionLayout(mSectionLayout);
    }

    private void initData() {
        mAdapter = createAdapter();
        mAdapter.setCallback(new QMUIStickySectionAdapter.Callback<SectionHeader, SectionItem>() {
            @Override
            public void loadMore(final QMUISection<SectionHeader, SectionItem> section, final boolean loadMoreBefore) {
                mSectionLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isAttachedToActivity()) {
                            ArrayList<SectionItem> list = new ArrayList<>();
                            for (int i = 0; i < 10; i++) {
                                list.add(new SectionItem("load more item " + i));
                            }
                            mAdapter.finishLoadMore(section, list, loadMoreBefore, false);
                        }
                    }
                }, 1000);
            }

            @Override
            public void onItemClick(QMUIStickySectionAdapter.ViewHolder holder, int position) {
                Toast.makeText(getContext(), "click item " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onItemLongClick(QMUIStickySectionAdapter.ViewHolder holder, int position) {
                Toast.makeText(getContext(), "long click item " + position, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        mSectionLayout.setAdapter(mAdapter, true);
        ArrayList<QMUISection<SectionHeader, SectionItem>> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(createSection("header " + i, i%2 != 0));
        }
        mAdapter.setData(list);
    }

    private QMUISection<SectionHeader, SectionItem> createSection(String headerText, boolean isFold) {
        SectionHeader header = new SectionHeader(headerText);
        ArrayList<SectionItem> contents = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            contents.add(new SectionItem("item " + i));
        }
        QMUISection<SectionHeader, SectionItem> section = new QMUISection<>(header, contents, isFold);
        // if test load more, you can open the code
        section.setExistAfterDataToLoad(true);
//        section.setExistBeforeDataToLoad(true);
        return section;
    }

    protected abstract QMUIStickySectionAdapter<
            SectionHeader, SectionItem, QMUIStickySectionAdapter.ViewHolder> createAdapter();

    protected abstract RecyclerView.LayoutManager createLayoutManager();


    private void showBottomSheet() {
        new QMUIBottomSheet.BottomListSheetBuilder(getContext())
                .addItem("test scroll to section header")
                .addItem("test scroll to section item")
                .addItem("test find position")
                .addItem("test find custom position")
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        switch (position) {
                            case 0: {
                                QMUISection<SectionHeader, SectionItem> section = mAdapter.getSectionDirectly(3);
                                if (section != null) {
                                    mAdapter.scrollToSectionHeader(section, true);
                                }
                                break;
                            }
                            case 1: {
                                QMUISection<SectionHeader, SectionItem> section = mAdapter.getSectionDirectly(3);
                                if (section != null) {
                                    SectionItem item = section.getItemAt(10);
                                    if (item != null) {
                                        mAdapter.scrollToSectionItem(section, item, true);
                                    }
                                }
                                break;
                            }
                            case 2: {
                                int targetPosition = mAdapter.findPosition(new QMUIStickySectionAdapter.PositionFinder<SectionHeader, SectionItem>() {
                                    @Override
                                    public boolean find(@NonNull QMUISection<SectionHeader, SectionItem> section, @Nullable SectionItem item) {
                                        return "header 4".equals(section.getHeader().getText()) && (item != null && "item 13".equals(item.getText()));
                                    }
                                }, true);
                                if (targetPosition != RecyclerView.NO_POSITION) {
                                    Toast.makeText(getContext(), "find position: " + targetPosition, Toast.LENGTH_SHORT).show();
                                    QMUISection<SectionHeader, SectionItem> section = mAdapter.getSection(targetPosition);
                                    SectionItem item = mAdapter.getSectionItem(targetPosition);
                                    if (item != null) {
                                        mAdapter.scrollToSectionItem(section, item, true);
                                    } else if (section != null) {
                                        mAdapter.scrollToSectionHeader(section, true);
                                    } else {
                                        mLayoutManager.scrollToPosition(targetPosition);
                                    }

                                } else {
                                    Toast.makeText(getContext(), "failed to find position", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            }
                            case 3: {
                                int targetPosition = mAdapter.findCustomPosition(QMUISection.SECTION_INDEX_UNKNOWN, QDListWithDecorationSectionAdapter.ITEM_INDEX_LIST_FOOTER, false);
                                if (targetPosition != RecyclerView.NO_POSITION) {
                                    Toast.makeText(getContext(), "find position: " + targetPosition, Toast.LENGTH_SHORT).show();
                                    mLayoutManager.scrollToPosition(targetPosition);
                                }
                            }
                        }
                        dialog.dismiss();
                    }
                })
                .build().show();
    }
}
