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


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.TransformationMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.layout.QMUIConstraintLayout;
import com.qmuiteam.qmui.layout.QMUILinearLayout;
import com.qmuiteam.qmui.skin.QMUISkinHelper;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.textview.QMUISpanTouchFixTextView;

import java.util.ArrayList;
import java.util.BitSet;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * QMUIDialog 对话框一般由 {@link QMUIDialogBuilder} 及其子类创建, 不同的 Builder 可以创建不同类型的对话框,
 * 例如消息类型的对话框、菜单项对话框等等。
 *
 * @author cginechen
 * @date 2015-10-20
 * @see QMUIDialogBuilder
 */
public class QMUIDialog extends QMUIBaseDialog {
    private Context mBaseContext;

    public QMUIDialog(Context context) {
        this(context, R.style.QMUI_Dialog);
    }

    public QMUIDialog(Context context, int styleRes) {
        super(context, styleRes);
        mBaseContext = context;
        init();
    }

    private void init() {
        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }


    public void showWithImmersiveCheck(Activity activity) {
        // http://stackoverflow.com/questions/22794049/how-to-maintain-the-immersive-mode-in-dialogs
        Window window = getWindow();
        if (window == null) {
            return;
        }

        Window activityWindow = activity.getWindow();
        int activitySystemUi = activityWindow.getDecorView().getSystemUiVisibility();
        if ((activitySystemUi & View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) == View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN ||
                (activitySystemUi & View.SYSTEM_UI_FLAG_FULLSCREEN) == View.SYSTEM_UI_FLAG_FULLSCREEN) {
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            window.getDecorView().setSystemUiVisibility(
                    activity.getWindow().getDecorView().getSystemUiVisibility());
            super.show();
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        } else {
            super.show();
        }
    }

    public void showWithImmersiveCheck() {
        if (!(mBaseContext instanceof Activity)) {
            super.show();
            return;
        }
        Activity activity = (Activity) mBaseContext;
        showWithImmersiveCheck(activity);
    }


    /**
     * 消息类型的对话框 Builder。通过它可以生成一个带标题、文本消息、按钮的对话框。
     */
    public static class MessageDialogBuilder extends QMUIDialogBuilder<MessageDialogBuilder> {
        protected CharSequence mMessage;

        public MessageDialogBuilder(Context context) {
            super(context);
        }

        /**
         * 设置对话框的消息文本
         */
        public MessageDialogBuilder setMessage(CharSequence message) {
            this.mMessage = message;
            return this;
        }

        /**
         * 设置对话框的消息文本
         */
        public MessageDialogBuilder setMessage(int resId) {
            return setMessage(getBaseContext().getResources().getString(resId));
        }

        @Nullable
        @Override
        protected View onCreateContent(@NonNull QMUIDialog dialog, @NonNull QMUIDialogView parent, @NonNull Context context) {
            if (mMessage != null && mMessage.length() != 0) {
                QMUISpanTouchFixTextView tv = new QMUISpanTouchFixTextView(context);
                assignMessageTvWithAttr(tv, hasTitle(), R.attr.qmui_dialog_message_content_style);
                tv.setText(mMessage);
                tv.setMovementMethodDefault();

                QMUISkinValueBuilder valueBuilder = QMUISkinValueBuilder.acquire();
                valueBuilder.textColor(R.attr.qmui_skin_support_dialog_message_text_color);
                QMUISkinHelper.setSkinValue(tv, valueBuilder);
                QMUISkinValueBuilder.release(valueBuilder);

                return wrapWithScroll(tv);
            }
            return null;
        }

