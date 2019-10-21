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

package com.qmuiteam.qmui.widget.roundwidget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.alpha.QMUIAlphaButton;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.skin.defaultAttr.IQMUISkinDefaultAttrProvider;
import com.qmuiteam.qmui.util.QMUIViewHelper;

import androidx.annotation.Nullable;
import androidx.collection.SimpleArrayMap;

/**
 * 使按钮能方便地指定圆角、边框颜色、边框粗细、背景色
 * <p>
 * 注意: 因为该控件的圆角采用 View 的 background 实现, 所以与原生的 <code>android:background</code> 有冲突。
 * <ul>
 * <li>如果在 xml 中用 <code>android:background</code> 指定 background, 该 background 不会生效。</li>
 * <li>如果在该 View 构造完后用 {@link #setBackgroundResource(int)} 等方法设置背景, 该背景将覆盖圆角效果。</li>
 * </ul>
 * </p>
 * <p>
 * 如需在 xml 中指定圆角、边框颜色、边框粗细、背景色等值,采用 xml 属性 {@link com.qmuiteam.qmui.R.styleable#QMUIRoundButton}
 * </p>
 * <p>
 * 如需在 Java 中指定以上属性, 需要通过 {@link #getBackground()} 获取 {@link QMUIRoundButtonDrawable} 对象,
 * 然后使用 {@link QMUIRoundButtonDrawable} 提供的方法进行设置。
 * </p>
 * <p>
 *
 * @see QMUIRoundButtonDrawable
 * </p>
 */
public class QMUIRoundButton extends QMUIAlphaButton implements IQMUISkinDefaultAttrProvider {

    private QMUIRoundButtonDrawable mRoundBg;
    private static SimpleArrayMap<String, Integer> sDefaultSkinAttrs;

    static {
        sDefaultSkinAttrs = new SimpleArrayMap<>(3);
        sDefaultSkinAttrs.put(QMUISkinValueBuilder.BACKGROUND, R.attr.qmui_skin_support_round_btn_bg_color);
        sDefaultSkinAttrs.put(QMUISkinValueBuilder.BORDER, R.attr.qmui_skin_support_round_btn_border_color);
        sDefaultSkinAttrs.put(QMUISkinValueBuilder.TEXT_COLOR, R.attr.qmui_skin_support_round_btn_text_color);
    }


    public QMUIRoundButton(Context context) {
        super(context);
        init(context, null, 0);
    }

    public QMUIRoundButton(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.QMUIButtonStyle);
        init(context, attrs, R.attr.QMUIButtonStyle);
    }

    public QMUIRoundButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mRoundBg = QMUIRoundButtonDrawable.fromAttributeSet(context, attrs, defStyleAttr);
        QMUIViewHelper.setBackgroundKeepingPadding(this, mRoundBg);
        setChangeAlphaWhenDisable(false);
        setChangeAlphaWhenPress(false);
    }

    @Override
    public void setBackgroundColor(int color) {
        mRoundBg.setBgData(ColorStateList.valueOf(color));
    }

    public void setBgData(@Nullable ColorStateList colors) {
        mRoundBg.setBgData(colors);
    }

    public void setStrokeData(int width, @Nullable ColorStateList colors) {
        mRoundBg.setStrokeData(width, colors);
    }

    public int getStrokeWidth(){
        return mRoundBg.getStrokeWidth();
    }

    public void setStrokeColors(ColorStateList colors) {
        mRoundBg.setStrokeColors(colors);
    }

    @Override
    public SimpleArrayMap<String, Integer> getDefaultSkinAttrs() {
        return sDefaultSkinAttrs;
    }
}
