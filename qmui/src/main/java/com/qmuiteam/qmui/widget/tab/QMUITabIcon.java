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

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;

import com.qmuiteam.qmui.util.QMUIColorHelper;
import com.qmuiteam.qmui.util.QMUILangHelper;

public class QMUITabIcon extends Drawable implements Drawable.Callback {

    public static final int TAB_ICON_INTRINSIC = -1;
    private @NonNull
    Drawable mNormalIconDrawable;
    private @Nullable
    Drawable mSelectedIconDrawable;
    private float mCurrentSelectFraction = 0f;
    private boolean mDynamicChangeIconColor = true;

    public QMUITabIcon(@NonNull Drawable normalIconDrawable, @Nullable Drawable selectedIconDrawable){
        this(normalIconDrawable, selectedIconDrawable, true);
    }
    public QMUITabIcon(@NonNull Drawable normalIconDrawable, @Nullable Drawable selectedIconDrawable, boolean dynamicChangeIconColor) {
        mNormalIconDrawable = normalIconDrawable.mutate();
        mNormalIconDrawable.setCallback(this);
        if (selectedIconDrawable != null) {
            mSelectedIconDrawable = selectedIconDrawable.mutate();
            mSelectedIconDrawable.setCallback(this);
        }

        mNormalIconDrawable.setAlpha(255);
        int nw = mNormalIconDrawable.getIntrinsicWidth();
        int nh = mNormalIconDrawable.getIntrinsicHeight();
        mNormalIconDrawable.setBounds(0, 0, nw, nh);
        if (mSelectedIconDrawable != null) {
            mSelectedIconDrawable.setAlpha(0);
            mSelectedIconDrawable.setBounds(0, 0, nw, nh);
        }
        mDynamicChangeIconColor = dynamicChangeIconColor;
    }

    public boolean hasSelectedIcon() {
        return mSelectedIconDrawable != null;
    }

    public void tint(int normalColor, int selectColor) {
        if (mSelectedIconDrawable == null) {
            DrawableCompat.setTint(mNormalIconDrawable, QMUIColorHelper.computeColor(normalColor, selectColor, mCurrentSelectFraction));
        } else {
            DrawableCompat.setTint(mNormalIconDrawable, normalColor);
            DrawableCompat.setTint(mSelectedIconDrawable, selectColor);
        }
        invalidateSelf();
    }

    public void src(@NonNull Drawable normalDrawable, @NonNull Drawable selectDrawable) {
        int normalAlpha = (int) (255 * (1 - mCurrentSelectFraction));
        mNormalIconDrawable.setCallback(null);
        mNormalIconDrawable = normalDrawable.mutate();
        mNormalIconDrawable.setCallback(this);
        mNormalIconDrawable.setAlpha(normalAlpha);
        if (mSelectedIconDrawable != null) {
            mSelectedIconDrawable.setCallback(null);
        }
        mSelectedIconDrawable = selectDrawable.mutate();
        mSelectedIconDrawable.setCallback(this);
        mSelectedIconDrawable.setAlpha(255 - normalAlpha);
        invalidateSelf();
    }

    public void src(@NonNull Drawable normalDrawable, int normalColor, int selectColor) {
        mNormalIconDrawable.setCallback(this);
        mNormalIconDrawable = normalDrawable.mutate();
        mNormalIconDrawable.setCallback(this);
        if (mSelectedIconDrawable != null) {
            mSelectedIconDrawable.setCallback(null);
            mSelectedIconDrawable = null;
        }
        if(mDynamicChangeIconColor){
            DrawableCompat.setTint(mNormalIconDrawable, QMUIColorHelper.computeColor(normalColor, selectColor, mCurrentSelectFraction));
        }

        invalidateSelf();
    }

    @Override
    public int getIntrinsicWidth() {
        return mNormalIconDrawable.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mNormalIconDrawable.getIntrinsicHeight();
    }

    @Override
    public void setAlpha(int alpha) {
        // not used
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        // not used
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    /**
     * set the select faction for QMUITabIcon, value must be in [0, 1]
     *
     * @param fraction muse be in [0, 1]
     */
    public void setSelectFraction(float fraction, int color) {
        fraction = QMUILangHelper.constrain(fraction, 0f, 1f);
        mCurrentSelectFraction = fraction;
        if (mSelectedIconDrawable == null) {
            if(mDynamicChangeIconColor){
                DrawableCompat.setTint(mNormalIconDrawable, color);
            }
        } else {
            int normalAlpha = (int) (255 * (1 - fraction));
            mNormalIconDrawable.setAlpha(normalAlpha);
            mSelectedIconDrawable.setAlpha(255 - normalAlpha);
        }
        invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        mNormalIconDrawable.draw(canvas);
        if (mSelectedIconDrawable != null) {
            mSelectedIconDrawable.draw(canvas);
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mNormalIconDrawable.setBounds(bounds);
        if (mSelectedIconDrawable != null) {
            mSelectedIconDrawable.setBounds(bounds);
        }
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable who) {
        Callback callback = getCallback();
        if(callback != null){
            callback.invalidateDrawable(who);
        }
    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        Callback callback = getCallback();
        if(callback != null){
            callback.scheduleDrawable(who, what, when);
        }
    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
        Callback callback = getCallback();
        if(callback != null){
            callback.unscheduleDrawable(who, what);
        }
    }
}
