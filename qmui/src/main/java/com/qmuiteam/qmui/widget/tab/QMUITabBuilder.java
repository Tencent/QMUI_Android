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

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;

import androidx.annotation.Nullable;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;


/**
 * use {@link QMUITabSegment#tabBuilder()} to get a instance
 */
public class QMUITabBuilder {
    /**
     * icon in normal state
     */
    private int normalDrawableAttr = 0;
    private @Nullable Drawable normalDrawable;
    /**
     * icon in selected state
     */
    private int selectedDrawableAttr = 0;
    private @Nullable Drawable selectedDrawable;
    /**
     * change icon by tint color, if true, selectedDrawable will not work
     */
    private boolean dynamicChangeIconColor = false;

    /**
     * for skin change. if true, then normalDrawableAttr and selectedDrawableAttr will not work.
     * otherwise, icon will be replaced by normalDrawableAttr and selectedDrawableAttr
     */
    private boolean skinChangeWithTintColor = false;
    private boolean skinChangeNormalWithTintColor = true;
    private boolean skinChangeSelectedWithTintColor = true;

    /**
     * text size in normal state
     */
    private int normalTextSize;
    /**
     * text size in selected state
     */
    private int selectTextSize;

    /**
     * text color(icon color in if dynamicChangeIconColor == true) in  normal state
     */
    private int normalColorAttr = R.attr.qmui_skin_support_tab_normal_color;
    /**
     * text color(icon color in if dynamicChangeIconColor == true) in  selected state
     */
    private int selectedColorAttr = R.attr.qmui_skin_support_tab_selected_color;

    /**
     * text color with no skin support
     */
    private int normalColor = 0;

    /**
     * text color with no skin support
     */
    private int selectColor = 0;

    /**
     * icon position(left/top/right/bottom)
     */
    private @QMUITab.IconPosition int iconPosition = QMUITab.ICON_POSITION_TOP;
    /**
     * gravity of text
     */
    private int gravity = Gravity.CENTER;
    /**
     * text
     */
    private CharSequence text;

    /**
     * text typeface in normal state
     */
    private Typeface normalTypeface;

    /**
     * text typeface in selected state
     */
    private Typeface selectedTypeface;

    /**
     * width of tab icon in normal state
     */
    private int normalTabIconWidth = QMUITabIcon.TAB_ICON_INTRINSIC;
    /**
     * height of tab icon in normal state
     */
    int normalTabIconHeight = QMUITabIcon.TAB_ICON_INTRINSIC;
    /**
     * scale of tab icon in selected state
     */
    float selectedTabIconScale = 1f;

    float typefaceUpdateAreaPercent = 0.25f;

    /**
     * signCount or redPoint
     */
    private int signCount = QMUITab.NO_SIGN_COUNT_AND_RED_POINT;

    /**
     * max signCount digits, if the number is over the digits, use 'xx+' to present
     * if signCountDigits == 2 and number is 110, then component will show '99+'
     */
    private int signCountDigits = 2;
    /**
     * the horizontal offset of signCount(redPoint) view
     */
    private int signCountHorizontalOffset;
    /**
     * the vertical offset of signCount(redPoint) view
     */
    private int signCountVerticalOffset;

    private int signCountVerticalAlign = QMUITab.SIGN_COUNT_VERTICAL_ALIGN_BOTTOM_TO_CONTENT_TOP;

    /**
     * the gap between icon and text
     */
    private int iconTextGap;

    /**
     * allow icon draw outside of tab view
     */
    private boolean allowIconDrawOutside = true;


    QMUITabBuilder(Context context) {
        iconTextGap = QMUIDisplayHelper.dp2px(context, 2);
        normalTextSize = selectTextSize = QMUIDisplayHelper.dp2px(context, 12);
        signCountHorizontalOffset = QMUIDisplayHelper.dp2px(context, 3);
        signCountVerticalOffset = signCountHorizontalOffset;
    }

