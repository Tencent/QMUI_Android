package com.qmuiteam.qmui.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.util.QMUINotchHelper;

public class QMUINotchConsumeLayout extends FrameLayout implements INotchInsetConsumer {
    public QMUINotchConsumeLayout(Context context) {
        this(context, null);
    }

    public QMUINotchConsumeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QMUINotchConsumeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFitsSystemWindows(false);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!QMUINotchHelper.isNotchOfficialSupport()) {
            notifyInsetMaybeChanged();
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!QMUINotchHelper.isNotchOfficialSupport()) {
            notifyInsetMaybeChanged();
        }
    }

    @Override
    public boolean notifyInsetMaybeChanged() {
        setPadding(
                QMUINotchHelper.getSafeInsetLeft(this),
                QMUINotchHelper.getSafeInsetTop(this),
                QMUINotchHelper.getSafeInsetRight(this),
                QMUINotchHelper.getSafeInsetBottom(this)
        );
        return true;
    }
}
