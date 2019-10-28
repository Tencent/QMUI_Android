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

package com.qmuiteam.qmui.skin;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.skin.defaultAttr.IQMUISkinDefaultAttrProvider;
import com.qmuiteam.qmui.skin.defaultAttr.QMUISkinSimpleDefaultAttrProvider;
import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class QMUISkinHelper {

    public static QMUISkinValueBuilder sSkinValueBuilder = QMUISkinValueBuilder.acquire();

    public static int getSkinColor(View view, int colorAttr) {
        Integer skin = (Integer) view.getTag(R.id.qmui_skin_current_index);
        Resources.Theme theme;
        if (skin == null || skin <= 0) {
            theme = view.getContext().getTheme();
        } else {
            theme = QMUISkinManager.defaultInstance(view.getContext()).getTheme(skin);
        }
        return QMUIResHelper.getAttrColor(theme, colorAttr);
    }

    @Nullable
    public static Drawable getSkinDrawable(View view, int drawableAttr) {
        Integer skin = (Integer) view.getTag(R.id.qmui_skin_current_index);
        Resources.Theme theme;
        if (skin == null || skin <= 0) {
            theme = view.getContext().getTheme();
        } else {
            theme = QMUISkinManager.defaultInstance(view.getContext()).getTheme(skin);
        }
        return QMUIResHelper.getAttrDrawable(view.getContext(), theme, drawableAttr);
    }


    public static void setSkinValue(@NonNull View view, QMUISkinValueBuilder skinValueBuilder) {
        setSkinValue(view, skinValueBuilder.build());
    }

    public static void setSkinValue(@NonNull View view, String value) {
        view.setTag(R.id.qmui_skin_value, value);
    }

    @MainThread
    public static void setSkinValue(@NonNull View view, SkinWriter writer) {
        writer.write(sSkinValueBuilder);
        setSkinValue(view, sSkinValueBuilder.build());
        sSkinValueBuilder.clear();
    }

    public static void setSkinDefaultProvider(@NonNull View view,
                                              IQMUISkinDefaultAttrProvider provider){
        view.setTag(R.id.qmui_skin_default_attr_provider, provider);
    }
}
