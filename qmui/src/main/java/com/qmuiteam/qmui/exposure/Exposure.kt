package com.qmuiteam.qmui.exposure

import android.view.View


enum class ExposureType {
    first, dataChange, repeat
}

interface Exposure {
    fun same(data: Exposure): Boolean
    fun expose(view: View, type: ExposureType)
}



class SimpleExposure(val key: Any?, val block: (type: ExposureType) -> Unit) : Exposure {
    override fun same(data: Exposure): Boolean {
        return data is SimpleExposure && data.key == key
    }

    override fun expose(view: View, type: ExposureType) {
        block(type)
    }
}