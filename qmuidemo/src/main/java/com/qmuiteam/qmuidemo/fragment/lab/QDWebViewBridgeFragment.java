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

package com.qmuiteam.qmuidemo.fragment.lab;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.Toast;

import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.webview.QMUIBridgeWebViewClient;
import com.qmuiteam.qmui.widget.webview.QMUIWebView;
import com.qmuiteam.qmui.widget.webview.QMUIWebViewBridgeHandler;
import com.qmuiteam.qmui.widget.webview.QMUIWebViewClient;
import com.qmuiteam.qmui.widget.webview.QMUIWebViewContainer;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.fragment.QDWebExplorerFragment;
import com.qmuiteam.qmuidemo.lib.Group;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDSchemeManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Widget(group = Group.Other, name = "Webview Bridge")
public class QDWebViewBridgeFragment extends QDWebExplorerFragment {

    public QDWebViewBridgeFragment() {
        String url = "file:///android_asset/demo.html";
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_URL, url);
        bundle.putString(EXTRA_TITLE, "测试 Bridge");
        setArguments(bundle);
    }

    @Override
    protected boolean needDispatchSafeAreaInset() {
        return false;
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
                        .setSkinManager(QMUISkinManager.defaultInstance(getContext()))
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
    protected QMUIWebViewClient getWebViewClient() {
        QMUIWebViewBridgeHandler handler = new QMUIWebViewBridgeHandler(mWebView) {

            @Override
            protected List<String> getSupportedCmdList() {
                List<String> ret = new ArrayList<>();
                ret.add("test");
                return ret;
            }

            @Override
            protected void handleMessage(String message, MessageFinishCallback callback) {
                try {
                    JSONObject json = new JSONObject(message);
                    String id = json.getString("id");
                    String info = json.getString("info");
                    Toast.makeText(getContext(), "id = " + id + "; info = " + info, Toast.LENGTH_SHORT).show();
                    JSONObject result = new JSONObject();
                    result.put("code", 100);
                    result.put("message", "Native 的执行结果");
                    callback.finish(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.finish(null);
                }
            }

        };
        return new QMUIBridgeWebViewClient(needDispatchSafeAreaInset(), false, handler){
            @Override
            @TargetApi(21)
            protected boolean onShouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if(QDSchemeManager.getInstance().handle(request.getUrl().toString())){
                    return true;
                }
                return super.onShouldOverrideUrlLoading(view, request);
            }

            @Override
            protected boolean onShouldOverrideUrlLoading(WebView view, String url) {
                if(QDSchemeManager.getInstance().handle(url)){
                    return true;
                }
                return super.onShouldOverrideUrlLoading(view, url);
            }
        };
    }

    @Override
    protected void onScrollWebContent(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        mTopBarLayout.computeAndSetBackgroundAlpha(scrollY, 0, QMUIDisplayHelper.dp2px(getContext(), 20));
    }
}
