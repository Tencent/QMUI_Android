package com.qmuiteam.qmui.arch;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUIWindowInsetLayout;

/**
 * 基础的 Activity，配合 {@link QMUIFragment} 使用。
 * Created by cgspine on 15/9/14.
 */
public abstract class QMUIFragmentActivity extends AppCompatActivity {
    public static final String REQUEST_CODE_TAG = "QMUI_REQUEST_CODE_TAG";
    private static final String TAG = "QMUIFragmentActivity";
    private QMUIWindowInsetLayout mFragmentContainer;

    @SuppressWarnings("SameReturnValue")
    protected abstract int getContextViewId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        mFragmentContainer = new QMUIWindowInsetLayout(this);
        mFragmentContainer.setId(getContextViewId());
        setContentView(mFragmentContainer);
    }

    public FrameLayout getFragmentContainer() {
        return mFragmentContainer;
    }

    @Override
    public void onBackPressed() {
        QMUIFragment fragment = getCurrentFragment();
        if (fragment != null) {
            fragment.popBackStack();
        }
    }

    /**
     * 获取当前的 Fragment。
     */
    public QMUIFragment getCurrentFragment() {
        return (QMUIFragment) getSupportFragmentManager().findFragmentById(getContextViewId());
    }

    public void startFragment(QMUIFragment fragment) {
        startFragment(fragment, null);
    }

    public void startFragment(QMUIFragment fragment, Bundle bundle) {
        Log.i(TAG, "startFragment");
        if (bundle != null) {
            Bundle arguments = fragment.getArguments();
            if (arguments == null) {
                arguments = new Bundle();
            }
            arguments.putAll(bundle);
            fragment.setArguments(arguments);
        }
        QMUIFragment.TransitionConfig transitionConfig = fragment.onFetchTransitionConfig();
        String tagName = fragment.getClass().getSimpleName();
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(transitionConfig.enter, transitionConfig.exit, transitionConfig.popenter, transitionConfig.popout)
                .replace(getContextViewId(), fragment, tagName)
                .addToBackStack(tagName)
                .commit();
    }

    public void startFragmentForResult(QMUIFragment fromQmuiFragment, QMUIFragment tofragment, int requestCode) {
        startFragmentForResult(fromQmuiFragment, tofragment, requestCode, null);
    }

    public void startFragmentForResult(QMUIFragment fromQmuiFragment, QMUIFragment tofragment, int requestCode, Bundle bundle) {
        Log.i(TAG, "startFragment");
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putParcelable(REQUEST_CODE_TAG, new RequestInfo(fromQmuiFragment.getId(), requestCode, bundle));
        startFragment(tofragment, bundle);
    }

    public void onActivityFragmentResult(int fragmentId, int requestCode, int resultCode, Bundle data) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        Fragment fragmentById = supportFragmentManager.findFragmentById(fragmentId);
        if (fragmentById != null) {
            if (fragmentById instanceof QMUIFragment) {
                QMUIFragment qmuiFragment = (QMUIFragment) fragmentById;
                if (qmuiFragment.isAdded()) {
                    qmuiFragment.onFragmentResult(resultCode, requestCode, data);
                }
            }
        }
    }

    /**
     * 退出当前的 Fragment。
     */
    public void popBackStack() {
        Log.i(TAG, "popBackStack: getSupportFragmentManager().getBackStackEntryCount() = " + getSupportFragmentManager().getBackStackEntryCount());
        if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            QMUIFragment fragment = getCurrentFragment();
            if (fragment == null) {
                finish();
                return;
            }
            QMUIFragment.TransitionConfig transitionConfig = fragment.onFetchTransitionConfig();
            Object toExec = fragment.onLastFragmentFinish();
            if (toExec != null) {
                if (toExec instanceof QMUIFragment) {
                    QMUIFragment mFragment = (QMUIFragment) toExec;
                    startFragment(mFragment);
                } else if (toExec instanceof Intent) {
                    Intent intent = (Intent) toExec;
                    finish();
                    startActivity(intent);
                    overridePendingTransition(transitionConfig.popenter, transitionConfig.popout);
                } else {
                    throw new Error("can not handle the result in onLastFragmentFinish");
                }
            } else {
                finish();
                overridePendingTransition(transitionConfig.popenter, transitionConfig.popout);
            }
        } else {
            getSupportFragmentManager().popBackStackImmediate();
        }
    }

    /**
     * <pre>
     * 返回到clazz类型的Fragment，
     * 如 Home --> List --> Detail，
     * popBackStack(Home.class)之后，就是Home
     *
     * 如果堆栈没有clazz或者就是当前的clazz（如上例的popBackStack(Detail.class)），就相当于popBackStack()
     * </pre>
     */
    public void popBackStack(Class<? extends QMUIFragment> clazz) {
        getSupportFragmentManager().popBackStack(clazz.getSimpleName(), 0);
    }

    /**
     * <pre>
     * 返回到非clazz类型的Fragment
     *
     * 如果上一个是目标clazz，则会继续pop，直到上一个不是clazz。
     * </pre>
     */
    public void popBackStackInclusive(Class<? extends QMUIFragment> clazz) {
        getSupportFragmentManager().popBackStack(clazz.getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}