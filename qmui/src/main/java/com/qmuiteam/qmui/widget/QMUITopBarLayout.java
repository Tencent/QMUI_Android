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

package com.qmuiteam.qmui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.alpha.QMUIAlphaImageButton;
import com.qmuiteam.qmui.layout.QMUIFrameLayout;
import com.qmuiteam.qmui.qqface.QMUIQQFaceView;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.skin.defaultAttr.IQMUISkinDefaultAttrProvider;

import androidx.collection.SimpleArrayMap;

/**
 * 这是一个对 {@link QMUITopBar} 的代理类，需要它的原因是：
 * 我们用 fitSystemWindows 实现沉浸式状态栏后，需要将 {@link QMUITopBar} 的背景衍生到状态栏后面，这个时候 fitSystemWindows 是通过
 * 更改 padding 实现的，而 {@link QMUITopBar} 是在高度固定的前提下做各种行为的，例如按钮的垂直居中，因此我们需要在外面包裹一层并消耗 padding
 * <p>
 * 这个类一般是配合 {@link QMUIWindowInsetLayout} 使用，并需要设置 fitSystemWindows 为 true
 * </p>
 *
 * @author cginechen
 * @date 2016-11-26
 */

public class QMUITopBarLayout extends QMUIFrameLayout implements IQMUISkinDefaultAttrProvider {
    private QMUITopBar mTopBar;
    private SimpleArrayMap<String, Integer> mDefaultSkinAttrs = new SimpleArrayMap<>(2);

    public QMUITopBarLayout(Context context) {
        this(context, null);
    }

    public QMUITopBarLayout(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.QMUITopBarStyle);
    }

    public QMUITopBarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDefaultSkinAttrs.put(QMUISkinValueBuilder.BOTTOM_SEPARATOR, R.attr.qmui_skin_support_topbar_separator_color);
        mDefaultSkinAttrs.put(QMUISkinValueBuilder.BACKGROUND, R.attr.qmui_skin_support_topbar_bg);
        mTopBar = new QMUITopBar(context, attrs, defStyleAttr);
        mTopBar.setBackground(null);
        mTopBar.updateBottomDivider(0, 0, 0, 0);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, mTopBar.getTopBarHeight());
        addView(mTopBar, lp);
    }

    public QMUITopBar getTopBar() {
        return mTopBar;
    }

    public void setCenterView(View view) {
        mTopBar.setCenterView(view);
    }

    public QMUIQQFaceView setTitle(int resId) {
        return mTopBar.setTitle(resId);
    }

    public QMUIQQFaceView setTitle(String title) {
        return mTopBar.setTitle(title);
    }

    public void showTitleView(boolean toShow) {
        mTopBar.showTitleView(toShow);
    }

    public QMUIQQFaceView setSubTitle(int resId) {
        return mTopBar.setSubTitle(resId);
    }

    public QMUIQQFaceView setSubTitle(String subTitle) {
        return mTopBar.setSubTitle(subTitle);
    }

    public void setTitleGravity(int gravity) {
        mTopBar.setTitleGravity(gravity);
    }

    public void addLeftView(View view, int viewId) {
        mTopBar.addLeftView(view, viewId);
    }

    public void addLeftView(View view, int viewId, RelativeLayout.LayoutParams layoutParams) {
        mTopBar.addLeftView(view, viewId, layoutParams);
    }

    public void addRightView(View view, int viewId) {
        mTopBar.addRightView(view, viewId);
    }

    public void addRightView(View view, int viewId, RelativeLayout.LayoutParams layoutParams) {
        mTopBar.addRightView(view, viewId, layoutParams);
    }

    public QMUIAlphaImageButton addRightImageButton(int drawableResId, int viewId) {
        return mTopBar.addRightImageButton(drawableResId, viewId);
    }

    public QMUIAlphaImageButton addLeftImageButton(int drawableResId, int viewId) {
        return mTopBar.addLeftImageButton(drawableResId, viewId);
    }

    public Button addLeftTextButton(int stringResId, int viewId) {
        return mTopBar.addLeftTextButton(stringResId, viewId);
    }

    public Button addLeftTextButton(String buttonText, int viewId) {
        return mTopBar.addLeftTextButton(buttonText, viewId);
    }

    public Button addRightTextButton(int stringResId, int viewId) {
        return mTopBar.addRightTextButton(stringResId, viewId);
    }

    public Button addRightTextButton(String buttonText, int viewId) {
        return mTopBar.addRightTextButton(buttonText, viewId);
    }

    public QMUIAlphaImageButton addLeftBackImageButton() {
        return mTopBar.addLeftBackImageButton();
    }

    public void removeAllLeftViews() {
        mTopBar.removeAllLeftViews();
    }

    public void removeAllRightViews() {
        mTopBar.removeAllRightViews();
    }

    public void removeCenterViewAndTitleView() {
        mTopBar.removeCenterViewAndTitleView();
    }

    /**
     * 设置 TopBar 背景的透明度
     *
     * @param alpha 取值范围：[0, 255]，255表示不透明
     */
    public void setBackgroundAlpha(int alpha) {
        this.getBackground().mutate().setAlpha(alpha);
    }

    /**
     * 根据当前 offset、透明度变化的初始 offset 和目标 offset，计算并设置 Topbar 的透明度
     *
     * @param currentOffset     当前 offset
     * @param alphaBeginOffset  透明度开始变化的offset，即当 currentOffset == alphaBeginOffset 时，透明度为0
     * @param alphaTargetOffset 透明度变化的目标offset，即当 currentOffset == alphaTargetOffset 时，透明度为1
     */
    public int computeAndSetBackgroundAlpha(int currentOffset, int alphaBeginOffset, int alphaTargetOffset) {
        double alpha = (float) (currentOffset - alphaBeginOffset) / (alphaTargetOffset - alphaBeginOffset);
        alpha = Math.max(0, Math.min(alpha, 1)); // from 0 to 1
        int alphaInt = (int) (alpha * 255);
        this.setBackgroundAlpha(alphaInt);
        return alphaInt;
    }

    public void setDefaultSkinAttr(String name, int attr) {
        mDefaultSkinAttrs.put(name, attr);
    }

    @Override
    public SimpleArrayMap<String, Integer> getDefaultSkinAttrs() {
        return mDefaultSkinAttrs;
    }
}
