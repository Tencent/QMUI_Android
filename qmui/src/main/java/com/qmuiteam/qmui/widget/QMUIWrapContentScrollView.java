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
import android.view.ViewGroup;

/**
 * height is wrapContent but limited by maxHeight
 * <p>
 * Created by cgspine on 2017/12/21.
 */

public class QMUIWrapContentScrollView extends QMUIObservableScrollView {
    private int mMaxHeight = Integer.MAX_VALUE >> 2;

    public QMUIWrapContentScrollView(Context context) {
        super(context);
    }

    public QMUIWrapContentScrollView(Context context, int maxHeight) {
        super(context);
        mMaxHeight = maxHeight;
    }

    public QMUIWrapContentScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QMUIWrapContentScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setMaxHeight(int maxHeight) {
        if (mMaxHeight != maxHeight) {
            mMaxHeight = maxHeight;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ViewGroup.LayoutParams lp = getLayoutParams();
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int maxHeight = Math.min(heightSize, mMaxHeight);
        int expandSpec;
        if (lp != null && lp.height > 0 && lp.height <= mMaxHeight) {
            expandSpec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
        } else {
            expandSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        }

        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
