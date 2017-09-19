package com.qmuiteam.qmui.widget.grouplist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.util.QMUILangHelper;


/**
 * 用作通用列表 {@link QMUIGroupListView} 里每个 {@link QMUIGroupListView.Section} 的头部或尾部，也可单独使用。
 *
 * @author molicechen
 * @date 2015-01-07
 */

public class QMUIGroupListSectionHeaderFooterView extends LinearLayout {

    private TextView mTextView;

    public QMUIGroupListSectionHeaderFooterView(Context context) {
        this(context, null, R.attr.QMUIGroupListSectionViewStyle);
    }


    public QMUIGroupListSectionHeaderFooterView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.QMUIGroupListSectionViewStyle);
    }

    public QMUIGroupListSectionHeaderFooterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public QMUIGroupListSectionHeaderFooterView(Context context, CharSequence titleText) {
        this(context);
        setText(titleText);
    }

    public QMUIGroupListSectionHeaderFooterView(Context context, CharSequence titleText, boolean isFooter) {
        this(context);

        if (isFooter) {
            // Footer View 不需要 padding bottom
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), 0);
        }

        setText(titleText);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.qmui_group_list_section_layout, this, true);
        setGravity(Gravity.BOTTOM);

        mTextView = (TextView) findViewById(R.id.group_list_section_header_textView);
    }

    public void setText(CharSequence text) {
        if (QMUILangHelper.isNullOrEmpty(text)) {
            mTextView.setVisibility(GONE);
        } else {
            mTextView.setVisibility(VISIBLE);
        }
        mTextView.setText(text);
    }

    public TextView getTextView() {
        return mTextView;
    }

    public void setTextGravity(int gravity) {
        mTextView.setGravity(gravity);
    }
}
