/*
 * Tencent is pleased to support the open source community by making QMUI_Android available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qmuiteam.qmui.span;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import android.text.style.ReplacementSpan;

/**
 * 支持调整字体大小的 span。{@link android.text.style.AbsoluteSizeSpan} 可以调整字体大小，但在中英文混排下由于 decent 的不同，
 * 无法根据具体需求进行底部对齐或者顶部对齐。而 QMUITextSizeSpan 则可以多传一个参数，让你可以根据具体情况来决定偏移值。
 *
 * @author cginechen
 * @date 2016-12-02
 */

public class QMUITextSizeSpan extends ReplacementSpan {
    private int mTextSize;
    private int mVerticalOffset;
    private Paint mPaint;
    private Typeface mTypeface;

    public QMUITextSizeSpan(int textSize, int verticalOffset){
       this(textSize, verticalOffset, null);
    }

    public QMUITextSizeSpan(int textSize, int verticalOffset, Typeface typeface){
        mTextSize = textSize;
        mVerticalOffset = verticalOffset;
        mTypeface = typeface;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        mPaint = new Paint(paint);
        mPaint.setTextSize(mTextSize);
        mPaint.setTypeface(mTypeface);
        if(mTextSize > paint.getTextSize() && fm != null){
            Paint.FontMetricsInt newFm = mPaint.getFontMetricsInt();
            fm.descent = newFm.descent;
            fm.ascent = newFm.ascent;
            fm.top = newFm.top;
            fm.bottom = newFm.bottom;
        }
        return (int) mPaint.measureText(text, start, end);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top,
                     int y, int bottom, @NonNull Paint paint) {
        int baseline = y + mVerticalOffset;
        canvas.drawText(text, start, end, x, baseline, mPaint);
    }
}
