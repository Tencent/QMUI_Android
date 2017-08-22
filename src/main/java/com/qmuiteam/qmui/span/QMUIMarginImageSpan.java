package com.qmuiteam.qmui.span;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

/**
 * 支持设置图片左右间距的 ImageSpan
 *
 * @author chantchen
 * @date 2015-12-16
 */
public class QMUIMarginImageSpan extends QMUIAlignMiddleImageSpan {

    private int mSpanMarginLeft = 0;
    private int mSpanMarginRight = 0;
    private int mOffsetY = 0;

    public QMUIMarginImageSpan(Drawable d, int verticalAlignment, int marginLeft, int marginRight) {
        super(d, verticalAlignment);
        mSpanMarginLeft = marginLeft;
        mSpanMarginRight = marginRight;
    }

    public QMUIMarginImageSpan(Drawable d, int verticalAlignment, int marginLeft, int marginRight, int offsetY) {
        this(d, verticalAlignment, marginLeft, marginRight);
        mOffsetY = offsetY;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        if (mSpanMarginLeft != 0 || mSpanMarginRight != 0) {
            super.getSize(paint, text, start, end, fm);
            Drawable d = getDrawable();
            return d.getIntrinsicWidth() + mSpanMarginLeft + mSpanMarginRight;
        } else {
            return super.getSize(paint, text, start, end, fm);
        }
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {
        canvas.save();
        canvas.translate(0, mOffsetY);
        // marginRight不用专门处理，只靠getSize()中改变即可
        super.draw(canvas, text, start, end, x + mSpanMarginLeft, top, y, bottom, paint);
        canvas.restore();
    }
}
