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

package com.qmuiteam.qmui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.view.WindowInsetsCompat;

import com.qmuiteam.qmui.layout.QMUIFrameLayout;
import com.qmuiteam.qmui.util.QMUIWindowInsetHelper;

@Deprecated
public class QMUIWindowInsetLayout extends QMUIFrameLayout {

    public QMUIWindowInsetLayout(Context context) {
        this(context, null);
    }

    public QMUIWindowInsetLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QMUIWindowInsetLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        QMUIWindowInsetHelper.handleWindowInsets(child, WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout());
    }
}
