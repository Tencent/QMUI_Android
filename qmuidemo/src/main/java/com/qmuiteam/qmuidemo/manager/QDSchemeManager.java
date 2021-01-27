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
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.qmuiteam.qmui.arch.QMUISwipeBackActivityManager;
import com.qmuiteam.qmui.arch.scheme.QMUISchemeHandleInterpolator;
import com.qmuiteam.qmui.arch.scheme.QMUISchemeHandler;
import com.qmuiteam.qmui.arch.scheme.QMUISchemeParamValueDecoder;

import java.util.Map;

public class QDSchemeManager {
    private static final String TAG = "QDSchemeManager";
    public static final String SCHEME_PREFIX = "qmui://";
    private static QDSchemeManager sInstance;

    public static QDSchemeManager getInstance() {
        if (sInstance == null) {
            sInstance = new QDSchemeManager();
        }
        return sInstance;
    }

    private QMUISchemeHandler mSchemeHandler;

    private QDSchemeManager() {
        mSchemeHandler = new QMUISchemeHandler.Builder(SCHEME_PREFIX)
                .blockSameSchemeTimeout(1000)
                .addInterpolator(new QMUISchemeHandleInterpolator() {
                    @Override
                    public boolean intercept(@NonNull QMUISchemeHandler schemeHandler,
                                             @NonNull Activity activity,
                                             @NonNull String action,
                                             @NonNull Map<String, String> params,
                                             @NonNull String origin) {
                        // Log the scheme.
                        Log.i(TAG, "handle scheme: " + origin);
                        return false;
                    }
                })
                .addInterpolator(new QMUISchemeParamValueDecoder())
                .build();
    }

    public boolean handle(String scheme) {
        if (!mSchemeHandler.handle(scheme)) {
            Log.i(TAG, "scheme can not be handled: " + scheme);
            Toast.makeText(QMUISwipeBackActivityManager.getInstance().getCurrentActivity(),
                    "scheme can not be handled: " + scheme, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
