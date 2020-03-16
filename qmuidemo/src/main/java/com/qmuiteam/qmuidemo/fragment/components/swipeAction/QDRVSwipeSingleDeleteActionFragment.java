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

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qmuiteam.qmui.recyclerView.QMUIRVItemSwipeAction;
import com.qmuiteam.qmui.recyclerView.QMUISwipeAction;
import com.qmuiteam.qmui.recyclerView.QMUISwipeViewHolder;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.pullLayout.QMUIPullLayout;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
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

@Widget(group = Group.Other, name = "Swipe Left: Single Action And Allow Deletion")
public class QDRVSwipeSingleDeleteActionFragment extends BaseFragment {
    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.pull_layout)
    QMUIPullLayout mPullLayout;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private Adapter mAdapter;

    private QDItemDescription mQDItemDescription;

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_pull_refresh_and_load_more_test_layout, null);
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
            public void onActionTriggered(@NonNull QMUIPullLayout.PullAction pullAction) {
                mPullLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (pullAction.getPullEdge() == QMUIPullLayout.PULL_EDGE_TOP) {
                            onRefreshData();
                        } else if (pullAction.getPullEdge() == QMUIPullLayout.PULL_EDGE_BOTTOM) {
                            onLoadMore();
                        }
                        mPullLayout.finishActionRun(pullAction);
                    }
                }, 3000);
            }
        });

        QMUIRVItemSwipeAction swipeAction = new QMUIRVItemSwipeAction(true, new QMUIRVItemSwipeAction.Callback() {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                mAdapter.remove(viewHolder.getAdapterPosition());
            }

            @Override
            public int getSwipeDirection(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return QMUIRVItemSwipeAction.SWIPE_LEFT;
            }

            @Override
            public void onClickAction(QMUIRVItemSwipeAction swipeAction, RecyclerView.ViewHolder selected, QMUISwipeAction action) {
                super.onClickAction(swipeAction, selected, action);
                mAdapter.remove(selected.getAdapterPosition());
            }
        });
        swipeAction.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override
            public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });

        mAdapter = new Adapter(getContext());
        mRecyclerView.setAdapter(mAdapter);
        onDataLoaded();
    }

    private void onDataLoaded() {
        List<String> data = new ArrayList<>(Arrays.asList("Helps", "Maintain", "Liver", "Health", "Function", "Supports", "Healthy", "Fat",
                "Metabolism", "Nuturally", "Bracket", "Refrigerator", "Bathtub", "Wardrobe", "Comb", "Apron", "Carpet", "Bolster", "Pillow", "Cushion"));
        Collections.shuffle(data);
        mAdapter.setData(data);
    }

    private void onRefreshData() {
        List<String> data = new ArrayList<>();
        long id = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            data.add("onRefreshData-" + id + "-" + i);
        }
        mAdapter.prepend(data);
        mRecyclerView.scrollToPosition(0);
    }

    private void onLoadMore() {
        List<String> data = new ArrayList<>();
        long id = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            data.add("onLoadMore-" + id + "-" + i);
        }
        mAdapter.append(data);
    }

    class Adapter extends RecyclerView.Adapter<QMUISwipeViewHolder> {

        private List<String> mData = new ArrayList<>();

        private final QMUISwipeAction mDeleteAction;

        public Adapter(Context context) {
            QMUISwipeAction.ActionBuilder builder = new QMUISwipeAction.ActionBuilder()
                    .textSize(QMUIDisplayHelper.sp2px(context, 14))
                    .textColor(Color.WHITE)
                    .paddingStartEnd(QMUIDisplayHelper.dp2px(getContext(), 14));

            mDeleteAction = builder.text("删除").backgroundColor(Color.RED).build();
        }

        public void setData(@Nullable List<String> list) {
            mData.clear();
            if (list != null) {
                mData.addAll(list);
            }
            notifyDataSetChanged();
        }

        public void remove(int pos) {
            mData.remove(pos);
            notifyItemRemoved(pos);
        }

        public void add(int pos, String item) {
            mData.add(pos, item);
            notifyItemInserted(pos);
        }

        public void prepend(@NonNull List<String> items) {
            mData.addAll(0, items);
            notifyDataSetChanged();
        }

        public void append(@NonNull List<String> items) {
            mData.addAll(items);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public QMUISwipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_list_item_1, parent, false);
            final QMUISwipeViewHolder vh = new QMUISwipeViewHolder(view);
            vh.addSwipeAction(mDeleteAction);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(),
                            "click position=" + vh.getAdapterPosition(),
                            Toast.LENGTH_SHORT).show();
                }
            });
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull QMUISwipeViewHolder holder, int position) {
            TextView textView = holder.itemView.findViewById(R.id.text);
            textView.setText(mData.get(position));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }
}
