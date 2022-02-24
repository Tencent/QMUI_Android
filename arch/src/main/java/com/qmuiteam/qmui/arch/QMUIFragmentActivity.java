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
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * the container activity for {@link QMUIFragment}.
 * Created by cgspine on 15/9/14.
 */
public abstract class QMUIFragmentActivity extends InnerBaseActivity implements QMUIFragmentContainerProvider {
    public static final String QMUI_INTENT_DST_FRAGMENT_NAME = "qmui_intent_dst_fragment_name";
    public static final String QMUI_INTENT_FRAGMENT_ARG = "qmui_intent_fragment_arg";
    public static final String QMUI_INTENT_FRAGMENT_LIST_ARG = "qmui_intent_fragment_list_arg";
    public static final String QMUI_MUTI_START_INDEX = "qmui_muti_start_index";
    private static final String TAG = "QMUIFragmentActivity";
    private RootView mRootView;
    private FragmentAutoInitResult mFragmentAutoInitResult = FragmentAutoInitResult.unHandled;
    private boolean isChildHandlePopBackRequested = false;

    @Override
    public int getContextViewId() {
        return R.id.qmui_activity_fragment_container_id;
    }

    @Override
    public FragmentManager getContainerFragmentManager() {
        return getSupportFragmentManager();
    }

