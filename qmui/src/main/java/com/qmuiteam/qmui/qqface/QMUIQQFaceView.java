package com.qmuiteam.qmui.qqface;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.qmuiteam.qmui.QMUILog;
import com.qmuiteam.qmui.link.ITouchableSpan;
import com.qmuiteam.qmui.span.QMUITouchableSpan;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.R;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.view.View.MeasureSpec.AT_MOST;

/**
 * 表情控件
 *
 * <ul>
 * <li>支持显示表情的伪 {@link android.widget.TextView}（继续自定义 {@link View}，而不是真正的 {@link android.widget.TextView})，
 * 实现了 {@link android.widget.TextView} 的 maxLine、ellipsize、textSize、textColor 等基本功能。</li>
 * <li>支持与 {@link QMUITouchableSpan} 配合使用实现内容可点击。</li>
 * </ul>
 *
 * @author cginechen
 * @date 2016-12-21
 */

public class QMUIQQFaceView extends View {
    private static final String TAG = "QMUIQQFaceView";
    private CharSequence mOriginText;
    private QMUIQQFaceCompiler.ElementList mElementList;
    private QMUIQQFaceCompiler mCompiler;
    private boolean mOpenQQFace = true;
    private TextPaint mPaint;
    private Paint mSpanBgPaint;
    private int mTextSize;
    private int mTextColor;
    private int mLineSpace = -1;
    private int mFontHeight;
    private int mQQFaceSize = 0;
    private int mFirstBaseLine;
    private int mMaxLine = Integer.MAX_VALUE;
    private boolean mIsSingleLine = false;
    private int mLines = 0;
    private Set<SpanInfo> mSpanInfos = new HashSet<>();
    private static final String mEllipsizeText = "...";
    private String mMoreActionText;
    private int mMoreActionColor;
    private int mMoreActionTextLength = 0;
    private int mEllipsizeTextLength = 0;
    private TextUtils.TruncateAt mEllipsize = TextUtils.TruncateAt.END;
    private boolean mIsNeedEllipsize = false;
    private int mNeedDrawLine = 0;
    private int mQQFaceSizeAddon = 0; // 可以让QQ表情高度比字体高度小一点或大一点
    private QQFaceViewListener mListener;
    private int mMaxWidth = Integer.MAX_VALUE;
    private PressCancelAction mPendingPressCancelAction = null;
    private boolean mJumpHandleMeasureAndDraw = false;
    private Runnable mDelayTextSetter = null;
    private boolean mIncludePad = true;
    private Typeface mTypeface = null;
    private int mParagraphSpace = 0; // 段间距
    private int mSpecialDrawablePadding = 0;

    public QMUIQQFaceView(Context context) {
        this(context, null);
    }

