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

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.qmuiteam.qmui.QMUILog;
import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.skin.defaultAttr.IQMUISkinDefaultAttrProvider;
import com.qmuiteam.qmui.util.QMUIResHelper;

public class QMUISkinHelper {

    public static QMUISkinValueBuilder sSkinValueBuilder = QMUISkinValueBuilder.acquire();

    public static Resources.Theme getSkinTheme(@NonNull View view) {
        QMUISkinManager.ViewSkinCurrent current = QMUISkinManager.getViewSkinCurrent(view);
        Resources.Theme theme;
        if (current == null || current.index < 0) {
            theme = view.getContext().getTheme();
        } else {
            theme = QMUISkinManager.of(current.managerName, view.getContext()).getTheme(current.index);
        }
        return theme;
    }

    public static int getSkinColor(@NonNull View view, int colorAttr) {
        return QMUIResHelper.getAttrColor(getSkinTheme(view), colorAttr);
    }

    public static ColorStateList getSkinColorStateList(@NonNull View view, int colorAttr) {
        return QMUIResHelper.getAttrColorStateList(view.getContext(), getSkinTheme(view), colorAttr);
    }

    @Nullable
    public static Drawable getSkinDrawable(@NonNull View view, int drawableAttr) {
        return QMUIResHelper.getAttrDrawable(view.getContext(), getSkinTheme(view), drawableAttr);
    }


    public static void setSkinValue(@NonNull View view, QMUISkinValueBuilder skinValueBuilder) {
        setSkinValue(view, skinValueBuilder.build());
    }

    public static void setSkinValue(@NonNull View view, String value) {
        view.setTag(R.id.qmui_skin_value, value);
        refreshViewSkin(view);

    }

    @MainThread
    public static void setSkinValue(@NonNull View view, SkinWriter writer) {
        writer.write(sSkinValueBuilder);
        setSkinValue(view, sSkinValueBuilder.build());
        sSkinValueBuilder.clear();
    }

    public static void refreshRVItemDecoration(@NonNull RecyclerView view, IQMUISkinHandlerDecoration itemDecoration){
        QMUISkinManager.ViewSkinCurrent skinCurrent = QMUISkinManager.getViewSkinCurrent(view);
        if(skinCurrent != null){
            QMUISkinManager.of(skinCurrent.managerName, view.getContext()).refreshRecyclerDecoration(view, itemDecoration, skinCurrent.index);
        }
    }

    public static int getCurrentSkinIndex(@NonNull View view) {
        QMUISkinManager.ViewSkinCurrent viewSkinCurrent = QMUISkinManager.getViewSkinCurrent(view);
        if (viewSkinCurrent != null) {
            return viewSkinCurrent.index;
        }
        return QMUISkinManager.DEFAULT_SKIN;
    }

    public static void refreshViewSkin(@NonNull View view){
        QMUISkinManager.ViewSkinCurrent skinCurrent = QMUISkinManager.getViewSkinCurrent(view);
        if (skinCurrent != null) {
            QMUISkinManager.of(skinCurrent.managerName, view.getContext()).refreshTheme(view, skinCurrent.index);
        }
    }

    public static void syncViewSkin(@NonNull View view, @NonNull View sourceView){
        QMUISkinManager.ViewSkinCurrent source = QMUISkinManager.getViewSkinCurrent(sourceView);
        if (source != null) {
            QMUISkinManager.ViewSkinCurrent skin = QMUISkinManager.getViewSkinCurrent(view);
            if(!source.equals(skin)) {
                QMUISkinManager.of(source.managerName, view.getContext()).dispatch(view, source.index);
            }
        }
    }

    public static void setSkinDefaultProvider(@NonNull View view,
                                              IQMUISkinDefaultAttrProvider provider) {
        view.setTag(R.id.qmui_skin_default_attr_provider, provider);
    }

    public static void warnRuleNotSupport(View view, String rule){
        QMUILog.w("QMUISkinManager",
                view.getClass().getSimpleName() + " does't support " + rule);
    }
}
