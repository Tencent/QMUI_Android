package com.qmuiteam.qmui.layout;

import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by cgspine on 2018/3/23.
 */

public interface IQMUILayout {
    int HIDE_RADIUS_SIDE_NONE = 0;
    int HIDE_RADIUS_SIDE_TOP = 1;
    int HIDE_RADIUS_SIDE_RIGHT = 2;
    int HIDE_RADIUS_SIDE_BOTTOM = 3;
    int HIDE_RADIUS_SIDE_LEFT = 4;

    @IntDef(value = {
            HIDE_RADIUS_SIDE_NONE,
            HIDE_RADIUS_SIDE_TOP,
            HIDE_RADIUS_SIDE_RIGHT,
            HIDE_RADIUS_SIDE_BOTTOM,
            HIDE_RADIUS_SIDE_LEFT})
    @Retention(RetentionPolicy.SOURCE)
    @interface HideRadiusSide {
    }

    /**
     * limit the width of a layout
     *
     * @param widthLimit
     * @return
     */
    boolean setWidthLimit(int widthLimit);

    /**
     * limit the height of a layout
     *
     * @param heightLimit
     * @return
     */
    boolean setHeightLimit(int heightLimit);

    /**
     * use the shadow elevation from the theme
     */
    void setUseThemeGeneralShadowElevation();

    /**
     * determine if the outline contain the padding area, usually false
     *
     * @param outlineExcludePadding
     */
    void setOutlineExcludePadding(boolean outlineExcludePadding);

    /**
     * See {@link android.view.View#setElevation(float)}
     *
     * @param elevation
     */
    void setShadowElevation(int elevation);

    /**
     * See {@link View#getElevation()}
     *
     * @return
     */
    int getShadowElevation();

    /**
     * set the outline alpha, which will change the shadow
     *
     * @param shadowAlpha
     */
    void setShadowAlpha(float shadowAlpha);

    /**
     * get the outline alpha we set
     *
     * @return
     */
    float getShadowAlpha();

    /**
     * set the layout radius
     * @param radius
     */
    void setRadius(int radius);

    /**
     * set the layout radius with one or none side been hidden
     * @param radius
     * @param hideRadiusSide
     */
    void setRadius(int radius, @QMUILayoutHelper.HideRadiusSide int hideRadiusSide);

    /**
     * get the layout radius
     * @return
     */
    int getRadius();

    /**
     * inset the outline if needed
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    void setOutlineInset(int left, int top, int right, int bottom);

    /**
     * the shadow elevation only work after L, so we provide a downgrading compatible solutions for android 4.x
     * usually we use border, but the border may be redundant for android L+. so will not show border default,
     * if your designer like the border exists with shadow, you can call setShowBorderOnlyBeforeL(false)
     *
     * @param showBorderOnlyBeforeL
     */
    void setShowBorderOnlyBeforeL(boolean showBorderOnlyBeforeL);

    /**
     * in some case, we maybe hope the layout only have radius in one side.
     * but there is no convenient way to write the code like canvas.drawPath,
     * so we take another way that hide one radius side
     *
     * @param hideRadiusSide
     */
    void setHideRadiusSide(@HideRadiusSide int hideRadiusSide);

    /**
     * get the side that we have hidden the radius
     *
     * @return
     */
    int getHideRadiusSide();

    /**
     * this method will determine the radius and shadow.
     *
     * @param radius
     * @param shadowElevation
     * @param shadowAlpha
     */
    void setRadiusAndShadow(int radius, int shadowElevation, float shadowAlpha);

    /**
     * this method will determine the radius and shadow with one or none side be hidden
     *
     * @param radius
     * @param hideRadiusSide
     * @param shadowElevation
     * @param shadowAlpha
     */
    void setRadiusAndShadow(int radius, @HideRadiusSide int hideRadiusSide, int shadowElevation, float shadowAlpha);

    /**
     * border color, if you don not set it, the layout will not draw the border
     *
     * @param borderColor
     */
    void setBorderColor(@ColorInt int borderColor);

    /**
     * border width, default is 1px, usually no need to set
     *
     * @param borderWidth
     */
    void setBorderWidth(int borderWidth);

    /**
     * config the top divider
     *
     * @param topInsetLeft
     * @param topInsetRight
     * @param topDividerHeight
     * @param topDividerColor
     */
    void updateTopDivider(int topInsetLeft, int topInsetRight, int topDividerHeight, int topDividerColor);

    /**
     * config the bottom divider
     *
     * @param bottomInsetLeft
     * @param bottomInsetRight
     * @param bottomDividerHeight
     * @param bottomDividerColor
     */
    void updateBottomDivider(int bottomInsetLeft, int bottomInsetRight, int bottomDividerHeight, int bottomDividerColor);

    /**
     * config the left divider
     *
     * @param leftInsetTop
     * @param leftInsetBottom
     * @param leftDividerWidth
     * @param leftDividerColor
     */
    void updateLeftDivider(int leftInsetTop, int leftInsetBottom, int leftDividerWidth, int leftDividerColor);

    /**
     * config the right divider
     *
     * @param rightInsetTop
     * @param rightInsetBottom
     * @param rightDividerWidth
     * @param rightDividerColor
     */
    void updateRightDivider(int rightInsetTop, int rightInsetBottom, int rightDividerWidth, int rightDividerColor);

    /**
     * show top divider, and hide others
     *
     * @param topInsetLeft
     * @param topInsetRight
     * @param topDividerHeight
     * @param topDividerColor
     */
    void onlyShowTopDivider(int topInsetLeft, int topInsetRight, int topDividerHeight, int topDividerColor);

    /**
     * show bottom divider, and hide others
     *
     * @param bottomInsetLeft
     * @param bottomInsetRight
     * @param bottomDividerHeight
     * @param bottomDividerColor
     */
    void onlyShowBottomDivider(int bottomInsetLeft, int bottomInsetRight, int bottomDividerHeight, int bottomDividerColor);

    /**
     * show left divider, and hide others
     *
     * @param leftInsetTop
     * @param leftInsetBottom
     * @param leftDividerWidth
     * @param leftDividerColor
     */
    void onlyShowLeftDivider(int leftInsetTop, int leftInsetBottom, int leftDividerWidth, int leftDividerColor);

    /**
     * show right divider, and hide others
     *
     * @param rightInsetTop
     * @param rightInsetBottom
     * @param rightDividerWidth
     * @param rightDividerColor
     */
    void onlyShowRightDivider(int rightInsetTop, int rightInsetBottom, int rightDividerWidth, int rightDividerColor);

    /**
     * after config the border, sometimes we need change the alpha of divider with animation,
     * so we provide a method to individually change the alpha
     *
     * @param dividerAlpha [0, 255]
     */
    void setTopDividerAlpha(int dividerAlpha);

    /**
     * @param dividerAlpha [0, 255]
     */
    void setBottomDividerAlpha(int dividerAlpha);

    /**
     * @param dividerAlpha [0, 255]
     */
    void setLeftDividerAlpha(int dividerAlpha);

    /**
     * @param dividerAlpha [0, 255]
     */
    void setRightDividerAlpha(int dividerAlpha);

}
