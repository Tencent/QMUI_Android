/*
 * Tencent is pleased to support the open source community by making QMUI_Android available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qmuiteam.qmui.widget.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.layout.QMUIConstraintLayout;
import com.qmuiteam.qmui.skin.QMUISkinHelper;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.textview.QMUISpanTouchFixTextView;

import androidx.appcompat.widget.AppCompatImageView;


/**
 * 菜单类型的对话框的item
 *
 * @author chantchen
 * @date 2016-1-20
 */

public class QMUIDialogMenuItemView extends QMUIConstraintLayout {
    private int index = -1;
    private MenuItemViewListener mListener;
    private boolean mIsChecked = false;

    public QMUIDialogMenuItemView(Context context) {
        super(context, null, R.attr.qmui_dialog_menu_item_style);
        QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
        builder.background(R.attr.qmui_skin_support_s_dialog_menu_item_bg);
        QMUISkinHelper.setSkinValue(this, builder);
        QMUISkinValueBuilder.release(builder);
    }

    @SuppressLint("CustomViewStyleable")
    public static TextView createItemTextView(Context context) {
        TextView tv = new QMUISpanTouchFixTextView(context);
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
        tv.setId(View.generateViewId());
        tv.setSingleLine(true);
        tv.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        tv.setDuplicateParentStateEnabled(false);
        QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
        builder.textColor(R.attr.qmui_skin_support_dialog_menu_item_text_color);
        QMUISkinHelper.setSkinValue(tv, builder);
        QMUISkinValueBuilder.release(builder);
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
            LayoutParams lp = new LayoutParams(0, 0);
            lp.leftToLeft = LayoutParams.PARENT_ID;
            lp.rightToRight = LayoutParams.PARENT_ID;
            lp.bottomToBottom = LayoutParams.PARENT_ID;
            lp.topToTop = LayoutParams.PARENT_ID;
            addView(mTextView, lp);
        }

        public void setText(CharSequence text) {
            mTextView.setText(text);
        }

        @Deprecated
        public void setTextColor(int color) {
            mTextView.setTextColor(color);
        }

        public void setTextColorAttr(int colorAttr) {
            int color = QMUISkinHelper.getSkinColor(this, colorAttr);
            mTextView.setTextColor(color);
            QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
            builder.textColor(colorAttr);
            QMUISkinHelper.setSkinValue(mTextView, builder);
            QMUISkinValueBuilder.release(builder);
        }
    }

    public static class MarkItemView extends QMUIDialogMenuItemView {
        private Context mContext;
        private TextView mTextView;
        private AppCompatImageView mCheckedView;

        @SuppressLint("CustomViewStyleable")
        public MarkItemView(Context context) {
            super(context);
            mContext = context;
            mCheckedView = new AppCompatImageView(mContext);
            mCheckedView.setId(View.generateViewId());

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
            QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
            builder.src(R.attr.qmui_skin_support_dialog_mark_drawable);
            QMUISkinHelper.setSkinValue(mCheckedView, builder);
            QMUISkinValueBuilder.release(builder);

            LayoutParams checkLp = new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            checkLp.rightToRight = LayoutParams.PARENT_ID;
            checkLp.topToTop = LayoutParams.PARENT_ID;
            checkLp.bottomToBottom = LayoutParams.PARENT_ID;
            addView(mCheckedView, checkLp);


            mTextView = createItemTextView(mContext);
            LayoutParams tvLp = new LayoutParams(0, 0);
            tvLp.leftToLeft = LayoutParams.PARENT_ID;
            tvLp.topToTop = LayoutParams.PARENT_ID;
            tvLp.bottomToBottom = LayoutParams.PARENT_ID;
            tvLp.rightToLeft = mCheckedView.getId();
            tvLp.rightMargin = markMarginHor;
            addView(mTextView, tvLp);
            mCheckedView.setVisibility(INVISIBLE);
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
            mCheckedView.setVisibility(isChecked ? VISIBLE : INVISIBLE);
        }
    }

    @SuppressLint({"ViewConstructor", "CustomViewStyleable"})
    public static class CheckItemView extends QMUIDialogMenuItemView {
        private Context mContext;
        private TextView mTextView;
        private AppCompatImageView mCheckedView;

        public CheckItemView(Context context, boolean right) {
            super(context);
            mContext = context;
            mCheckedView = new AppCompatImageView(mContext);
            mCheckedView.setId(View.generateViewId());

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

            LayoutParams checkLp = new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            checkLp.topToTop = LayoutParams.PARENT_ID;
            checkLp.bottomToBottom = LayoutParams.PARENT_ID;
            if(right){
                checkLp.rightToRight = LayoutParams.PARENT_ID;
            }else{
                checkLp.leftToLeft = LayoutParams.PARENT_ID;
            }
            QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
            builder.src(R.attr.qmui_skin_support_s_dialog_check_drawable);
            QMUISkinHelper.setSkinValue(mCheckedView, builder);
            QMUISkinValueBuilder.release(builder);
            addView(mCheckedView, checkLp);

            mTextView = createItemTextView(mContext);
            LayoutParams tvLp = new LayoutParams(0, 0);
            if(right){
                tvLp.leftToLeft = LayoutParams.PARENT_ID;
                tvLp.rightToLeft = mCheckedView.getId();
                tvLp.rightMargin = markMarginHor;
            }else{
                tvLp.rightToRight = LayoutParams.PARENT_ID;
                tvLp.leftToRight = mCheckedView.getId();
                tvLp.leftMargin = markMarginHor;
            }

            tvLp.topToTop = LayoutParams.PARENT_ID;
            tvLp.bottomToBottom = LayoutParams.PARENT_ID;
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
