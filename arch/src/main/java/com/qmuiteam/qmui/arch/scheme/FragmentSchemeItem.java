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
import androidx.fragment.app.Fragment;

import com.qmuiteam.qmui.QMUILog;
import com.qmuiteam.qmui.arch.QMUIFragment;
import com.qmuiteam.qmui.arch.QMUIFragmentActivity;
import com.qmuiteam.qmui.arch.annotation.FragmentContainerParam;
import com.qmuiteam.qmui.util.QMUILangHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
                              boolean useRefreshIfMatchedCurrent,
                              @NonNull Class<? extends QMUIFragmentActivity>[] activityClsList,
                              @Nullable Class<? extends QMUISchemeFragmentFactory> fragmentFactoryCls,
                              boolean forceNewActivity,
                              @Nullable String forceNewActivityKey,
                              @Nullable ArrayMap<String, String> required,
                              @Nullable String[] keysForInt,
                              @Nullable String[] keysForBool,
                              @Nullable String[] keysForLong,
                              @Nullable String[] keysForFloat,
                              @Nullable String[] keysForDouble,
                              @Nullable Class<? extends QMUISchemeMatcher> schemeMatcherCls,
                              @Nullable Class<? extends QMUISchemeValueConverter> schemeValueConverterCls) {
        super(required, useRefreshIfMatchedCurrent, keysForInt, keysForBool, keysForLong,
                keysForFloat, keysForDouble, schemeMatcherCls, schemeValueConverterCls);
        mFragmentCls = fragmentCls;
        mActivityClsList = activityClsList;
        mForceNewActivity = forceNewActivity;
        mForceNewActivityKey = forceNewActivityKey;
        mFragmentFactoryCls = fragmentFactoryCls;
    }

    @Override
    public boolean handle(@NonNull QMUISchemeHandler handler,
                          @NonNull Activity activity,
                          @Nullable Map<String, SchemeValue> scheme,
                          @NonNull String origin) {
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

        if (!isCurrentActivityCanStartFragment(activity, scheme) || isForceNewActivity(scheme)) {
            Intent intent = factory.factory(activity, mActivityClsList, mFragmentCls, scheme, origin);
            if (intent != null) {
                factory.startActivity(activity, intent);
                if(shouldFinishCurrent(scheme)){
                    activity.finish();
                }
                return true;
            }
            return false;
        }

        QMUIFragmentActivity fragmentActivity = (QMUIFragmentActivity) activity;
        Fragment currentFragment = fragmentActivity.getCurrentFragment();
        if(isUseRefreshIfMatchedCurrent()
                && currentFragment != null
                && currentFragment.getClass() == mFragmentCls
                && currentFragment instanceof FragmentSchemeRefreshable){
            ((FragmentSchemeRefreshable) currentFragment).refreshFromScheme(factory.factory(scheme, origin));
            return true;
        }

        QMUIFragment fragment = factory.factory(mFragmentCls, scheme, origin);
        if(fragment != null){
            int commitId;
            if(shouldFinishCurrent(scheme)){
                commitId = factory.startFragmentAndDestroyCurrent(fragmentActivity, fragment);
            }else{
                commitId = factory.startFragment(fragmentActivity, fragment);
            }
            if (commitId == -1) {
                QMUILog.d(QMUISchemeHandler.TAG, "start fragment failed.");
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean isCurrentActivityCanStartFragment(Activity activity, @Nullable Map<String, SchemeValue> scheme) {
        if (!(activity instanceof QMUIFragmentActivity)) {
            return false;
        }

        QMUIFragmentActivity fragmentActivity = (QMUIFragmentActivity) activity;
        if (fragmentActivity.getSupportFragmentManager().isStateSaved()) {
            // use new activity if the state has already been saved.
            return false;
        }

        loop:for (Class<? extends QMUIFragmentActivity> aClass : mActivityClsList) {
            if (aClass.isAssignableFrom(activity.getClass())) {
                FragmentContainerParam fragmentContainerParam = aClass.getAnnotation(FragmentContainerParam.class);
                if(fragmentContainerParam == null){
                    return true;
                }
                String[] required = fragmentContainerParam.required();
                if(required.length == 0){
                    return true;
                }
                if(scheme == null || scheme.isEmpty()){
                    continue;
                }
                for (String s : required) {
                    SchemeValue value = scheme.get(s);
                    if (value == null || !activity.getIntent().hasExtra(s)) {
                        continue loop;
                    }
                    if(value.type == Boolean.TYPE){
                        if(activity.getIntent().getBooleanExtra(s, false) != (boolean)value.value){
                            continue loop;
                        }
                    }else if(value.type == Integer.TYPE){
                        if(activity.getIntent().getIntExtra(s, 0) != (int)value.value){
                            continue loop;
                        }
                    }else if(value.type == Long.TYPE){
                        if(activity.getIntent().getLongExtra(s, 0) != (long)value.value){
                            continue loop;
                        }
                    }else if(value.type == Float.TYPE){
                        if(activity.getIntent().getFloatExtra(s, 0) != (float)value.value){
                            continue loop;
                        }
                    }else if(value.type == Double.TYPE){
                        if(activity.getIntent().getDoubleExtra(s, 0) != (double)value.value){
                            continue loop;
                        }
                    }else if(!Objects.equals( activity.getIntent().getStringExtra(s), value.value)){
                        continue loop;
                    }
                }
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

        SchemeValue schemeValue = null;
        if(!QMUILangHelper.isNullOrEmpty(mForceNewActivityKey)){
            schemeValue = scheme.get(mForceNewActivityKey);
        }else{
            schemeValue = scheme.get(QMUISchemeHandler.ARG_FORCE_TO_NEW_ACTIVITY);
        }

        return schemeValue != null && schemeValue.type == Boolean.TYPE && ((Boolean) schemeValue.value);
    }
}
