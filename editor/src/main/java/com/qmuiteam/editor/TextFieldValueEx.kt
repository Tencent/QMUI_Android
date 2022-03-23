package com.qmuiteam.editor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.sp

//region ==============  spanStyle ==============
fun TextFieldValue.bold(bold: BoldBehavior): TextFieldValue {
    return textStyle(
        style = SpanStyle(fontWeight = FontWeight(bold.weight)),
        tag = bold.tag
    ) {
        it.isBoldTag()
    }
}

fun TextFieldValue.textColor(textColor: TextColorBehavior): TextFieldValue {
    return textStyle(
        style = SpanStyle(color = textColor.color),
        tag = textColor.tag
    ) {
        it.isBoldTag()
    }
}

private fun TextFieldValue.textStyle(style: SpanStyle, tag: String, shouldHandle: (String) -> Boolean): TextFieldValue {
    return modifySpans { spans ->
        if (selection.collapsed) {
            val contained = spans.find {
                it.tag.isBoldTag() && it.isCursorContained(selection.start)
            }
            if (contained == null) {
                spans.add(MutableRange(style, selection.start, selection.end, tag))
            }
        } else {
            var i = 0
            var handled = false
            while (i < spans.size) {
                val span = spans[i]
                if (shouldHandle(span.tag)) {
                    if (span.start >= selection.start && span.end <= selection.end) {
                        spans.removeAt(i)
                        i--
                    } else if (span.end > selection.end && span.start < selection.start) {
                        if (span.tag == tag) {
                            handled = true
                            break
                        }
                        spans.add(i, MutableRange(span.item, selection.end, span.end, span.tag))
                        span.end = selection.start
                        i++
                    } else if (span.end > selection.start && span.start < selection.end) {
                        if (span.start >= selection.start) {
                            span.start = selection.end
                        }
                        if (span.end <= selection.end) {
                            span.end = selection.start
                        }
                    }
                }
                i++
            }

            if (!handled) {
                spans.add(MutableRange(style, selection.start, selection.end, tag))
            }
        }
    }
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

//endregion

//region ==============  paragraphStyle ==============

internal fun TextFieldValue.quote(): TextFieldValue {
    return paragraphStyle(
        ParagraphStyle(
            textIndent = TextIndent(10.sp, 10.sp)
        ),
        QuoteBehavior.tag
    )
}

internal fun TextFieldValue.unOrder(): TextFieldValue {
    return paragraphStyle(
        ParagraphStyle(
            textIndent = TextIndent(10.sp, 10.sp)
        ),
        UnOrderListBehavior.tag
    )
}

internal fun TextFieldValue.header(level: HeaderLevel): TextFieldValue {
    return paragraphStyle(
        ParagraphStyle(),
        level.tag,
        SpanStyle(fontSize = level.fontSize)
    )
}

private fun MutableRange<ParagraphStyle>.replaceStyleIfNeeded(
    value: TextFieldValue,
    tag: String,
    style: ParagraphStyle
): AnnotatedString.Range<ParagraphStyle>? {
    if (value.selection.collapsed) {
        val shouldModify = when {
            start == end -> start == value.selection.start
            value.selection.start in start until end -> true
            value.selection.start == end -> value.text[end - 1] != '\n'
            else -> false
        }
        if (shouldModify) {
            if (this.tag != tag) {
                val ret = AnnotatedString.Range(item, start, end)
                this.item = style
                this.tag = tag
                return ret
            }
        }
    } else {
        if (start < value.selection.end && end > value.selection.start && this.tag != tag) {
            val ret = AnnotatedString.Range(item, start, end)
            this.item = style
            this.tag = tag
            return ret
        }
    }
    return null
}

private fun TextFieldValue.paragraphStyle(
    style: ParagraphStyle,
    tag: String,
    companionSpan: SpanStyle? = null
): TextFieldValue {
    val replacedParagraphs = mutableListOf<AnnotatedString.Range<ParagraphStyle>>()
    val paragraphs = modifyParagraphs { paragraphs ->
        paragraphs.forEach { paragraph ->
            paragraph.replaceStyleIfNeeded(this, tag, style)?.let {
                replacedParagraphs.add(it)
            }
        }
    }

    if (replacedParagraphs.isEmpty()) {
        return paragraphs
    }

    return paragraphs.modifySpans { spans ->
        spans.removeAll { span ->
            replacedParagraphs.find {
                it.start == span.start && it.end == span.end && it.tag == span.tag
            } != null
        }
        replacedParagraphs.forEach { range ->
            companionSpan?.let {
                spans.add(MutableRange(it, range.start, range.end, tag))
            }
        }

    }
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

//endregion

internal fun TextFieldValue.check(): TextFieldValue {
    val paragraphs = mutableListOf<AnnotatedString.Range<ParagraphStyle>>()
    var currentIndex = 0
    var nextIndex = text.indexOf('\n')
    while (nextIndex >= 0) {
        val exist = annotatedString.paragraphStyles.find { it.start == currentIndex && it.end == nextIndex + 1 }
        if (exist == null) {
            paragraphs.add(AnnotatedString.Range(ParagraphStyle(), currentIndex, nextIndex + 1, NormalParagraphBehavior.tag))
        } else {
            paragraphs.add(exist)
        }
        currentIndex = nextIndex + 1
        nextIndex = text.indexOf('\n', currentIndex)
    }

    if (currentIndex < text.length) {
        val exist = annotatedString.paragraphStyles.find { it.start == currentIndex && it.end == text.length }
        if (exist == null) {
            paragraphs.add(AnnotatedString.Range(ParagraphStyle(), currentIndex, text.length, NormalParagraphBehavior.tag))
        } else {
            paragraphs.add(exist)
        }
    }

    if (text.isEmpty() || (selection.collapsed && selection.end == text.length)) {
        val exist = annotatedString.paragraphStyles.find { it.start == text.length && it.end == text.length }
        if (exist == null) {
            paragraphs.add(AnnotatedString.Range(ParagraphStyle(), text.length, text.length, NormalParagraphBehavior.tag))
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