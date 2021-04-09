/*
 * Tencent is pleased to support the open source community by making QMUI_Android available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qmuiteam.qmui.arch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelStoreOwner;

import com.qmuiteam.qmui.QMUILog;
import com.qmuiteam.qmui.arch.annotation.DefaultFirstFragment;
import com.qmuiteam.qmui.arch.first.FirstFragmentFinder;
import com.qmuiteam.qmui.arch.first.FirstFragmentFinders;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;

/**
 * the container activity for {@link QMUIFragment}.
 * Created by cgspine on 15/9/14.
 */
public abstract class QMUIFragmentActivity extends InnerBaseActivity implements QMUIFragmentContainerProvider {
    public static final String QMUI_INTENT_DST_FRAGMENT = "qmui_intent_dst_fragment";
    public static final String QMUI_INTENT_DST_FRAGMENT_NAME = "qmui_intent_dst_fragment_name";
    public static final String QMUI_INTENT_FRAGMENT_ARG = "qmui_intent_fragment_arg";
    private static final String TAG = "QMUIFragmentActivity";
    private RootView mRootView;
    private boolean mIsFirstFragmentAdded = false;
    private boolean isChildHandlePopBackRequested = false;

    @Override
    public int getContextViewId() {
        return R.id.qmui_activity_fragment_container_id;
    }

    @Override
    public FragmentManager getContainerFragmentManager() {
        return getSupportFragmentManager();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        performTranslucent();
        mRootView = onCreateRootView(getContextViewId());
        setContentView(mRootView);
        if (savedInstanceState == null) {
            long start = System.currentTimeMillis();
            Intent intent = getIntent();
            Class<? extends QMUIFragment> firstFragmentClass = null;

            // 1. try get first fragment from annotation @FirstFragments.
            int dstFragment = intent.getIntExtra(QMUI_INTENT_DST_FRAGMENT, -1);
            if (dstFragment != -1) {
                FirstFragmentFinder finder = FirstFragmentFinders.getInstance().get(getClass());
                if (finder != null) {
                    firstFragmentClass = finder.getFragmentClassById(dstFragment);
                }

            }

            // 2. try get first fragment from fragment class name
            if (firstFragmentClass == null) {
                String fragmentClassName = intent.getStringExtra(QMUI_INTENT_DST_FRAGMENT_NAME);
                if (fragmentClassName != null) {
                    try {
                        firstFragmentClass = (Class<? extends QMUIFragment>) Class.forName(fragmentClassName);
                    } catch (ClassNotFoundException e) {
                        QMUILog.d(TAG, "Can not find " + fragmentClassName);
                    }
                }
            }

            // 3. try get fragment from annotation @DefaultFirstFragment
            if (firstFragmentClass == null) {
                firstFragmentClass = getDefaultFirstFragment();
            }

            if (firstFragmentClass != null) {
                QMUIFragment firstFragment = instantiationFirstFragment(firstFragmentClass, intent);
                if (firstFragment != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(getContextViewId(), firstFragment, firstFragment.getClass().getSimpleName())
                            .addToBackStack(firstFragment.getClass().getSimpleName())
                            .commit();
                    mIsFirstFragmentAdded = true;
                }
            }
            Log.i(TAG, "the time it takes to inject first fragment from annotation is " + (System.currentTimeMillis() - start));
        }
    }

    protected void performTranslucent() {
        QMUIStatusBarHelper.translucent(this);
    }

    /**
     * used for subclasses to see if the parent class initializes the first fragment。
     * it must be called after super.onCreate in subclasses.
     *
     * @return true if first fragment is initialized.
     */
    protected boolean isFirstFragmentAdded() {
        return mIsFirstFragmentAdded;
    }

    protected void setFirstFragmentAdded(boolean firstFragmentAdded) {
        mIsFirstFragmentAdded = firstFragmentAdded;
    }

