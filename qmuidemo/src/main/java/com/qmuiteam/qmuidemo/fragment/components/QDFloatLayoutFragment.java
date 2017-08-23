package com.qmuiteam.qmuidemo.fragment.components;

import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUIFloatLayout;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmuidemo.QDDataManager;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.model.QDItemDescription;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;

import butterknife.BindView;
import butterknife.ButterKnife;

@Widget(widgetClass = QMUIFloatLayout.class, iconRes = R.mipmap.icon_grid_float_layout)
public class QDFloatLayoutFragment extends BaseFragment {

    @BindView(R.id.topbar) QMUITopBar mTopBar;
    @BindView(R.id.qmuidemo_floatlayout) QMUIFloatLayout mFloatLayout;

    private QDItemDescription mQDItemDescription;

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_floatlayout, null);
        ButterKnife.bind(this, root);

        mQDItemDescription = QDDataManager.getInstance().getDescription(this.getClass());
        initTopBar();
        for (int i = 0; i < 8; i++) {
            addItemToFloatLayout(mFloatLayout);
        }
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

        mTopBar.addRightImageButton(R.mipmap.icon_topbar_overflow, R.id.topbar_right_change_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showBottomSheet();
                    }
                });

    }

    private void addItemToFloatLayout(QMUIFloatLayout floatLayout) {
        int currentChildCount = floatLayout.getChildCount();

        TextView textView = new TextView(getActivity());
        int textViewPadding = QMUIDisplayHelper.dp2px(getContext(), 4);
        textView.setPadding(textViewPadding, textViewPadding, textViewPadding, textViewPadding);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.qmui_config_color_white));
        textView.setText(String.valueOf(currentChildCount));
        textView.setBackgroundResource(currentChildCount % 2 == 0 ? R.color.app_color_theme_3 : R.color.app_color_theme_6);

        int textViewSize = QMUIDisplayHelper.dpToPx(60);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(textViewSize, textViewSize);
        floatLayout.addView(textView, lp);
    }

    private void removeItemFromFloatLayout(QMUIFloatLayout floatLayout) {
        if (floatLayout.getChildCount() == 0) {
            return;
        }
        floatLayout.removeView(floatLayout.getChildAt(floatLayout.getChildCount() - 1));
    }

    private void showBottomSheet() {
        new QMUIBottomSheet.BottomListSheetBuilder(getContext())
                .addItem("增加一个item")
                .addItem("减少一个item")
                .addItem("居左")
                .addItem("居中")
                .addItem("居右")
                .addItem("限制最多显示1行")
                .addItem("限制最多显示4个item")
                .addItem("不限制行数或个数")
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        switch (position) {
                            case 0:
                                addItemToFloatLayout(mFloatLayout);
                                break;
                            case 1:
                                removeItemFromFloatLayout(mFloatLayout);
                                break;
                            case 2:
                                mFloatLayout.setGravity(Gravity.LEFT);
                                break;
                            case 3:
                                mFloatLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                                break;
                            case 4:
                                mFloatLayout.setGravity(Gravity.RIGHT);
                                break;
                            case 5:
                                mFloatLayout.setMaxLines(1);
                                break;
                            case 6:
                                mFloatLayout.setMaxNumber(4);
                                break;
                            case 7:
                                mFloatLayout.setMaxLines(Integer.MAX_VALUE);
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                })
                .build().show();
    }

}
