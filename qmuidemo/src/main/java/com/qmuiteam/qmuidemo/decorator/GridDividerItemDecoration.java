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

package com.qmuiteam.qmuidemo.decorator;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import com.qmuiteam.qmui.skin.IQMUISkinHandlerDecoration;
import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmuidemo.R;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

/**
 * @author cginechen
 * @date 2016-10-21
 */

public class GridDividerItemDecoration extends RecyclerView.ItemDecoration implements IQMUISkinHandlerDecoration {

    private Paint mDividerPaint = new Paint();
    private int mSpanCount;
    private final int mDividerAttr;

    public GridDividerItemDecoration(Context context, int spanCount) {
        this(context, spanCount, R.attr.qmui_skin_support_color_separator, 1f);
    }

    public GridDividerItemDecoration(Context context, int spanCount, int dividerColorAttr, float dividerWidth) {
        mSpanCount = spanCount;
        mDividerAttr = dividerColorAttr;
        mDividerPaint.setStrokeWidth(dividerWidth);
        mDividerPaint.setStyle(Paint.Style.STROKE);
        mDividerPaint.setColor(QMUIResHelper.getAttrColor(context, dividerColorAttr));
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            int position = parent.getChildLayoutPosition(child);
            int column = (position + 1) % mSpanCount;

            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int childBottom = child.getBottom() + params.bottomMargin +
                    Math.round(ViewCompat.getTranslationY(child));
            final int childRight = child.getRight() + params.rightMargin +
                    Math.round(ViewCompat.getTranslationX(child));

            if (childBottom < parent.getHeight()) {
                c.drawLine(child.getLeft(), childBottom, childRight, childBottom, mDividerPaint);
            }

            if (column < mSpanCount) {
                c.drawLine(childRight, child.getTop(), childRight, childBottom, mDividerPaint);
            }

        }
    }

    @Override
    public void handle(@NotNull RecyclerView recyclerView, @NotNull QMUISkinManager manager, int skinIndex, @NotNull Resources.Theme theme) {
        mDividerPaint.setColor(QMUIResHelper.getAttrColor(theme, mDividerAttr));
        recyclerView.invalidate();
    }
}
