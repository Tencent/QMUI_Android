package com.qmuiteam.qmuidemo.fragment.util;

import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIColorHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.lib.Group;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.model.QDItemDescription;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * {@link QMUIColorHelper} 的使用示例。
 * Created by Kayo on 2016/12/1.
 */

@Widget(group = Group.Helper, widgetClass = QMUIColorHelper.class, iconRes = R.mipmap.icon_grid_color_helper)
public class QDColorHelperFragment extends BaseFragment {

    @BindView(R.id.topbar) QMUITopBar mTopBar;
    @BindView(R.id.square_alpha) View mAlphaView;
    @BindView(R.id.square_desc_alpha) TextView mAlphaTextView;
    @BindView(R.id.ratioSeekBar) SeekBar mRatioSeekBar;
    @BindView(R.id.transformTextView) TextView mTransformTextView;
    @BindView(R.id.ratioSeekBarWrap) LinearLayout mRatioSeekBarWrap;

    private QDItemDescription mQDItemDescription;

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_colorhelper, null);
        ButterKnife.bind(this, root);

        mQDItemDescription = QDDataManager.getInstance().getDescription(this.getClass());
        initTopBar();

        initContent();

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

    private void initContent() {
        // 设置颜色的 alpha 值
        float alpha = 0.5f;
        int alphaColor = QMUIColorHelper.setColorAlpha(ContextCompat.getColor(getContext(), R.color.colorHelper_square_alpha_background), alpha);
        mAlphaView.setBackgroundColor(alphaColor);
        mAlphaTextView.setText(String.format(getResources().getString(R.string.colorHelper_squqre_alpha), alpha));

        // 根据比例，在两个 color 值之间计算出一个 color 值
        final int fromColor = ContextCompat.getColor(getContext(), R.color.colorHelper_square_from_ratio_background);
        final int toColor = ContextCompat.getColor(getContext(), R.color.colorHelper_square_to_ratio_background);

        mRatioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int ratioColor = QMUIColorHelper.computeColor(fromColor, toColor, (float) progress / 100);
                mRatioSeekBarWrap.setBackgroundColor(ratioColor);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mRatioSeekBar.setProgress(50);

        // 将 color 颜色值转换为字符串
        String transformColor = QMUIColorHelper.colorToString(mTransformTextView.getCurrentTextColor());
        mTransformTextView.setText(String.format("这个 TextView 的字体颜色是：%1$s", transformColor));
    }
}
