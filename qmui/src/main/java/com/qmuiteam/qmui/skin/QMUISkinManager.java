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
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Trace;
import android.text.Spanned;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.qmuiteam.qmui.BuildConfig;
import com.qmuiteam.qmui.QMUILog;
import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.qqface.QMUIQQFaceView;
import com.qmuiteam.qmui.skin.annotation.QMUISkinListenWithHierarchyChange;
import com.qmuiteam.qmui.skin.defaultAttr.IQMUISkinDefaultAttrProvider;
import com.qmuiteam.qmui.skin.handler.IQMUISkinRuleHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleAlphaHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleBackgroundHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleBgTintColorHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleBorderHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleHintColorHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleMoreBgColorHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleMoreTextColorHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleProgressColorHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleSeparatorHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleSrcHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleTextColorHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleTextCompoundSrcHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleTextCompoundTintColorHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleTintColorHandler;
import com.qmuiteam.qmui.skin.handler.QMUISkinRuleUnderlineHandler;
import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SimpleArrayMap;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

public final class QMUISkinManager {
    private static final String TAG = "QMUISkinManager";
    public static final int DEFAULT_SKIN = -1;
    private static final String[] EMPTY_ITEMS = new String[]{};
    private static QMUISkinManager sInstance;

    @MainThread
    public static QMUISkinManager defaultInstance(Context context) {
        if (sInstance != null) {
            return sInstance;
        }
        context = context.getApplicationContext();
        return defaultInstance(context.getResources(), context.getPackageName());
    }

