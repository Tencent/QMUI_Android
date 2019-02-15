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

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.qmuiteam.qmui.util.QMUIColorHelper;
import com.qmuiteam.qmui.util.QMUIDrawableHelper;

public class QMUITabIcon extends Drawable {

    private final @NonNull Drawable mNormalIconDrawable;
    private final @Nullable Drawable mSelectedIconDrawable;
    private final boolean mDynamicChangeIconColor;
    private final int mNormalIconColor;
    private final int mSelectedIconColor;

    public QMUITabIcon(@NonNull Drawable normalIconDrawable, @NonNull Drawable selectedIconDrawable) {
        mNormalIconDrawable = normalIconDrawable;
        mSelectedIconDrawable = selectedIconDrawable;
        mDynamicChangeIconColor = false;
        mNormalIconColor = 0;
        mSelectedIconColor = 0;
        mNormalIconDrawable.setAlpha(255);
        mSelectedIconDrawable.setAlpha(0);
        int nw = mNormalIconDrawable.getIntrinsicWidth();
        int nh = mNormalIconDrawable.getIntrinsicHeight();
        mNormalIconDrawable.setBounds(0, 0, nw, nh);
        mSelectedIconDrawable.setBounds(0, 0, nw, nh);
    }

    public QMUITabIcon(@NonNull Drawable normalIconDrawable, int normalIconColor, int selectedIconColor) {
        mNormalIconDrawable = normalIconDrawable;
        mSelectedIconDrawable = null;
        mDynamicChangeIconColor = true;
        mNormalIconColor = normalIconColor;
        mSelectedIconColor = selectedIconColor;
        mNormalIconDrawable.setAlpha(255);
        QMUIDrawableHelper.setDrawableTintColor(mNormalIconDrawable, normalIconColor);
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
     * set the select percent for QMUITabIcon, value must be in [0, 1]
     *
     * @param percent muse be in [0, 1]
     */
    public void setCurrentSelectPercent(float percent) {
        percent = Math.min(1f, Math.max(0f, percent));
        if (mDynamicChangeIconColor) {
            int targetColor = QMUIColorHelper.computeColor(mNormalIconColor, mSelectedIconColor, percent);
            QMUIDrawableHelper.setDrawableTintColor(mNormalIconDrawable, targetColor);
        } else {
            int normalAlpha = (int) (255 * (1 - percent));
            mNormalIconDrawable.setAlpha(normalAlpha);
            if (mSelectedIconDrawable != null) {
                mSelectedIconDrawable.setAlpha(255 - normalAlpha);
            }
        }
        invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        mNormalIconDrawable.draw(canvas);
        if (!mDynamicChangeIconColor && mSelectedIconDrawable != null) {
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
