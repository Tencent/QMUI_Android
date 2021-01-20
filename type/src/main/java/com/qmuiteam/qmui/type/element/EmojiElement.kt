package com.qmuiteam.qmui.type.element;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qmuiteam.qmui.type.TypeEnvironment;

public class EmojiElement extends Element {

    private final Drawable mDrawable;

    public EmojiElement(
            @NonNull Drawable drawable,
            Character singleChar,
            @Nullable CharSequence text,
            int index, int originIndex) {
        super(singleChar, text, index, originIndex,
                text != null && text.length() > 2 && text.charAt(0) == '['
                        ? text.subSequence(1, text.length() - 1).toString() : null);
        mDrawable = drawable;
    }

    @Override
    protected void onMeasure(TypeEnvironment env) {
        Paint paint = env.getPaint();
        int size = (int) (paint.getFontMetrics().descent - paint.getFontMetrics().ascent);
        mDrawable.setBounds(0, 0, size, size);
        setMeasureDimen(size, size, 0);
    }

    @Override
    protected void onDraw(TypeEnvironment env, Canvas canvas) {
        drawBg(env, canvas);
        canvas.save();
        canvas.translate(getX(), getY());
        mDrawable.draw(canvas);
        canvas.restore();
        drawBorder(env, canvas);
    }
}
