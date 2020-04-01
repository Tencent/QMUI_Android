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

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.layout.QMUIButton;
import com.qmuiteam.qmui.skin.QMUISkinHelper;
import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.textview.QMUISpanTouchFixTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class QMUIBottomSheetBaseBuilder<T extends QMUIBottomSheetBaseBuilder> {
    private Context mContext;
    protected QMUIBottomSheet mDialog;
    private CharSequence mTitle;
    private boolean mAddCancelBtn;
    private String mCancelText;
    private DialogInterface.OnDismissListener mOnBottomDialogDismissListener;
    private int mRadius = -1;
    private boolean mAllowDrag = false;
    private QMUISkinManager mSkinManager;
    private QMUIBottomSheetBehavior.DownDragDecisionMaker mDownDragDecisionMaker = null;

    public QMUIBottomSheetBaseBuilder(Context context) {
        mContext = context;
    }

    @SuppressWarnings("unchecked")
    public T setTitle(CharSequence title) {
        mTitle = title;
        return (T) this;
    }

    protected boolean hasTitle() {
        return mTitle != null && mTitle.length() != 0;
    }

    @SuppressWarnings("unchecked")
    public T setAllowDrag(boolean allowDrag) {
        mAllowDrag = allowDrag;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setSkinManager(@Nullable QMUISkinManager skinManager) {
        mSkinManager = skinManager;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setDownDragDecisionMaker(QMUIBottomSheetBehavior.DownDragDecisionMaker downDragDecisionMaker) {
        mDownDragDecisionMaker = downDragDecisionMaker;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setAddCancelBtn(boolean addCancelBtn) {
        mAddCancelBtn = addCancelBtn;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setCancelText(String cancelText) {
        mCancelText = cancelText;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setRadius(int radius) {
        mRadius = radius;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setOnBottomDialogDismissListener(DialogInterface.OnDismissListener listener) {
        mOnBottomDialogDismissListener = listener;
        return (T) this;
    }

    public QMUIBottomSheet build() {
        return build(R.style.QMUI_BottomSheet);
    }

    public QMUIBottomSheet build(int style) {
        mDialog = new QMUIBottomSheet(mContext, style);
        Context dialogContext = mDialog.getContext();
        QMUIBottomSheetRootLayout rootLayout = mDialog.getRootView();
        rootLayout.removeAllViews();
        View titleView = onCreateTitleView(mDialog, rootLayout, dialogContext);
        if (titleView != null) {
            mDialog.addContentView(titleView);
        }
        onAddCustomViewBetweenTitleAndContent(mDialog, rootLayout, dialogContext);
        View contentView = onCreateContentView(mDialog, rootLayout, dialogContext);
        if (contentView != null) {
            mDialog.addContentView(contentView);
        }
        onAddCustomViewAfterContent(mDialog, rootLayout, dialogContext);

        if (mAddCancelBtn) {
            mDialog.addContentView(onCreateCancelBtn(mDialog, rootLayout, dialogContext),
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            QMUIResHelper.getAttrDimen(dialogContext,
                                    R.attr.qmui_bottom_sheet_cancel_btn_height)));
        }

        if (mOnBottomDialogDismissListener != null) {
            mDialog.setOnDismissListener(mOnBottomDialogDismissListener);
        }
        if (mRadius != -1) {
            mDialog.setRadius(mRadius);
        }
        mDialog.setSkinManager(mSkinManager);
        QMUIBottomSheetBehavior behavior = mDialog.getBehavior();
        behavior.setAllowDrag(mAllowDrag);
        behavior.setDownDragDecisionMaker(mDownDragDecisionMaker);
        return mDialog;
    }


    @Nullable
    protected View onCreateTitleView(@NonNull QMUIBottomSheet bottomSheet,
                                     @NonNull QMUIBottomSheetRootLayout rootLayout,
                                     @NonNull Context context) {
        if (hasTitle()) {
            QMUISpanTouchFixTextView tv = new QMUISpanTouchFixTextView(context);
            tv.setId(R.id.qmui_bottom_sheet_title);
            tv.setText(mTitle);
            tv.onlyShowBottomDivider(0, 0, 1,
                    QMUIResHelper.getAttrColor(context, R.attr.qmui_skin_support_bottom_sheet_separator_color));
            QMUIResHelper.assignTextViewWithAttr(tv, R.attr.qmui_bottom_sheet_title_style);
            QMUISkinValueBuilder valueBuilder = QMUISkinValueBuilder.acquire();

            valueBuilder.textColor(R.attr.qmui_skin_support_bottom_sheet_title_text_color);
            valueBuilder.bottomSeparator(R.attr.qmui_skin_support_bottom_sheet_separator_color);
            QMUISkinHelper.setSkinValue(tv, valueBuilder);
            valueBuilder.release();
            return tv;
        }
        return null;
    }

    protected void onAddCustomViewBetweenTitleAndContent(@NonNull QMUIBottomSheet bottomSheet,
                                                         @NonNull QMUIBottomSheetRootLayout rootLayout,
                                                         @NonNull Context context) {
    }

    @Nullable
    protected abstract View onCreateContentView(@NonNull QMUIBottomSheet bottomSheet,
                                                @NonNull QMUIBottomSheetRootLayout rootLayout,
                                                @NonNull Context context);

    protected void onAddCustomViewAfterContent(@NonNull QMUIBottomSheet bottomSheet,
                                               @NonNull QMUIBottomSheetRootLayout rootLayout,
                                               @NonNull Context context) {
    }

    @NonNull
    protected View onCreateCancelBtn(@NonNull final QMUIBottomSheet bottomSheet,
                                     @NonNull QMUIBottomSheetRootLayout rootLayout,
                                     @NonNull Context context) {
        QMUIButton button = new QMUIButton(context);
        button.setId(R.id.qmui_bottom_sheet_cancel);
        if (mCancelText == null || mCancelText.isEmpty()) {
            mCancelText = context.getString(R.string.qmui_cancel);
        }
        button.setPadding(0, 0,0, 0);
        button.setBackground(QMUIResHelper.getAttrDrawable(
                context, R.attr.qmui_skin_support_bottom_sheet_cancel_bg));
        button.setText(mCancelText);
        QMUIResHelper.assignTextViewWithAttr(button, R.attr.qmui_bottom_sheet_cancel_style);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheet.cancel();
            }
        });
        button.onlyShowTopDivider(0, 0, 1,
                QMUIResHelper.getAttrColor(
                        context, R.attr.qmui_skin_support_bottom_sheet_separator_color));

        QMUISkinValueBuilder valueBuilder = QMUISkinValueBuilder.acquire();
        valueBuilder.textColor(R.attr.qmui_skin_support_bottom_sheet_cancel_text_color);
        valueBuilder.topSeparator(R.attr.qmui_skin_support_bottom_sheet_separator_color);
        valueBuilder.background(R.attr.qmui_skin_support_bottom_sheet_cancel_bg);
        QMUISkinHelper.setSkinValue(button, valueBuilder);
        valueBuilder.release();
        return button;
    }
}
