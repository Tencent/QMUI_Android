package com.qmuiteam.qmui.arch;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.qmuiteam.qmui.util.QMUIColorHelper;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

class SwipeBackgroundView extends View {

    private ArrayList<ViewInfo> mViewWeakReference;
    private boolean mDoRotate = false;

    public SwipeBackgroundView(Context context) {
        super(context);
    }

    public SwipeBackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void bind(Activity activity, Activity swipeActivity, boolean restoreForSubWindow) {
        mDoRotate = false;
        if (mViewWeakReference != null) {
            mViewWeakReference.clear();
        }
        int orientation = activity.getResources().getConfiguration().orientation;
        if (orientation != getResources().getConfiguration().orientation) {
            // the screen orientation changed, reMeasure and reLayout
            int requestedOrientation = activity.getRequestedOrientation();
            if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||
                    requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                // TODO is it suitable for fixed screen orientation
                // the prev activity has locked the screen orientation
                mDoRotate = true;
            } else if (swipeActivity instanceof InnerBaseActivity) {
                swipeActivity.getWindow().getDecorView().setBackgroundColor(0);
                ((InnerBaseActivity) swipeActivity).convertToTranslucentCauseOrientationChanged();
                invalidate();
                return;
            }
        }

        if (!restoreForSubWindow) {
            View contentView = activity.findViewById(Window.ID_ANDROID_CONTENT);
            if (mViewWeakReference == null) {
                mViewWeakReference = new ArrayList<>();
            }
            mViewWeakReference.add(new ViewInfo(contentView, null, true));
            invalidate();
            return;
        }

        try {
            IBinder windowToken = activity.getWindow().getDecorView().getWindowToken();
            Field windowManagerGlobalField = activity.getWindowManager().getClass().getDeclaredField("mGlobal");
            windowManagerGlobalField.setAccessible(true);
            Object windowManagerGlobal = windowManagerGlobalField.get(activity.getWindowManager());
            if (windowManagerGlobal != null) {
                Field viewsField = windowManagerGlobal.getClass().getDeclaredField("mViews");
                viewsField.setAccessible(true);
                Field paramsField = windowManagerGlobal.getClass().getDeclaredField("mParams");
                paramsField.setAccessible(true);
                List<WindowManager.LayoutParams> params = (List<WindowManager.LayoutParams>) paramsField.get(windowManagerGlobal);
                List<View> views = (List<View>) viewsField.get(windowManagerGlobal);
                IBinder activityToken = null;
                // reverse order
                for (int i = params.size() - 1; i >= 0; i--) {
                    WindowManager.LayoutParams lp = params.get(i);
                    View view = views.get(i);
                    if (view.getWindowToken() == windowToken) {
                        activityToken = lp.token;
                        break;
                    }
                }
                if (activityToken != null) {
                    // reverse order
                    for (int i = params.size() - 1; i >= 0; i--) {
                        WindowManager.LayoutParams lp = params.get(i);
                        View view = views.get(i);
                        boolean isMain = view.getWindowToken() == windowToken;
                        // Dialog use activityToken in lp
                        // PopupWindow use windowToken in lp
                        if (isMain || lp.token == activityToken || lp.token == windowToken) {
                            View prevContentView = view.findViewById(Window.ID_ANDROID_CONTENT);
                            if (mViewWeakReference == null) {
                                mViewWeakReference = new ArrayList<>();
                            }
                            if (prevContentView != null) {
                                mViewWeakReference.add(new ViewInfo(prevContentView, lp, isMain));
                            }else {
                                // PopupWindow doest not exist a descendant view with id Window.ID_ANDROID_CONTENT
                                mViewWeakReference.add(new ViewInfo(view, lp, isMain));
                            }
                        }
                    }
                }

            }
        } catch (Exception ignored) {

        } finally {
            // sure get one view
            if (mViewWeakReference == null || mViewWeakReference.isEmpty()) {
                View contentView = activity.findViewById(Window.ID_ANDROID_CONTENT);
                if (mViewWeakReference == null) {
                    mViewWeakReference = new ArrayList<>();
                }
                mViewWeakReference.add(new ViewInfo(contentView, null, true));
            }
        }
        invalidate();
    }

    public void unBind() {
        if (mViewWeakReference != null) {
            mViewWeakReference.clear();
        }
        mViewWeakReference = null;
        mDoRotate = false;
    }

    boolean hasChildWindow() {
        return mViewWeakReference != null && mViewWeakReference.size() > 1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mViewWeakReference != null && mViewWeakReference.size() > 0) {
            if (mDoRotate) {
                canvas.translate(0, getHeight());
                canvas.rotate(-90, 0, 0);
            }
            // reverse order
            for (int i = mViewWeakReference.size() - 1; i >= 0; i--) {
                mViewWeakReference.get(i).draw(canvas);
            }

        }
    }

    static class ViewInfo {
        WeakReference<View> viewRef;
        WindowManager.LayoutParams lp;
        boolean isMain;
        private int[] tempLocations = new int[2];

        public ViewInfo(@NonNull View view, @Nullable WindowManager.LayoutParams lp, boolean isMain) {
            this.viewRef = new WeakReference<>(view);
            this.lp = lp;
            this.isMain = isMain;
        }

        void draw(Canvas canvas) {
            View view = viewRef.get();
            if (view != null) {
                if (isMain || lp == null) {
                    view.draw(canvas);
                } else {
                    canvas.drawColor(QMUIColorHelper.setColorAlpha(Color.BLACK, lp.dimAmount));
                    view.getLocationOnScreen(tempLocations);
                    canvas.translate(tempLocations[0], tempLocations[1]);
                    view.draw(canvas);
                    canvas.translate(-tempLocations[0], -tempLocations[1]);
                }
            }
        }
    }
}
