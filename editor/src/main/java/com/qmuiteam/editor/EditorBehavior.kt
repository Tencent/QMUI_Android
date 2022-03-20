package com.qmuiteam.editor

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue

interface EditorBehavior {
    fun apply(value: TextFieldValue): TextFieldValue
}


class Bold(val weight: Int) : EditorBehavior {
    override fun apply(value: TextFieldValue): TextFieldValue {
        return value.bold(weight)
    }
}

class Quote : EditorBehavior {
    override fun apply(value: TextFieldValue): TextFieldValue {
        return value.quote()
    }
}