package com.qmuiteam.qmui.util;

import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;

import com.qmuiteam.qmui.span.QMUIAlignMiddleImageSpan;
import com.qmuiteam.qmui.span.QMUIMarginImageSpan;

/**
 * @author cginechen
 * @date 2016-10-12
 */

public class QMUISpanHelper {

    /**
     * 在text左边或者右边添加icon,
     * 默认TextView添加leftDrawable或rightDrawable不能适应TextView match_parent的情况
     *
     * @param left
     * @param text
     * @param icon
     * @return
     */
    public static CharSequence generateSideIconText(boolean left, int iconPadding, CharSequence text, Drawable icon) {
        if (icon == null) {
            return text;
        }

        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        String iconTag = "[icon]";
        SpannableStringBuilder builder = new SpannableStringBuilder();
        int start, end;
        if (left) {
            start = 0;
            builder.append(iconTag);
            end = builder.length();
            builder.append(text);
        } else {
            builder.append(text);
            start = builder.length();
            builder.append(iconTag);
            end = builder.length();
        }

        QMUIMarginImageSpan imageSpan;

        if (left) {
            imageSpan = new QMUIMarginImageSpan(icon, QMUIAlignMiddleImageSpan.ALIGN_MIDDLE, 0, iconPadding);
        } else {
            imageSpan = new QMUIMarginImageSpan(icon, QMUIAlignMiddleImageSpan.ALIGN_MIDDLE, iconPadding, 0);
        }

        builder.setSpan(imageSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        imageSpan.setAvoidSuperChangeFontMetrics(true);
        return builder;
    }
}
