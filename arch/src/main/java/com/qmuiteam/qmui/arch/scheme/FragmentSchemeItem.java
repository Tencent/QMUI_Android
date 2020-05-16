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
import com.qmuiteam.qmui.arch.QMUIFragment;
import com.qmuiteam.qmui.arch.QMUIFragmentActivity;

import java.util.HashMap;
import java.util.Map;

class FragmentSchemeItem extends SchemeItem {
    private static HashMap<Class<? extends QMUISchemeFragmentFactory>, QMUISchemeFragmentFactory> sFactories;
    private final Class<? extends QMUIFragment> mFragmentCls;
    @NonNull
    private final Class<? extends QMUIFragmentActivity>[] mActivityClsList;
    private final boolean mForceNewActivity;
    private final String mForceNewActivityKey;
    @Nullable
    private final Class<? extends QMUISchemeFragmentFactory> mFragmentFactoryCls;

    public FragmentSchemeItem(@NonNull Class<? extends QMUIFragment> fragmentCls,
                              @NonNull Class<? extends QMUIFragmentActivity>[] activityClsList,
                              @Nullable Class<? extends QMUISchemeFragmentFactory> fragmentFactoryCls,
                              boolean forceNewActivity,
                              @Nullable String forceNewActivityKey,
                              @Nullable ArrayMap<String, String> required,
                              @Nullable String[] keysForInt,
                              @Nullable String[] keysForBool,
                              @Nullable String[] keysForLong,
                              @Nullable String[] keysForFloat,
                              @Nullable String[] keysForDouble) {
        super(required, keysForInt, keysForBool, keysForLong, keysForFloat, keysForDouble);
        mFragmentCls = fragmentCls;
        mActivityClsList = activityClsList;
        mForceNewActivity = forceNewActivity;
        mForceNewActivityKey = forceNewActivityKey;
        mFragmentFactoryCls = fragmentFactoryCls;
    }

    @Override
    public boolean handle(@NonNull QMUISchemeHandler handler,
                          @NonNull Activity activity,
                          @Nullable Map<String, SchemeValue> scheme) {
        if (mActivityClsList.length == 0) {
            QMUILog.d(QMUISchemeHandler.TAG, "Can not start a new fragment because the host is't provided");
            return false;
        }
        if (sFactories == null) {
            sFactories = new HashMap<>();
        }

        Class<? extends QMUISchemeFragmentFactory> factoryCls = mFragmentFactoryCls;
        if (factoryCls == null) {
            factoryCls = handler.getDefaultFragmentFactory();
        }

        QMUISchemeFragmentFactory factory = sFactories.get(factoryCls);
        if (factory == null) {
            try {
                factory = factoryCls.newInstance();
                sFactories.put(factoryCls, factory);
            } catch (Exception e) {
                QMUILog.printErrStackTrace(QMUISchemeHandler.TAG, e,
                        "error to instance QMUISchemeFragmentFactory: %d", factoryCls.getSimpleName());
            }
        }
        if (factory == null) {
            return false;
        }

        if (factory.shouldBlockJump(activity, mFragmentCls, scheme)) {
            return true;
        }

        if (!isCurrentActivityCanStartFragment(activity) || isForceNewActivity(scheme)) {
            Intent intent = factory.factory(activity, mActivityClsList, mFragmentCls, scheme);
            if (intent != null) {
                activity.startActivity(intent);
                return true;
            }
            return false;
        }

        QMUIFragmentActivity fragmentActivity = (QMUIFragmentActivity) activity;
        QMUIFragment fragment = factory.factory(mFragmentCls, scheme);
        if (fragment != null) {
            int commitId = fragmentActivity.startFragment(fragment);
            if (commitId == -1) {
                QMUILog.d(QMUISchemeHandler.TAG, "start fragment failed.");
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean isCurrentActivityCanStartFragment(Activity activity) {
        if (!(activity instanceof QMUIFragmentActivity)) {
            return false;
        }

        QMUIFragmentActivity fragmentActivity = (QMUIFragmentActivity) activity;
        if (fragmentActivity.getSupportFragmentManager().isStateSaved()) {
            // use new activity if the state has already been saved.
            return false;
        }

        for (Class<? extends QMUIFragmentActivity> aClass : mActivityClsList) {
            if (aClass.isAssignableFrom(activity.getClass())) {
                return true;
            }
        }
        return false;
    }

    private boolean isForceNewActivity(@Nullable Map<String, SchemeValue> scheme) {
        if (mForceNewActivity) {
            return true;
        }
        if (scheme == null || scheme.isEmpty()) {
            return false;
        }
        if (scheme.get(QMUISchemeHandler.ARG_FORCE_TO_NEW_ACTIVITY) != null) {
            return true;
        }

        if (mForceNewActivityKey != null) {
            SchemeValue schemeValue = scheme.get(mForceNewActivityKey);
            return schemeValue != null && schemeValue.type == Boolean.TYPE && ((Boolean) schemeValue.value);
        }

        return false;
    }
}