    QMUITabBuilder(QMUITabBuilder other) {
        this.normalDrawableAttr = other.normalDrawableAttr;
        this.selectedDrawableAttr = other.selectedDrawableAttr;
        this.normalDrawable = other.normalDrawable;
        this.selectedDrawable = other.selectedDrawable;
        this.dynamicChangeIconColor = other.dynamicChangeIconColor;
        this.normalTextSize = other.normalTextSize;
        this.selectTextSize = other.selectTextSize;
        this.normalColorAttr = other.normalColorAttr;
        this.selectedColorAttr = other.selectedColorAttr;
        this.iconPosition = other.iconPosition;
        this.gravity = other.gravity;
        this.text = other.text;
        this.signCount = other.signCount;
        this.signCountDigits = other.signCountDigits;
        this.signCountHorizontalOffset = other.signCountHorizontalOffset;
        this.signCountVerticalOffset = other.signCountVerticalOffset;
        this.signCountVerticalAlign = other.signCountVerticalAlign;
        this.normalTypeface = other.normalTypeface;
        this.selectedTypeface = other.selectedTypeface;
        this.normalTabIconWidth = other.normalTabIconWidth;
        this.normalTabIconHeight = other.normalTabIconHeight;
        this.selectedTabIconScale = other.selectedTabIconScale;
        this.iconTextGap = other.iconTextGap;
        this.allowIconDrawOutside = other.allowIconDrawOutside;
        this.typefaceUpdateAreaPercent = other.typefaceUpdateAreaPercent;
        this.skinChangeNormalWithTintColor = other.skinChangeNormalWithTintColor;
        this.skinChangeSelectedWithTintColor = other.skinChangeSelectedWithTintColor;
        this.skinChangeWithTintColor = other.skinChangeWithTintColor;
        this.normalColor = other.normalColor;
        this.selectColor = other.selectColor;
    }

    public QMUITabBuilder setAllowIconDrawOutside(boolean allowIconDrawOutside) {
        this.allowIconDrawOutside = allowIconDrawOutside;
        return this;
    }

    public QMUITabBuilder setTypefaceUpdateAreaPercent(float typefaceUpdateAreaPercent) {
        this.typefaceUpdateAreaPercent = typefaceUpdateAreaPercent;
        return this;
    }

    public QMUITabBuilder setNormalDrawable(Drawable normalDrawable) {
        this.normalDrawable = normalDrawable;
        return this;
    }

    public QMUITabBuilder setNormalDrawableAttr(int normalDrawableAttr) {
        this.normalDrawableAttr = normalDrawableAttr;
        return this;
    }

    public QMUITabBuilder setSelectedDrawable(Drawable selectedDrawable) {
        this.selectedDrawable = selectedDrawable;
        return this;
    }

    public QMUITabBuilder setSelectedDrawableAttr(int selectedDrawableAttr) {
        this.selectedDrawableAttr = selectedDrawableAttr;
        return this;
    }

    @Deprecated
    public QMUITabBuilder skinChangeWithTintColor(boolean skinChangeWithTintColor){
        this.skinChangeWithTintColor = skinChangeWithTintColor;
        return this;
    }

    public QMUITabBuilder skinChangeNormalWithTintColor(boolean skinChangeNormalWithTintColor){
        this.skinChangeNormalWithTintColor = skinChangeNormalWithTintColor;
        return this;
    }

    public QMUITabBuilder skinChangeSelectedWithTintColor(boolean skinChangeSelectedWithTintColor){
        this.skinChangeSelectedWithTintColor = skinChangeSelectedWithTintColor;
        return this;
    }

    public QMUITabBuilder setTextSize(int normalTextSize, int selectedTextSize) {
        this.normalTextSize = normalTextSize;
        this.selectTextSize = selectedTextSize;
        return this;
    }

    public QMUITabBuilder setTypeface(Typeface normalTypeface, Typeface selectedTypeface) {
        this.normalTypeface = normalTypeface;
        this.selectedTypeface = selectedTypeface;
        return this;
    }

    public QMUITabBuilder setNormalIconSizeInfo(int normalWidth, int normalHeight) {
        this.normalTabIconWidth = normalWidth;
        this.normalTabIconHeight = normalHeight;
        return this;
    }

    public QMUITabBuilder setSelectedIconScale(float selectedScale) {
        this.selectedTabIconScale = selectedScale;
        return this;
    }

    public QMUITabBuilder setIconTextGap(int iconTextGap) {
        this.iconTextGap = iconTextGap;
        return this;
    }

    public QMUITabBuilder setSignCount(int signCount) {
        this.signCount = signCount;
        return this;
    }

    public QMUITabBuilder setSignCountMarginInfo(int digit,
                                                 int horizontalOffset,
                                                 int verticalOffset){
        return setSignCountMarginInfo(digit, horizontalOffset,
                QMUITab.SIGN_COUNT_VERTICAL_ALIGN_BOTTOM_TO_CONTENT_TOP,
                verticalOffset);
    }

