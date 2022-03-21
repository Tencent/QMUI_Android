package com.qmuiteam.editor

import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

interface EditorBehavior {
    fun apply(value: TextFieldValue): TextFieldValue
}

internal fun String.isHeaderTag(): Boolean{
    return HeaderLevel.values().find { it.tag == this } != null
}

class Bold(val weight: Int) : EditorBehavior {
    override fun apply(value: TextFieldValue): TextFieldValue {
        return value.bold(weight)
    }
}

object Quote : EditorBehavior {

    const val tag = "quote"

    override fun apply(value: TextFieldValue): TextFieldValue {
        return value.quote()
    }
}


object UnOrderList : EditorBehavior {

    const val tag = "ul"

    override fun apply(value: TextFieldValue): TextFieldValue {
        return value.unOrder()
    }
}

class Header(val level: HeaderLevel): EditorBehavior {

    override fun apply(value: TextFieldValue): TextFieldValue {
        return value.header(level)
    }
}

enum class HeaderLevel(val tag: String, val fontSize: TextUnit) {
    h1("h1", 24.sp),
    h2("h2", 22.sp),
    h3("h3", 20.sp),
    h4("h4", 18.sp),
    h5("h5", 16.sp)
}
