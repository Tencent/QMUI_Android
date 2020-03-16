package com.qmuiteam.qmui.skin.handler;

import android.content.res.ColorStateList;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.qmuiteam.qmui.skin.QMUISkinHelper;

public class QMUISkinRuleHintColorHandler extends QMUISkinRuleColorStateListHandler {
    @Override
    void handle(View view, String name, ColorStateList colorStateList) {
        if (view instanceof TextView) {
            ((TextView) view).setHintTextColor(colorStateList);
        } else if (view instanceof TextInputLayout) {
            ((TextInputLayout) view).setHintTextColor(colorStateList);
        }else{
            QMUISkinHelper.warnRuleNotSupport(view, name);
        }
    }
}
