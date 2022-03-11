package com.qmuiteam.qmui.exposure


enum class ExposureType {
    first, dataChange, repeat
}

interface Exposure {
    fun same(data: Exposure): Boolean
    fun expose(type: ExposureType)
}

class SimpleExposure(val block: (type: ExposureType) -> Unit) : Exposure {
    override fun same(data: Exposure): Boolean {
        return data is SimpleExposure
    }

    override fun expose(type: ExposureType) {
        block(type)
    }
}