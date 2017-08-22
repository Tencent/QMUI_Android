package com.qmuiteam.qmuidemo.fragment.components;

import android.view.LayoutInflater;
import android.view.View;

import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmuidemo.QDDataManager;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.model.QDItemDescription;

import butterknife.BindView;
import butterknife.ButterKnife;

@Widget(name = "RoundButton", iconRes = R.mipmap.icon_grid_button)
public class QDButtonFragment extends BaseFragment {

    @BindView(R.id.topbar) QMUITopBar mTopBar;

    private QDItemDescription mQDItemDescription;

    @Override
    protected View onCreateView() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_button, null);
        ButterKnife.bind(this, view);
        mQDItemDescription = QDDataManager.getInstance().getDescription(this.getClass());

        initTopBar();

        return view;
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
}
