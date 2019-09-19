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

package com.qmuiteam.qmui.widget.webview;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowInsets;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUINotchHelper;
import com.qmuiteam.qmui.util.QMUIWindowInsetHelper;
import com.qmuiteam.qmui.widget.IWindowInsetLayout;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class QMUIWebView extends WebView implements IWindowInsetLayout {

    private static final String TAG = "QMUIWebView";
    private static boolean sIsReflectionOccurError = false;

    private Object mAwContents;
    private Object mWebContents;
    private Method mSetDisplayCutoutSafeAreaMethod;
    private Rect mSafeAreaRectCache;

    /**
     * if true, the web content may be located under status bar
     */
    private boolean mNeedDispatchSafeAreaInset = false;
    private Callback mCallback;
    private List<OnScrollChangeListener> mOnScrollChangeListeners = new ArrayList<>();
    private QMUIWindowInsetHelper mWindowInsetHelper;


    public QMUIWebView(Context context) {
        super(context);
        init();
    }

    public QMUIWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public QMUIWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        removeJavascriptInterface("searchBoxJavaBridge_");
        removeJavascriptInterface("accessibility");
        removeJavascriptInterface("accessibilityTraversal");
        mWindowInsetHelper = new QMUIWindowInsetHelper(this, this);
    }

    @Override
    public void addJavascriptInterface(Object object, String name) {

    }

    @Deprecated
    public void setCustomOnScrollChangeListener(OnScrollChangeListener onScrollChangeListener) {
        addCustomOnScrollChangeListener(onScrollChangeListener);
    }

    public void addCustomOnScrollChangeListener(OnScrollChangeListener listener) {
        if (!mOnScrollChangeListeners.contains(listener)) {
            mOnScrollChangeListeners.add(listener);
        }
    }

    public void removeOnScrollChangeListener(OnScrollChangeListener listener) {
        mOnScrollChangeListeners.remove(listener);
    }

    public void removeAllOnScrollChangeListener(){
        mOnScrollChangeListeners.clear();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        for (OnScrollChangeListener onScrollListener : mOnScrollChangeListeners) {
            onScrollListener.onScrollChange(this, l, t, oldl, oldt);
        }
    }

    @Override
    public void setWebViewClient(WebViewClient client) {
        if (client != null && !(client instanceof QMUIWebViewClient)) {
            throw new IllegalArgumentException("must use the instance of QMUIWebViewClient");
        }
        super.setWebViewClient(client);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    public void setNeedDispatchSafeAreaInset(boolean needDispatchSafeAreaInset) {
        if (mNeedDispatchSafeAreaInset != needDispatchSafeAreaInset) {
            mNeedDispatchSafeAreaInset = needDispatchSafeAreaInset;
            if (ViewCompat.isAttachedToWindow(this)) {
                if (needDispatchSafeAreaInset) {
                    ViewCompat.requestApplyInsets(this);
                } else {
                    // clear insets
                    setStyleDisplayCutoutSafeArea(new Rect());
                }
            }
        }
    }

    public boolean isNeedDispatchSafeAreaInset() {
        return mNeedDispatchSafeAreaInset;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    private void doNotSupportChangeCssEnv() {
        sIsReflectionOccurError = true;
        if (mCallback != null) {
            mCallback.onSureNotSupportChangeCssEnv();
        }
    }

    boolean isNotSupportChangeCssEnv() {
        return sIsReflectionOccurError;
    }

    @Override
    public boolean applySystemWindowInsets19(Rect insets) {
        return false;
    }

    @Override
    public boolean applySystemWindowInsets21(Object insets) {
        if (!mNeedDispatchSafeAreaInset) {
            return false;
        }
        float density = QMUIDisplayHelper.getDensity(getContext());
        int left, top, right, bottom;
        if (QMUINotchHelper.isNotchOfficialSupport()) {
            WindowInsets windowInsets = (WindowInsets) insets;
            left = windowInsets.getSystemWindowInsetLeft();
            top = windowInsets.getSystemWindowInsetTop();
            right = windowInsets.getSystemWindowInsetRight();
            bottom = windowInsets.getSystemWindowInsetBottom();
        } else {
            WindowInsetsCompat insetsCompat = (WindowInsetsCompat) insets;
            left = insetsCompat.getSystemWindowInsetLeft();
            top = insetsCompat.getSystemWindowInsetTop();
            right = insetsCompat.getSystemWindowInsetRight();
            bottom = insetsCompat.getSystemWindowInsetBottom();
        }
        Rect rect = new Rect(
                (int) (left / density + getExtraInsetLeft(density)),
                (int) (top / density + getExtraInsetTop(density)),
                (int) (right / density + getExtraInsetRight(density)),
                (int) (bottom / density + getExtraInsetBottom(density))
        );
        setStyleDisplayCutoutSafeArea(rect);
        return true;
    }

    protected int getExtraInsetTop(float density) {
        return 0;
    }

    protected int getExtraInsetLeft(float density) {
        return 0;
    }

    protected int getExtraInsetRight(float density) {
        return 0;
    }

    protected int getExtraInsetBottom(float density) {
        return 0;
    }

    @Override
    public void destroy() {
        mAwContents = null;
        mWebContents = null;
        mSetDisplayCutoutSafeAreaMethod = null;
        stopLoading();
        super.destroy();
    }

    private void setStyleDisplayCutoutSafeArea(@NonNull Rect rect) {
        if (sIsReflectionOccurError || Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            return;
        }

        if (rect == mSafeAreaRectCache) {
            return;
        }

        if (mSafeAreaRectCache == null) {
            mSafeAreaRectCache = new Rect(rect);
        } else {
            mSafeAreaRectCache.set(rect);
        }

        long start = System.currentTimeMillis();
        if (mAwContents == null || mWebContents == null || mSetDisplayCutoutSafeAreaMethod == null) {
            try {
                Field providerField = WebView.class.getDeclaredField("mProvider");
                providerField.setAccessible(true);
                Object provider = providerField.get(this);

                mAwContents = getAwContentsFieldValueInProvider(provider);
                if (mAwContents == null) {
                    return;
                }

                mWebContents = getWebContentsFieldValueInAwContents(mAwContents);
                if (mWebContents == null) {
                    return;
                }

                mSetDisplayCutoutSafeAreaMethod = getSetDisplayCutoutSafeAreaMethodInWebContents(mWebContents);
                if (mSetDisplayCutoutSafeAreaMethod == null) {
                    // no such method, maybe the old version
                    doNotSupportChangeCssEnv();
                    return;
                }
            } catch (Exception e) {
                doNotSupportChangeCssEnv();
                Log.i(TAG, "setStyleDisplayCutoutSafeArea error: " + e);
            }
        }

        try {
            mSetDisplayCutoutSafeAreaMethod.setAccessible(true);
            mSetDisplayCutoutSafeAreaMethod.invoke(mWebContents, rect);
        } catch (Exception e) {
            sIsReflectionOccurError = true;
            Log.i(TAG, "setStyleDisplayCutoutSafeArea error: " + e);
        }

        Log.i(TAG, "setDisplayCutoutSafeArea speed time: " + (System.currentTimeMillis() - start));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewCompat.requestApplyInsets(this);
    }

    private Object getAwContentsFieldValueInProvider(Object provider) throws IllegalAccessException, NoSuchFieldException {
        try {
            Field awContentsField = provider.getClass().getDeclaredField("mAwContents");
            if (awContentsField != null) {
                awContentsField.setAccessible(true);
                return awContentsField.get(provider);
            }
        } catch (NoSuchFieldException ignored) {

        }
        // Unfortunately, the source code is ugly in some roms, so we can not reflect the field/method by name
        for (Field field : provider.getClass().getDeclaredFields()) {
            // 1. get field mAwContents
            field.setAccessible(true);
            Object awContents = field.get(provider);
            if (awContents == null) {
                continue;
            }
            if (awContents.getClass().getSimpleName().equals("AwContents")) {
                return awContents;
            }
        }
        return null;
    }

    private Object getWebContentsFieldValueInAwContents(Object awContents) throws IllegalAccessException {
        try {
            Field webContentsField = awContents.getClass().getDeclaredField("mWebContents");
            if (webContentsField != null) {
                webContentsField.setAccessible(true);
                return webContentsField.get(awContents);
            }
        } catch (NoSuchFieldException ignored) {

        }
        // Unfortunately, the source code is ugly in some roms, so we can not reflect the field/method by name
        for (Field innerField : awContents.getClass().getDeclaredFields()) {
            innerField.setAccessible(true);
            Object webContents = innerField.get(awContents);
            if (webContents == null) {
                continue;
            }
            if (webContents.getClass().getSimpleName().equals("WebContentsImpl")) {
                return webContents;
            }
        }
        return null;
    }

    private Method getSetDisplayCutoutSafeAreaMethodInWebContents(Object webContents) {
        try {
            Method setDisplayCutoutSafeAreaMethod = webContents.getClass()
                    .getDeclaredMethod("setDisplayCutoutSafeArea", Rect.class);
            if (setDisplayCutoutSafeAreaMethod != null) {
                return setDisplayCutoutSafeAreaMethod;
            }
        } catch (NoSuchMethodException ignored) {

        }
        // Unfortunately, the source code is ugly in some roms, so we can not reflect the field/method by name
        // not very safe in future
        for (Method method : webContents.getClass().getDeclaredMethods()) {
            if (method.getReturnType() == void.class && method.getParameterTypes().length == 1 &&
                    method.getParameterTypes()[0] == Rect.class) {
                return method;
            }
        }
        return null;
    }

    public interface Callback {
        void onSureNotSupportChangeCssEnv();
    }

    public interface OnScrollChangeListener {
        /**
         * Called when the scroll position of a view changes.
         *
         * @param webView    The view whose scroll position has changed.
         * @param scrollX    Current horizontal scroll origin.
         * @param scrollY    Current vertical scroll origin.
         * @param oldScrollX Previous horizontal scroll origin.
         * @param oldScrollY Previous vertical scroll origin.
         */
        void onScrollChange(WebView webView, int scrollX, int scrollY, int oldScrollX, int oldScrollY);
    }
}
