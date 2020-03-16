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
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.collection.SimpleArrayMap;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.skin.defaultAttr.IQMUISkinDefaultAttrProvider;
import com.qmuiteam.qmui.util.QMUIResHelper;

public class QMUIPullRefreshView extends AppCompatImageView implements QMUIPullLayout.ActionPullWatcherView, IQMUISkinDefaultAttrProvider {
    private static final int MAX_ALPHA = 255;
    private static final float TRIM_RATE = 0.85f;
    private static final float TRIM_OFFSET = 0.4f;

    static final int CIRCLE_DIAMETER = 40;
    static final int CIRCLE_DIAMETER_LARGE = 56;

    private CircularProgressDrawable mProgress;
    private int mCircleDiameter;

    private static SimpleArrayMap<String, Integer> sDefaultSkinAttrs;

    static {
        sDefaultSkinAttrs = new SimpleArrayMap<>(4);
        sDefaultSkinAttrs.put(QMUISkinValueBuilder.TINT_COLOR, R.attr.qmui_skin_support_pull_refresh_view_color);
    }

    public QMUIPullRefreshView(Context context) {
        this(context, null);
    }

    public QMUIPullRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mProgress = new CircularProgressDrawable(context);
        setColorSchemeColors(QMUIResHelper.getAttrColor(
                context, R.attr.qmui_skin_support_pull_refresh_view_color));
        mProgress.setStyle(CircularProgressDrawable.LARGE);
        mProgress.setAlpha(MAX_ALPHA);
        mProgress.setArrowScale(0.8f);
        setImageDrawable(mProgress);
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mCircleDiameter = (int) (CIRCLE_DIAMETER * metrics.density);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mCircleDiameter, mCircleDiameter);
    }

    @Override
    public void onActionTriggered() {
        mProgress.start();
    }

    @Override
    public void onActionFinished() {
        mProgress.stop();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mProgress.stop();
    }

    @Override
    public void onPull(QMUIPullLayout.PullAction pullAction, int currentTargetOffset) {
        if (mProgress.isRunning()) {
            return;
        }
        int targetOffset = pullAction.getTargetTriggerOffset();
        float end = TRIM_RATE * Math.min(targetOffset, currentTargetOffset) / targetOffset;
        float rotate = TRIM_OFFSET * currentTargetOffset / targetOffset;
        mProgress.setArrowEnabled(true);
        mProgress.setStartEndTrim(0, end);
        mProgress.setProgressRotation(rotate);
    }

    public void setSize(@CircularProgressDrawable.ProgressDrawableSize int size) {
        if (size != CircularProgressDrawable.LARGE && size != CircularProgressDrawable.DEFAULT) {
            return;
        }
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        if (size == CircularProgressDrawable.LARGE) {
            mCircleDiameter = (int) (CIRCLE_DIAMETER_LARGE * metrics.density);
        } else {
            mCircleDiameter = (int) (CIRCLE_DIAMETER * metrics.density);
        }
        // force the bounds of the progress circle inside the circle view to
        // update by setting it to null before updating its size and then
        // re-setting it
        setImageDrawable(null);
        mProgress.setStyle(size);
        setImageDrawable(mProgress);
    }

    public void stop() {
        mProgress.stop();
    }

    public void doRefresh() {

    }

    public void setColorSchemeResources(@ColorRes int... colorResIds) {
        final Context context = getContext();
        int[] colorRes = new int[colorResIds.length];
        for (int i = 0; i < colorResIds.length; i++) {
            colorRes[i] = ContextCompat.getColor(context, colorResIds[i]);
        }
        setColorSchemeColors(colorRes);
    }

    public void setColorSchemeColors(@ColorInt int... colors) {
        mProgress.setColorSchemeColors(colors);
    }

    @Override
    public SimpleArrayMap<String, Integer> getDefaultSkinAttrs() {
        return sDefaultSkinAttrs;
    }
}
