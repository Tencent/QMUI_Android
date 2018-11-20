package com.qmuiteam.qmuidemo.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebSettings;

import com.qmuiteam.qmui.BuildConfig;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIPackageHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.webview.QMUIWebView;
import com.qmuiteam.qmuidemo.R;

/**
 * Created by cgspine on 2017/12/5.
 */

public class QDWebView extends QMUIWebView {

    public QDWebView(Context context) {
        this(context, null);
    }

    public QDWebView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.webViewStyle);
    }

    public QDWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressLint("SetJavaScriptEnabled")
    protected void init(Context context) {
        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDefaultTextEncodingName("GBK");
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setTextZoom(100);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }

        String screen = QMUIDisplayHelper.getScreenWidth(context) + "x" + QMUIDisplayHelper.getScreenHeight(context);
        String userAgent = "QMUIDemo/" + QMUIPackageHelper.getAppVersion(context)
                + " (Android; " + Build.VERSION.SDK_INT
                + "; Screen/" + screen + "; Scale/" + QMUIDisplayHelper.getDensity(context) + ")";
        String agent = getSettings().getUserAgentString();
        if (agent == null || !agent.contains(userAgent)) {
            getSettings().setUserAgentString(agent + " " + userAgent);
        }

        // 开启调试
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setWebContentsDebuggingEnabled(true);
        }
    }

    public void exec(final String jsCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(jsCode, null);
        } else {
            loadUrl(jsCode);
        }
    }

    @Override
    protected int getExtraInsetTop(float density) {
        return (int) (QMUIResHelper.getAttrDimen(getContext(), R.attr.qmui_topbar_height) / density);
    }
}
