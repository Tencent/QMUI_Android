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

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.QMUILog;
import com.qmuiteam.qmui.arch.annotation.DefaultFirstFragment;
import com.qmuiteam.qmui.arch.first.FirstFragmentFinder;
import com.qmuiteam.qmui.arch.first.FirstFragmentFinders;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUIWindowInsetLayout;

import java.lang.reflect.Field;

/**
 * the container activity for {@link QMUIFragment}.
 * Created by cgspine on 15/9/14.
 */
public abstract class QMUIFragmentActivity extends InnerBaseActivity {
    public static final String QMUI_INTENT_DST_FRAGMENT = "qmui_intent_dst_fragment";
    public static final String QMUI_INTENT_FRAGMENT_ARG = "qmui_intent_fragment_arg";
    private static final String TAG = "QMUIFragmentActivity";
    private RootView mFragmentContainer;
    private boolean mIsFirstFragmentAddedByAnnotation = false;

    @SuppressWarnings("SameReturnValue")
    protected abstract int getContextViewId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        mFragmentContainer = new RootView(this);
        mFragmentContainer.setId(getContextViewId());
        setContentView(mFragmentContainer);
        mIsFirstFragmentAddedByAnnotation = false;
        if (savedInstanceState == null) {
            long start = System.currentTimeMillis();
            FirstFragmentFinder finder = FirstFragmentFinders.getInstance().get(getClass());
            Intent intent = getIntent();
            Class<? extends QMUIFragment> firstFragmentClass = null;
            if (finder != null) {
                int dstFragment = intent.getIntExtra(QMUI_INTENT_DST_FRAGMENT, -1);
                firstFragmentClass = finder.getFragmentClassById(dstFragment);
            }

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
                    mIsFirstFragmentAddedByAnnotation = true;
                }
            }
            Log.i(TAG, "the time it takes to inject first fragment from annotation is " + (System.currentTimeMillis() - start));
        }
    }

    /**
     * used for subclasses to see if the parent class initializes the first fragment。
     * it must be called after super.onCreate in subclasses.
     *
     * @return true if first fragment is initialized.
     */
    protected boolean isFirstFragmentAddedByAnnotation() {
        return mIsFirstFragmentAddedByAnnotation;
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

    public FrameLayout getFragmentContainer() {
        return mFragmentContainer;
    }

    @Override
    public void onBackPressed() {
        QMUIFragment fragment = getCurrentFragment();
        if (fragment != null && !fragment.isInSwipeBack()) {
            fragment.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        QMUIFragment fragment = getCurrentFragment();
        if (fragment != null && !fragment.isInSwipeBack() && fragment.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        QMUIFragment fragment = getCurrentFragment();
        if (fragment != null && !fragment.isInSwipeBack() && fragment.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * get the current Fragment.
     */
    public QMUIFragment getCurrentFragment() {
        return (QMUIFragment) getSupportFragmentManager().findFragmentById(getContextViewId());
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
    public int startFragmentAndDestroyCurrent(final QMUIFragment fragment, final boolean useNewTransitionConfigWhenPop) {
        final QMUIFragment.TransitionConfig transitionConfig = fragment.onFetchTransitionConfig();
        String tagName = fragment.getClass().getSimpleName();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(transitionConfig.enter, transitionConfig.exit,
                        transitionConfig.popenter, transitionConfig.popout)
                .replace(getContextViewId(), fragment, tagName);
        int index = transaction.commit();
        Utils.findAndModifyOpInBackStackRecord(fragmentManager, -1, new Utils.OpHandler() {
            @Override
            public boolean handle(Object op) {
                Field cmdField = null;
                try {
                    cmdField = Utils.getOpCmdField(op);
                    cmdField.setAccessible(true);
                    int cmd = (int) cmdField.get(op);
                    if (cmd == 1) {
                        if (useNewTransitionConfigWhenPop) {
                            Field popEnterAnimField = Utils.getOpPopEnterAnimField(op);
                            popEnterAnimField.setAccessible(true);
                            popEnterAnimField.set(op, transitionConfig.popenter);

                            Field popExitAnimField = Utils.getOpPopExitAnimField(op);
                            popExitAnimField.setAccessible(true);
                            popExitAnimField.set(op, transitionConfig.popout);
                        }

                        Field oldFragmentField = Utils.getOpFragmentField(op);
                        oldFragmentField.setAccessible(true);
                        Object fragmentObj = oldFragmentField.get(op);
                        oldFragmentField.set(op, fragment);
                        Field backStackNestField = Fragment.class.getDeclaredField("mBackStackNesting");
                        backStackNestField.setAccessible(true);
                        int oldFragmentBackStackNest = (int) backStackNestField.get(fragmentObj);
                        backStackNestField.set(fragment, oldFragmentBackStackNest);
                        backStackNestField.set(fragmentObj, --oldFragmentBackStackNest);
                        return true;
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean needReNameTag() {
                return true;
            }

            @Override
            public String newTagName() {
                return fragment.getClass().getSimpleName();
            }
        });
        return index;
    }

    public int startFragment(QMUIFragment fragment) {
        Log.i(TAG, "startFragment");
        QMUIFragment.TransitionConfig transitionConfig = fragment.onFetchTransitionConfig();
        String tagName = fragment.getClass().getSimpleName();
        return getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(transitionConfig.enter, transitionConfig.exit, transitionConfig.popenter, transitionConfig.popout)
                .replace(getContextViewId(), fragment, tagName)
                .addToBackStack(tagName)
                .commit();
    }

    /**
     * Exit the current Fragment。
     */
    public void popBackStack() {
        Log.i(TAG, "popBackStack: getSupportFragmentManager().getBackStackEntryCount() = " + getSupportFragmentManager().getBackStackEntryCount());
        if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            QMUIFragment fragment = getCurrentFragment();
            if (fragment == null || QMUISwipeBackActivityManager.getInstance().canSwipeBack()) {
                finish();
                return;
            }
            QMUIFragment.TransitionConfig transitionConfig = fragment.onFetchTransitionConfig();
            Object toExec = fragment.onLastFragmentFinish();
            if (toExec != null) {
                if (toExec instanceof QMUIFragment) {
                    QMUIFragment mFragment = (QMUIFragment) toExec;
                    startFragmentAndDestroyCurrent(mFragment, false);
                } else if (toExec instanceof Intent) {
                    Intent intent = (Intent) toExec;
                    startActivity(intent);
                    overridePendingTransition(transitionConfig.popenter, transitionConfig.popout);
                    finish();
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
     * pop back to a clazz type fragment
     * <p>
     * Assuming there is a back stack: Home -> List -> Detail. Perform popBackStack(Home.class),
     * Home is the current fragment
     * <p>
     * if the clazz type fragment doest not exist in back stack, this method is Equivalent
     * to popBackStack()
     *
     * @param clazz the type of fragment
     */
    public void popBackStack(Class<? extends QMUIFragment> clazz) {
        getSupportFragmentManager().popBackStack(clazz.getSimpleName(), 0);
    }

    /**
     * pop back to a non-clazz type Fragment
     *
     * @param clazz the type of fragment
     */
    public void popBackStackInclusive(Class<? extends QMUIFragment> clazz) {
        getSupportFragmentManager().popBackStack(clazz.getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
        if (dstId == FirstFragmentFinder.NO_ID) {
            String fragmentName = firstFragment.getName();
            throw new RuntimeException("Can not find ID for " + fragmentName +
                    "; You must add annotation MayFirstFragment which include " + targetActivity.getName() +
                    " in " + fragmentName + " .");
        }
        intent.putExtra(QMUI_INTENT_DST_FRAGMENT, dstId);
        if (fragmentArgs != null) {
            intent.putExtra(QMUI_INTENT_FRAGMENT_ARG, fragmentArgs);
        }
        return intent;
    }


    private static class RootView extends QMUIWindowInsetLayout {

        public RootView(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            for (int i = 0; i < getChildCount(); i++) {
                SwipeBackLayout.updateLayoutInSwipeBack(getChildAt(i));
            }
        }

        @Override
        public boolean applySystemWindowInsets21(Object insets) {
            super.applySystemWindowInsets21(insets);
            return true;
        }

        @Override
        public boolean applySystemWindowInsets19(Rect insets) {
            super.applySystemWindowInsets19(insets);
            return true;
        }
    }
}