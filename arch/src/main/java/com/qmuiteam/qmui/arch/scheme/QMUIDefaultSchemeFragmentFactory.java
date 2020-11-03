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
import com.qmuiteam.qmui.arch.annotation.FragmentContainerParam;

import java.util.Map;

import static com.qmuiteam.qmui.arch.scheme.QMUISchemeHandler.ARG_FROM_SCHEME;

public class QMUIDefaultSchemeFragmentFactory implements QMUISchemeFragmentFactory {

    @Override
    @Nullable
    public QMUIFragment factory(@NonNull Class<? extends QMUIFragment> fragmentCls,
                                @Nullable Map<String, SchemeValue> scheme,
                                @NonNull String origin) {
        try {
            QMUIFragment fragment = fragmentCls.newInstance();
            fragment.setArguments(factory(scheme, origin));
            return fragment;
        } catch (Exception e) {
            QMUILog.printErrStackTrace(QMUISchemeHandler.TAG, e,
                    "Error to create fragment: %s", fragmentCls.getSimpleName());
            return null;
        }
    }


    @Override
    @Nullable
    public Bundle factory(@Nullable Map<String, SchemeValue> scheme, @NonNull String origin) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(QMUISchemeHandler.ARG_FROM_SCHEME, true);
        bundle.putString(QMUISchemeHandler.ARG_ORIGIN_SCHEME, origin);
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
    @Nullable
    public Intent factory(@NonNull Activity activity,
                          @NonNull Class<? extends QMUIFragmentActivity>[] activityClassList,
                          @NonNull Class<? extends QMUIFragment> fragmentCls,
                          @Nullable Map<String, SchemeValue> scheme,
                          @NonNull String origin) {
        Bundle bundle = factory(scheme, origin);
        if (activityClassList.length == 0) {
            return null;
        }
        loop: for (Class<? extends QMUIFragmentActivity> target : activityClassList) {
            Intent intent = QMUIFragmentActivity.intentOf(activity, target, fragmentCls, bundle);
            intent.putExtra(ARG_FROM_SCHEME, true);
            FragmentContainerParam fragmentContainerParam = target.getAnnotation(FragmentContainerParam.class);
            if (fragmentContainerParam == null) {
                return intent;
            }

            String[] required = fragmentContainerParam.required();
            String[] optional = fragmentContainerParam.optional();

            if(required.length == 0){
                putOptionalSchemeValuesToIntent(intent, scheme, optional);
                return intent;
            }

            if (scheme == null || scheme.isEmpty()) {
                // not matched.
                continue;
            }
            for (String arg : required) {
                SchemeValue value = scheme.get(arg);
                if(value == null){
                    // not matched.
                    continue loop;
                }
                putSchemeValueToIntent(intent, arg, value);
            }

            putOptionalSchemeValuesToIntent(intent, scheme, optional);
            return intent;
        }
        return null;
    }

    private void putOptionalSchemeValuesToIntent(Intent intent,
                                                 @Nullable Map<String, SchemeValue> scheme,
                                                 String[] optional){
        if (scheme == null || scheme.isEmpty()) {
            return;
        }
        for (String arg : optional) {
            SchemeValue value = scheme.get(arg);
            if(value != null){
                putSchemeValueToIntent(intent, arg, value);
            }
        }
    }

    private void putSchemeValueToIntent(Intent intent, String arg, @NonNull SchemeValue value){
        if (value.type == Boolean.TYPE) {
            intent.putExtra(arg, (boolean) value.value);
        } else if (value.type == Integer.TYPE) {
            intent.putExtra(arg, (int) value.value);
        } else if (value.type == Long.TYPE) {
            intent.putExtra(arg, (long) value.value);
        } else if (value.type == Float.TYPE) {
            intent.putExtra(arg, (float) value.value);
        } else if (value.type == Double.TYPE) {
            intent.putExtra(arg, (double) value.value);
        } else{
            intent.putExtra(arg, value.origin);
        }
    }

    @Override
    public void startActivity(@NonNull Activity activity, @NonNull Intent intent) {
        activity.startActivity(intent);
    }

    @Override
    public int startFragmentAndDestroyCurrent(QMUIFragmentActivity activity, QMUIFragment fragment) {
        return activity.startFragmentAndDestroyCurrent(fragment, true);
    }

    @Override
    public int startFragment(QMUIFragmentActivity activity, QMUIFragment fragment) {
        return activity.startFragment(fragment);
    }

    @Override
    public boolean shouldBlockJump(@NonNull Activity activity,
                                   @NonNull Class<? extends QMUIFragment> fragmentCls,
                                   @Nullable Map<String, SchemeValue> scheme) {
        return false;
    }
}
