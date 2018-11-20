package com.qmuiteam.qmui.widget.webview;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.widget.QMUIWindowInsetLayout;

public class QMUIWebViewContainer extends QMUIWindowInsetLayout {

    private QMUIWebView mWebView;
    private View mCustomView;
    private QMUIWebView.OnScrollChangeListener mOnScrollChangeListener;
    private Callback mCallback;


    public QMUIWebViewContainer(Context context) {
        super(context);
    }

    public QMUIWebViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void addWebView(@NonNull QMUIWebView webView, boolean needDispatchSafeAreaInset) {
        mWebView = webView;
        mWebView.setNeedDispatchSafeAreaInset(needDispatchSafeAreaInset);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mWebView.setOnScrollChangeListener(new OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (mOnScrollChangeListener != null) {
                        mOnScrollChangeListener.onScrollChange(v, scrollX, scrollY, oldScrollX, oldScrollY);
                    }
                }
            });
        } else {
            mWebView.setCustomOnScrollChangeListener(new QMUIWebView.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                }
            });
        }
        addView(mWebView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setCustomView(@NonNull View customView) {
        mCustomView = customView;
        addView(customView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // TODO only support for Android M+ ?
            if (customView instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) customView;
                if (viewGroup.getChildCount() > 0) {
                    viewGroup.getChildAt(0).setOnScrollChangeListener(new OnScrollChangeListener() {
                        @Override
                        public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                            if (mOnScrollChangeListener != null) {
                                mOnScrollChangeListener.onScrollChange(v, scrollX, scrollY, oldScrollX, oldScrollY);
                            }
                        }
                    });
                }
            }
        }

        if(mCallback != null){
            mCallback.onShowCustomView();
        }
    }

    public void setNeedDispatchSafeAreaInset(boolean needDispatchSafeAreaInset) {
        if (mWebView != null) {
            mWebView.setNeedDispatchSafeAreaInset(needDispatchSafeAreaInset);
        }
    }

    public void removeCustomView() {
        if (mCustomView != null) {
            removeView(mCustomView);
            mCustomView = null;
            if(mCallback != null){
                mCallback.onHideCustomView();
            }
        }
    }

    public void destroy() {
        removeView(mWebView);
        removeAllViews();
        mCustomView = null;
        mWebView.destroy();
    }


    public void setCustomOnScrollChangeListener(QMUIWebView.OnScrollChangeListener onScrollChangeListener) {
        mOnScrollChangeListener = onScrollChangeListener;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && mCustomView != null) {
            // webView will consume this event and cancel fullscreen state, this is not expected
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    public interface Callback {
        void onShowCustomView();
        void onHideCustomView();
    }
}
