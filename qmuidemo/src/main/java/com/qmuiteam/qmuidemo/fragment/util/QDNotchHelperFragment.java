package com.qmuiteam.qmuidemo.fragment.util;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUINotchHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.QMUITabSegment;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.Group;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@Widget(group = Group.Helper, name = "QMUINotchHelper", iconRes = R.mipmap.icon_grid_span)
public class QDNotchHelperFragment extends BaseFragment {
    @BindView(R.id.not_safe_bg) FrameLayout mNoSafeBgLayout;
    @BindView(R.id.safe_area_tv) TextView mSafeAreaTv;
    @BindView(R.id.topbar) QMUITopBarLayout mTopBar;
    @BindView(R.id.tabs_container) FrameLayout mTabContainer;
    @BindView(R.id.tabs) QMUITabSegment mTabSegment;

    @OnClick(R.id.safe_area_tv)
    void onClickTv() {
        if (QMUIDisplayHelper.isFullScreen(getActivity())) {
            changeToNotFullScreen();
        } else {
            changeToFullScreen();
        }
    }

    @Override
    protected View onCreateView() {
        View layout = LayoutInflater.from(getContext()).inflate(R.layout.fragment_notch, null);
        ButterKnife.bind(this, layout);
        initTopBar();
        initTabs();
        mNoSafeBgLayout.setPadding(
                QMUINotchHelper.getSafeInsetLeft(getContext()),
                QMUINotchHelper.getSafeInsetTop(getContext()),
                QMUINotchHelper.getSafeInsetRight(getContext()),
                QMUINotchHelper.getSafeInsetBottom(getContext())
        );
        return layout;
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        mTopBar.setTitle(QDDataManager.getInstance().getName(this.getClass()));
    }

    private void initTabs() {
        QMUITabSegment.Tab component = new QMUITabSegment.Tab(
                ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_component),
                ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_component_selected),
                "Components", false
        );

        QMUITabSegment.Tab util = new QMUITabSegment.Tab(
                ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_util),
                ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_util_selected),
                "Helper", false
        );
        QMUITabSegment.Tab lab = new QMUITabSegment.Tab(
                ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_lab),
                ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_lab_selected),
                "Lab", false
        );
        mTabSegment.addTab(component)
                .addTab(util)
                .addTab(lab);
        mTabSegment.notifyDataChanged();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        changeToFullScreen();
    }

    private void changeToFullScreen() {
        Activity activity = getActivity();
        if (activity != null) {
            QMUIDisplayHelper.setFullScreen(getActivity());
            Window window = activity.getWindow();
            if (window == null) {
                return;
            }
            View decorView = window.getDecorView();
            int systemUi = decorView.getSystemUiVisibility();
            systemUi |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                systemUi |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                systemUi |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            decorView.setSystemUiVisibility(systemUi);
            QMUIViewHelper.fadeOut(mTopBar, 300, null, true);
            QMUIViewHelper.fadeOut(mTabContainer, 300, null, true);
        }
    }

    private void changeToNotFullScreen() {
        Activity activity = getActivity();
        if (activity != null) {
            QMUIDisplayHelper.cancelFullScreen(getActivity());
            Window window = activity.getWindow();
            if (window == null) {
                return;
            }
            View decorView = window.getDecorView();
            int systemUi = decorView.getSystemUiVisibility();
            systemUi &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                systemUi &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
                systemUi |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            }
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                systemUi &= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            decorView.setSystemUiVisibility(systemUi);
            QMUIViewHelper.fadeIn(mTopBar, 300, null, true);
            QMUIViewHelper.fadeIn(mTabContainer, 300, null, true);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mNoSafeBgLayout != null) {
            mNoSafeBgLayout.setPadding(
                    QMUINotchHelper.getSafeInsetLeft(getContext()),
                    QMUINotchHelper.getSafeInsetTop(getContext()),
                    QMUINotchHelper.getSafeInsetRight(getContext()),
                    QMUINotchHelper.getSafeInsetBottom(getContext())
            );
        }
    }

    @Override
    protected void popBackStack() {
        changeToNotFullScreen();
        super.popBackStack();
    }
}
