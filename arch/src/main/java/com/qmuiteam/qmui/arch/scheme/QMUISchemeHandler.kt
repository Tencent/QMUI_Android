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

import com.qmuiteam.qmui.QMUILog
import com.qmuiteam.qmui.arch.QMUIFragmentActivity
import com.qmuiteam.qmui.arch.QMUISwipeBackActivityManager
import java.util.*

class QMUISchemeHandler private constructor(builder: Builder) {
    companion object {
        const val TAG = "QMUISchemeHandler"
        const val ARG_FROM_SCHEME = "__qmui_arg_from_scheme"
        const val ARG_ORIGIN_SCHEME = "__qmui_arg_origin_scheme"
        const val ARG_FORCE_TO_NEW_ACTIVITY = "__qmui_force_to_new_activity"
        const val ARG_FINISH_CURRENT = "__qmui_finish_current"
        private var sSchemeMap: SchemeMap? = null

        init {
            try {
                val cls = Class.forName(SchemeMap::class.java.name + "Impl")
                sSchemeMap = cls.newInstance() as SchemeMap
            } catch (e: ClassNotFoundException) {
                sSchemeMap = object : SchemeMap {
                    override fun findScheme(handler: QMUISchemeHandler, schemeAction: String, params: Map<String, String>?): SchemeItem? {
                        return null
                    }

                    override fun exists(handler: QMUISchemeHandler, schemeAction: String): Boolean {
                        return false
                    }
                }
            } catch (e: IllegalAccessException) {
                throw RuntimeException(
                    "Can not access the Class SchemeMapImpl. " +
                            "Please file a issue to report this."
                )
            } catch (e: InstantiationException) {
                throw RuntimeException(
                    "Can not instance the Class SchemeMapImpl. " +
                            "Please file a issue to report this."
                )
            }
        }
    }

    val prefix: String = builder.prefix
    private var interpolatorList: List<QMUISchemeHandlerInterceptor> = builder.interceptorList
    private val blockSameSchemeTimeout = builder.blockSameSchemeTimeout
    val defaultIntentFactory = builder.defaultIntentFactory
    val defaultFragmentFactory = builder.defaultFragmentFactory
    val defaultSchemeMatcher = builder.defaultSchemeMatcher
    private val fallbackInterceptor = builder.fallbackInterceptor
    private val unKnownSchemeHandler = builder.unKnownSchemeHandler
    private var lastHandledScheme: List<String>? = null
    private var lastSchemeHandledTime: Long = 0


    fun getSchemeItem(action: String, params: Map<String, String>?): SchemeItem? {
        return sSchemeMap?.findScheme(this, action, params)
    }

    fun handle(scheme: String): Boolean {
        val list = ArrayList<String>(1)
        list.add(scheme)
        return handleSchemes(list)
    }

    fun handleSchemes(schemes: List<String>): Boolean {
        if (schemes.isEmpty()) {
            return false
        }
        for (scheme in schemes) {
            if (!scheme.startsWith(prefix)) {
                return false
            }
        }
        if (schemes == lastHandledScheme && System.currentTimeMillis() - lastSchemeHandledTime < blockSameSchemeTimeout) {
            return true
        }
        val currentActivity = QMUISwipeBackActivityManager.getInstance().currentActivity ?: return false
        val schemeInfoList = ArrayList<SchemeInfo>(schemes.size)
        for (schemeParam in schemes) {
            val scheme = schemeParam.substring(prefix.length)
            val elements: Array<String?> = scheme.split("\\?".toRegex()).toTypedArray()
            val action = elements[0]
            if (elements.isEmpty() || action == null || action.isEmpty()) {
                return false
            }
            val params = mutableMapOf<String, String>()
            if (elements.size > 1) {
                parseParamsToMap(elements[1], params)
            }
            schemeInfoList.add(SchemeInfo(action, params, scheme))
        }
        var handled = false
        if (interpolatorList.isNotEmpty()) {
            for (interpolator in interpolatorList) {
                if (interpolator.intercept(this, currentActivity, schemeInfoList)) {
                    handled = true
                    break
                }
            }
        }
        if (!handled) {
            var failed = false
            val handleContext = SchemeHandleContext(currentActivity)
            for (schemeInfo in schemeInfoList) {
                val schemeItem = sSchemeMap!!.findScheme(this, schemeInfo.action, schemeInfo.params)
                if (schemeItem == null) {
                    QMUILog.i(TAG, "findScheme failed: ${schemeInfo.origin}")
                    if(unKnownSchemeHandler != null && unKnownSchemeHandler.handle(this, handleContext, schemeInfo)){
                        continue
                    }
                    failed = true
                    break
                }
                schemeItem.appendDefaultParams(schemeInfo.params)
                if (!schemeItem.handle(this, handleContext, schemeInfo)) {
                    QMUILog.i(TAG, "handle scheme failed: ${schemeInfo.origin}")
                    failed = true
                    break
                }
            }
            if (!failed) {
                val fragmentList = handleContext.fragmentList
                val buildingIntent = handleContext.buildingIntent
                if (handleContext.intentList.isEmpty() && buildingIntent == null) {
                    val fragments = fragmentList.mapNotNull {
                        it.factory.factory(it.fragmentClass, it.arg)
                    }
                    if (fragments.size == fragmentList.size) {
                        if (handleContext.shouldFinishCurrent) {
                            if (fragmentList.size == 1) {
                                fragmentList.last().factory.startFragmentAndDestroyCurrent(
                                    handleContext.activity as QMUIFragmentActivity, fragments[0], schemeInfoList[0]
                                )
                                handled = true
                            } else {
                                QMUILog.e(TAG, "startFragmentAndDestroyCurrent not support muti fragments")
                            }
                        } else {
                            val commitId =
                                fragmentList.last().factory.startFragment(handleContext.activity as QMUIFragmentActivity, fragments, schemeInfoList)
                            handled = commitId >= 0
                        }
                    }
                } else {
                    handled = handleContext.startActivities(schemeInfoList)
                    if (handled && handleContext.shouldFinishCurrent) {
                        handleContext.activity.finish()
                    }
                }
            }
        }
        if (!handled && fallbackInterceptor != null) {
            handled = fallbackInterceptor.intercept(this, currentActivity, schemeInfoList)
        }
        if (handled) {
            lastHandledScheme = schemes
            lastSchemeHandledTime = System.currentTimeMillis()
        }
        return handled
    }

    class Builder(val prefix: String) {
        val interceptorList = mutableListOf<QMUISchemeHandlerInterceptor>()

        var blockSameSchemeTimeout = BLOCK_SAME_SCHEME_DEFAULT_TIMEOUT
        var defaultIntentFactory: Class<out QMUISchemeIntentFactory> = QMUIDefaultSchemeIntentFactory::class.java
        var defaultFragmentFactory: Class<out QMUISchemeFragmentFactory> = QMUIDefaultSchemeFragmentFactory::class.java
        var defaultSchemeMatcher: Class<out QMUISchemeMatcher> = QMUIDefaultSchemeMatcher::class.java
        var unKnownSchemeHandler: QMUIUnknownSchemeHandler? = null
        var fallbackInterceptor: QMUISchemeHandlerInterceptor? = null

        fun addInterceptor(interceptor: QMUISchemeHandlerInterceptor) {
            interceptorList.add(interceptor)
        }

        fun build(): QMUISchemeHandler {
            return QMUISchemeHandler(this)
        }

        companion object {
            const val BLOCK_SAME_SCHEME_DEFAULT_TIMEOUT: Long = 500
        }
    }
}