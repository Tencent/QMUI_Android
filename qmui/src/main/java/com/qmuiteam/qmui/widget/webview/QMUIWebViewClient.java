package com.qmuiteam.qmui.widget.webview;

import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class QMUIWebViewClient extends WebViewClient {

    public static final int JS_FAKE_KEY_CODE_EVENT = 112; // F1

    private boolean mNeedDispatchSafeAreaInset;

    public QMUIWebViewClient(boolean needDispatchSafeAreaInset) {
        mNeedDispatchSafeAreaInset = needDispatchSafeAreaInset;
    }

    @Override
    public void onPageFinished(final WebView view, String url) {
        super.onPageFinished(view, url);
        boolean sureNotSupportModifyCssEnv = (view instanceof QMUIWebView) &&
                ((QMUIWebView) view).isNotSupportChangeCssEnv();
        if (mNeedDispatchSafeAreaInset && !sureNotSupportModifyCssEnv) {
            String jsCode = "(function(){\n" +
                    "   document.body.addEventListener('keydown', function(e){\n" +
                    "        if(e.keyCode == " + JS_FAKE_KEY_CODE_EVENT + "){\n" +
                    "             var html = document.documentElement;\n" +
                    "             var requestFullscreen = html.requestFullscreen || html.webkitRequestFullscreen;\n" +
                    "             requestFullscreen.call(html);\n" +
                    "        }\n" +
                    "    })\n" +
                    "})()";
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
                view.evaluateJavascript(jsCode, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        dispatchFullscreenRequestEvent(view);
                    }
                });
            }else{
                // Usually, there is no chance to come here.
                view.loadUrl("javascript:" + jsCode);
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dispatchFullscreenRequestEvent(view);
                    }
                }, 250);
            }


        }
    }

    private void dispatchFullscreenRequestEvent(WebView webView){
        KeyEvent keyEvent = new KeyEvent(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_F1,
                0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0);
        webView.dispatchKeyEvent(keyEvent);
    }
}
