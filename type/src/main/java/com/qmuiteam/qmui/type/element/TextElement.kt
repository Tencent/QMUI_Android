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
package com.qmuiteam.qmui.type.element

import android.graphics.Canvas
import com.qmuiteam.qmui.type.TypeEnvironment

open class TextElement(text: CharSequence, index: Int, start: Int) : Element(text, index, start) {

    override fun onMeasure(env: TypeEnvironment) {
        val paint = env.paint
        setMeasureDimen((paint.measureText(text, 0, text.length) + 0.5f).toInt(),
                paint.fontMetricsInt.descent - paint.fontMetricsInt.ascent,
                -paint.fontMetricsInt.ascent)
    }

    override fun onDraw(env: TypeEnvironment, canvas: Canvas) {
        drawBg(env, canvas)
        canvas.drawText(text, 0, text.length, x.toFloat(), (y + baseLine).toFloat(), env.paint)
        drawBorder(env, canvas)
    }
}