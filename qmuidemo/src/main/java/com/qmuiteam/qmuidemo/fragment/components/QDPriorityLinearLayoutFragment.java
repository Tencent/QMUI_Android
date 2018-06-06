package com.qmuiteam.qmuidemo.fragment.components;

import android.view.LayoutInflater;
import android.view.View;

import com.qmuiteam.qmui.layout.QMUIPriorityLinearLayout;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;

import butterknife.BindView;
import butterknife.ButterKnife;

@Widget(widgetClass = QMUIPriorityLinearLayout.class, iconRes = R.mipmap.icon_grid_float_layout)
public class QDPriorityLinearLayoutFragment extends BaseFragment {

    @BindView(R.id.topbar) QMUITopBarLayout mTopBar;

    public QDPriorityLinearLayoutFragment() {
    }

    @Override
    protected View onCreateView() {
        View rootView = LayoutInflater.from(getContext()).inflate(
                R.layout.fragment_priority_linear_layout, null);
        ButterKnife.bind(this, rootView);
        initTopBar();
        return rootView;
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
}