    public QMUIQQFaceView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.QMUIQQFaceStyle);
    }

    public QMUIQQFaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = getContext().obtainStyledAttributes(attrs,
                R.styleable.QMUIQQFaceView, defStyleAttr, 0);
        mQQFaceSizeAddon = -QMUIDisplayHelper.dp2px(context, 2); // 默认表情小一点好看
        mTextSize = array.getDimensionPixelSize(R.styleable.QMUIQQFaceView_android_textSize,
                QMUIDisplayHelper.dp2px(context, 14));
        mTextColor = array.getColor(R.styleable.QMUIQQFaceView_android_textColor, Color.BLACK);
        mIsSingleLine = array.getBoolean(R.styleable.QMUIQQFaceView_android_singleLine, false);
        mMaxLine = array.getInt(R.styleable.QMUIQQFaceView_android_maxLines, mMaxLine);
        int lineSpace = array.getDimensionPixelOffset(R.
                styleable.QMUIQQFaceView_android_lineSpacingExtra, 0);
        setLineSpace(lineSpace);
        int ellipsize = -1;
        ellipsize = array.getInt(R.styleable.QMUIQQFaceView_android_ellipsize, ellipsize);
        switch (ellipsize) {
            case 1:
                mEllipsize = TextUtils.TruncateAt.START;
                break;
            case 2:
                mEllipsize = TextUtils.TruncateAt.MIDDLE;
                break;
            case 3:
            default:
                mEllipsize = TextUtils.TruncateAt.END;
                break;
        }
        mMaxWidth = array.getDimensionPixelSize(R.styleable.QMUIQQFaceView_android_maxWidth, mMaxWidth);
        mSpecialDrawablePadding = array.getDimensionPixelSize(R.styleable.QMUIQQFaceView_qmui_special_drawable_padding, 0);
        final String text = array.getString(R.styleable.QMUIQQFaceView_android_text);
        if (!QMUILangHelper.isNullOrEmpty(text)) {
            mDelayTextSetter = new Runnable() {
                @Override
                public void run() {
                    setText(text);
                }
            };
        }
        mMoreActionText = array.getString(R.styleable.QMUIQQFaceView_qmui_more_action_text);
        mMoreActionColor = array.getColor(R.styleable.QMUIQQFaceView_qmui_more_action_color, mTextColor);

        array.recycle();
        mPaint = new TextPaint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mTextSize);
        mPaint.setColor(mTextColor);
        mEllipsizeTextLength = (int) Math.ceil(mPaint.measureText(mEllipsizeText));
        measureMoreActionTextLength();
        mSpanBgPaint = new Paint();
        mSpanBgPaint.setAntiAlias(true);
        mSpanBgPaint.setStyle(Paint.Style.FILL);
    }

    public void setOpenQQFace(boolean openQQFace) {
        mOpenQQFace = openQQFace;
    }

    public void setMaxWidth(int maxWidth) {
        if (mMaxWidth != maxWidth) {
            mMaxWidth = maxWidth;
            requestLayout();
        }
    }

    public int getMaxWidth() {
        return mMaxWidth;
    }

    SpanInfo mTouchSpanInfo = null;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        if (mSpanInfos.isEmpty()) {
            return super.onTouchEvent(event);
        }
        final int action = event.getAction();

        if (mTouchSpanInfo == null && action != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(event);
        }

        // touch事件前先消耗掉还存在的mPendingPressCancelAction
        if (mPendingPressCancelAction != null) {
            mPendingPressCancelAction.run();
            mPendingPressCancelAction = null;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mTouchSpanInfo = null;

                for (SpanInfo spanInfo : mSpanInfos) {
                    if (spanInfo.onTouch(x, y)) {
                        mTouchSpanInfo = spanInfo;
                        break;
                    }
                }
                if (mTouchSpanInfo == null) {
                    return super.onTouchEvent(event);
                }
                mTouchSpanInfo.setPressed(true);
                mTouchSpanInfo.invalidateSpan();
                break;
            case MotionEvent.ACTION_CANCEL:
                mPendingPressCancelAction = null;
                mTouchSpanInfo.setPressed(false);
                mTouchSpanInfo.invalidateSpan();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mTouchSpanInfo.onTouch(x, y)) {
                    mTouchSpanInfo.setPressed(false);
                    mTouchSpanInfo.invalidateSpan();
                    mTouchSpanInfo = null;
                }
                break;
            case MotionEvent.ACTION_UP:
                mTouchSpanInfo.onClick();
                mPendingPressCancelAction = new PressCancelAction(mTouchSpanInfo);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mPendingPressCancelAction != null) {
                            mPendingPressCancelAction.run();
                        }
                    }
                }, 100);
                break;
        }
        return true;
    }

    public void setCompiler(QMUIQQFaceCompiler compiler) {
        mCompiler = compiler;
        if (mDelayTextSetter != null) {
            mDelayTextSetter.run();
        }
    }

    public void setTypeface(Typeface typeface) {
        if (mTypeface != typeface) {
            mTypeface = typeface;
            needReCalculateFontHeight = true;
            mPaint.setTypeface(typeface);
            requestLayout();
            invalidate();
        }
    }

    public void setParagraphSpace(int paragraphSpace) {
        if (mParagraphSpace != paragraphSpace) {
            mParagraphSpace = paragraphSpace;
            requestLayout();
            invalidate();
        }
    }

    public void setMoreActionText(String moreActionText) {
        if (mMoreActionText == null || !mMoreActionText.equals(moreActionText)) {
            mMoreActionText = moreActionText;
            measureMoreActionTextLength();
            requestLayout();
            invalidate();
        }
    }

    public void setMoreActionColor(int color) {
        if (color != mMoreActionColor) {
            mMoreActionColor = color;
            invalidate();
        }
    }

    private void measureMoreActionTextLength() {
        if (QMUILangHelper.isNullOrEmpty(mMoreActionText)) {
            mMoreActionTextLength = 0;
        } else {
            mMoreActionTextLength = (int) Math.ceil(mPaint.measureText(mMoreActionText));
        }
    }


    public void setSpecialDrawablePadding(int specialDrawablePadding) {
        if (mSpecialDrawablePadding != specialDrawablePadding) {
            mSpecialDrawablePadding = specialDrawablePadding;
            requestLayout();
            invalidate();
        }
    }

    public void setIncludeFontPadding(boolean includepad) {
        if (mIncludePad != includepad) {
            needReCalculateFontHeight = true;
            mIncludePad = includepad;
            requestLayout();
            invalidate();
        }
    }

    public void setQQFaceSizeAddon(int QQFaceSizeAddon) {
        if (mQQFaceSizeAddon != QQFaceSizeAddon) {
            mQQFaceSizeAddon = QQFaceSizeAddon;
            mNeedReCalculateLines = true;
            requestLayout();
            invalidate();
        }
    }

    public void setLineSpace(int lineSpace) {
        if (mLineSpace != lineSpace) {
            mLineSpace = lineSpace;
            requestLayout();
            invalidate();
        }
    }

    public void setEllipsize(TextUtils.TruncateAt where) {
        if (mEllipsize != where) {
            mEllipsize = where;
            requestLayout();
            invalidate();
        }
    }

    public void setMaxLine(int maxLine) {
        if (mMaxLine != maxLine) {
            mMaxLine = maxLine;
            requestLayout();
            invalidate();
        }
    }

    public int getMaxLine() {
        return mMaxLine;
    }

    public int getLineCount() {
        return mLines;
    }

    public void setSingleLine(boolean singleLine) {
        if (mIsSingleLine != singleLine) {
            mIsSingleLine = singleLine;
            requestLayout();
            invalidate();
        }
    }

    public void setTextColor(@ColorInt int textColor) {
        if (mTextColor != textColor) {
            mTextColor = textColor;
            mPaint.setColor(textColor);
            invalidate();
        }
    }

    public TextPaint getPaint() {
        return mPaint;
    }

    public void setTextSize(int textSize) {
        if (mTextSize != textSize) {
            mTextSize = textSize;
            mPaint.setTextSize(mTextSize);
            needReCalculateFontHeight = true;
            mNeedReCalculateLines = true;
            mEllipsizeTextLength = (int) Math.ceil(mPaint.measureText(mEllipsizeText));
            measureMoreActionTextLength();
            requestLayout();
            invalidate();
        }
    }

    public int getTextSize() {
        return mTextSize;
    }

    public CharSequence getText() {
        return mOriginText;
    }

    public void setText(CharSequence charSequence) {
        mDelayTextSetter = null;
        CharSequence oldText = mOriginText;
        if (mOriginText != null && mOriginText.equals(charSequence)) {
            return;
        }
        mOriginText = charSequence;
        if (mOpenQQFace && mCompiler == null) {
            throw new RuntimeException("mCompiler == null");
        }

        if (QMUILangHelper.isNullOrEmpty(mOriginText)) {
            if (!QMUILangHelper.isNullOrEmpty(oldText)) {
                mElementList = null;
                requestLayout();
                invalidate();
            }
            return;
        }

        if (mOpenQQFace && mCompiler != null) {
            mElementList = mCompiler.compile(mOriginText);
        } else {
            mElementList = new QMUIQQFaceCompiler.ElementList(0, mOriginText.length());
            String[] strings = mOriginText.toString().split("\\n");
            for (int i = 0; i < strings.length; i++) {
                mElementList.add(QMUIQQFaceCompiler.Element.createTextElement(strings[i]));
                if (i != strings.length - 1) {
                    mElementList.add(QMUIQQFaceCompiler.Element.createNextLineElement());
                }
            }
        }
        mNeedReCalculateLines = true;
        int paddingHor = getPaddingLeft() + getPaddingRight();
        if (getLayoutParams() == null) {
            return;
        }
        if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            requestLayout();
            invalidate();
            return;
        }
        if (getWidth() > paddingHor) {
            mLines = 0;
            calculateLinesAndContentWidth(getWidth());
            int oldDrawLine = mNeedDrawLine;
            calculateNeedDrawLine();
            // 优化： 如果高度固定或者绘制的行数相同，则不进行requestLayout
            if (oldDrawLine == mNeedDrawLine
                    || getLayoutParams().height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                invalidate();
            } else {
                requestLayout();
                invalidate();
            }
        }
    }

    private boolean needReCalculateFontHeight = true;

    private void calculateFontHeight() {
        if (needReCalculateFontHeight) {
            Paint.FontMetricsInt fontMetricsInt = mPaint.getFontMetricsInt();
            if (fontMetricsInt == null) {
                mFontHeight = mQQFaceSize = 0;
            } else {
                needReCalculateFontHeight = false;
                int top = getFontHeightCalTop(fontMetricsInt, mIncludePad);
                int bottom = getFontHeightCalBottom(fontMetricsInt, mIncludePad);
                int fontHeight = bottom - top;
                mQQFaceSize = fontHeight + mQQFaceSizeAddon;
                int specialMaxDrawableHeight = mCompiler.getSpecialBoundsMaxHeight();
                int drawableSize = Math.max(mQQFaceSize, specialMaxDrawableHeight);
                if (fontHeight >= drawableSize) {
                    mFontHeight = fontHeight;
                    mFirstBaseLine = -top;
                } else {
                    mFontHeight = drawableSize;
                    mFirstBaseLine = -top + (mFontHeight - drawableSize) / 2;
                }
            }
        }
    }


    protected int getFontHeightCalTop(Paint.FontMetricsInt fontMetricsInt, boolean includePad) {
        return includePad ? fontMetricsInt.top : fontMetricsInt.ascent;
    }

    protected int getFontHeightCalBottom(Paint.FontMetricsInt fontMetricsInt, boolean includePad) {
        return includePad ? fontMetricsInt.bottom : fontMetricsInt.descent;
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        if (getPaddingLeft() != left || getPaddingRight() != right) {
            mNeedReCalculateLines = true;
        }
        super.setPadding(left, top, right, bottom);
    }

    private int mCurrentCalWidth = 0;
    private int mCurrentCalLine = 0;
    private int mContentCalMaxWidth = 0;
    private boolean mNeedReCalculateLines = false; // 缓存，避免onMeasure重复计算
    private int mLastCalLimitWidth = 0;
    private int mLastCalContentWidth = 0;
    private int mLastCalLines = 0;

    private int calculateLinesAndContentWidth(int limitWidth) {
        if (limitWidth <= (getPaddingRight() + getPaddingLeft()) || isElementEmpty()) {
            mLines = 0;
            mLastCalLines = 0;
            mLastCalContentWidth = 0;
            return mLastCalContentWidth;
        }

        if (!mNeedReCalculateLines && mLastCalLimitWidth == limitWidth) {
            mLines = mLastCalLines;
            return mLastCalContentWidth;
        }
        mLastCalLimitWidth = limitWidth;
        List<QMUIQQFaceCompiler.Element> elements = mElementList.getElements();
        mSpanInfos.clear();
        mCurrentCalLine = 1;
        mCurrentCalWidth = getPaddingLeft();
        calculateLinesInner(elements, limitWidth);
        if (mCurrentCalLine != mLines) {
            if (mListener != null) {
                mListener.onCalculateLinesChange(mCurrentCalLine);
            }
            mLines = mCurrentCalLine;
        }

        if (mLines == 1) {
            mLastCalContentWidth = mCurrentCalWidth + getPaddingRight();
        } else {
            mLastCalContentWidth = limitWidth;
        }
        mLastCalLines = mLines;

        return mLastCalContentWidth;
    }

    private void calculateNeedDrawLine() {
        mNeedDrawLine = mLines;
        if (mIsSingleLine) {
            mNeedDrawLine = Math.min(1, mLines);
        } else if (mMaxLine < mLines) {
            mNeedDrawLine = mMaxLine;
        }

        mIsNeedEllipsize = mLines > mNeedDrawLine;
    }

    private void calculateLinesInner(List<QMUIQQFaceCompiler.Element> elements, int limitWidth) {
        QMUIQQFaceCompiler.Element element;
        int widthStart = getPaddingLeft(), widthEnd = limitWidth - getPaddingRight();
        for (int i = 0; i < elements.size(); i++) {
            if (mJumpHandleMeasureAndDraw) {
                break;
            }
//            if (mCurrentCalLine > mMaxLine && mEllipsize == TextUtils.TruncateAt.END) {
//                // 如果超过最大行数，就打断测量，但这样存在的问题是getLines获取不到真实的行数
//                break;
//            }
            element = elements.get(i);
            if (element.getType() == QMUIQQFaceCompiler.ElementType.DRAWABLE) {
                if (mCurrentCalWidth + mQQFaceSize > widthEnd) {
                    gotoCalNextLine(widthStart);
                    mCurrentCalWidth += mQQFaceSize;
                } else if (mCurrentCalWidth + mQQFaceSize == widthEnd) {
                    gotoCalNextLine(widthStart);
                } else {
                    mCurrentCalWidth += mQQFaceSize;
                }
                if (widthEnd - widthStart < mQQFaceSize) {
                    // 一个表情的宽度都容不下
                    mJumpHandleMeasureAndDraw = true;
                }
            } else if (element.getType() == QMUIQQFaceCompiler.ElementType.TEXT) {
                CharSequence text = element.getText();
                measureText(text, widthStart, widthEnd);
            } else if (element.getType() == QMUIQQFaceCompiler.ElementType.SPAN) {
                QMUIQQFaceCompiler.ElementList spanElementList = element.getChildList();
                ITouchableSpan span = element.getTouchableSpan();
                if (spanElementList != null && spanElementList.getElements().size() > 0) {
                    if (span == null) {
                        calculateLinesInner(spanElementList.getElements(), limitWidth);
                        continue;
                    }
                    SpanInfo spanInfo = new SpanInfo(span);
                    spanInfo.setStart(mCurrentCalLine, mCurrentCalWidth);
                    calculateLinesInner(spanElementList.getElements(), limitWidth);
                    spanInfo.setEnd(mCurrentCalLine, mCurrentCalWidth);
                    mSpanInfos.add(spanInfo);
                }
            } else if (element.getType() == QMUIQQFaceCompiler.ElementType.NEXTLINE) {
                gotoCalNextLine(widthStart);
            } else if (element.getType() == QMUIQQFaceCompiler.ElementType.SPECIAL_BOUNDS_DRAWABLE) {
                Drawable drawable = element.getSpecialBoundsDrawable();
                int width = drawable.getIntrinsicWidth();
                if (i == 0 || i == elements.size() - 1) {
                    width += mSpecialDrawablePadding;
                } else {
                    width += mSpecialDrawablePadding * 2;
                }
                if (mCurrentCalWidth + width > widthEnd) {
                    gotoCalNextLine(widthStart);
                    mCurrentCalWidth += width;
                } else if (mCurrentCalWidth + width == widthEnd) {
                    gotoCalNextLine(widthStart);
                } else {
                    mCurrentCalWidth += width;
                }
                if (widthEnd - widthStart < width) {
                    // 一个表情的宽度都容不下
                    mJumpHandleMeasureAndDraw = true;
                }
            }
        }
    }

    private boolean isElementEmpty() {
        return mElementList == null ||
                mElementList.getElements() == null ||
                mElementList.getElements().isEmpty();
    }

    private void setContentCalMaxWidth(int width) {
        mContentCalMaxWidth = Math.max(width, mContentCalMaxWidth);
    }

    private void gotoCalNextLine(int widthStart) {
        mCurrentCalLine++;
        setContentCalMaxWidth(mCurrentCalWidth);
        mCurrentCalWidth = widthStart;
    }

    private void measureText(CharSequence text, int widthStart, int widthEnd) {
        int textWidth = (int) Math.ceil(mPaint.measureText(text, 0, text.length()));
        int breakPoint;
        if (mCurrentCalWidth >= widthEnd) {
            gotoCalNextLine(widthStart);
        }
        int loopCount = 0;
        while (textWidth + mCurrentCalWidth > widthEnd) {
            loopCount++;
            if (loopCount >= 10 && loopCount % 10 == 0) {
                QMUILog.d(TAG, "measureText: text = %s, mCurrentCalWidth = %d, " +
                                "widthStart = %d, widthEnd = %d,loopCount = %d",
                        text, mCurrentCalWidth, widthStart, widthEnd, loopCount);
            }
            breakPoint = mPaint.breakText(text, 0, text.length(), true, widthEnd - mCurrentCalWidth, null);
            if (breakPoint == 0 && mCurrentCalWidth == widthStart) {
                // mCurrentCalWidth已经是最小值，但又一个字都容纳不下，只能说明widthEnd太小，可能还在测量中
                mJumpHandleMeasureAndDraw = true;
                return;
            }
            gotoCalNextLine(widthStart);
            text = text.subSequence(breakPoint, text.length());
            textWidth = (int) Math.ceil((mPaint.measureText(text, 0, text.length())));
        }
        mCurrentCalWidth += textWidth;
    }

    public void setListener(QQFaceViewListener listener) {
        mListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        long start = System.currentTimeMillis();
        mJumpHandleMeasureAndDraw = false;
        calculateFontHeight();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        Log.i(TAG, "widthSize = " + widthSize + "; heightSize = " + heightSize);

        mLines = 0;
        int width, height;
        switch (widthMode) {
            case AT_MOST:
            default:
                if (mOriginText == null || mOriginText.length() == 0) {
                    width = 0;
                } else {
                    width = calculateLinesAndContentWidth(Math.min(widthSize, mMaxWidth));
                }
                break;
            case MeasureSpec.EXACTLY:
            case MeasureSpec.UNSPECIFIED:
                width = widthSize;
                calculateLinesAndContentWidth(width);
        }

        if (mJumpHandleMeasureAndDraw) {
            setMeasuredDimension(width, heightMode == AT_MOST ? 0 : heightSize);
            return;
        }

        calculateNeedDrawLine();

        switch (heightMode) {
            case AT_MOST:
            case MeasureSpec.UNSPECIFIED:
            default:
                height = getPaddingTop() + getPaddingBottom();
                if (mNeedDrawLine < 2) {
                    height += mNeedDrawLine * mFontHeight;
                } else {
                    height += (mNeedDrawLine - 1) * (mFontHeight + mLineSpace) + mFontHeight;
                }
                break;
            case MeasureSpec.EXACTLY:
                height = heightSize;
                break;
        }
        setMeasuredDimension(width, height);
        Log.i(TAG, "mLines = " + mLines + " ; width = " + width + " ; height = " + height +
                "; measure time = " + (System.currentTimeMillis() - start));
    }

