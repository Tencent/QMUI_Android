package com.qmuiteam.qmuidemo.fragment.components;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * {@link QMUIRadiusImageView} 的使用示例。
 * Created by cgspine on 15/9/15.
 */
@Widget(widgetClass = QMUIRadiusImageView.class, iconRes = R.mipmap.icon_grid_radius_image_view)
public class QDRadiusImageViewFragment extends BaseFragment {

    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.radiusImageView)
    QMUIRadiusImageView mRadiusImageView;

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_radius_imageview, null);
        ButterKnife.bind(this, root);

        initTopBar();

        reset();

        return root;
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        mTopBar.setTitle(QDDataManager.getInstance().getName(this.getClass()));

        // 动态修改效果按钮
        mTopBar.addRightImageButton(R.mipmap.icon_topbar_overflow, R.id.topbar_right_change_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showBottomSheetList();
                    }
                });
    }

    private void reset() {
        mRadiusImageView.setBorderColor(ContextCompat.getColor(getContext(), R.color.radiusImageView_border_color));
        mRadiusImageView.setBorderWidth(QMUIDisplayHelper.dp2px(getContext(), 2));
        mRadiusImageView.setCornerRadius(QMUIDisplayHelper.dp2px(getContext(), 10));
        mRadiusImageView.setSelectedMaskColor(ContextCompat.getColor(getContext(), R.color.radiusImageView_selected_mask_color));
        mRadiusImageView.setSelectedBorderColor(ContextCompat.getColor(getContext(), R.color.radiusImageView_selected_border_color));
        mRadiusImageView.setSelectedBorderWidth(QMUIDisplayHelper.dp2px(getContext(), 3));
        mRadiusImageView.setTouchSelectModeEnabled(true);
        mRadiusImageView.setCircle(false);
        mRadiusImageView.setOval(false);
    }

    private void showBottomSheetList() {
        new QMUIBottomSheet.BottomListSheetBuilder(getActivity())
                .addItem(getResources().getString(R.string.circularImageView_modify_1))
                .addItem(getResources().getString(R.string.circularImageView_modify_2))
                .addItem(getResources().getString(R.string.circularImageView_modify_3))
                .addItem(getResources().getString(R.string.circularImageView_modify_4))
                .addItem(getResources().getString(R.string.circularImageView_modify_5))
                .addItem(getResources().getString(R.string.circularImageView_modify_6))
                .addItem(getResources().getString(R.string.circularImageView_modify_7))
                .addItem(getResources().getString(R.string.circularImageView_modify_8))
                .addItem(getResources().getString(R.string.circularImageView_modify_9))
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        dialog.dismiss();
                        reset();
                        switch (position) {
                            case 0:
                                mRadiusImageView.setBorderColor(Color.BLACK);
                                mRadiusImageView.setBorderWidth(QMUIDisplayHelper.dp2px(getContext(), 4));
                                break;
                            case 1:
                                mRadiusImageView.setSelectedBorderWidth(QMUIDisplayHelper.dp2px(getContext(), 6));
                                mRadiusImageView.setSelectedBorderColor(Color.GREEN);
                                break;
                            case 2:
                                mRadiusImageView.setSelectedMaskColor(ContextCompat.getColor(getContext(), R.color.radiusImageView_selected_mask_color));
                                break;
                            case 3:
                                if (mRadiusImageView.isSelected()) {
                                    mRadiusImageView.setSelected(false);
                                } else {
                                    mRadiusImageView.setSelected(true);
                                }
                                break;
                            case 4:
                                mRadiusImageView.setCornerRadius(QMUIDisplayHelper.dp2px(getContext(), 20));
                                break;
                            case 5:
                                mRadiusImageView.setCircle(true);
                                break;
                            case 6:
                                mRadiusImageView.setOval(true);
                            case 7:
                                mRadiusImageView.setTouchSelectModeEnabled(false);
                            default:
                                break;
                        }
                    }
                })
                .build()
                .show();
    }
}
