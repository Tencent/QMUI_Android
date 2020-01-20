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

package com.qmuiteam.qmui.widget.pullLayout;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.collection.SimpleArrayMap;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.ImageViewCompat;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.skin.QMUISkinHelper;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.skin.defaultAttr.IQMUISkinDefaultAttrProvider;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.QMUILoadingView;

public class QMUIPullLoadMoreView extends ConstraintLayout implements QMUIPullLayout.ActionPullWatcherView {

    private boolean mIsLoading = false;
    private QMUILoadingView mLoadingView;
    private AppCompatImageView mArrowView;
    private AppCompatTextView mTextView;
    private int mHeight;
    private String mPullText;
    private String mReleaseText;
    private boolean mIsInReleaseState = false;

    public QMUIPullLoadMoreView(Context context) {
        this(context, null);
    }

    public QMUIPullLoadMoreView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.QMUIPullLoadMoreStyle);

    }

    public QMUIPullLoadMoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs,
                R.styleable.QMUIPullLoadMoreView, defStyleAttr, 0);
        mPullText = array.getString(R.styleable.QMUIPullLoadMoreView_qmui_pull_load_more_pull_text);
        mReleaseText = array.getString(R.styleable.QMUIPullLoadMoreView_qmui_pull_load_more_release_text);
        mHeight = array.getDimensionPixelSize(
                R.styleable.QMUIPullLoadMoreView_qmui_pull_load_more_height,
                QMUIDisplayHelper.dp2px(context, 56));
        int loadSize = array.getDimensionPixelSize(
                R.styleable.QMUIPullLoadMoreView_qmui_pull_load_more_loading_size,
                QMUIDisplayHelper.dp2px(context, 20));
        int textSize = array.getDimensionPixelSize(
                R.styleable.QMUIPullLoadMoreView_qmui_pull_load_more_text_size,
                QMUIDisplayHelper.sp2px(context, 14));
        int arrowTextGap =  array.getDimensionPixelSize(
                R.styleable.QMUIPullLoadMoreView_qmui_pull_load_more_arrow_text_gap,
                QMUIDisplayHelper.dp2px(context, 10));
        Drawable arrow = array.getDrawable(R.styleable.QMUIPullLoadMoreView_qmui_pull_load_more_arrow);
        int bgColor = array.getColor(
                R.styleable.QMUIPullLoadMoreView_qmui_skin_support_pull_load_more_bg_color,
                Color.TRANSPARENT);
        int loadingTintColor = array.getColor(
                R.styleable.QMUIPullLoadMoreView_qmui_skin_support_pull_load_more_loading_tint_color,
                Color.BLACK);
        int arrowTintColor = array.getColor(
                R.styleable.QMUIPullLoadMoreView_qmui_skin_support_pull_load_more_arrow_tint_color,
                Color.BLACK);
        int textColor = array.getColor(
                R.styleable.QMUIPullLoadMoreView_qmui_skin_support_pull_load_more_text_color,
                Color.BLACK);
        array.recycle();

        mLoadingView = new QMUILoadingView(context);
        mLoadingView.setSize(loadSize);
        mLoadingView.setColor(loadingTintColor);
        mLoadingView.setVisibility(View.GONE);
        LayoutParams lp = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftToLeft = LayoutParams.PARENT_ID;
        lp.rightToRight = LayoutParams.PARENT_ID;
        lp.topToTop = LayoutParams.PARENT_ID;
        lp.bottomToBottom = LayoutParams.PARENT_ID;
        addView(mLoadingView, lp);

        mArrowView = new AppCompatImageView(context);
        mArrowView.setId(View.generateViewId());
        mArrowView.setImageDrawable(arrow);
        mArrowView.setRotation(180);
        ImageViewCompat.setImageTintList(mArrowView, ColorStateList.valueOf(arrowTintColor));

        mTextView = new AppCompatTextView(context);
        mTextView.setId(View.generateViewId());
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        mTextView.setTextColor(textColor);
        mTextView.setText(mPullText);

        lp = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftToLeft =  LayoutParams.PARENT_ID;
        lp.rightToLeft = mTextView.getId();
        lp.topToTop = LayoutParams.PARENT_ID;
        lp.bottomToBottom = LayoutParams.PARENT_ID;
        lp.horizontalChainStyle = LayoutParams.CHAIN_PACKED;
        addView(mArrowView, lp);

        lp = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftToRight = mArrowView.getId();
        lp.rightToRight = LayoutParams.PARENT_ID;
        lp.topToTop = LayoutParams.PARENT_ID;
        lp.bottomToBottom = LayoutParams.PARENT_ID;
        lp.leftMargin = arrowTextGap;
        addView(mTextView, lp);
        setBackgroundColor(bgColor);


        QMUISkinValueBuilder skinValueBuilder = QMUISkinValueBuilder.acquire();
        skinValueBuilder.background(R.attr.qmui_skin_support_pull_load_more_bg_color);
        QMUISkinHelper.setSkinValue(this, skinValueBuilder);

        skinValueBuilder.clear();
        skinValueBuilder.tintColor(R.attr.qmui_skin_support_pull_load_more_loading_tint_color);
        QMUISkinHelper.setSkinValue(mLoadingView, skinValueBuilder);

        skinValueBuilder.clear();
        skinValueBuilder.tintColor(R.attr.qmui_skin_support_pull_load_more_arrow_tint_color);
        QMUISkinHelper.setSkinValue(mArrowView, skinValueBuilder);

        skinValueBuilder.clear();
        skinValueBuilder.textColor(R.attr.qmui_skin_support_pull_load_more_text_color);
        QMUISkinHelper.setSkinValue(mTextView, skinValueBuilder);

        skinValueBuilder.release();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY));
    }

    @Override
    public void onPull(QMUIPullLayout.PullAction pullAction, int currentTargetOffset) {
        if(mIsLoading){
            return;
        }
        if(mIsInReleaseState){
            if(pullAction.getTargetTriggerOffset() > currentTargetOffset){
                mIsInReleaseState = false;
                mTextView.setText(mPullText);
                mArrowView.animate().rotation(180).start();
            }
        }else{
            if(pullAction.getTargetTriggerOffset() <= currentTargetOffset){
                mIsInReleaseState = true;
                mTextView.setText(mReleaseText);
                mArrowView.animate().rotation(0).start();
            }
        }

    }

    @Override
    public void onActionTriggered() {
        mIsLoading = true;
        mLoadingView.setVisibility(View.VISIBLE);
        mLoadingView.start();
        mArrowView.setVisibility(View.GONE);
        mTextView.setVisibility(View.GONE);
    }

    @Override
    public void onActionFinished() {
        mIsLoading = false;
        mLoadingView.stop();
        mLoadingView.setVisibility(View.GONE);
        mArrowView.setVisibility(View.VISIBLE);
        mTextView.setVisibility(View.VISIBLE);
    }
}
