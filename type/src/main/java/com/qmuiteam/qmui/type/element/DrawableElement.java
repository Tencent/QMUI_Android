package com.qmuiteam.qmui.type.element;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qmuiteam.qmui.type.TypeEnvironment;

public class DrawableElement extends Element {

    private final Drawable mDrawable;

    public DrawableElement(
            @NonNull Drawable drawable,
            Character singleChar,
            @Nullable CharSequence text,
            int index, int originIndex) {
        super(singleChar, text, index, originIndex,
                text != null && text.length() > 2 && text.charAt(0) == '['
                        ? text.subSequence(1, text.length() - 1).toString() : null);
        mDrawable = drawable.mutate();
        mDrawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
    }

    @Override
    protected void onMeasure(TypeEnvironment env) {
        setMeasureDimen(
                mDrawable.getIntrinsicWidth(),
                mDrawable.getIntrinsicHeight(),
                0);
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
