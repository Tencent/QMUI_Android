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

package com.qmuiteam.qmuidemo.fragment.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmuidemo.QDMainActivity;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.base.BaseRecyclerAdapter;
import com.qmuiteam.qmuidemo.base.RecyclerViewHolder;
import com.qmuiteam.qmuidemo.decorator.GridDividerItemDecoration;
import com.qmuiteam.qmuidemo.fragment.QDAboutFragment;
import com.qmuiteam.qmuidemo.fragment.util.QDNotchHelperFragment;
import com.qmuiteam.qmuidemo.model.QDItemDescription;

import java.util.List;

/**
 * @author cginechen
 * @date 2016-10-20
 */

public abstract class HomeController extends LinearLayout {

    protected QMUITopBarLayout mTopBar;
    protected RecyclerView mRecyclerView;

    private HomeControlListener mHomeControlListener;
    private ItemAdapter mItemAdapter;
    private int mDiffRecyclerViewSaveStateId = QMUIViewHelper.generateViewId();

    public HomeController(Context context) {
        super(context);
        setOrientation(LinearLayout.VERTICAL);
        mTopBar = new QMUITopBarLayout(context);
        mTopBar.setId(View.generateViewId());
        mTopBar.setFitsSystemWindows(true);
        mRecyclerView = new RecyclerView(context);
        mRecyclerView.setId(View.generateViewId());
        addView(mTopBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(mRecyclerView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0, 1f));
        initTopBar();
        initRecyclerView();
    }

    protected void startFragment(BaseFragment fragment) {
        if (mHomeControlListener != null) {
            mHomeControlListener.startFragment(fragment);
        }
    }

    public void setHomeControlListener(HomeControlListener homeControlListener) {
        mHomeControlListener = homeControlListener;
    }

    protected abstract String getTitle();

    private void initTopBar() {
        mTopBar.setTitle(getTitle());

        mTopBar.addRightImageButton(R.mipmap.icon_topbar_about, R.id.topbar_right_about_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                QDAboutFragment fragment = new QDAboutFragment();
                startFragment(fragment);
            }
        });
    }

    private void initRecyclerView() {
        mItemAdapter = getItemAdapter();
        mItemAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int pos) {
                QDItemDescription item = mItemAdapter.getItem(pos);
                try {
                    BaseFragment fragment = item.getDemoClass().newInstance();
                    if (fragment instanceof QDNotchHelperFragment) {
                        Context context = getContext();
                        Intent intent = QDMainActivity.of(context, QDNotchHelperFragment.class);
                        context.startActivity(intent);
                        if (context instanceof Activity) {
                            ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                    } else {
                        startFragment(fragment);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mRecyclerView.setAdapter(mItemAdapter);
        int spanCount = 3;
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        mRecyclerView.addItemDecoration(new GridDividerItemDecoration(getContext(), spanCount));
    }

    protected abstract ItemAdapter getItemAdapter();

    public interface HomeControlListener {
        void startFragment(BaseFragment fragment);
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        int id = mRecyclerView.getId();
        mRecyclerView.setId(mDiffRecyclerViewSaveStateId);
        super.dispatchSaveInstanceState(container);
        mRecyclerView.setId(id);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        int id = mRecyclerView.getId();
        mRecyclerView.setId(mDiffRecyclerViewSaveStateId);
        super.dispatchRestoreInstanceState(container);
        mRecyclerView.setId(id);
    }

    static class ItemAdapter extends BaseRecyclerAdapter<QDItemDescription> {

        public ItemAdapter(Context ctx, List<QDItemDescription> data) {
            super(ctx, data);
        }

        @Override
        public int getItemLayoutId(int viewType) {
            return R.layout.home_item_layout;
        }

        @Override
        public void bindData(RecyclerViewHolder holder, int position, QDItemDescription item) {
            holder.getTextView(R.id.item_name).setText(item.getName());
            if (item.getIconRes() != 0) {
                holder.getImageView(R.id.item_icon).setImageResource(item.getIconRes());
            }
        }
    }
}
