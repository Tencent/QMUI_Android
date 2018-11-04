package com.qmuiteam.qmuidemo.fragment.lab;

import com.qmuiteam.qmuidemo.fragment.QDWebExplorerFragment;

public class QDArchWebViewTestFragment extends QDWebExplorerFragment {

    @Override
    protected void initTopbar() {
        super.initTopbar();
        QDArchTestFragment.injectEntrance(mTopBarLayout);
    }
}
