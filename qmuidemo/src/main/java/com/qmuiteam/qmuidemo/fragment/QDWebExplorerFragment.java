package com.qmuiteam.qmuidemo.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.ZoomButtonsController;

import com.qmuiteam.qmui.arch.QMUIFragment;
import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.view.QDWebView;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by cgspine on 2017/12/4.
 */

public class QDWebExplorerFragment extends BaseFragment {
    public static final String EXTRA_URL = "EXTRA_URL";
    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    public static final String EXTRA_NEED_DECODE = "EXTRA_NEED_DECODE";

    private final static int PROGRESS_PROCESS = 0;
    private final static int PROGRESS_GONE = 1;


    @BindView(R.id.topbar) protected QMUITopBarLayout mTopBarLayout;
    @BindView(R.id.webview_container) FrameLayout mWebViewContainer;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    private QDWebView mWebView;


    private String mUrl;
    private String mTitle;
    private ProgressHandler mProgressHandler;
    private boolean mIsPageFinished = false;
    private boolean mNeedDecodeUrl = false;

    @Override
    protected View onCreateView() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            String url = bundle.getString(EXTRA_URL);
            mTitle = bundle.getString(EXTRA_TITLE);
            mNeedDecodeUrl = bundle.getBoolean(EXTRA_NEED_DECODE, false);
            if (url != null && url.length() > 0) {
                handleUrl(url);
            }
        }

        mProgressHandler = new ProgressHandler();

        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_webview_explorer, null);
        ButterKnife.bind(this, view);
        initTopbar();
        initWebView();
        return view;
    }

    protected void initTopbar() {
        mTopBarLayout.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });
        updateTitle(mTitle);
    }

    private void updateTitle(String title) {
        if (title != null && !title.equals("")) {
            mTitle = title;
            mTopBarLayout.setTitle(mTitle);
        }
    }

    protected void initWebView() {
        mWebView = new QDWebView(getContext());
        mWebViewContainer.addView(mWebView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                boolean needConfirm = !url.startsWith("http://qmuiteam.com") && !url.startsWith("https://qmuiteam.com");
                if (needConfirm) {
                    final String finalURL = url;
                    new QMUIDialog.MessageDialogBuilder(getContext())
                            .setMessage("确认下载此文件？")
                            .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                    popBackStack();
                                }
                            })
                            .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                    doDownload(finalURL);
                                    popBackStack();
                                }
                            })
                            .show();
                } else {
                    doDownload(url);
                }
            }

            private void doDownload(String url) {

            }
        });

        mWebView.setWebChromeClient(getWebViewChromeClient());
        mWebView.setWebViewClient(getWebViewClient());
        mWebView.requestFocus(View.FOCUS_DOWN);
        setZoomControlGone(mWebView);
        mWebView.loadUrl(mUrl);
    }

    private void handleUrl(String url) {
        if (mNeedDecodeUrl) {
            String decodeURL;
            try {
                decodeURL = URLDecoder.decode(url, "utf-8");
            } catch (UnsupportedEncodingException ignored) {
                decodeURL = url;
            }
            mUrl = decodeURL;
        } else {
            mUrl = url;
        }
    }

    protected WebChromeClient getWebViewChromeClient() {
        return new ExplorerWebViewChromeClient();
    }

    protected WebViewClient getWebViewClient() {
        return new ExplorerWebViewClient();
    }

    private void sendProgressMessage(int progressType, int newProgress, int duration) {
        Message msg = new Message();
        msg.what = progressType;
        msg.arg1 = newProgress;
        msg.arg2 = duration;
        mProgressHandler.sendMessage(msg);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebViewContainer.removeView(mWebView);
            mWebViewContainer.removeAllViews();
            mWebView.destroy();
            mWebView = null;
        }
    }

    public static void setZoomControlGone(WebView webView) {
        webView.getSettings().setDisplayZoomControls(false);
        @SuppressWarnings("rawtypes")
        Class classType;
        Field field;
        try {
            classType = WebView.class;
            field = classType.getDeclaredField("mZoomButtonsController");
            field.setAccessible(true);
            ZoomButtonsController zoomButtonsController = new ZoomButtonsController(
                    webView);
            zoomButtonsController.getZoomControls().setVisibility(View.GONE);
            try {
                field.set(webView, zoomButtonsController);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        } catch (SecurityException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    protected class ExplorerWebViewChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            // 修改进度条
            if (newProgress > mProgressHandler.mDstProgressIndex) {
                sendProgressMessage(PROGRESS_PROCESS, newProgress, 100);
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            updateTitle(view.getTitle());
        }
    }

    protected class ExplorerWebViewClient extends WebViewClient {

        public ExplorerWebViewClient() {
            super();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (QMUILangHelper.isNullOrEmpty(mTitle)) {
                updateTitle(view.getTitle());
            }
            if (mProgressHandler.mDstProgressIndex == 0) {
                sendProgressMessage(PROGRESS_PROCESS, 30, 500);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            sendProgressMessage(PROGRESS_GONE, 100, 0);
            if (QMUILangHelper.isNullOrEmpty(mTitle)) {
                updateTitle(view.getTitle());
            }
        }
    }

    private class ProgressHandler extends Handler {

        private int mDstProgressIndex;
        private int mDuration;
        private ObjectAnimator mAnimator;


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROGRESS_PROCESS:
                    mIsPageFinished = false;
                    mDstProgressIndex = msg.arg1;
                    mDuration = msg.arg2;
                    mProgressBar.setVisibility(View.VISIBLE);
                    if (mAnimator != null && mAnimator.isRunning()) {
                        mAnimator.cancel();
                    }
                    mAnimator = ObjectAnimator.ofInt(mProgressBar, "progress", mDstProgressIndex);
                    mAnimator.setDuration(mDuration);
                    mAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (mProgressBar.getProgress() == 100) {
                                sendEmptyMessageDelayed(PROGRESS_GONE, 500);
                            }
                        }
                    });
                    mAnimator.start();
                    break;
                case PROGRESS_GONE:
                    mDstProgressIndex = 0;
                    mDuration = 0;
                    mProgressBar.setProgress(0);
                    mProgressBar.setVisibility(View.GONE);
                    if (mAnimator != null && mAnimator.isRunning()) {
                        mAnimator.cancel();
                    }
                    mAnimator = ObjectAnimator.ofInt(mProgressBar, "progress", 0);
                    mAnimator.setDuration(0);
                    mAnimator.removeAllListeners();
                    mIsPageFinished = true;
                    break;
                default:
                    break;
            }
        }
    }
}
