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

import android.graphics.Bitmap;
import android.os.Build;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class QMUIWebViewClient extends WebViewClient {

    public static final int JS_FAKE_KEY_CODE_EVENT = 112; // F1

    private boolean mNeedDispatchSafeAreaInset;
    private boolean mDisableVideoFullscreenBtnAlways;
    private boolean mIsPageFinished = false;

    public QMUIWebViewClient(boolean needDispatchSafeAreaInset, boolean disableVideoFullscreenBtnAlways) {
        mNeedDispatchSafeAreaInset = needDispatchSafeAreaInset;
        mDisableVideoFullscreenBtnAlways = disableVideoFullscreenBtnAlways;
    }


    public void setNeedDispatchSafeAreaInset(QMUIWebView webView) {
        if (!mNeedDispatchSafeAreaInset) {
            mNeedDispatchSafeAreaInset = true;
            if (mIsPageFinished) {
                dispatchFullscreenRequestAction(webView);
            }
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, @Nullable Bitmap favicon) {
        mIsPageFinished = false;
        super.onPageStarted(view, url, favicon);
    }


    @Override
    public void onPageFinished(final WebView view, String url) {
        super.onPageFinished(view, url);
        mIsPageFinished = true;
        if (mDisableVideoFullscreenBtnAlways) {
            runJsCode(view, getJsCodeForDisableVideoFullscreenBtn(), null);
        }
        if (mNeedDispatchSafeAreaInset && view instanceof QMUIWebView) {
            dispatchFullscreenRequestAction((QMUIWebView) view);
        }
    }

    private String getJsCodeForDisableVideoFullscreenBtn() {
        return "(function(){\n" +
                // disable fullscreen btn on video
                "   var head = document.getElementsByTagName('head')[0];\n" +
                "   var style = document.createElement('style');\n" +
                "   style.type = 'text/css';" +
                "   style.innerHTML = 'video::-webkit-media-controls-fullscreen-button{display: none !important;}'\n" +
                "   head.appendChild(style);\n" +
                "})()";
    }

    private String getJsCodeForFullscreenHtml() {
        return "(function(){\n" +
                "   document.body.addEventListener('keydown', function(e){\n" +
                "        if(e.keyCode == " + JS_FAKE_KEY_CODE_EVENT + "){\n" +
                "             var html = document.documentElement;\n" +
                "             var requestFullscreen = html.requestFullscreen || html.webkitRequestFullscreen;\n" +
                "             requestFullscreen.call(html);\n" +
                "        }\n" +
                "    })\n" +
                "})()";
    }

    private void dispatchFullscreenRequestAction(final QMUIWebView webView) {
        boolean sureNotSupportModifyCssEnv = webView.isNotSupportChangeCssEnv();
        if (sureNotSupportModifyCssEnv) {
            return;
        }

        if (!mDisableVideoFullscreenBtnAlways) {
            runJsCode(webView, getJsCodeForDisableVideoFullscreenBtn(), null);
        }
        runJsCode(webView, getJsCodeForFullscreenHtml(), new Runnable() {
            @Override
            public void run() {
                dispatchFullscreenRequestEvent(webView);
            }
        });
    }

    private void dispatchFullscreenRequestEvent(WebView webView) {
        KeyEvent keyEvent = new KeyEvent(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_F1,
                0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0);
        webView.dispatchKeyEvent(keyEvent);
    }

    private void runJsCode(WebView webView, @NonNull String jsCode, @Nullable final Runnable finishAction) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            if (finishAction == null) {
                webView.evaluateJavascript(jsCode, null);
            } else {
                webView.evaluateJavascript(jsCode, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        finishAction.run();
                    }
                });
            }

        } else {
            // Usually, there is no chance to come here.
            webView.loadUrl("javascript:" + jsCode);
            if (finishAction != null) {
                webView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finishAction.run();
                    }
                }, 250);
            }

        }
    }
}
