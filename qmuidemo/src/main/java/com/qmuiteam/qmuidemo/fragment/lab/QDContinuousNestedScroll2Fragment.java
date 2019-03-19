package com.qmuiteam.qmuidemo.fragment.lab;

import android.content.Context;
import android.graphics.Color;
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
import com.qmuiteam.qmui.nestedScroll.QMUIContinuousNestedTopWebView;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUIPagerAdapter;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.QMUIViewPager;
import com.qmuiteam.qmui.widget.webview.QMUIWebView;
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

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

@Widget(group = Group.Other, name = "webview + header + viewpager")
public class QDContinuousNestedScroll2Fragment extends BaseFragment {
    private static final String TAG = "ContinuousNestedScroll";
    @BindView(R.id.topbar) QMUITopBarLayout mTopBarLayout;
    @BindView(R.id.coordinator) QMUIContinuousNestedScrollLayout mCoordinatorLayout;

    private QMUIWebView mNestedWebView;
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
        mNestedWebView = new QMUIContinuousNestedTopWebView(getContext());
        int matchParent = ViewGroup.LayoutParams.MATCH_PARENT;
        CoordinatorLayout.LayoutParams webViewLp = new CoordinatorLayout.LayoutParams(
                matchParent, matchParent);
        webViewLp.setBehavior(new QMUIContinuousNestedTopAreaBehavior(getContext()));
        mCoordinatorLayout.setTopAreaView(mNestedWebView, webViewLp);

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
