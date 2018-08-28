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
     * @param left true 则在文字左边添加 icon，false 则在文字右边添加 icon
     * @param text 文字内容
     * @param icon 需要被添加的 icon
     * @return 返回带有 icon 的文字
     */
    public static CharSequence generateSideIconText(boolean left, int iconPadding, CharSequence text, Drawable icon) {
        return generateSideIconText(left, iconPadding, text, icon, 0);
    }

    public static CharSequence generateSideIconText(boolean left, int iconPadding, CharSequence text, Drawable icon, int iconOffsetY) {
        return generateHorIconText(text, left ? iconPadding : 0, left ? icon : null, left ? 0 : iconPadding, left ? null : icon, iconOffsetY);
    }

    public static CharSequence generateHorIconText(CharSequence text, int leftPadding, Drawable iconLeft, int rightPadding, Drawable iconRight) {
        return generateHorIconText(text, leftPadding, iconLeft, rightPadding, iconRight, 0);
    }

    public static CharSequence generateHorIconText(CharSequence text, int leftPadding, Drawable iconLeft, int rightPadding, Drawable iconRight, int iconOffsetY) {
        if(iconLeft == null && iconRight == null) {
            return text;
        }
        String iconTag = "[icon]";
        SpannableStringBuilder builder = new SpannableStringBuilder();
        int start, end;
        if (iconLeft != null) {
            iconLeft.setBounds(0, 0, iconLeft.getIntrinsicWidth(), iconLeft.getIntrinsicHeight());
            start = 0;
            builder.append(iconTag);
            end = builder.length();

            QMUIMarginImageSpan imageSpan = new QMUIMarginImageSpan(iconLeft, QMUIAlignMiddleImageSpan.ALIGN_MIDDLE, 0, leftPadding, iconOffsetY);
            imageSpan.setAvoidSuperChangeFontMetrics(true);
            builder.setSpan(imageSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        builder.append(text);
        if (iconRight != null) {
            iconRight.setBounds(0, 0, iconRight.getIntrinsicWidth(), iconRight.getIntrinsicHeight());
            start = builder.length();
            builder.append(iconTag);
            end = builder.length();

            QMUIMarginImageSpan imageSpan = new QMUIMarginImageSpan(iconRight, QMUIAlignMiddleImageSpan.ALIGN_MIDDLE, rightPadding, 0, iconOffsetY);
            imageSpan.setAvoidSuperChangeFontMetrics(true);
            builder.setSpan(imageSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        return builder;
    }
}
