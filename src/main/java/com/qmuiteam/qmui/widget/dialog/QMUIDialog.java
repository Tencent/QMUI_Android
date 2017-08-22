package com.qmuiteam.qmui.widget.dialog;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.text.InputType;
import android.text.method.TransformationMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.R;

import java.util.ArrayList;

/**
 * QMUIDialog 对话框一般由 {@link QMUIDialogBuilder} 及其子类创建, 不同的 Builder 可以创建不同类型的对话框,
 * 例如消息类型的对话框、菜单项对话框等等。
 *
 * @author cginechen
 * @date 2015-10-20
 * @see QMUIDialogBuilder
 */
public class QMUIDialog extends Dialog {

    public QMUIDialog(Context context) {
        this(context, R.style.QMUI_Dialog);
    }

    public QMUIDialog(Context context, int styleRes) {
        super(context, styleRes);
        init();
    }

    private void init() {
        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDialogWidth();
    }

    private void initDialogWidth() {
        Window window = getWindow();
        if (window == null) {
            return;
        }
        window.setDimAmount(0.6f); // 部分刷机会导致背景透明，这里保证一次
        WindowManager.LayoutParams wmlp = window.getAttributes();
        wmlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        window.setAttributes(wmlp);
    }

    /**
     * 消息类型的对话框 Builder。通过它可以生成一个带标题、文本消息、按钮的对话框。
     */
    public static class MessageDialogBuilder extends QMUIDialogBuilder<MessageDialogBuilder> {
        protected CharSequence mMessage;

        private TextView mTextView;

