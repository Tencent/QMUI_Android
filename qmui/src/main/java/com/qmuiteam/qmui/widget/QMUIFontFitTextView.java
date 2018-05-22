package com.qmuiteam.qmui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.R;

/**
 * 使 {@link TextView} 在宽度固定的情况下，文字多到一行放不下时能缩小文字大小来自适应
 *
 * http://stackoverflow.com/questions/2617266/how-to-adjust-text-font-size-to-fit-textview
 */
public class QMUIFontFitTextView extends TextView {

    private Paint mTestPaint;
    private float minSize;
    private float maxSize;

    public QMUIFontFitTextView(Context context) {
        this(context, null);
    }

    public QMUIFontFitTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTestPaint = new Paint();
        mTestPaint.set(this.getPaint());

        TypedArray array = context.obtainStyledAttributes(attrs,
                R.styleable.QMUIFontFitTextView);
        minSize = array.getDimensionPixelSize(
                R.styleable.QMUIFontFitTextView_qmui_minTextSize, Math.round(14 * QMUIDisplayHelper.DENSITY));
        maxSize = array.getDimensionPixelSize(
                R.styleable.QMUIFontFitTextView_qmui_maxTextSize, Math.round(18 * QMUIDisplayHelper.DENSITY));
        array.recycle();
        //max size defaults to the initially specified text size unless it is too small
    }

    /* Re size the font so the specified text fits in the text box
     * assuming the text box is the specified width.
     */
    private void refitText(String text, int textWidth) {
        if (textWidth <= 0)
            return;
        int targetWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();
        float hi = maxSize;
        float lo = minSize;
        float size;
        final float threshold = 0.5f; // How close we have to be

        mTestPaint.set(this.getPaint());

        mTestPaint.setTextSize(maxSize);
        if(mTestPaint.measureText(text) <= targetWidth) {
            lo = maxSize;
        } else {
            mTestPaint.setTextSize(minSize);
            if(mTestPaint.measureText(text) < targetWidth) {
                while((hi - lo) > threshold) {
                    size = (hi+lo)/2;
                    mTestPaint.setTextSize(size);
                    if(mTestPaint.measureText(text) >= targetWidth)
                        hi = size; // too big
                    else
                        lo = size; // too small
                }
            }
        }

        // Use lo so that we undershoot rather than overshoot
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, lo);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int height = getMeasuredHeight();
        refitText(this.getText().toString(), parentWidth);
        this.setMeasuredDimension(parentWidth, height);
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        refitText(text.toString(), this.getWidth());
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        if (w != oldw) {
            refitText(this.getText().toString(), w);
        }
    }
}