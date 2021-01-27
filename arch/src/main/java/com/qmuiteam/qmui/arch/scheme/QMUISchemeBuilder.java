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

package com.qmuiteam.qmui.arch.scheme;


import android.net.Uri;
import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class QMUISchemeBuilder {
    private String mPrefix;
    private String mAction;
    private ArrayMap<String, String> mParams;
    private boolean mEncodeParams;

    public QMUISchemeBuilder(String prefix, String action, boolean encodeParams) {
        mPrefix = prefix;
        mAction = action;
        mEncodeParams = encodeParams;
        mParams = new ArrayMap<>();
    }

    public QMUISchemeBuilder param(String name, String value) {
        if (mEncodeParams) {
            mParams.put(name, Uri.encode(value));
        } else {
            mParams.put(name, value);
        }

        return this;
    }

    public QMUISchemeBuilder param(String name, int value) {
        mParams.put(name, String.valueOf(value));
        return this;
    }

    public QMUISchemeBuilder param(String name, boolean value) {
        mParams.put(name, value ? "1" : "0");
        return this;
    }

    public QMUISchemeBuilder param(String name, long value) {
        mParams.put(name, String.valueOf(value));
        return this;
    }

    public QMUISchemeBuilder param(String name, float value) {
        mParams.put(name, String.valueOf(value));
        return this;
    }

    public QMUISchemeBuilder param(String name, double value) {
        mParams.put(name, String.valueOf(value));
        return this;
    }

    public QMUISchemeBuilder finishCurrent(boolean finishCurrent) {
        mParams.put(QMUISchemeHandler.ARG_FINISH_CURRENT, finishCurrent ? "1" : "0");
        return this;
    }

    public QMUISchemeBuilder forceToNewActivity(boolean forceNew) {
        mParams.put(QMUISchemeHandler.ARG_FORCE_TO_NEW_ACTIVITY, forceNew ? "1" : "0");
        return this;
    }

    public String build() {
        StringBuilder builder = new StringBuilder();
        builder.append(mPrefix);
        builder.append(mAction);
        builder.append("?");
        for (int i = 0; i < mParams.size(); i++) {
            if (i != 0) {
                builder.append("&");
            }
            builder.append(mParams.keyAt(i));
            builder.append("=");
            builder.append(mParams.valueAt(i));
        }
        return builder.toString();
    }

    public static QMUISchemeBuilder from(@NonNull String prefix, @NonNull String action, @Nullable String params, boolean encodeNewParams){
        QMUISchemeBuilder builder = new QMUISchemeBuilder(prefix, action, encodeNewParams);
        Map<String, String> paramsMap = new HashMap<>();
        QMUISchemeHandler.parseParams(params, paramsMap);
        if(!paramsMap.isEmpty()){
            builder.mParams.putAll(paramsMap);
        }
        return builder;
    }
}
