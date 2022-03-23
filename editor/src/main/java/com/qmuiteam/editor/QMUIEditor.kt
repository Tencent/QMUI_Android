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
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
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
                        val rect = if (paragraph.start == paragraph.end) {
                            val cursorRect = it.multiParagraph.getCursorRect(paragraph.start)
                            Rect(
                                0f,
                                cursorRect.top,
                                it.multiParagraph.width,
                                cursorRect.bottom
                            )
                        } else {
                            val start = it.multiParagraph.getBoundingBox(paragraph.start)
                            val end = it.multiParagraph.getBoundingBox(paragraph.end - 1)
                            Rect(
                                0f,
                                start.top,
                                it.multiParagraph.width,
                                end.bottom
                            )
                        }
                        if (paragraph.tag == QuoteBehavior.tag) {
                            list.add(QuoteDecoration(rect))
                        } else if (paragraph.tag == UnOrderListBehavior.tag) {
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
                    if (textFieldValue.text.isEmpty()) {
                        Text(text = hint, style = hintStyle)
                    }
                    innerTextField()
                }
            )
        }
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
            val toInsertPos = lastIndex + 1
            mutableParagraph.forEachIndexed { index, item ->
                item.modifyByInsert(toInsertPos, index == mutableParagraph.size - 1)
            }
            val stopSpans = mutableListOf<MutableRange<SpanStyle>>()
            val normalSpans = mutableListOf<MutableRange<SpanStyle>>()
            mutableSpan.forEach {
                if (it.tag.startsWith(StopBehavior.prefix)) {
                    stopSpans.add(it)
                } else {
                    normalSpans.add(it)
                }
            }
            mutableSpan.forEach { item ->
                item.modifyByInsert(
                    toInsertPos,
                    stopSpans.find { it.end == item.end && it.tag.endsWith(item.tag) } == null
                )
                // update companion span.
                mutableParagraph.find { it.tag == item.tag && it.start == item.start }?.let {
                    item.end = it.end
                }
            }
            stopSpans.forEach {
                it.modifyByInsert(toInsertPos, true)
                if (it.end > it.start) {
                    mutableSpan.remove(it)
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

            if (current.text[point.oldIndex] == '\n') {
                val prevParagraph = mutableParagraph.find { it.end == point.oldIndex + 1 }
                val nextParagraph = mutableParagraph.find { it.start == point.oldIndex + 1 && it.end != it.start }
                nextParagraph?.let { np ->
                    prevParagraph?.let { pp ->
                        pp.end = np.end
                        mutableSpan.find { it.start == pp.start && it.tag == pp.tag }?.let {
                            it.end = np.end
                        }
                    }
                    mutableParagraph.remove(np)
                    mutableSpan.removeAll { np.start == it.start && it.tag == np.tag }
                }
            }

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