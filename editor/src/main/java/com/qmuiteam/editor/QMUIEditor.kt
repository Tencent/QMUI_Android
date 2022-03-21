package com.qmuiteam.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
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
                    .offset(dpRect.left, dpRect.top - 6.dp)
                    .width(dpRect.width)
                    .height(dpRect.height + 12.dp)
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


class UnOrderedDecoration(val rect: Rect) : EditorDecoration {
    @Composable
    override fun Compose() {
        key(this) {
            val dpRect = with(LocalDensity.current) {
                DpRect(rect.left.toDp(), rect.top.toDp(), rect.right.toDp(), rect.bottom.toDp())
            }
            Box(
                Modifier
                    .offset(dpRect.left, dpRect.top + dpRect.height / 2 - 2.dp)
                    .width(4.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
            )
        }
    }
}

@Composable
fun QMUIEditor(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    channel: Channel<EditorBehavior>,
    hint: AnnotatedString = AnnotatedString(""),
    hintStyle: TextStyle = TextStyle.Default.copy(color = Color.Gray),
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
                        val rect = if(paragraph.start == paragraph.end){
                            val cursorRect = it.multiParagraph.getCursorRect(paragraph.start)
                            Rect(
                                0f,
                                cursorRect.top,
                                it.multiParagraph.width,
                                cursorRect.bottom
                            )
                        }else{
                            val start = it.multiParagraph.getBoundingBox(paragraph.start)
                            val end = it.multiParagraph.getBoundingBox(paragraph.end - 1)
                            Rect(
                                0f,
                                start.top,
                                it.multiParagraph.width,
                                end.bottom
                            )
                        }
                        if (paragraph.tag == Quote.tag) {
                            list.add(QuoteDecoration(rect))
                        } else if (paragraph.tag == UnOrderList.tag) {
                            list.add(UnOrderedDecoration(rect))
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
                    if(textFieldValue.text.isEmpty()){
                        Text(text = hint, style = hintStyle)
                    }
                    innerTextField()
                }
            )
        }
    }
}

private fun TextFieldValue.check(): TextFieldValue {
    val paragraphs = mutableListOf<AnnotatedString.Range<ParagraphStyle>>()
    var currentIndex = 0
    var nextIndex = text.indexOf('\n')
    while (nextIndex >= 0) {
        val exist = annotatedString.paragraphStyles.find { it.start == 0 && it.end == nextIndex + 1 }
        if (exist == null) {
            paragraphs.add(AnnotatedString.Range(ParagraphStyle(), currentIndex, nextIndex + 1, "p"))
        } else {
            paragraphs.add(exist)
        }
        currentIndex = nextIndex + 1
        nextIndex = text.indexOf('\n', currentIndex)
    }

    if (currentIndex < text.length) {
        val exist = annotatedString.paragraphStyles.find { it.start == 0 && it.end == text.length }
        if (exist == null) {
            paragraphs.add(AnnotatedString.Range(ParagraphStyle(), currentIndex, text.length, "p"))
        } else {
            paragraphs.add(exist)
        }
    }

    if (text.isEmpty() || (selection.collapsed && selection.end == text.length)) {
        val exist = annotatedString.paragraphStyles.find { it.start == text.length && it.end == text.length }
        if (exist == null) {
            paragraphs.add(AnnotatedString.Range(ParagraphStyle(), text.length, text.length, "p"))
        } else {
            paragraphs.add(exist)
        }
    }
    return TextFieldValue(
        AnnotatedString(text, annotatedString.spanStyles, paragraphs),
        selection,
        composition
    )
}

private fun TextFieldValue.modifySpans(
    block: (spans: MutableList<MutableRange<SpanStyle>>) -> Unit
): TextFieldValue {
    val mutableSpans = mutableListOf<MutableRange<SpanStyle>>()
    annotatedString.spanStyles.forEach {
        mutableSpans.add(MutableRange(it.item, it.start, it.end, it.tag))
    }
    block(mutableSpans)
    val spanStyles = mutableSpans.map {
        AnnotatedString.Range(it.item, it.start, it.end, it.tag)
    }

    return TextFieldValue(
        AnnotatedString(text, spanStyles, annotatedString.paragraphStyles),
        selection,
        composition
    )
}

