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

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.qmuiteam.qmui.QMUILog;
import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.util.QMUILangHelper;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class QMUISkinLayoutInflaterFactory implements LayoutInflater.Factory2 {
    private static final String TAG = "QMUISkin";
    private static final String[] sClassPrefixList = {
            "android.widget.",
            "android.webkit.",
            "android.app.",
            "android.view."
    };
    private static final HashMap<String, String> sSuccessClassNamePrefixMap = new HashMap<>();

    private Resources.Theme mEmptyTheme;
    private WeakReference<Activity> mActivityWeakReference;
    private LayoutInflater mOriginLayoutInflater;

    public QMUISkinLayoutInflaterFactory(Activity activity, LayoutInflater originLayoutInflater) {
        mActivityWeakReference = new WeakReference<>(activity);
        mOriginLayoutInflater = originLayoutInflater;
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        Activity activity = mActivityWeakReference.get();
        View view = null;
        if(activity instanceof AppCompatActivity){
            view = ((AppCompatActivity)activity).getDelegate().createView(parent, name, context, attrs);
        }

        if(view == null){
            try{
                if (!name.contains(".")) {
                    if(sSuccessClassNamePrefixMap.containsKey(name)){
                        view = LayoutInflater.from(context)
                                .createView(name, sSuccessClassNamePrefixMap.get(name), attrs);
                    }else{
                        for (String prefix : sClassPrefixList) {
                            view = mOriginLayoutInflater.createView(name, prefix, attrs);
                            if (view != null) {
                                sSuccessClassNamePrefixMap.put(name, prefix);
                                break;
                            }
                        }
                    }
                }else{
                    view = mOriginLayoutInflater.createView(name, null, attrs);
                }
            }catch (ClassNotFoundException ignore){

            }catch (Exception e){
                QMUILog.e(TAG, "Failed to inflate view " + name + "; error: " + e.getMessage());
            }
        }

        if (view != null) {
            QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
            getSkinValueFromAttributeSet(view.getContext(), attrs, builder);
            if (!builder.isEmpty()) {
                QMUISkinHelper.setSkinValue(view, builder);
            }
            QMUISkinValueBuilder.release(builder);
        }

        return view;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return onCreateView(null, name, context, attrs);
    }

    public void getSkinValueFromAttributeSet(Context context, @Nullable AttributeSet attrs, QMUISkinValueBuilder builder) {
        // use a empty theme, so we can get the attr's own value, not it's ref value
        if(mEmptyTheme == null){
            mEmptyTheme = context.getApplicationContext().getResources().newTheme();
        }
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
            int id = context.getResources().getIdentifier(
                    name, "attr", context.getPackageName());
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
            }else if(attr == R.styleable.QMUISkinDef_qmui_skin_bg_tint_color) {
                builder.bgTintColor(id);
            }else if(attr == R.styleable.QMUISkinDef_qmui_skin_progress_color){
                builder.progressColor(id);
            }else if(attr == R.styleable.QMUISkinDef_qmui_skin_underline){
                builder.underline(id);
            }else if(attr == R.styleable.QMUISkinDef_qmui_skin_more_bg_color){
                builder.moreBgColor(id);
            }else if(attr == R.styleable.QMUISkinDef_qmui_skin_more_text_color){
                builder.moreTextColor(id);
            }else if(attr == R.styleable.QMUISkinDef_qmui_skin_hint_color){
                builder.hintColor(id);
            }else if(attr == R.styleable.QMUISkinDef_qmui_skin_text_compound_tint_color){
                builder.textCompoundTintColor(id);
            }else if(attr == R.styleable.QMUISkinDef_qmui_skin_text_compound_src_left){
                builder.textCompoundLeftSrc(id);
            }else if(attr == R.styleable.QMUISkinDef_qmui_skin_text_compound_src_top){
                builder.textCompoundTopSrc(id);
            }else if(attr == R.styleable.QMUISkinDef_qmui_skin_text_compound_src_right){
                builder.textCompoundRightSrc(id);
            }else if(attr == R.styleable.QMUISkinDef_qmui_skin_text_compound_src_bottom){
                builder.textCompoundBottomSrc(id);
            }
        }
        a.recycle();
    }
}
