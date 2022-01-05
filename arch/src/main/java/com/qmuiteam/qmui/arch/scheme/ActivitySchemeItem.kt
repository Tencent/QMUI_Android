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
import android.util.ArrayMap
import com.qmuiteam.qmui.QMUILog

private val factories by lazy { ArrayMap<Class<out QMUISchemeIntentFactory>, QMUISchemeIntentFactory>() }

internal class ActivitySchemeItem(
    private val activityClass: Class<out Activity>,
    useRefreshIfMatchedCurrent: Boolean,
    private val intentFactoryCls: Class<out QMUISchemeIntentFactory>?,
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
    required, useRefreshIfMatchedCurrent, keysForInt, keysForBool,
    keysForLong, keysForFloat, keysForDouble, defaultParams, schemeMatcherCls, schemeValueConverterCls
) {
    override fun handle(
        handler: QMUISchemeHandler,
        handleContext: SchemeHandleContext,
        schemeInfo: SchemeInfo
    ): Boolean {
        var factoryCls = intentFactoryCls
        if (factoryCls == null) {
            factoryCls = handler.defaultIntentFactory
        }
        var factory = factories[factoryCls]
        if (factory == null) {
            try {
                factory = factoryCls.newInstance()
                factories[factoryCls] = factory
            } catch (e: Exception) {
                QMUILog.printErrStackTrace(
                    QMUISchemeHandler.TAG, e, "error to instance QMUISchemeIntentFactory: %d",
                    factoryCls.simpleName
                )
            }
        }
        if (factory != null) {
            val params = convertFrom(schemeInfo.params)
            if (factory.shouldBlockJump(handleContext.activity, activityClass, params)) {
                return false
            }
            val intent = factory.factory(handleContext.activity, activityClass, params, schemeInfo.origin)
            if (handleContext.canUseRefresh() &&
                isUseRefreshIfMatchedCurrent &&
                activityClass == handleContext.activity::class.java &&
                handleContext.activity is ActivitySchemeRefreshable
            ) {
                (handleContext.activity as ActivitySchemeRefreshable).refreshFromScheme(intent)
            } else {
                if (intent == null) {
                    return false
                }
                handleContext.pushActivity(activityClass, intent, factory)

                if (shouldFinishCurrent(params)) {
                    handleContext.shouldFinishCurrent = true
                }
            }
            return true
        }
        return false
    }
}