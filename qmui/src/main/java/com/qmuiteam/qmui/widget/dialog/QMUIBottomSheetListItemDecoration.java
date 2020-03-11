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
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.skin.IQMUISkinHandlerDecoration;
import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.util.QMUIResHelper;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public class QMUIBottomSheetListItemDecoration extends RecyclerView.ItemDecoration
        implements IQMUISkinHandlerDecoration {

    private final Paint mSeparatorPaint;
    private final int mSeparatorAttr;

    public QMUIBottomSheetListItemDecoration(Context context) {
        mSeparatorPaint = new Paint();
        mSeparatorPaint.setStrokeWidth(1);
        mSeparatorPaint.setStyle(Paint.Style.STROKE);
        mSeparatorAttr = R.attr.qmui_skin_support_bottom_sheet_separator_color;
        if (mSeparatorAttr != 0) {
            mSeparatorPaint.setColor(QMUIResHelper.getAttrColor(context, mSeparatorAttr));
        }
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        RecyclerView.Adapter adapter = parent.getAdapter();
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (adapter == null || layoutManager == null || mSeparatorAttr == 0) {
            return;
        }
        for (int i = 0; i < parent.getChildCount(); i++) {
            View view = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(view);
            if (view instanceof QMUIBottomSheetListItemView) {
                if (position > 0 &&
                        adapter.getItemViewType(position - 1) != QMUIBottomSheetListAdapter.ITEM_TYPE_NORMAL) {
                    int top = layoutManager.getDecoratedTop(view);
                    c.drawLine(0, top, parent.getWidth(), top, mSeparatorPaint);
                }
                if (position + 1 < adapter.getItemCount() &&
                        adapter.getItemViewType(position + 1) == QMUIBottomSheetListAdapter.ITEM_TYPE_NORMAL) {
                    int bottom = layoutManager.getDecoratedBottom(view);
                    c.drawLine(0, bottom, parent.getWidth(), bottom, mSeparatorPaint);
                }
            }
        }
    }

    @Override
    public void handle(@NotNull RecyclerView recyclerView,
                       @NotNull QMUISkinManager manager,
                       int skinIndex,
                       @NotNull Resources.Theme theme) {
        if (mSeparatorAttr != 0) {
            mSeparatorPaint.setColor(QMUIResHelper.getAttrColor(theme, mSeparatorAttr));
        }
    }
}
