package com.qmuiteam.compose.core.helper

interface LogTag {
    val TAG: String
        get() = getTag(javaClass)
}

fun logTag(clazz: Class<*>): LogTag = object : LogTag {
    override val TAG = getTag(clazz)
}

inline fun <reified T: Any> logTag(): LogTag = logTag(T::class.java)

private fun getTag(clazz: Class<*>): String {
    val tag = clazz.simpleName
    return if (tag.length <= 23) {
        tag
    } else {
        tag.substring(0, 23)
    }
}