    protected Class<? extends QMUIFragment> getDefaultFirstFragment() {
        Class<?> cls = getClass();
        while (cls != null && cls != QMUIFragmentActivity.class && QMUIFragmentActivity.class.isAssignableFrom(cls)) {
            if (cls.isAnnotationPresent(DefaultFirstFragment.class)) {
                DefaultFirstFragment defaultFirstFragment = cls.getAnnotation(DefaultFirstFragment.class);
                if (defaultFirstFragment != null) {
                    return defaultFirstFragment.value();
                }
            }
            cls = cls.getSuperclass();
        }
        return null;
    }

    protected QMUIFragment instantiationFirstFragment(Class<? extends QMUIFragment> cls, Intent intent) {
        try {
            QMUIFragment fragment = cls.newInstance();
            Bundle args = intent.getBundleExtra(QMUI_INTENT_FRAGMENT_ARG);
            if (args != null) {
                fragment.setArguments(args);
            }
            return fragment;
        } catch (IllegalAccessException e) {
            QMUILog.d(TAG, "Can not access " + cls.getName() + " for first fragment");
        } catch (InstantiationException e) {
            QMUILog.d(TAG, "Can not instance " + cls.getName() + " for first fragment");
        }
        return null;
    }

    @Override
    public FragmentContainerView getFragmentContainerView() {
        return mRootView.getFragmentContainerView();
    }

    @Override
    public ViewModelStoreOwner getContainerViewModelStoreOwner() {
        return this;
    }

    @Override
    public void requestForHandlePopBack(boolean toHandle) {
        isChildHandlePopBackRequested = toHandle;
    }

    @Override
    public boolean isChildHandlePopBackRequested() {
        return isChildHandlePopBackRequested;
    }

    protected RootView onCreateRootView(int fragmentContainerId) {
        return new DefaultRootView(this, fragmentContainerId);
    }

