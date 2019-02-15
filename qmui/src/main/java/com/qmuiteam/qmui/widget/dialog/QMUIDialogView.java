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
import android.graphics.Canvas;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.layout.QMUILinearLayout;
import com.qmuiteam.qmui.util.QMUIResHelper;

/**
 * Created by cgspine on 2018/2/28.
 */

public class QMUIDialogView extends QMUILinearLayout {

    private int mMinWidth;
    private int mMaxWidth;
    private OnDecorationListener mOnDecorationListener;

    public QMUIDialogView(Context context) {
        this(context, null);
    }

    public QMUIDialogView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QMUIDialogView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mMinWidth = QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_min_width);
        mMaxWidth = QMUIResHelper.getAttrDimen(context, R.attr.qmui_dialog_max_width);
    }

    public void setOnDecorationListener(OnDecorationListener onDecorationListener) {
        mOnDecorationListener = onDecorationListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (mMaxWidth > 0 && widthSize > mMaxWidth) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, widthMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST) {
            int measureWidth = getMeasuredWidth();
            if (measureWidth < mMinWidth && mMinWidth < widthSize) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMinWidth, MeasureSpec.EXACTLY);
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mOnDecorationListener != null){
            mOnDecorationListener.onDraw(canvas, this);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if(mOnDecorationListener != null){
            mOnDecorationListener.onDrawOver(canvas, this);
        }
    }

    public void setMinWidth(int minWidth) {
        mMinWidth = minWidth;
    }

    public void setMaxWidth(int maxWidth) {
        mMaxWidth = maxWidth;
    }

    public interface OnDecorationListener{
        void onDraw(Canvas canvas, QMUIDialogView view);
        void onDrawOver(Canvas canvas, QMUIDialogView view);
    }
}
