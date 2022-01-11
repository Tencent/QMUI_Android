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

import android.net.Uri
import android.util.ArrayMap
import java.util.*

class QMUISchemeBuilder(
    private val prefix: String,
    private val action: String,
    private val encodeParams: Boolean
) {

    companion object {
        fun from(prefix: String, action: String, params: String?, encodeNewParams: Boolean): QMUISchemeBuilder {
            val builder = QMUISchemeBuilder(prefix, action, encodeNewParams)
            val paramsMap = HashMap<String, String>()
            parseParamsToMap(params, paramsMap)
            if (paramsMap.isNotEmpty()) {
                builder.params.putAll(paramsMap)
            }
            return builder
        }
    }

    private val params = ArrayMap<String, String>()

    fun param(name: String, value: String): QMUISchemeBuilder {
        if (encodeParams) {
            params[name] = Uri.encode(value)
        } else {
            params[name] = value
        }
        return this
    }

    fun param(name: String, value: Int): QMUISchemeBuilder {
        params[name] = value.toString()
        return this
    }

    fun param(name: String, value: Boolean): QMUISchemeBuilder {
        params[name] = if (value) "1" else "0"
        return this
    }

    fun param(name: String, value: Long): QMUISchemeBuilder {
        params[name] = value.toString()
        return this
    }

    fun param(name: String, value: Float): QMUISchemeBuilder {
        params[name] = value.toString()
        return this
    }

    fun param(name: String, value: Double): QMUISchemeBuilder {
        params[name] = value.toString()
        return this
    }

    fun finishCurrent(finishCurrent: Boolean): QMUISchemeBuilder {
        params[QMUISchemeHandler.ARG_FINISH_CURRENT] = if (finishCurrent) "1" else "0"
        return this
    }

    fun forceToNewActivity(forceNew: Boolean): QMUISchemeBuilder {
        params[QMUISchemeHandler.ARG_FORCE_TO_NEW_ACTIVITY] = if (forceNew) "1" else "0"
        return this
    }

    fun build(): String {
        val builder = StringBuilder()
        builder.append(prefix)
        builder.append(action)
        builder.append("?")
        for (i in 0 until params.size) {
            if (i != 0) {
                builder.append("&")
            }
            builder.append(params.keyAt(i))
            builder.append("=")
            builder.append(params.valueAt(i))
        }
        return builder.toString()
    }
}