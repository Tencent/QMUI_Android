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


package com.qmuiteam.qmuidemo.view;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUILoadingView;

public class QDLoadingItemView extends FrameLayout {

    private QMUILoadingView mLoadingView;

    public QDLoadingItemView(@NonNull Context context) {
        this(context, null);
    }

    public QDLoadingItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mLoadingView = new QMUILoadingView(context,
                QMUIDisplayHelper.dp2px(context, 24), Color.LTGRAY);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        addView(mLoadingView, lp);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(
                QMUIDisplayHelper.dp2px(getContext(), 48), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mLoadingView.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mLoadingView.stop();
    }
}
