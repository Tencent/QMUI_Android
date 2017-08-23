package com.qmuiteam.qmuidemo.fragment.components;

import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.span.QMUITouchableSpan;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.textview.QMUISpanTouchFixTextView;
import com.qmuiteam.qmuidemo.QDDataManager;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author cginechen
 * @date 2017-05-05
 */
@Widget(widgetClass = QMUISpanTouchFixTextView.class, iconRes = R.mipmap.icon_grid_span_touch_fix_text_view)
public class QDSpanTouchFixTextViewFragment extends BaseFragment {

    @BindView(R.id.topbar) QMUITopBar mTopBar;
    @BindView(R.id.sysytem_tv_1) TextView mSystemTv1;
    @BindView(R.id.sysytem_tv_2) TextView mSystemTv2;
    @BindView(R.id.touch_fix_tv_1) QMUISpanTouchFixTextView mSpanTouchFixTextView1;
    @BindView(R.id.touch_fix_tv_2) QMUISpanTouchFixTextView mSpanTouchFixTextView2;

    private int highlightTextNormalColor;
    private int highlightTextPressedColor;
    private int highlightBgNormalColor;
    private int highlightBgPressedColor;

    @OnClick({R.id.touch_fix_tv_1, R.id.sysytem_tv_1})
    void onClickTv(View v) {
        Toast.makeText(getContext(), "onClickTv", Toast.LENGTH_SHORT).show();
    }

    @OnClick({R.id.click_area_1, R.id.click_area_2})
    void onClickArea() {
        Toast.makeText(getContext(), "onClickArea", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected View onCreateView() {
        highlightTextNormalColor = ContextCompat.getColor(getContext(), R.color.app_color_blue_2);
        highlightTextPressedColor = ContextCompat.getColor(getContext(), R.color.app_color_blue_2_pressed);
        highlightBgNormalColor = QMUIResHelper.getAttrColor(getContext(), R.attr.qmui_config_color_gray_8);
        highlightBgPressedColor = QMUIResHelper.getAttrColor(getContext(), R.attr.qmui_config_color_gray_6);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_touch_span_fix_layout, null);
        ButterKnife.bind(this, view);
        initTopBar();

        // 场景一
        mSystemTv1.setMovementMethod(LinkMovementMethod.getInstance());
        mSystemTv1.setText(generateSp(getResources().getString(R.string.system_behavior_1)));

        mSpanTouchFixTextView1.setMovementMethodDefault();
        mSpanTouchFixTextView1.setText(generateSp(getResources().getString(R.string.span_touch_fix_1)));

        // 场景二
        mSystemTv2.setMovementMethod(LinkMovementMethod.getInstance());
        mSystemTv2.setText(generateSp(getResources().getString(R.string.system_behavior_2)));

        mSpanTouchFixTextView2.setMovementMethodDefault();
        mSpanTouchFixTextView2.setNeedForceEventToParent(true);
        mSpanTouchFixTextView2.setText(generateSp(getResources().getString(R.string.span_touch_fix_2)));

        return view;
    }

    private SpannableString generateSp(String text) {
        String highlight1 = "@qmui";
        String highlight2 = "#qmui#";
        SpannableString sp = new SpannableString(text);
        int start = 0, end;
        int index;
        while ((index = text.indexOf(highlight1, start)) > -1) {
            end = index + highlight1.length();
            sp.setSpan(new QMUITouchableSpan(highlightTextNormalColor, highlightTextPressedColor,
                    highlightBgNormalColor, highlightBgPressedColor) {
                @Override
                public void onSpanClick(View widget) {
                    Toast.makeText(getContext(), "click @qmui", Toast.LENGTH_SHORT).show();
                }
            }, index, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            start = end;
        }

        start = 0;
        while ((index = text.indexOf(highlight2, start)) > -1) {
            end = index + highlight2.length();
            sp.setSpan(new QMUITouchableSpan(highlightTextNormalColor, highlightTextPressedColor,
                    highlightBgNormalColor, highlightBgPressedColor) {
                @Override
                public void onSpanClick(View widget) {
                    Toast.makeText(getContext(), "click #qmui#", Toast.LENGTH_SHORT).show();
                }
            }, index, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            start = end;
        }
        return sp;
    }

    private void initTopBar() {
        mTopBar.setTitle(QDDataManager.getInstance().getDescription(this.getClass()).getName());
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });
    }
}
