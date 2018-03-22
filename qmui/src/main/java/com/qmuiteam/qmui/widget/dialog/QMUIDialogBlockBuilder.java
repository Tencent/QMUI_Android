package com.qmuiteam.qmui.widget.dialog;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.QMUIWrapContentScrollView;
import com.qmuiteam.qmui.widget.textview.QMUISpanTouchFixTextView;

/**
 * @author cginechen
 * @date 2015-12-12
 */
public class QMUIDialogBlockBuilder extends QMUIDialogBuilder<QMUIDialogBlockBuilder> {
    private CharSequence mContent;


    public QMUIDialogBlockBuilder(Context context) {
        super(context);
        setActionDivider(1, R.color.qmui_config_color_separator, 0, 0);
    }


    public QMUIDialogBlockBuilder setContent(CharSequence content) {
        mContent = content;
        return this;
    }

    public QMUIDialogBlockBuilder setContent(int contentRes) {
        mContent = getBaseContext().getResources().getString(contentRes);
        return this;
    }

    @Override
    protected void onConfigTitleView(TextView titleView) {
        super.onConfigTitleView(titleView);
        if(mContent == null || mContent.length() == 0){
            TypedArray a = titleView.getContext().obtainStyledAttributes(null,
                    R.styleable.QMUIDialogTitleTvCustomDef, R.attr.qmui_dialog_title_style, 0);
            int count = a.getIndexCount();
            for (int i = 0; i < count; i++) {
                int attr = a.getIndex(i);
                if (attr == R.styleable.QMUIDialogTitleTvCustomDef_qmui_paddingBottomWhenNotContent) {
                    titleView.setPadding(
                            titleView.getPaddingLeft(),
                            titleView.getPaddingTop(),
                            titleView.getPaddingRight(),
                            a.getDimensionPixelSize(attr, titleView.getPaddingBottom())
                    );
                }
            }
            a.recycle();
        }
    }

    @Override
    protected void onCreateContent(QMUIDialog dialog, ViewGroup parent, Context context) {
        if(mContent != null && mContent.length() > 0){
            TextView contentTv = new QMUISpanTouchFixTextView(context);
            QMUIResHelper.assignTextViewWithAttr(contentTv, R.attr.qmui_dialog_message_content_style);

            if (!hasTitle()) {
                TypedArray a = context.obtainStyledAttributes(null,
                        R.styleable.QMUIDialogMessageTvCustomDef,
                        R.attr.qmui_dialog_message_content_style, 0);
                int count = a.getIndexCount();
                for (int i = 0; i < count; i++) {
                    int attr = a.getIndex(i);
                    if (attr == R.styleable.QMUIDialogMessageTvCustomDef_qmui_paddingTopWhenNotTitle) {
                        contentTv.setPadding(
                                contentTv.getPaddingLeft(),
                                a.getDimensionPixelSize(attr, contentTv.getPaddingTop()),
                                contentTv.getPaddingRight(),
                                contentTv.getPaddingBottom()
                        );
                    }
                }
                a.recycle();
            }
            contentTv.setText(mContent);


            QMUIWrapContentScrollView scrollView = new QMUIWrapContentScrollView(context);
            scrollView.setMaxHeight(getContentAreaMaxHeight());
            scrollView.addView(contentTv);
            parent.addView(scrollView);
        }
    }

    @Override
    public QMUIDialog create(int style) {
        setActionContainerOrientation(VERTICAL);
        return super.create(style);
    }
}