        public MessageDialogBuilder(Context context) {
            super(context);
            mTextView = new TextView(mContext);
            mTextView.setTextColor(QMUIResHelper.getAttrColor(mContext, R.attr.qmui_config_color_gray_4));
            mTextView.setLineSpacing(QMUIDisplayHelper.dpToPx(2), 1.0f);
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_content_message_text_size));
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
            return setMessage(mContext.getResources().getString(resId));
        }

        @Override
        protected void onCreateContent(QMUIDialog dialog, ViewGroup parent) {
            if (mMessage != null && mMessage.length() != 0) {

                mTextView.setText(mMessage);
                mTextView.setPadding(
                        QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_padding_horizontal),
                        QMUIResHelper.getAttrDimen(mContext, hasTitle() ? R.attr.qmui_dialog_content_padding_top : R.attr.qmui_dialog_content_padding_top_when_no_title),
                        QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_padding_horizontal),
                        QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_content_padding_bottom)
                );
                parent.addView(mTextView);
            }
        }

        public TextView getTextView() {
            return mTextView;
        }
    }

    /**
     * 带 CheckBox 的消息确认框 Builder
     */
    public static class CheckBoxMessageDialogBuilder extends QMUIDialogBuilder<CheckBoxMessageDialogBuilder> {

        protected String mMessage;
        private boolean mIsChecked = false;
        private Drawable mCheckMarkDrawable;
        private TextView mTextView;

        public CheckBoxMessageDialogBuilder(Context context) {
            super(context);
            mCheckMarkDrawable = QMUIResHelper.getAttrDrawable(context, R.attr.qmui_s_checkbox);

            mTextView = new TextView(mContext);
            mTextView.setTextColor(QMUIResHelper.getAttrColor(mContext, R.attr.qmui_config_color_gray_4));
            mTextView.setLineSpacing(QMUIDisplayHelper.dpToPx(2), 1.0f);
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_content_message_text_size));
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
            return setMessage(mContext.getResources().getString(resid));
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

        @Override
        protected void onCreateContent(QMUIDialog dialog, ViewGroup parent) {
            if (mMessage != null && mMessage.length() != 0) {

                mTextView.setText(mMessage);
                mTextView.setPadding(
                        QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_padding_horizontal),
                        hasTitle() ? QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_confirm_content_padding_top) : QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_content_padding_top_when_no_title),
                        QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_padding_horizontal),
                        QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_confirm_content_padding_bottom)
                );
                mCheckMarkDrawable.setBounds(0, 0, mCheckMarkDrawable.getIntrinsicWidth(), mCheckMarkDrawable.getIntrinsicHeight());
                mTextView.setCompoundDrawables(mCheckMarkDrawable, null, null, null);
                mTextView.setCompoundDrawablePadding(QMUIDisplayHelper.dpToPx(12));
                mTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setChecked(!mIsChecked);
                    }
                });
                mTextView.setSelected(mIsChecked);
                parent.addView(mTextView);
            }
        }

        public TextView getTextView() {
            return mTextView;
        }

    }

    /**
     * 带输入框的对话框 Builder
     */
    public static class EditTextDialogBuilder extends QMUIDialogBuilder<EditTextDialogBuilder> {
        protected String mPlaceholder;
        protected TransformationMethod mTransformationMethod;
        protected RelativeLayout mMainLayout;
        protected EditText mEditText;
        protected ImageView mRightImageView;
        private int mInputType = InputType.TYPE_CLASS_TEXT;

        public EditTextDialogBuilder(Context context) {
            super(context);
            mEditText = new EditText(mContext);
            mEditText.setHintTextColor(QMUIResHelper.getAttrColor(mContext, R.attr.qmui_config_color_gray_3));
            mEditText.setTextColor(QMUIResHelper.getAttrColor(mContext, R.attr.qmui_config_color_black));
            mEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_content_message_text_size));
            mEditText.setFocusable(true);
            mEditText.setFocusableInTouchMode(true);
            mEditText.setImeOptions(EditorInfo.IME_ACTION_GO);
            mEditText.setGravity(Gravity.CENTER_VERTICAL);
            mEditText.setId(R.id.qmui_dialog_edit_input);

            mRightImageView = new ImageView(mContext);
            mRightImageView.setId(R.id.qmui_dialog_edit_right_icon);
            mRightImageView.setVisibility(View.GONE);
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
            return setPlaceholder(mContext.getResources().getString(resId));
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

        @Override
        protected void onCreateContent(QMUIDialog dialog, ViewGroup parent) {
            mMainLayout = new RelativeLayout(mContext);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = QMUIResHelper.getAttrDimen(mContext, hasTitle() ? R.attr.qmui_dialog_edit_content_padding_top : R.attr.qmui_dialog_content_padding_top_when_no_title);
            lp.leftMargin = QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_padding_horizontal);
            lp.rightMargin = QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_padding_horizontal);
            lp.bottomMargin = QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_edit_content_padding_bottom);
            mMainLayout.setBackgroundResource(R.drawable.qmui_edittext_bg_border_bottom);
            mMainLayout.setLayoutParams(lp);

			if(mTransformationMethod!=null){
				mEditText.setTransformationMethod(mTransformationMethod);
			}else{
				mEditText.setInputType(mInputType);
			}

			mEditText.setBackgroundResource(0);
            mEditText.setPadding(0, 0, 0, QMUIDisplayHelper.dpToPx(5));
            RelativeLayout.LayoutParams editLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            editLp.addRule(RelativeLayout.LEFT_OF, mRightImageView.getId());
            editLp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            if (mPlaceholder != null) {
                mEditText.setHint(mPlaceholder);
            }
            mMainLayout.addView(mEditText, createEditTextLayoutParams());
            mMainLayout.addView(mRightImageView, createRightIconLayoutParams());

            parent.addView(mMainLayout);
        }

        protected RelativeLayout.LayoutParams createEditTextLayoutParams() {
            RelativeLayout.LayoutParams editLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            editLp.addRule(RelativeLayout.LEFT_OF, mRightImageView.getId());
            editLp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            return editLp;
        }

        protected RelativeLayout.LayoutParams createRightIconLayoutParams() {
            RelativeLayout.LayoutParams rightIconLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rightIconLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            rightIconLp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            rightIconLp.leftMargin = QMUIDisplayHelper.dpToPx(5);
            return rightIconLp;
        }

        @Override
        protected void onAfter(QMUIDialog dialog, LinearLayout parent) {
            super.onAfter(dialog, parent);
            dialog.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                }
            });
            mEditText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mEditText.requestFocus();
                    ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(mEditText, 0);
                }
            }, 300);
        }

        public EditText getEditText() {
            return mEditText;
        }

        public ImageView getRightImageView() {
            return mRightImageView;
        }
    }

    private static class MenuBaseDialogBuilder<T extends QMUIDialogBuilder> extends QMUIDialogBuilder<T> {
        protected ArrayList<QMUIDialogMenuItemView> mMenuItemViews;
        protected LinearLayout mMenuItemContainer;
        protected LinearLayout.LayoutParams mMenuItemLp;

        public MenuBaseDialogBuilder(Context context) {
            super(context);
            mMenuItemViews = new ArrayList<>();
            mMenuItemLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_content_list_item_height)
            );
            mMenuItemLp.gravity = Gravity.CENTER_VERTICAL;
        }

        public void clear() {
            mMenuItemViews.clear();
        }

        public T addItem(QMUIDialogMenuItemView itemView, final OnClickListener listener) {
            itemView.setMenuIndex(mMenuItemViews.size());
            itemView.setListener(new QMUIDialogMenuItemView.MenuItemViewListener() {
                @Override
                public void onClick(int index) {
                    onItemClick(index);
                    if (listener != null) {
                        listener.onClick(mDialog, index);
                    }
                }
            });
            mMenuItemViews.add(itemView);
            return (T) this;
        }

        protected void onItemClick(int index) {

        }

        @Override
        protected void onCreateContent(QMUIDialog dialog, ViewGroup parent) {
            mMenuItemContainer = new LinearLayout(mContext);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            mMenuItemContainer.setPadding(
                    0, QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_content_padding_top_when_list),
                    0, QMUIResHelper.getAttrDimen(mContext, mActions.size() > 0 ? R.attr.qmui_dialog_content_padding_bottom : R.attr.qmui_dialog_content_padding_bottom_when_no_action)
            );
            mMenuItemContainer.setLayoutParams(layoutParams);
            mMenuItemContainer.setOrientation(LinearLayout.VERTICAL);
            if (mMenuItemViews.size() == 1) {
                mMenuItemContainer.setPadding(
                        0,
                        hasTitle() ? QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_content_padding_top_when_list) : 0,
                        0,
                        mActions.size() > 0 ? QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_content_padding_bottom) : 0
                );
            }
            for (QMUIDialogMenuItemView itemView : mMenuItemViews) {
                mMenuItemContainer.addView(itemView, mMenuItemLp);
            }
            parent.addView(mMenuItemContainer);
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
         * 添加菜单项
         *
         * @param items    所有菜单项的文字
         * @param listener 菜单项的点击事件
         */
        public MenuDialogBuilder addItems(CharSequence[] items, OnClickListener listener) {
            for (int i = 0; i < items.length; i++) {
                addItem(new QMUIDialogMenuItemView.TextItemView(mContext, items[i]), listener);
            }
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

        @Override
        protected void onCreateContent(QMUIDialog dialog, ViewGroup parent) {
            super.onCreateContent(dialog, parent);
            if (mCheckedIndex > -1 && mCheckedIndex < mMenuItemViews.size()) {
                mMenuItemViews.get(mCheckedIndex).setChecked(true);
            }
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
            for (int i = 0; i < items.length; i++) {
                addItem(new QMUIDialogMenuItemView.MarkItemView(mContext, items[i]), listener);
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
        private int mCheckedItems;

        public MultiCheckableDialogBuilder(Context context) {
            super(context);
        }

        /**
         * 设置被选中的菜单项的下标
         *
         * @param checkedItems <b>注意: 该 int 参数的每一位标识菜单项的每一项是否被选中</b>
         *                     <p>如 20 表示选中下标为 1、3 的菜单项, 因为 (2<<1) + (2<<3) = 20</p>
         */
        public MultiCheckableDialogBuilder setCheckedItems(int checkedItems) {
            mCheckedItems = checkedItems;
            return this;
        }

        /**
         * 设置被选中的菜单项的下标
         *
         * @param checkedIndexes 被选中的菜单项的下标组成的数组,如 [1,3] 表示选中下标为 1、3 的菜单项
         */
        public MultiCheckableDialogBuilder setCheckedItems(int[] checkedIndexes) {
            int checkedItemRecord = 0;
            for (int i = 0; i < checkedIndexes.length; i++) {
                checkedItemRecord += 2 << (checkedIndexes[i]);
            }
            return setCheckedItems(checkedItemRecord);
        }

        /**
         * 添加菜单项
         *
         * @param items    所有菜单项的文字
         * @param listener 菜单项的点击事件,可以在点击事件里调用 {@link #setCheckedItems(int[])}} 来设置选中某些菜单项
         */
        public MultiCheckableDialogBuilder addItems(CharSequence[] items, OnClickListener listener) {
            for (int i = 0; i < items.length; i++) {
                addItem(new QMUIDialogMenuItemView.CheckItemView(mContext, true, items[i]), listener);
            }
            return this;
        }

        @Override
        protected void onCreateContent(QMUIDialog dialog, ViewGroup parent) {
            super.onCreateContent(dialog, parent);
            for (int i = 0; i < mMenuItemViews.size(); i++) {
                QMUIDialogMenuItemView itemView = mMenuItemViews.get(i);
                int v = 2 << i;
                itemView.setChecked((v & mCheckedItems) == v);
            }
        }

        @Override
        protected void onItemClick(int index) {
            QMUIDialogMenuItemView itemView = mMenuItemViews.get(index);
            itemView.setChecked(!itemView.isChecked());
        }

        /**
         * @return 被选中的菜单项的下标 <b>注意: 如果选中的是1，3项(以0开始)，因为 (2<<1) + (2<<3) = 20</b>
         */
        public int getCheckedItemRecord() {
            int output = 0;
            int length = mMenuItemViews.size();

            for (int i = 0; i < length; i++) {
                QMUIDialogMenuItemView itemView = mMenuItemViews.get(i);
                if (itemView.isChecked()) {
                    output += 2 << itemView.getMenuIndex();
                }
            }
            mCheckedItems = output;
            return output;
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
            if (getCheckedItemRecord() <= 0) {
                return true;
            }
            return false;
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

        @Override
        protected void onCreateContent(QMUIDialog dialog, ViewGroup parent) {
            parent.addView(LayoutInflater.from(mContext).inflate(mLayoutId, parent, false));
        }
    }

    /**
     * 随键盘升降自动调整 Dialog 高度的 Builder
     */
    public static abstract class AutoResizeDialogBuilder extends QMUIDialogBuilder {

        private ScrollView mScrollerView;

        private int mAnchorHeight = 0;
        private int mScreenHeight = 0;
        private int mScrollHeight = 0;

        public AutoResizeDialogBuilder(Context context) {
            super(context);
        }

        @Override
        protected void onCreateContent(QMUIDialog dialog, ViewGroup parent) {
            mScrollerView = new ScrollView(mContext);
            mScrollerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, onGetScrollHeight()));
            mScrollerView.addView(onBuildContent(dialog, mScrollerView));
            parent.addView(mScrollerView);
        }

        @Override
        protected void onAfter(QMUIDialog dialog, LinearLayout parent) {
            super.onAfter(dialog, parent);
            bindEvent();
        }

        public abstract View onBuildContent(QMUIDialog dialog, ScrollView parent);

        public int onGetScrollHeight() {
            return ScrollView.LayoutParams.WRAP_CONTENT;
        }

        private void bindEvent() {
            mAnchorTopView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                }
            });
            mAnchorBottomView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                }
            });
            mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    View mDecor = mDialog.getWindow().getDecorView();
                    Rect r = new Rect();
                    mDecor.getWindowVisibleDisplayFrame(r);
                    mScreenHeight = QMUIDisplayHelper.getScreenHeight(mContext);
                    int anchorShouldHeight = mScreenHeight - r.bottom;
                    if (anchorShouldHeight != mAnchorHeight) {
                        mAnchorHeight = anchorShouldHeight;
                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mAnchorBottomView.getLayoutParams();
                        lp.height = mAnchorHeight;
                        mAnchorBottomView.setLayoutParams(lp);
                        LinearLayout.LayoutParams slp = (LinearLayout.LayoutParams) mScrollerView.getLayoutParams();
                        if (onGetScrollHeight() == ViewGroup.LayoutParams.WRAP_CONTENT) {
                            mScrollHeight = Math.max(mScrollHeight, mScrollerView.getMeasuredHeight());
                        } else {
                            mScrollHeight = onGetScrollHeight();
                        }
                        if (mAnchorHeight == 0) {
                            slp.height = mScrollHeight;
                        } else {
                            mScrollerView.getChildAt(0).requestFocus();
                            slp.height = mScrollHeight - mAnchorHeight;
                        }
                        mScrollerView.setLayoutParams(slp);
                    } else {
                        //如果内容过高,anchorShouldHeight=0,但实际下半部分会被截断,因此需要保护
                        //由于高度超过后,actionContainer并不会去测量和布局,所以这里拿不到action的高度,因此用比例估算一个值
                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mDialogWrapper.getLayoutParams();
                        int dialogLayoutMaxHeight = mScreenHeight - lp.bottomMargin - lp.topMargin - r.top;
                        int scrollLayoutHeight = mScrollerView.getMeasuredHeight();
                        if (scrollLayoutHeight > dialogLayoutMaxHeight * 0.8) {
                            mScrollHeight = (int) (dialogLayoutMaxHeight * 0.8);
                            LinearLayout.LayoutParams slp = (LinearLayout.LayoutParams) mScrollerView.getLayoutParams();
                            slp.height = mScrollHeight;
                            mScrollerView.setLayoutParams(slp);
                        }
                    }
                }
            });
        }
    }
}
