package com.qmuiteam.qmui.exposure

import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import com.qmuiteam.qmui.R

enum class EffectResult {
    pass, handled, unHandled
}

interface ExposureEffect {
    fun doBeforeExpose(
        target: View,
        container: ViewGroup,
        exposure: Exposure,
        lastExposure: Exposure?,
        type: ExposureType
    ): EffectResult

    fun doAfterUnExpose(
        target: View,
        container: ViewGroup,
        data: Exposure
    ){

    }
}

class ParentExposedRequestExposureEffect(val parent: ViewGroup) : ExposureEffect {
    override fun doBeforeExpose(
        target: View,
        container: ViewGroup,
        exposure: Exposure,
        lastExposure: Exposure?,
        type: ExposureType
    ): EffectResult {
        val isParentConfigSet = parent.getTag(R.id.qmui_exposure_config) as? Boolean ?: false
        if (!isParentConfigSet) {
            throw RuntimeException("You should config the exposure on parent($parent) for constraint effect.")
        }
        val holder = parent.getTag(R.id.qmui_exposure_holder) as? Runnable
        if (holder != null) {
            parent.removeCallbacks(holder)
            parent.setTag(R.id.qmui_exposure_holder, null)
            holder.run()
        }
        return if(parent.isInExposure()) EffectResult.pass else EffectResult.unHandled
    }
}


class RecyclerExposureEffect(
    val parent: ViewGroup,
    val safeDuration: Long = 500,
    val zombieDuration: Long = 2000
) : ExposureEffect {

    private val exposureSet = mutableSetOf<Pair<Exposure, Long>>()
    private val zombieSet = mutableSetOf<Pair<Exposure, Long>>()

    override fun doBeforeExpose(
        target: View,
        container: ViewGroup,
        exposure: Exposure,
        lastExposure: Exposure?,
        type: ExposureType
    ): EffectResult {
        clearZombie()
        if(type == ExposureType.dataChange){
            lastExposure?.also { last ->
                val exist = exposureSet.find { it.first.same(last) }
                if(exist == null || exist.second + safeDuration < SystemClock.elapsedRealtime()){
                    zombieSet.removeAll { it.first.same(last) }
                    zombieSet.add(last to SystemClock.elapsedRealtime())
                    if(exist != null){
                        exposureSet.removeAll { it.first.same(last) }
                    }
                }
            }
        }
        if(exposureSet.find { it.first.same(exposure) } != null){
            zombieSet.removeAll { it.first.same(exposure) }
            return EffectResult.handled
        }
        val zombie = zombieSet.find { it.first.same(exposure) }
        if(zombie != null){
            exposureSet.add(exposure to SystemClock.elapsedRealtime())
            zombieSet.remove(zombie)
            return EffectResult.handled
        }
        zombieSet.removeAll { it.first.same(exposure) }
        exposureSet.add(exposure to SystemClock.elapsedRealtime())
        return EffectResult.pass
    }

    override fun doAfterUnExpose(target: View, container: ViewGroup, data: Exposure) {
        clearZombie()
        zombieSet.removeAll { it.first.same(data) }
        zombieSet.add(data to SystemClock.elapsedRealtime())
        exposureSet.removeAll { it.first.same(data) }
    }

    private fun clearZombie(){
        val iterator = zombieSet.iterator()
        while (iterator.hasNext()){
            val next = iterator.next()
            if(next.second + zombieDuration < SystemClock.elapsedRealtime()){
                iterator.remove()
            }
        }
    }
}


internal class ExposureEffectList(
    val container: ViewGroup,
    val effectList: List<ExposureEffect>
)