        @Nullable
        @Override
        protected View onCreateTitle(@NonNull QMUIDialog dialog, @NonNull QMUIDialogView parent, @NonNull Context context) {
            View tv = super.onCreateTitle(dialog, parent, context);
            if (tv != null && (mMessage == null || mMessage.length() == 0)) {
                TypedArray a = context.obtainStyledAttributes(null,
                        R.styleable.QMUIDialogTitleTvCustomDef, R.attr.qmui_dialog_title_style, 0);
                int count = a.getIndexCount();
                for (int i = 0; i < count; i++) {
                    int attr = a.getIndex(i);
                    if (attr == R.styleable.QMUIDialogTitleTvCustomDef_qmui_paddingBottomWhenNotContent) {
                        tv.setPadding(
                                tv.getPaddingLeft(),
                                tv.getPaddingTop(),
                                tv.getPaddingRight(),
                                a.getDimensionPixelSize(attr, tv.getPaddingBottom())
                        );
                    }
                }
                a.recycle();
            }
            return tv;
        }

        public static void assignMessageTvWithAttr(TextView messageTv, boolean hasTitle, int defAttr) {
            QMUIResHelper.assignTextViewWithAttr(messageTv, defAttr);

            if (!hasTitle) {
                TypedArray a = messageTv.getContext().obtainStyledAttributes(null,
                        R.styleable.QMUIDialogMessageTvCustomDef, defAttr, 0);
                int count = a.getIndexCount();
                for (int i = 0; i < count; i++) {
                    int attr = a.getIndex(i);
                    if (attr == R.styleable.QMUIDialogMessageTvCustomDef_qmui_paddingTopWhenNotTitle) {
                        messageTv.setPadding(
                                messageTv.getPaddingLeft(),
                                a.getDimensionPixelSize(attr, messageTv.getPaddingTop()),
                                messageTv.getPaddingRight(),
                                messageTv.getPaddingBottom()
                        );
                    }
                }
                a.recycle();
            }
        }
    }

    /**
     * 带 CheckBox 的消息确认框 Builder
     */
    public static class CheckBoxMessageDialogBuilder extends QMUIDialogBuilder<CheckBoxMessageDialogBuilder> {
        protected String mMessage;
        private boolean mIsChecked = false;
        private QMUISpanTouchFixTextView mTextView;

        public CheckBoxMessageDialogBuilder(Context context) {
            super(context);

        }

        /**
         * 设置对话框的消息文本
         */
        public CheckBoxMessageDialogBuilder setMessage(String message) {
            this.mMessage = message;
            return this;
        }

        /**
         * 设置对话框的消息文本
         */
        public CheckBoxMessageDialogBuilder setMessage(int resid) {
            return setMessage(getBaseContext().getResources().getString(resid));
        }

        /**
         * CheckBox 是否处于勾选状态
         */
        public boolean isChecked() {
            return mIsChecked;
        }

        /**
         * 设置 CheckBox 的勾选状态
         */
        public CheckBoxMessageDialogBuilder setChecked(boolean checked) {
            if (mIsChecked != checked) {
                mIsChecked = checked;
                if (mTextView != null) {
                    mTextView.setSelected(checked);
                }
            }

            return this;
        }

