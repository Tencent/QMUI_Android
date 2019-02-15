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
import androidx.customview.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.qmuiteam.qmui.layout.QMUIFrameLayout;
import com.qmuiteam.qmuidemo.R;

/**
 * Created by cgspine on 2018/3/22.
 */

public class QDShadowAdjustLayout extends QMUIFrameLayout {
    ViewDragHelper viewDragHelper;

    public QDShadowAdjustLayout(Context context) {
        this(context, null);
    }

    public QDShadowAdjustLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        viewDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return child.getId() == R.id.layout_for_test;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return left;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                return top;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return viewDragHelper.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewDragHelper.processTouchEvent(event);
        return true;
    }
}
