package com.qmuiteam.qmui.exposure

import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.ViewTreeObserver
import android.widget.AbsListView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.qmuiteam.qmui.R
import com.qmuiteam.qmui.kotlin.debounceRun
import com.qmuiteam.qmui.widget.tab.QMUIBasicTabSegment

/**
 *  Exposure 使用：
 *  1. 使用场景：
 *     a. 简单使用：simpleExposure(key=xxx, ...)
 *     b. 复杂使用， view 初始化时 registerExposure(...)， 渲染数据时 bindExposure(Exposure)
 *     c. 和 RecyclerView/ListView 配合，onBindViewHolder 时：simpleExposure(key=xxx, ...)， 或者在 onCreateViewHolder 时 registerExposure(...)，
 *        onBindViewHolder 时 bindExposure(Exposure)
 *     d. 有自定义 View 复用逻辑的容器，同 c, 但 ViewGroup 需要调用 setToRecyclerContainer()
 *     e. 如果子 View 需要在父 View 已曝光的前提下才能认为是曝光， 那么父容器需要调用 setSelfExposedWhenDescendantExposed()
 *
 *  2. Exposure 类
 *     曝光所用的数据类，使用者需要自定义，框架通过 same(Exposure) 判断数据是否变更而觉得是否需要重新曝光， RecyclerView 复用排重也依赖于它
 *     框架在满足曝光时触发 expose() 方法
 *
 *  3. 可配置项：
 *     holdTime -> 需要在可视区域停留超过 holdTime 后才算曝光， 默认 600ms
 *     debounceTimeout -> debounce 处理，防止界面多次 layout / scroll 不停触发曝光检查， 默认 400ms
 *     containerProvider -> 在 containerProvider 提供的 ViewGroup 里可视才算曝光，默认是整个界面的 rootView
 *     exposureChecker -> 曝光检查器，默认是可视面积超过自身总面积的 80% 算可见
 */

fun View.simpleExposure(
    holdTime: Long = 600,
    debounceTimeout: Long = 400,
    containerProvider: ExposureContainerProvider = DefaultExposureContainerProvider,
    exposureChecker: ExposureChecker = defaultExposureChecker,
    key: Any?,
    doExpose: (type: ExposureType) -> Unit
) {
    registerExposure(holdTime, debounceTimeout, containerProvider, exposureChecker)
    bindExposure(SimpleExposure(key) {
        doExpose(it)
    })
}

fun View.exposure(
    holdTime: Long = 600,
    debounceTimeout: Long = 400,
    containerProvider: ExposureContainerProvider = DefaultExposureContainerProvider,
    exposureChecker: ExposureChecker = fullExposureChecker,
    exposure: Exposure
){
    registerExposure(holdTime, debounceTimeout, containerProvider, exposureChecker)
    bindExposure(exposure)
}

fun View.registerExposure(
    holdTime: Long = 600,
    debounceTimeout: Long = 400,
    containerProvider: ExposureContainerProvider = DefaultExposureContainerProvider,
    exposureChecker: ExposureChecker = fullExposureChecker
) {
    setTag(R.id.qmui_exposure_config, true)
    var attachListener = getTag(R.id.qmui_exposure_register) as? View.OnAttachStateChangeListener
    if(attachListener != null){
        return
    }
    attachListener = object : View.OnAttachStateChangeListener {
        private val onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            checkExposure(holdTime, debounceTimeout, containerProvider, exposureChecker)
        }

        private val onScrollListener = ViewTreeObserver.OnScrollChangedListener {
            checkExposure(holdTime, debounceTimeout, containerProvider, exposureChecker)
        }

        private val customTriggerListener = CustomExposureTriggerListener {
            checkExposure(holdTime, debounceTimeout, containerProvider, exposureChecker)
        }

        override fun onViewAttachedToWindow(v: View?) {
            checkExposure(holdTime, debounceTimeout, containerProvider, exposureChecker)
            viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
            viewTreeObserver.addOnScrollChangedListener(onScrollListener)
            containerProvider.provide(this@registerExposure)?.let { container ->
                var exposureCheck = container.getTag(R.id.qmui_exposure_custom_check_trigger) as? CustomExposureTrigger
                if(exposureCheck == null){
                    exposureCheck = CustomExposureTrigger().also {
                        container.setTag(R.id.qmui_exposure_custom_check_trigger, it)
                    }
                }
                exposureCheck.addListener(customTriggerListener)
            }
        }

        override fun onViewDetachedFromWindow(v: View?) {
            viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
            viewTreeObserver.removeOnScrollChangedListener(onScrollListener)
            containerProvider.provide(this@registerExposure)?.let { container ->
                (container.getTag(R.id.qmui_exposure_custom_check_trigger) as? CustomExposureTrigger)?.removeListener(customTriggerListener)
            }
            clearExposureHolder()
            clearExposureDebounce()
            doUnExpose()
        }

    }
    setTag(R.id.qmui_exposure_register, attachListener)
    addOnAttachStateChangeListener(attachListener)
    if(isAttachedToWindow){
        attachListener.onViewAttachedToWindow(this)
    }
}

