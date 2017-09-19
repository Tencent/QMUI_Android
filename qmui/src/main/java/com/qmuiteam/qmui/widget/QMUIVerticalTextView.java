package com.qmuiteam.qmui.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 在 {@link TextView} 的基础上支持文字竖排
 *
 * <p>默认将文字竖排显示, 可使用 {@link #setVerticalMode(boolean)} 来开启/关闭竖排功能</p>
 */
public class QMUIVerticalTextView extends TextView {

    /**
     * 是否将文字显示成竖排
     */
    private boolean mIsVerticalMode = true;

    private int mLineCount; // 行数
    private float[] mLineWidths; // 下标: 行号; 数组内容: 该行的宽度(由该行最宽的字符决定)
    private int[] mLineBreakIndex; // 下标: 行号; 数组内容: 该行最后一个字符的下标

    public QMUIVerticalTextView(Context context) {
        super(context);
        init();
    }

    public QMUIVerticalTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public QMUIVerticalTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
    }

    @SuppressLint("DrawAllocation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mIsVerticalMode) {
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            float width = getPaddingLeft() + getPaddingRight();
            float height = getPaddingTop() + getPaddingBottom();
            char[] chars = getText().toString().toCharArray();
            final Paint paint = getPaint();
            final Paint.FontMetricsInt fontMetricsInt = paint.getFontMetricsInt();

            final int lineMaxBottom = (heightMode == MeasureSpec.UNSPECIFIED ? Integer.MAX_VALUE : heightSize)
                    - getPaddingBottom();

            float currentLineHeight = getPaddingTop();
            float lineMaxHeight = currentLineHeight;
            int lineIndex = 0;
            mLineCount = 0;
            mLineWidths = new float[chars.length + 1]; // 加1是为了处理高度不够放下一个字的情况,needBreakLine会一直为true直到最后一个字
            mLineBreakIndex = new int[chars.length + 1];
            // 从右向左,从上向下布局
            for (int i = 0; i < chars.length; i++) {
                final char c = chars[i];
                // rotate
//            boolean needRotate = !Caches.isCJK(c);
                boolean needRotate = !isCJKCharacter(c);
                // char height
                float charHeight;
                float charWidth;
                if (needRotate) {
                    charWidth = fontMetricsInt.descent - fontMetricsInt.ascent;
                    charHeight = paint.measureText(chars, i, 1);
                } else {
                    charWidth = paint.measureText(chars, i, 1);
                    charHeight = fontMetricsInt.descent - fontMetricsInt.ascent;
                }

                // is need break line
                boolean needBreakLine = currentLineHeight + charHeight > lineMaxBottom
                        && i > 0; // i > 0 是因为即使在第一列高度不够,也不用换下一列
                if (needBreakLine) {
                    // break line
                    if (lineMaxHeight < currentLineHeight) {
                        lineMaxHeight = currentLineHeight;
                    }
                    mLineBreakIndex[lineIndex] = i - 1;
                    width += mLineWidths[lineIndex];
                    lineIndex++;
                    // reset
                    currentLineHeight = charHeight;
                } else {
                    currentLineHeight += charHeight;
                    if (lineMaxHeight < currentLineHeight) {
                        lineMaxHeight = currentLineHeight;
                    }
                }

                if (mLineWidths[lineIndex] < charWidth) {
                    mLineWidths[lineIndex] = charWidth;
                }
                // last column width
                if (i == chars.length - 1) {
                    width += mLineWidths[lineIndex];
                    height = lineMaxHeight + getPaddingBottom();
                }
            }
            if (chars.length > 0) {
                mLineCount = lineIndex + 1;
                mLineBreakIndex[lineIndex] = chars.length - 1;
            }

            // 计算 lineSpacing
            if (mLineCount > 1) {
                int lineSpacingCount = mLineCount - 1;
                for (int i = 0; i < lineSpacingCount; i++) {
                    width += mLineWidths[i] * (getLineSpacingMultiplier() - 1) + getLineSpacingExtra();
                }
            }

            if (heightMode == MeasureSpec.EXACTLY) {
                height = heightSize;
            } else if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }
            if (widthMode == MeasureSpec.EXACTLY) {
                width = widthSize;
            } else if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(width, widthSize);
            }

            setMeasuredDimension((int) width, (int) height);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onDraw(Canvas canvas) {
        if (!mIsVerticalMode) {
            super.onDraw(canvas);
        } else {
            if (mLineCount == 0) {
                return;
            }

            final TextPaint paint = getPaint();
            paint.setColor(getCurrentTextColor());
            paint.drawableState = getDrawableState();
            final Paint.FontMetricsInt fontMetricsInt = paint.getFontMetricsInt();
            final char[] chars = getText().toString().toCharArray();

            canvas.save();

            int curLine = 0;
            float curLineX = getWidth() - getPaddingRight() - mLineWidths[curLine];
            float curX = curLineX;
            float curY = getPaddingTop();
            for (int i = 0; i < chars.length; i++) {
                final char c = chars[i];
//            boolean needRotate = !Caches.isCJK(c);
                boolean needRotate = !isCJKCharacter(c);
                final int saveCount = canvas.save();
                if (needRotate) {
                    canvas.rotate(90, curX, curY);
                }
                // draw
                float textX = curX;
                float textBaseline = needRotate ?
                        curY - (mLineWidths[curLine] - (fontMetricsInt.bottom - fontMetricsInt.top)) / 2 - fontMetricsInt.descent :
                        curY - fontMetricsInt.ascent;
                canvas.drawText(chars, i, 1, textX, textBaseline, paint);
                canvas.restoreToCount(saveCount);

                // if break line
                boolean hasNextChar = i + 1 < chars.length;
                if (hasNextChar) {
//                boolean breakLine = needBreakLine(i, mLineCharsCount, curLine);
                    boolean nextCharBreakLine = i + 1 > mLineBreakIndex[curLine];
                    if (nextCharBreakLine && curLine + 1 < mLineWidths.length) {
                        // new line
                        curLine++;
                        curLineX -= (mLineWidths[curLine] * getLineSpacingMultiplier() + getLineSpacingExtra());
                        curX = curLineX;
                        curY = getPaddingTop();
                    } else {
                        // move to next char
                        if (needRotate) {
                            curY += paint.measureText(chars, i, 1);
                        } else {
                            curY += fontMetricsInt.descent - fontMetricsInt.ascent;
                        }
                    }
                }
            }

            canvas.restore();
        }
    }

    // This method is copied from moai.ik.helper.CharacterHelper.isCJKCharacter(char input)
    private static boolean isCJKCharacter(char input) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(input);
        //noinspection RedundantIfStatement
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                //全角数字字符和日韩字符
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                //韩文字符集
                || ub == Character.UnicodeBlock.HANGUL_SYLLABLES
                || ub == Character.UnicodeBlock.HANGUL_JAMO
                || ub == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
                //日文字符集
                || ub == Character.UnicodeBlock.HIRAGANA //平假名
                || ub == Character.UnicodeBlock.KATAKANA //片假名
                || ub == Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS
                ) {
            return true;
        } else {
            return false;
        }
        //其他的CJK标点符号，可以不做处理
        //|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
        //|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
    }

    public void setVerticalMode(boolean verticalMode) {
        mIsVerticalMode = verticalMode;
        requestLayout();
    }

    public boolean isVerticalMode() {
        return mIsVerticalMode;
    }

}