//    private int getParagraphCount(){
//        if(mElementList == null || mElementList.getElements() == null || mElementList.getElements().isEmpty()){
//            return 0;
//        }
//        List<QMUIQQFaceCompiler.Element> elementList = mElementList.getElements();
//        int paragraphCount = 0;
//        for(int i = 0; i < elementList.size(); i++){
//            QMUIQQFaceCompiler.Element element = elementList.get(i);
//            if(i == elementList.size() - 1){
//                if(element.getType() != QMUIQQFaceCompiler.ElementType.NEXTLINE){
//                    paragraphCount ++;
//                }
//            }else{
//                if(element.getType() == QMUIQQFaceCompiler.ElementType.NEXTLINE){
//                    paragraphCount++;
//                }
//            }
//        }
//        return paragraphCount;
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mJumpHandleMeasureAndDraw || mOriginText == null || mLines == 0 || isElementEmpty()) {
            return;
        }
        long start = System.currentTimeMillis();
        List<QMUIQQFaceCompiler.Element> elements = mElementList.getElements();
        mCurrentDrawBaseLine = getPaddingTop() + mFirstBaseLine;
        mCurrentDrawLine = 1;
        mCurrentDrawUsedWidth = getPaddingLeft();
        mIsExecutedMiddleEllipsize = false;
        drawElements(canvas, elements, getWidth() - getPaddingLeft() - getPaddingRight());
        Log.i(TAG, "onDraw spend time = " + (System.currentTimeMillis() - start));
    }

    private int mCurrentDrawBaseLine;
    private int mCurrentDrawLine;
    private int mCurrentDrawUsedWidth;
    private boolean mIsInDrawSpan = false;
    private QMUITouchableSpan mCurrentDrawSpan;

    private void drawElements(Canvas canvas, List<QMUIQQFaceCompiler.Element> elements, int usefulWidth) {
        int startLeft = getPaddingLeft(), endWidth = usefulWidth + startLeft;
        if (mIsNeedEllipsize && mEllipsize == TextUtils.TruncateAt.START) {
            canvas.drawText(mEllipsizeText, 0, mEllipsizeText.length(), startLeft, mFirstBaseLine, mPaint);
        }

        QMUIQQFaceCompiler.Element element;
        for (int i = 0; i < elements.size(); i++) {
            element = elements.get(i);
            QMUIQQFaceCompiler.ElementType type = element.getType();
            if (type == QMUIQQFaceCompiler.ElementType.DRAWABLE) {
                onDrawQQFace(canvas, element.getDrawableRes(), null, startLeft, endWidth, i == 0, i == elements.size() - 1);
            } else if (type == QMUIQQFaceCompiler.ElementType.SPECIAL_BOUNDS_DRAWABLE) {
                onDrawQQFace(canvas, 0, element.getSpecialBoundsDrawable(), startLeft, endWidth, i == 0, i == elements.size() - 1);
            } else if (type == QMUIQQFaceCompiler.ElementType.TEXT) {
                CharSequence text = element.getText();
                onDrawText(canvas, text, startLeft, endWidth);
            } else if (type == QMUIQQFaceCompiler.ElementType.SPAN) {
                QMUIQQFaceCompiler.ElementList spanElementList = element.getChildList();
                mCurrentDrawSpan = element.getTouchableSpan();
                if (spanElementList != null && !spanElementList.getElements().isEmpty()) {
                    if (mCurrentDrawSpan == null) {
                        drawElements(canvas, spanElementList.getElements(), usefulWidth);
                        continue;
                    }
                    mIsInDrawSpan = true;
                    @ColorInt int spanColor = mCurrentDrawSpan.isPressed() ?
                            mCurrentDrawSpan.getPressedTextColor() :
                            mCurrentDrawSpan.getNormalTextColor();
                    mPaint.setColor(spanColor == 0 ? mTextColor : spanColor);
                    drawElements(canvas, spanElementList.getElements(), usefulWidth);
                    mPaint.setColor(mTextColor);
                    mIsInDrawSpan = false;
                }
            } else if (type == QMUIQQFaceCompiler.ElementType.NEXTLINE) {
                int ellipsizeLength = mEllipsizeTextLength + mMoreActionTextLength;
                if (mIsNeedEllipsize && mEllipsize == TextUtils.TruncateAt.END &&
                        mCurrentDrawUsedWidth <= endWidth - ellipsizeLength && mCurrentDrawLine == mNeedDrawLine) {
                    drawText(canvas, mEllipsizeText, 0, mEllipsizeText.length(), mEllipsizeTextLength);
                    mCurrentDrawUsedWidth += mEllipsizeTextLength;
                    drawMoreActionText(canvas);
                    return;
                }
                toNewDrawLine(startLeft, true);
            }
        }
    }

    private void drawMoreActionText(Canvas canvas) {
        if (!QMUILangHelper.isNullOrEmpty(mMoreActionText)) {
            mPaint.setColor(mMoreActionColor);
            canvas.drawText(mMoreActionText, 0, mMoreActionText.length(), mCurrentDrawUsedWidth, mCurrentDrawBaseLine, mPaint);
            mPaint.setColor(mTextColor);
        }
    }

    private void toNewDrawLine(int startLeft) {
        toNewDrawLine(startLeft, false);
    }

    /**
     * 控制段落切换
     */
    private void toNewDrawLine(int startLeft, boolean paragraph) {
        int addOn = (paragraph ? mParagraphSpace : 0) + mLineSpace;
        mCurrentDrawLine++;
        if (mIsNeedEllipsize) {
            if (mEllipsize == TextUtils.TruncateAt.START) {
                if (mCurrentDrawLine > mLines - mNeedDrawLine + 1) {
                    mCurrentDrawBaseLine += mFontHeight + addOn;
                }
            } else if (mEllipsize == TextUtils.TruncateAt.MIDDLE) {
                if (!mIsExecutedMiddleEllipsize || mMiddleEllipsizeWidthRecord == -1) {
                    mCurrentDrawBaseLine += mFontHeight + addOn;
                }
            } else {
                mCurrentDrawBaseLine += mFontHeight + addOn;
            }
        } else {
            mCurrentDrawBaseLine += mFontHeight + addOn;
        }
        mCurrentDrawUsedWidth = startLeft;
    }

    private void onRealDrawText(Canvas canvas, CharSequence text, int widthStart, int widthEnd) {
        int textWidth = (int) Math.ceil(mPaint.measureText(text, 0, text.length()));
        int breakPoint;
        while (textWidth + mCurrentDrawUsedWidth > widthEnd) {
            breakPoint = mPaint.breakText(text, 0, text.length(), true,
                    widthEnd - mCurrentDrawUsedWidth, null);
            drawText(canvas, text, 0, breakPoint, widthEnd - mCurrentDrawUsedWidth);
            toNewDrawLine(widthStart);
            text = text.subSequence(breakPoint, text.length());
            textWidth = (int) Math.ceil(mPaint.measureText(text, 0, text.length()));
        }
        drawText(canvas, text, 0, text.length(), textWidth);
        mCurrentDrawUsedWidth += textWidth;
    }

    private int getMiddleEllipsizeLine() {
        int ellipsizeLine;
        if (mNeedDrawLine % 2 == 0) {
            ellipsizeLine = mNeedDrawLine / 2;
        } else {
            ellipsizeLine = (mNeedDrawLine + 1) / 2;
        }
        return ellipsizeLine;
    }


    private int mMiddleEllipsizeWidthRecord = -1;
    private boolean mIsExecutedMiddleEllipsize = false;

    private void onDrawText(Canvas canvas, CharSequence text, int widthStart, int widthEnd) {
        if (mIsNeedEllipsize) {
            if (mEllipsize == TextUtils.TruncateAt.START) {
                if (mCurrentDrawLine > mLines - mNeedDrawLine) {
                    onRealDrawText(canvas, text, widthStart, widthEnd);
                } else if (mCurrentDrawLine < mLines - mNeedDrawLine) {
                    int textWidth = (int) Math.ceil(mPaint.measureText(text, 0, text.length()));
                    if (textWidth + mCurrentDrawUsedWidth > widthEnd) {
                        int breakPoint = mPaint.breakText(text, 0, text.length(), true,
                                widthEnd - mCurrentDrawUsedWidth, null);
                        toNewDrawLine(widthStart);
                        onDrawText(canvas, text.subSequence(breakPoint, text.length()), widthStart, widthEnd);
                    } else {
                        mCurrentDrawUsedWidth += textWidth;
                    }
                } else {
                    int textWidth = (int) Math.ceil(mPaint.measureText(text, 0, text.length()));
                    int needStopWidth = mCurrentCalWidth + mEllipsizeTextLength
                            + QMUIDisplayHelper.dp2px(getContext(), 5); // 测量会存在误差
                    if (textWidth + mCurrentDrawUsedWidth < needStopWidth) {
                        mCurrentDrawUsedWidth += textWidth;
                    } else if (textWidth + mCurrentDrawUsedWidth == needStopWidth) {
                        toNewDrawLine(widthStart + mEllipsizeTextLength);
                    } else {
                        int breakPoint = mPaint.breakText(text, 0, text.length(), true,
                                needStopWidth - mCurrentDrawUsedWidth, null);
                        toNewDrawLine(widthStart + mEllipsizeTextLength);
                        onDrawText(canvas, text.subSequence(breakPoint, text.length()), widthStart, widthEnd);
                    }
                }
            } else if (mEllipsize == TextUtils.TruncateAt.MIDDLE) {
                int ellipsizeLine = getMiddleEllipsizeLine();
                int textWidth = (int) Math.ceil(mPaint.measureText(text, 0, text.length()));
                int breakPoint;
                if (mCurrentDrawLine < ellipsizeLine) {
                    if (textWidth + mCurrentDrawUsedWidth > widthEnd) {
                        breakPoint = mPaint.breakText(text, 0, text.length(), true,
                                widthEnd - mCurrentDrawUsedWidth, null);
                        drawText(canvas, text, 0, breakPoint, widthEnd - mCurrentDrawUsedWidth);
                        toNewDrawLine(widthStart);
                        text = text.subSequence(breakPoint, text.length());
                        onDrawText(canvas, text, widthStart, widthEnd);
                    } else {
                        drawText(canvas, text, 0, text.length(), textWidth);
                        mCurrentDrawUsedWidth += textWidth;
                    }
                } else if (mCurrentDrawLine == ellipsizeLine) {
                    int needStop = getWidth() / 2 - mEllipsizeTextLength / 2;
                    if (mIsExecutedMiddleEllipsize) {
                        handleTextAfterMiddleEllipsize(canvas, text, widthStart,
                                widthEnd, ellipsizeLine, textWidth);
                    } else if (textWidth + mCurrentDrawUsedWidth < needStop) {
                        drawText(canvas, text, 0, text.length(), textWidth);
                        mCurrentDrawUsedWidth += textWidth;
                    } else if (textWidth + mCurrentDrawUsedWidth == needStop) {
                        drawText(canvas, text, 0, text.length(), textWidth);
                        mCurrentDrawUsedWidth += textWidth;
                        drawText(canvas, mEllipsizeText, 0, mEllipsizeText.length(), mEllipsizeTextLength);
                        mCurrentDrawUsedWidth += mEllipsizeTextLength;
                        mMiddleEllipsizeWidthRecord = mCurrentDrawUsedWidth;
                        mIsExecutedMiddleEllipsize = true;
                    } else {
                        breakPoint = mPaint.breakText(text, 0, text.length(), true, needStop - mCurrentDrawUsedWidth, null);
                        textWidth = (int) Math.ceil(mPaint.measureText(text, 0, breakPoint));
                        drawText(canvas, text, 0, breakPoint, textWidth);
                        mCurrentDrawUsedWidth += textWidth;
                        drawText(canvas, mEllipsizeText, 0, mEllipsizeText.length(), mEllipsizeTextLength);
                        mCurrentDrawUsedWidth += mEllipsizeTextLength;
                        mMiddleEllipsizeWidthRecord = mCurrentDrawUsedWidth;
                        mIsExecutedMiddleEllipsize = true;
                        if (breakPoint < text.length()) {
                            text = text.subSequence(breakPoint, text.length());
                            textWidth = (int) Math.ceil(mPaint.measureText(text, 0, text.length()));
                            handleTextAfterMiddleEllipsize(canvas, text, widthStart,
                                    widthEnd, ellipsizeLine, textWidth);
                        }
                    }
                } else {
                    handleTextAfterMiddleEllipsize(canvas, text, widthStart,
                            widthEnd, ellipsizeLine, textWidth);
                }
            } else {
                int textWidth = (int) Math.ceil(mPaint.measureText(text, 0, text.length()));
                int breakPoint;
                if (mCurrentDrawLine == mNeedDrawLine) {
                    int ellipsizeLength = mEllipsizeTextLength + mMoreActionTextLength;
                    if (textWidth + mCurrentDrawUsedWidth >= widthEnd - ellipsizeLength) {
                        if (textWidth + mCurrentDrawUsedWidth > widthEnd - ellipsizeLength) {
                            breakPoint = mPaint.breakText(text, 0, text.length(), true,
                                    widthEnd - mCurrentDrawUsedWidth - ellipsizeLength, null);
                            drawText(canvas, text, 0, breakPoint, textWidth);
                            textWidth = (int) Math.ceil(mPaint.measureText(text, 0, breakPoint));
                            mCurrentDrawUsedWidth += textWidth;
                        } else {
                            drawText(canvas, text, 0, text.length(), textWidth);
                            mCurrentDrawUsedWidth += textWidth;
                        }
                        drawText(canvas, mEllipsizeText, 0, mEllipsizeText.length(), mEllipsizeTextLength);
                        mCurrentDrawUsedWidth += mEllipsizeTextLength;
                        drawMoreActionText(canvas);
                        // 依然要去到下一行，使得后续不会进入这个逻辑
                        toNewDrawLine(widthStart);
                    } else {
                        drawText(canvas, text, 0, text.length(), textWidth);
                        mCurrentDrawUsedWidth += textWidth;
                    }
                } else if (mCurrentDrawLine < mNeedDrawLine) {
                    if (textWidth + mCurrentDrawUsedWidth > widthEnd) {
                        breakPoint = mPaint.breakText(text, 0, text.length(), true,
                                widthEnd - mCurrentDrawUsedWidth, null);
                        drawText(canvas, text, 0, breakPoint, widthEnd - mCurrentDrawUsedWidth);
                        toNewDrawLine(widthStart);
                        text = text.subSequence(breakPoint, text.length());
                        onDrawText(canvas, text, widthStart, widthEnd);
                    } else {
                        drawText(canvas, text, 0, text.length(), textWidth);
                        mCurrentDrawUsedWidth += textWidth;
                    }
                }
            }

        } else {
            onRealDrawText(canvas, text, widthStart, widthEnd);
        }
    }

    private void handleTextAfterMiddleEllipsize(Canvas canvas, CharSequence text,
                                                int widthStart, int widthEnd, int ellipsizeLine, int textWidth) {
        if (mMiddleEllipsizeWidthRecord == -1) {
            onRealDrawText(canvas, text, widthStart, widthEnd);
            return;
        }
        int endLines = mNeedDrawLine - ellipsizeLine;
        int breakPoint;
        int borrowWidth = (widthEnd - mMiddleEllipsizeWidthRecord) - mCurrentCalWidth;
        int needStopLine = borrowWidth > 0 ? mLines - endLines - 1 : mLines - endLines;
        int needStopWidth = (borrowWidth > 0 ? widthEnd - borrowWidth :
                mMiddleEllipsizeWidthRecord - (widthEnd - mCurrentCalWidth)) +
                QMUIDisplayHelper.dp2px(getContext(), 5);

        if (mCurrentDrawLine < needStopLine) {
            if (textWidth + mCurrentDrawUsedWidth > widthEnd) {
                breakPoint = mPaint.breakText(text, 0, text.length(), true,
                        widthEnd - mCurrentDrawUsedWidth, null);
                toNewDrawLine(widthStart);
                onDrawText(canvas, text.subSequence(breakPoint, text.length()), widthStart, widthEnd);
            } else {
                mCurrentDrawUsedWidth += textWidth;
            }
        } else if (mCurrentDrawLine == needStopLine) {
            if (textWidth + mCurrentDrawUsedWidth < needStopWidth) {
                mCurrentDrawUsedWidth += textWidth;
            } else if (textWidth + mCurrentDrawUsedWidth == needStopWidth) {
                mCurrentDrawUsedWidth = mMiddleEllipsizeWidthRecord;
                mMiddleEllipsizeWidthRecord = -1;
                mLastNeedStopLineRecord = needStopLine;
            } else {
                breakPoint = mPaint.breakText(text, 0, text.length(), true,
                        needStopWidth - mCurrentDrawUsedWidth, null);
                mCurrentDrawUsedWidth = mMiddleEllipsizeWidthRecord;
                mMiddleEllipsizeWidthRecord = -1;
                mLastNeedStopLineRecord = needStopLine;
                onRealDrawText(canvas, text.subSequence(breakPoint, text.length()), widthStart, widthEnd);
            }
        } else {
            onRealDrawText(canvas, text, widthStart, widthEnd);
        }
    }

    private void drawText(Canvas canvas, CharSequence text, int start, int end, int textWidth) {
        if (mIsInDrawSpan && mCurrentDrawSpan != null) {
            @ColorInt int color = mCurrentDrawSpan.isPressed() ? mCurrentDrawSpan.getPressedBackgroundColor() :
                    mCurrentDrawSpan.getNormalBackgroundColor();
            if (color != Color.TRANSPARENT) {
                mSpanBgPaint.setColor(color);
                canvas.drawRect(mCurrentDrawUsedWidth, mCurrentDrawBaseLine - mFirstBaseLine,
                        mCurrentDrawUsedWidth + textWidth,
                        mCurrentDrawBaseLine - mFirstBaseLine + mFontHeight, mSpanBgPaint);
            }
        }
        canvas.drawText(text, start, end, mCurrentDrawUsedWidth, mCurrentDrawBaseLine, mPaint);
    }

    private void onDrawQQFace(Canvas canvas, int res, Drawable specialDrawable, int widthStart, int widthEnd, boolean isFirst, boolean isLast) {
        int size = res != -1 ? mQQFaceSize : specialDrawable.getIntrinsicWidth() + (isFirst || isLast ? mSpecialDrawablePadding : mSpecialDrawablePadding * 2);
        if (mIsNeedEllipsize) {
            if (mEllipsize == TextUtils.TruncateAt.START) {
                if (mCurrentDrawLine > mLines - mNeedDrawLine) {
                    onRealDrawQQFace(canvas, res, specialDrawable, mNeedDrawLine - mLines, widthStart, widthEnd, isFirst, isLast);
                } else if (mCurrentDrawLine < mLines - mNeedDrawLine) {
                    if (size + mCurrentDrawUsedWidth > widthEnd) {
                        toNewDrawLine(widthStart);
                        onDrawQQFace(canvas, res, specialDrawable, widthStart, widthEnd, isFirst, isLast);
                    } else {
                        mCurrentDrawUsedWidth += size;
                    }
                } else {
                    int needStopWidth = mCurrentCalWidth + mEllipsizeTextLength;
                    if (size + mCurrentDrawUsedWidth < needStopWidth) {
                        mCurrentDrawUsedWidth += size;
                    } else {
                        toNewDrawLine(widthStart + mEllipsizeTextLength);
                    }
                }
            } else if (mEllipsize == TextUtils.TruncateAt.MIDDLE) {
                int ellipsizeLine = getMiddleEllipsizeLine();
                if (mCurrentDrawLine < ellipsizeLine) {
                    if (size + mCurrentDrawUsedWidth > widthEnd) {
                        onRealDrawQQFace(canvas, res, specialDrawable, 0, widthStart, widthEnd, isFirst, isLast);
                    } else {
                        drawQQFace(canvas, res, specialDrawable, mCurrentDrawLine, isFirst, isLast);
                        mCurrentDrawUsedWidth += size;
                    }
                } else if (mCurrentDrawLine == ellipsizeLine) {
                    int needStop = getWidth() / 2 - mEllipsizeTextLength / 2;
                    if (mIsExecutedMiddleEllipsize) {
                        handleQQFaceAfterMiddleEllipsize(canvas, res, specialDrawable, widthStart, widthEnd, ellipsizeLine, isFirst, isLast);
                    } else if (size + mCurrentDrawUsedWidth < needStop) {
                        drawQQFace(canvas, res, specialDrawable, mCurrentDrawLine, isFirst, isLast);
                        mCurrentDrawUsedWidth += size;
                    } else if (size + mCurrentDrawUsedWidth == needStop) {
                        drawQQFace(canvas, res, specialDrawable, mCurrentDrawLine, isFirst, isLast);
                        mCurrentDrawUsedWidth += size;
                        drawText(canvas, mEllipsizeText, 0, mEllipsizeText.length(), mEllipsizeTextLength);
                        mCurrentDrawUsedWidth += mEllipsizeTextLength;
                        mMiddleEllipsizeWidthRecord = mCurrentDrawUsedWidth;
                        mIsExecutedMiddleEllipsize = true;
                    } else {
                        drawText(canvas, mEllipsizeText, 0, mEllipsizeText.length(), mEllipsizeTextLength);
                        mCurrentDrawUsedWidth += mEllipsizeTextLength;
                        mMiddleEllipsizeWidthRecord = mCurrentDrawUsedWidth;
                        mIsExecutedMiddleEllipsize = true;
                    }
                } else {
                    handleQQFaceAfterMiddleEllipsize(canvas, res, specialDrawable, widthStart, widthEnd, ellipsizeLine, isFirst, isLast);
                }
            } else {
                if (mCurrentDrawLine == mNeedDrawLine) {
                    int ellipsizeLength = mEllipsizeTextLength + mMoreActionTextLength;
                    if (size + mCurrentDrawUsedWidth >= widthEnd - ellipsizeLength) {
                        if (size + mCurrentDrawUsedWidth == widthEnd - ellipsizeLength) {
                            drawQQFace(canvas, res, specialDrawable, mCurrentDrawLine, isFirst, isLast);
                            mCurrentDrawUsedWidth += size;
                        }
                        drawText(canvas, mEllipsizeText, 0, mEllipsizeText.length(), mEllipsizeTextLength);
                        mCurrentDrawUsedWidth += mEllipsizeTextLength;
                        drawMoreActionText(canvas);
                        // 去新的一行，避免再次走入这一行的逻辑
                        toNewDrawLine(widthStart);
                    } else {
                        drawQQFace(canvas, res, specialDrawable, mCurrentDrawLine, isFirst, isLast);
                        mCurrentDrawUsedWidth += size;
                    }
                } else if (mCurrentDrawLine < mNeedDrawLine) {
                    if (size + mCurrentDrawUsedWidth > widthEnd) {
                        onRealDrawQQFace(canvas, res, specialDrawable, 0, widthStart, widthEnd, isFirst, isLast);
                    } else {
                        drawQQFace(canvas, res, specialDrawable, mCurrentDrawLine, isFirst, isLast);
                        mCurrentDrawUsedWidth += size;
                    }
                }
            }

        } else {
            onRealDrawQQFace(canvas, res, specialDrawable, 0, widthStart, widthEnd, isFirst, isLast);
        }
    }

    private int mLastNeedStopLineRecord = -1;

    private void handleQQFaceAfterMiddleEllipsize(Canvas canvas, int res, Drawable specialDrawable, int widthStart,
                                                  int widthEnd, int ellipsizeLine, boolean isFirst, boolean isLast) {
        int size = res != 0 ? mQQFaceSize : specialDrawable.getIntrinsicWidth() + (isFirst || isLast ? mSpecialDrawablePadding : mSpecialDrawablePadding * 2);
        if (mMiddleEllipsizeWidthRecord == -1) {
            onRealDrawQQFace(canvas, res, specialDrawable, ellipsizeLine - mLastNeedStopLineRecord, widthStart, widthEnd, isFirst, isLast);
            return;
        }

        int endLines = mNeedDrawLine - ellipsizeLine;
        int borrowWidth = (widthEnd - mMiddleEllipsizeWidthRecord) - mCurrentCalWidth;
        int needStopLine = borrowWidth > 0 ? mLines - endLines - 1 : mLines - endLines;
        int needStopWidth = (borrowWidth > 0 ? widthEnd - borrowWidth :
                mMiddleEllipsizeWidthRecord - (widthEnd - mCurrentCalWidth)) +
                QMUIDisplayHelper.dp2px(getContext(), 5);

        if (mCurrentDrawLine < needStopLine) {
            if (size + mCurrentDrawUsedWidth > widthEnd) {
                toNewDrawLine(widthStart);
                onDrawQQFace(canvas, res, specialDrawable, widthStart, widthEnd, isFirst, isLast);
            } else {
                mCurrentDrawUsedWidth += size;
            }
        } else if (mCurrentDrawLine == needStopLine) {
            if (size + mCurrentDrawUsedWidth < needStopWidth) {
                mCurrentDrawUsedWidth += size;
            } else {
                mCurrentDrawUsedWidth = mMiddleEllipsizeWidthRecord;
                mMiddleEllipsizeWidthRecord = -1;
                mLastNeedStopLineRecord = needStopLine;
            }
        } else {
            onRealDrawQQFace(canvas, res, specialDrawable, ellipsizeLine - needStopLine, widthStart, widthEnd, isFirst, isLast);
        }
    }

    private void onRealDrawQQFace(Canvas canvas, int res, Drawable specialDrawable, int adjustLine,
                                  int widthStart, int widthEnd, boolean isFirst, boolean isLast) {
        int size = res != 0 ? mQQFaceSize : specialDrawable.getIntrinsicWidth() + (isFirst || isLast ? mSpecialDrawablePadding : mSpecialDrawablePadding * 2);
        if (mCurrentDrawUsedWidth + size > widthEnd) {
            toNewDrawLine(widthStart);
        }
        drawQQFace(canvas, res, specialDrawable, mCurrentDrawLine + adjustLine, isFirst, isLast);
        mCurrentDrawUsedWidth += size;
    }

    private void drawQQFace(Canvas canvas, int res, Drawable specialDrawable, int line, boolean isFirst, boolean isLast) {
        Drawable drawable = res != 0 ? ContextCompat.getDrawable(getContext(), res) : specialDrawable;
        int size = res != 0 ? mQQFaceSize : specialDrawable.getIntrinsicWidth() + (isFirst || isLast ? mSpecialDrawablePadding : mSpecialDrawablePadding * 2);
        if (drawable == null) {
            return;
        }
        int drawableTop;
        if (res != 0) {
            drawableTop = (mFontHeight - mQQFaceSize) / 2;
            drawable.setBounds(0, drawableTop, mQQFaceSize, drawableTop + mQQFaceSize);
        } else {
            drawableTop = (mFontHeight - drawable.getIntrinsicHeight()) / 2;
            int left = isLast ? mSpecialDrawablePadding : 0;
            drawable.setBounds(left, drawableTop, left + drawable.getIntrinsicWidth(), drawableTop + drawable.getIntrinsicHeight());
        }
        int top = getPaddingTop();
        if (line > 1) {
            top = (line - 1) * (mFontHeight + mLineSpace) + top;
        }
        canvas.save();
        canvas.translate(mCurrentDrawUsedWidth, top);
        if (mIsInDrawSpan && mCurrentDrawSpan != null) {
            @ColorInt int color = mCurrentDrawSpan.isPressed() ? mCurrentDrawSpan.getPressedBackgroundColor() :
                    mCurrentDrawSpan.getNormalBackgroundColor();
            if (color != Color.TRANSPARENT) {
                mSpanBgPaint.setColor(color);
                canvas.drawRect(0, 0, size, mFontHeight, mSpanBgPaint);
            }
        }
        drawable.draw(canvas);
        canvas.restore();
    }

    private class SpanInfo {
        private ITouchableSpan mTouchableSpan;
        private int mStartPoint;
        private int mEndPoint;
        private int mStartLine;
        private int mEndLine;

        public SpanInfo(ITouchableSpan touchableSpan) {
            mTouchableSpan = touchableSpan;
        }

        public void setStart(int startLine, int startPoint) {
            mStartLine = startLine;
            mStartPoint = startPoint;
        }

        public void setPressed(boolean pressed) {
            mTouchableSpan.setPressed(pressed);
        }

        public void setEnd(int endLine, int endPoint) {
            mEndLine = endLine;
            mEndPoint = endPoint;
        }

        public void onClick() {
            mTouchableSpan.onClick(QMUIQQFaceView.this);
        }

        public void invalidateSpan() {
            int top = getPaddingTop();
            if (mStartLine > 1) {
                top = (mStartLine - 1) * (mFontHeight + mLineSpace) + top;
            }

            int bottom = (mEndLine - 1) * (mFontHeight + mLineSpace) + top + mFontHeight;
            Rect bounds = new Rect();
            bounds.top = top;
            bounds.bottom = bottom;
            bounds.left = getPaddingLeft();
            bounds.right = getWidth() - getPaddingRight();
            if (mStartLine == mEndLine) {
                bounds.left = mStartPoint;
                bounds.right = mEndPoint;
            }
            invalidate(bounds);
        }

        @SuppressWarnings("SimplifiableIfStatement")
        public boolean onTouch(int x, int y) {
            int top = getPaddingTop();
            if (mStartLine > 1) {
                top = (mStartLine - 1) * (mFontHeight + mLineSpace) + top;
            }

            int bottom = (mEndLine - 1) * (mFontHeight + mLineSpace) + top + mFontHeight;

            if (y < top || y > bottom) {
                return false;
            }

            if (mStartLine == mEndLine) {
                return x >= mStartPoint && x <= mEndPoint;
            }

            int startLineBottom = top + mFontHeight;
            int endLineTop = bottom - mFontHeight;
            if (y > startLineBottom && y < endLineTop) {
                //noinspection SimplifiableIfStatement
                if (mEndLine - mStartLine == 1) {
                    return x >= mStartPoint && x <= mEndPoint;
                }
                return true;
            } else if (y <= startLineBottom) {
                return x >= mStartPoint;
            } else {
                return x <= mEndPoint;
            }

        }
    }

    public static class PressCancelAction implements Runnable {
        private WeakReference<SpanInfo> mWeakReference;

        public PressCancelAction(SpanInfo spanInfo) {
            mWeakReference = new WeakReference<>(spanInfo);
        }

        @Override
        public void run() {
            SpanInfo spanInfo = mWeakReference.get();
            if (spanInfo != null) {
                spanInfo.setPressed(false);
                spanInfo.invalidateSpan();
            }
        }
    }

    public interface QQFaceViewListener {
        void onCalculateLinesChange(int lines);
    }
}
