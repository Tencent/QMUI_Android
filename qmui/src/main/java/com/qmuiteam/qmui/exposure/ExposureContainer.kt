package com.qmuiteam.qmui.exposure

import android.view.View
import android.view.ViewGroup

interface ExposureContainerProvider {
    fun provide(view: View): ViewGroup?
}

object DefaultExposureContainerProvider : ExposureContainerProvider {
    override fun provide(view: View): ViewGroup? {
        return view.rootView as? ViewGroup
    }
}