    @MainThread
    public static QMUISkinManager defaultInstance(Resources resources, String packageName) {
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
        public void onLayoutChange(
                View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
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

    public QMUISkinManager(Resources resources, String packageName) {
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
        mRuleHandlers.put(QMUISkinValueBuilder.BG_TINT_COLOR, new QMUISkinRuleBgTintColorHandler());
        mRuleHandlers.put(QMUISkinValueBuilder.PROGRESS_COLOR, new QMUISkinRuleProgressColorHandler());
        mRuleHandlers.put(QMUISkinValueBuilder.TEXT_COMPOUND_TINT_COLOR, new QMUISkinRuleTextCompoundTintColorHandler());
        IQMUISkinRuleHandler textCompoundSrcHandler = new QMUISkinRuleTextCompoundSrcHandler();
        mRuleHandlers.put(QMUISkinValueBuilder.TEXT_COMPOUND_LEFT_SRC, textCompoundSrcHandler);
        mRuleHandlers.put(QMUISkinValueBuilder.TEXT_COMPOUND_TOP_SRC, textCompoundSrcHandler);
        mRuleHandlers.put(QMUISkinValueBuilder.TEXT_COMPOUND_RIGHT_SRC, textCompoundSrcHandler);
        mRuleHandlers.put(QMUISkinValueBuilder.TEXT_COMPOUND_BOTTOM_SRC, textCompoundSrcHandler);
        mRuleHandlers.put(QMUISkinValueBuilder.HINT_COLOR, new QMUISkinRuleHintColorHandler());
        mRuleHandlers.put(QMUISkinValueBuilder.UNDERLINE, new QMUISkinRuleUnderlineHandler());
        mRuleHandlers.put(QMUISkinValueBuilder.MORE_TEXT_COLOR, new QMUISkinRuleMoreTextColorHandler());
        mRuleHandlers.put(QMUISkinValueBuilder.MORE_BG_COLOR, new QMUISkinRuleMoreBgColorHandler());
    }

    @Nullable
    public Resources.Theme getTheme(int skinIndex) {
        SkinItem skinItem = mSkins.get(skinIndex);
        if (skinItem != null) {
            return skinItem.getTheme();
        }
        return null;
    }

    @Nullable
    public Resources.Theme getCurrentTheme() {
        SkinItem skinItem = mSkins.get(mCurrentSkin);
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

        Integer currentTheme = (Integer) view.getTag(R.id.qmui_skin_current_index);
        if (currentTheme != null && currentTheme == skinIndex) {
            return;
        }
        view.setTag(R.id.qmui_skin_current_index, skinIndex);

        if (view instanceof IQMUISkinDispatchInterceptor) {
            if (((IQMUISkinDispatchInterceptor) view).intercept(skinIndex, theme)) {
                return;
            }
        }
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
        } else if ((view instanceof TextView) || (view instanceof QMUIQQFaceView)) {
            CharSequence text;
            if (view instanceof TextView) {
                text = ((TextView) view).getText();
            } else {
                text = ((QMUIQQFaceView) view).getText();
            }
            if (text instanceof Spanned) {
                IQMUISkinHandlerSpan[] spans = ((Spanned) text).getSpans(0, text.length(), IQMUISkinHandlerSpan.class);
                if (spans != null) {
                    for (int i = 0; i < spans.length; i++) {
                        spans[i].handle(view, this, skinIndex, theme);
                    }
                }
                view.invalidate();
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

    public void refreshTheme(@NonNull View view) {
        Integer skinIndex = (Integer) view.getTag(R.id.qmui_skin_current_index);
        if (skinIndex == null || skinIndex <= 0) {
            return;
        }

        SkinItem skinItem = mSkins.get(skinIndex);
        if (skinItem != null) {
            applyTheme(view, skinIndex, skinItem.theme);
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
            int attr = getAttrFromName(kv[1].trim());
            if (attr == 0) {
                QMUILog.w(TAG, "Failed to get attr id from name: " + kv[1]);
                continue;
            }
            attrs.put(key, attr);
        }
        return attrs;
    }

    public int getAttrFromName(String attrName) {
        return mResources.getIdentifier(attrName, "attr", mPackageName);
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

    // =====================================================================================

    private int mCurrentSkin = DEFAULT_SKIN;
    private final List<WeakReference<?>> mSkinObserverList = new ArrayList<>();
    private final List<WeakReference<OnSkinChangeListener>> mSkinChangeListeners = new ArrayList<>();

    public void register(@NonNull Activity activity) {
        if (!containSkinObserver(activity)) {
            mSkinObserverList.add(new WeakReference<>(activity));
        }
        dispatch(activity.findViewById(Window.ID_ANDROID_CONTENT), mCurrentSkin);
    }

    public void unRegister(@NonNull Activity activity) {
        removeSkinObserver(activity);
    }

    public void register(@NonNull Fragment fragment) {
        if (!containSkinObserver(fragment)) {
            mSkinObserverList.add(new WeakReference<>(fragment));
        }
        dispatch(fragment.getView(), mCurrentSkin);
    }

    public void unRegister(@NonNull Fragment fragment) {
        removeSkinObserver(fragment);
    }

    public void register(@NonNull View view) {
        if (!containSkinObserver(view)) {
            mSkinObserverList.add(new WeakReference<>(view));
        }
        dispatch(view, mCurrentSkin);
    }

    public void unRegister(@NonNull View view) {
        removeSkinObserver(view);
    }

    public void register(@NonNull Dialog dialog) {
        if (!containSkinObserver(dialog)) {
            mSkinObserverList.add(new WeakReference<>(dialog));
        }
        Window window = dialog.getWindow();
        if (window != null) {
            dispatch(window.getDecorView(), mCurrentSkin);
        }
    }

    public void unRegister(@NonNull Dialog dialog) {
        removeSkinObserver(dialog);
    }

    public void register(@NonNull PopupWindow popupWindow) {
        if (!containSkinObserver(popupWindow)) {
            mSkinObserverList.add(new WeakReference<>(popupWindow));
        }
        dispatch(popupWindow.getContentView(), mCurrentSkin);
    }

    public void unRegister(@NonNull PopupWindow popupWindow) {
        removeSkinObserver(popupWindow);
    }

    public void register(@NonNull Window window) {
        if (!containSkinObserver(window)) {
            mSkinObserverList.add(new WeakReference<>(window));
        }
        dispatch(window.getDecorView(), mCurrentSkin);
    }

    public void unRegister(@NonNull Window window) {
        removeSkinObserver(window);
    }

    private void removeSkinObserver(Object object) {
        for (int i = mSkinObserverList.size() - 1; i >= 0; i--) {
            Object item = mSkinObserverList.get(i).get();
            if (item == object) {
                mSkinObserverList.remove(i);
                return;
            } else if (item == null) {
                mSkinObserverList.remove(i);
            }
        }
    }

    private boolean containSkinObserver(Object object) {
        //reverse order for remove
        for (int i = mSkinObserverList.size() - 1; i >= 0; i--) {
            Object item = mSkinObserverList.get(i).get();
            if (item == object) {
                return true;
            } else if (item == null) {
                mSkinObserverList.remove(i);
            }
        }
        return false;
    }

    public void changeSkin(int index) {
        if (mCurrentSkin == index) {
            return;
        }
        int oldIndex = mCurrentSkin;
        mCurrentSkin = index;
        for (int i = mSkinObserverList.size() - 1; i >= 0; i--) {
            Object item = mSkinObserverList.get(i).get();
            if (item == null) {
                mSkinObserverList.remove(i);
            } else {
                if (item instanceof Activity) {
                    Activity activity = (Activity) item;
                    activity.getWindow().setBackgroundDrawable(QMUIResHelper.getAttrDrawable(
                            activity, mSkins.get(index).getTheme(), R.attr.qmui_skin_support_activity_background));
                    dispatch(activity.findViewById(Window.ID_ANDROID_CONTENT), index);
                } else if (item instanceof Fragment) {
                    dispatch(((Fragment) item).getView(), index);
                } else if (item instanceof Dialog) {
                    Window window = ((Dialog) item).getWindow();
                    if (window != null) {
                        dispatch(window.getDecorView(), index);
                    }
                } else if (item instanceof PopupWindow) {
                    dispatch(((PopupWindow) item).getContentView(), index);
                } else if (item instanceof Window) {
                    dispatch(((Window) item).getDecorView(), index);
                } else if (item instanceof View) {
                    dispatch((View) item, index);
                }
            }
        }

        for (int i = mSkinChangeListeners.size() - 1; i >= 0; i--) {
            OnSkinChangeListener item = mSkinChangeListeners.get(i).get();
            if (item == null) {
                mSkinChangeListeners.remove(i);
            } else {
                item.onSkinChange(oldIndex, mCurrentSkin);
            }
        }
    }

    public void addSkinChangeListener(@NonNull OnSkinChangeListener listener) {
        Iterator<WeakReference<OnSkinChangeListener>> iterator = mSkinChangeListeners.iterator();
        while (iterator.hasNext()) {
            Object item = iterator.next().get();
            if (item != null) {
                return;
            } else {
                iterator.remove();
            }
        }
        mSkinChangeListeners.add(new WeakReference<>(listener));
    }

    public void removeSkinChangeListener(@NonNull OnSkinChangeListener listener) {
        Iterator<WeakReference<OnSkinChangeListener>> iterator = mSkinChangeListeners.iterator();
        while (iterator.hasNext()) {
            Object item = iterator.next().get();
            if (item != null) {
                if (item == listener) {
                    iterator.remove();
                }
            } else {
                iterator.remove();
            }
        }
    }

    public int getCurrentSkin() {
        return mCurrentSkin;
    }

    public interface OnSkinChangeListener {
        void onSkinChange(int oldSkin, int newSkin);
    }

}