fun View.unregisterExposure(){
    setTag(R.id.qmui_exposure_config, false)
    val attachListener = getTag(R.id.qmui_exposure_register) as? View.OnAttachStateChangeListener
    if(attachListener != null){
        removeOnAttachStateChangeListener(attachListener)
        attachListener.onViewDetachedFromWindow(this)
        setTag(R.id.qmui_exposure_register, null)
    }
}

fun View.bindExposure(exposure: Exposure) {
    setTag(R.id.qmui_exposure_data, exposure)
}

fun View.isInExposure(): Boolean {
    return getTag(R.id.qmui_exposure_ing) as? Boolean ?: false
}

fun View.setToRecyclerContainer() {
    setTag(R.id.qmui_exposure_is_recycler_container, true)
}

fun ViewGroup.setSelfExposedWhenDescendantExposed(need: Boolean) {
    if(need){
        setTag(R.id.qmui_exposure_parent_expose_request, ParentExposedRequestExposureEffect(this))
    }else{
        setTag(R.id.qmui_exposure_parent_expose_request, null)
    }

}

fun ViewGroup.customConfigRecyclerExposureEffect(effect: RecyclerExposureEffect) {
    setTag(R.id.qmui_exposure_recycler_collection, effect)
}

fun View.triggerCustomExposureChecker(
    containerProvider: ExposureContainerProvider = DefaultExposureContainerProvider
) {
    if(!isAttachedToWindow){
        return
    }
    (containerProvider.provide(this)?.getTag(R.id.qmui_exposure_custom_check_trigger) as? CustomExposureTrigger)?.trigger()
}

fun View.defaultCanExpose(): Boolean {
    if (!isAttachedToWindow) {
        return false
    }
    if (windowVisibility != View.VISIBLE) {
        return false
    }
    if (visibility != View.VISIBLE) {
        return false
    }
    var p: ViewParent? = parent
    while (p != null && p is ViewGroup) {
        if (p.visibility != View.VISIBLE) {
            return false
        }
        p = p.parent
    }
    return true
}

fun View.checkExposure(
    holdTime: Long = 1000,
    debounceTimeout: Long = 500,
    containerProvider: ExposureContainerProvider = DefaultExposureContainerProvider,
    exposureChecker: ExposureChecker = fullExposureChecker,
) {
    val holderRunnable = getTag(R.id.qmui_exposure_holder) as? Runnable
    if (holderRunnable != null) {
        return
    }
    debounceRun(R.id.qmui_exposure_debounce, debounceTimeout) {
        val container = containerProvider.provide(this) ?: return@debounceRun
        val isInExposure = isInExposure()
        if (checkIsExposure(container, exposureChecker)) {
            if (!isInExposure || checkIsExposureDataChanged()) {
                val runnable = Runnable {
                    setTag(R.id.qmui_exposure_holder, null)
                    if (checkIsExposure(container, exposureChecker)) {
                        val data = getTag(R.id.qmui_exposure_data) as? Exposure ?: return@Runnable
                        val last = getTag(R.id.qmui_exposure_last_data) as? Exposure
                        val type = when {
                            last == null -> ExposureType.first
                            !last.same(data) -> ExposureType.dataChange
                            else -> ExposureType.repeat
                        }
                        if (doExpose(container, data, last, type)) {
                            setTag(R.id.qmui_exposure_ing, true)
                            setTag(R.id.qmui_exposure_last_data, data)
                        }
                    }
                }.also {
                    setTag(R.id.qmui_exposure_holder, it)
                }
                postDelayed(runnable, holdTime)
            }

        } else if (isInExposure) {
            doUnExpose()
        }
    }
}

