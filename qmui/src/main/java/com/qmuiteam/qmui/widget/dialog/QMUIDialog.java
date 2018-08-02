package com.qmuiteam.qmui.widget.dialog;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.text.InputType;
import android.text.method.TransformationMethod;
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

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.QMUIWrapContentScrollView;
import com.qmuiteam.qmui.widget.textview.QMUISpanTouchFixTextView;

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
    boolean mCancelable = true;
    private boolean mCanceledOnTouchOutside = true;
    private boolean mCanceledOnTouchOutsideSet;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDialog();
    }

    private void initDialog() {
        Window window = getWindow();
        if (window == null) {
            return;
        }
        WindowManager.LayoutParams wmlp = window.getAttributes();
        wmlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wmlp.gravity = Gravity.CENTER;
        window.setAttributes(wmlp);
    }

    @Override
    public void setCancelable(boolean cancelable) {
        super.setCancelable(cancelable);
        mCancelable = cancelable;
    }

    @Override
    public void setCanceledOnTouchOutside(boolean cancel) {
        super.setCanceledOnTouchOutside(cancel);
        if (cancel && !mCancelable) {
            mCancelable = true;
        }
        mCanceledOnTouchOutside = cancel;
        mCanceledOnTouchOutsideSet = true;
    }

    boolean shouldWindowCloseOnTouchOutside() {
        if (!mCanceledOnTouchOutsideSet) {
            if (Build.VERSION.SDK_INT < 11) {
                mCanceledOnTouchOutside = true;
            } else {
                TypedArray a = getContext().obtainStyledAttributes(
                        new int[]{android.R.attr.windowCloseOnTouchOutside});
                mCanceledOnTouchOutside = a.getBoolean(0, true);
                a.recycle();
            }
            mCanceledOnTouchOutsideSet = true;
        }
        return mCanceledOnTouchOutside;
    }

    void cancelOutSide() {
        if (mCancelable && isShowing() && shouldWindowCloseOnTouchOutside()) {
            cancel();
        }
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
                (activitySystemUi & View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) == View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) {
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

    @Override
    public void show() {
        super.show();
    }

    /**
     * 消息类型的对话框 Builder。通过它可以生成一个带标题、文本消息、按钮的对话框。
     */
    public static class MessageDialogBuilder extends QMUIDialogBuilder<MessageDialogBuilder> {
        protected CharSequence mMessage;
        private QMUIWrapContentScrollView mScrollContainer;
        private QMUISpanTouchFixTextView mTextView;

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

        @Override
        protected void onCreateContent(QMUIDialog dialog, ViewGroup parent, Context context) {
            if (mMessage != null && mMessage.length() != 0) {
                mTextView = new QMUISpanTouchFixTextView(context);
                assignMessageTvWithAttr(mTextView, hasTitle(), R.attr.qmui_dialog_message_content_style);
                mTextView.setText(mMessage);
                mTextView.setMovementMethodDefault();


                mScrollContainer = new QMUIWrapContentScrollView(context);
                mScrollContainer.setMaxHeight(getContentAreaMaxHeight());
                mScrollContainer.setVerticalScrollBarEnabled(false);
                mScrollContainer.addView(mTextView);
                parent.addView(mScrollContainer);
            }
        }

        @Override
        protected void onConfigTitleView(TextView titleView) {
            super.onConfigTitleView(titleView);
            if (mMessage == null || mMessage.length() == 0) {
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

        public QMUISpanTouchFixTextView getTextView() {
            return mTextView;
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

        private QMUIWrapContentScrollView mScrollContainer;
        protected String mMessage;
        private boolean mIsChecked = false;
        private Drawable mCheckMarkDrawable;
        private QMUISpanTouchFixTextView mTextView;

        public CheckBoxMessageDialogBuilder(Context context) {
            super(context);
            mCheckMarkDrawable = QMUIResHelper.getAttrDrawable(context, R.attr.qmui_s_checkbox);
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

        @Override
        protected void onCreateContent(QMUIDialog dialog, ViewGroup parent, Context context) {
            if (mMessage != null && mMessage.length() != 0) {
                mScrollContainer = new QMUIWrapContentScrollView(context);
                mTextView = new QMUISpanTouchFixTextView(context);
                mTextView.setMovementMethodDefault();
                MessageDialogBuilder.assignMessageTvWithAttr(mTextView, hasTitle(), R.attr.qmui_dialog_message_content_style);
                ScrollView.LayoutParams lp = new ScrollView.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.gravity = mTextView.getGravity();
                mScrollContainer.addView(mTextView, lp);
                mScrollContainer.setVerticalScrollBarEnabled(false);
                mScrollContainer.setMaxHeight(getContentAreaMaxHeight());
                mTextView.setText(mMessage);
                mCheckMarkDrawable.setBounds(0, 0, mCheckMarkDrawable.getIntrinsicWidth(), mCheckMarkDrawable.getIntrinsicHeight());
                mTextView.setCompoundDrawables(mCheckMarkDrawable, null, null, null);
                mTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setChecked(!mIsChecked);
                    }
                });
                mTextView.setSelected(mIsChecked);
                parent.addView(mScrollContainer);
            }
        }

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
        protected RelativeLayout mMainLayout;
        protected EditText mEditText;
        protected ImageView mRightImageView;
        private int mInputType = InputType.TYPE_CLASS_TEXT;
        private CharSequence mDefaultText = null;

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

        @Override
        protected void onCreateContent(QMUIDialog dialog, ViewGroup parent, Context context) {
            mEditText = new EditText(context);
            MessageDialogBuilder.assignMessageTvWithAttr(mEditText, hasTitle(), R.attr.qmui_dialog_edit_content_style);
            mEditText.setFocusable(true);
            mEditText.setFocusableInTouchMode(true);
            mEditText.setImeOptions(EditorInfo.IME_ACTION_GO);
            mEditText.setId(R.id.qmui_dialog_edit_input);

            if (!QMUILangHelper.isNullOrEmpty(mDefaultText)) {
                mEditText.setText(mDefaultText);
            }

            mRightImageView = new ImageView(context);
            mRightImageView.setId(R.id.qmui_dialog_edit_right_icon);
            mRightImageView.setVisibility(View.GONE);

            mMainLayout = new RelativeLayout(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = mEditText.getPaddingTop();
            lp.leftMargin = mEditText.getPaddingLeft();
            lp.rightMargin = mEditText.getPaddingRight();
            lp.bottomMargin = mEditText.getPaddingBottom();
            mMainLayout.setBackgroundResource(R.drawable.qmui_edittext_bg_border_bottom);
            mMainLayout.setLayoutParams(lp);

            if (mTransformationMethod != null) {
                mEditText.setTransformationMethod(mTransformationMethod);
            } else {
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
        protected void onAfter(QMUIDialog dialog, LinearLayout parent, Context context) {
            super.onAfter(dialog, parent, context);
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

        public EditText getEditText() {
            return mEditText;
        }

        public ImageView getRightImageView() {
            return mRightImageView;
        }
    }


    public static class MenuBaseDialogBuilder<T extends QMUIDialogBuilder> extends QMUIDialogBuilder<T> {
        protected ArrayList<ItemViewFactory> mMenuItemViewsFactoryList;
        protected LinearLayout mMenuItemContainer;
        protected QMUIWrapContentScrollView mContentScrollView;
        protected LinearLayout.LayoutParams mMenuItemLp;
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

        @Override
        protected void onCreateContent(QMUIDialog dialog, ViewGroup parent, Context context) {

            mMenuItemContainer = new LinearLayout(context);
            mMenuItemContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            mMenuItemContainer.setLayoutParams(layoutParams);

            TypedArray a = context.obtainStyledAttributes(null, R.styleable.QMUIDialogMenuContainerStyleDef, R.attr.qmui_dialog_menu_container_style, 0);
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

            mMenuItemLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight);
            mMenuItemLp.gravity = Gravity.CENTER_VERTICAL;

            if (mMenuItemViewsFactoryList.size() == 1) {
                paddingBottom = paddingTop = paddingVerWhenSingle;
            }

            if (hasTitle()) {
                paddingTop = paddingTopWhenTitle;
            }

            if (mActions.size() > 0) {
                paddingBottom = paddingBottomWhenAction;
            }

            mMenuItemContainer.setPadding(0, paddingTop, 0, paddingBottom);


            mMenuItemViews.clear();
            for (ItemViewFactory factory : mMenuItemViewsFactoryList) {
                QMUIDialogMenuItemView itemView = factory.createItemView(context);
                mMenuItemContainer.addView(itemView, mMenuItemLp);
                mMenuItemViews.add(itemView);
            }


            mContentScrollView = new QMUIWrapContentScrollView(context);
            mContentScrollView.setMaxHeight(getContentAreaMaxHeight());
            mContentScrollView.addView(mMenuItemContainer);
            mContentScrollView.setVerticalScrollBarEnabled(false);
            parent.addView(mContentScrollView);
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

        @Override
        protected void onCreateContent(QMUIDialog dialog, ViewGroup parent, Context context) {
            super.onCreateContent(dialog, parent, context);
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
            if (checkedIndexes != null && checkedIndexes.length > 0) {
                for (int checkedIndexe : checkedIndexes) {
                    checkedItemRecord += 2 << (checkedIndexe);
                }
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

        @Override
        protected void onCreateContent(QMUIDialog dialog, ViewGroup parent, Context context) {
            super.onCreateContent(dialog, parent, context);
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
            return getCheckedItemRecord() <= 0;
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
        protected void onCreateContent(QMUIDialog dialog, ViewGroup parent, Context context) {
            parent.addView(LayoutInflater.from(context).inflate(mLayoutId, parent, false));
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
        protected void onCreateContent(QMUIDialog dialog, ViewGroup parent, Context context) {
            mScrollerView = new ScrollView(context);
            mScrollerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, onGetScrollHeight()));
            mScrollerView.addView(onBuildContent(dialog, mScrollerView));
            parent.addView(mScrollerView);
        }

        @Override
        protected void onAfter(QMUIDialog dialog, LinearLayout parent, Context context) {
            super.onAfter(dialog, parent, context);
            bindEvent(context);
        }

        public abstract View onBuildContent(QMUIDialog dialog, ScrollView parent);

        public int onGetScrollHeight() {
            return ScrollView.LayoutParams.WRAP_CONTENT;
        }

        private void bindEvent(final Context context) {
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
                    //noinspection ConstantConditions
                    View mDecor = mDialog.getWindow().getDecorView();
                    Rect r = new Rect();
                    mDecor.getWindowVisibleDisplayFrame(r);
                    mScreenHeight = QMUIDisplayHelper.getScreenHeight(context);
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
                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mDialogView.getLayoutParams();
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
