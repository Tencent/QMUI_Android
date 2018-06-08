package com.qmuiteam.qmui.widget.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.Space;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.layout.QMUIButton;
import com.qmuiteam.qmui.layout.QMUILinearLayout;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建 {@link QMUIDialog} 的 Builder 基类, 不同的 Builder 子类拥有创建不同类型对话框的能力, 具体见子类。
 * <p>该类产生的 Dialog 分为上中下三个部分:</p>
 * <ul>
 * <li>上部分是 title 区域, 支持显示纯文本标题, 通过 {@link #setTitle(int)} 系列方法设置。
 * 子类也可以通过 override {@link #onCreateTitle(QMUIDialog, ViewGroup, Context)} 方法自定义</li>
 * <li>中间部分的内容由各个子类决定, 子类通过 override {@link #onCreateContent(QMUIDialog, ViewGroup, Context)} 方法自定义。</li>
 * <li>下部分是操作区域, 支持添加操作按钮, 通过 {@link #addAction(int, int, QMUIDialogAction.ActionListener)} 系列方法添加。
 * 子类也可以通过 override {@link #onCreateHandlerBar(QMUIDialog, ViewGroup, Context)} 方法自定义。
 * 其中操作按钮有内联和块级之分, 也有普通、正向、反向之分, 具体见 {@link QMUIDialogAction}
 * </li>
 * </ul>
 *
 * @author cginechen
 * @date 2015-10-20
 */
public abstract class QMUIDialogBuilder<T extends QMUIDialogBuilder> {

    @IntDef({HORIZONTAL, VERTICAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Orientation {
    }

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    /**
     * A global theme provider, use to distinguish theme from different builder type
     */
    private static OnProvideDefaultTheme sOnProvideDefaultTheme = null;

    public static void setOnProvideDefaultTheme(OnProvideDefaultTheme onProvideDefaultTheme) {
        QMUIDialogBuilder.sOnProvideDefaultTheme = onProvideDefaultTheme;
    }

    private Context mContext;
    protected QMUIDialog mDialog;
    protected String mTitle;
    private boolean mCancelable = true;
    private boolean mCanceledOnTouchOutside = true;

    protected LinearLayout mRootView;
    protected QMUIDialogView mDialogView;
    protected View mAnchorTopView;
    protected View mAnchorBottomView;
    protected List<QMUIDialogAction> mActions = new ArrayList<>();
    private QMUIDialogView.OnDecorationListener mOnDecorationListener;

    protected TextView mTitleView;
    protected QMUILinearLayout mActionContainer;
    private int mContentAreaMaxHeight = -1;

    @Orientation private int mActionContainerOrientation = HORIZONTAL;
    private boolean mChangeAlphaForPressOrDisable = true;
    private int mActionDividerThickness = 0;
    private int mActionDividerColorRes = R.color.qmui_config_color_separator;
    private int mActionDividerInsetStart = 0;
    private int mActionDividerInsetEnd = 0;

    public QMUIDialogBuilder(Context context) {
        this.mContext = context;
    }

    protected int getContentAreaMaxHeight() {
        if (mContentAreaMaxHeight == -1) {
            // 屏幕高度的0.85 - 预估的 title 和 action 高度
            return (int) (QMUIDisplayHelper.getScreenHeight(mContext) * 0.85) - QMUIDisplayHelper.dp2px(mContext, 100);
        }
        return mContentAreaMaxHeight;
    }

    public Context getBaseContext() {
        return mContext;
    }

    /**
     * 设置内容区域最高的高度
     *
     * @param contentAreaMaxHeight
     */
    public T setContentAreaMaxHeight(int contentAreaMaxHeight) {
        mContentAreaMaxHeight = contentAreaMaxHeight;
        return (T) this;
    }

    /**
     * 设置对话框顶部的标题文字
     */
    @SuppressWarnings("unchecked")
    public T setTitle(String title) {
        if (title != null && title.length() > 0) {
            this.mTitle = title + mContext.getString(R.string.qmui_tool_fixellipsize);
        }
        return (T) this;
    }

    /**
     * 设置对话框顶部的标题文字
     */
    public T setTitle(int resId) {
        return setTitle(mContext.getResources().getString(resId));
    }

    @SuppressWarnings("unchecked")
    public T setCancelable(boolean cancelable) {
        mCancelable = cancelable;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
        mCanceledOnTouchOutside = canceledOnTouchOutside;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setOnDecorationListener(QMUIDialogView.OnDecorationListener onDecorationListener) {
        mOnDecorationListener = onDecorationListener;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setActionContainerOrientation(int actionContainerOrientation) {
        mActionContainerOrientation = actionContainerOrientation;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setChangeAlphaForPressOrDisable(boolean changeAlphaForPressOrDisable) {
        mChangeAlphaForPressOrDisable = changeAlphaForPressOrDisable;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setActionDivider(int thickness, int colorRes, int startInset, int endInset) {
        mActionDividerThickness = thickness;
        mActionDividerColorRes = colorRes;
        mActionDividerInsetStart = startInset;
        mActionDividerInsetEnd = endInset;
        return (T) this;
    }


    //region 添加action

    /**
     * 添加对话框底部的操作按钮
     */
    @SuppressWarnings("unchecked")
    public T addAction(@Nullable QMUIDialogAction action) {
        if (action != null) {
            mActions.add(action);
        }

        return (T) this;
    }

    /**
     * 添加无图标正常类型的操作按钮
     *
     * @param strResId 文案
     * @param listener 点击回调事件
     */
    public T addAction(int strResId, QMUIDialogAction.ActionListener listener) {
        return addAction(0, strResId, listener);
    }

    /**
     * 添加无图标正常类型的操作按钮
     *
     * @param str      文案
     * @param listener 点击回调事件
     */
    public T addAction(CharSequence str, QMUIDialogAction.ActionListener listener) {
        return addAction(0, str, QMUIDialogAction.ACTION_PROP_NEUTRAL, listener);
    }


    /**
     * 添加普通类型的操作按钮
     *
     * @param iconResId 图标
     * @param strResId  文案
     * @param listener  点击回调事件
     */
    public T addAction(int iconResId, int strResId, QMUIDialogAction.ActionListener listener) {
        return addAction(iconResId, strResId, QMUIDialogAction.ACTION_PROP_NEUTRAL, listener);
    }

    /**
     * 添加普通类型的操作按钮
     *
     * @param iconResId 图标
     * @param str       文案
     * @param listener  点击回调事件
     */
    public T addAction(int iconResId, CharSequence str, QMUIDialogAction.ActionListener listener) {
        return addAction(iconResId, str, QMUIDialogAction.ACTION_PROP_NEUTRAL, listener);
    }


    /**
     * 添加操作按钮
     *
     * @param iconRes  图标
     * @param strRes   文案
     * @param prop     属性
     * @param listener 点击回调事件
     */
    public T addAction(int iconRes, int strRes, @QMUIDialogAction.Prop int prop, QMUIDialogAction.ActionListener listener) {
        return addAction(iconRes, mContext.getResources().getString(strRes), prop, listener);
    }

    /**
     * 添加操作按钮
     *
     * @param iconRes  图标
     * @param str      文案
     * @param prop     属性
     * @param listener 点击回调事件
     */
    @SuppressWarnings("unchecked")
    public T addAction(int iconRes, CharSequence str, @QMUIDialogAction.Prop int prop, QMUIDialogAction.ActionListener listener) {
        QMUIDialogAction action = new QMUIDialogAction(mContext, iconRes, str, prop, listener);
        mActions.add(action);
        return (T) this;
    }


    //endregion

    /**
     * 判断对话框是否需要显示title
     *
     * @return 是否有title
     */
    protected boolean hasTitle() {
        return mTitle != null && mTitle.length() != 0;
    }

    /**
     * 产生一个 Dialog 并显示出来
     */
    public QMUIDialog show() {
        final QMUIDialog dialog = create();
        dialog.show();
        return dialog;
    }

    /**
     * 只产生一个 Dialog, 不显示出来
     *
     * @see #create(int)
     */
    public QMUIDialog create() {
        if (sOnProvideDefaultTheme != null) {
            int theme = sOnProvideDefaultTheme.getThemeForBuilder(this);
            if (theme > 0) {
                return create(theme);
            }
        }
        return create(R.style.QMUI_Dialog);
    }

    /**
     * 产生一个Dialog，但不显示出来。
     *
     * @param style Dialog 的样式
     * @see #create()
     */
    @SuppressLint("InflateParams")
    public QMUIDialog create(@StyleRes int style) {
        mDialog = new QMUIDialog(mContext, style);
        Context dialogContext = mDialog.getContext();

        mRootView = (LinearLayout) LayoutInflater.from(dialogContext).inflate(
                R.layout.qmui_dialog_layout, null);
        mDialogView = (QMUIDialogView) mRootView.findViewById(R.id.dialog);
        mDialogView.setOnDecorationListener(mOnDecorationListener);
        mAnchorTopView = mRootView.findViewById(R.id.anchor_top);
        mAnchorBottomView = mRootView.findViewById(R.id.anchor_bottom);
        // title
        onCreateTitle(mDialog, mDialogView, dialogContext);

        //content
        onCreateContent(mDialog, mDialogView, dialogContext);

        // 操作
        onCreateHandlerBar(mDialog, mDialogView, dialogContext);


        mDialog.addContentView(mRootView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mDialog.setCancelable(mCancelable);
        mDialog.setCanceledOnTouchOutside(mCanceledOnTouchOutside);
        onAfter(mDialog, mRootView, dialogContext);
        return mDialog;
    }

    /**
     * 创建顶部的标题区域
     */
    protected void onCreateTitle(QMUIDialog dialog, ViewGroup parent, Context context) {
        if (hasTitle()) {
            mTitleView = new TextView(context);

            mTitleView.setText(mTitle);
            QMUIResHelper.assignTextViewWithAttr(mTitleView, R.attr.qmui_dialog_title_style);
            onConfigTitleView(mTitleView);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mTitleView.setLayoutParams(lp);
            parent.addView(mTitleView);
        }
    }

    protected void onConfigTitleView(TextView titleView) {

    }

    public TextView getTitleView() {
        return mTitleView;
    }

    /**
     * 创建中间的区域
     */
    protected abstract void onCreateContent(QMUIDialog dialog, ViewGroup parent, Context context);

    /**
     * 创建底部的操作栏区域
     */
    protected void onCreateHandlerBar(final QMUIDialog dialog, ViewGroup parent, Context context) {
        int size = mActions.size();
        if (size > 0) {
            TypedArray a = context.obtainStyledAttributes(null, R.styleable.QMUIDialogActionContainerCustomDef, R.attr.qmui_dialog_action_container_style, 0);
            int count = a.getIndexCount();
            int justifyContent = 1, spaceCustomIndex = 0;
            int actionHeight = -1, actionSpace = 0;
            for (int i = 0; i < count; i++) {
                int attr = a.getIndex(i);
                if (attr == R.styleable.QMUIDialogActionContainerCustomDef_qmui_dialog_action_container_justify_content) {
                    justifyContent = a.getInteger(attr, justifyContent);
                } else if (attr == R.styleable.QMUIDialogActionContainerCustomDef_qmui_dialog_action_container_custom_space_index) {
                    spaceCustomIndex = a.getInteger(attr, 0);
                } else if (attr == R.styleable.QMUIDialogActionContainerCustomDef_qmui_dialog_action_space) {
                    actionSpace = a.getDimensionPixelSize(attr, 0);
                } else if (attr == R.styleable.QMUIDialogActionContainerCustomDef_qmui_dialog_action_height) {
                    actionHeight = a.getDimensionPixelSize(attr, 0);
                }
            }
            a.recycle();
            int spaceInsertPos = -1;
            if (mActionContainerOrientation == VERTICAL) {
                spaceInsertPos = -1;
            } else if (justifyContent == 0) {
                spaceInsertPos = size;
            } else if (justifyContent == 1) {
                spaceInsertPos = 0;
            } else if (justifyContent == 3) {
                spaceInsertPos = spaceCustomIndex;
            }


            mActionContainer = new QMUILinearLayout(context, null, R.attr.qmui_dialog_action_container_style);
            mActionContainer.setOrientation(mActionContainerOrientation == VERTICAL ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
            mActionContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            for (int i = 0; i < size; i++) {
                if (spaceInsertPos == i) {
                    mActionContainer.addView(createActionContainerSpace(context));
                }
                QMUIDialogAction action = mActions.get(i);

                LinearLayout.LayoutParams actionLp;
                if (mActionContainerOrientation == VERTICAL) {
                    actionLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, actionHeight);
                } else {
                    actionLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, actionHeight);
                    if (spaceInsertPos >= 0) {
                        if (i >= spaceInsertPos) {
                            actionLp.leftMargin = actionSpace;
                        } else {
                            actionLp.rightMargin = actionSpace;
                        }
                    }
                    if (justifyContent == 2) {
                        actionLp.weight = 1;
                    }
                }
                QMUIButton actionView = action.buildActionView(mDialog, i);

                // add divider
                if (mActionDividerThickness > 0 && i > 0 && spaceInsertPos != i) {
                    if (mActionContainerOrientation == VERTICAL) {
                        actionView.onlyShowTopDivider(mActionDividerInsetStart, mActionDividerInsetEnd,
                                mActionDividerThickness, ContextCompat.getColor(context, mActionDividerColorRes));
                    } else {
                        actionView.onlyShowLeftDivider(mActionDividerInsetStart, mActionDividerInsetEnd,
                                mActionDividerThickness, ContextCompat.getColor(context, mActionDividerColorRes));
                    }

                }

                actionView.setChangeAlphaWhenDisable(mChangeAlphaForPressOrDisable);
                actionView.setChangeAlphaWhenPress(mChangeAlphaForPressOrDisable);
                mActionContainer.addView(actionView, actionLp);
            }

            if (spaceInsertPos == size) {
                mActionContainer.addView(createActionContainerSpace(context));
            }

            if (mActionContainerOrientation == HORIZONTAL) {
                mActionContainer.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        int width = right - left;
                        int childCount = mActionContainer.getChildCount();
                        if (childCount > 0) {
                            View lastChild = mActionContainer.getChildAt(childCount - 1);
                            // 如果ActionButton的宽度过宽，则减小padding
                            if (lastChild.getRight() > width) {
                                int childPaddingHor = Math.max(0, lastChild.getPaddingLeft() - QMUIDisplayHelper.dp2px(mContext, 3));
                                for (int i = 0; i < childCount; i++) {
                                    mActionContainer.getChildAt(i).setPadding(childPaddingHor, 0, childPaddingHor, 0);
                                }
                            }
                        }

                    }
                });
            }
            parent.addView(mActionContainer);

        }
    }

    private View createActionContainerSpace(Context context) {
        Space space = new Space(context);
        LinearLayout.LayoutParams spaceLp = new LinearLayout.LayoutParams(0, 0);
        spaceLp.weight = 1;
        space.setLayoutParams(spaceLp);
        return space;
    }

    protected void onAfter(QMUIDialog dialog, LinearLayout parent, Context context) {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.cancelOutSide();
            }
        };
        mAnchorBottomView.setOnClickListener(listener);
        mAnchorTopView.setOnClickListener(listener);
        mRootView.setOnClickListener(listener);
    }

    public View getAnchorTopView() {
        return mAnchorTopView;
    }

    public View getAnchorBottomView() {
        return mAnchorBottomView;
    }

    public List<QMUIDialogAction> getPositiveAction() {
        List<QMUIDialogAction> output = new ArrayList<>();
        for (QMUIDialogAction action : mActions) {
            if (action.getActionProp() == QMUIDialogAction.ACTION_PROP_POSITIVE) {
                output.add(action);
            }
        }
        return output;
    }

    public interface OnProvideDefaultTheme {
        int getThemeForBuilder(QMUIDialogBuilder builder);
    }
}
