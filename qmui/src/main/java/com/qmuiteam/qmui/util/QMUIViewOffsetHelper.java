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

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qmuiteam.qmui.util;

import android.view.View;

import androidx.core.view.ViewCompat;

/**
 * Utility helper for moving a {@link View} around using
 * {@link View#offsetLeftAndRight(int)} and
 * {@link View#offsetTopAndBottom(int)}.
 * <p>
 * Also the setting of absolute offsets (similar to translationX/Y), rather than additive
 * offsets.
 */
public final class QMUIViewOffsetHelper {

    private final View mView;

    private int mLayoutTop;
    private int mLayoutLeft;
    private int mOffsetTop;
    private int mOffsetLeft;

    private boolean mVerticalOffsetEnabled = true;
    private boolean mHorizontalOffsetEnabled = true;

    public QMUIViewOffsetHelper(View view) {
        mView = view;
    }

    public void onViewLayout() {
        // Now grab the intended top
        mLayoutTop = mView.getTop();
        mLayoutLeft = mView.getLeft();

        // And offset it as needed
        updateOffsets();
    }

    private void updateOffsets() {
        ViewCompat.offsetTopAndBottom(mView, mOffsetTop - (mView.getTop() - mLayoutTop));
        ViewCompat.offsetLeftAndRight(mView, mOffsetLeft - (mView.getLeft() - mLayoutLeft));
    }

    /**
     * Set the top and bottom offset for this {@link QMUIViewOffsetHelper}'s view.
     *
     * @param offset the offset in px.
     * @return true if the offset has changed
     */
    public boolean setTopAndBottomOffset(int offset) {
        if (mVerticalOffsetEnabled && mOffsetTop != offset) {
            mOffsetTop = offset;
            updateOffsets();
            return true;
        }
        return false;
    }

    /**
     * Set the left and right offset for this {@link QMUIViewOffsetHelper}'s view.
     *
     * @param offset the offset in px.
     * @return true if the offset has changed
     */
    public boolean setLeftAndRightOffset(int offset) {
        if (mHorizontalOffsetEnabled && mOffsetLeft != offset) {
            mOffsetLeft = offset;
            updateOffsets();
            return true;
        }
        return false;
    }

    public boolean setOffset(int leftOffset, int topOffset) {
        if(!mHorizontalOffsetEnabled && !mVerticalOffsetEnabled){
            return false;
        }else if(mHorizontalOffsetEnabled && mVerticalOffsetEnabled){
            if (mOffsetLeft != leftOffset || mOffsetTop != topOffset) {
                mOffsetLeft = leftOffset;
                mOffsetTop = topOffset;
                updateOffsets();
                return true;
            }
            return false;
        }else if(mHorizontalOffsetEnabled){
            return setLeftAndRightOffset(leftOffset);
        }else{
            return setTopAndBottomOffset(topOffset);
        }
    }

    public int getTopAndBottomOffset() {
        return mOffsetTop;
    }

    public int getLeftAndRightOffset() {
        return mOffsetLeft;
    }

    public int getLayoutTop() {
        return mLayoutTop;
    }

    public int getLayoutLeft() {
        return mLayoutLeft;
    }

    public void setHorizontalOffsetEnabled(boolean horizontalOffsetEnabled) {
        mHorizontalOffsetEnabled = horizontalOffsetEnabled;
    }

    public boolean isHorizontalOffsetEnabled() {
        return mHorizontalOffsetEnabled;
    }

    public void setVerticalOffsetEnabled(boolean verticalOffsetEnabled) {
        mVerticalOffsetEnabled = verticalOffsetEnabled;
    }

    public boolean isVerticalOffsetEnabled() {
        return mVerticalOffsetEnabled;
    }
}