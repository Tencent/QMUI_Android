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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

import static com.qmuiteam.qmui.arch.scheme.QMUISchemeHandler.ARG_FROM_SCHEME;
import static com.qmuiteam.qmui.arch.scheme.QMUISchemeHandler.ARG_ORIGIN_SCHEME;

public class QMUIDefaultSchemeIntentFactory implements QMUISchemeIntentFactory {
    @Override
    public Intent factory(@NonNull Activity activity,
                          @NonNull Class<? extends Activity> activityClass,
                          @Nullable Map<String, SchemeValue> scheme,
                          @NonNull String origin) {
        Intent intent = new Intent(activity, activityClass);
        intent.putExtra(ARG_FROM_SCHEME, true);
        intent.putExtra(ARG_ORIGIN_SCHEME, origin);
        if (scheme != null && !scheme.isEmpty()) {
            for (Map.Entry<String, SchemeValue> item : scheme.entrySet()) {
                String name = item.getKey();
                SchemeValue schemeValue = item.getValue();
                if (schemeValue.type == Integer.TYPE) {
                    intent.putExtra(name, ((int) schemeValue.value));
                } else if (schemeValue.type == Boolean.TYPE) {
                    intent.putExtra(name, ((boolean) schemeValue.value));
                } else if (schemeValue.type == Long.TYPE) {
                    intent.putExtra(name, ((long) schemeValue.value));
                } else if (schemeValue.type == Float.TYPE) {
                    intent.putExtra(name, ((float) schemeValue.value));
                } else if (schemeValue.type == Double.TYPE) {
                    intent.putExtra(name, ((double) schemeValue.value));
                } else {
                    intent.putExtra(name, schemeValue.origin);
                }
            }
        }
        return intent;
    }

    @Override
    public void startActivity(@NonNull Activity activity, @NonNull Intent intent) {
        activity.startActivity(intent);
    }

    @Override
    public boolean shouldBlockJump(@NonNull Activity activity,
                                   @NonNull Class<? extends Activity> activityClass,
                                   @Nullable Map<String, SchemeValue> scheme) {
        return false;
    }
}