        @Nullable
        @Override
        protected View onCreateContent(QMUIDialog dialog, QMUIDialogView parent, Context context) {
            if (mMessage != null && mMessage.length() != 0) {
                mTextView = new QMUISpanTouchFixTextView(context);
                mTextView.setMovementMethodDefault();
                MessageDialogBuilder.assignMessageTvWithAttr(mTextView, hasTitle(), R.attr.qmui_dialog_message_content_style);
                mTextView.setText(mMessage);
                Drawable drawable = QMUISkinHelper.getSkinDrawable(mTextView, R.attr.qmui_skin_support_s_dialog_check_drawable);
                if (drawable != null) {
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    mTextView.setCompoundDrawables(drawable, null, null, null);
                }
                QMUISkinValueBuilder valueBuilder = QMUISkinValueBuilder.acquire();
                valueBuilder.textColor(R.attr.qmui_skin_support_dialog_message_text_color);
                valueBuilder.textCompoundLeftSrc(R.attr.qmui_skin_support_s_dialog_check_drawable);
                QMUISkinHelper.setSkinValue(mTextView, valueBuilder);
                QMUISkinValueBuilder.release(valueBuilder);
                mTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setChecked(!mIsChecked);
                    }
                });
                mTextView.setSelected(mIsChecked);
                return wrapWithScroll(mTextView);
            }
            return null;
        }

        @Deprecated
        public QMUISpanTouchFixTextView getTextView() {
            return mTextView;
        }

    }

    /**
     * 带输入框的对话框 Builder
     */
    public static class EditTextDialogBuilder extends QMUIDialogBuilder<EditTextDialogBuilder> {
        protected String mPlaceholder;
        protected TransformationMethod mTransformationMethod;
        protected EditText mEditText;
        protected AppCompatImageView mRightImageView;
        private int mInputType = InputType.TYPE_CLASS_TEXT;
        private CharSequence mDefaultText = null;
        private TextWatcher mTextWatcher;

        public EditTextDialogBuilder(Context context) {
            super(context);
        }

        /**
         * 设置输入框的 placeholder
         */
        public EditTextDialogBuilder setPlaceholder(String placeholder) {
            this.mPlaceholder = placeholder;
            return this;
        }

        /**
         * 设置输入框的 placeholder
         */
        public EditTextDialogBuilder setPlaceholder(int resId) {
            return setPlaceholder(getBaseContext().getResources().getString(resId));
        }

        public EditTextDialogBuilder setDefaultText(CharSequence defaultText) {
            mDefaultText = defaultText;
            return this;
        }

        /**
         * 设置 EditText 的 transformationMethod
         */
        public EditTextDialogBuilder setTransformationMethod(TransformationMethod transformationMethod) {
            mTransformationMethod = transformationMethod;
            return this;
        }

        /**
         * 设置 EditText 的 inputType
         */
        public EditTextDialogBuilder setInputType(int inputType) {
            mInputType = inputType;
            return this;
        }

        public EditTextDialogBuilder setTextWatcher(TextWatcher textWatcher) {
            mTextWatcher = textWatcher;
            return this;
        }

        @Override
        protected ConstraintLayout.LayoutParams onCreateContentLayoutParams(Context context) {
            ConstraintLayout.LayoutParams lp = super.onCreateContentLayoutParams(context);
            int marginHor = QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_padding_horizontal);
            lp.leftMargin = marginHor;
            lp.rightMargin = marginHor;
            lp.topMargin = QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_edit_margin_top);
            lp.bottomMargin = QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_edit_margin_bottom);
            return lp;
        }

        @Nullable
        @Override
        protected View onCreateContent(QMUIDialog dialog, QMUIDialogView parent, Context context) {
            QMUIConstraintLayout boxLayout = new QMUIConstraintLayout(context);
            boxLayout.onlyShowBottomDivider(0, 0,
                    QMUIResHelper.getAttrDimen(context,
                            R.attr.qmui_dialog_edit_bottom_line_height),
                    QMUIResHelper.getAttrColor(context,
                            R.attr.qmui_skin_support_dialog_edit_bottom_line_color));
            QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
            builder.bottomSeparator(R.attr.qmui_skin_support_dialog_edit_bottom_line_color);
            QMUISkinHelper.setSkinValue(boxLayout, builder);

            mEditText = new AppCompatEditText(context);
            mEditText.setBackgroundResource(0);
            MessageDialogBuilder.assignMessageTvWithAttr(mEditText, hasTitle(), R.attr.qmui_dialog_edit_content_style);
            mEditText.setFocusable(true);
            mEditText.setFocusableInTouchMode(true);
            mEditText.setImeOptions(EditorInfo.IME_ACTION_GO);
            mEditText.setId(R.id.qmui_dialog_edit_input);

            if (!QMUILangHelper.isNullOrEmpty(mDefaultText)) {
                mEditText.setText(mDefaultText);
            }
            if (mTextWatcher != null) {
                mEditText.addTextChangedListener(mTextWatcher);
            }
            builder.clear();
            builder.textColor(R.attr.qmui_skin_support_dialog_edit_text_color);
            builder.hintColor(R.attr.qmui_skin_support_dialog_edit_text_hint_color);
            QMUISkinHelper.setSkinValue(mEditText, builder);
            QMUISkinValueBuilder.release(builder);


            mRightImageView = new AppCompatImageView(context);
            mRightImageView.setId(R.id.qmui_dialog_edit_right_icon);
            mRightImageView.setVisibility(View.GONE);
            configRightImageView(mRightImageView, mEditText);

            if (mTransformationMethod != null) {
                mEditText.setTransformationMethod(mTransformationMethod);
            } else {
                mEditText.setInputType(mInputType);
            }

            if (mPlaceholder != null) {
                mEditText.setHint(mPlaceholder);
            }
            boxLayout.addView(mEditText, createEditTextLayoutParams(context));
            boxLayout.addView(mRightImageView, createRightIconLayoutParams(context));

            return boxLayout;
        }

        protected void configRightImageView(AppCompatImageView imageView, EditText editText) {

        }

        protected ConstraintLayout.LayoutParams createEditTextLayoutParams(Context context) {
            ConstraintLayout.LayoutParams editLp = new ConstraintLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT);
            editLp.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
            editLp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            editLp.rightToLeft = R.id.qmui_dialog_edit_right_icon;
            editLp.rightToRight = QMUIDisplayHelper.dp2px(context, 5);
            editLp.goneRightMargin = 0;
            return editLp;
        }

        protected ConstraintLayout.LayoutParams createRightIconLayoutParams(Context context) {
            ConstraintLayout.LayoutParams rightIconLp = new ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rightIconLp.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
            rightIconLp.bottomToBottom = R.id.qmui_dialog_edit_input;
            return rightIconLp;
        }

        @Override
        protected void onAfterCreate(QMUIDialog dialog, QMUIDialogRootLayout rootLayout, Context context) {
            super.onAfterCreate(dialog, rootLayout, context);
            final InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            dialog.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    inputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                }
            });
            mEditText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mEditText.requestFocus();
                    inputMethodManager.showSoftInput(mEditText, 0);
                }
            }, 300);
        }

        /**
         * 注意该方法只在调用 {@link #create()} 或 {@link #create(int)} 或 {@link #show()} 生成 Dialog 之后
         * 才能返回对应的 EditText，在此之前将返回 null
         */
        @Deprecated
        public EditText getEditText() {
            return mEditText;
        }

        public ImageView getRightImageView() {
            return mRightImageView;
        }
    }


    public static class MenuBaseDialogBuilder<T extends QMUIDialogBuilder> extends QMUIDialogBuilder<T> {
        protected ArrayList<ItemViewFactory> mMenuItemViewsFactoryList;
        protected ArrayList<QMUIDialogMenuItemView> mMenuItemViews = new ArrayList<>();

        public MenuBaseDialogBuilder(Context context) {
            super(context);
            mMenuItemViewsFactoryList = new ArrayList<>();
        }

        public void clear() {
            mMenuItemViewsFactoryList.clear();
        }

        @SuppressWarnings("unchecked")
        @Deprecated
        public T addItem(final QMUIDialogMenuItemView itemView, final OnClickListener listener) {
            itemView.setMenuIndex(mMenuItemViewsFactoryList.size());
            itemView.setListener(new QMUIDialogMenuItemView.MenuItemViewListener() {
                @Override
                public void onClick(int index) {
                    onItemClick(index);
                    if (listener != null) {
                        listener.onClick(mDialog, index);
                    }
                }
            });
            mMenuItemViewsFactoryList.add(new ItemViewFactory() {
                @Override
                public QMUIDialogMenuItemView createItemView(Context context) {
                    return itemView;
                }
            });
            return (T) this;
        }

        public T addItem(final ItemViewFactory itemViewFactory, final OnClickListener listener) {
            mMenuItemViewsFactoryList.add(new ItemViewFactory() {
                @Override
                public QMUIDialogMenuItemView createItemView(Context context) {
                    QMUIDialogMenuItemView itemView = itemViewFactory.createItemView(context);
                    itemView.setMenuIndex(mMenuItemViewsFactoryList.indexOf(this));
                    itemView.setListener(new QMUIDialogMenuItemView.MenuItemViewListener() {
                        @Override
                        public void onClick(int index) {
                            onItemClick(index);
                            if (listener != null) {
                                listener.onClick(mDialog, index);
                            }
                        }
                    });
                    return itemView;
                }
            });
            return (T) this;
        }

        protected void onItemClick(int index) {

        }

        @Nullable
        @Override
        protected View onCreateContent(QMUIDialog dialog, QMUIDialogView parent, Context context) {
            LinearLayout layout = new QMUILinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);


            TypedArray a = context.obtainStyledAttributes(
                    null, R.styleable.QMUIDialogMenuContainerStyleDef, R.attr.qmui_dialog_menu_container_style, 0);
            int count = a.getIndexCount();
            int paddingTop = 0, paddingBottom = 0, paddingVerWhenSingle = 0,
                    paddingTopWhenTitle = 0, paddingBottomWhenAction = 0, itemHeight = -1;
            for (int i = 0; i < count; i++) {
                int attr = a.getIndex(i);
                if (attr == R.styleable.QMUIDialogMenuContainerStyleDef_android_paddingTop) {
                    paddingTop = a.getDimensionPixelSize(attr, paddingTop);
                } else if (attr == R.styleable.QMUIDialogMenuContainerStyleDef_android_paddingBottom) {
                    paddingBottom = a.getDimensionPixelSize(attr, paddingBottom);
                } else if (attr == R.styleable.QMUIDialogMenuContainerStyleDef_qmui_dialog_menu_container_single_padding_vertical) {
                    paddingVerWhenSingle = a.getDimensionPixelSize(attr, paddingVerWhenSingle);
                } else if (attr == R.styleable.QMUIDialogMenuContainerStyleDef_qmui_dialog_menu_container_padding_top_when_title_exist) {
                    paddingTopWhenTitle = a.getDimensionPixelSize(attr, paddingTopWhenTitle);
                } else if (attr == R.styleable.QMUIDialogMenuContainerStyleDef_qmui_dialog_menu_container_padding_bottom_when_action_exist) {
                    paddingBottomWhenAction = a.getDimensionPixelSize(attr, paddingBottomWhenAction);
                } else if (attr == R.styleable.QMUIDialogMenuContainerStyleDef_qmui_dialog_menu_item_height) {
                    itemHeight = a.getDimensionPixelSize(attr, itemHeight);
                }
            }
            a.recycle();

            if (mMenuItemViewsFactoryList.size() == 1) {
                paddingBottom = paddingTop = paddingVerWhenSingle;
            }

            if (hasTitle()) {
                paddingTop = paddingTopWhenTitle;
            }

            if (mActions.size() > 0) {
                paddingBottom = paddingBottomWhenAction;
            }

            layout.setPadding(0, paddingTop, 0, paddingBottom);

            LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight);
            itemLp.gravity = Gravity.CENTER_VERTICAL;


            mMenuItemViews.clear();
            for (ItemViewFactory factory : mMenuItemViewsFactoryList) {
                QMUIDialogMenuItemView itemView = factory.createItemView(context);
                layout.addView(itemView, itemLp);
                mMenuItemViews.add(itemView);
            }
            return wrapWithScroll(layout);
        }

        public interface ItemViewFactory {
            QMUIDialogMenuItemView createItemView(Context context);
        }
    }

    /**
     * 菜单类型的对话框 Builder
     */
    public static class MenuDialogBuilder extends MenuBaseDialogBuilder<MenuDialogBuilder> {

        public MenuDialogBuilder(Context context) {
            super(context);
        }

        /**
         * 添加多个菜单项
         *
         * @param items    所有菜单项的文字
         * @param listener 菜单项的点击事件
         */
        public MenuDialogBuilder addItems(CharSequence[] items, OnClickListener listener) {
            for (final CharSequence item : items) {
                addItem(item, listener);
            }
            return this;
        }

        /**
         * 添加单个菜单项
         *
         * @param item     菜单项的文字
         * @param listener 菜单项的点击事件
         */
        public MenuDialogBuilder addItem(final CharSequence item, OnClickListener listener) {
            addItem(new ItemViewFactory() {
                @Override
                public QMUIDialogMenuItemView createItemView(Context context) {
                    return new QMUIDialogMenuItemView.TextItemView(context, item);
                }
            }, listener);
            return this;
        }

    }

    /**
     * 单选类型的对话框 Builder
     */
    public static class CheckableDialogBuilder extends MenuBaseDialogBuilder<CheckableDialogBuilder> {

        /**
         * 当前被选中的菜单项的下标, 负数表示没选中任何项
         */
        private int mCheckedIndex = -1;

        public CheckableDialogBuilder(Context context) {
            super(context);
        }

        /**
         * 获取当前选中的菜单项的下标
         *
         * @return 负数表示没选中任何项
         */
        public int getCheckedIndex() {
            return mCheckedIndex;
        }

        /**
         * 设置选中的菜单项的下班
         */
        public CheckableDialogBuilder setCheckedIndex(int checkedIndex) {
            mCheckedIndex = checkedIndex;
            return this;
        }

        @Nullable
        @Override
        protected View onCreateContent(QMUIDialog dialog, QMUIDialogView parent, Context context) {
            View result = super.onCreateContent(dialog, parent, context);
            if (mCheckedIndex > -1 && mCheckedIndex < mMenuItemViews.size()) {
                mMenuItemViews.get(mCheckedIndex).setChecked(true);
            }
            return result;
        }

        @Override
        protected void onItemClick(int index) {
            for (int i = 0; i < mMenuItemViews.size(); i++) {
                QMUIDialogMenuItemView itemView = mMenuItemViews.get(i);
                if (i == index) {
                    itemView.setChecked(true);
                    mCheckedIndex = index;
                } else {
                    itemView.setChecked(false);
                }
            }
        }

        /**
         * 添加菜单项
         *
         * @param items    所有菜单项的文字
         * @param listener 菜单项的点击事件,可以在点击事件里调用 {@link #setCheckedIndex(int)} 来设置选中某些菜单项
         */
        public CheckableDialogBuilder addItems(CharSequence[] items, OnClickListener listener) {
            for (final CharSequence item : items) {
                addItem(new ItemViewFactory() {
                    @Override
                    public QMUIDialogMenuItemView createItemView(Context context) {
                        return new QMUIDialogMenuItemView.MarkItemView(context, item);
                    }
                }, listener);
            }
            return this;
        }
    }

    /**
     * 多选类型的对话框 Builder
     */
    public static class MultiCheckableDialogBuilder extends MenuBaseDialogBuilder<MultiCheckableDialogBuilder> {

        /**
         * 该 int 的每一位标识菜单的每一项是否被选中 (1为选中,0位不选中)
         */
        private BitSet mCheckedItems = new BitSet();

        public MultiCheckableDialogBuilder(Context context) {
            super(context);
        }

        /**
         * 设置被选中的菜单项的下标
         *
         * @param checkedItems <b>注意: 该 int 参数的每一位标识菜单项的每一项是否被选中</b>
         *                     <p>如 20 表示选中下标为 1、3 的菜单项, 因为 (2<<1) + (2<<3) = 20</p>
         */
        public MultiCheckableDialogBuilder setCheckedItems(BitSet checkedItems) {
            mCheckedItems.clear();
            mCheckedItems.or(checkedItems);
            return this;
        }

        /**
         * 设置被选中的菜单项的下标
         *
         * @param checkedIndexes 被选中的菜单项的下标组成的数组,如 [1,3] 表示选中下标为 1、3 的菜单项
         */
        public MultiCheckableDialogBuilder setCheckedItems(int[] checkedIndexes) {
            mCheckedItems.clear();
            if (checkedIndexes != null && checkedIndexes.length > 0) {
                for (int checkedIndex : checkedIndexes) {
                    mCheckedItems.set(checkedIndex);
                }
            }
            return this;
        }

        /**
         * 添加菜单项
         *
         * @param items    所有菜单项的文字
         * @param listener 菜单项的点击事件,可以在点击事件里调用 {@link #setCheckedItems(int[])}} 来设置选中某些菜单项
         */
        public MultiCheckableDialogBuilder addItems(CharSequence[] items, OnClickListener listener) {
            for (final CharSequence item : items) {
                addItem(new ItemViewFactory() {
                    @Override
                    public QMUIDialogMenuItemView createItemView(Context context) {
                        return new QMUIDialogMenuItemView.CheckItemView(context, true, item);
                    }
                }, listener);
            }
            return this;
        }

        @Nullable
        @Override
        protected View onCreateContent(QMUIDialog dialog, QMUIDialogView parent, Context context) {
            View result = super.onCreateContent(dialog, parent, context);
            for (int i = 0; i < mMenuItemViews.size(); i++) {
                QMUIDialogMenuItemView itemView = mMenuItemViews.get(i);
                itemView.setChecked(mCheckedItems.get(i));
            }
            return result;
        }

        @Override
        protected void onItemClick(int index) {
            QMUIDialogMenuItemView itemView = mMenuItemViews.get(index);
            itemView.setChecked(!itemView.isChecked());
            mCheckedItems.set(index, itemView.isChecked());
        }

        /**
         * @return 被选中的菜单项的下标 <b>注意: 如果选中的是1，3项(以0开始)，因为 (2<<1) + (2<<3) = 20</b>
         */
        public BitSet getCheckedItemRecord() {
            return (BitSet) mCheckedItems.clone();
        }

        /**
         * @return 被选中的菜单项的下标数组。如果选中的是1，3项(以0开始)，则返回[1,3]
         */
        public int[] getCheckedItemIndexes() {
            ArrayList<Integer> array = new ArrayList<>();
            int length = mMenuItemViews.size();

            for (int i = 0; i < length; i++) {
                QMUIDialogMenuItemView itemView = mMenuItemViews.get(i);
                if (itemView.isChecked()) {
                    array.add(itemView.getMenuIndex());
                }
            }
            int[] output = new int[array.size()];
            for (int i = 0; i < array.size(); i++) {
                output[i] = array.get(i);
            }
            return output;
        }

        protected boolean existCheckedItem() {
            return !mCheckedItems.isEmpty();
        }
    }

    /**
     * 自定义对话框内容区域的 Builder
     */
    public static class CustomDialogBuilder extends QMUIDialogBuilder {

        private int mLayoutId;

        public CustomDialogBuilder(Context context) {
            super(context);
        }

        /**
         * 设置内容区域的 layoutResId
         */
        public CustomDialogBuilder setLayout(@LayoutRes int layoutResId) {
            mLayoutId = layoutResId;
            return this;
        }

        @Nullable
        @Override
        protected View onCreateContent(QMUIDialog dialog, QMUIDialogView parent, Context context) {
            return LayoutInflater.from(context).inflate(mLayoutId, parent, false);
        }
    }

    /**
     * 随键盘升降自动调整 Dialog 高度的 Builder
     */
    public static abstract class AutoResizeDialogBuilder extends QMUIDialogBuilder {

        protected ScrollView mScrollView;

        public AutoResizeDialogBuilder(Context context) {
            super(context);
            setCheckKeyboardOverlay(true);
        }

        @Nullable
        @Override
        protected View onCreateContent(@NonNull QMUIDialog dialog,@NonNull QMUIDialogView parent, @NonNull Context context) {
            mScrollView = wrapWithScroll(onBuildContent(dialog, context));
            return mScrollView;
        }

        public abstract View onBuildContent(@NonNull QMUIDialog dialog, @NonNull Context context);
    }
}
