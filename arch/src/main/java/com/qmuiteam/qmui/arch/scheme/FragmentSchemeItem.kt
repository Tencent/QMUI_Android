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
package com.qmuiteam.qmui.arch.scheme

import android.app.Activity
import android.content.Intent
import android.util.ArrayMap
import com.qmuiteam.qmui.QMUILog
import com.qmuiteam.qmui.arch.QMUIFragment
import com.qmuiteam.qmui.arch.QMUIFragmentActivity
import com.qmuiteam.qmui.arch.annotation.FragmentContainerParam

private val factories by lazy {
    mutableMapOf<Class<out QMUISchemeFragmentFactory>, QMUISchemeFragmentFactory>()
}

internal class FragmentSchemeItem(
    private val fragmentCls: Class<out QMUIFragment?>,
    useRefreshIfMatchedCurrent: Boolean,
    private val activityClsList: Array<Class<out QMUIFragmentActivity>>,
    private val fragmentFactoryCls: Class<out QMUISchemeFragmentFactory>?,
    private val forceNewActivity: Boolean,
    required: ArrayMap<String, String?>?,
    keysForInt: Array<String>?,
    keysForBool: Array<String>?,
    keysForLong: Array<String>?,
    keysForFloat: Array<String>?,
    keysForDouble: Array<String>?,
    defaultParams: Array<String>?,
    schemeMatcherCls: Class<out QMUISchemeMatcher>?,
    schemeValueConverterCls: Class<out QMUISchemeValueConverter>?
) : SchemeItem(
    required, useRefreshIfMatchedCurrent, keysForInt, keysForBool, keysForLong,
    keysForFloat, keysForDouble, defaultParams, schemeMatcherCls, schemeValueConverterCls
) {
    override fun handle(
        handler: QMUISchemeHandler,
        handleContext: SchemeHandleContext,
        schemeInfo: SchemeInfo
    ): Boolean {
        if (activityClsList.isEmpty()) {
            QMUILog.d(QMUISchemeHandler.TAG, "Can not start a new fragment because the host is't provided")
            return false
        }

        var factoryCls = fragmentFactoryCls
        if (factoryCls == null) {
            factoryCls = handler.defaultFragmentFactory
        }
        var factory = factories[factoryCls]
        if (factory == null) {
            try {
                factory = factoryCls.newInstance()
                factories[factoryCls] = factory
            } catch (e: Exception) {
                QMUILog.printErrStackTrace(
                    QMUISchemeHandler.TAG, e,
                    "error to instance QMUISchemeFragmentFactory: %d", factoryCls.simpleName
                )
            }
        }
        if (factory == null) {
            return false
        }
        val params = convertFrom(schemeInfo.params)
        if (factory.shouldBlockJump(handleContext.activity, fragmentCls, params)) {
            return false
        }
        val bundle = factory.factory(params, schemeInfo.origin)
        if (!isCurrentActivityCanStartFragment(handleContext, params) || isForceNewActivity(params)) {
            val ret = handleContext.flushAndBuildFirstFragment(activityClsList, params, FragmentAndArg(fragmentCls, bundle, factory))
            if (ret) {
                if (shouldFinishCurrent(params)) {
                    handleContext.shouldFinishCurrent = true
                }
                return true
            }
            return false
        }
        if (handleContext.canUseRefresh() && isUseRefreshIfMatchedCurrent) {
            val fragmentActivity = handleContext.activity as QMUIFragmentActivity
            val currentFragment = fragmentActivity.currentFragment
            if (currentFragment != null && currentFragment.javaClass == fragmentCls && currentFragment is FragmentSchemeRefreshable) {
                currentFragment.refreshFromScheme(bundle)
                return true
            }
        }
        handleContext.pushFragment(FragmentAndArg(fragmentCls, bundle, factory))
        if (shouldFinishCurrent(params)) {
            handleContext.shouldFinishCurrent = true
        }
        return true
    }

    private fun isCurrentActivityCanStartFragment(handleContext: SchemeHandleContext, scheme: Map<String, SchemeValue>?): Boolean {
        if (handleContext.intentList.isNotEmpty() || handleContext.buildingIntent != null) {
            if (!QMUIFragmentActivity::class.java.isAssignableFrom(handleContext.buildingActivityClass)) {
                return false
            }
            val buildingIntent = handleContext.buildingIntent ?: return false
            for (cls in activityClsList) {
                if (isCurrentActivityCanStartFragment(
                        handleContext.buildingActivityClass,
                        buildingIntent,
                        cls,
                        scheme
                    )
                ) {
                    return true
                }
            }
            return false
        }
        if (handleContext.activity !is QMUIFragmentActivity) {
            return false
        }
        if (handleContext.activity.supportFragmentManager.isStateSaved) {
            // use new activity if the state has already been saved.
            return false
        }
        for (cls in activityClsList) {
            if (isCurrentActivityCanStartFragment(
                    handleContext.buildingActivityClass,
                    handleContext.activity.intent,
                    cls,
                    scheme
                )
            ) {
                return true
            }
        }
        return false
    }

    private fun isCurrentActivityCanStartFragment(
        buildingActivity: Class<out Activity>,
        buildingIntent: Intent,
        targetActivity: Class<out QMUIFragmentActivity>,
        scheme: Map<String, SchemeValue>?
    ): Boolean {
        if (!targetActivity.isAssignableFrom(buildingActivity)) {
            return false
        }
        val fragmentContainerParam = targetActivity.getAnnotation(FragmentContainerParam::class.java) ?: return true
        val required: Array<String> = fragmentContainerParam.required
        val any: Array<String> = fragmentContainerParam.any
        if (required.isEmpty() && any.isEmpty()) {
            return true
        }
        if (scheme == null || scheme.isEmpty()) {
            return false
        }
        for (s in required) {
            val value = scheme[s]
            if (value == null || !buildingIntent.hasExtra(s)) {
                return false
            }
            if (value.type == java.lang.Boolean.TYPE) {
                if (buildingIntent.getBooleanExtra(s, false) != value.value as Boolean) {
                    return false
                }
            } else if (value.type == Integer.TYPE) {
                if (buildingIntent.getIntExtra(s, 0) != value.value as Int) {
                    return false
                }
            } else if (value.type == java.lang.Long.TYPE) {
                if (buildingIntent.getLongExtra(s, 0) != value.value as Long) {
                    return false
                }
            } else if (value.type == java.lang.Float.TYPE) {
                if (buildingIntent.getFloatExtra(s, 0f) != value.value as Float) {
                    return false
                }
            } else if (value.type == java.lang.Double.TYPE) {
                if (buildingIntent.getDoubleExtra(s, 0.0) != value.value as Double) {
                    return false
                }
            } else if (buildingIntent.getStringExtra(s) != value.value) {
                return false
            }
        }
        for (s in any) {
            if (buildingIntent.hasExtra(s)) {
                val value = scheme[s] ?: return false
                if (value.type == java.lang.Boolean.TYPE) {
                    if (buildingIntent.getBooleanExtra(s, false) != value.value as Boolean) {
                        return false
                    }
                } else if (value.type == Integer.TYPE) {
                    if (buildingIntent.getIntExtra(s, 0) != value.value as Int) {
                        return false
                    }
                } else if (value.type == java.lang.Long.TYPE) {
                    if (buildingIntent.getLongExtra(s, 0) != value.value as Long) {
                        return false
                    }
                } else if (value.type == java.lang.Float.TYPE) {
                    if (buildingIntent.getFloatExtra(s, 0f) != value.value as Float) {
                        return false
                    }
                } else if (value.type == java.lang.Double.TYPE) {
                    if (buildingIntent.getDoubleExtra(s, 0.0) != value.value as Double) {
                        return false
                    }
                } else if (buildingIntent.getStringExtra(s) != value.value) {
                    return false
                }
            }
        }
        return true
    }

    private fun isForceNewActivity(scheme: Map<String, SchemeValue>?): Boolean {
        if (forceNewActivity) {
            return true
        }
        if (scheme == null || scheme.isEmpty()) {
            return false
        }
        val schemeValue = scheme[QMUISchemeHandler.ARG_FORCE_TO_NEW_ACTIVITY]
        return schemeValue != null && schemeValue.type == java.lang.Boolean.TYPE && (schemeValue.value as Boolean)
    }
}