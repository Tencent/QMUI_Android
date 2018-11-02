package com.qmuiteam.qmui.arch;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;

import java.lang.ref.WeakReference;

class SwipeBackgroundView extends View {

    private WeakReference<View> mViewWeakReference;

    public SwipeBackgroundView(Context context) {
        super(context);
    }

    public SwipeBackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void bind(Activity activity, Activity swipeActivity) {
        View contentView = activity.findViewById(Window.ID_ANDROID_CONTENT);
        if (contentView != null) {
            int orientation = contentView.getResources().getConfiguration().orientation;
            if (orientation != getResources().getConfiguration().orientation) {
                // the screen orientation changed, reMeasure and reLayout
                if (swipeActivity instanceof InnerBaseActivity) {
                    ((InnerBaseActivity) swipeActivity).convertToTranslucentCauseOrientationChanged();
                }
            }
            mViewWeakReference = new WeakReference<>(contentView);
        }
        invalidate();
    }

    public void unBind() {
        mViewWeakReference = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mViewWeakReference != null && mViewWeakReference.get() != null) {
            mViewWeakReference.get().draw(canvas);
        }
    }
}
