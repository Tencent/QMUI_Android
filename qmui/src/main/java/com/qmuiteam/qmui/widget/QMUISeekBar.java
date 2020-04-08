package com.qmuiteam.qmui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SimpleArrayMap;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;

public class QMUISeekBar extends QMUISlider {
    private int mTickHeight;
    private int mTickWidth;

    private static SimpleArrayMap<String, Integer> sDefaultSkinAttrs;

    static {
        sDefaultSkinAttrs = new SimpleArrayMap<>(2);
        sDefaultSkinAttrs.put(QMUISkinValueBuilder.BACKGROUND, R.attr.qmui_skin_support_seek_bar_color);
        sDefaultSkinAttrs.put(QMUISkinValueBuilder.PROGRESS_COLOR, R.attr.qmui_skin_support_seek_bar_color);
    }

    public QMUISeekBar(@NonNull Context context) {
        this(context, null);
    }

    public QMUISeekBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.QMUISeekBarStyle);
    }

    public QMUISeekBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = getContext().obtainStyledAttributes(attrs,
                R.styleable.QMUISeekBar, defStyleAttr, 0);
        mTickWidth = array.getDimensionPixelSize(R.styleable.QMUISeekBar_qmui_seek_bar_tick_width,
                QMUIDisplayHelper.dp2px(context, 1));
        mTickHeight = array.getDimensionPixelSize(R.styleable.QMUISeekBar_qmui_seek_bar_tick_height,
                QMUIDisplayHelper.dp2px(context, 4));
        array.recycle();
        setClickToChangeProgress(true);
    }

    public void setTickHeight(int tickHeight) {
        mTickHeight = tickHeight;
        invalidate();
    }

    public void setTickWidth(int tickWidth) {
        mTickWidth = tickWidth;
        invalidate();
    }

    public int getTickHeight() {
        return mTickHeight;
    }

    @Override
    protected void drawRect(Canvas canvas, RectF rect, int barHeight, Paint paint, boolean forProgress) {
        canvas.drawRect(rect, paint);
    }

    @Override
    protected void drawTick(Canvas canvas, int currentTickCount, int totalTickCount,
                            int left, int right, float y,
                            Paint paint, int barNormalColor, int barProgressColor) {
        if (mTickHeight <= 0 || mTickWidth <= 0 || totalTickCount < 1) {
            return;
        }
        float step = ((float) (right - left - mTickWidth)) / totalTickCount;
        float t = y - mTickHeight / 2f;
        float b = y + mTickHeight / 2f;
        float l, r;
        float x = left + mTickWidth / 2f;
        for (int i = 0; i <= totalTickCount; i++) {
            l = x - mTickWidth / 2f;
            r = x + mTickWidth / 2f;
            paint.setColor(i <= currentTickCount ? barProgressColor : barNormalColor);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(l, t, r, b, paint);
            x += step;
        }
    }

    @Override
    public SimpleArrayMap<String, Integer> getDefaultSkinAttrs() {
        return sDefaultSkinAttrs;
    }
}
