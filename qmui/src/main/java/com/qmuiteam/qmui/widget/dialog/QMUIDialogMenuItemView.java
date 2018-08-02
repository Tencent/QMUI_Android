package com.qmuiteam.qmui.widget.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.layout.QMUIRelativeLayout;
import com.qmuiteam.qmui.util.QMUIDrawableHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;


/**
 * 菜单类型的对话框的item
 *
 * @author chantchen
 * @date 2016-1-20
 */

public class QMUIDialogMenuItemView extends QMUIRelativeLayout {
    private int index = -1;
    private MenuItemViewListener mListener;
    private boolean mIsChecked = false;

    public QMUIDialogMenuItemView(Context context) {
        super(context, null, R.attr.qmui_dialog_menu_item_style);
    }

    public static TextView createItemTextView(Context context) {
        TextView tv = new TextView(context);
        TypedArray a = context.obtainStyledAttributes(null, R.styleable.QMUIDialogMenuTextStyleDef, R.attr.qmui_dialog_menu_item_style, 0);
        int count = a.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.QMUIDialogMenuTextStyleDef_android_gravity) {
                tv.setGravity(a.getInt(attr, -1));
            } else if (attr == R.styleable.QMUIDialogMenuTextStyleDef_android_textColor) {
                tv.setTextColor(a.getColorStateList(attr));
            } else if (attr == R.styleable.QMUIDialogMenuTextStyleDef_android_textSize) {
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, a.getDimensionPixelSize(attr, 0));
            }
        }
        a.recycle();

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
            mCheckedView.setId(QMUIViewHelper.generateViewId());

            TypedArray a = context.obtainStyledAttributes(null, R.styleable.QMUIDialogMenuMarkDef,
                    R.attr.qmui_dialog_menu_item_style, 0);
            int markMarginHor = 0;
            int count = a.getIndexCount();
            for (int i = 0; i < count; i++) {
                int attr = a.getIndex(i);
                if (attr == R.styleable.QMUIDialogMenuMarkDef_qmui_dialog_menu_item_check_mark_margin_hor) {
                    markMarginHor = a.getDimensionPixelSize(attr, 0);
                } else if (attr == R.styleable.QMUIDialogMenuMarkDef_qmui_dialog_menu_item_mark_drawable) {
                    mCheckedView.setImageDrawable(QMUIResHelper.getAttrDrawable(context, a, attr));
                }
            }
            a.recycle();

            LayoutParams checkLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            checkLp.addRule(CENTER_VERTICAL, TRUE);
            checkLp.addRule(ALIGN_PARENT_RIGHT, TRUE);
            checkLp.leftMargin = markMarginHor;
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
            QMUIViewHelper.safeSetImageViewSelected(mCheckedView, isChecked);
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
            mCheckedView.setId(QMUIViewHelper.generateViewId());

            TypedArray a = context.obtainStyledAttributes(null, R.styleable.QMUIDialogMenuCheckDef,
                    R.attr.qmui_dialog_menu_item_style, 0);
            int markMarginHor = 0;
            int count = a.getIndexCount();
            for (int i = 0; i < count; i++) {
                int attr = a.getIndex(i);
                if (attr == R.styleable.QMUIDialogMenuCheckDef_qmui_dialog_menu_item_check_mark_margin_hor) {
                    markMarginHor = a.getDimensionPixelSize(attr, 0);
                } else if (attr == R.styleable.QMUIDialogMenuCheckDef_qmui_dialog_menu_item_check_drawable) {
                    mCheckedView.setImageDrawable(QMUIResHelper.getAttrDrawable(context, a, attr));
                }
            }
            a.recycle();

            LayoutParams checkLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            checkLp.addRule(CENTER_VERTICAL, TRUE);
            if (right) {
                checkLp.addRule(ALIGN_PARENT_RIGHT, TRUE);
                checkLp.leftMargin = markMarginHor;
            } else {
                checkLp.addRule(ALIGN_PARENT_LEFT, TRUE);
                checkLp.rightMargin = markMarginHor;
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

        public CharSequence getText() {
            return mTextView.getText();
        }

        @Override
        protected void notifyCheckChange(boolean isChecked) {
            QMUIViewHelper.safeSetImageViewSelected(mCheckedView, isChecked);
        }
    }
}
