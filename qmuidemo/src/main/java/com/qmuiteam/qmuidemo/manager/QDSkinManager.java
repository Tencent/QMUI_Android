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
package com.qmuiteam.qmuidemo.manager;

import android.content.Context;
import android.content.res.Configuration;

import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmuidemo.QDApplication;
import com.qmuiteam.qmuidemo.R;

public class QDSkinManager {
    public static final int SKIN_BLUE = 1;
    public static final int SKIN_DARK = 2;
    public static final int SKIN_WHITE = 3;


    public static void install(Context context) {
        QMUISkinManager skinManager = QMUISkinManager.defaultInstance(context);
        skinManager.addSkin(SKIN_BLUE, R.style.app_skin_blue);
        skinManager.addSkin(SKIN_DARK, R.style.app_skin_dark);
        skinManager.addSkin(SKIN_WHITE, R.style.app_skin_white);
        boolean isDarkMode = (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        int storeSkinIndex = QDPreferenceManager.getInstance(context).getSkinIndex();
        if (isDarkMode && storeSkinIndex != SKIN_DARK) {
            skinManager.changeSkin(SKIN_DARK);
        } else if (!isDarkMode && storeSkinIndex == SKIN_DARK) {
            skinManager.changeSkin(SKIN_BLUE);
        }else{
            skinManager.changeSkin(storeSkinIndex);
        }
    }

    public static void changeSkin(int index) {
        QMUISkinManager.defaultInstance(QDApplication.getContext()).changeSkin(index);
        QDPreferenceManager.getInstance(QDApplication.getContext()).setSkinIndex(index);
    }

    public static int getCurrentSkin() {
        return QMUISkinManager.defaultInstance(QDApplication.getContext()).getCurrentSkin();
    }
}