    /**
     * get the current Fragment.
     */
    @Nullable
    public Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(getContextViewId());
    }

    @Nullable
    private QMUIFragment getCurrentQMUIFragment() {
        Fragment current = getCurrentFragment();
        if (current instanceof QMUIFragment) {
            return (QMUIFragment) current;
        }
        return null;
    }


    /**
     * start a new fragment and then destroy current fragment.
     * assume there is a fragment stack(A->B->C), and you use this method to start a new
     * fragment D and destroy fragment C. Now you are in fragment D, if you want call
     * {@link #popBackStack()} to back to B, what the animation should be? Sometimes we hope run
     * animation generated by transition B->C, but sometimes we hope run animation generated by
     * transition C->D. this why second parameter exists.
     *
     * @param fragment                      new fragment to start
     * @param useNewTransitionConfigWhenPop if true, use animation generated by transition C->D,
     *                                      else, use animation generated by transition B->C
     */

    public int startFragmentAndDestroyCurrent(QMUIFragment fragment, final boolean useNewTransitionConfigWhenPop) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.isStateSaved()) {
            QMUILog.d(TAG, "startFragment can not be invoked after onSaveInstanceState");
            return -1;
        }
        QMUIFragment.TransitionConfig transitionConfig = fragment.onFetchTransitionConfig();
        String tagName = fragment.getClass().getSimpleName();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(transitionConfig.enter, transitionConfig.exit,
                        transitionConfig.popenter, transitionConfig.popout)
                .setPrimaryNavigationFragment(null)
                .replace(getContextViewId(), fragment, tagName);
        int index = transaction.commit();
        Utils.modifyOpForStartFragmentAndDestroyCurrent(fragmentManager, fragment, useNewTransitionConfigWhenPop, transitionConfig);
        return index;
    }

    /**
     *
     * @param fragment target fragment to start
     * @return commit id
     *
     */
    public int startFragment(QMUIFragment fragment) {
        Log.i(TAG, "startFragment");
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.isStateSaved()) {
            QMUILog.d(TAG, "startFragment can not be invoked after onSaveInstanceState");
            return -1;
        }
        QMUIFragment.TransitionConfig transitionConfig = fragment.onFetchTransitionConfig();
        String tagName = fragment.getClass().getSimpleName();
        return fragmentManager.beginTransaction()
                .setCustomAnimations(transitionConfig.enter, transitionConfig.exit, transitionConfig.popenter, transitionConfig.popout)
                .replace(getContextViewId(), fragment, tagName)
                .setPrimaryNavigationFragment(null)
                .addToBackStack(tagName)
                .commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        QMUIFragment fragment = getCurrentQMUIFragment();
        if (fragment != null && !fragment.isInSwipeBack() && fragment.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        QMUIFragment fragment = getCurrentQMUIFragment();
        if (fragment != null && !fragment.isInSwipeBack() && fragment.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void popBackStack() {
        getOnBackPressedDispatcher().onBackPressed();
    }


    public static Intent intentOf(@NonNull Context context,
                                  @NonNull Class<? extends QMUIFragmentActivity> targetActivity,
                                  @NonNull Class<? extends QMUIFragment> firstFragment) {
        return intentOf(context, targetActivity, firstFragment, null);
    }

    /**
     * create a intent for a new QMUIFragmentActivity
     *
     * @param context        Generally it is activity
     * @param targetActivity target activity class
     * @param firstFragment  first fragment in target activity
     * @param fragmentArgs   args for first fragment
     * @return
     */
    public static Intent intentOf(@NonNull Context context,
                                  @NonNull Class<? extends QMUIFragmentActivity> targetActivity,
                                  @NonNull Class<? extends QMUIFragment> firstFragment,
                                  @Nullable Bundle fragmentArgs) {
        Intent intent = new Intent(context, targetActivity);
        FirstFragmentFinder finder = FirstFragmentFinders.getInstance().get(targetActivity);
        int dstId = FirstFragmentFinder.NO_ID;
        if (finder != null) {
            dstId = finder.getIdByFragmentClass(firstFragment);
        }
        intent.putExtra(QMUI_INTENT_DST_FRAGMENT, dstId);
        intent.putExtra(QMUI_INTENT_DST_FRAGMENT_NAME, firstFragment.getName());
        if (fragmentArgs != null) {
            intent.putExtra(QMUI_INTENT_FRAGMENT_ARG, fragmentArgs);
        }
        return intent;
    }

    public static Intent intentOf(@NonNull Context context,
                                  @NonNull Class<? extends QMUIFragmentActivity> targetActivity,
                                  @NonNull String firstFragmentClassName,
                                  @Nullable Bundle fragmentArgs) {
        Intent intent = new Intent(context, targetActivity);
        intent.putExtra(QMUI_INTENT_DST_FRAGMENT_NAME, firstFragmentClassName);
        if (fragmentArgs != null) {
            intent.putExtra(QMUI_INTENT_FRAGMENT_ARG, fragmentArgs);
        }
        return intent;
    }

    public static abstract class RootView extends FrameLayout {


        public RootView(Context context, int fragmentContainerId) {
            super(context);
            setId(R.id.qmui_activity_root_id);
        }

        public abstract FragmentContainerView getFragmentContainerView();
    }

    @Override
    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Exception ignore) {
            // 1. Under Android O, Activity#onBackPressed doesn't check FragmentManager's save state.
            // 2. IndexOutOfBoundsException caused by ViewGroup#removeView(View) in EmotionUI.
        }
    }

    @SuppressLint("ViewConstructor")
    public static class DefaultRootView extends RootView {
        private FragmentContainerView mFragmentContainerView;

        public DefaultRootView(Context context, int fragmentContainerId) {
            super(context, fragmentContainerId);
            mFragmentContainerView = new FragmentContainerView(context);
            mFragmentContainerView.setId(fragmentContainerId);
            addView(mFragmentContainerView, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }

        @Override
        public FragmentContainerView getFragmentContainerView() {
            return mFragmentContainerView;
        }
    }
}