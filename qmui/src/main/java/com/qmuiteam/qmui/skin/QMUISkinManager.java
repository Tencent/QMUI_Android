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
import android.util.ArrayMap;
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
import java.util.Objects;

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
    private static ArrayMap<String, QMUISkinManager> sInstances = new ArrayMap<>();
    private static final String DEFAULT_NAME = "default";

    @MainThread
    public static QMUISkinManager defaultInstance(Context context) {
        context = context.getApplicationContext();
        return of(DEFAULT_NAME, context.getResources(), context.getPackageName());
    }

    @MainThread
    public static QMUISkinManager of(String name, Resources resources, String packageName) {
        QMUISkinManager instance = sInstances.get(name);
        if(instance == null){
            instance =  new QMUISkinManager(name, resources, packageName);
            sInstances.put(name, instance);
        }
        return instance;
    }

    @MainThread
    public static QMUISkinManager of(String name, Context context){
        context = context.getApplicationContext();
        return of(name, context.getResources(), context.getPackageName());
    }


    //==============================================================================================

    private String mName;
    private Resources mResources;
    private String mPackageName;
    private SparseArray<SkinItem> mSkins = new SparseArray<>();
    private static HashMap<String, IQMUISkinRuleHandler> sRuleHandlers = new HashMap<>();
    private static HashMap<Integer, Resources.Theme> sStyleIdThemeMap = new HashMap<>();

    static {
        sRuleHandlers.put(QMUISkinValueBuilder.BACKGROUND, new QMUISkinRuleBackgroundHandler());
        IQMUISkinRuleHandler textColorHandler = new QMUISkinRuleTextColorHandler();
        sRuleHandlers.put(QMUISkinValueBuilder.TEXT_COLOR, textColorHandler);
        sRuleHandlers.put(QMUISkinValueBuilder.SECOND_TEXT_COLOR, textColorHandler);
        sRuleHandlers.put(QMUISkinValueBuilder.SRC, new QMUISkinRuleSrcHandler());
        sRuleHandlers.put(QMUISkinValueBuilder.BORDER, new QMUISkinRuleBorderHandler());
        IQMUISkinRuleHandler separatorHandler = new QMUISkinRuleSeparatorHandler();
        sRuleHandlers.put(QMUISkinValueBuilder.TOP_SEPARATOR, separatorHandler);
        sRuleHandlers.put(QMUISkinValueBuilder.RIGHT_SEPARATOR, separatorHandler);
        sRuleHandlers.put(QMUISkinValueBuilder.BOTTOM_SEPARATOR, separatorHandler);
        sRuleHandlers.put(QMUISkinValueBuilder.LEFT_SEPARATOR, separatorHandler);
        sRuleHandlers.put(QMUISkinValueBuilder.TINT_COLOR, new QMUISkinRuleTintColorHandler());
        sRuleHandlers.put(QMUISkinValueBuilder.ALPHA, new QMUISkinRuleAlphaHandler());
        sRuleHandlers.put(QMUISkinValueBuilder.BG_TINT_COLOR, new QMUISkinRuleBgTintColorHandler());
        sRuleHandlers.put(QMUISkinValueBuilder.PROGRESS_COLOR, new QMUISkinRuleProgressColorHandler());
        sRuleHandlers.put(QMUISkinValueBuilder.TEXT_COMPOUND_TINT_COLOR, new QMUISkinRuleTextCompoundTintColorHandler());
        IQMUISkinRuleHandler textCompoundSrcHandler = new QMUISkinRuleTextCompoundSrcHandler();
        sRuleHandlers.put(QMUISkinValueBuilder.TEXT_COMPOUND_LEFT_SRC, textCompoundSrcHandler);
        sRuleHandlers.put(QMUISkinValueBuilder.TEXT_COMPOUND_TOP_SRC, textCompoundSrcHandler);
        sRuleHandlers.put(QMUISkinValueBuilder.TEXT_COMPOUND_RIGHT_SRC, textCompoundSrcHandler);
        sRuleHandlers.put(QMUISkinValueBuilder.TEXT_COMPOUND_BOTTOM_SRC, textCompoundSrcHandler);
        sRuleHandlers.put(QMUISkinValueBuilder.HINT_COLOR, new QMUISkinRuleHintColorHandler());
        sRuleHandlers.put(QMUISkinValueBuilder.UNDERLINE, new QMUISkinRuleUnderlineHandler());
        sRuleHandlers.put(QMUISkinValueBuilder.MORE_TEXT_COLOR, new QMUISkinRuleMoreTextColorHandler());
        sRuleHandlers.put(QMUISkinValueBuilder.MORE_BG_COLOR, new QMUISkinRuleMoreBgColorHandler());
    }

    public static void setRuleHandler(String name, IQMUISkinRuleHandler handler){
        sRuleHandlers.put(name, handler);
    }

    // Actually, ViewGroup.OnHierarchyChangeListener is a better choice, but it only has a setter.
    // Add child will trigger onLayoutChange
    private static View.OnLayoutChangeListener mOnLayoutChangeListener = new View.OnLayoutChangeListener() {

        @Override
        public void onLayoutChange(
                View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (v instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) v;
                int childCount = viewGroup.getChildCount();
                if (childCount > 0) {
                    ViewSkinCurrent current = getViewSkinCurrent(viewGroup);
                    if (current != null) {
                        View child;
                        for (int i = 0; i < childCount; i++) {
                            child = viewGroup.getChildAt(i);
                            ViewSkinCurrent childTheme = getViewSkinCurrent(child);
                            if (!current.equals(childTheme)) {
                                of(current.managerName, child.getContext()).dispatch(child, current.index);
                            }
                        }
                    }
                }
            }
        }
    };


    private static ViewGroup.OnHierarchyChangeListener mOnHierarchyChangeListener = new ViewGroup.OnHierarchyChangeListener() {
        @Override
        public void onChildViewAdded(View parent, View child) {
            ViewSkinCurrent current = getViewSkinCurrent(parent);
            if (current != null) {
                ViewSkinCurrent childTheme = getViewSkinCurrent(child);
                if (!current.equals(childTheme)) {
                    of(current.managerName, child.getContext()).dispatch(child, current.index);
                }
            }
        }

        @Override
        public void onChildViewRemoved(View parent, View child) {

        }
    };



    public QMUISkinManager(String name, Resources resources, String packageName) {
        mName = name;
        mResources = resources;
        mPackageName = packageName;
    }

    public String getName() {
        return mName;
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

    static ViewSkinCurrent getViewSkinCurrent(View view){
        Object current = view.getTag(R.id.qmui_skin_current);
        if(current instanceof ViewSkinCurrent){
            return (ViewSkinCurrent) current;
        }
        return null;
    }

    public void dispatch(View view, int skinIndex) {
        if (view == null) {
            return;
        }
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Trace.beginSection("QMUISkin::dispatch");
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
        ViewSkinCurrent currentTheme = getViewSkinCurrent(view);
        if(currentTheme != null && currentTheme.index == skinIndex && Objects.equals(currentTheme.managerName, mName)){
            return;
        }
        view.setTag(R.id.qmui_skin_current, new ViewSkinCurrent(mName, skinIndex));

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
        try{
            if (view instanceof IQMUISkinHandlerView) {
                ((IQMUISkinHandlerView) view).handle(this, skinIndex, theme, attrs);
            } else {
                defaultHandleSkinAttrs(view, theme, attrs);
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
        }catch (Throwable throwable){
            QMUILog.printErrStackTrace(TAG, throwable,
                    "catch error when apply theme: " + view.getClass().getSimpleName() +
                            "; " + skinIndex + "; attrs = " + (attrs == null ? "null" : attrs.toString()));
        }
    }

    void refreshRecyclerDecoration(@NonNull RecyclerView recyclerView,
                                 @NonNull IQMUISkinHandlerDecoration decoration,
                                 int skinIndex){
        SkinItem skinItem = mSkins.get(skinIndex);
        if (skinItem != null) {
            decoration.handle(recyclerView, this, skinIndex, skinItem.getTheme());
        }
    }

    void refreshTheme(@NonNull View view, int skinIndex) {
        SkinItem skinItem = mSkins.get(skinIndex);
        if (skinItem != null) {
            applyTheme(view, skinIndex, skinItem.getTheme());
        }
    }

    public void defaultHandleSkinAttrs(@NonNull View view, Resources.Theme theme, @Nullable SimpleArrayMap<String, Integer> attrs) {
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
        IQMUISkinRuleHandler handler = sRuleHandlers.get(name);
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
            SimpleArrayMap<String, Integer> defaultAttrs = ((IQMUISkinDefaultAttrProvider) view).getDefaultSkinAttrs();
            if(defaultAttrs != null && !defaultAttrs.isEmpty()){
                attrs = new SimpleArrayMap<>(defaultAttrs);
            }
        }
        IQMUISkinDefaultAttrProvider provider = (IQMUISkinDefaultAttrProvider) view.getTag(
                R.id.qmui_skin_default_attr_provider);
        if (provider != null) {
            SimpleArrayMap<String, Integer> providedAttrs = provider.getDefaultSkinAttrs();
            if(providedAttrs != null && !providedAttrs.isEmpty()){
                if (attrs != null) {
                    attrs.putAll(providedAttrs);
                } else {
                    attrs = new SimpleArrayMap<>(providedAttrs);
                }
            }
        }

        if (attrs == null) {
            if(items.length <= 0){
                return null;
            }
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
        private int styleRes;

        SkinItem(int styleRes) {
            this.styleRes = styleRes;
        }

        public int getStyleRes() {
            return styleRes;
        }

        @NonNull
        Resources.Theme getTheme() {
            Resources.Theme theme = sStyleIdThemeMap.get(styleRes);
            if (theme == null) {
                theme = mResources.newTheme();
                theme.applyStyle(styleRes, true);
                sStyleIdThemeMap.put(styleRes, theme);
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
                item.onSkinChange(this, oldIndex, mCurrentSkin);
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
        void onSkinChange(QMUISkinManager skinManager, int oldSkin, int newSkin);
    }

    class ViewSkinCurrent{
        String managerName;
        int index;
        ViewSkinCurrent(String managerName, int index){
            this.managerName = managerName;
            this.index = index;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ViewSkinCurrent that = (ViewSkinCurrent) o;
            return index == that.index &&
                    Objects.equals(managerName, that.managerName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(managerName, index);
        }
    }
}
