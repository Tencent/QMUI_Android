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
import android.graphics.drawable.Drawable;
import android.view.Gravity;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.layout.QMUILinearLayout;
import com.qmuiteam.qmui.skin.QMUISkinHelper;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.util.QMUIResHelper;

public class QMUITipDialogView extends QMUILinearLayout {

    private final int mMaxWidth;
    private final int mMiniWidth;
    private final int mMiniHeight;

    public QMUITipDialogView(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        int radius = QMUIResHelper.getAttrDimen(context, R.attr.qmui_tip_dialog_radius);
        Drawable background = QMUIResHelper.getAttrDrawable(context, R.attr.qmui_skin_support_tip_dialog_bg);
        int paddingHor = QMUIResHelper.getAttrDimen(context, R.attr.qmui_tip_dialog_padding_horizontal);
        int paddingVer = QMUIResHelper.getAttrDimen(context, R.attr.qmui_tip_dialog_padding_vertical);
        setBackground(background);
        setPadding(paddingHor, paddingVer, paddingHor, paddingVer);
        setRadius(radius);
        QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
        builder.background(R.attr.qmui_skin_support_tip_dialog_bg);
        QMUISkinHelper.setSkinValue(this, builder);
        builder.release();
        mMaxWidth = QMUIResHelper.getAttrDimen(context, R.attr.qmui_tip_dialog_max_width);
        mMiniWidth = QMUIResHelper.getAttrDimen(context, R.attr.qmui_tip_dialog_min_width);
        mMiniHeight = QMUIResHelper.getAttrDimen(context, R.attr.qmui_tip_dialog_min_height);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if(widthSize > mMaxWidth){
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, widthMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        boolean needRemeasure = false;
        if(getMeasuredWidth() < mMiniWidth){
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMiniWidth, MeasureSpec.EXACTLY);
            needRemeasure = true;
        }

        if(getMeasuredHeight() < mMiniHeight){
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mMiniHeight, MeasureSpec.EXACTLY);
            needRemeasure = true;
        }

        if(needRemeasure){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
