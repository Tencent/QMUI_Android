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
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.util.QMUINotchHelper;

public class QMUINotchConsumeLayout extends FrameLayout implements INotchInsetConsumer {
    public QMUINotchConsumeLayout(Context context) {
        this(context, null);
    }

    public QMUINotchConsumeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QMUINotchConsumeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFitsSystemWindows(false);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!QMUINotchHelper.isNotchOfficialSupport()) {
            notifyInsetMaybeChanged();
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!QMUINotchHelper.isNotchOfficialSupport()) {
            notifyInsetMaybeChanged();
        }
    }

    @Override
    public boolean notifyInsetMaybeChanged() {
        setPadding(
                QMUINotchHelper.getSafeInsetLeft(this),
                QMUINotchHelper.getSafeInsetTop(this),
                QMUINotchHelper.getSafeInsetRight(this),
                QMUINotchHelper.getSafeInsetBottom(this)
        );
        return true;
    }
}
