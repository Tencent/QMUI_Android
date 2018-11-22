package com.qmuiteam.qmui.widget.webview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.util.QMUINotchHelper;
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

        if (mCallback != null) {
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
            if (mCallback != null) {
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

    @Override
    @TargetApi(19)
    public boolean applySystemWindowInsets19(Rect insets) {
        if (getFitsSystemWindows()) {
            Rect childInsets = new Rect(insets);
            mQMUIWindowInsetHelper.computeInsetsWithGravity(this, childInsets);
            setPadding(childInsets.left, childInsets.top, childInsets.right, childInsets.bottom);
            return true;
        }
        return super.applySystemWindowInsets19(insets);
    }

    @Override
    @TargetApi(21)
    public boolean applySystemWindowInsets21(Object insets) {
        if (getFitsSystemWindows()) {
            int insetLeft = 0, insetRight = 0, insetTop = 0, insetBottom = 0;
            if (insets instanceof WindowInsetsCompat) {
                WindowInsetsCompat windowInsetsCompat = (WindowInsetsCompat) insets;
                insetLeft = windowInsetsCompat.getSystemWindowInsetLeft();
                insetRight = windowInsetsCompat.getSystemWindowInsetRight();
                insetTop = windowInsetsCompat.getSystemWindowInsetTop();
                insetBottom = windowInsetsCompat.getSystemWindowInsetBottom();
            } else if (insets instanceof WindowInsets) {
                WindowInsets windowInsets = (WindowInsets) insets;
                insetLeft = windowInsets.getSystemWindowInsetLeft();
                insetRight = windowInsets.getSystemWindowInsetRight();
                insetTop = windowInsets.getSystemWindowInsetTop();
                insetBottom = windowInsets.getSystemWindowInsetBottom();
            }

            if (QMUINotchHelper.needFixLandscapeNotchAreaFitSystemWindow(this) &&
                    getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                insetLeft = Math.max(insetLeft, QMUINotchHelper.getSafeInsetLeft(this));
                insetRight = Math.max(insetRight, QMUINotchHelper.getSafeInsetRight(this));
            }

            Rect childInsets = new Rect(insetLeft, insetTop, insetRight, insetBottom);
            mQMUIWindowInsetHelper.computeInsetsWithGravity(this, childInsets);
            setPadding(childInsets.left, childInsets.top, childInsets.right, childInsets.bottom);
            return true;
        }

        return super.applySystemWindowInsets21(insets);
    }

    public int getWebContentScrollY(){
        if(mCustomView instanceof ViewGroup && ((ViewGroup)mCustomView).getChildCount() > 0){
            ((ViewGroup)mCustomView).getChildAt(0).getScrollY();
        }else if(mWebView != null){
            return mWebView.getScrollY();
        }
        return 0;
    }

    public int getWebContentScrollX(){
        if(mCustomView instanceof ViewGroup && ((ViewGroup)mCustomView).getChildCount() > 0){
            ((ViewGroup)mCustomView).getChildAt(0).getScrollX();
        }else if(mWebView != null){
            return mWebView.getScrollX();
        }
        return 0;
    }

    public interface Callback {
        void onShowCustomView();

        void onHideCustomView();
    }
}
