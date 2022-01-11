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

interface QMUISchemeIntentFactory {
    fun factory(
        activity: Activity,
        activityClass: Class<out Activity>,
        scheme: Map<String, SchemeValue>?,
        origin: String
    ): Intent?

    fun startActivities(
        activity: Activity,
        intent: List<Intent>,
        schemeInfo: List<SchemeInfo>
    )

    fun shouldBlockJump(
        activity: Activity,
        activityClass: Class<out Activity>,
        scheme: Map<String, SchemeValue>?
    ): Boolean
}


open class QMUIDefaultSchemeIntentFactory : QMUISchemeIntentFactory {
    override fun factory(
        activity: Activity,
        activityClass: Class<out Activity>,
        scheme: Map<String, SchemeValue>?,
        origin: String
    ): Intent {
        val intent = Intent(activity, activityClass)
        intent.putExtra(QMUISchemeHandler.ARG_FROM_SCHEME, true)
        intent.putExtra(QMUISchemeHandler.ARG_ORIGIN_SCHEME, origin)
        if (scheme != null && scheme.isNotEmpty()) {
            for ((name, schemeValue) in scheme) {
                when (schemeValue.type) {
                    Integer.TYPE -> intent.putExtra(name, schemeValue.value as Int)
                    java.lang.Boolean.TYPE -> intent.putExtra(name, schemeValue.value as Boolean)
                    java.lang.Long.TYPE -> intent.putExtra(name, schemeValue.value as Long)
                    java.lang.Float.TYPE -> intent.putExtra(name, schemeValue.value as Float)
                    java.lang.Double.TYPE -> intent.putExtra(name, schemeValue.value as Double)
                    else -> intent.putExtra(name, schemeValue.origin)
                }
            }
        }
        return intent
    }

    override fun startActivities(activity: Activity, intent: List<Intent>, schemeInfo: List<SchemeInfo>) {
        if (intent.size == 1) {
            activity.startActivity(intent[0])
        } else {
            activity.startActivities(intent.toTypedArray())
        }
    }

    override fun shouldBlockJump(
        activity: Activity,
        activityClass: Class<out Activity?>,
        scheme: Map<String, SchemeValue>?
    ): Boolean {
        return false
    }
}