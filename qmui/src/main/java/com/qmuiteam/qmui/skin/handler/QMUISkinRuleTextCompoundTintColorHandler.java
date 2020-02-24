package com.qmuiteam.qmui.skin.handler;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIDrawableHelper;

import androidx.core.widget.TintableCompoundDrawablesView;

public class QMUISkinRuleTextCompoundTintColorHandler extends QMUISkinRuleColorStateListHandler {

    @Override
    void handle(View view, String name, ColorStateList colorStateList) {
        if(colorStateList == null){
            return;
        }
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tv.setCompoundDrawableTintList(colorStateList);
            } else if (tv instanceof TintableCompoundDrawablesView) {
                ((TintableCompoundDrawablesView) tv).setSupportCompoundDrawablesTintList(colorStateList);
            } else {
                Drawable[] drawables = tv.getCompoundDrawables();
                for (int i = 0; i < drawables.length; i++) {
                    Drawable drawable = drawables[i];
                    if (drawable != null) {
                        drawable = drawable.mutate();
                        QMUIDrawableHelper.setDrawableTintColor(drawable, colorStateList.getDefaultColor());
                        drawables[i] = drawable;
                    }
                }
                tv.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
            }
        }
    }
}
