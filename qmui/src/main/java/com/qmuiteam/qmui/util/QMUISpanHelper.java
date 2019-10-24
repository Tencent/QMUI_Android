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

package com.qmuiteam.qmui.util;

import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;

import com.qmuiteam.qmui.span.QMUIAlignMiddleImageSpan;
import com.qmuiteam.qmui.span.QMUIMarginImageSpan;

import androidx.annotation.Nullable;

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
    public static CharSequence generateSideIconText(boolean left,
                                                    int iconPadding, CharSequence text, Drawable icon) {
        return generateSideIconText(
                left, iconPadding, text, icon, 0);
    }

    public static CharSequence generateSideIconText(boolean left,
                                                    int iconPadding, CharSequence text, Drawable icon,
                                                    int iconOffsetY){
        return generateSideIconText(
                left, iconPadding, text, icon, iconOffsetY, 0, null);
    }

    public static CharSequence generateSideIconText(boolean left,
                                                    int iconPadding, CharSequence text, Drawable icon,
                                                    int iconTintAttr, @Nullable View skinFollowView){
        return generateSideIconText(
                left, iconPadding, text, icon, 0, iconTintAttr, skinFollowView);
    }

    public static CharSequence generateSideIconText(boolean left,
                                                    int iconPadding, CharSequence text, Drawable icon,
                                                    int iconOffsetY, int iconTintAttr,
                                                    @Nullable View skinFollowView) {
        return generateHorIconText(text,
                left ? iconPadding : 0, left ? icon : null, left ? iconTintAttr : 0,
                left ? 0 : iconPadding, left ? null : icon, left ? 0 : iconTintAttr,
                iconOffsetY, skinFollowView);
    }



    public static CharSequence generateHorIconText(CharSequence text,
                                                   int leftPadding, Drawable iconLeft,
                                                   int rightPadding, Drawable iconRight) {
        return generateHorIconText(text, leftPadding, iconLeft, rightPadding, iconRight,0);
    }


    public static CharSequence generateHorIconText(CharSequence text,
                                                   int leftPadding, Drawable iconLeft,
                                                   int rightPadding, Drawable iconRight,
                                                   int iconOffsetY) {
        return generateHorIconText(text, leftPadding, iconLeft, 0,
                rightPadding, iconRight, 0, iconOffsetY, null);
    }

    public static CharSequence generateHorIconText(CharSequence text,
                                                   int leftPadding, Drawable iconLeft, int iconLeftTintAttr,
                                                   int rightPadding, Drawable iconRight, int iconRightTintAttr,
                                                   @Nullable View skinFollowView) {
        return generateHorIconText(text, leftPadding, iconLeft, iconLeftTintAttr,
                rightPadding, iconRight, iconRightTintAttr,0, skinFollowView);
    }

    public static CharSequence generateHorIconText(CharSequence text,
                                                   int leftPadding, Drawable iconLeft, int iconLeftTintAttr,
                                                   int rightPadding, Drawable iconRight, int iconRightTintAttr,
                                                   int iconOffsetY,
                                                   @Nullable View skinFollowView) {
        if (iconLeft == null && iconRight == null) {
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

            QMUIMarginImageSpan imageSpan = new QMUIMarginImageSpan(iconLeft,
                    QMUIAlignMiddleImageSpan.ALIGN_MIDDLE, 0, leftPadding, iconOffsetY);
            imageSpan.setSkinSupportWithTintColor(skinFollowView, iconLeftTintAttr);
            imageSpan.setAvoidSuperChangeFontMetrics(true);
            builder.setSpan(imageSpan, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        builder.append(text);
        if (iconRight != null) {
            iconRight.setBounds(0, 0, iconRight.getIntrinsicWidth(), iconRight.getIntrinsicHeight());
            start = builder.length();
            builder.append(iconTag);
            end = builder.length();

            QMUIMarginImageSpan imageSpan = new QMUIMarginImageSpan(iconRight,
                    QMUIAlignMiddleImageSpan.ALIGN_MIDDLE, rightPadding, 0, iconOffsetY);
            imageSpan.setSkinSupportWithTintColor(skinFollowView, iconRightTintAttr);
            imageSpan.setAvoidSuperChangeFontMetrics(true);
            builder.setSpan(imageSpan, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        return builder;
    }
}
