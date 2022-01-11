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
import android.os.Bundle
import com.qmuiteam.qmui.arch.QMUIFragment
import com.qmuiteam.qmui.arch.QMUIFragmentActivity
import com.qmuiteam.qmui.arch.annotation.FragmentContainerParam
import java.util.*

class SchemeHandleContext(val activity: Activity) {

    val intentList: MutableList<Intent> = ArrayList()
    val fragmentList: MutableList<FragmentAndArg> = ArrayList()

    var buildingIntent: Intent? = null
    var buildingActivityClass: Class<out Activity> = activity::class.java
    var shouldFinishCurrent = false

    private var schemeIntentFactory: QMUISchemeIntentFactory? = null
    private var schemeFragmentFactory: QMUISchemeFragmentFactory? = null

    fun startActivities(schemeInfo: List<SchemeInfo>): Boolean {
        flushFragment()
        if (intentList.isEmpty()) {
            return false
        }
        intentList.forEachIndexed { index, intent ->
            intent.putExtra(QMUIFragmentActivity.QMUI_MUTI_START_INDEX, index)
        }
        schemeFragmentFactory?.let {
            it.startActivities(activity, intentList, schemeInfo)
            return true
        }
        schemeIntentFactory?.let {
            it.startActivities(activity, intentList, schemeInfo)
            return true
        }
        return false
    }

    fun canUseRefresh(): Boolean {
        return intentList.isEmpty() && fragmentList.isEmpty()
    }

    fun pushActivity(cls: Class<out Activity>, intent: Intent, factory: QMUISchemeIntentFactory) {
        flushFragment()
        intentList.add(intent)
        schemeIntentFactory = factory
        schemeFragmentFactory = null
        buildingActivityClass = cls
    }

    private fun flushFragment() {
        if (fragmentList.isNotEmpty()) {
            val intent = buildingIntent ?: Intent(activity, buildingActivityClass).apply {
                putExtras(activity.intent)
            }.let {
                fragmentList.first().factory.proxy(it)
            }
            val fragmentListArg = arrayListOf<Bundle>()
            fragmentList.forEach {
                fragmentListArg.add(Bundle().apply {
                    putString(QMUIFragmentActivity.QMUI_INTENT_DST_FRAGMENT_NAME, it.fragmentClass.name)
                    putBundle(QMUIFragmentActivity.QMUI_INTENT_FRAGMENT_ARG, it.arg)
                })
            }
            intent.putParcelableArrayListExtra(QMUIFragmentActivity.QMUI_INTENT_FRAGMENT_LIST_ARG, fragmentListArg)
            intentList.add(intent)
            buildingIntent = null
            fragmentList.clear()
        }
    }

    fun flushAndBuildFirstFragment(
        activityClsList: Array<Class<out QMUIFragmentActivity>>,
        params: Map<String, SchemeValue>?,
        fragmentAndArg: FragmentAndArg
    ): Boolean {
        flushFragment()
        for (target in activityClsList) {
            val intent = buildIntentForFragment(target, params)
            if (intent != null) {
                buildingIntent = fragmentAndArg.factory.proxy(intent)
                buildingActivityClass = target
                pushFragment(fragmentAndArg)
                return true
            }
        }
        return false
    }


    fun pushFragment(fragmentAndArg: FragmentAndArg) {
        fragmentList.add(fragmentAndArg)
        schemeIntentFactory = null
        schemeFragmentFactory = fragmentAndArg.factory
    }

    private fun buildIntentForFragment(
        activityCls: Class<out QMUIFragmentActivity>,
        params: Map<String, SchemeValue>?
    ): Intent? {
        val intent = Intent(activity, activityCls)
        intent.putExtra(QMUISchemeHandler.ARG_FROM_SCHEME, true)
        val fragmentContainerParam = activityCls.getAnnotation(FragmentContainerParam::class.java) ?: return intent
        val required: Array<String> = fragmentContainerParam.required
        val any: Array<String> = fragmentContainerParam.any
        val optional: Array<String> = fragmentContainerParam.optional
        if (required.isEmpty() && any.isEmpty()) {
            putOptionalSchemeValuesToIntent(intent, params, optional)
            return intent
        }
        if (params == null || params.isEmpty()) {
            // not matched.
            return null
        }
        if (required.isNotEmpty()) {
            for (arg in required) {
                val value = params[arg] ?: return null // not matched.
                putSchemeValueToIntent(intent, arg, value)
            }
        }
        if (any.isNotEmpty()) {
            var hasAny = false
            for (arg in any) {
                val value = params[arg]
                if (value != null) {
                    putSchemeValueToIntent(intent, arg, value)
                    hasAny = true
                }
            }
            if (!hasAny) {
                return null
            }
        }
        putOptionalSchemeValuesToIntent(intent, params, optional)
        return intent
    }


    private fun putOptionalSchemeValuesToIntent(
        intent: Intent,
        scheme: Map<String, SchemeValue>?,
        optional: Array<String>
    ) {
        if (scheme == null || scheme.isEmpty()) {
            return
        }
        for (arg in optional) {
            val value = scheme[arg]
            value?.let { putSchemeValueToIntent(intent, arg, it) }
        }
    }

    private fun putSchemeValueToIntent(intent: Intent, arg: String, value: SchemeValue) {
        when (value.type) {
            java.lang.Boolean.TYPE -> intent.putExtra(arg, value.value as Boolean)
            Integer.TYPE -> intent.putExtra(arg, value.value as Int)
            java.lang.Long.TYPE -> intent.putExtra(arg, value.value as Long)
            java.lang.Float.TYPE -> intent.putExtra(arg, value.value as Float)
            java.lang.Double.TYPE -> intent.putExtra(arg, value.value as Double)
            else -> intent.putExtra(arg, value.origin)
        }
    }
}

class FragmentAndArg(
    val fragmentClass: Class<out QMUIFragment>,
    val arg: Bundle?,
    val factory: QMUISchemeFragmentFactory
)
