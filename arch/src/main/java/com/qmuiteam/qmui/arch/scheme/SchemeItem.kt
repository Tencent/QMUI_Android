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

import android.util.ArrayMap
import com.qmuiteam.qmui.QMUILog
import java.util.*

private val schemeMatchers by lazy {
    HashMap<Class<out QMUISchemeMatcher>, QMUISchemeMatcher>()
}
private val schemeValueConverters by lazy {
    HashMap<Class<out QMUISchemeValueConverter>, QMUISchemeValueConverter>()
}

abstract class SchemeItem(
    private val required: ArrayMap<String, String?>?,
    val isUseRefreshIfMatchedCurrent: Boolean,
    private val keysForInt: Array<String>?,
    private val keysForBool: Array<String>?,
    private val keysForLong: Array<String>?,
    private val keysForFloat: Array<String>?,
    private val keysForDouble: Array<String>?,
    private val defaultParams: Array<String>?,
    private val schemeMatcherCls: Class<out QMUISchemeMatcher>?,
    private val schemeValueConverterCls: Class<out QMUISchemeValueConverter>?
) {

    fun appendDefaultParams(schemeParams: MutableMap<String, String>?) {
        if(schemeParams == null || defaultParams == null){
            return
        }
        for (item in defaultParams) {
            if (item.isNotEmpty()) {
                val pair = item.split("=")
                if (pair.size == 2) {
                    if(!schemeParams.contains(pair[0])){
                        schemeParams[pair[0]] = pair[1]
                    }
                }
            }
        }
    }

    protected fun convertFrom(schemeParams: Map<String, String>?): Map<String, SchemeValue>? {

        if (schemeParams == null || schemeParams.isEmpty()) {
            return null
        }
        val queryMap = mutableMapOf<String, SchemeValue>()
        for ((name, value) in schemeParams) {
            if (name.isEmpty()) {
                continue
            }
            var usedValue = value
            if (schemeValueConverterCls != null) {
                var converter = schemeValueConverters[schemeValueConverterCls]
                if (converter == null) {
                    try {
                        converter = schemeValueConverterCls.newInstance()
                        schemeValueConverters[schemeValueConverterCls] = converter
                    } catch (e: Exception) {
                        QMUILog.printErrStackTrace(
                            QMUISchemeHandler.TAG, e,
                            "error to instance QMUISchemeValueConverter: %d", schemeValueConverterCls.simpleName
                        )
                    }
                }
                if (converter != null) {
                    usedValue = converter.convert(name, value, schemeParams)
                }
            }
            try {
                when {
                    keysForInt?.contains(name) == true -> {
                        queryMap[name] = SchemeValue(usedValue, Integer.valueOf(usedValue), Integer.TYPE)
                    }
                    isBoolKey(name) -> {
                        queryMap[name] = SchemeValue(usedValue, convertStringToBool(usedValue), java.lang.Boolean.TYPE)
                    }
                    keysForLong?.contains(name) == true -> {
                        queryMap[name] = SchemeValue(usedValue, java.lang.Long.valueOf(usedValue), java.lang.Long.TYPE)
                    }
                    keysForFloat?.contains(name) == true -> {
                        queryMap[name] = SchemeValue(usedValue, java.lang.Float.valueOf(usedValue), java.lang.Float.TYPE)
                    }
                    keysForDouble?.contains(name) == true -> {
                        queryMap[name] = SchemeValue(usedValue, java.lang.Double.valueOf(usedValue), java.lang.Double.TYPE)
                    }
                    else -> {
                        queryMap[name] = SchemeValue(usedValue, usedValue, String::class.java)
                    }
                }
            } catch (e: Exception) {
                QMUILog.printErrStackTrace(QMUISchemeHandler.TAG, e, "error to parse scheme param: %s = %s", name, value)
            }
        }
        return queryMap
    }

    private fun isBoolKey(name: String): Boolean {
        return QMUISchemeHandler.ARG_FORCE_TO_NEW_ACTIVITY == name || QMUISchemeHandler.ARG_FINISH_CURRENT == name ||
                keysForBool?.contains(name) == true
    }

    private fun convertStringToBool(text: String?): Boolean {
        return !(text.isNullOrBlank() || "0" == text || "false" == text.lowercase())
    }

    protected fun shouldFinishCurrent(scheme: Map<String, SchemeValue>?): Boolean {
        if (scheme == null || scheme.isEmpty()) {
            return false
        }
        val schemeValue = scheme[QMUISchemeHandler.ARG_FINISH_CURRENT]
        return schemeValue != null && schemeValue.type == java.lang.Boolean.TYPE && schemeValue.value as Boolean
    }

    private fun getSchemeMatcher(handler: QMUISchemeHandler): QMUISchemeMatcher? {
        var schemeMatcherCls = schemeMatcherCls
        if (schemeMatcherCls == null) {
            schemeMatcherCls = handler.defaultSchemeMatcher
        }
        var matcher = schemeMatchers[schemeMatcherCls]
        if (matcher == null) {
            try {
                matcher = schemeMatcherCls.newInstance()
                schemeMatchers[schemeMatcherCls] = matcher
            } catch (e: Exception) {
                QMUILog.printErrStackTrace(
                    QMUISchemeHandler.TAG, e,
                    "error to instance QMUISchemeMatcher: %d", schemeMatcherCls.simpleName
                )
            }
        }
        return matcher
    }

    // used by generated code(SchemeMapImpl)
    fun match(handler: QMUISchemeHandler, params: Map<String, String?>?): Boolean {
        val matcher = getSchemeMatcher(handler)
        return matcher?.match(this, params) ?: matchRequiredParam(params)
    }

    fun matchRequiredParam(params: Map<String, String?>?): Boolean {
        if (required == null || required.isEmpty()) {
            return true
        }
        if (params == null || params.isEmpty()) {
            return false
        }
        for (i in 0 until required.size) {
            val key = required.keyAt(i)
            if (!params.containsKey(key)) {
                return false
            }
            val value = required.valueAt(i)
                ?: // if no value. that means scheme must provide this key.
                continue
            val actual = params[key]
            if (actual == null || actual != value) {
                return false
            }
        }
        return true
    }

    abstract fun handle(handler: QMUISchemeHandler, handleContext: SchemeHandleContext, schemeInfo: SchemeInfo): Boolean
}