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

package com.qmuiteam.qmui.type.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import com.qmuiteam.qmui.type.TypeEnvironment

open class BaseTypeView(context: Context,
                        attrs: AttributeSet? = null,
                        defStyleAttr: Int = 0): View(context, attrs, defStyleAttr) {
    val environment = TypeEnvironment()

    var textSize: Float
        get() = environment.textSize
        set(value){
            throwIfRunning("setTextSize")
            if(environment.textSize != value){
                environment.textSize = value
                requestLayout()
            }
        }

    var textColor: Int
        get() = environment.textColor
        set(value){
            throwIfRunning("setTextColor")
            if(environment.textColor != value){
                environment.textColor = value
                invalidate()
            }
        }

    var typeface: Typeface?
        get() = environment.typeface
        set(value){
            throwIfRunning("setTypeface")
            if(environment.typeface != value){
                environment.typeface = value
                requestLayout()
            }
        }

    var lineSpace: Int
        get() = environment.lineSpace
        set(value){
            throwIfRunning("setLineSpace")
            if(environment.lineSpace != value){
                environment.lineSpace = value
                requestLayout()
            }
        }

    var lineHeight: Int
        get() = environment.lineHeight
        set(value){
            throwIfRunning("setLineHeight")
            if(environment.lineHeight != value){
                environment.lineHeight = value
                requestLayout()
            }
        }

    var paragraphSpace: Int
        get() = environment.paragraphSpace
        set(value){
            throwIfRunning("setParagraphSpace")
            if(environment.paragraphSpace != value){
                environment.paragraphSpace = value
                requestLayout()
            }
        }


    fun throwIfRunning(action: String) {
        if(environment.isRunning()){
            throw RuntimeException("can not perform $action when running.")
        }
    }
}