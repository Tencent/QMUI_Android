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

package com.qmuiteam.qmui.widget.dialog;

import android.content.Context;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.ViewStub;
import android.widget.TextView;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.alpha.QMUIAlphaLinearLayout;

/**
 * QMUIBottomSheet 的ItemView
 * @author zander
 * @date 2017-12-05
 */
public class QMUIBottomSheetItemView extends QMUIAlphaLinearLayout {

    private AppCompatImageView mAppCompatImageView;
    private ViewStub mSubScript;
    private TextView mTextView;


    public QMUIBottomSheetItemView(Context context) {
        super(context);
    }

    public QMUIBottomSheetItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QMUIBottomSheetItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAppCompatImageView = (AppCompatImageView) findViewById(R.id.grid_item_image);
        mSubScript = (ViewStub) findViewById(R.id.grid_item_subscript);
        mTextView = (TextView) findViewById(R.id.grid_item_title);
    }

    public AppCompatImageView getAppCompatImageView() {
        return mAppCompatImageView;
    }

    public TextView getTextView() {
        return mTextView;
    }

    public ViewStub getSubScript() {
        return mSubScript;
    }
}
