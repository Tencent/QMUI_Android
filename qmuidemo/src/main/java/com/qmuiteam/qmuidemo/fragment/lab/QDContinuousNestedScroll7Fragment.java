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

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.nestedScroll.IQMUIContinuousNestedBottomView;
import com.qmuiteam.qmui.nestedScroll.QMUIContinuousNestedBottomAreaBehavior;
import com.qmuiteam.qmui.nestedScroll.QMUIContinuousNestedBottomDelegateLayout;
import com.qmuiteam.qmui.nestedScroll.QMUIContinuousNestedBottomRecyclerView;
import com.qmuiteam.qmui.nestedScroll.QMUIContinuousNestedScrollLayout;
import com.qmuiteam.qmui.nestedScroll.QMUIContinuousNestedTopAreaBehavior;
import com.qmuiteam.qmui.nestedScroll.QMUIContinuousNestedTopDelegateLayout;
import com.qmuiteam.qmui.nestedScroll.QMUIContinuousNestedTopWebView;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUIPagerAdapter;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.QMUIViewPager;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.base.BaseRecyclerAdapter;
import com.qmuiteam.qmuidemo.base.RecyclerViewHolder;
import com.qmuiteam.qmuidemo.lib.Group;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

@Widget(group = Group.Other, name = "(header + webview + bottom) + (part sticky header + viewpager)")
public class QDContinuousNestedScroll7Fragment extends BaseFragment {
    private static final String TAG = "ContinuousNestedScroll";
    @BindView(R.id.topbar) QMUITopBarLayout mTopBarLayout;
    @BindView(R.id.coordinator) QMUIContinuousNestedScrollLayout mCoordinatorLayout;

    private QMUIContinuousNestedTopDelegateLayout mTopDelegateLayout;
    private QMUIContinuousNestedTopWebView mNestedWebView;
    private BottomView mBottomView;


