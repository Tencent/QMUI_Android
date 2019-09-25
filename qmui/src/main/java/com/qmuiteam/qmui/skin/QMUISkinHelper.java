package com.qmuiteam.qmui.skin;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.util.QMUIResHelper;

import androidx.annotation.Nullable;

public class QMUISkinHelper {

    public static int getSkinColor(View view, int colorAttr) {
        Integer skin = (Integer) view.getTag(R.id.qmui_skin_current_index);
        Resources.Theme theme;
        if (skin == null || skin <= 0) {
            theme = view.getContext().getTheme();
        } else {
            theme = QMUISkinManager.getInstance(view.getContext()).getTheme(skin);
        }
        return QMUIResHelper.getAttrColor(theme, colorAttr);
    }

    @Nullable
    public static Drawable getSkinDrawable(View view, int drawableAttr) {
        Integer skin = (Integer) view.getTag(R.id.qmui_skin_current_index);
        Resources.Theme theme;
        if (skin == null || skin <= 0) {
            theme = view.getContext().getTheme();
        } else {
            theme = QMUISkinManager.getInstance(view.getContext()).getTheme(skin);
        }
        return QMUIResHelper.getAttrDrawable(view.getContext(), theme, drawableAttr);
    }
}
