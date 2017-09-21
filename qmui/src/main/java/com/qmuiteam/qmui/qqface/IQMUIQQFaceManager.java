package com.qmuiteam.qmui.qqface;

import android.graphics.drawable.Drawable;

/**
 * QMUIQQFace资源管理接口，其实现参考QMUIDemo
 * 1. 不想将所有emoji表情资源全都打包到qmui中
 * 2. 使用者可以高度自定义表情资源
 *
 * @author cginechen
 * @date 2016-12-21
 */

public interface IQMUIQQFaceManager {

    /**
     * 判断是否是SoftBank编码表情，用于缩小解析范围，返回true调用{@link #getSoftbankEmojiResource}进行解析
     * SoftBank虽然是Unicode编码，但是位于BMP，所以可以直接通过char来做判断
     * <p>
     * Android很多输入法都自带一套SoftBank表情，但不同输入法的表情并不统一。
     */
    boolean maybeSoftBankEmoji(char c);

    /**
     * 获取SoftBank编码表情，如果没有则返回0
     */
    int getSoftbankEmojiResource(char c);

    /**
     * 判断Unicode编码字符是否是表情，用于缩小解析范围，返回true调用{@link #getEmojiResource}进行解析
     */
    boolean maybeEmoji(int codePoint);

    /**
     * 获取Unicode编码表情，如果没有则返回0
     */
    int getEmojiResource(int codePoint);

    /**
     * 获取双字符编码表情， 如果没有则返回0
     */
    int getDoubleUnicodeEmoji(int currentCodePoint, int nextCodePoint);

    /**
     * 将字符串解析例为表情，如果没有则返回0
     */
    int getQQfaceResource(CharSequence text);

    /**
     * 寻找特殊bounds的Drawable, 字符串请以[开头和以]结尾
     */
    Drawable getSpecialBoundsDrawable(CharSequence text);

    /**
     * 获取特殊bounds的Drawable的最高height, 这将决定行高
     * fixme: 目前会影响所有行，要改为只影响含有特殊Drawable的行
     */
    int getSpecialDrawableMaxHeight();
}
