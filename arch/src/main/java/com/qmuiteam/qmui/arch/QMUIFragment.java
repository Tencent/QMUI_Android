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
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.QMUILog;
import com.qmuiteam.qmui.util.QMUIKeyboardHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.viewpager.widget.ViewPager;

import static com.qmuiteam.qmui.arch.SwipeBackLayout.EDGE_LEFT;

/**
 * With the use of {@link QMUIFragmentActivity}, {@link QMUIFragment} brings more features,
 * such as swipe back, transition config, and so on.
 * <p>
 * Created by cgspine on 15/9/14.
 */
public abstract class QMUIFragment extends Fragment implements QMUIFragmentLazyLifecycleOwner.Callback {
    private static final String SWIPE_BACK_VIEW = "swipe_back_view";
    private static final String TAG = QMUIFragment.class.getSimpleName();

    protected static final TransitionConfig SLIDE_TRANSITION_CONFIG = new TransitionConfig(
            R.anim.slide_in_right, R.anim.slide_out_left,
            R.anim.slide_in_left, R.anim.slide_out_right);

    protected static final TransitionConfig SCALE_TRANSITION_CONFIG = new TransitionConfig(
            R.anim.scale_enter, R.anim.slide_still,
            R.anim.slide_still, R.anim.scale_exit);


    public static final int RESULT_CANCELED = Activity.RESULT_CANCELED;
    public static final int RESULT_OK = Activity.RESULT_OK;
    public static final int RESULT_FIRST_USER = Activity.RESULT_FIRST_USER;

    public static final int ANIMATION_ENTER_STATUS_NOT_START = -1;
    public static final int ANIMATION_ENTER_STATUS_STARTED = 0;
    public static final int ANIMATION_ENTER_STATUS_END = 1;


    private static final int NO_REQUEST_CODE = 0;
    private int mSourceRequestCode = NO_REQUEST_CODE;
    private Intent mResultData = null;
    private int mResultCode = RESULT_CANCELED;
    private QMUIFragment mChildTargetFragment;


    private View mBaseView;
    private SwipeBackLayout mCacheSwipeBackLayout;
    private View mCacheRootView;
    private boolean isCreateForSwipeBack = false;
    private int mBackStackIndex = 0;
    private SwipeBackLayout.ListenerRemover mListenerRemover;
    private SwipeBackgroundView mSwipeBackgroundView;
    private boolean mIsInSwipeBack = false;

    private int mEnterAnimationStatus = ANIMATION_ENTER_STATUS_NOT_START;
    private boolean mCalled = true;
    private ArrayList<Runnable> mDelayRenderRunnableList = new ArrayList<>();
    private QMUIFragmentLazyLifecycleOwner mLazyViewLifecycleOwner;

    public QMUIFragment() {
        super();
    }

    public final QMUIFragmentActivity getBaseFragmentActivity() {
        return (QMUIFragmentActivity) getActivity();
    }

    public boolean isAttachedToActivity() {
        return !isRemoving() && mBaseView != null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBaseView = null;
    }

    protected void startFragmentAndDestroyCurrent(QMUIFragment fragment) {
        startFragmentAndDestroyCurrent(fragment, true);
    }

    /**
     * see {@link QMUIFragmentActivity#startFragmentAndDestroyCurrent(QMUIFragment, boolean)}
     *
     * @param fragment
     * @param useNewTransitionConfigWhenPop
     */
    protected void startFragmentAndDestroyCurrent(QMUIFragment fragment, boolean useNewTransitionConfigWhenPop) {
        if (getTargetFragment() != null) {
            // transfer target fragment
            fragment.setTargetFragment(getTargetFragment(), getTargetRequestCode());
            setTargetFragment(null, 0);
        }
        QMUIFragmentActivity baseFragmentActivity = this.getBaseFragmentActivity();
        if (baseFragmentActivity != null) {
            if (this.isAttachedToActivity()) {
                ViewCompat.setTranslationZ(mCacheSwipeBackLayout, --mBackStackIndex);
                baseFragmentActivity.startFragmentAndDestroyCurrent(fragment, useNewTransitionConfigWhenPop);
            } else {
                Log.e("QMUIFragment", "fragment not attached:" + this);
            }
        } else {
            Log.e("QMUIFragment", "startFragment null:" + this);
        }
    }

