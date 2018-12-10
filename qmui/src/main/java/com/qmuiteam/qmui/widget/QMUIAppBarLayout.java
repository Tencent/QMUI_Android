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

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowInsets;

import com.qmuiteam.qmui.util.QMUIWindowInsetHelper;

import java.lang.reflect.Field;

/**
 * add support for API 19 when use with {@link android.support.design.widget.CoordinatorLayout}
 * and {@link QMUICollapsingTopBarLayout}
 *
 * notice: we use reflection to change the field value in AppBarLayout. use it only if you need to
 * set fitSystemWindows for StatusBar
 *
 * @author cginechen
 * @date 2017-09-20
 */

public class QMUIAppBarLayout extends AppBarLayout implements IWindowInsetLayout {
    public QMUIAppBarLayout(Context context) {
        super(context);
    }

    public QMUIAppBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean applySystemWindowInsets19(final Rect insets) {
        if (ViewCompat.getFitsSystemWindows(this)) {
            //noinspection TryWithIdenticalCatches
            try {
                Field field = AppBarLayout.class.getDeclaredField("mLastInsets");
                field.setAccessible(true);
                field.set(this, new WindowInsetsCompat(null) {
                    @Override
                    public int getSystemWindowInsetTop() {
                        return insets.top;
                    }
                });
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (QMUIWindowInsetHelper.jumpDispatch(child)) {
                    continue;
                }


                if (!QMUIWindowInsetHelper.isHandleContainer(child)) {
                    child.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                } else {
                    if (child instanceof IWindowInsetLayout) {
                        ((IWindowInsetLayout) child).applySystemWindowInsets19(insets);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean applySystemWindowInsets21(Object insets) {
        return true;
    }
}
