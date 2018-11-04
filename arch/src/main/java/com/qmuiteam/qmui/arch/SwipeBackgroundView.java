package com.qmuiteam.qmui.arch;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;

import java.lang.ref.WeakReference;

class SwipeBackgroundView extends View {

    private WeakReference<View> mViewWeakReference;
    private boolean mDoRotate = false;

    public SwipeBackgroundView(Context context) {
        super(context);
    }

    public SwipeBackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void bind(Activity activity, Activity swipeActivity) {
        mDoRotate = false;
        View contentView = activity.findViewById(Window.ID_ANDROID_CONTENT);
        if (contentView != null) {
            int orientation = contentView.getResources().getConfiguration().orientation;
            if (orientation != getResources().getConfiguration().orientation) {
                // the screen orientation changed, reMeasure and reLayout
                int requestedOrientation = activity.getRequestedOrientation();
                if(requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||
                        requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
                    // TODO is it suitable for fixed screen orientation
                    // the prev activity has locked the screen orientation
                    mDoRotate = true;
                } else if (swipeActivity instanceof InnerBaseActivity) {
                    ((InnerBaseActivity) swipeActivity).convertToTranslucentCauseOrientationChanged();
                }
            }
            mViewWeakReference = new WeakReference<>(contentView);
        }
        invalidate();
    }

    public void unBind() {
        mViewWeakReference = null;
        mDoRotate = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mViewWeakReference != null && mViewWeakReference.get() != null) {
            View view = mViewWeakReference.get();
            if(mDoRotate){
                canvas.translate(0, getHeight());
                canvas.rotate(-90, 0, 0);
            }
            view.draw(canvas);
        }
    }
}
