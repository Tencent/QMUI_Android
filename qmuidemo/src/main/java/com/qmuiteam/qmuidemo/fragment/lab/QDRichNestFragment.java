package com.qmuiteam.qmuidemo.fragment.lab;

import android.view.LayoutInflater;
import android.view.View;

import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.Group;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import butterknife.BindView;
import butterknife.ButterKnife;

@Widget(group = Group.Lab, name = "rich nest", iconRes = R.mipmap.icon_grid_in_progress)
public class QDRichNestFragment extends BaseFragment {

    @BindView(R.id.topbar) QMUITopBarLayout mTopBarLayout;
    @BindView(R.id.coordinator) CoordinatorLayout mCoordinatorLayout;

    @Override
    protected View onCreateView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_rich_nest, null);
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

    }
}
