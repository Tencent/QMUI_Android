package com.qmuiteam.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.*
import com.qmuiteam.compose.core.ui.qmuiPrimaryColor
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch



interface EditorDecoration {
    @Composable
    fun Compose()
}

class QuoteDecoration(val rect: Rect) : EditorDecoration {
    @Composable
    override fun Compose() {
        key(this) {
            val dpRect = with(LocalDensity.current) {
                DpRect(rect.left.toDp(), rect.top.toDp(), rect.right.toDp(), rect.bottom.toDp())
            }
            Box(
                Modifier
                    .offset(dpRect.left, dpRect.top)
                    .width(dpRect.width)
                    .height(dpRect.height)
                    .background(Color.LightGray)
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Color.Gray)
                )
            }
        }
    }
}

@Composable
fun QMUIEditor(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    channel: Channel<EditorBehavior>,
    textStyle: TextStyle = TextStyle.Default,
    focusRequester: FocusRequester = remember {
        FocusRequester()
    },
    cursorBrush: Brush = SolidColor(qmuiPrimaryColor),
    onValueChange: (TextFieldValue) -> Unit
) {

    var textFieldValue by remember(value) {
        mutableStateOf(value.check())
    }

    var editorDecorations by remember {
        mutableStateOf(listOf<EditorDecoration>())
    }

    LaunchedEffect(key1 = value) {
        launch {
            while (isActive) {
                val behavior = channel.receive()
                textFieldValue = behavior.apply(textFieldValue)
            }
        }
    }

    // TODO Fix here, BasicTextField can scroll inner , but i can't read the scroll position.
    BoxWithConstraints(modifier) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            BasicTextField(
                value = textFieldValue,
                onTextLayout = {
                    val list = mutableListOf<EditorDecoration>()
                    it.layoutInput.text.paragraphStyles.forEach { paragraph ->
                        if (paragraph.tag == "quote") {
                            val start = it.multiParagraph.getBoundingBox(paragraph.start)
                            val end = it.multiParagraph.getBoundingBox(paragraph.end - 1)
                            list.add(
                                QuoteDecoration(
                                    Rect(
                                        0f,
                                        start.top,
                                        it.multiParagraph.width,
                                        end.bottom
                                    )
                                )
                            )
                        }
                    }
                    editorDecorations = list
                },
                onValueChange = {
                    textFieldValue = updateTextFieldValue(textFieldValue, it)
                    onValueChange(textFieldValue)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = this@BoxWithConstraints.maxHeight)
                    .focusRequester(focusRequester),
                textStyle = textStyle,
                cursorBrush = cursorBrush,
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        editorDecorations.forEach {
                            it.Compose()
                        }
                    }
                    innerTextField()
                }
            )
        }
    }
}

private fun TextFieldValue.check(): TextFieldValue{
    val paragraphs = mutableListOf<AnnotatedString.Range<ParagraphStyle>>()
    var currentIndex = 0
    var nextIndex = text.indexOf('\n')
    while (nextIndex >= 0){
        val exist = annotatedString.paragraphStyles.find { it.start == 0 && it.end == nextIndex+1}
        if(exist == null){
            paragraphs.add(AnnotatedString.Range(ParagraphStyle(), currentIndex, nextIndex+1, "p"))
        }else{
            paragraphs.add(exist)
        }
        currentIndex = nextIndex+1
        nextIndex = text.indexOf('\n', currentIndex)
    }

    if(currentIndex < text.length){
        val exist = annotatedString.paragraphStyles.find { it.start == 0 && it.end == text.length}
        if(exist == null){
            paragraphs.add(AnnotatedString.Range(ParagraphStyle(), currentIndex, text.length, "p"))
        }else{
            paragraphs.add(exist)
        }
    }

    if(text.isEmpty() || (selection.collapsed && selection.end == text.length)){
        val exist = annotatedString.paragraphStyles.find { it.start == text.length && it.end == text.length}
        if(exist == null){
            paragraphs.add(AnnotatedString.Range(ParagraphStyle(), text.length, text.length, "p"))
        }else{
            paragraphs.add(exist)
        }
    }
    return TextFieldValue(
        AnnotatedString(text, annotatedString.spanStyles, paragraphs),
        selection,
        composition
    )
}

private fun TextFieldValue.modify(
    block: (spans: MutableList<MutableRange<SpanStyle>>, paragraphs: MutableList<MutableRange<ParagraphStyle>>) -> Unit
): TextFieldValue {
    val currentMutableStyles = mutableListOf<MutableRange<SpanStyle>>()
    val currentMutableParagraph = mutableListOf<MutableRange<ParagraphStyle>>()
    annotatedString.spanStyles.forEach {
        if (it.start != it.end) {
            currentMutableStyles.add(MutableRange(it.item, it.start, it.end, it.tag))
        }

    }
    annotatedString.paragraphStyles.forEach {
        if (it.start != it.end) {
            currentMutableParagraph.add(MutableRange(it.item, it.start, it.end, it.tag))
        }
    }
    block(currentMutableStyles, currentMutableParagraph)
    val spanStyles = currentMutableStyles.map {
        AnnotatedString.Range(it.item, it.start, it.end, it.tag)
    }

    val paragraphStyles = currentMutableParagraph.map {
        AnnotatedString.Range(it.item, it.start, it.end, it.tag)
    }
    return TextFieldValue(
        AnnotatedString(text, spanStyles, paragraphStyles),
        selection,
        composition
    )
}