private fun View.checkIsExposureDataChanged(): Boolean {
    val data = getTag(R.id.qmui_exposure_data) as? Exposure ?: return false
    val last = getTag(R.id.qmui_exposure_last_data) as? Exposure
    return last == null || !last.same(data)
}

private fun View.checkIsExposure(
    container: ViewGroup,
    exposureChecker: ExposureChecker = fullExposureChecker
): Boolean {
    if (!exposureChecker.canExpose(this)) {
        return false
    }
    return exposureChecker.isExposedInContainer(container, this)
}

internal fun View.clearExposureHolder() {
    (getTag(R.id.qmui_exposure_holder) as? Runnable)?.let {
        removeCallbacks(it)
        setTag(R.id.qmui_exposure_holder, null)
    }
}

internal fun View.clearExposureDebounce() {
    (getTag(R.id.qmui_exposure_debounce) as? Runnable)?.let {
        removeCallbacks(it)
        setTag(R.id.qmui_exposure_debounce, null)
    }
}


internal fun View.doExpose(
    container: ViewGroup,
    exposure: Exposure,
    lastExposure: Exposure?,
    exposureType: ExposureType
): Boolean {
    var p = parent as? ViewGroup
    val exposureList = mutableListOf<ExposureEffect>()
    var effectResult = EffectResult.pass
    while (p != null && p != container) {
        val parentAlready = p.getTag(R.id.qmui_exposure_parent_expose_request) as? ParentExposedRequestExposureEffect
        if (parentAlready != null) {
            exposureList.add(parentAlready)
            val ret = parentAlready.doBeforeExpose(this, container, exposure, lastExposure, exposureType)
            if (ret != EffectResult.pass) {
                effectResult = ret
                break
            }
        }
        if (parent == p &&
            (p is RecyclerView ||
                    p is AbsListView ||
                    p is QMUIBasicTabSegment ||
                    p is ViewPager ||
                    p.getTag(R.id.qmui_exposure_is_recycler_container) == true)
        ) {
            var recyclerEffect = p.getTag(R.id.qmui_exposure_recycler_collection) as? RecyclerExposureEffect
            if (recyclerEffect == null) {
                recyclerEffect = RecyclerExposureEffect(p)
                p.setTag(R.id.qmui_exposure_recycler_collection, recyclerEffect)
            }
            exposureList.add(recyclerEffect)
            val ret = recyclerEffect.doBeforeExpose(this, container, exposure, lastExposure, exposureType)
            if (ret != EffectResult.pass) {
                effectResult = ret
                break
            }
        }

        val customEffect = p.getTag(R.id.qmui_exposure_custom_effect) as? ExposureEffect
        if (customEffect != null) {
            exposureList.add(customEffect)
            val ret = customEffect.doBeforeExpose(this, container, exposure, lastExposure, exposureType)
            if (ret != EffectResult.pass) {
                effectResult = ret
                break
            }
        }

        p = p.parent as? ViewGroup
    }
    setTag(R.id.qmui_exposure_effect_list, ExposureEffectList(container, exposureList))
    if (effectResult == EffectResult.pass) {
        exposure.expose(this, exposureType)
        effectResult = EffectResult.handled
    }
    return effectResult == EffectResult.handled
}

internal fun View.doUnExpose() {
    if (isInExposure()) {
        setTag(R.id.qmui_exposure_ing, false)
        val exposure = getTag(R.id.qmui_exposure_data) as? Exposure ?: return
        (getTag(R.id.qmui_exposure_effect_list) as? ExposureEffectList)?.let {
            it.effectList.forEach { effect ->
                effect.doAfterUnExpose(this, it.container, exposure)
            }
        }
    }
}

