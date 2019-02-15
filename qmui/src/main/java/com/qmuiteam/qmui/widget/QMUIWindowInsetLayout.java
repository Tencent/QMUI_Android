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
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;

import com.qmuiteam.qmui.layout.QMUIFrameLayout;
import com.qmuiteam.qmui.util.QMUIWindowInsetHelper;

/**
 * From: https://github.com/oxoooo/earth/blob/30bd82fac7867be596bddf3bd0b32d8be3800665/app/src/main/java/ooo/oxo/apps/earth/widget/WindowInsetsFrameLayout.java
 * 教程(英文): https://medium.com/google-developers/why-would-i-want-to-fitssystemwindows-4e26d9ce1eec#.6i7s7gyam
 * 教程翻译: https://github.com/bboyfeiyu/android-tech-frontier/blob/master/issue-35/%E4%B8%BA%E4%BB%80%E4%B9%88%E6%88%91%E4%BB%AC%E8%A6%81%E7%94%A8fitsSystemWindows.md
 * <p>
 * 对于Keyboard的处理我们需要格外小心，这个组件不能只是处理状态栏，因为android还存在NavBar
 * 当windowInsets.bottom > 100dp的时候，我们认为是弹起了键盘。一旦弹起键盘，那么将由QMUIWindowInsetLayout消耗掉，其子view的windowInsets.bottom传递为0
 *
 * @author cginechen
 * @date 2016-03-25
 */
public class QMUIWindowInsetLayout extends QMUIFrameLayout implements IWindowInsetLayout {
    protected QMUIWindowInsetHelper mQMUIWindowInsetHelper;

    public QMUIWindowInsetLayout(Context context) {
        this(context, null);
    }

    public QMUIWindowInsetLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QMUIWindowInsetLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mQMUIWindowInsetHelper = new QMUIWindowInsetHelper(this, this);
    }


    @SuppressWarnings("deprecation")
    @Override
    protected boolean fitSystemWindows(Rect insets) {
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            return applySystemWindowInsets19(insets);
        }
        return super.fitSystemWindows(insets);
    }

    @Override
    public boolean applySystemWindowInsets19(Rect insets) {
        return mQMUIWindowInsetHelper.defaultApplySystemWindowInsets19(this, insets);
    }

    @Override
    public boolean applySystemWindowInsets21(Object insets) {
        return mQMUIWindowInsetHelper.defaultApplySystemWindowInsets21(this, insets);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewCompat.requestApplyInsets(this);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // xiaomi 8 not reapply insets default...
        ViewCompat.requestApplyInsets(this);
    }
}