    protected void startFragment(QMUIFragment fragment) {
        QMUIFragmentActivity baseFragmentActivity = this.getBaseFragmentActivity();
        if (baseFragmentActivity != null) {
            if (this.isAttachedToActivity()) {
                baseFragmentActivity.startFragment(fragment);
            } else {
                Log.e("QMUIFragment", "fragment not attached:" + this);
            }
        } else {
            Log.e("QMUIFragment", "startFragment null:" + this);
        }
    }

    /**
     * simulate the behavior of startActivityForResult/onActivityResult:
     * 1. Jump fragment1 to fragment2 via startActivityForResult(fragment2, requestCode)
     * 2. Pass data from fragment2 to fragment1 via setFragmentResult(RESULT_OK, data)
     * 3. Get data in fragment1 through onFragmentResult(requestCode, resultCode, data)
     *
     * @param fragment    target fragment
     * @param requestCode request code
     */
    public void startFragmentForResult(QMUIFragment fragment, int requestCode) {
        if (requestCode == NO_REQUEST_CODE) {
            throw new RuntimeException("requestCode can not be " + NO_REQUEST_CODE);
        }
        QMUIFragmentActivity baseFragmentActivity = this.getBaseFragmentActivity();
        if (baseFragmentActivity != null) {
            FragmentManager targetFragmentManager = baseFragmentActivity.getSupportFragmentManager();
            Fragment topFragment = this;
            Fragment parent = this;
            while (parent != null) {
                topFragment = parent;
                if (parent.getFragmentManager() == targetFragmentManager) {
                    break;
                }
                parent = parent.getParentFragment();
            }
            mSourceRequestCode = requestCode;
            if (topFragment == this) {
                mChildTargetFragment = null;
                fragment.setTargetFragment(this, requestCode);
            } else if (topFragment.getFragmentManager() == targetFragmentManager) {
                QMUIFragment qmuiFragment = (QMUIFragment) topFragment;
                qmuiFragment.mSourceRequestCode = requestCode;
                qmuiFragment.mChildTargetFragment = this;
                fragment.setTargetFragment(qmuiFragment, requestCode);
            } else {
                throw new RuntimeException("fragment manager not matched");
            }
            startFragment(fragment);
        }
    }


