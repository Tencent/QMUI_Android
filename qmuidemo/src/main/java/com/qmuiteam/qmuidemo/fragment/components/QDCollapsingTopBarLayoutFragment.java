package com.qmuiteam.qmuidemo.fragment.components;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.qmuiteam.qmui.widget.QMUICollapsingTopBarLayout;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmuidemo.QDDataManager;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.adaptor.QDRecyclerViewAdapter;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author cginechen
 * @date 2017-09-02
 */

@Widget(widgetClass = QMUICollapsingTopBarLayout.class)
public class QDCollapsingTopBarLayoutFragment extends BaseFragment {

    private View mRootView;
    QDRecyclerViewAdapter mRecyclerViewAdapter;
    LinearLayoutManager mPagerLayoutManager;


    @BindView(R.id.recyclerView) RecyclerView mRecyclerView;
    @BindView(R.id.collapsing_topbar_layout) QMUICollapsingTopBarLayout mCollapsingTopBarLayout;
    @BindView(R.id.topbar) QMUITopBar mTopBar;

    @Override
    protected View onCreateView() {
        mRootView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_collapsing_topbar_layout, null);
        ButterKnife.bind(this, mRootView);
        initTopBar();
        mPagerLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mPagerLayoutManager);
        mRecyclerViewAdapter = new QDRecyclerViewAdapter();
        mRecyclerViewAdapter.setItemCount(10);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        return mRootView;
    }

    @Override
    protected boolean translucentFull() {
        return true;
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
