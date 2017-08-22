package com.qmuiteam.qmuidemo.fragment.util;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.qmuiteam.qmui.span.QMUIAlignMiddleImageSpan;
import com.qmuiteam.qmui.span.QMUIBlockSpaceSpan;
import com.qmuiteam.qmui.span.QMUICustomTypefaceSpan;
import com.qmuiteam.qmui.span.QMUIMarginImageSpan;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIDrawableHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmuidemo.QDApplication;
import com.qmuiteam.qmuidemo.QDDataManager;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.Group;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.model.QDItemDescription;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Kayo on 2016/12/15.
 */

@Widget(group = Group.Component, name = "Span", iconRes = R.mipmap.icon_grid_span)
public class QDSpanFragment extends BaseFragment {

    /**
     * 特殊字体 人民币符号
     */
    public static Typeface TYPEFACE_RMB;

    static {
        try {
            Typeface tmpRmb = Typeface.createFromAsset(QDApplication.getContext().getAssets(),
                    "fonts/iconfont.ttf");
            TYPEFACE_RMB = Typeface.create(tmpRmb, Typeface.NORMAL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BindView(R.id.topbar) QMUITopBar mTopBar;
    @BindView(R.id.alignMiddle) TextView mAlignMiddleTextView;
    @BindView(R.id.marginImage) TextView mMarginImageTextView;
    @BindView(R.id.blockSpace) TextView mBlockSpaceTextView;
    @BindView(R.id.customTypeface) TextView mCustomTypefaceTextView;
    private QDItemDescription mQDItemDescription;

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_spanhelper, null);
        ButterKnife.bind(this, root);

        mQDItemDescription = QDDataManager.getInstance().getDescription(this.getClass());
        initTopBar();

        initContentView();

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

    private void initContentView() {
        // 支持垂直居中的 ImageSpan
        int alignMiddleIconLength = QMUIDisplayHelper.dp2px(getContext(), 20);
        final float spanWidthCharacterCount = 2f;
        SpannableString spannable = new SpannableString("[icon]" + "这是一行示例文字，前面的 Span 设置了和文字垂直居中并占 " + spanWidthCharacterCount + " 个中文字的宽度");
        Drawable iconDrawable = QMUIDrawableHelper.createDrawableWithSize(getResources(), alignMiddleIconLength, alignMiddleIconLength, QMUIDisplayHelper.dp2px(getContext(), 4), ContextCompat.getColor(getContext(), R.color.app_color_theme_3));
        if (iconDrawable != null) {
            iconDrawable.setBounds(0, 0, iconDrawable.getIntrinsicWidth(), iconDrawable.getIntrinsicHeight());
        }
        ImageSpan alignMiddleImageSpan = new QMUIAlignMiddleImageSpan(iconDrawable, QMUIAlignMiddleImageSpan.ALIGN_MIDDLE, spanWidthCharacterCount);
        spannable.setSpan(alignMiddleImageSpan, 0, "[icon]".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        mAlignMiddleTextView.setText(spannable);

        // 支持增加左右间距的 ImageSpan
        int marginImageLength = QMUIDisplayHelper.dp2px(getContext(), 20);
        Drawable marginIcon = QMUIDrawableHelper.createDrawableWithSize(getResources(), marginImageLength, marginImageLength, QMUIDisplayHelper.dp2px(getContext(), 4), ContextCompat.getColor(getContext(), R.color.app_color_theme_5));
        marginIcon.setBounds(0, 0, marginIcon.getIntrinsicWidth(), marginIcon.getIntrinsicHeight());
        CharSequence marginImageTextOne = "左侧内容";
        SpannableString marginImageText = new SpannableString(marginImageTextOne + "[margin]右侧内容");
        QMUIMarginImageSpan marginImageSpan = new QMUIMarginImageSpan(marginIcon, QMUIAlignMiddleImageSpan.ALIGN_MIDDLE, QMUIDisplayHelper.dp2px(getContext(), 10), QMUIDisplayHelper.dp2px(getContext(), 10));
        marginImageText.setSpan(marginImageSpan, marginImageTextOne.length(), marginImageTextOne.length() + "[margin]".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        mMarginImageTextView.setText(marginImageText);

        // 整行的空白 Span，可用来用于制作段间距
        String paragraphFirst = "这是第一段比较长的段落，演示在段落之间插入段落间距。\n";
        String paragraphSecond = "这是第二段比较长的段落，演示在段落之间插入段落间距。";
        String spaceString = "[space]";
        SpannableString paragraphText = new SpannableString(paragraphFirst + spaceString + paragraphSecond);
        QMUIBlockSpaceSpan blockSpaceSpan = new QMUIBlockSpaceSpan(QMUIDisplayHelper.dp2px(getContext(), 6));
        paragraphText.setSpan(blockSpaceSpan, paragraphFirst.length(), paragraphFirst.length() + spaceString.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        mBlockSpaceTextView.setText(paragraphText);

        // 自定义部分文字的字体
        SpannableString customTypefaceText = new SpannableString(getResources().getString(R.string.spanUtils_rmb) + "100， 前面的人民币符号使用自定义字体特殊处理，对比这个普通的人民币符号: " + getResources().getString(R.string.spanUtils_rmb));
        customTypefaceText.setSpan(new QMUICustomTypefaceSpan("", TYPEFACE_RMB), 0, getString(R.string.spanUtils_rmb).length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        mCustomTypefaceTextView.setText(customTypefaceText);
    }
}
