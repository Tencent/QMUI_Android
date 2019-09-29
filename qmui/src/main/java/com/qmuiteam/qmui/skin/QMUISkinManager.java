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
import android.os.Build;
import android.os.Trace;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.qmuiteam.qmui.BuildConfig;
import com.qmuiteam.qmui.QMUILog;
import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.skin.annotation.QMUISkinListenWithHierarchyChange;
import com.qmuiteam.qmui.skin.defaultAttr.IQMUISkinDefaultAttrProvider;
import com.qmuiteam.qmui.skin.handler.IQMUISkinRuleHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleAlphaHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleBackgroundHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleBorderHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleSeparatorHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleSrcHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleTextColorHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleTintColorHandler;
import com.qmuiteam.qmui.util.QMUILangHelper;

import java.util.HashMap;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SimpleArrayMap;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

public final class QMUISkinManager {
    private static final String TAG = "QMUISkinManager";
    public static final int DEFAULT_SKIN = -1;
    private static final String[] EMPTY_ITEMS = new String[]{};
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

    //==============================================================================================

    private Resources mResources;
    private String mPackageName;
    private SparseArray<SkinItem> mSkins = new SparseArray<>();
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

    private ViewGroup.OnHierarchyChangeListener mOnHierarchyChangeListener = new ViewGroup.OnHierarchyChangeListener() {
        @Override
        public void onChildViewAdded(View parent, View child) {
            Integer currentTheme = (Integer) parent.getTag(R.id.qmui_skin_current_index);
            if (currentTheme != null) {
                Integer childTheme = (Integer) child.getTag(R.id.qmui_skin_current_index);
                if (!currentTheme.equals(childTheme)) {
                    dispatch(child, currentTheme);
                }
            }
        }

        @Override
        public void onChildViewRemoved(View parent, View child) {

        }
    };

    private QMUISkinManager(Resources resources, String packageName) {
        mResources = resources;
        mPackageName = packageName;
        mRuleHandlers.put(QMUISkinValueBuilder.BACKGROUND, new QMUISkinRuleBackgroundHandler());
        IQMUISkinRuleHandler textColorHandler = new QMUISkinRuleTextColorHandler();
        mRuleHandlers.put(QMUISkinValueBuilder.TEXT_COLOR, textColorHandler);
        mRuleHandlers.put(QMUISkinValueBuilder.SECOND_TEXT_COLOR, textColorHandler);
        mRuleHandlers.put(QMUISkinValueBuilder.SRC, new QMUISkinRuleSrcHandler());
        mRuleHandlers.put(QMUISkinValueBuilder.BORDER, new QMUISkinRuleBorderHandler());
        IQMUISkinRuleHandler separatorHandler = new QMUISkinRuleSeparatorHandler();
        mRuleHandlers.put(QMUISkinValueBuilder.TOP_SEPARATOR, separatorHandler);
        mRuleHandlers.put(QMUISkinValueBuilder.RIGHT_SEPARATOR, separatorHandler);
        mRuleHandlers.put(QMUISkinValueBuilder.BOTTOM_SEPARATOR, separatorHandler);
        mRuleHandlers.put(QMUISkinValueBuilder.LEFT_SEPARATOR, separatorHandler);
        mRuleHandlers.put(QMUISkinValueBuilder.TINT_COLOR, new QMUISkinRuleTintColorHandler());
        mRuleHandlers.put(QMUISkinValueBuilder.ALPHA, new QMUISkinRuleAlphaHandler());
    }

    @Nullable
    public Resources.Theme getTheme(int skinIndex) {
        SkinItem skinItem = mSkins.get(skinIndex);
        if (skinItem != null) {
            return skinItem.getTheme();
        }
        return null;
    }


    @MainThread
    public void addSkin(int index, int styleRes) {
        if (index <= 0) {
            throw new IllegalArgumentException("index must greater than 0");
        }
        SkinItem skinItem = mSkins.get(index);
        if (skinItem != null) {
            if (skinItem.getStyleRes() == styleRes) {
                return;
            }
            throw new RuntimeException("already exist the theme item for " + index);
        }
        skinItem = new SkinItem(styleRes);
        mSkins.append(index, skinItem);
    }


