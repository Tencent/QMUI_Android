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
import android.graphics.Rect;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.util.QMUIWindowHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class QMUIDialogRootLayout extends ViewGroup {

    private QMUIDialogView mDialogView;
    private FrameLayout.LayoutParams mDialogViewLp;
    private int mMinWidth;
    private int mMaxWidth;
    private int mInsetHor;
    private int mInsetVer;
    private boolean mCheckKeyboardOverlay = false;
    private float mMaxPercent = 0.75f;


    public QMUIDialogRootLayout(@NonNull Context context, @NonNull QMUIDialogView dialogView,
                                @Nullable FrameLayout.LayoutParams dialogViewLp) {
        super(context);
        mDialogView = dialogView;
        if (dialogViewLp == null) {
            dialogViewLp = new FrameLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        mDialogViewLp = dialogViewLp;
        addView(mDialogView, dialogViewLp);
        mMinWidth = QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_min_width);
        mMaxWidth = QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_max_width);
        mInsetHor = QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_inset_hor);
        mInsetVer = QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_inset_ver);
    }

    public void setMinWidth(int minWidth) {
        mMinWidth = minWidth;
    }

    public void setMaxWidth(int maxWidth) {
        mMaxWidth = maxWidth;
    }

    public void setInsetHor(int insetHor) {
        mInsetHor = insetHor;
    }

    public void setInsetVer(int insetVer) {
        mInsetVer = insetVer;
    }

    public void setCheckKeyboardOverlay(boolean checkKeyboardOverlay) {
        mCheckKeyboardOverlay = checkKeyboardOverlay;
    }

    public void setMaxPercent(float maxPercent) {
        mMaxPercent = maxPercent;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int keyboardOverlayHeight = 0;
        if (mCheckKeyboardOverlay) {
            Rect visibleInsetRect = QMUIWindowHelper.unSafeGetWindowVisibleInsets(this);
            if (visibleInsetRect != null) {
                keyboardOverlayHeight = visibleInsetRect.bottom;
            }
        }
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int childWidthMeasureSpec, childHeightMeasureSpec;
        if (mDialogViewLp.width > 0) {
            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mDialogViewLp.width, MeasureSpec.EXACTLY);
        } else {
            int childMaxWidth = Math.min(mMaxWidth, widthSize - 2 * mInsetHor);
            if (childMaxWidth <= mMinWidth) {
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mMinWidth, MeasureSpec.EXACTLY);
            } else if (mDialogViewLp.width == LayoutParams.MATCH_PARENT) {
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childMaxWidth, MeasureSpec.EXACTLY);
            } else {
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childMaxWidth, MeasureSpec.AT_MOST);
            }
        }

        if (mDialogViewLp.height > 0) {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mDialogViewLp.height, MeasureSpec.EXACTLY);
        } else {
            int childMaxHeight;
            if(keyboardOverlayHeight > 0){
                childMaxHeight = Math.max(heightSize - 2 * mInsetVer - keyboardOverlayHeight, 0);
            }else{
                childMaxHeight = Math.max((int)(heightSize * mMaxPercent) - 2 * mInsetHor, 0);
            }
            if (mDialogViewLp.height == LayoutParams.MATCH_PARENT) {
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childMaxHeight, MeasureSpec.EXACTLY);
            } else {
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childMaxHeight, MeasureSpec.AT_MOST);
            }
        }
        mDialogView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        if (mDialogView.getMeasuredWidth() < mMinWidth) {
            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mMinWidth, MeasureSpec.EXACTLY);
            mDialogView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
        // InsetVer works when keyboard overlay occurs
        setMeasuredDimension(mDialogView.getMeasuredWidth(), mDialogView.getMeasuredHeight() + 2 * mInsetVer);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int w = r - l;
        int childLeft = (w - mDialogView.getMeasuredWidth()) / 2;
        mDialogView.layout(childLeft, mInsetVer,
                childLeft + mDialogView.getMeasuredWidth(),
                mInsetVer + mDialogView.getMeasuredHeight());
    }

    public QMUIDialogView getDialogView() {
        return mDialogView;
    }
}
