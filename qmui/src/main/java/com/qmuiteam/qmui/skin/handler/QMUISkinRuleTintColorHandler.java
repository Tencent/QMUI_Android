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

import android.content.res.ColorStateList;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.qmuiteam.qmui.skin.QMUISkinHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.QMUILoadingView;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;

import androidx.core.widget.CompoundButtonCompat;
import androidx.core.widget.ImageViewCompat;

import org.jetbrains.annotations.NotNull;

public class QMUISkinRuleTintColorHandler extends QMUISkinRuleColorStateListHandler {

    @Override
    protected void handle(@NotNull View view, @NotNull String name, ColorStateList colorStateList) {
        if(colorStateList == null){
            return;
        }
        if(view instanceof QMUILoadingView){
            ((QMUILoadingView) view).setColor(colorStateList.getDefaultColor());
        }else if(view instanceof QMUIPullRefreshLayout.RefreshView){
            ((QMUIPullRefreshLayout.RefreshView)view).setColorSchemeColors(colorStateList.getDefaultColor());
        }else if (view instanceof ImageView) {
            ImageViewCompat.setImageTintList((ImageView) view, colorStateList);
        }else if(view instanceof CompoundButton){
            CompoundButtonCompat.setButtonTintList((CompoundButton)view, colorStateList);
        }else{
            QMUISkinHelper.warnRuleNotSupport(view, name);
        }
    }
}
