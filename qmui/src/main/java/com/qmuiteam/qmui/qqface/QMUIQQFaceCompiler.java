package com.qmuiteam.qmui.qqface;

import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.util.LruCache;

import com.qmuiteam.qmui.span.QMUITouchableSpan;
import com.qmuiteam.qmui.util.QMUILangHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link QMUIQQFaceView} 的内容解析器，将文本内容解析成 {@link QMUIQQFaceView} 想要的数据格式。
 *
 * @author cginechen
 * @date 2016-12-21
 */

public class QMUIQQFaceCompiler {
    private static final int SPAN_COLUMN = 2;

    private volatile static QMUIQQFaceCompiler sInstance;

    // cache
    private LruCache<CharSequence, ElementList> mCache;

    private IQMUIQQFaceManager mQQFaceManager;

    // 有多线程保护，如果内容非常多，可以扔到后台去（虽然暂时没有这个必要，但不避免有人想这么做）
    public static QMUIQQFaceCompiler getInstance(IQMUIQQFaceManager manager) {
        if (sInstance == null) {
            synchronized (QMUIQQFaceCompiler.class) {
                if (sInstance == null) {
                    sInstance = new QMUIQQFaceCompiler(manager);
                }
            }
        }
        return sInstance;
    }

    private QMUIQQFaceCompiler(IQMUIQQFaceManager manager) {
        mCache = new LruCache<>(30);
        mQQFaceManager = manager;
    }

    public int getSpecialBoundsMaxHeight(){
        return mQQFaceManager.getSpecialDrawableMaxHeight();
    }

    public ElementList compile(CharSequence text) {
        if (QMUILangHelper.isNullOrEmpty(text)) {
            return null;
        }
        return compile(text, 0, text.length());
    }

    public ElementList compile(CharSequence text, int start, int end) {
        return compile(text, start, end, false);
    }

    private ElementList compile(CharSequence text, int start, int end, boolean inSpan) {
        if (QMUILangHelper.isNullOrEmpty(text)) {
            return null;
        }
        if (start < 0 || start >= text.length()) {
            throw new IllegalArgumentException("start must >= 0 and < text.length");
        }
        if (end <= start) {
            throw new IllegalArgumentException("end must > start");
        }
        int size = text.length();

        if (end > size) {
            end = size;
        }

        boolean hasClickableSpans = false;
        QMUITouchableSpan[] spans = null;
        int[] spanInfo = null;
        if (!inSpan && (text instanceof Spannable)) {
            spans = ((Spannable) text).getSpans(
                    0,
                    text.length() - 1,
                    QMUITouchableSpan.class);
            hasClickableSpans = spans.length > 0;
            if (hasClickableSpans) {
                spanInfo = new int[spans.length * SPAN_COLUMN];
                for (int i = 0; i < spans.length; i++) {
                    spanInfo[i * SPAN_COLUMN] = ((Spannable) text).getSpanStart(spans[i]);
                    spanInfo[i * SPAN_COLUMN + 1] = ((Spannable) text).getSpanEnd(spans[i]);
                }
            }
        }

        ElementList elementList = mCache.get(text);
        if (!hasClickableSpans && elementList != null && start == elementList.getStart() && end == elementList.getEnd()) {
            return elementList;
        }
        elementList = realCompile(text, start, end, spans, spanInfo);
        mCache.put(text, elementList);
        return elementList;
    }

    public void setCache(LruCache<CharSequence, ElementList> cache) {
        mCache = cache;
    }

