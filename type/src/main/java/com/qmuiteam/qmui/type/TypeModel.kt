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
package com.qmuiteam.qmui.type

import android.graphics.Typeface
import com.qmuiteam.qmui.type.element.Element
import java.util.*

class TypeModel(
        val origin: CharSequence,
        private val mElementMap: Map<Int, Element>,
        private val mFirstElement: Element,
        private val mLastElement: Element) {

    var firstEffect: Element? = null

    fun addTypefaceEffect(start: Int, end: Int, typeface: Typeface): EffectRemover? {
        val types: MutableList<Int> = ArrayList()
        types.add(TypeEnvironment.TYPE_TYPEFACE)
        return unsafeAddEffect(start, end, types, object : EnvironmentUpdater {
            override fun update(env: TypeEnvironment) {
                env.typeface = typeface
            }
        })
    }

    fun addTextSizeEffect(start: Int, end: Int, textSize: Float): EffectRemover? {
        val types: MutableList<Int> = ArrayList()
        types.add(TypeEnvironment.TYPE_TEXT_SIZE)
        return unsafeAddEffect(start, end, types, object : EnvironmentUpdater {
            override fun update(env: TypeEnvironment) {
                env.textSize = textSize
            }
        })
    }

    fun addBgEffect(start: Int, end: Int, bgColor: Int): EffectRemover? {
        val types: MutableList<Int> = ArrayList()
        types.add(TypeEnvironment.TYPE_BG_COLOR)
        return unsafeAddEffect(start, end, types, object : EnvironmentUpdater {
            override fun update(env: TypeEnvironment) {
                env.backgroundColor = bgColor
            }
        })
    }

    fun addTextColorEffect(start: Int, end: Int, textColor: Int): EffectRemover? {
        val types: MutableList<Int> = ArrayList()
        types.add(TypeEnvironment.TYPE_TEXT_COLOR)
        return unsafeAddEffect(start, end, types, object : EnvironmentUpdater {
            override fun update(env: TypeEnvironment) {
                env.textColor = textColor
            }
        })
    }

    fun addUnderLineEffect(start: Int, end: Int, underLineColor: Int, underLineHeight: Int): EffectRemover? {
        val types: MutableList<Int> = ArrayList()
        types.add(TypeEnvironment.TYPE_BORDER_BOTTOM_WIDTH)
        types.add(TypeEnvironment.TYPE_BORDER_BOTTOM_COLOR)
        return unsafeAddEffect(start, end, types, object : EnvironmentUpdater {
            override fun update(env: TypeEnvironment) {
                env.setBorderBottom(underLineHeight, underLineColor)
            }
        })
    }

    fun unsafeAddEffect(start: Int, end: Int, types: List<Int>, environmentUpdater: EnvironmentUpdater): EffectRemover? {
        val elementStart = mElementMap[start]
        val elementEnd = mElementMap[end]
        if (elementStart == null || elementEnd == null) {
            return null
        }
        for (type in types) {
            elementStart.addSaveType(type)
            elementEnd.addRestoreType(type)
        }
        elementStart.addEnvironmentUpdater(environmentUpdater)
        firstEffect = if (firstEffect == null) {
            elementStart
        } else {
            elementStart.insertEffectTo(firstEffect!!)
        }
        firstEffect = elementEnd.insertEffectTo(firstEffect!!)
        return DefaultEffectRemove(this, start, end, types, environmentUpdater)
    }

    fun unsafeRemoveEffect(start: Int, end: Int, types: List<Int>, environmentUpdater: EnvironmentUpdater): Boolean {
        val elementStart = mElementMap[start]
        val elementEnd = mElementMap[end]
        if (elementStart == null || elementEnd == null) {
            return false
        }
        for (type in types) {
            elementStart.removeSaveType(type)
            elementEnd.removeStoreType(type)
        }
        elementStart.removeEnvironmentUpdater(environmentUpdater)
        firstEffect = elementStart.removeFromEffectListIfNeeded(firstEffect)
        firstEffect = elementEnd.removeFromEffectListIfNeeded(firstEffect)
        return true
    }

    fun firstElement(): Element {
        return mFirstElement
    }

    fun lastElement(): Element {
        return mLastElement
    }

    operator fun get(pos: Int): Element? {
        return mElementMap[pos]
    }

    interface EffectRemover {
        fun remove()
    }
}

class DefaultEffectRemove(
        private val typeModel: TypeModel,
        private val start: Int,
        private val end: Int,
        private val types: List<Int>,
        private val environmentUpdater: EnvironmentUpdater) : TypeModel.EffectRemover {
    override fun remove() {
        typeModel.unsafeRemoveEffect(start, end, types, environmentUpdater)
    }
}