    @Override
    protected View onCreateView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_continuous_nested_scroll, null);
        ButterKnife.bind(this, view);
        initTopBar();
        initCoordinatorLayout();
        return view;
    }

    private void initTopBar() {
        mTopBarLayout.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        mTopBarLayout.setTitle(QDDataManager.getInstance().getName(this.getClass()));
    }

    private void initCoordinatorLayout() {
        mTopDelegateLayout = new QMUIContinuousNestedTopDelegateLayout(getContext());
        mTopDelegateLayout.setBackgroundColor(Color.LTGRAY);
        mNestedWebView = new QMUIContinuousNestedTopWebView(getContext());

        AppCompatTextView headerView = new AppCompatTextView(getContext()) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(
                        QMUIDisplayHelper.dp2px(getContext(), 100), MeasureSpec.EXACTLY
                ));
            }
        };
        headerView.setTextSize(17);
        headerView.setBackgroundColor(Color.GRAY);
        headerView.setTextColor(Color.WHITE);
        headerView.setText("This is Top Header");
        headerView.setGravity(Gravity.CENTER);
        mTopDelegateLayout.setHeaderView(headerView);

        AppCompatTextView footerView = new AppCompatTextView(getContext()) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(
                        QMUIDisplayHelper.dp2px(getContext(), 100), MeasureSpec.EXACTLY
                ));
            }
        };
        footerView.setTextSize(17);
        footerView.setBackgroundColor(Color.GRAY);
        footerView.setTextColor(Color.WHITE);
        footerView.setGravity(Gravity.CENTER);
        footerView.setText("This is Top Footer");
        mTopDelegateLayout.setFooterView(footerView);

        mTopDelegateLayout.setDelegateView(mNestedWebView);

        int matchParent = ViewGroup.LayoutParams.MATCH_PARENT;
        CoordinatorLayout.LayoutParams topLp = new CoordinatorLayout.LayoutParams(
                matchParent, matchParent);
        topLp.setBehavior(new QMUIContinuousNestedTopAreaBehavior(getContext()));
        mCoordinatorLayout.setTopAreaView(mTopDelegateLayout, topLp);

        mBottomView = new BottomView(getContext());
        CoordinatorLayout.LayoutParams recyclerViewLp = new CoordinatorLayout.LayoutParams(
                matchParent, matchParent);
        recyclerViewLp.setBehavior(new QMUIContinuousNestedBottomAreaBehavior());
        mCoordinatorLayout.setBottomAreaView(mBottomView, recyclerViewLp);

        mNestedWebView.loadUrl("https://mp.weixin.qq.com/s/zgfLOMD2JfZJKfHx-5BsBg");

        mCoordinatorLayout.addOnScrollListener(new QMUIContinuousNestedScrollLayout.OnScrollListener() {
            @Override
            public void onTopScroll(int offset, int range, int innerOffset, int innerRange) {
                Log.i(TAG, "offset = " + offset + " ; range = " + range +
                        "; innerOffset = " + innerOffset + " ;innerRange = " + innerRange);
            }
        });
    }

    private void onDataLoaded(BaseRecyclerAdapter<String> adapter) {
        List<String> data = new ArrayList<>(Arrays.asList("Helps", "Maintain", "Liver", "Health", "Function", "Supports", "Healthy", "Fat",
                "Metabolism", "Nuturally", "Bracket", "Refrigerator", "Bathtub", "Wardrobe", "Comb", "Apron", "Carpet", "Bolster", "Pillow", "Cushion"));
        Collections.shuffle(data);
        adapter.setData(data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mNestedWebView != null) {
            mCoordinatorLayout.removeView(mNestedWebView);
            mNestedWebView.destroy();
            mNestedWebView = null;
        }
    }

    class BottomView extends QMUIContinuousNestedBottomDelegateLayout {

        private MyViewPager mViewPager;
        private QMUIContinuousNestedBottomRecyclerView mCurrentItemView;

        public BottomView(Context context) {
            super(context);
        }

        public BottomView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public BottomView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @NonNull
        @Override
        protected View onCreateHeaderView() {
            TextView headerView = new TextView(getContext());
            headerView.setTextSize(16);
            headerView.setTextColor(Color.BLACK);
            headerView.setBackgroundColor(Color.LTGRAY);
            headerView.setGravity(Gravity.CENTER);
            headerView.setText("This is normal view with ViewPager below");
            return headerView;
        }

        @Override
        protected int getHeaderHeightLayoutParam() {
            return QMUIDisplayHelper.dp2px(getContext(), 200);
        }

        @Override
        protected int getHeaderStickyHeight() {
            return QMUIDisplayHelper.dp2px(getContext(), 50);
        }


        @NonNull
        @Override
        protected View onCreateContentView() {
            mViewPager = new MyViewPager(getContext());
            mViewPager.setAdapter(new QMUIPagerAdapter() {
                @Override
                protected Object hydrate(ViewGroup container, int position) {
                    QMUIContinuousNestedBottomRecyclerView recyclerView = new QMUIContinuousNestedBottomRecyclerView(getContext());

                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()) {
                        @Override
                        public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                            return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT);
                        }
                    });

                    BaseRecyclerAdapter<String> adapter = new BaseRecyclerAdapter<String>(getContext(), null) {
                        @Override
                        public int getItemLayoutId(int viewType) {
                            return android.R.layout.simple_list_item_1;
                        }

                        @Override
                        public void bindData(RecyclerViewHolder holder, int position, String item) {
                            holder.setText(android.R.id.text1, item);
                        }
                    };
                    adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View itemView, int pos) {
                            Toast.makeText(getContext(), "click position=" + pos, Toast.LENGTH_SHORT).show();
                        }
                    });
                    recyclerView.setAdapter(adapter);
                    onDataLoaded(adapter);
                    return recyclerView;
                }

                @Override
                protected void populate(ViewGroup container, Object item, int position) {
                    container.addView((View) item);
                }

                @Override
                protected void destroy(ViewGroup container, int position, Object object) {
                    container.removeView((View) object);
                }

                @Override
                public int getCount() {
                    return 3;
                }

                @Override
                public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
                    return view == o;
                }

                @Override
                public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                    super.setPrimaryItem(container, position, object);
                    mCurrentItemView = (QMUIContinuousNestedBottomRecyclerView) object;
                }
            });
            return mViewPager;
        }

        class MyViewPager extends QMUIViewPager implements IQMUIContinuousNestedBottomView {

            public MyViewPager(Context context) {
                super(context);
            }

            @Override
            public void consumeScroll(int dyUnconsumed) {
                if (mCurrentItemView != null) {
                    mCurrentItemView.consumeScroll(dyUnconsumed);
                }

            }

            @Override
            public int getContentHeight() {
                if (mCurrentItemView != null) {
                    return mCurrentItemView.getContentHeight();
                }
                return 0;
            }
        }
    }
}
