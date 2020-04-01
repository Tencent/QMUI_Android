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
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.layout.QMUIButton;
import com.qmuiteam.qmui.layout.QMUILinearLayout;
import com.qmuiteam.qmui.skin.QMUISkinHelper;
import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.QMUIWrapContentScrollView;
import com.qmuiteam.qmui.widget.textview.QMUISpanTouchFixTextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * 创建 {@link QMUIDialog} 的 Builder 基类, 不同的 Builder 子类拥有创建不同类型对话框的能力, 具体见子类。
 * <p>该类产生的 Dialog 分为上中下三个部分:</p>
 * <ul>
 * <li>上部分是 title 区域, 支持显示纯文本标题, 通过 {@link #setTitle(int)} 系列方法设置。
 * 子类也可以通过 override {@link #onCreateTitle(QMUIDialog, QMUIDialogView, Context)} 方法自定义</li>
 * <li>中间部分的内容由各个子类决定, 子类通过 override {@link #onCreateContent(QMUIDialog, QMUIDialogView, Context)} 方法自定义。</li>
 * <li>下部分是操作区域, 支持添加操作按钮, 通过 {@link #addAction(int, int, QMUIDialogAction.ActionListener)} 系列方法添加。
 * 子类也可以通过 override {@link #onCreateOperatorLayout(QMUIDialog, QMUIDialogView, Context)} 方法自定义。
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

    protected QMUIDialogRootLayout mRootView;
    protected QMUIDialogView mDialogView;
    protected List<QMUIDialogAction> mActions = new ArrayList<>();
    private QMUIDialogView.OnDecorationListener mOnDecorationListener;

    @Orientation private int mActionContainerOrientation = HORIZONTAL;
    private boolean mChangeAlphaForPressOrDisable = true;
    private int mActionDividerThickness = 0;
    private int mActionDividerColorAttr = R.attr.qmui_skin_support_dialog_action_divider_color;
    private int mActionDividerInsetStart = 0;
    private int mActionDividerInsetEnd = 0;
    private int mActionDividerColor = 0;
    private boolean mCheckKeyboardOverlay = false;
    private QMUISkinManager mSkinManager;
    private float mMaxPercent = 0.75f;

    public QMUIDialogBuilder(Context context) {
        this.mContext = context;
    }

    public Context getBaseContext() {
        return mContext;
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
    public T setActionDivider(int thickness, int colorAttr, int startInset, int endInset) {
        mActionDividerThickness = thickness;
        mActionDividerColorAttr = colorAttr;
        mActionDividerInsetStart = startInset;
        mActionDividerInsetEnd = endInset;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setActionDividerInsetAndThickness(int thickness, int startInset, int endInset){
        mActionDividerThickness = thickness;
        mActionDividerInsetStart = startInset;
        mActionDividerInsetEnd = endInset;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setActionDividerColorAttr(int colorAttr){
        mActionDividerColorAttr = colorAttr;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setActionDividerColor(int color){
        mActionDividerColor = color;
        mActionDividerColorAttr = 0;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setCheckKeyboardOverlay(boolean checkKeyboardOverlay) {
        mCheckKeyboardOverlay = checkKeyboardOverlay;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setSkinManager(@Nullable QMUISkinManager skinManager) {
        mSkinManager = skinManager;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setMaxPercent(float maxPercent) {
        mMaxPercent = maxPercent;
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
        QMUIDialogAction action = new QMUIDialogAction(str)
                .iconRes(iconRes)
                .prop(prop)
                .onClick(listener);
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

        mDialogView = onCreateDialogView(dialogContext);
        mRootView = new QMUIDialogRootLayout(dialogContext, mDialogView, onCreateDialogLayoutParams());
        mRootView.setCheckKeyboardOverlay(mCheckKeyboardOverlay);
        mRootView.setOverlayOccurInMeasureCallback(new QMUIDialogRootLayout.OverlayOccurInMeasureCallback() {
            @Override
            public void call() {
                onOverlayOccurredInMeasure();
            }
        });
        mRootView.setMaxPercent(mMaxPercent);
        configRootLayout(mRootView);
        mDialogView = mRootView.getDialogView();
        mDialogView.setOnDecorationListener(mOnDecorationListener);
        // title
        View titleView = onCreateTitle(mDialog, mDialogView, dialogContext);
        View operatorLayout = onCreateOperatorLayout(mDialog, mDialogView, dialogContext);
        View contentLayout = onCreateContent(mDialog, mDialogView, dialogContext);
        checkAndSetId(titleView, R.id.qmui_dialog_title_id);
        checkAndSetId(operatorLayout, R.id.qmui_dialog_operator_layout_id);
        checkAndSetId(contentLayout, R.id.qmui_dialog_content_id);

        // chain
        if (titleView != null) {
            ConstraintLayout.LayoutParams lp = onCreateTitleLayoutParams(dialogContext);
            if (contentLayout != null) {
                lp.bottomToTop = contentLayout.getId();
            } else if (operatorLayout != null) {
                lp.bottomToTop = operatorLayout.getId();
            } else {
                lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            }
            mDialogView.addView(titleView, lp);
        }

        if (contentLayout != null) {
            ConstraintLayout.LayoutParams lp = onCreateContentLayoutParams(dialogContext);
            if (titleView != null) {
                lp.topToBottom = titleView.getId();
            } else {
                lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            }

            if (operatorLayout != null) {
                lp.bottomToTop = operatorLayout.getId();
            } else {
                lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            }
            mDialogView.addView(contentLayout, lp);
        }

        if (operatorLayout != null) {
            ConstraintLayout.LayoutParams lp = onCreateOperatorLayoutLayoutParams(dialogContext);
            if (contentLayout != null) {
                lp.topToBottom = contentLayout.getId();
            } else if (titleView != null) {
                lp.topToBottom = titleView.getId();
            } else {
                lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            }
            mDialogView.addView(operatorLayout, lp);
        }

        mDialog.addContentView(mRootView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mDialog.setCancelable(mCancelable);
        mDialog.setCanceledOnTouchOutside(mCanceledOnTouchOutside);
        mDialog.setSkinManager(mSkinManager);
        onAfterCreate(mDialog, mRootView, dialogContext);
        return mDialog;
    }

    protected void onAfterCreate(@NonNull QMUIDialog dialog, @NonNull QMUIDialogRootLayout rootLayout, @NonNull Context context){

    }

    protected void onOverlayOccurredInMeasure(){

    }

    private void checkAndSetId(@Nullable View view, int id) {
        if (view != null && view.getId() == View.NO_ID) {
            view.setId(id);
        }
    }

    protected void configRootLayout(@NonNull QMUIDialogRootLayout rootLayout){

    }

    protected void skinConfigDialogView(QMUIDialogView dialogView){
        QMUISkinValueBuilder valueBuilder = QMUISkinValueBuilder.acquire();
        valueBuilder.background(R.attr.qmui_skin_support_dialog_bg);
        QMUISkinHelper.setSkinValue(dialogView, valueBuilder);
        QMUISkinValueBuilder.release(valueBuilder);
    }
    protected void skinConfigTitleView(TextView titleView){
        QMUISkinValueBuilder valueBuilder = QMUISkinValueBuilder.acquire();
        valueBuilder.textColor(R.attr.qmui_skin_support_dialog_title_text_color);
        QMUISkinHelper.setSkinValue(titleView, valueBuilder);
        QMUISkinValueBuilder.release(valueBuilder);
    }
    protected void skinConfigActionContainer(ViewGroup actionContainer){
        QMUISkinValueBuilder valueBuilder = QMUISkinValueBuilder.acquire();
        valueBuilder.topSeparator(R.attr.qmui_skin_support_dialog_action_container_separator_color);
        QMUISkinHelper.setSkinValue(actionContainer, valueBuilder);
        QMUISkinValueBuilder.release(valueBuilder);
    }

    @NonNull
    protected QMUIDialogView onCreateDialogView(@NonNull Context context){
        QMUIDialogView dialogView = new QMUIDialogView(context);
        dialogView.setBackground(QMUIResHelper.getAttrDrawable(context, R.attr.qmui_skin_support_dialog_bg));
        dialogView.setRadius(QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_radius));
        skinConfigDialogView(dialogView);
        return dialogView;
    }

    @NonNull
    protected FrameLayout.LayoutParams onCreateDialogLayoutParams() {
        return new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Nullable
    protected View onCreateTitle(@NonNull QMUIDialog dialog, @NonNull QMUIDialogView parent, @NonNull Context context) {
        if (hasTitle()) {
            TextView tv = new QMUISpanTouchFixTextView(context);
            tv.setId(R.id.qmui_dialog_title_id);
            tv.setText(mTitle);
            QMUIResHelper.assignTextViewWithAttr(tv, R.attr.qmui_dialog_title_style);
            skinConfigTitleView(tv);
            return tv;
        }
        return null;
    }

    @NonNull
    protected ConstraintLayout.LayoutParams onCreateTitleLayoutParams(@NonNull Context context) {
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.verticalChainStyle = ConstraintLayout.LayoutParams.CHAIN_PACKED;
        return lp;
    }


    @Nullable
    protected abstract View onCreateContent(@NonNull QMUIDialog dialog, @NonNull QMUIDialogView parent, @NonNull Context context);


    protected QMUIWrapContentScrollView wrapWithScroll(@NonNull View view){
        QMUIWrapContentScrollView scrollView = new QMUIWrapContentScrollView(view.getContext());
        scrollView.addView(view);
        scrollView.setVerticalScrollBarEnabled(false);
        return scrollView;
    }

    protected ConstraintLayout.LayoutParams onCreateContentLayoutParams(@NonNull Context context) {
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.constrainedHeight = true;
        return lp;
    }


    @Nullable
    protected View onCreateOperatorLayout(@NonNull final QMUIDialog dialog, @NonNull QMUIDialogView parent, @NonNull Context context) {
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
            if (mActionContainerOrientation != VERTICAL) {
                if (justifyContent == 0) {
                    spaceInsertPos = size;
                } else if (justifyContent == 1) {
                    spaceInsertPos = 0;
                } else if (justifyContent == 3) {
                    spaceInsertPos = spaceCustomIndex;
                }
            }

            final QMUILinearLayout layout = new QMUILinearLayout(context, null, R.attr.qmui_dialog_action_container_style);
            layout.setId(R.id.qmui_dialog_operator_layout_id);
            layout.setOrientation(mActionContainerOrientation == VERTICAL ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
            skinConfigActionContainer(layout);

            for (int i = 0; i < size; i++) {
                if (spaceInsertPos == i) {
                    layout.addView(createActionContainerSpace(context));
                }
                QMUIDialogAction action = mActions.get(i);
                action.skinSeparatorColorAttr(mActionDividerColorAttr);
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
                    int color = mActionDividerColorAttr == 0 ? mActionDividerColor :
                            QMUISkinHelper.getSkinColor(actionView, mActionDividerColorAttr);
                    if (mActionContainerOrientation == VERTICAL) {
                        actionView.onlyShowTopDivider(mActionDividerInsetStart,
                                mActionDividerInsetEnd, mActionDividerThickness, color);
                    } else {
                        actionView.onlyShowLeftDivider(mActionDividerInsetStart,
                                mActionDividerInsetEnd, mActionDividerThickness, color);
                    }
                }

                actionView.setChangeAlphaWhenDisable(mChangeAlphaForPressOrDisable);
                actionView.setChangeAlphaWhenPress(mChangeAlphaForPressOrDisable);
                layout.addView(actionView, actionLp);
            }

            if (spaceInsertPos == size) {
                layout.addView(createActionContainerSpace(context));
            }

            if (mActionContainerOrientation == HORIZONTAL) {
                layout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        int width = right - left;
                        int childCount = layout.getChildCount();
                        if (childCount > 0) {
                            View lastChild = layout.getChildAt(childCount - 1);
                            // 如果ActionButton的宽度过宽，则减小padding
                            if (lastChild.getRight() > width) {
                                int childPaddingHor = Math.max(0, lastChild.getPaddingLeft() - QMUIDisplayHelper.dp2px(mContext, 3));
                                for (int i = 0; i < childCount; i++) {
                                    layout.getChildAt(i).setPadding(childPaddingHor, 0, childPaddingHor, 0);
                                }
                            }
                        }

                    }
                });
            }
            return layout;
        }
        return null;
    }

    @NonNull
    protected ConstraintLayout.LayoutParams onCreateOperatorLayoutLayoutParams(@NonNull Context context) {
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.verticalChainStyle = ConstraintLayout.LayoutParams.CHAIN_PACKED;
        return lp;
    }

    private View createActionContainerSpace(Context context) {
        Space space = new Space(context);
        LinearLayout.LayoutParams spaceLp = new LinearLayout.LayoutParams(0, 0);
        spaceLp.weight = 1;
        space.setLayoutParams(spaceLp);
        return space;
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