    @SuppressWarnings("ConstantConditions")
    private ElementList realCompile(CharSequence text, int start, int end, QMUITouchableSpan[] spans, int[] spanInfo) {
        int size = text.length();
        int nearSpanIndex = -1;
        int nearSpanStart = Integer.MAX_VALUE;
        int nearSpanEnd = nearSpanStart;
        if (spans != null && spans.length > 0) {
            nearSpanIndex = 0;
            nearSpanStart = spanInfo[0];
            nearSpanEnd = spanInfo[1];
        }

        ElementList elementList = new ElementList(start, end);
        if (start > 0) {
            elementList.add(Element.createTextElement(text.subSequence(0, start)));
        }
        int index = start, last = start;
        boolean inParentheses = false;
        while (index < end) {
            // 优先处理Span的情况
            if (index == nearSpanStart) {
                if (index - last > 0) {
                    if (inParentheses) {
                        inParentheses = false;
                        last--;
                    }
                    elementList.add(Element.createTextElement(text.subSequence(last, index)));
                }
                elementList.add(Element.createTouchSpanElement(
                        text.subSequence(nearSpanStart, nearSpanEnd), spans[nearSpanIndex], this));
                index = last = nearSpanEnd;
                nearSpanIndex++;
                if (nearSpanIndex >= spans.length) {
                    nearSpanStart = nearSpanEnd = Integer.MAX_VALUE;
                } else {
                    nearSpanStart = spanInfo[nearSpanIndex * SPAN_COLUMN];
                    nearSpanEnd = spanInfo[nearSpanIndex * SPAN_COLUMN + 1];
                }

                continue;
            }

            char c = text.charAt(index);
            if (c == '[') {
                if (index - last > 0) {
                    elementList.add(Element.createTextElement(text.subSequence(last, index)));
                }
                inParentheses = true;
                last = index++;
                continue;
            } else if (c == ']' && inParentheses) {
                inParentheses = false;
                index++;
                if (index - last > 0) {
                    String label = text.subSequence(last, index).toString();
                    Drawable specialDrawable = mQQFaceManager.getSpecialBoundsDrawable(label);
                    if (specialDrawable != null) {
                        elementList.add(Element.createSpeaicalBoundsDrawableElement(specialDrawable));
                        last = index;
                    } else {
                        int res = mQQFaceManager.getQQfaceResource(label);
                        if (res != 0) {
                            elementList.add(Element.createDrawableElement(res));
                            last = index;
                        }
                    }
                }
                continue;
            } else if (c == '\n') {
                if (inParentheses) {
                    inParentheses = false;
                }
                if (index - last > 0) {
                    elementList.add(Element.createTextElement(text.subSequence(last, index)));
                }
                elementList.add(Element.createNextLineElement());
                last = ++index;
                continue;
            }
            if (inParentheses) {
                if (index - last > 8) {
                    inParentheses = false;
                } else {
                    index++;
                    continue;
                }


            }

            int skip = 0;
            int icon = 0;
            if (mQQFaceManager.maybeSoftBankEmoji(c)) {
                icon = mQQFaceManager.getSoftbankEmojiResource(c);
                skip = icon == 0 ? 0 : 1;
            }
            if (icon == 0) {
                int unicode = Character.codePointAt(text, index);
                skip = Character.charCount(unicode);
                if (mQQFaceManager.maybeEmoji(unicode)) {
                    icon = mQQFaceManager.getEmojiResource(unicode);
                }
                if (icon == 0 && start + skip < end) {
                    int nextUnicode = Character.codePointAt(text, start + skip);
                    icon = mQQFaceManager.getDoubleUnicodeEmoji(unicode, nextUnicode);
                    if (icon != 0) {
                        skip += Character.charCount(nextUnicode);
                    }
                }
            }
            if (icon != 0) {
                if (last != index) {
                    elementList.add(Element.createTextElement(text.subSequence(last, index)));
                }
                elementList.add(Element.createDrawableElement(icon));
                index += skip;
                last = index;
            } else {
                index++;
            }
        }
        if (last < end) {
            elementList.add(Element.createTextElement(text.subSequence(last, size)));
        }
        return elementList;
    }

    public enum ElementType {
        TEXT,
        DRAWABLE,
        SPECIAL_BOUNDS_DRAWABLE,
        SPAN,
        NEXTLINE
    }

    public static class Element {
        private ElementType mType;
        private CharSequence mText;
        private int mDrawableRes;
        private Drawable mSpecialBoundsDrawable;
        private ElementList mChildList; // for span
        private QMUITouchableSpan mTouchableSpan;

        public ElementType getType() {
            return mType;
        }

        public CharSequence getText() {
            return mText;
        }

        public int getDrawableRes() {
            return mDrawableRes;
        }

        public ElementList getChildList() {
            return mChildList;
        }

        public QMUITouchableSpan getTouchableSpan() {
            return mTouchableSpan;
        }

        public Drawable getSpecialBoundsDrawable() {
            return mSpecialBoundsDrawable;
        }

        public static Element createTextElement(CharSequence text) {
            Element element = new Element();
            element.mType = ElementType.TEXT;
            element.mText = text;
            return element;
        }

        public static Element createDrawableElement(int drawableRes) {
            Element element = new Element();
            element.mType = ElementType.DRAWABLE;
            element.mDrawableRes = drawableRes;
            return element;
        }

        public static Element createSpeaicalBoundsDrawableElement(Drawable specialBoundsDrawable) {
            Element element = new Element();
            element.mType = ElementType.SPECIAL_BOUNDS_DRAWABLE;
            element.mSpecialBoundsDrawable = specialBoundsDrawable;
            return element;
        }

        public static Element createTouchSpanElement(CharSequence text,
                                                     QMUITouchableSpan touchableSpan,
                                                     QMUIQQFaceCompiler compiler) {
            Element element = new Element();
            element.mType = ElementType.SPAN;
            element.mChildList = compiler.compile(text, 0, text.length(), true);
            element.mTouchableSpan = touchableSpan;
            return element;
        }

        public static Element createNextLineElement() {
            Element element = new Element();
            element.mType = ElementType.NEXTLINE;
            return element;
        }
    }


    public static class ElementList {
        private int mStart;
        private int mEnd;
        private int mQQFaceCount = 0;
        private int mNewLineCount = 0;
        private List<Element> mElements;

        public ElementList(int start, int end) {
            mStart = start;
            mEnd = end;
            mElements = new ArrayList<>();
        }

        public int getStart() {
            return mStart;
        }

        public int getEnd() {
            return mEnd;
        }

        public int getNewLineCount() {
            return mNewLineCount;
        }

        public int getQQFaceCount() {
            return mQQFaceCount;
        }

        public void add(Element element) {
            if (element.getType() == ElementType.DRAWABLE) {
                mQQFaceCount++;
            } else if (element.getType() == ElementType.NEXTLINE) {
                mNewLineCount++;
            } else if (element.getType() == ElementType.SPAN) {
                mQQFaceCount += element.getChildList().getQQFaceCount();
                mNewLineCount += element.getChildList().getNewLineCount();
            }
            mElements.add(element);
        }

        public List<Element> getElements() {
            return mElements;
        }
    }
}
