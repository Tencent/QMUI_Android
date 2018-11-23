package com.qmuiteam.qmuidemo.fragment.lab;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.webview.QMUIWebView;
import com.qmuiteam.qmui.widget.webview.QMUIWebViewContainer;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.fragment.QDWebExplorerFragment;
import com.qmuiteam.qmuidemo.lib.Group;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;

@Widget(group = Group.Lab, widgetClass = QMUIWebView.class, iconRes = R.mipmap.icon_grid_in_progress)
public class QDWebViewFixFragment extends QDWebExplorerFragment {

    public QDWebViewFixFragment() {
        String url = "http://cgsdream.org/static/html/test-css-env-safe-area-inset.html";
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_URL, url);
        bundle.putString(EXTRA_TITLE, "test-css-env-safe-area-inset");
        setArguments(bundle);
    }

    @Override
    protected boolean needDispatchSafeAreaInset() {
        return true;
    }


    @Override
    protected void configWebView(QMUIWebViewContainer webViewContainer, QMUIWebView webView) {
        webView.setCallback(new QMUIWebView.Callback() {
            @Override
            public void onSureNotSupportChangeCssEnv() {
                new QMUIDialog.MessageDialogBuilder(getContext())
                        .setMessage("Do not support to change css env")
                        .addAction(new QMUIDialogAction(getContext(), R.string.ok, new QMUIDialogAction.ActionListener() {

                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                            }
                        }))
                        .show();
            }
        });
    }

    @Override
    protected WebChromeClient getWebViewChromeClient() {
        return new ExplorerWebViewChromeClient(this) {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                mTopBarLayout.setBackgroundAlpha(0);
            }

            @Override
            public void onHideCustomView() {
                super.onHideCustomView();
            }
        };
    }

    @Override
    protected void onScrollWebContent(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        mTopBarLayout.computeAndSetBackgroundAlpha(scrollY, 0, QMUIDisplayHelper.dp2px(getContext(), 20));
    }
}
