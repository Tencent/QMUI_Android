package com.qmuiteam.qmui.widget.webview;

import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebChromeClient;

import java.lang.ref.WeakReference;

public class QMUIWebChromeClient extends WebChromeClient {

    private WeakReference<QMUIWebViewContainer> mWebViewContainer;

    public QMUIWebChromeClient(@Nullable QMUIWebViewContainer webViewContainer) {
        mWebViewContainer = new WeakReference<>(webViewContainer);
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        QMUIWebViewContainer container = mWebViewContainer.get();
        if (container != null) {
            container.setCustomView(view);
        }
    }

    @Override
    public void onHideCustomView() {
        QMUIWebViewContainer container = mWebViewContainer.get();
        if (container != null) {
            container.removeCustomView();
        }
    }
}