    public RootView getRootView() {
        return mRootView;
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

            // 1. handle muti fragments
            mFragmentAutoInitResult = instantiationMutiFragment(intent);

            if (mFragmentAutoInitResult == FragmentAutoInitResult.unHandled) {
                try {
                    Class<? extends QMUIFragment> firstFragmentClass = null;
                    // 2. try get first fragment from fragment class name
                    String fragmentClassName = intent.getStringExtra(QMUI_INTENT_DST_FRAGMENT_NAME);
                    if (fragmentClassName != null && !fragmentClassName.isEmpty()) {
                        firstFragmentClass = (Class<? extends QMUIFragment>) Class.forName(fragmentClassName);
                    }

                    // 3. try get fragment from annotation @DefaultFirstFragment
                    if (firstFragmentClass == null) {
                        firstFragmentClass = getDefaultFirstFragment();
                    }

                    if (firstFragmentClass != null) {
                        QMUIFragment firstFragment = instantiationFragment(firstFragmentClass, intent.getBundleExtra(QMUI_INTENT_FRAGMENT_ARG));
                        if (firstFragment != null) {
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .add(getContextViewId(), firstFragment, firstFragment.getClass().getSimpleName())
                                    .addToBackStack(firstFragment.getClass().getSimpleName())
                                    .commit();
                            mFragmentAutoInitResult = FragmentAutoInitResult.success;
                        }
                    }
                } catch (Exception e) {
                    QMUILog.d(TAG, "fragment auto inited: " + e.getMessage());
                    mFragmentAutoInitResult = FragmentAutoInitResult.failed;
                }

            }
            Log.i(TAG, "the time it takes to inject first fragment from annotation is " + (System.currentTimeMillis() - start));
        }
    }

    protected FragmentAutoInitResult instantiationMutiFragment(Intent intent) {
        List<Bundle> fragmentBundles = intent.getParcelableArrayListExtra(QMUI_INTENT_FRAGMENT_LIST_ARG);
        if (fragmentBundles != null && fragmentBundles.size() > 0) {
            List<QMUIFragment> fragments = new ArrayList<>(fragmentBundles.size());
            for (Bundle bundle : fragmentBundles) {
                String fragmentClassName = bundle.getString(QMUI_INTENT_DST_FRAGMENT_NAME);
                try {
                    Class<? extends QMUIFragment> cls = (Class<? extends QMUIFragment>) Class.forName(fragmentClassName);
                    QMUIFragment fragment = instantiationFragment(cls, bundle.getBundle(QMUI_INTENT_FRAGMENT_ARG));
                    if (fragment == null) {
                        return FragmentAutoInitResult.failed;
                    }
                    fragments.add(fragment);
                } catch (ClassNotFoundException e) {
                    QMUILog.d(TAG, "Can not find " + fragmentClassName);
                }
            }
            if (fragments.size() > 0) {
                initMutiFragment(fragments);
                return FragmentAutoInitResult.success;
            }
        }
        return FragmentAutoInitResult.unHandled;
    }

    protected boolean initMutiFragment(QMUIFragment... fragments) {
        List<QMUIFragment> list = new ArrayList<>(fragments.length);
        Collections.addAll(list, fragments);
        return initMutiFragment(list);
    }

    protected boolean initMutiFragment(List<QMUIFragment> fragments) {
        if (fragments.size() == 0) {
            return false;
        }
        boolean disableSwipeBack = getIntent().getIntExtra(QMUIFragmentActivity.QMUI_MUTI_START_INDEX, 0) > 0;
        if (fragments.size() == 1) {
            QMUIFragment fragment = fragments.get(0);
            if (disableSwipeBack) {
                fragment.mDisableSwipeBackByMutiStarted = true;
            }
            String tagName = fragment.getClass().getSimpleName();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(getContextViewId(), fragment, tagName)
                    .addToBackStack(tagName)
                    .commit();
            return true;
        }
        ArrayList<FragmentTransaction> transactions = new ArrayList<>();
        FragmentManager fragmentManager = getSupportFragmentManager();
        for (int i = 0; i < fragments.size(); i++) {
            QMUIFragment fragment = fragments.get(i);
            if (disableSwipeBack) {
                fragment.mDisableSwipeBackByMutiStarted = true;
            }
            disableSwipeBack = true;
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            fragment.mDisableSwipeBackByMutiStarted = true;
            String tagName = fragment.getClass().getSimpleName();
            if (i == 0) {
                transaction.add(getContextViewId(), fragment, tagName);
            } else {
                QMUIFragment.TransitionConfig transitionConfig = fragment.onFetchTransitionConfig();
                transaction.setCustomAnimations(0, 0, transitionConfig.popenter, transitionConfig.popout);
                transaction.replace(getContextViewId(), fragment, tagName);
            }
            transaction.addToBackStack(tagName);
            transaction.setReorderingAllowed(true);
            transactions.add(transaction);
        }
        for (FragmentTransaction transaction : transactions) {
            transaction.commit();
        }
        return true;
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
    protected FragmentAutoInitResult isFragmentAutoInitResult() {
        return mFragmentAutoInitResult;
    }

    protected void setFragmentAutoInitResult(FragmentAutoInitResult fragmentAutoInitResult) {
        mFragmentAutoInitResult = fragmentAutoInitResult;
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

    protected QMUIFragment instantiationFragment(Class<? extends QMUIFragment> cls, Bundle args) {
        try {
            QMUIFragment fragment = cls.newInstance();
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
        if (fragmentManager.isDestroyed()) {
            return -1;
        }
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
        if (fragmentManager.isDestroyed()) {
            return -1;
        }
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


    public int startFragments(List<QMUIFragment> fragments) {
        Log.i(TAG, "startFragment");
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.isDestroyed()) {
            return -1;
        }
        if (fragmentManager.isStateSaved()) {
            QMUILog.d(TAG, "startFragment can not be invoked after onSaveInstanceState");
            return -1;
        }
        if (fragments.size() == 0) {
            return -1;
        }
        ArrayList<FragmentTransaction> transactions = new ArrayList<>();
        QMUIFragment.TransitionConfig lastTransitionConfig = fragments.get(fragments.size() - 1).onFetchTransitionConfig();
        for (QMUIFragment fragment : fragments) {
            FragmentTransaction transaction = fragmentManager.beginTransaction().setPrimaryNavigationFragment(null);
            QMUIFragment.TransitionConfig transitionConfig = fragment.onFetchTransitionConfig();
            fragment.mDisableSwipeBackByMutiStarted = true;
            String tagName = fragment.getClass().getSimpleName();
            transaction.setCustomAnimations(transitionConfig.enter, lastTransitionConfig.exit, transitionConfig.popenter, transitionConfig.popout);
            transaction.replace(getContextViewId(), fragment, tagName);
            transaction.addToBackStack(tagName);
            transactions.add(transaction);
            transaction.setReorderingAllowed(true);
        }
        for (FragmentTransaction transaction : transactions) {
            transaction.commit();
        }
        return 0;
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

    public enum FragmentAutoInitResult {success, failed, unHandled}
}