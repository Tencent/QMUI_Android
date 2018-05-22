package com.qmuiteam.qmuidemo.fragment.components;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qmuiteam.qmui.layout.QMUILayoutHelper;
import com.qmuiteam.qmui.layout.QMUILinearLayout;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.model.QDItemDescription;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by cgspine on 2018/3/22.
 */

@Widget(name = "QMUILayout", iconRes = R.mipmap.icon_grid_layout)
public class QDLayoutFragment extends BaseFragment {

    @BindView(R.id.topbar) QMUITopBarLayout mTopBar;
    @BindView(R.id.layout_for_test) QMUILinearLayout mTestLayout;
    @BindView(R.id.test_seekbar_alpha) SeekBar mAlphaSeekBar;
    @BindView(R.id.test_seekbar_elevation) SeekBar mElevationSeekBar;
    @BindView(R.id.alpha_tv) TextView mAlphaTv;
    @BindView(R.id.elevation_tv) TextView mElevationTv;
    @BindView(R.id.hide_radius_group) RadioGroup mHideRadiusGroup;

    private QDItemDescription mQDItemDescription;
    private float mShadowAlpha = 0.25f;
    private int mShadowElevationDp = 14;
    private int mRadius;

    @Override
    protected View onCreateView() {
        mRadius = QMUIDisplayHelper.dp2px(getContext(), 15);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_layout, null);
        ButterKnife.bind(this, view);
        mQDItemDescription = QDDataManager.getInstance().getDescription(this.getClass());
        initTopBar();
        initLayout();
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

    private void initLayout() {
        mAlphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mShadowAlpha = progress * 1f / 100;
                mAlphaTv.setText("alpha: " + mShadowAlpha);
                mTestLayout.setRadiusAndShadow(mRadius,
                        QMUIDisplayHelper.dp2px(getContext(), mShadowElevationDp),
                        mShadowAlpha);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mElevationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mShadowElevationDp = progress;
                mElevationTv.setText("elevation: " + progress + "dp");
                mTestLayout.setRadiusAndShadow(mRadius,
                        QMUIDisplayHelper.dp2px(getActivity(), mShadowElevationDp), mShadowAlpha);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mAlphaSeekBar.setProgress((int) (mShadowAlpha * 100));
        mElevationSeekBar.setProgress(mShadowElevationDp);

        mHideRadiusGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.hide_radius_none:
                        mTestLayout.setRadius(mRadius, QMUILayoutHelper.HIDE_RADIUS_SIDE_NONE);
                        break;
                    case R.id.hide_radius_left:
                        mTestLayout.setRadius(mRadius, QMUILayoutHelper.HIDE_RADIUS_SIDE_LEFT);
                        break;
                    case R.id.hide_radius_top:
                        mTestLayout.setRadius(mRadius, QMUILayoutHelper.HIDE_RADIUS_SIDE_TOP);
                        break;
                    case R.id.hide_radius_bottom:
                        mTestLayout.setRadius(mRadius, QMUILayoutHelper.HIDE_RADIUS_SIDE_BOTTOM);
                        break;
                    case R.id.hide_radius_right:
                        mTestLayout.setRadius(mRadius, QMUILayoutHelper.HIDE_RADIUS_SIDE_RIGHT);
                        break;
                }
            }
        });
    }
}
