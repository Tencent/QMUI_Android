package com.qmuiteam.qmui.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * @author cginechen
 * @date 2016-11-07
 *
 * https://github.com/yshrsmz/KeyboardVisibilityEvent/blob/master/keyboardvisibilityevent/src/main/java/net/yslibrary/android/keyboardvisibilityevent/AutoActivityLifecycleCallback.java
 */

public abstract class QMUIActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    private final Activity mTargetActivity;

    public QMUIActivityLifecycleCallbacks(Activity targetActivity) {
        mTargetActivity = targetActivity;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (activity == mTargetActivity) {
            mTargetActivity.getApplication().unregisterActivityLifecycleCallbacks(this);
            onTargetActivityDestroyed();
        }
    }

    protected abstract void onTargetActivityDestroyed();
}
