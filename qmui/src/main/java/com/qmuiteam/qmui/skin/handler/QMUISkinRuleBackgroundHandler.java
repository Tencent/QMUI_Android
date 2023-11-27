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

import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.skin.SkinValue;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.QMUIProgressBar;
import com.qmuiteam.qmui.widget.QMUISlider;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.jetbrains.annotations.NotNull;

public class QMUISkinRuleBackgroundHandler implements IQMUISkinRuleHandler {

    @Override
    public void handle(@NotNull QMUISkinManager skinManager, @NotNull View view, @NotNull SkinValue skinValue, @NotNull String name, int attr) {
        if(view instanceof QMUIRoundButton){
            ((QMUIRoundButton)view).setBgData(skinValue.getColorStateList(view.getContext(), attr));
        }else if(view instanceof QMUIProgressBar){
            view.setBackgroundColor(skinValue.getColor(view.getContext(), attr));
        }else if(view instanceof QMUISlider){
            ((QMUISlider)view).setBarNormalColor(skinValue.getColor(view.getContext(), attr));
        }else{
            QMUIViewHelper.setBackgroundKeepingPadding(view, skinValue.getDrawable(view.getContext(), attr));
        }
    }
}
