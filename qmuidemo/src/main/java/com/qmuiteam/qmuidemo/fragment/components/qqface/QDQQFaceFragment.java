package com.qmuiteam.qmuidemo.fragment.components.qqface;

import android.view.LayoutInflater;
import android.view.View;

import com.qmuiteam.qmui.qqface.QMUIQQFaceView;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;
import com.qmuiteam.qmuidemo.QDDataManager;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.model.QDItemDescription;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.lib.Group;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author cginechen
 * @date 2016-12-22
 */
@Widget(group = Group.Lab, widgetClass = QMUIQQFaceView.class, iconRes = R.mipmap.icon_grid_qq_face_view)
public class QDQQFaceFragment extends BaseFragment {
    @BindView(R.id.topbar) QMUITopBar mTopBar;
    @BindView(R.id.groupListView) QMUIGroupListView mGroupListView;

    private QDDataManager mQDDataManager;
    private QDItemDescription mQDItemDescription;

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_grouplistview, null);
        ButterKnife.bind(this, root);

        mQDDataManager = QDDataManager.getInstance();
        mQDItemDescription = mQDDataManager.getDescription(this.getClass());
        initTopBar();

        initGroupListView();

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

    private void initGroupListView() {
        QMUIGroupListView.newSection(getContext())
                .addItemView(mGroupListView.createItemView(mQDDataManager.getName(
                        QDQQFaceUsageFragment.class)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QDQQFaceUsageFragment fragment = new QDQQFaceUsageFragment();
                        startFragment(fragment);
                    }
                })
                .addItemView(mGroupListView.createItemView(mQDDataManager.getName(
                        QDQQFacePerformanceTestFragment.class)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QDQQFacePerformanceTestFragment fragment = new QDQQFacePerformanceTestFragment();
                        startFragment(fragment);
                    }
                })
                .addTo(mGroupListView);


    }
}
