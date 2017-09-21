package com.qmuiteam.qmui.widget.popup;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

/**
 * 修改自 @author Lorensius W. L. T <lorenz@londatiga.net>
 */
public abstract class QMUIBasePopup {
    protected Context mContext;
    protected PopupWindow mWindow;
    private RootView mRootViewWrapper;
    protected View mRootView;
    protected Drawable mBackground = null;
    protected WindowManager mWindowManager;
    private PopupWindow.OnDismissListener mDismissListener;

    protected Point mScreenSize = new Point();
    protected int mWindowHeight = 0;
    protected int mWindowWidth = 0;

    //cache
    private boolean mNeedCacheSize = true;

    /**
     * Constructor.
     *
     * @param context Context
     */
    public QMUIBasePopup(Context context) {
        mContext = context;
        mWindow = new PopupWindow(context);
        mWindow.setTouchInterceptor(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    mWindow.dismiss();
                    return false;
                }
                return false;
            }
        });

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

    }

    /**
     * On dismiss
     */
    protected void onDismiss() {
    }

    /**
     * On PreShow
     */
    protected void onPreShow() {
    }

    public final void show(View view) {
        show(view, view);
    }

    /**
     *
     * @param parent a parent view to get the {@link android.view.View#getWindowToken()} token from
     * @param anchorView provide anchor for positioning
     */
    public final void show(View parent, View anchorView) {
        preShow();
        Display screenDisplay = mWindowManager.getDefaultDisplay();
        screenDisplay.getSize(mScreenSize);
        if (mWindowWidth == 0 || mWindowHeight == 0 || !mNeedCacheSize) {
            measureWindowSize();
        }

        Point point = onShow(anchorView);

        mWindow.showAtLocation(parent, Gravity.NO_GRAVITY, point.x, point.y);

        // 在相关的View被移除时，window也自动移除。避免当Fragment退出后，Fragment中弹出的PopupWindow还存在于界面上。
        anchorView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                if (isShowing()) {
                    dismiss();
                }
            }
        });
    }

    protected abstract Point onShow(View attachedView);

    /**
     * On pre show
     */
    private void preShow() {
        if (mRootViewWrapper == null)
            throw new IllegalStateException("setContentView was not called with a view to display.");

        onPreShow();
        if (mBackground == null){
            mWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        } else {
            mWindow.setBackgroundDrawable(mBackground);
        }

        mWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        mWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mWindow.setTouchable(true);
        mWindow.setFocusable(true);
        mWindow.setOutsideTouchable(true);

        mWindow.setContentView(mRootViewWrapper);
    }

    public boolean isShowing() {
        return mWindow != null && mWindow.isShowing();
    }

    private void measureWindowSize() {
        mRootView.measure(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        mWindowWidth = mRootView.getMeasuredWidth();
        mWindowHeight = mRootView.getMeasuredHeight();
    }

    /**
     * Set background drawable.
     *
     * @param background Background drawable
     */
    public void setBackgroundDrawable(Drawable background) {
        mBackground = background;
    }

    /**
     * Set content view.
     *
     * @param root Root view
     */
    public void setContentView(View root) {
        if (root == null)
            throw new IllegalStateException("setContentView was not called with a view to display.");
        mRootViewWrapper = new RootView(mContext);
        mRootViewWrapper.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mRootView = root;
        mRootViewWrapper.addView(root);
        mWindow.setContentView(mRootViewWrapper);
        mWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                QMUIBasePopup.this.onDismiss();
                if (mDismissListener != null) {
                    mDismissListener.onDismiss();
                }
            }
        });
    }


    /**
     * Set content view.
     *
     * @param layoutResID Resource id
     */
    public void setContentView(int layoutResID) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setContentView(inflater.inflate(layoutResID, null));
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        mDismissListener = listener;
    }

    public void dismiss() {
        mWindow.dismiss();
    }

    protected void onConfigurationChanged(Configuration newConfig) {

    }

    public void setNeedCacheSize(boolean needCacheSize) {
        mNeedCacheSize = needCacheSize;
    }

    public class RootView extends FrameLayout {
        public RootView(Context context) {
            super(context);
        }

        public RootView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onConfigurationChanged(Configuration newConfig) {
            if (mWindow != null && mWindow.isShowing()) {
                mWindow.dismiss();
            }
            QMUIBasePopup.this.onConfigurationChanged(newConfig);

        }
    }
}