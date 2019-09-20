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

import android.app.Activity;
import android.content.Context;

import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmuidemo.QDApplication;
import com.qmuiteam.qmuidemo.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class QDSkinManager {
    public static final int SKIN_BLUE = 1;
    public static final int SKIN_DARK = 2;
    public static final int SKIN_WHITE = 3;
    public static int sCurrentTheme = Integer.MIN_VALUE;

    private static final ArrayList<WeakReference<Activity>> mListenActivityList = new ArrayList<>();

    public static void install(Context context) {
        QMUISkinManager skinManager = QMUISkinManager.getInstance(context);
        skinManager.addTheme(SKIN_BLUE, R.style.app_skin_blue);
        skinManager.addTheme(SKIN_DARK, R.style.app_skin_dark);
        skinManager.addTheme(SKIN_WHITE, R.style.app_skin_white);
    }

    public static void changeSkin(int index) {
        if (sCurrentTheme == index) {
            return;
        }
        sCurrentTheme = index;
        QDPreferenceManager.getInstance(QDApplication.getContext()).setSkinIndex(index);
        for (WeakReference<Activity> wr : mListenActivityList) {
            if (wr.get() != null) {
                dispatch(wr.get(), sCurrentTheme);
            }
        }
    }

    public static void dispatch(Activity activity, int skin) {
        QMUISkinManager.getInstance(activity).dispatch(QMUIViewHelper.getActivityRoot(activity), skin);
        if(skin == SKIN_WHITE){
            QMUIStatusBarHelper.setStatusBarLightMode(activity);
        }else{
            QMUIStatusBarHelper.setStatusBarDarkMode(activity);
        }

    }

    public static void register(Activity activity) {
        mListenActivityList.add(new WeakReference<>(activity));
        if (sCurrentTheme == Integer.MIN_VALUE) {
            sCurrentTheme = QDPreferenceManager.getInstance(activity).getSkinIndex();
        }
        dispatch(activity, sCurrentTheme);
    }

    public static void unRegister(Activity activity) {
        for (int i = mListenActivityList.size() - 1; i >= 0; i++) {
            WeakReference<Activity> wr = mListenActivityList.get(i);
            if (wr.get() == null || wr.get() == activity) {
                mListenActivityList.remove(wr);
            }
        }
    }
}