private fun TextFieldValue.modifyParagraphs(
    block: (paragraphs: MutableList<MutableRange<ParagraphStyle>>) -> Unit
): TextFieldValue {
    val mutableParagraphs = mutableListOf<MutableRange<ParagraphStyle>>()
    annotatedString.paragraphStyles.forEach {
        mutableParagraphs.add(MutableRange(it.item, it.start, it.end, it.tag))
    }

    block(mutableParagraphs)

    val paragraphStyles = mutableParagraphs.map {
        AnnotatedString.Range(it.item, it.start, it.end, it.tag)
    }

    return TextFieldValue(
        AnnotatedString(text, annotatedString.spanStyles, paragraphStyles),
        selection,
        composition
    )
}

fun TextFieldValue.bold(weight: Int): TextFieldValue {
    return modifySpans { spans ->
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

private fun MutableRange<ParagraphStyle>.updateStyleIfNeeded(
    value: TextFieldValue, tag: String, style: ParagraphStyle
): Boolean{
    if (value.selection.collapsed) {
        val shouldModify = if(start == end){
            start == start
        }else if(value.selection.start in start until end){
            true
        } else if(value.selection.end == end){
            value.text[end - 1] != '\n'
        }else false
        if (shouldModify) {
            item = style
            this.tag = tag
            return true
        }
    } else {
        if (start < value.selection.end && end > value.selection.start) {
            item = style
            this.tag = tag
            return true
        }
    }
    return false
}

internal fun TextFieldValue.paragraphStyle(tag: String, style: ParagraphStyle): TextFieldValue {
    return modifyParagraphs { paragraphs ->
        paragraphs.forEach {
            it.updateStyleIfNeeded(this, tag, style)
        }
    }.modifySpans { spans ->
        spans.removeAll {
            if (selection.collapsed) {
                it.tag == "h" && it.start <= selection.end && it.end >= selection.start
            } else {
                it.tag == "h" && it.start < selection.end && it.end > selection.start
            }

        }
    }
}

internal fun TextFieldValue.quote(): TextFieldValue {
    return paragraphStyle(
        Quote.tag, ParagraphStyle(
            textIndent = TextIndent(10.sp, 10.sp)
        )
    )
}

internal fun TextFieldValue.unOrder(): TextFieldValue {
    return paragraphStyle(
        UnOrderList.tag, ParagraphStyle(
            textIndent = TextIndent(10.sp, 10.sp)
        )
    )
}

internal fun TextFieldValue.header(level: HeaderLevel): TextFieldValue {
    var start = Int.MAX_VALUE
    var end = Int.MIN_VALUE
    val ret = modifyParagraphs { paragraphs ->
        val style = ParagraphStyle()
        paragraphs.forEach {
            if(it.updateStyleIfNeeded(this,  level.tag, style)){
                start = it.start.coerceAtMost(start)
                end = it.end.coerceAtLeast(end)
            }
        }
    }
    if (start == Int.MAX_VALUE || end == Int.MIN_VALUE) {
        return ret
    }
    return ret.modifySpans {
        var i = 0
        while (i < it.size) {
            val span = it[i]
            if (span.end > start && (span.start < end || (span.start == end && span.start == span.end))) {
                when {
                    span.start < start -> {
                        span.end = start
                    }
                    span.end > end -> {
                        span.start = end
                    }
                    else -> {
                        it.removeAt(i)
                        i--
                    }
                }
            }
            i++
        }
        it.add(MutableRange(SpanStyle(fontSize = level.fontSize), start, end, "h"))
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
        return TextFieldValue(AnnotatedString(""), next.selection, next.composition).check()
    }

    val mutableSpan = mutableListOf<MutableRange<SpanStyle>>()
    val mutableParagraph = mutableListOf<MutableRange<ParagraphStyle>>()
    current.annotatedString.spanStyles.forEach {
        mutableSpan.add(MutableRange(it.item, it.start, it.end, it.tag))
    }
    current.annotatedString.paragraphStyles.forEach {
        mutableParagraph.add(MutableRange(it.item, it.start, it.end, it.tag))
    }
    var indexCorrect = 0
    wordEdit(current, next).list.forEach { point ->
        val lastIndex = point.oldIndex + indexCorrect
        if (point.action == WordEditAction.insert) {
            mutableParagraph.forEachIndexed { index, item ->
                item.modifyByInsert(lastIndex, index == mutableParagraph.size - 1)
            }
            mutableSpan.forEach { item ->
                item.modifyByInsert(lastIndex, true)
                if(item.tag=="h"){
                    mutableParagraph.find { it.start == item.start }?.let {
                        item.end = it.end
                    }
                }
            }
            if (next.text[point.newIndex] == '\n') {
                for (i in 0 until mutableParagraph.size) {
                    val paragraph = mutableParagraph[i]
                    if (paragraph.start <= point.newIndex && paragraph.end > point.newIndex) {
                        if (!paragraph.tag.isHeaderTag()) {
                            mutableParagraph.add(i + 1, MutableRange(paragraph.item, point.newIndex + 1, paragraph.end, paragraph.tag))
                        } else {
                            mutableParagraph.add(i + 1, MutableRange(ParagraphStyle(), point.newIndex + 1, paragraph.end, "p"))
                            mutableSpan.find {
                                it.start == paragraph.start && it.end == paragraph.end && it.tag == "h"
                            }?.let { it.end = point.newIndex + 1 }
                        }

                        paragraph.end = point.newIndex + 1
                        break
                    }
                }
            }
            indexCorrect++
        } else if (point.action == WordEditAction.delete) {
            var i = 0
            while (i < mutableSpan.size) {
                val span = mutableSpan[i]
                val shouldRemove = span.modifyByDelete(lastIndex)
                if (shouldRemove) {
                    mutableSpan.removeAt(i)
                    i -= 1
                }
                i++
            }
            i = 0
            while (i < mutableParagraph.size) {
                val paragraph = mutableParagraph[i]
                val shouldRemove = paragraph.modifyByDelete(lastIndex)
                if (shouldRemove) {
                    mutableParagraph.removeAt(i)
                    i -= 1
                }
                i++
            }
            indexCorrect--
        }
    }
    mutableSpan.removeAll {
        it.start == it.end && (it.end < next.selection.start || it.start > next.selection.end)
    }
    mutableParagraph.removeAll {
        it.start == it.end && (it.end < next.selection.start || it.start > next.selection.end)
    }
    val spanStyles = mutableSpan.map {
        AnnotatedString.Range(it.item, it.start, it.end, it.tag)
    }

    val paragraphStyles = mutableParagraph.map {
        AnnotatedString.Range(it.item, it.start, it.end, it.tag)
    }
    return TextFieldValue(
        AnnotatedString(next.text, spanStyles, paragraphStyles),
        next.selection,
        next.composition
    )
}

private class MutableRange<T>(var item: T, var start: Int, var end: Int, var tag: String) {
    fun modifyByInsert(insertPos: Int, appendIfAtEnd: Boolean) {
        val toInsertPos = insertPos + 1
        if (start == end) {
            if (toInsertPos < start) {
                start++
                end++
            } else if (toInsertPos == start) {
                end++
            }
        } else {
            if (toInsertPos < start) {
                start++
                end++
            } else if (toInsertPos < end || (appendIfAtEnd && toInsertPos == end)) {
                end++
            }
        }
    }

    fun modifyByDelete(deletePos: Int): Boolean {
        if (start == end) {
            if (deletePos < start - 1) {
                start--
                end--
            } else if (deletePos == start - 1) {
                start--
                end--
                return true
            }
        }
        if (deletePos < start) {
            start--
            end--
        } else if (deletePos < end) {
            end--
        }
        return false
    }
}