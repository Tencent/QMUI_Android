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
package com.qmuiteam.qmui.skin;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.qmuiteam.qmui.QMUILog;
import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.util.QMUILangHelper;

public class QMUISkinLayoutInflaterFactory implements LayoutInflater.Factory2 {
    private static final String TAG = "QMUISkin";
    private static final String[] sClassPrefixList = {
            "android.widget.",
            "android.webkit.",
            "android.app.",
            "android.view."
    };
    private QMUISkinValueBuilder mBuilder;
    private Resources.Theme mEmptyTheme;

    public QMUISkinLayoutInflaterFactory(Context context) {
        mEmptyTheme = context.getApplicationContext().getResources().newTheme();
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View view = null;
        if (!name.contains(".")) {
            for (String prefix : sClassPrefixList) {
                try {
                    view = LayoutInflater.from(context).createView(name, prefix, attrs);
                    if (view != null) {
                        break;
                    }
                } catch (ClassNotFoundException ignore) {

                }
            }
            if (view == null) {
                QMUILog.e(TAG, "Failed to inflate view " + name);
            }
        } else {
            try {
                view = LayoutInflater.from(context).createView(name, null, attrs);
            } catch (ClassNotFoundException e) {
                QMUILog.e(TAG, "Failed to inflate view " + name + "; error: " + e.getMessage());
            }
        }

        if (view != null) {
            if (mBuilder == null) {
                mBuilder = new QMUISkinValueBuilder();
            } else {
                mBuilder.clear();
            }
            getSkinValueFromAttributeSet(view, attrs, mBuilder);
            if (!mBuilder.isEmpty()) {
                QMUISkinHelper.setSkinValue(view, mBuilder);
            }
            mBuilder.clear();
        }

        return view;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return onCreateView(null, name, context, attrs);
    }

    protected void getSkinValueFromAttributeSet(View view, AttributeSet attrs, QMUISkinValueBuilder builder) {
        // use a empty theme, so we can get the attr's own value, not it's ref value
        TypedArray a = mEmptyTheme.obtainStyledAttributes(attrs, R.styleable.QMUISkinDef, 0, 0);
        int count = a.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = a.getIndex(i);
            String name = a.getString(attr);
            if (QMUILangHelper.isNullOrEmpty(name)) {
                continue;
            }
            if (name.startsWith("?")) {
                name = name.substring(1);
            }
            int id = view.getContext().getResources().getIdentifier(
                    name, "attr", view.getContext().getPackageName());
            if (id == 0) {
                continue;
            }
            if (attr == R.styleable.QMUISkinDef_qmui_skin_background) {
                builder.background(id);
            } else if (attr == R.styleable.QMUISkinDef_qmui_skin_alpha) {
                builder.alpha(id);
            } else if (attr == R.styleable.QMUISkinDef_qmui_skin_border) {
                builder.border(id);
            } else if (attr == R.styleable.QMUISkinDef_qmui_skin_text_color) {
                builder.textColor(id);
            } else if (attr == R.styleable.QMUISkinDef_qmui_skin_second_text_color) {
                builder.secondTextColor(id);
            } else if (attr == R.styleable.QMUISkinDef_qmui_skin_btn_text_color) {
                builder.btnTextColor(id);
            } else if (attr == R.styleable.QMUISkinDef_qmui_skin_src) {
                builder.src(id);
            } else if (attr == R.styleable.QMUISkinDef_qmui_skin_tint_color) {
                builder.tintColor(id);
            } else if (attr == R.styleable.QMUISkinDef_qmui_skin_separator_top) {
                builder.topSeparator(id);
            } else if (attr == R.styleable.QMUISkinDef_qmui_skin_separator_right) {
                builder.rightSeparator(id);
            } else if (attr == R.styleable.QMUISkinDef_qmui_skin_separator_bottom) {
                builder.bottomSeparator(id);
            } else if (attr == R.styleable.QMUISkinDef_qmui_skin_separator_left) {
                builder.leftSeparator(id);
            }
        }
        a.recycle();
    }
}