    public void setFragmentResult(int resultCode, Intent data) {
        int targetRequestCode = getTargetRequestCode();
        if (targetRequestCode == 0) {
            QMUILog.w(TAG, "call setFragmentResult, but not requestCode exists");
            return;
        }
        Fragment fragment = getTargetFragment();
        if (!(fragment instanceof QMUIFragment)) {
            return;
        }
        QMUIFragment targetFragment = (QMUIFragment) fragment;

        if (targetFragment.mSourceRequestCode == targetRequestCode) {
            if (targetFragment.mChildTargetFragment != null) {
                targetFragment = targetFragment.mChildTargetFragment;
            }
            targetFragment.mResultCode = resultCode;
            targetFragment.mResultData = data;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            int backStackEntryCount = fragmentManager.getBackStackEntryCount();
            for (int i = backStackEntryCount - 1; i >= 0; i--) {
                FragmentManager.BackStackEntry entry = fragmentManager.getBackStackEntryAt(i);
                if (getClass().getSimpleName().equals(entry.getName())) {
                    mBackStackIndex = i;
                    break;
                }
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLazyViewLifecycleOwner = new QMUIFragmentLazyLifecycleOwner(this);
        mLazyViewLifecycleOwner.setViewVisible(getUserVisibleHint());
        getViewLifecycleOwner().getLifecycle().addObserver(mLazyViewLifecycleOwner);
    }

    @Override
    public void onStart() {
        super.onStart();
        int requestCode = mSourceRequestCode;
        int resultCode = mResultCode;
        Intent data = mResultData;
        QMUIFragment childTargetFragment = mChildTargetFragment;

        mSourceRequestCode = NO_REQUEST_CODE;
        mResultCode = RESULT_CANCELED;
        mResultData = null;
        mChildTargetFragment = null;

        if (requestCode != NO_REQUEST_CODE) {
            if (childTargetFragment == null) {
                // only handle the result when there is not child target.
                onFragmentResult(requestCode, resultCode, data);
            }
        }
    }

    private SwipeBackLayout newSwipeBackLayout() {
        View rootView = mCacheRootView;
        if (rootView == null) {
            rootView = onCreateView();
            mCacheRootView = rootView;
        } else {
            if (rootView.getParent() != null) {
                ((ViewGroup) rootView.getParent()).removeView(rootView);
            }
        }
        if (translucentFull()) {
            rootView.setFitsSystemWindows(false);
        } else {
            rootView.setFitsSystemWindows(true);
        }
        final SwipeBackLayout swipeBackLayout = SwipeBackLayout.wrap(rootView, dragBackEdge(),
                new SwipeBackLayout.Callback() {
                    @Override
                    public boolean canSwipeBack() {
                        if (mEnterAnimationStatus != ANIMATION_ENTER_STATUS_END) {
                            return false;
                        }
                        if (!canDragBack()) {
                            return false;
                        }

                        if (getParentFragment() != null) {
                            return false;
                        }

                        View view = getView();
                        if (view == null) {
                            return false;
                        }

                        // if the Fragment is in ViewPager, then stop drag back
                        ViewParent parent = view.getParent();
                        while (parent != null) {
                            if (parent instanceof ViewPager) {
                                return false;
                            }
                            parent = parent.getParent();
                        }

                        FragmentManager fragmentManager = getFragmentManager();
                        if (fragmentManager == null || fragmentManager.getBackStackEntryCount() <= 1) {
                            return QMUISwipeBackActivityManager.getInstance().canSwipeBack();
                        }
                        return true;
                    }
                });
        mListenerRemover = swipeBackLayout.addSwipeListener(mSwipeListener);
        return swipeBackLayout;
    }

    private SwipeBackLayout.SwipeListener mSwipeListener = new SwipeBackLayout.SwipeListener() {

        private QMUIFragment mModifiedFragment = null;

        @Override
        public void onScrollStateChange(int state, float scrollPercent) {
            Log.i(TAG, "SwipeListener:onScrollStateChange: state = " + state + " ;scrollPercent = " + scrollPercent);
            ViewGroup container = getBaseFragmentActivity().getFragmentContainer();
            mIsInSwipeBack = state != SwipeBackLayout.STATE_IDLE;
            if (state == SwipeBackLayout.STATE_IDLE) {
                if (mSwipeBackgroundView != null) {
                    if (scrollPercent <= 0.0F) {
                        mSwipeBackgroundView.unBind();
                        mSwipeBackgroundView = null;
                    } else if (scrollPercent >= 1.0F) {
                        // unbind mSwipeBackgroundView util onDestroy
                        if (getActivity() != null) {
                            getActivity().finish();
                            int exitAnim = mSwipeBackgroundView.hasChildWindow() ?
                                    R.anim.swipe_back_exit_still : R.anim.swipe_back_exit;
                            getActivity().overridePendingTransition(R.anim.swipe_back_enter, exitAnim);
                        }
                    }
                    return;
                }
                if (scrollPercent <= 0.0F) {
                    handleSwipeBackCancelOrFinished(container);
                } else if (scrollPercent >= 1.0F) {
                    handleSwipeBackCancelOrFinished(container);
                    FragmentManager fragmentManager = getFragmentManager();
                    Utils.findAndModifyOpInBackStackRecord(fragmentManager, -1, new Utils.OpHandler() {
                        @Override
                        public boolean handle(Object op) {
                            Field cmdField;
                            try {
                                cmdField = op.getClass().getDeclaredField("cmd");
                                cmdField.setAccessible(true);
                                int cmd = (int) cmdField.get(op);
                                if (cmd == 1) {
                                    Field popEnterAnimField = op.getClass().getDeclaredField("popExitAnim");
                                    popEnterAnimField.setAccessible(true);
                                    popEnterAnimField.set(op, 0);
                                } else if (cmd == 3) {
                                    Field popExitAnimField = op.getClass().getDeclaredField("popEnterAnim");
                                    popExitAnimField.setAccessible(true);
                                    popExitAnimField.set(op, 0);
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
                            return false;
                        }

                        @Override
                        public String newTagName() {
                            return null;
                        }
                    });
                    popBackStack();
                }
            }
        }

        @Override
        public void onScroll(int edgeFlag, float scrollPercent) {
            scrollPercent = Math.max(0f, Math.min(1f, scrollPercent));
            int targetOffset = (int) (Math.abs(backViewInitOffset()) * (1 - scrollPercent));
            ViewGroup container = getBaseFragmentActivity().getFragmentContainer();
            int childCount = container.getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                View view = container.getChildAt(i);
                Object tag = view.getTag(R.id.qmui_arch_swipe_layout_in_back);
                if (SWIPE_BACK_VIEW.equals(tag)) {
                    SwipeBackLayout.offsetInScroll(view, edgeFlag, targetOffset);
                }
            }
            if (mSwipeBackgroundView != null) {
                SwipeBackLayout.offsetInScroll(mSwipeBackgroundView, edgeFlag, targetOffset);
            }
        }

        @SuppressLint("PrivateApi")
        @Override
        public void onEdgeTouch(int edgeFlag) {
            Log.i(TAG, "SwipeListener:onEdgeTouch: edgeFlag = " + edgeFlag);
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager == null) {
                return;
            }
            QMUIKeyboardHelper.hideKeyboard(mBaseView);
            onDragStart();
            int backStackCount = fragmentManager.getBackStackEntryCount();
            if (backStackCount > 1) {
                try {
                    FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(backStackCount - 1);

                    Field opsField = backStackEntry.getClass().getDeclaredField("mOps");
                    opsField.setAccessible(true);
                    Object opsObj = opsField.get(backStackEntry);
                    if (opsObj instanceof List<?>) {
                        List<?> ops = (List<?>) opsObj;
                        for (Object op : ops) {
                            Field cmdField = op.getClass().getDeclaredField("cmd");
                            cmdField.setAccessible(true);
                            int cmd = (int) cmdField.get(op);
                            if (cmd == 3) {
                                Field popEnterAnimField = op.getClass().getDeclaredField("popEnterAnim");
                                popEnterAnimField.setAccessible(true);
                                popEnterAnimField.set(op, 0);

                                Field fragmentField = op.getClass().getDeclaredField("fragment");
                                fragmentField.setAccessible(true);
                                Object fragmentObject = fragmentField.get(op);
                                if (fragmentObject instanceof QMUIFragment) {
                                    mModifiedFragment = (QMUIFragment) fragmentObject;
                                    ViewGroup container = getBaseFragmentActivity().getFragmentContainer();
                                    mModifiedFragment.isCreateForSwipeBack = true;
                                    View baseView = mModifiedFragment.onCreateView(LayoutInflater.from(getContext()), container, null);
                                    mModifiedFragment.isCreateForSwipeBack = false;
                                    if (baseView != null) {
                                        addViewInSwipeBack(container, baseView, 0);
                                        handleChildFragmentListWhenSwipeBackStart(baseView);
                                        SwipeBackLayout.offsetInEdgeTouch(baseView, edgeFlag,
                                                Math.abs(backViewInitOffset()));
                                    }
                                }
                            }
                        }
                    }


                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else if (getParentFragment() == null) {
                Activity currentActivity = getActivity();
                if (currentActivity != null) {
                    ViewGroup decorView = (ViewGroup) currentActivity.getWindow().getDecorView();
                    Activity prevActivity = QMUISwipeBackActivityManager.getInstance()
                            .getPenultimateActivity(currentActivity);
                    if (decorView.getChildAt(0) instanceof SwipeBackgroundView) {
                        mSwipeBackgroundView = (SwipeBackgroundView) decorView.getChildAt(0);
                    } else {
                        mSwipeBackgroundView = new SwipeBackgroundView(getContext());
                        decorView.addView(mSwipeBackgroundView, 0, new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    }
                    mSwipeBackgroundView.bind(prevActivity, currentActivity, restoreSubWindowWhenDragBack());
                    SwipeBackLayout.offsetInEdgeTouch(mSwipeBackgroundView, edgeFlag,
                            Math.abs(backViewInitOffset()));
                }
            }
        }

        @Override
        public void onScrollOverThreshold() {
            Log.i(TAG, "SwipeListener:onEdgeTouch:onScrollOverThreshold");
        }

        private void addViewInSwipeBack(ViewGroup parent, View child) {
            addViewInSwipeBack(parent, child, -1);
        }

        private void addViewInSwipeBack(ViewGroup parent, View child, int index) {
            if (parent != null && child != null) {
                child.setTag(R.id.qmui_arch_swipe_layout_in_back, SWIPE_BACK_VIEW);
                parent.addView(child, index);
            }
        }

        private void removeViewInSwipeBack(ViewGroup parent, Function<View, Void> onRemove) {
            if (parent != null) {
                int childCount = parent.getChildCount();
                for (int i = childCount - 1; i >= 0; i--) {
                    View view = parent.getChildAt(i);
                    Object tag = view.getTag(R.id.qmui_arch_swipe_layout_in_back);
                    if (SWIPE_BACK_VIEW.equals(tag)) {
                        if (onRemove != null) {
                            onRemove.apply(view);
                        }
                        parent.removeView(view);
                    }
                }
            }
        }


        private void handleChildFragmentListWhenSwipeBackStart(View baseView) throws
                NoSuchFieldException, IllegalAccessException {
            // handle issue #235
            if (baseView instanceof ViewGroup) {
                ViewGroup childMainContainer = (ViewGroup) baseView;
                FragmentManager childFragmentManager = mModifiedFragment.getChildFragmentManager();
                List<Fragment> childFragmentList = childFragmentManager.getFragments();
                int childContainerId = 0;
                ViewGroup childContainer = null;
                for (Fragment fragment : childFragmentList) {
                    if (fragment instanceof QMUIFragment) {
                        QMUIFragment qmuiFragment = (QMUIFragment) fragment;
                        Field containerIdField = Fragment.class.getDeclaredField("mContainerId");
                        containerIdField.setAccessible(true);
                        int containerId = containerIdField.getInt(qmuiFragment);
                        if (containerId != 0) {
                            if (childContainerId != containerId) {
                                childContainerId = containerId;
                                childContainer = childMainContainer.findViewById(containerId);
                            }
                            if (childContainer != null) {
                                qmuiFragment.isCreateForSwipeBack = true;
                                View childView = fragment.onCreateView(
                                        LayoutInflater.from(childContainer.getContext()), childContainer, null);
                                qmuiFragment.isCreateForSwipeBack = false;
                                addViewInSwipeBack(childContainer, childView);
                            }
                        }
                    }
                }
            }
        }


        private void handleSwipeBackCancelOrFinished(ViewGroup container) {
            removeViewInSwipeBack(container, new Function<View, Void>() {
                @Override
                public Void apply(View input) {
                    if (mModifiedFragment == null) {
                        return null;
                    }
                    if (input instanceof ViewGroup) {
                        ViewGroup childMainContainer = (ViewGroup) input;
                        FragmentManager childFragmentManager = mModifiedFragment.getChildFragmentManager();
                        List<Fragment> childFragmentList = childFragmentManager.getFragments();
                        int childContainerId = 0;
                        try {
                            for (Fragment fragment : childFragmentList) {
                                if (fragment instanceof QMUIFragment) {
                                    QMUIFragment qmuiFragment = (QMUIFragment) fragment;
                                    Field containerIdField = Fragment.class.getDeclaredField("mContainerId");
                                    containerIdField.setAccessible(true);
                                    int containerId = containerIdField.getInt(qmuiFragment);
                                    if (containerId != 0 && childContainerId != containerId) {
                                        childContainerId = containerId;
                                        ViewGroup childContainer = childMainContainer.findViewById(containerId);
                                        removeViewInSwipeBack(childContainer, null);
                                    }
                                }
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        }

                    }
                    return null;
                }
            });
            mModifiedFragment = null;
        }
    };

    private boolean canNotUseCacheViewInCreateView() {
        return mCacheSwipeBackLayout.getParent() != null || ViewCompat.isAttachedToWindow(mCacheSwipeBackLayout);
    }

    public boolean isInSwipeBack() {
        return mIsInSwipeBack;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SwipeBackLayout swipeBackLayout;
        if (mCacheSwipeBackLayout == null) {
            swipeBackLayout = newSwipeBackLayout();
            mCacheSwipeBackLayout = swipeBackLayout;
        } else {
            if (canNotUseCacheViewInCreateView()) {
                // try removeView first
                container.removeView(mCacheSwipeBackLayout);
            }

            if (canNotUseCacheViewInCreateView()) {
                // give up!!!
                Log.i(TAG, "can not use cache swipeBackLayout, this may happen " +
                        "if invoke popBackStack duration fragment transition");
                mCacheSwipeBackLayout.clearSwipeListeners();
                swipeBackLayout = newSwipeBackLayout();
                mCacheSwipeBackLayout = swipeBackLayout;
            } else {
                swipeBackLayout = mCacheSwipeBackLayout;
            }
        }


        if (!isCreateForSwipeBack) {
            mBaseView = swipeBackLayout.getContentView();
            swipeBackLayout.setTag(R.id.qmui_arch_swipe_layout_in_back, null);
        }

        ViewCompat.setTranslationZ(swipeBackLayout, mBackStackIndex);
        Log.i(TAG, getClass().getSimpleName() + " onCreateView: mBackStackIndex = " + mBackStackIndex);

        swipeBackLayout.setFitsSystemWindows(false);

        if (getActivity() != null) {
            QMUIViewHelper.requestApplyInsets(getActivity().getWindow());
        }

        return swipeBackLayout;
    }

    protected void onBackPressed() {
        popBackStack();
    }

    protected void popBackStack() {
        if (mEnterAnimationStatus != ANIMATION_ENTER_STATUS_END) {
            return;
        }
        getBaseFragmentActivity().popBackStack();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (!enter && getParentFragment() != null && getParentFragment().isRemoving()) {
            // This is a workaround for the bug where child fragments disappear when
            // the parent is removed (as all children are first removed from the parent)
            // See https://code.google.com/p/android/issues/detail?id=55228
            Animation doNothingAnim = new AlphaAnimation(1, 1);
            int duration = getResources().getInteger(R.integer.qmui_anim_duration);
            doNothingAnim.setDuration(duration);
            return doNothingAnim;
        }
        Animation animation = null;
        if (enter) {
            try {
                animation = AnimationUtils.loadAnimation(getContext(), nextAnim);

            } catch (Resources.NotFoundException ignored) {

            } catch (RuntimeException ignored) {

            }
            if (animation != null) {
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        onEnterAnimationStart(animation);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        checkAndCallOnEnterAnimationEnd(animation);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            } else {
                onEnterAnimationStart(null);
                checkAndCallOnEnterAnimationEnd(null);
            }
        }
        return animation;
    }


    private void checkAndCallOnEnterAnimationEnd(@Nullable Animation animation) {
        mCalled = false;
        onEnterAnimationEnd(animation);
        if (!mCalled) {
            throw new RuntimeException("QMUIFragment " + this + " did not call through to super.onEnterAnimationEnd(Animation)");
        }
    }


    /**
     * onCreateView
     */
    protected abstract View onCreateView();

    /**
     * Will be performed in onStart
     *
     * @param requestCode request code
     * @param resultCode  result code
     * @param data        extra data
     */
    protected void onFragmentResult(int requestCode, int resultCode, Intent data) {

    }

    /**
     * disable or enable drag back
     *
     * @return
     */
    protected boolean canDragBack() {
        return true;
    }

    /**
     * if enable drag back,
     *
     * @return
     */
    protected int backViewInitOffset() {
        return 0;
    }

    /**
     * called when drag back started.
     */
    protected void onDragStart() {

    }

    protected int dragBackEdge() {
        return EDGE_LEFT;
    }

    /**
     * the action will be performed before the start of the enter animation start or after the
     * enter animation is finished
     *
     * @param runnable the action to perform
     */
    public void runAfterAnimation(Runnable runnable) {
        runAfterAnimation(runnable, false);
    }

    /**
     * When data is rendered duration the transition animation, it will cause a choppy. this method
     * will promise the data is rendered before or after transition animation
     *
     * @param runnable the action to perform
     * @param onlyEnd  if true, the action is only performed after the enter animation is finished,
     *                 otherwise it can be performed before the start of the enter animation start
     *                 or after the enter animation is finished.
     */
    public void runAfterAnimation(Runnable runnable, boolean onlyEnd) {
        Utils.assertInMainThread();
        boolean ok = onlyEnd ? mEnterAnimationStatus == ANIMATION_ENTER_STATUS_END :
                mEnterAnimationStatus != ANIMATION_ENTER_STATUS_STARTED;
        if (ok) {
            runnable.run();
        } else {
            mDelayRenderRunnableList.add(runnable);
        }
    }

    protected void onEnterAnimationStart(@Nullable Animation animation) {
        mEnterAnimationStatus = ANIMATION_ENTER_STATUS_STARTED;
    }

    protected void onEnterAnimationEnd(@Nullable Animation animation) {
        if (mCalled) {
            throw new IllegalAccessError("don't call #onEnterAnimationEnd() directly");
        }
        mCalled = true;
        if (mDelayRenderRunnableList.size() > 0) {
            for (int i = 0; i < mDelayRenderRunnableList.size(); i++) {
                mDelayRenderRunnableList.get(i).run();
            }
            mDelayRenderRunnableList.clear();
        }
        mEnterAnimationStatus = ANIMATION_ENTER_STATUS_END;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mListenerRemover != null) {
            mListenerRemover.remove();
        }
        if (mSwipeBackgroundView != null) {
            mSwipeBackgroundView.unBind();
            mSwipeBackgroundView = null;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        notifyFragmentVisibleToUserChanged(isParentVisibleToUser() && isVisibleToUser);
    }

    @Override
    public boolean isVisibleToUser() {
        return getUserVisibleHint() && isParentVisibleToUser();
    }

    /**
     * @return true if parentFragments is visible to user
     */
    private boolean isParentVisibleToUser() {
        Fragment parentFragment = getParentFragment();
        while (parentFragment != null) {
            if (!parentFragment.getUserVisibleHint()) {
                return false;
            }
            parentFragment = parentFragment.getParentFragment();
        }
        return true;
    }

    private void notifyFragmentVisibleToUserChanged(boolean isVisibleToUser) {
        if (mLazyViewLifecycleOwner != null) {
            mLazyViewLifecycleOwner.setViewVisible(isVisibleToUser);
        }
        if (isAdded()) {
            List<Fragment> childFragments = getChildFragmentManager().getFragments();
            for (Fragment fragment : childFragments) {
                if (fragment instanceof QMUIFragment) {
                    ((QMUIFragment) fragment).notifyFragmentVisibleToUserChanged(
                            isVisibleToUser && fragment.getUserVisibleHint());
                }
            }
        }
    }

    public LifecycleOwner getLazyViewLifecycleOwner() {
        if (mLazyViewLifecycleOwner == null) {
            throw new IllegalStateException("Can't access the QMUIFragment View's LifecycleOwner when "
                    + "getView() is null i.e., before onViewCreated() or after onDestroyView()");
        }
        return mLazyViewLifecycleOwner;
    }

    /**
     * Immersive processing
     *
     * @return if true, the area under status bar belongs to content; otherwise it belongs to padding
     */
    protected boolean translucentFull() {
        return false;
    }

    /**
     * When finishing to pop back last fragment, let activity have a chance to do something
     * like start a new fragment
     *
     * @return QMUIFragment to start a new fragment or Intent to start a new Activity
     */
    @SuppressWarnings("SameReturnValue")
    public Object onLastFragmentFinish() {
        return null;
    }

    /**
     * restore sub window(e.g dialog) when drag back to previous activity
     *
     * @return
     */
    protected boolean restoreSubWindowWhenDragBack() {
        return true;
    }

    /**
     * Fragment Transition Controller
     */
    public TransitionConfig onFetchTransitionConfig() {
        return SLIDE_TRANSITION_CONFIG;
    }


    public static final class TransitionConfig {
        public final int enter;
        public final int exit;
        public final int popenter;
        public final int popout;

        public TransitionConfig(int enter, int popout) {
            this(enter, 0, 0, popout);
        }

        public TransitionConfig(int enter, int exit, int popenter, int popout) {
            this.enter = enter;
            this.exit = exit;
            this.popenter = popenter;
            this.popout = popout;
        }
    }
}