fun TextFieldValue.bold(weight: Int): TextFieldValue {
    return modify { spans, _ ->
        if (selection.collapsed) {
            val contained = spans.find {
                it.tag == "bold" && it.end >= selection.start && it.start <= selection.start
            }
            if (contained == null) {
                spans.add(
                    MutableRange(
                        SpanStyle(fontWeight = FontWeight.Bold),
                        selection.start,
                        selection.end,
                        "bold"
                    )
                )
            }
        } else {
            spans.forEach {
                if (it.tag == "bold") {
                    if (selection.collapsed) {
                        if (it.end > selection.start && it.start < selection.end) {
                            if (selection.start > it.start) {
                                it.end = selection.start
                            }
                            if (selection.end < it.end) {
                                it.start = selection.end
                            }
                        }
                    }
                }
            }
            spans.add(
                MutableRange(
                    SpanStyle(fontWeight = FontWeight.Bold),
                    selection.start,
                    selection.end,
                    "bold"
                )
            )
        }
    }
}

fun TextFieldValue.quote(): TextFieldValue {
    return modify { _, paragraphs ->
        val selectionEnd = selection.end
        val prev = text.lastIndexOf('\n', selectionEnd) + 1
        var next = text.indexOf('\n', selectionEnd)
        if (next < 0) {
            next = text.length
        }
        val toRemoveList = arrayListOf<MutableRange<ParagraphStyle>>()
        paragraphs.forEach {
            if (it.end > prev && it.start < next) {
                toRemoveList.add(it)
            }
        }
        toRemoveList.forEach {
            paragraphs.remove(it)
        }
        paragraphs.add(
            MutableRange(
                ParagraphStyle(
                    textIndent = TextIndent(5.sp, 5.sp)
                ), prev, next, "quote"
            )
        )
    }
}

private fun updateTextFieldValue(
    current: TextFieldValue,
    next: TextFieldValue
): TextFieldValue {
    if (current.text == next.text) {
        return TextFieldValue(current.annotatedString, next.selection, next.composition)
    }
    if (next.text.isBlank()) {
        return TextFieldValue(AnnotatedString(""), next.selection, next.composition)
    }

    val currentMutableStyles = mutableListOf<MutableRange<SpanStyle>>()
    val currentMutableParagraph = mutableListOf<MutableRange<ParagraphStyle>>()
    current.annotatedString.spanStyles.forEach {
        currentMutableStyles.add(MutableRange(it.item, it.start, it.end, it.tag))
    }
    current.annotatedString.paragraphStyles.forEach {
        currentMutableParagraph.add(MutableRange(it.item, it.start, it.end, it.tag))
    }
    var indexCorrect = 0
    wordEdit(current, next).list.forEach { point ->
        val lastIndex = point.oldIndex + indexCorrect
        if (point.action == WordEditAction.insert) {
            currentMutableStyles.forEach {
                it.modifyByInsert(next.text, lastIndex)
            }
            currentMutableParagraph.forEach {
                it.modifyByInsert(next.text, lastIndex)
            }
            indexCorrect++
        } else if (point.action == WordEditAction.delete) {
            currentMutableStyles.forEach {
                it.modifyByDelete(lastIndex)
            }
            currentMutableParagraph.forEach {
                it.modifyByDelete(lastIndex)
            }
            indexCorrect--
        }
    }
    val spanStyles = currentMutableStyles.map {
        AnnotatedString.Range(it.item, it.start, it.end, it.tag)
    }

    val paragraphStyles = currentMutableParagraph.map {
        AnnotatedString.Range(it.item, it.start, it.end, it.tag)
    }
    return TextFieldValue(
        AnnotatedString(next.text, spanStyles, paragraphStyles),
        next.selection,
        next.composition
    )
}

private class MutableRange<T>(val item: T, var start: Int, var end: Int, val tag: String) {
    fun modifyByInsert(text: String, insertPos: Int) {
        if (start == end) {
            if (insertPos < start - 1) {
                start++
                end++
            } else if (insertPos == start - 1) {
                end++
            }
        } else {
            if (insertPos < start) {
                start++
                end++
            } else if (insertPos == end - 1) {
                end++
            } else if (insertPos < end) {
                end++
            }
        }
    }

    fun modifyByDelete(deletePos: Int) {
        if (start == end) {
            if (deletePos < start - 1) {
                start--
                end--
            } else if (deletePos == start - 1) {
                start--
                end--
            }
        } else {
            if (deletePos < start) {
                start--
                end--
            } else if (deletePos < end) {
                end--
            }
        }
    }
}