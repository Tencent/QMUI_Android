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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.util.QMUINotchHelper;
import com.qmuiteam.qmui.util.QMUIWindowInsetHelper;
import com.qmuiteam.qmui.widget.QMUIWindowInsetLayout;

import androidx.annotation.NonNull;
import androidx.core.view.WindowInsetsCompat;

public class QMUIWebViewContainer extends QMUIWindowInsetLayout {

    private QMUIWebView mWebView;
    private QMUIWebView.OnScrollChangeListener mOnScrollChangeListener;

    public QMUIWebViewContainer(Context context) {
        super(context);
    }

    public QMUIWebViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void addWebView(@NonNull QMUIWebView webView, boolean needDispatchSafeAreaInset) {
        mWebView = webView;
        mWebView.setNeedDispatchSafeAreaInset(needDispatchSafeAreaInset);
        mWebView.addCustomOnScrollChangeListener(new QMUIWebView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(WebView webView, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (mOnScrollChangeListener != null) {
                    mOnScrollChangeListener.onScrollChange(webView, scrollX, scrollY, oldScrollX, oldScrollY);
                }
            }
        });
        addView(mWebView, getWebViewLayoutParams());
    }

    protected FrameLayout.LayoutParams getWebViewLayoutParams() {
        return new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }


    public void setNeedDispatchSafeAreaInset(boolean needDispatchSafeAreaInset) {
        if (mWebView != null) {
            mWebView.setNeedDispatchSafeAreaInset(needDispatchSafeAreaInset);
        }
    }

    public void destroy() {
        removeView(mWebView);
        removeAllViews();
        mWebView.setWebChromeClient(null);
        mWebView.setWebViewClient(null);
        mWebView.destroy();
    }


    public void setCustomOnScrollChangeListener(QMUIWebView.OnScrollChangeListener onScrollChangeListener) {
        mOnScrollChangeListener = onScrollChangeListener;
    }

    @Override
    @TargetApi(19)
    public boolean applySystemWindowInsets19(Rect insets) {
        if (getFitsSystemWindows()) {
            Rect childInsets = new Rect(insets);
            QMUIWindowInsetHelper.computeInsets(this, childInsets);
            setPadding(childInsets.left, childInsets.top, childInsets.right, childInsets.bottom);
            return true;
        }
        return super.applySystemWindowInsets19(insets);
    }


    @Override
    public WindowInsetsCompat applySystemWindowInsets21(WindowInsetsCompat insets) {
        if (getFitsSystemWindows()) {
            int insetLeft = insets.getSystemWindowInsetLeft();
            int insetRight = insets.getSystemWindowInsetRight();
            int insetTop = insets.getSystemWindowInsetTop();
            int insetBottom = insets.getSystemWindowInsetBottom();

            if (QMUINotchHelper.needFixLandscapeNotchAreaFitSystemWindow(this) &&
                    getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                insetLeft = Math.max(insetLeft, QMUINotchHelper.getSafeInsetLeft(this));
                insetRight = Math.max(insetRight, QMUINotchHelper.getSafeInsetRight(this));
            }

            Rect childInsets = new Rect(insetLeft, insetTop, insetRight, insetBottom);
            QMUIWindowInsetHelper.computeInsets(this, childInsets);
            setPadding(childInsets.left, childInsets.top, childInsets.right, childInsets.bottom);
            return insets.consumeSystemWindowInsets();
        }
        return super.applySystemWindowInsets21(insets);
    }
}
