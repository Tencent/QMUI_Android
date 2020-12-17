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
import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qmuiteam.qmui.QMUILog;

import java.util.HashMap;
import java.util.Map;

class ActivitySchemeItem extends SchemeItem {
    private static HashMap<Class<? extends QMUISchemeIntentFactory>, QMUISchemeIntentFactory> sFactories;

    @NonNull
    private final Class<? extends Activity> mActivityClass;
    @Nullable
    private final Class<? extends QMUISchemeIntentFactory> mIntentFactoryCls;

    public ActivitySchemeItem(@NonNull Class<? extends Activity> activityClass,
                              boolean useRefreshIfMatchedCurrent,
                              @Nullable Class<? extends QMUISchemeIntentFactory> intentFactoryCls,
                              @Nullable ArrayMap<String, String> required,
                              @Nullable String[] keysForInt,
                              @Nullable String[] keysForBool,
                              @Nullable String[] keysForLong,
                              @Nullable String[] keysForFloat,
                              @Nullable String[] keysForDouble,
                              @Nullable Class<? extends QMUISchemeMatcher> schemeMatcherCls,
                              @Nullable Class<? extends QMUISchemeValueConverter> schemeValueConverterCls) {
        super(required, useRefreshIfMatchedCurrent, keysForInt, keysForBool,
                keysForLong, keysForFloat, keysForDouble, schemeMatcherCls, schemeValueConverterCls);
        mActivityClass = activityClass;
        mIntentFactoryCls = intentFactoryCls;
    }

    @Override
    public boolean handle(@NonNull QMUISchemeHandler handler,
                          @NonNull Activity activity,
                          @Nullable Map<String, SchemeValue> scheme,
                          @NonNull String origin) {
        if (sFactories == null) {
            sFactories = new HashMap<>();
        }
        Class<? extends QMUISchemeIntentFactory> factoryCls = mIntentFactoryCls;
        if(factoryCls == null){
            factoryCls = handler.getDefaultIntentFactory();
        }

        QMUISchemeIntentFactory factory = sFactories.get(factoryCls);
        if (factory == null) {
            try {
                factory = factoryCls.newInstance();
                sFactories.put(factoryCls, factory);
            } catch (Exception e) {
                QMUILog.printErrStackTrace(QMUISchemeHandler.TAG, e,
                        "error to instance QMUISchemeIntentFactory: %d", factoryCls.getSimpleName());
            }
        }

        if (factory != null) {
            if (factory.shouldBlockJump(activity, mActivityClass, scheme)) {
                return true;
            }

            Intent intent = factory.factory(activity, mActivityClass, scheme, origin);

            if(isUseRefreshIfMatchedCurrent() && mActivityClass == activity.getClass() && activity instanceof ActivitySchemeRefreshable){
                ((ActivitySchemeRefreshable) activity).refreshFromScheme(intent);
            }else{
                factory.startActivity(activity, intent);
                if(shouldFinishCurrent(scheme)){
                    activity.finish();
                }
            }
            return true;
        }
        return false;
    }
}
