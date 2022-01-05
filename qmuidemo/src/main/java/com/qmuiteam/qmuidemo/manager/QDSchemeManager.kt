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
package com.qmuiteam.qmuidemo.manager

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.qmuiteam.qmui.arch.QMUISwipeBackActivityManager
import com.qmuiteam.qmui.arch.scheme.QMUISchemeHandler
import com.qmuiteam.qmui.arch.scheme.QMUISchemeHandlerInterceptor
import com.qmuiteam.qmui.arch.scheme.QMUISchemeParamValueDecoder
import com.qmuiteam.qmui.arch.scheme.SchemeInfo

class QDSchemeManager private constructor() {

    companion object {
        private const val TAG = "QDSchemeManager"
        const val SCHEME_PREFIX = "qmui://"

        @JvmStatic
        val instance by lazy { QDSchemeManager() }
    }

    private val schemeHandler = QMUISchemeHandler.Builder(SCHEME_PREFIX).apply {
        blockSameSchemeTimeout = 1000
        interceptorList.add(object : QMUISchemeHandlerInterceptor {
            override fun intercept(
                schemeHandler: QMUISchemeHandler,
                activity: Activity,
                schemes: List<SchemeInfo>
            ): Boolean {
                // Log the scheme.
                val sb = StringBuilder()
                for (scheme in schemes) {
                    sb.append(scheme.origin)
                    sb.append(";")
                }
                Log.i(TAG, "handle scheme: $sb")
                return false
            }
        })
        interceptorList.add(QMUISchemeParamValueDecoder())
    }.build()

    fun handle(scheme: String): Boolean {
        if (!schemeHandler.handle(scheme)) {
            Log.i(TAG, "scheme can not be handled: $scheme")
            Toast.makeText(
                QMUISwipeBackActivityManager.getInstance().currentActivity,
                "scheme can not be handled: $scheme", Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    fun handleMuti(schemes:List<String>): Boolean {
        if(!schemeHandler.handleSchemes(schemes)){
            Log.i(TAG, "scheme can not be handled: ${schemes.joinToString(",")}")
            Toast.makeText(
                QMUISwipeBackActivityManager.getInstance().currentActivity,
                "scheme can not be handled: ${schemes.joinToString(",")}", Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }
}