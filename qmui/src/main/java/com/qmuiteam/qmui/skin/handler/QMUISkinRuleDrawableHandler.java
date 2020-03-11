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
package com.qmuiteam.qmui.skin.handler;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;

import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.util.QMUIResHelper;

import org.jetbrains.annotations.NotNull;

public abstract class QMUISkinRuleDrawableHandler implements IQMUISkinRuleHandler {
    @Override
    public final void handle(@NotNull @NonNull QMUISkinManager skinManager,
                             @NotNull @NonNull View view,
                             @NotNull @NonNull Resources.Theme theme,
                             @NotNull @NonNull String name, int attr) {
        handle(view, name, QMUIResHelper.getAttrDrawable(view.getContext(), theme, attr));
    }

    protected abstract void handle(@NonNull View view, @NonNull String name, Drawable drawable);
}
