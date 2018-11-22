package com.qmuiteam.qmui.widget.webview;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.SystemClock;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class QMUIWebViewClient extends WebViewClient {

    public static final int JS_FAKE_KEY_CODE_EVENT = 112; // F1

    private boolean mNeedDispatchSafeAreaInset;
    private boolean mIsPageFinished = false;

    public QMUIWebViewClient(boolean needDispatchSafeAreaInset) {
        mNeedDispatchSafeAreaInset = needDispatchSafeAreaInset;
    }

    public void setNeedDispatchSafeAreaInset(QMUIWebView webView, boolean needDispatchSafeAreaInset) {
        if (mNeedDispatchSafeAreaInset != needDispatchSafeAreaInset) {
            mNeedDispatchSafeAreaInset = needDispatchSafeAreaInset;
            if (mNeedDispatchSafeAreaInset && mIsPageFinished) {
                dispatchFullscreenRequestAction(webView);
            }
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        mIsPageFinished = false;
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(final WebView view, String url) {
        super.onPageFinished(view, url);
        mIsPageFinished = true;
        if (mNeedDispatchSafeAreaInset && view instanceof QMUIWebView) {
            dispatchFullscreenRequestAction((QMUIWebView) view);
        }
    }

    private void dispatchFullscreenRequestAction(final QMUIWebView webView) {
        boolean sureNotSupportModifyCssEnv = webView.isNotSupportChangeCssEnv();
        if (sureNotSupportModifyCssEnv) {
            return;
        }
        String jsCode = "(function(){\n" +
                "   document.body.addEventListener('keydown', function(e){\n" +
                "        if(e.keyCode == " + JS_FAKE_KEY_CODE_EVENT + "){\n" +
                "             var html = document.documentElement;\n" +
                "             var requestFullscreen = html.requestFullscreen || html.webkitRequestFullscreen;\n" +
                "             requestFullscreen.call(html);\n" +
                "        }\n" +
                "    })\n" +
                "})()";
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(jsCode, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    dispatchFullscreenRequestEvent(webView);
                }
            });
        } else {
            // Usually, there is no chance to come here.
            webView.loadUrl("javascript:" + jsCode);
            webView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dispatchFullscreenRequestEvent(webView);
                }
            }, 250);
        }
    }

    private void dispatchFullscreenRequestEvent(WebView webView) {
        KeyEvent keyEvent = new KeyEvent(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_F1,
                0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0);
        webView.dispatchKeyEvent(keyEvent);
    }
}
