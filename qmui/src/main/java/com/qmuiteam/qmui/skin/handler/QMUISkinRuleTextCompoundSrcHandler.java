package com.qmuiteam.qmui.skin.handler;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import com.qmuiteam.qmui.skin.QMUISkinHelper;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;

import org.jetbrains.annotations.NotNull;

public class QMUISkinRuleTextCompoundSrcHandler extends QMUISkinRuleDrawableHandler {
    @Override
    protected void handle(@NotNull View view, @NotNull String name, Drawable drawable) {
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            if (drawable != null) {
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            }
            Drawable[] drawables = tv.getCompoundDrawables();
            if (QMUISkinValueBuilder.TEXT_COMPOUND_LEFT_SRC.equals(name)) {
                drawables[0] = drawable;
            } else if (QMUISkinValueBuilder.TEXT_COMPOUND_TOP_SRC.equals(name)) {
                drawables[1] = drawable;
            } else if (QMUISkinValueBuilder.TEXT_COMPOUND_RIGHT_SRC.equals(name)) {
                drawables[2] = drawable;
            } else if (QMUISkinValueBuilder.TEXT_COMPOUND_BOTTOM_SRC.equals(name)) {
                drawables[3] = drawable;
            }
            tv.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
        }else{
            QMUISkinHelper.warnRuleNotSupport(view, name);
        }
    }
}
