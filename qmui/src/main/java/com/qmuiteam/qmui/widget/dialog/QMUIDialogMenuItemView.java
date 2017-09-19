package com.qmuiteam.qmui.widget.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;


/**
 * 菜单类型的对话框的item
 *
 * @author chantchen
 * @date 2016-1-20
 */

public class QMUIDialogMenuItemView extends RelativeLayout {
    private int index = -1;
    private MenuItemViewListener mListener;
    private boolean mIsChecked = false;

    public QMUIDialogMenuItemView(Context context) {
        super(context);
        QMUIViewHelper.setBackgroundKeepingPadding(this, QMUIResHelper.getAttrDrawable(context, R.attr.qmui_dialog_content_list_item_bg));
        setPadding(
                QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_padding_horizontal), 0,
                QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_padding_horizontal), 0
        );
    }

    public static TextView createItemTextView(Context context) {
        TextView tv = new TextView(context);
        tv.setTextColor(QMUIResHelper.getAttrColor(context, R.attr.qmui_dialog_menu_item_text_color));
        tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_content_list_item_text_size));
        tv.setSingleLine(true);
        tv.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        tv.setDuplicateParentStateEnabled(false);
        return tv;
    }

    public int getMenuIndex() {
        return this.index;
    }

    public void setMenuIndex(int index) {
        this.index = index;
    }

    protected void notifyCheckChange(boolean isChecked) {

    }

    public boolean isChecked() {
        return mIsChecked;
    }

    public void setChecked(boolean checked) {
        mIsChecked = checked;
        notifyCheckChange(mIsChecked);
    }

    public void setListener(MenuItemViewListener listener) {
        if (!isClickable()) {
            setClickable(true);
        }
        mListener = listener;
    }

    @Override
    public boolean performClick() {
        if (mListener != null) {
            mListener.onClick(index);
        }
        return super.performClick();
    }

    public interface MenuItemViewListener {
        void onClick(int index);
    }

    public static class TextItemView extends QMUIDialogMenuItemView {
        protected TextView mTextView;

        public TextItemView(Context context) {
            super(context);
            init();
        }

        public TextItemView(Context context, CharSequence text) {
            super(context);
            init();
            setText(text);
        }

        private void init() {
            mTextView = createItemTextView(getContext());
            addView(mTextView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }

        public void setText(CharSequence text) {
            mTextView.setText(text);
        }

        public void setTextColor(int color) {
            mTextView.setTextColor(color);
        }
    }

    public static class MarkItemView extends QMUIDialogMenuItemView {
        private Context mContext;
        private TextView mTextView;
        private ImageView mCheckedView;

        public MarkItemView(Context context) {
            super(context);
            mContext = context;
            mCheckedView = new ImageView(mContext);
            mCheckedView.setImageResource(R.drawable.qmui_s_dialog_check_mark);
            mCheckedView.setId(QMUIViewHelper.generateViewId());
            LayoutParams checkLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            checkLp.addRule(CENTER_VERTICAL, TRUE);
            checkLp.addRule(ALIGN_PARENT_RIGHT, TRUE);
            checkLp.leftMargin = QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_menu_item_check_icon_margin_horizontal);
            addView(mCheckedView, checkLp);

            mTextView = createItemTextView(mContext);
            LayoutParams tvLp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            tvLp.addRule(ALIGN_PARENT_LEFT, TRUE);
            tvLp.addRule(LEFT_OF, mCheckedView.getId());
            addView(mTextView, tvLp);
        }

        public MarkItemView(Context context, CharSequence text) {
            this(context);
            setText(text);
        }

        public void setText(CharSequence text) {
            mTextView.setText(text);
        }

        @Override
        protected void notifyCheckChange(boolean isChecked) {
            mCheckedView.setSelected(isChecked);
        }
    }

    @SuppressLint("ViewConstructor")
    public static class CheckItemView extends QMUIDialogMenuItemView {
        private Context mContext;
        private TextView mTextView;
        private ImageView mCheckedView;

        public CheckItemView(Context context, boolean right) {
            super(context);
            mContext = context;
            mCheckedView = new ImageView(mContext);
            mCheckedView.setImageDrawable(QMUIResHelper.getAttrDrawable(context, R.attr.qmui_s_checkbox));
            mCheckedView.setId(QMUIViewHelper.generateViewId());
            LayoutParams checkLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            checkLp.addRule(CENTER_VERTICAL, TRUE);
            if (right) {
                checkLp.addRule(ALIGN_PARENT_RIGHT, TRUE);
                checkLp.leftMargin = QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_menu_item_check_icon_margin_horizontal);
            } else {
                checkLp.addRule(ALIGN_PARENT_LEFT, TRUE);
                checkLp.rightMargin = QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_menu_item_check_icon_margin_horizontal);
            }

            addView(mCheckedView, checkLp);

            mTextView = createItemTextView(mContext);
            LayoutParams tvLp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            if (right) {
                tvLp.addRule(LEFT_OF, mCheckedView.getId());
            } else {
                tvLp.addRule(RIGHT_OF, mCheckedView.getId());
            }

            addView(mTextView, tvLp);
        }

        public CheckItemView(Context context, boolean right, CharSequence text) {
            this(context, right);
            setText(text);
        }

        public void setText(CharSequence text) {
            mTextView.setText(text);
        }

        @Override
        protected void notifyCheckChange(boolean isChecked) {
            mCheckedView.setSelected(isChecked);
        }
    }
}