    public void dispatch(View view, int skinIndex) {
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Trace.beginSection("QMUISkin::dispatch");
        }
        if (view == null) {
            return;
        }
        SkinItem skinItem = mSkins.get(skinIndex);
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
        if (view instanceof IQMUISkinDispatchInterceptor) {
            if (((IQMUISkinDispatchInterceptor) view).intercept(skinIndex, theme)) {
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
            if (useHierarchyChangeListener(viewGroup)) {
                viewGroup.setOnHierarchyChangeListener(mOnHierarchyChangeListener);
            } else {
                viewGroup.addOnLayoutChangeListener(mOnLayoutChangeListener);
            }
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                runDispatch(viewGroup.getChildAt(i), skinIndex, theme);
            }
            if (view instanceof RecyclerView) {
                RecyclerView recyclerView = (RecyclerView) view;
                int itemDecorationCount = recyclerView.getItemDecorationCount();
                for (int i = 0; i < itemDecorationCount; i++) {
                    RecyclerView.ItemDecoration itemDecoration = recyclerView.getItemDecorationAt(i);
                    if (itemDecoration instanceof IQMUISkinHandlerDecoration) {
                        ((IQMUISkinHandlerDecoration) itemDecoration).handle(recyclerView, this, skinIndex, theme);
                    }
                }
            }
        }
    }

    private boolean useHierarchyChangeListener(ViewGroup viewGroup) {
        return viewGroup instanceof RecyclerView ||
                viewGroup instanceof ViewPager ||
                viewGroup instanceof AdapterView ||
                viewGroup.getClass().isAnnotationPresent(QMUISkinListenWithHierarchyChange.class);
    }

    private void applyTheme(@NonNull View view, int skinIndex, Resources.Theme theme) {
        SimpleArrayMap<String, Integer> attrs = getSkinAttrs(view);
        if (view instanceof IQMUISkinHandlerView) {
            ((IQMUISkinHandlerView) view).handle(this, skinIndex, theme, attrs);
        } else {
            defaultHandleSkinAttrs(view, theme, attrs);
        }
    }

    public void defaultHandleSkinAttrs(@NonNull View view, Resources.Theme theme, SimpleArrayMap<String, Integer> attrs) {
        if (attrs != null) {
            for (int i = 0; i < attrs.size(); i++) {
                String key = attrs.keyAt(i);
                Integer attr = attrs.valueAt(i);
                if (attr == null) {
                    continue;
                }
                defaultHandleSkinAttr(view, theme, key, attr);
            }
        }
    }

    public void defaultHandleSkinAttr(View view, Resources.Theme theme, String name, int attr) {
        if (attr == 0) {
            return;
        }
        IQMUISkinRuleHandler handler = mRuleHandlers.get(name);
        if (handler == null) {
            QMUILog.w(TAG, "Do not find handler for skin attr name: " + name);
            return;
        }
        handler.handle(this, view, theme, name, attr);
    }

    @Nullable
    private SimpleArrayMap<String, Integer> getSkinAttrs(View view) {
        String skinValue = (String) view.getTag(R.id.qmui_skin_value);
        String[] items;
        if (skinValue == null || skinValue.isEmpty()) {
            items = EMPTY_ITEMS;
        } else {
            items = skinValue.split("[|]");
        }

        SimpleArrayMap<String, Integer> attrs = null;
        if (view instanceof IQMUISkinDefaultAttrProvider) {
            attrs = new SimpleArrayMap<>(((IQMUISkinDefaultAttrProvider) view).getDefaultSkinAttrs());
        }
        IQMUISkinDefaultAttrProvider provider = (IQMUISkinDefaultAttrProvider) view.getTag(
                R.id.qmui_skin_default_attr_provider);
        if (provider != null) {
            if (attrs != null) {
                // override
                attrs.putAll(provider.getDefaultSkinAttrs());
            } else {
                attrs = new SimpleArrayMap<>(provider.getDefaultSkinAttrs());
            }
        } else if (attrs == null) {
            attrs = new SimpleArrayMap<>(items.length);
        }

        for (String item : items) {
            String[] kv = item.split(":");
            if (kv.length != 2) {
                continue;
            }
            String key = kv[0].trim();
            if (QMUILangHelper.isNullOrEmpty(key)) {
                continue;
            }
            int attr = mResources.getIdentifier(kv[1].trim(), "attr", mPackageName);
            if (attr == 0) {
                QMUILog.w(TAG, "Failed to get attr id from name: " + kv[1]);
                continue;
            }
            attrs.put(key, attr);
        }
        return attrs;
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
