package com.qmuiteam.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

interface EditorBehavior {
    fun apply(value: TextFieldValue): TextFieldValue
}

internal fun String.isHeaderTag(): Boolean{
    return HeaderLevel.values().find { it.tag == this } != null
}

internal fun String.isBoldTag(): Boolean{
    return startsWith(BoldBehavior.prefix)
}

class BoldBehavior(val weight: Int = 700) : EditorBehavior {

    companion object {
        val prefix = "blod"
    }

    val tag: String = "$prefix:$weight"

    override fun apply(value: TextFieldValue): TextFieldValue {
        return value.bold(this)
    }
}

class StopBehavior(val target: String): EditorBehavior {
    companion object {
        val prefix = "stop"
    }

    val tag: String = "${prefix}:$target"

    override fun apply(value: TextFieldValue): TextFieldValue {
        return value
    }
}

class TextColorBehavior(val color: Color = Color.White) : EditorBehavior {

    companion object {
        val prefix = "color"
    }

    val tag: String = "$prefix:$color"

    override fun apply(value: TextFieldValue): TextFieldValue {
        return value.textColor(this)
    }
}

object NormalParagraphBehavior: EditorBehavior {

    const val tag = "p"

    override fun apply(value: TextFieldValue): TextFieldValue {
        return value.quote()
    }
}

object QuoteBehavior : EditorBehavior {

    const val tag = "quote"

    override fun apply(value: TextFieldValue): TextFieldValue {
        return value.quote()
    }
}


object UnOrderListBehavior : EditorBehavior {

    const val tag = "ul"

    override fun apply(value: TextFieldValue): TextFieldValue {
        return value.unOrder()
    }
}

class HeaderBehavior(val level: HeaderLevel): EditorBehavior {

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
