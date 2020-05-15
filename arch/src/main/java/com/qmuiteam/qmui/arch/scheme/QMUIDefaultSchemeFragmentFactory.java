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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qmuiteam.qmui.QMUILog;
import com.qmuiteam.qmui.arch.QMUIFragment;
import com.qmuiteam.qmui.arch.QMUIFragmentActivity;

import java.util.Map;

import static com.qmuiteam.qmui.arch.scheme.QMUISchemeHandler.ARG_FROM_SCHEME;

public class QMUIDefaultSchemeFragmentFactory implements QMUISchemeFragmentFactory {

    @Override
    @Nullable
    public QMUIFragment factory(@NonNull Class<? extends QMUIFragment> fragmentCls,
                                @Nullable Map<String, SchemeValue> scheme) {
        try {
            QMUIFragment fragment = fragmentCls.newInstance();
            fragment.setArguments(createBundleForScheme(scheme));
            return fragment;
        } catch (Exception e) {
            QMUILog.printErrStackTrace(QMUISchemeHandler.TAG, e,
                    "Error to create fragment: %s", fragmentCls.getSimpleName());
            return null;
        }
    }

    @Override
    @Nullable
    public Intent factory(@NonNull Activity activity,
                          @NonNull Class<? extends QMUIFragmentActivity>[] activityClassList,
                          @NonNull Class<? extends QMUIFragment> fragmentCls,
                          @Nullable Map<String, SchemeValue> scheme) {
        Bundle bundle = createBundleForScheme(scheme);
        if (activityClassList.length == 0) {
            return null;
        }
        Intent intent = QMUIFragmentActivity.intentOf(activity, activityClassList[0], fragmentCls, bundle);
        intent.putExtra(ARG_FROM_SCHEME, true);
        return intent;
    }

    @NonNull
    private Bundle createBundleForScheme(@Nullable Map<String, SchemeValue> scheme) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(QMUISchemeHandler.ARG_FROM_SCHEME, true);
        if (scheme != null && !scheme.isEmpty()) {
            for (Map.Entry<String, SchemeValue> item : scheme.entrySet()) {
                String name = item.getKey();
                SchemeValue schemeValue = item.getValue();
                if (schemeValue.type == Integer.TYPE) {
                    bundle.putInt(name, ((int) schemeValue.value));
                } else if (schemeValue.type == Boolean.TYPE) {
                    bundle.putBoolean(name, ((boolean) schemeValue.value));
                } else if (schemeValue.type == Long.TYPE) {
                    bundle.putLong(name, ((long) schemeValue.value));
                } else if (schemeValue.type == Float.TYPE) {
                    bundle.putFloat(name, ((float) schemeValue.value));
                } else if (schemeValue.type == Double.TYPE) {
                    bundle.putDouble(name, ((double) schemeValue.value));
                } else {
                    bundle.putString(name, schemeValue.origin);
                }
            }
        }
        return bundle;
    }

    @Override
    public boolean shouldBlockJump(@NonNull Activity activity,
                                   @NonNull Class<? extends QMUIFragment> fragmentCls,
                                   @Nullable Map<String, SchemeValue> scheme) {
        return false;
    }
}
