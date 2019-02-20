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

import com.qmuiteam.qmui.util.QMUIDrawableHelper;
import com.qmuiteam.qmui.util.QMUILangHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class QMUITabIcon extends Drawable {

    public static final int TAB_ICON_INTRINSIC = -1;
    private final @NonNull Drawable mNormalIconDrawable;
    private final @Nullable Drawable mSelectedIconDrawable;

    public QMUITabIcon(@NonNull Drawable normalIconDrawable, @Nullable Drawable selectedIconDrawable) {
        mNormalIconDrawable = normalIconDrawable;
        mSelectedIconDrawable = selectedIconDrawable;
        mNormalIconDrawable.setAlpha(255);
        int nw = mNormalIconDrawable.getIntrinsicWidth();
        int nh = mNormalIconDrawable.getIntrinsicHeight();
        mNormalIconDrawable.setBounds(0, 0, nw, nh);
        if (mSelectedIconDrawable != null) {
            mSelectedIconDrawable.setAlpha(0);
            mSelectedIconDrawable.setBounds(0, 0, nw, nh);
        }
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
        if (mSelectedIconDrawable == null) {
            QMUIDrawableHelper.setDrawableTintColor(mNormalIconDrawable, color);
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
}
