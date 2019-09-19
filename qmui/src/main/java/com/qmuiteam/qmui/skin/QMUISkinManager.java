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
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Trace;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.qmuiteam.qmui.BuildConfig;
import com.qmuiteam.qmui.QMUILog;
import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.skin.handler.IQMUISkinRuleHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleBackgroundHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleBorderHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleSeparatorHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleSrcHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleTextColorHandler;
import com.qmuiteam.qmui.util.QMUIDrawableHelper;

import java.util.HashMap;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public final class QMUISkinManager {
    private static final String TAG = "QMUISkinManager";
    public static final int DEFAULT_SKIN = -1;
    private static QMUISkinManager sInstance;

    @MainThread
    public static QMUISkinManager getInstance(Context context) {
        if (sInstance != null) {
            return sInstance;
        }
        context = context.getApplicationContext();
        return getInstance(context.getResources(), context.getPackageName());
    }

    @MainThread
    public static QMUISkinManager getInstance(Resources resources, String packageName) {
        if (sInstance == null) {
            sInstance = new QMUISkinManager(resources, packageName);
        }
        return sInstance;
    }

    public static void setSkinValue(@NonNull View view, QMUISkinValueBuilder skinValueBuilder) {
        view.setTag(R.id.qmui_skin_value, skinValueBuilder.build());
    }

    public static void setSkinValue(@NonNull View view, String value) {
        view.setTag(R.id.qmui_skin_value, value);
    }

    //==============================================================================================

    private Resources mResources;
    private String mPackageName;
    private SparseArray<SkinItem> mThemes = new SparseArray<>();
    private TypedValue mTmpValue;
    private HashMap<String, IQMUISkinRuleHandler> mRuleHandlers = new HashMap<>();

    // Actually, ViewGroup.OnHierarchyChangeListener is a better choice, but it only has a setter.
    // Add child will trigger onLayoutChange
    private View.OnLayoutChangeListener mOnLayoutChangeListener = new View.OnLayoutChangeListener() {

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (v instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) v;
                int childCount = viewGroup.getChildCount();
                if (childCount > 0) {
                    Integer currentTheme = (Integer) viewGroup.getTag(R.id.qmui_skin_current_index);
                    if (currentTheme != null) {
                        View child;
                        for (int i = 0; i < childCount; i++) {
                            child = viewGroup.getChildAt(i);
                            Integer childTheme = (Integer) child.getTag(R.id.qmui_skin_current_index);
                            if (!currentTheme.equals(childTheme)) {
                                dispatch(child, currentTheme);
                            }
                        }
                    }
                }
            }
        }
    };

    private QMUISkinManager(Resources resources, String packageName) {
        mResources = resources;
        mPackageName = packageName;
        mRuleHandlers.put(QMUISkinValueBuilder.BACKGROUND, new QMUISkinRuleBackgroundHandler());
        mRuleHandlers.put(QMUISkinValueBuilder.TEXT_COLOR, new QMUISkinRuleTextColorHandler());
        mRuleHandlers.put(QMUISkinValueBuilder.SRC, new QMUISkinRuleSrcHandler());
        mRuleHandlers.put(QMUISkinValueBuilder.BORDER, new QMUISkinRuleBorderHandler());
        mRuleHandlers.put(QMUISkinValueBuilder.SEPARATOR, new QMUISkinRuleSeparatorHandler());
    }

    @Nullable
    public Resources.Theme getTheme(int skinIndex) {
        SkinItem skinItem = mThemes.get(skinIndex);
        if (skinItem != null) {
            return skinItem.getTheme();
        }
        return null;
    }

    public int getColor(Resources.Theme theme, int attr) {
        if (mTmpValue == null) {
            mTmpValue = new TypedValue();
        }
        theme.resolveAttribute(attr, mTmpValue, true);
        if (mTmpValue.type == TypedValue.TYPE_ATTRIBUTE) {
            return getColor(theme, mTmpValue.data);
        }
        return mTmpValue.data;
    }

    @Nullable
    public Drawable getDrawable(Context context, Resources.Theme theme, int attr) {
        if (mTmpValue == null) {
            mTmpValue = new TypedValue();
        }
        theme.resolveAttribute(attr, mTmpValue, true);
        if (mTmpValue.type >= TypedValue.TYPE_FIRST_COLOR_INT
                && mTmpValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            return new ColorDrawable(mTmpValue.data);
        }
        if (mTmpValue.type == TypedValue.TYPE_ATTRIBUTE) {
            return getDrawable(context, theme, mTmpValue.data);
        }
        if (mTmpValue.resourceId != 0) {
            return QMUIDrawableHelper.getVectorDrawable(context, mTmpValue.resourceId);
        }
        return null;
    }

    public ColorStateList getColorStateList(Context context, Resources.Theme theme, int attr) {
        if (mTmpValue == null) {
            mTmpValue = new TypedValue();
        }
        theme.resolveAttribute(attr, mTmpValue, true);
        if (mTmpValue.type == TypedValue.TYPE_ATTRIBUTE) {
            return getColorStateList(context, theme, mTmpValue.data);
        }
        return ContextCompat.getColorStateList(context, mTmpValue.resourceId);
    }


    @MainThread
    public void addTheme(int index, int styleRes) {
        SkinItem skinItem = mThemes.get(index);
        if (skinItem != null) {
            if (skinItem.getStyleRes() == styleRes) {
                return;
            }
            throw new RuntimeException("already exist the theme item for " + index);
        }
        skinItem = new SkinItem(styleRes);
        mThemes.append(index, skinItem);
    }


    public void dispatch(View view, int skinIndex) {
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Trace.beginSection("QMUISkin::dispatch");
        }
        if (view == null) {
            return;
        }
        SkinItem skinItem = mThemes.get(skinIndex);
        Resources.Theme theme;
        if (skinItem == null) {
            if (skinIndex != DEFAULT_SKIN) {
                throw new IllegalArgumentException("The skin " + skinIndex + " does not exist");
            }
            theme = view.getContext().getTheme();
        } else {
            theme = skinItem.getTheme();
        }
        runDispatch(view, skinIndex, theme);
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Trace.endSection();
        }
    }


    private void runDispatch(@NonNull View view, int skinIndex, Resources.Theme theme) {
        if (view instanceof IQMUISkinDispatchInterceptorView) {
            if (((IQMUISkinDispatchInterceptorView) view).intercept(skinIndex, theme)) {
                return;
            }
        }

        Integer currentTheme = (Integer) view.getTag(R.id.qmui_skin_current_index);
        if (currentTheme != null && currentTheme == skinIndex) {
            return;
        }
        view.setTag(R.id.qmui_skin_current_index, skinIndex);
        applyTheme(view, skinIndex, theme);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            viewGroup.addOnLayoutChangeListener(mOnLayoutChangeListener);
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                runDispatch(viewGroup.getChildAt(i), skinIndex, theme);
            }
        }
    }

    private void applyTheme(@NonNull View view, int skinIndex, Resources.Theme theme) {
        if (view instanceof IQMUISkinHandlerView) {
            ((IQMUISkinHandlerView) view).handle(skinIndex, theme);
        } else {
            String skinValue = (String) view.getTag(R.id.qmui_skin_value);
            if (skinValue == null || skinValue.isEmpty()) {
                return;
            }
            String[] items = skinValue.split("[|]");
            for (String item : items) {
                String[] kv = item.split(":");
                if (kv.length < 2 || kv.length > 3) {
                    continue;
                }
                String key = kv[0].trim();
                int attr = mResources.getIdentifier(kv[1].trim(), "attr", mPackageName);
                if (attr == 0) {
                    QMUILog.w(TAG, "Failed to get attr id from name: " + kv[1]);
                }
                IQMUISkinRuleHandler handler = mRuleHandlers.get(key);
                if (handler == null) {
                    QMUILog.w(TAG, "Do not find handler for skin attr name: " + key);
                    continue;
                }
                String extra = null;
                if (kv.length == 3) {
                    extra = kv[2].trim();
                }
                handler.handle(this, view, theme, attr, extra);
            }
        }
    }

    class SkinItem {
        private Resources.Theme theme;
        private int styleRes;

        SkinItem(int styleRes) {
            this.styleRes = styleRes;
        }

        public int getStyleRes() {
            return styleRes;
        }

        @NonNull
        Resources.Theme getTheme() {
            if (theme == null) {
                theme = mResources.newTheme();
                theme.applyStyle(styleRes, true);
            }
            return theme;
        }
    }
}