    public QMUITabBuilder setSignCountMarginInfo(int digit,
                                                 int horizontalOffset,
                                                 int verticalAlign,
                                                 int verticalOffset
    ) {
        this.signCountDigits = digit;
        this.signCountHorizontalOffset = horizontalOffset;
        this.signCountVerticalOffset = verticalOffset;
        this.signCountVerticalAlign = verticalAlign;
        return this;
    }

    public QMUITabBuilder setColorAttr(int normalColorAttr, int selectedColorAttr) {
        this.normalColorAttr = normalColorAttr;
        this.selectedColorAttr = selectedColorAttr;
        return this;
    }

    public QMUITabBuilder setNormalColorAttr(int normalColorAttr) {
        this.normalColorAttr = normalColorAttr;
        return this;
    }

    public QMUITabBuilder setSelectedColorAttr(int selectedColorAttr) {
        this.selectedColorAttr = selectedColorAttr;
        return this;
    }

    public QMUITabBuilder setColor(int normalColor, int selectColor){
        this.normalColorAttr = 0;
        this.selectedColorAttr = 0;
        this.normalColor = normalColor;
        this.selectColor = selectColor;
        return this;
    }

    public QMUITabBuilder setNormalColor(int normalColor) {
        this.normalColorAttr = 0;
        this.normalColor = normalColor;
        return this;
    }

    public QMUITabBuilder setSelectColor(int selectColor) {
        this.selectedColorAttr = 0;
        this.selectColor = selectColor;
        return this;
    }

    public QMUITabBuilder setDynamicChangeIconColor(boolean dynamicChangeIconColor) {
        this.dynamicChangeIconColor = dynamicChangeIconColor;
        return this;
    }

    public QMUITabBuilder setGravity(int gravity) {
        this.gravity = gravity;
        return this;
    }

    public QMUITabBuilder setIconPosition(@QMUITab.IconPosition int iconPosition) {
        this.iconPosition = iconPosition;
        return this;
    }

    public QMUITabBuilder setText(CharSequence text) {
        this.text = text;
        return this;
    }

    public QMUITab build(Context context) {
        QMUITab tab = new QMUITab(this.text);
        if(!skinChangeWithTintColor){
            if(!skinChangeNormalWithTintColor){
                if(normalDrawableAttr != 0){
                    normalDrawable = QMUIResHelper.getAttrDrawable(context, normalDrawableAttr);
                }
            }

            if(!skinChangeSelectedWithTintColor){
                if(selectedDrawableAttr != 0){
                    selectedDrawable = QMUIResHelper.getAttrDrawable(context, selectedDrawableAttr);
                }
            }
        }

        tab.skinChangeWithTintColor = this.skinChangeWithTintColor;
        tab.skinChangeNormalWithTintColor = this.skinChangeNormalWithTintColor;
        tab.skinChangeSelectedWithTintColor = this.skinChangeSelectedWithTintColor;

        if (normalDrawable != null) {
            if (dynamicChangeIconColor || selectedDrawable == null) {
                tab.tabIcon = new QMUITabIcon(normalDrawable, null, true);
                // must same
                tab.skinChangeSelectedWithTintColor = tab.skinChangeNormalWithTintColor;
            } else {
                tab.tabIcon = new QMUITabIcon(normalDrawable, selectedDrawable, false);
            }
            tab.tabIcon.setBounds(0, 0, normalTabIconWidth, normalTabIconHeight);
        }
        tab.normalIconAttr = this.normalDrawableAttr;
        tab.selectedIconAttr = this.selectedDrawableAttr;
        tab.normalTabIconWidth = this.normalTabIconWidth;
        tab.normalTabIconHeight = this.normalTabIconHeight;
        tab.selectedTabIconScale = this.selectedTabIconScale;
        tab.gravity = this.gravity;
        tab.iconPosition = this.iconPosition;
        tab.normalTextSize = this.normalTextSize;
        tab.selectedTextSize = this.selectTextSize;
        tab.normalTypeface = this.normalTypeface;
        tab.selectedTypeface = this.selectedTypeface;
        tab.normalColorAttr = this.normalColorAttr;
        tab.selectedColorAttr = this.selectedColorAttr;
        tab.normalColor = this.normalColor;
        tab.selectColor = this.selectColor;
        tab.signCount = this.signCount;
        tab.signCountDigits = this.signCountDigits;
        tab.signCountHorizontalOffset = this.signCountHorizontalOffset;
        tab.signCountVerticalAlign = this.signCountVerticalAlign;
        tab.signCountVerticalOffset = this.signCountVerticalOffset;
        tab.iconTextGap = this.iconTextGap;
        tab.typefaceUpdateAreaPercent = this.typefaceUpdateAreaPercent;
        return tab;
    }
}
