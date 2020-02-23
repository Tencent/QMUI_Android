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
import android.util.AttributeSet;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.layout.QMUILinearLayout;
import com.qmuiteam.qmui.skin.QMUISkinHelper;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.util.QMUIResHelper;

public class QMUIBottomSheetRootLayout extends QMUILinearLayout {

    private final int mUsePercentMinHeight;
    private final float mHeightPercent;
    private final int mMaxWidth;

    public QMUIBottomSheetRootLayout(Context context) {
        this(context, null);
    }

    public QMUIBottomSheetRootLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setBackground(QMUIResHelper.getAttrDrawable(context, R.attr.qmui_skin_support_bottom_sheet_bg));
        QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
        builder.background(R.attr.qmui_skin_support_bottom_sheet_bg);
        QMUISkinHelper.setSkinValue(this, builder);
        builder.release();

        int radius = QMUIResHelper.getAttrDimen(context, R.attr.qmui_bottom_sheet_radius);
        if (radius > 0) {
            setRadius(radius, HIDE_RADIUS_SIDE_BOTTOM);
        }
        mUsePercentMinHeight = QMUIResHelper.getAttrDimen(context, R.attr.qmui_bottom_sheet_use_percent_min_height);
        mHeightPercent = QMUIResHelper.getAttrFloatValue(context, R.attr.qmui_bottom_sheet_height_percent);
        mMaxWidth = QMUIResHelper.getAttrDimen(context, R.attr.qmui_bottom_sheet_max_width);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if(widthSize > mMaxWidth){
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, widthMode);
        }
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightSize >= mUsePercentMinHeight) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    (int) (heightSize * mHeightPercent), MeasureSpec.AT_MOST);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
