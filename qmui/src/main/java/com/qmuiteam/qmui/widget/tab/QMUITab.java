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

package com.qmuiteam.qmui.widget.tab;

import android.graphics.Typeface;
import android.view.Gravity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;

public class QMUITab {
    public static final int ICON_POSITION_LEFT = 0;
    public static final int ICON_POSITION_TOP = 1;
    public static final int ICON_POSITION_RIGHT = 2;
    public static final int ICON_POSITION_BOTTOM = 3;

    static final int NO_SIGN_COUNT_AND_RED_POINT = -1;
    static final int RED_POINT_SIGN_COUNT = 0;

    @IntDef(value = {
            ICON_POSITION_LEFT,
            ICON_POSITION_TOP,
            ICON_POSITION_RIGHT,
            ICON_POSITION_BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface IconPosition {
    }


    boolean allowIconDrawOutside;
    int iconTextGap;
    int normalTextSize;
    int selectedTextSize;
    Typeface normalTypeface;
    Typeface selectedTypeface;
    int normalColor;
    int selectedColor;
    int normalTabIconWidth = QMUITabIcon.TAB_ICON_INTRINSIC;
    int normalTabIconHeight = QMUITabIcon.TAB_ICON_INTRINSIC;
    float selectedTabIconScale = 1f;
    QMUITabIcon tabIcon = null;
    int contentWidth = 0;
    int contentLeft = 0;
    @IconPosition int iconPosition = ICON_POSITION_TOP;
    int gravity = Gravity.CENTER;
    private CharSequence text;
    int signCountDigits = 2;
    int signCountLeftMarginWithIconOrText = 0;
    int signCountBottomMarginWithIconOrText = 0;
    int signCount = NO_SIGN_COUNT_AND_RED_POINT;

    float rightSpaceWeight = 0f;
    float leftSpaceWeight = 0f;
    int leftAddonMargin = 0;
    int rightAddonMargin = 0;


    QMUITab(CharSequence text) {
        this.text = text;
    }


    public CharSequence getText() {
        return text;
    }

    public void setText(CharSequence text) {
        this.text = text;
    }

    public int getIconPosition() {
        return iconPosition;
    }

    public void setIconPosition(@IconPosition int iconPosition) {
        this.iconPosition = iconPosition;
    }

    public void setSpaceWeight(float leftWeight, float rightWeight) {
        leftSpaceWeight = leftWeight;
        rightSpaceWeight = rightWeight;
    }


    public int getGravity() {
        return gravity;
    }

    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    public void setSignCount(int signCount) {
        this.signCount = signCount;
    }

    public void setRedPoint(){
        this.signCount =  RED_POINT_SIGN_COUNT;
    }

    public int getSignCount(){
        return this.signCount;
    }

    public boolean isRedPointShowing(){
        return this.signCount == RED_POINT_SIGN_COUNT;
    }

    public void clearSignCountOrRedPoint(){
        this.signCount = NO_SIGN_COUNT_AND_RED_POINT;
    }


    public int getNormalColor() {
        return normalColor;
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    public int getNormalTextSize() {
        return normalTextSize;
    }

    public int getSelectedTextSize() {
        return selectedTextSize;
    }

    public QMUITabIcon getTabIcon() {
        return tabIcon;
    }

    public Typeface getNormalTypeface() {
        return normalTypeface;
    }

    public Typeface getSelectedTypeface() {
        return selectedTypeface;
    }

    public int getNormalTabIconWidth() {
        if (normalTabIconWidth == QMUITabIcon.TAB_ICON_INTRINSIC && tabIcon != null) {
            return tabIcon.getIntrinsicWidth();
        }
        return normalTabIconWidth;
    }

    public int getNormalTabIconHeight() {
        if (normalTabIconHeight == QMUITabIcon.TAB_ICON_INTRINSIC && tabIcon != null) {
            return tabIcon.getIntrinsicWidth();
        }
        return normalTabIconHeight;
    }

    public float getSelectedTabIconScale() {
        return selectedTabIconScale;
    }

    public int getIconTextGap() {
        return iconTextGap;
    }

    public boolean isAllowIconDrawOutside() {
        return allowIconDrawOutside;
    }
}
