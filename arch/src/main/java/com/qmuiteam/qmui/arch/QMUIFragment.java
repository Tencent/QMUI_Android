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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.qmuiteam.qmui.QMUIConfig;
import com.qmuiteam.qmui.QMUILog;
import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord;
import com.qmuiteam.qmui.arch.effect.Effect;
import com.qmuiteam.qmui.arch.effect.FragmentResultEffect;
import com.qmuiteam.qmui.arch.effect.QMUIFragmentEffectHandler;
import com.qmuiteam.qmui.arch.effect.QMUIFragmentEffectRegistration;
import com.qmuiteam.qmui.arch.effect.QMUIFragmentEffectRegistry;
import com.qmuiteam.qmui.arch.effect.QMUIFragmentResultEffectHandler;
import com.qmuiteam.qmui.arch.record.LatestVisitArgumentCollector;
import com.qmuiteam.qmui.arch.record.RecordArgumentEditor;
import com.qmuiteam.qmui.arch.scheme.FragmentSchemeRefreshable;
import com.qmuiteam.qmui.arch.scheme.QMUISchemeHandler;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIKeyboardHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.qmuiteam.qmui.arch.SwipeBackLayout.DRAG_DIRECTION_BOTTOM_TO_TOP;
import static com.qmuiteam.qmui.arch.SwipeBackLayout.DRAG_DIRECTION_LEFT_TO_RIGHT;
import static com.qmuiteam.qmui.arch.SwipeBackLayout.DRAG_DIRECTION_NONE;
import static com.qmuiteam.qmui.arch.SwipeBackLayout.DRAG_DIRECTION_RIGHT_TO_LEFT;
import static com.qmuiteam.qmui.arch.SwipeBackLayout.DRAG_DIRECTION_TOP_TO_BOTTOM;
import static com.qmuiteam.qmui.arch.SwipeBackLayout.EDGE_BOTTOM;
import static com.qmuiteam.qmui.arch.SwipeBackLayout.EDGE_LEFT;
import static com.qmuiteam.qmui.arch.SwipeBackLayout.EDGE_RIGHT;
import static com.qmuiteam.qmui.arch.SwipeBackLayout.EDGE_TOP;

/**
 * With the use of {@link QMUIFragmentActivity}, {@link QMUIFragment} brings more features,
 * such as swipe back, transition config, and so on.
 * <p>
 * Created by cgspine on 15/9/14.
 */
public abstract class QMUIFragment extends Fragment implements
        QMUIFragmentLazyLifecycleOwner.Callback,
        LatestVisitArgumentCollector,
        FragmentSchemeRefreshable,
        SwipeBackLayout.OnKeyboardInsetHandler{
    static final String SWIPE_BACK_VIEW = "swipe_back_view";
    private static final String TAG = QMUIFragment.class.getSimpleName();

    public static final TransitionConfig SLIDE_TRANSITION_CONFIG = new TransitionConfig(
            R.anim.slide_in_right, R.anim.slide_out_left,
            R.anim.slide_in_left, R.anim.slide_out_right);

    public static final TransitionConfig SCALE_TRANSITION_CONFIG = new TransitionConfig(
            R.anim.scale_enter, R.anim.slide_still,
            R.anim.slide_still, R.anim.scale_exit);


    public static final int RESULT_CANCELED = Activity.RESULT_CANCELED;
    public static final int RESULT_OK = Activity.RESULT_OK;
    public static final int RESULT_FIRST_USER = Activity.RESULT_FIRST_USER;

    public static final int ANIMATION_ENTER_STATUS_NOT_START = -1;
    public static final int ANIMATION_ENTER_STATUS_STARTED = 0;
    public static final int ANIMATION_ENTER_STATUS_END = 1;
    private static boolean sPopBackWhenSwipeFinished = false;

    private static final int NO_REQUEST_CODE = 0;
    private static final AtomicInteger sNextRc = new AtomicInteger(1);
    private static int sLatestVisitFragmentUUid = -1;
    private int mSourceRequestCode = NO_REQUEST_CODE;
    private final int mUUid = sNextRc.getAndIncrement();
    private int mTargetFragmentUUid = -1;
    private int mTargetRequestCode = NO_REQUEST_CODE;

    private View mBaseView;
    private SwipeBackLayout mCacheSwipeBackLayout;
    private View mCacheRootView;
    private boolean isCreateForSwipeBack = false;
    private SwipeBackLayout.ListenerRemover mListenerRemover;
    private SwipeBackgroundView mSwipeBackgroundView;
    private boolean mIsInSwipeBack = false;

    private int mEnterAnimationStatus = ANIMATION_ENTER_STATUS_NOT_START;
    private MutableLiveData<Boolean> isInEnterAnimationLiveData = new MutableLiveData<>(false);
    private boolean mCalled = true;
    private ArrayList<Runnable> mDelayRenderRunnableList;
    private ArrayList<Runnable> mPostResumeRunnableList;
    private Runnable mCheckPostResumeRunnable = new Runnable() {
        @Override
        public void run() {
            if (isResumed() && mPostResumeRunnableList != null) {
                ArrayList<Runnable> list = mPostResumeRunnableList;
                if (!list.isEmpty()) {
                    for (Runnable runnable : list) {
                        runnable.run();
                    }
                }
                mPostResumeRunnableList = null;
            }
        }
    };
    private QMUIFragmentLazyLifecycleOwner mLazyViewLifecycleOwner;
    private QMUIFragmentEffectRegistry mFragmentEffectRegistry;

    private OnBackPressedDispatcher mOnBackPressedDispatcher;
    private OnBackPressedCallback mOnBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (sPopBackWhenSwipeFinished) {
                // must use normal back procedure when swipe finished.
                onNormalBackPressed();
                return;
            }
            QMUIFragment.this.onBackPressed();
        }
    };

    public QMUIFragment() {
        super();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mOnBackPressedDispatcher = requireActivity().getOnBackPressedDispatcher();
        mOnBackPressedDispatcher.addCallback(this, mOnBackPressedCallback);
        registerEffect(this, new QMUIFragmentResultEffectHandler() {
            @Override
            public boolean shouldHandleEffect(@NonNull FragmentResultEffect effect) {
                return effect.getRequestCode() == mSourceRequestCode && effect.getRequestFragmentUUid() == mUUid;
            }

            @Override
            public void handleEffect(@NonNull FragmentResultEffect effect) {
                onFragmentResult(effect.getRequestCode(), effect.getResultCode(), effect.getIntent());
                mSourceRequestCode = NO_REQUEST_CODE;
            }

            @Override
            public void handleEffect(@NonNull List<FragmentResultEffect> effects) {
                // only handle the latest
                handleEffect(effects.get(effects.size() - 1));
            }
        });
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
        mEnterAnimationStatus = ANIMATION_ENTER_STATUS_NOT_START;
    }

    @Override
    public void onResume() {
        checkLatestVisitRecord();
        checkForRequestForHandlePopBack();
        super.onResume();
        if (mBaseView != null && mPostResumeRunnableList != null && !mPostResumeRunnableList.isEmpty()) {
            mBaseView.post(mCheckPostResumeRunnable);
        }
    }

    protected void checkForRequestForHandlePopBack(){
        QMUIFragmentContainerProvider provider = findFragmentContainerProvider();
        if(provider != null){
            provider.requestForHandlePopBack(false);
        }
    }

    protected boolean shouldPerformLatestVisitRecord() {
        return true;
    }

    private void checkLatestVisitRecord() {

        Activity activity = getActivity();
        if (!(activity instanceof QMUIFragmentActivity)) {
            return;
        }

        if (this instanceof QMUINavFragment) {
            return;
        }

        sLatestVisitFragmentUUid = mUUid;

        if (!shouldPerformLatestVisitRecord()) {
            QMUILatestVisit.getInstance(getContext()).clearFragmentLatestVisitRecord();
            return;
        }

        Class<? extends QMUIFragment> cls = getClass();
        LatestVisitRecord latestVisitRecord = cls.getAnnotation(LatestVisitRecord.class);
        if (latestVisitRecord == null || (latestVisitRecord.onlyForDebug() && !QMUIConfig.DEBUG)) {
            QMUILatestVisit.getInstance(getContext()).clearFragmentLatestVisitRecord();
            return;
        }


        if (!activity.getClass().isAnnotationPresent(LatestVisitRecord.class)) {
            throw new RuntimeException(String.format("Can not perform LatestVisitRecord, " +
                    "%s must be annotated by LatestVisitRecord", activity.getClass().getSimpleName()));
        }
        QMUILatestVisit.getInstance(getContext()).performLatestVisitRecord(this);
    }

    public final void onLatestVisitArgumentChanged() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.INITIALIZED) && sLatestVisitFragmentUUid == mUUid) {
            checkLatestVisitRecord();
        }
    }

    @Override
    public void onCollectLatestVisitArgument(RecordArgumentEditor editor) {

    }


    @Nullable
    public <T extends Effect> QMUIFragmentEffectRegistration registerEffect(
            @NonNull final LifecycleOwner lifecycleOwner,
            @NonNull final QMUIFragmentEffectHandler<T> effectHandler) {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            throw new RuntimeException("Fragment(" + getClass().getSimpleName() + ") not attached to Activity.");
        }
        ensureFragmentEffectRegistry();
        return mFragmentEffectRegistry.register(lifecycleOwner, effectHandler);
    }

    public <T extends Effect> void notifyEffect(T effect) {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            QMUILog.d(TAG, "Fragment(" + getClass().getSimpleName() + ") not attached to Activity.");
            return;
        }
        ensureFragmentEffectRegistry();
        mFragmentEffectRegistry.notifyEffect(effect);
    }

    private void ensureFragmentEffectRegistry() {
        if (mFragmentEffectRegistry == null) {
            QMUIFragmentContainerProvider provider = findFragmentContainerProvider();
            ViewModelStoreOwner viewModelStoreOwner = provider != null ? provider.getContainerViewModelStoreOwner() : requireActivity();
            mFragmentEffectRegistry = new ViewModelProvider(viewModelStoreOwner).get(QMUIFragmentEffectRegistry.class);
        }
    }

    @Nullable
    protected QMUIFragmentContainerProvider findFragmentContainerProvider() {
        Fragment parent = getParentFragment();
        while (parent != null) {
            if (parent instanceof QMUIFragmentContainerProvider) {
                return (QMUIFragmentContainerProvider) parent;
            } else {
                parent = parent.getParentFragment();
            }
        }
        Activity activity = getActivity();
        if (activity instanceof QMUIFragmentContainerProvider) {
            return (QMUIFragmentContainerProvider) activity;
        }
        return null;
    }

    protected int startFragmentAndDestroyCurrent(QMUIFragment fragment) {
        return startFragmentAndDestroyCurrent(fragment, true);
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
    protected int startFragmentAndDestroyCurrent(QMUIFragment fragment,
                                                 boolean useNewTransitionConfigWhenPop) {
        if (!checkStateLoss("startFragmentAndDestroyCurrent")) {
            return -1;
        }

        QMUIFragmentContainerProvider provider = findFragmentContainerProvider();
        if (provider == null) {
            if (BuildConfig.DEBUG) {
                throw new RuntimeException("Can not find the fragment container provider.");
            } else {
                Log.d(TAG, "Can not find the fragment container provider.");
                return -1;
            }
        }

        QMUIFragment.TransitionConfig transitionConfig = fragment.onFetchTransitionConfig();
        String tagName = fragment.getClass().getSimpleName();
        FragmentManager fragmentManager = provider.getContainerFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction()
                .setCustomAnimations(
                        transitionConfig.enter, transitionConfig.exit,
                        transitionConfig.popenter, transitionConfig.popout)
                .setPrimaryNavigationFragment(null)
                .replace(provider.getContextViewId(), fragment, tagName);
        int index = transaction.commit();
        Utils.modifyOpForStartFragmentAndDestroyCurrent(fragmentManager, fragment, useNewTransitionConfigWhenPop, transitionConfig);
        return index;
    }

    /**
     * start a new fragment and add to BackStack
     * @param fragment the fragment to start
     * @return Returns the identifier of this transaction's back stack entry,
     * if {@link FragmentTransaction#addToBackStack(String)} had been called.  Otherwise, returns
     * a negative number.
     */
    public int startFragment(QMUIFragment fragment) {
        if (!checkStateLoss("startFragment")) {
            return -1;
        }
        QMUIFragmentContainerProvider provider = findFragmentContainerProvider();
        if (provider == null) {
            if (BuildConfig.DEBUG) {
                throw new RuntimeException("Can not find the fragment container provider.");
            } else {
                Log.d(TAG, "Can not find the fragment container provider.");
                return -1;
            }
        }
        return startFragment(fragment, provider);
    }

    /**
     * simulate the behavior of startActivityForResult/onActivityResult:
     * 1. Jump fragment1 to fragment2 via startActivityForResult(fragment2, requestCode)
     * 2. Pass data from fragment2 to fragment1 via setFragmentResult(RESULT_OK, data)
     * 3. Get data in fragment1 through onFragmentResult(requestCode, resultCode, data)
     *
     * @deprecated use {@link #registerEffect} for a replacement
     *
     * @param fragment    target fragment
     * @param requestCode request code
     */
    @Deprecated
    public int startFragmentForResult(QMUIFragment fragment, int requestCode) {
        if (!checkStateLoss("startFragmentForResult")) {
            return -1;
        }
        if (requestCode == NO_REQUEST_CODE) {
            throw new RuntimeException("requestCode can not be " + NO_REQUEST_CODE);
        }
        QMUIFragmentContainerProvider provider = findFragmentContainerProvider();
        if (provider == null) {
            if (BuildConfig.DEBUG) {
                throw new RuntimeException("Can not find the fragment container provider.");
            } else {
                Log.d(TAG, "Can not find the fragment container provider.");
                return -1;
            }
        }

        mSourceRequestCode = requestCode;
        fragment.mTargetFragmentUUid = mUUid;
        fragment.mTargetRequestCode = requestCode;
        return startFragment(fragment, provider);
    }

    private int startFragment(QMUIFragment fragment, QMUIFragmentContainerProvider provider) {
        QMUIFragment.TransitionConfig transitionConfig = fragment.onFetchTransitionConfig();
        String tagName = fragment.getClass().getSimpleName();
        return provider.getContainerFragmentManager()
                .beginTransaction()
                .setPrimaryNavigationFragment(null)
                .setCustomAnimations(transitionConfig.enter, transitionConfig.exit, transitionConfig.popenter, transitionConfig.popout)
                .replace(provider.getContextViewId(), fragment, tagName)
                .addToBackStack(tagName)
                .commit();
    }

    /**
     *
     * @param resultCode
     * @param data
     *
     * @deprecated use {@link #notifyEffect} for a replacement
     */
    @Deprecated
    public void setFragmentResult(int resultCode, Intent data) {
        if (mTargetRequestCode == NO_REQUEST_CODE) {
            return;
        }
        notifyEffect(new FragmentResultEffect(mTargetFragmentUUid, resultCode, mTargetRequestCode, data));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mBaseView.getTag(R.id.qmui_arch_reused_layout) == null) {
            onViewCreated(mBaseView);
        }
        mLazyViewLifecycleOwner = new QMUIFragmentLazyLifecycleOwner(this);
        mLazyViewLifecycleOwner.setViewVisible(getUserVisibleHint());
        getViewLifecycleOwner().getLifecycle().addObserver(mLazyViewLifecycleOwner);
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
            rootView.setTag(R.id.qmui_arch_reused_layout, true);
        }
        if (translucentFull()) {
            rootView.setFitsSystemWindows(false);
        } else {
            rootView.setFitsSystemWindows(true);
        }
        final SwipeBackLayout swipeBackLayout = SwipeBackLayout.wrap(rootView,
                dragViewMoveAction(),
                new SwipeBackLayout.Callback() {
                    @Override
                    public int getDragDirection(SwipeBackLayout swipeBackLayout, SwipeBackLayout.ViewMoveAction viewMoveAction, float downX, float downY, float dx, float dy, float touchSlop) {

                        mCalled = false;
                        boolean canHandle = canHandleSwipeBack();
                        if (!mCalled) {
                            throw new RuntimeException(getClass().getSimpleName() + " did not call through to super.shouldPreventSwipeBack()");
                        }

                        if(!canHandle){
                            return DRAG_DIRECTION_NONE;
                        }
                        return QMUIFragment.this.getDragDirection(
                                swipeBackLayout, viewMoveAction, downX, downY, dx, dy, touchSlop);
                    }
                });
        mListenerRemover = swipeBackLayout.addSwipeListener(mSwipeListener);
        swipeBackLayout.setOnKeyboardInsetHandler(this);
        if (isCreateForSwipeBack) {
            swipeBackLayout.setTag(R.id.fragment_container_view_tag, this);
        }
        return swipeBackLayout;
    }

    private SwipeBackLayout.SwipeListener mSwipeListener = new SwipeBackLayout.SwipeListener() {

        private QMUIFragment mModifiedFragment = null;

        @Override
        public void onScrollStateChange(int state, float scrollPercent) {
            Log.i(TAG, "SwipeListener:onScrollStateChange: state = " + state + " ;scrollPercent = " + scrollPercent);
            QMUIFragmentContainerProvider provider = findFragmentContainerProvider();
            if (provider == null || provider.getFragmentContainerView() == null) {
                return;
            }
            FragmentContainerView container = provider.getFragmentContainerView();
            mIsInSwipeBack = state != SwipeBackLayout.STATE_IDLE;
            if (state == SwipeBackLayout.STATE_IDLE) {
                if (mSwipeBackgroundView != null) {
                    if (scrollPercent <= 0.0F) {
                        mSwipeBackgroundView.unBind();
                        mSwipeBackgroundView = null;
                    } else if (scrollPercent >= 1.0F) {
                        // unbind mSwipeBackgroundView util onDestroy
                        if (getActivity() != null) {
                            sPopBackWhenSwipeFinished = true;
                            popBackStack();
                            int exitAnim = mSwipeBackgroundView.hasChildWindow() ?
                                    R.anim.swipe_back_exit_still : R.anim.swipe_back_exit;
                            getActivity().overridePendingTransition(R.anim.swipe_back_enter, exitAnim);
                            sPopBackWhenSwipeFinished = false;
                        }
                    }
                    return;
                }
                if (scrollPercent <= 0.0F) {
                    handleSwipeBackCancelOrFinished(container);
                } else if (scrollPercent >= 1.0F) {
                    handleSwipeBackCancelOrFinished(container);
                    FragmentManager fragmentManager = provider.getContainerFragmentManager();
                    Utils.findAndModifyOpInBackStackRecord(fragmentManager, -1, new Utils.OpHandler() {
                        @Override
                        public boolean handle(Object op) {
                            Field cmdField = Utils.getOpCmdField(op);
                            if (cmdField == null) {
                                return false;
                            }
                            try {
                                cmdField.setAccessible(true);
                                int cmd = (int) cmdField.get(op);
                                if (cmd == 1) {
                                    Field popEnterAnimField = Utils.getOpPopExitAnimField(op);
                                    if (popEnterAnimField != null) {
                                        popEnterAnimField.setAccessible(true);
                                        popEnterAnimField.set(op, 0);
                                    }
                                } else if (cmd == 3) {
                                    Field popExitAnimField = Utils.getOpPopEnterAnimField(op);
                                    if (popExitAnimField != null) {
                                        popExitAnimField.setAccessible(true);
                                        popExitAnimField.set(op, 0);
                                    }
                                }
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
                    sPopBackWhenSwipeFinished = true;
                    popBackStack();
                    sPopBackWhenSwipeFinished = false;
                }
            }
        }

        @Override
        public void onScroll(int dragDirection, int moveEdge, float scrollPercent) {
            scrollPercent = Math.max(0f, Math.min(1f, scrollPercent));
            QMUIFragmentContainerProvider provider = findFragmentContainerProvider();
            if (provider == null || provider.getFragmentContainerView() == null) {
                return;
            }
            FragmentContainerView container = provider.getFragmentContainerView();
            int targetOffset = (int) (Math.abs(
                    backViewInitOffset(container.getContext(), dragDirection, moveEdge)) * (1 - scrollPercent));
            int childCount = container.getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                View view = container.getChildAt(i);
                Object tag = view.getTag(R.id.qmui_arch_swipe_layout_in_back);
                if (SWIPE_BACK_VIEW.equals(tag)) {
                    SwipeBackLayout.translateInSwipeBack(view, moveEdge, targetOffset);
                }
            }
            if (mSwipeBackgroundView != null) {
                SwipeBackLayout.translateInSwipeBack(mSwipeBackgroundView, moveEdge, targetOffset);
            }
        }

        @SuppressLint("PrivateApi")
        @Override
        public void onSwipeBackBegin(final int dragDirection, final int moveEdge) {
            Log.i(TAG, "SwipeListener:onSwipeBackBegin: moveEdge = " + moveEdge);
            QMUIFragmentContainerProvider provider = findFragmentContainerProvider();
            if (provider == null || provider.getFragmentContainerView() == null) {
                return;
            }
            final FragmentContainerView container = provider.getFragmentContainerView();

            QMUIKeyboardHelper.hideKeyboard(mBaseView);
            onDragStart();
            FragmentManager fragmentManager = provider.getContainerFragmentManager();
            int backStackCount = fragmentManager.getBackStackEntryCount();
            if (backStackCount > 1) {
                Utils.findAndModifyOpInBackStackRecord(fragmentManager, -1, new Utils.OpHandler() {
                    @Override
                    public boolean handle(Object op) {
                        Field cmdField = Utils.getOpCmdField(op);
                        if (cmdField == null) {
                            return false;
                        }
                        try {
                            cmdField.setAccessible(true);
                            int cmd = (int) cmdField.get(op);
                            if (cmd == 3) {
                                Field fragmentField = Utils.getOpFragmentField(op);
                                if (fragmentField != null) {
                                    fragmentField.setAccessible(true);
                                    Object fragmentObject = fragmentField.get(op);
                                    if (fragmentObject instanceof QMUIFragment) {
                                        mModifiedFragment = (QMUIFragment) fragmentObject;
                                        mModifiedFragment.isCreateForSwipeBack = true;
                                        View baseView = mModifiedFragment.onCreateView(LayoutInflater.from(getContext()), container, null);
                                        mModifiedFragment.isCreateForSwipeBack = false;
                                        if (baseView != null) {
                                            addViewInSwipeBack(container, baseView, 0);
                                            handleChildFragmentListWhenSwipeBackStart(mModifiedFragment, baseView);
                                            SwipeBackLayout.translateInSwipeBack(baseView, moveEdge,
                                                    Math.abs(backViewInitOffset(baseView.getContext(), dragDirection, moveEdge)));
                                        }
                                    }
                                }
                            }
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
                    SwipeBackLayout.translateInSwipeBack(mSwipeBackgroundView, moveEdge,
                            Math.abs(backViewInitOffset(decorView.getContext(), dragDirection, moveEdge)));
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
                        view.setTranslationY(0);
                        view.setTranslationX(0);
                        parent.removeView(view);
                    }
                }
            }
        }


        private void handleChildFragmentListWhenSwipeBackStart(Fragment parentFragment, View baseView) throws IllegalAccessException {
            // handle issue #235
            if (baseView instanceof ViewGroup) {
                ViewGroup childMainContainer = (ViewGroup) baseView;
                FragmentManager childFragmentManager = parentFragment.getChildFragmentManager();
                List<Fragment> childFragmentList = childFragmentManager.getFragments();
                int childContainerId = 0;
                ViewGroup childContainer = null;
                for (Fragment fragment : childFragmentList) {
                    if (fragment instanceof QMUIFragment) {
                        QMUIFragment qmuiFragment = (QMUIFragment) fragment;
                        Field containerIdField = null;
                        try {
                            containerIdField = Fragment.class.getDeclaredField("mContainerId");
                        } catch (NoSuchFieldException e) {
                            continue;
                        }
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
                                handleChildFragmentListWhenSwipeBackStart(fragment, childView);
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
                mCacheRootView.setTag(R.id.qmui_arch_reused_layout, true);
            }
        }


        if (!isCreateForSwipeBack) {
            mBaseView = swipeBackLayout.getContentView();
            swipeBackLayout.setTag(R.id.qmui_arch_swipe_layout_in_back, null);
        }

        swipeBackLayout.setFitsSystemWindows(false);

        if (getActivity() != null) {
            QMUIViewHelper.requestApplyInsets(getActivity().getWindow());
        }

        return swipeBackLayout;
    }


    private void bubbleBackPressedEvent() {
        // disable this and go with FragmentManager's backPressesCallback
        // because it will call execPendingActions before popBackStackImmediate
        mOnBackPressedCallback.setEnabled(false);
        mOnBackPressedDispatcher.onBackPressed();
        mOnBackPressedCallback.setEnabled(true);
    }

    protected final void onNormalBackPressed() {
        runSideEffectOnNormalBackPressed();
        if (getParentFragment() != null) {
            bubbleBackPressedEvent();
            return;
        }

        Activity activity = requireActivity();
        if (activity instanceof QMUIFragmentContainerProvider) {
            QMUIFragmentContainerProvider provider = (QMUIFragmentContainerProvider) activity;
            if (provider.getContainerFragmentManager().getBackStackEntryCount() > 1 || provider.getContainerFragmentManager().getPrimaryNavigationFragment() == this) {
                bubbleBackPressedEvent();
            } else {
                QMUIFragment.TransitionConfig transitionConfig = onFetchTransitionConfig();
                if (QMUISwipeBackActivityManager.getInstance().canSwipeBack()) {
                    requireActivity().finish();
                    requireActivity().overridePendingTransition(transitionConfig.popenter, transitionConfig.popout);
                    return;
                }
                Object toExec = onLastFragmentFinish();
                if (toExec != null) {
                    if (toExec instanceof QMUIFragment) {
                        QMUIFragment fragment = (QMUIFragment) toExec;
                        startFragmentAndDestroyCurrent(fragment, false);
                    } else if (toExec instanceof Intent) {
                        Intent intent = (Intent) toExec;
                        startActivity(intent);
                        requireActivity().overridePendingTransition(transitionConfig.popenter, transitionConfig.popout);
                        requireActivity().finish();
                    } else {
                        onHandleSpecLastFragmentFinish(requireActivity(), transitionConfig, toExec);
                    }
                } else {
                    requireActivity().finish();
                    requireActivity().overridePendingTransition(transitionConfig.popenter, transitionConfig.popout);
                }
            }
        } else {
            bubbleBackPressedEvent();
        }
    }

    protected void runSideEffectOnNormalBackPressed() {

    }

    protected void onBackPressed() {
        onNormalBackPressed();
    }

    protected void onHandleSpecLastFragmentFinish(FragmentActivity fragmentActivity,
                                                  QMUIFragment.TransitionConfig transitionConfig,
                                                  Object toExec) {
        fragmentActivity.finish();
        fragmentActivity.overridePendingTransition(transitionConfig.popenter, transitionConfig.popout);
    }

    /**
     * pop back
     */
    protected void popBackStack() {
        if (mOnBackPressedDispatcher != null) {
            mOnBackPressedDispatcher.onBackPressed();
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
     * @param cls the type of target fragment
     */
    protected void popBackStack(Class<? extends QMUIFragment> cls) {
        if (checkPopBack()) {
            getParentFragmentManager().popBackStack(cls.getSimpleName(), 0);
        }
    }

    /**
     * pop back to a non-class type Fragment
     *
     * @param cls the target fragment class type
     */
    protected void popBackStackInclusive(Class<? extends QMUIFragment> cls) {
        if (checkPopBack()) {
            getParentFragmentManager().popBackStack(cls.getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    private boolean checkPopBack() {
        if (!isResumed() || mEnterAnimationStatus != ANIMATION_ENTER_STATUS_END) {
            return false;
        }
        return checkStateLoss("popBackStack");
    }

    protected void popBackStackAfterResume() {
        if (isResumed() && mEnterAnimationStatus == ANIMATION_ENTER_STATUS_END) {
            popBackStack();
        } else {
            runAfterAnimation(new Runnable() {
                @Override
                public void run() {
                    if (isResumed()) {
                        popBackStack();
                    } else {
                        runAfterResumed(new Runnable() {
                            @Override
                            public void run() {
                                popBackStack();
                            }
                        });
                    }
                }
            }, true);
        }
    }

    private boolean checkStateLoss(String logName) {
        if (!isAdded()) {
            return false;
        }
        FragmentManager fragmentManager = getParentFragmentManager();
        if (fragmentManager.isStateSaved()) {
            QMUILog.d(TAG, logName + " can not be invoked after onSaveInstanceState");
            return false;
        }
        return true;
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (!enter) {
            // This is a workaround for the bug where child value disappear when
            // the parent is removed (as all children are first removed from the parent)
            // See https://code.google.com/p/android/issues/detail?id=55228
            Fragment rootParentFragment = null;
            Fragment parentFragment = getParentFragment();
            while (parentFragment != null) {
                rootParentFragment = parentFragment;
                parentFragment = parentFragment.getParentFragment();
            }
            if (rootParentFragment != null && rootParentFragment.isRemoving()) {
                Animation doNothingAnim = new AlphaAnimation(1, 1);
                int duration = getResources().getInteger(R.integer.qmui_anim_duration);
                doNothingAnim.setDuration(duration);
                return doNothingAnim;
            }

        }
        Animation animation = null;
        if (enter) {
            try {
                animation = AnimationUtils.loadAnimation(getContext(), nextAnim);
            } catch (Throwable ignored) {

            }
            if (animation != null) {
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        checkAndCallOnEnterAnimationStart(animation);
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
                checkAndCallOnEnterAnimationStart(null);
                checkAndCallOnEnterAnimationEnd(null);
            }
        }
        return animation;
    }

    private void checkAndCallOnEnterAnimationStart(@Nullable Animation animation) {
        mCalled = false;
        onEnterAnimationStart(animation);
        if (!mCalled) {
            throw new RuntimeException(getClass().getSimpleName() + " did not call through to super.onEnterAnimationStart(Animation)");
        }
    }

    private void checkAndCallOnEnterAnimationEnd(@Nullable Animation animation) {
        mCalled = false;
        onEnterAnimationEnd(animation);
        if (!mCalled) {
            throw new RuntimeException(getClass().getSimpleName() + " did not call through to super.onEnterAnimationEnd(Animation)");
        }
    }


    /**
     * onCreateView
     */
    protected abstract View onCreateView();


    /**
     * Corresponding to {@link #onCreateView()}, it called only when new UI (not cached UI)
     * is created by {@link #onCreateView()}.
     * It may be used to bind views to fragment and dynamically create child views such as
     * {@link QMUITopBar#addLeftBackImageButton()}
     *
     * @param rootView the view created by {@link #onCreateView()}
     */
    protected void onViewCreated(@NonNull View rootView) {

    }

    /**
     * Will be performed in onStart
     *
     * @param requestCode request code
     * @param resultCode  result code
     * @param data        extra data
     *
     * @deprecated use {@link #registerEffect} for a replacement
     */
    @Deprecated
    protected void onFragmentResult(int requestCode, int resultCode, Intent data) {

    }

    /**
     * disable or enable drag back
     *
     * @return if true open dragBack, otherwise close dragBack
     * @deprecated Use {@link #getDragDirection(SwipeBackLayout, SwipeBackLayout.ViewMoveAction, float, float, float, float, float)}
     */
    @Deprecated
    protected boolean canDragBack() {
        return true;
    }


    /**
     * disable or enable drag back
     * @param context context
     * @param dragDirection gesture direction
     * @param moveEdge view move edge
     * @return if true open dragBack, otherwise close dragBack
     *
     * @deprecated Use {@link #getDragDirection(SwipeBackLayout, SwipeBackLayout.ViewMoveAction, float, float, float, float, float)}
     */
    @Deprecated
    protected boolean canDragBack(Context context, int dragDirection, int moveEdge) {
        return canDragBack();
    }

    /**
     * @return the init offset for backView for Parallax scrolling
     * @deprecated Use {@link #backViewInitOffset(Context, int, int)}
     */
    @Deprecated
    protected int backViewInitOffset() {
        return 0;
    }

    protected int backViewInitOffset(Context context, int dragDirection, int moveEdge) {
        return backViewInitOffset();
    }

    /**
     * called when drag back started.
     */
    protected void onDragStart() {

    }

    /**
     * @return
     * @deprecated Use {@link #dragBackDirection()}
     */
    @Deprecated
    protected int dragBackEdge() {
        return EDGE_LEFT;
    }

    /**
     *
     * @return
     * @deprecated Use {@link #getDragDirection(SwipeBackLayout, SwipeBackLayout.ViewMoveAction, float, float, float, float, float)}
     */
    @Deprecated
    protected int dragBackDirection() {
        int oldEdge = dragBackEdge();
        if (oldEdge == EDGE_RIGHT) {
            return SwipeBackLayout.DRAG_DIRECTION_RIGHT_TO_LEFT;
        } else if (oldEdge == EDGE_TOP) {
            return SwipeBackLayout.DRAG_DIRECTION_TOP_TO_BOTTOM;
        } else if (oldEdge == EDGE_BOTTOM) {
            return SwipeBackLayout.DRAG_DIRECTION_BOTTOM_TO_TOP;
        }
        return SwipeBackLayout.DRAG_DIRECTION_LEFT_TO_RIGHT;
    }

    protected SwipeBackLayout.ViewMoveAction dragViewMoveAction() {
        return SwipeBackLayout.MOVE_VIEW_AUTO;
    }

    protected boolean canHandleSwipeBack(){
        mCalled = true;
        // 1. can not swipe back if enter animation is not finished
        if (mEnterAnimationStatus != ANIMATION_ENTER_STATUS_END) {
            return false;
        }

        QMUIFragmentContainerProvider provider = findFragmentContainerProvider();
        if (provider == null) {
            return false;
        }
        FragmentManager fragmentManager = provider.getContainerFragmentManager();

        // 3. is not managed by QMUIFragmentContainerProvider
        if (fragmentManager == null || fragmentManager != getParentFragmentManager()) {
            return false;
        }

        // 4. should handle by child
        if(provider.isChildHandlePopBackRequested()){
            return false;
        }

        // 5. can not swipe back if the view is null
        View view = getView();
        if (view == null) {
            return false;
        }

        // 6. can not swipe back if the backStack entry count is less than 2
        if (fragmentManager.getBackStackEntryCount() <= 1 &&
                !QMUISwipeBackActivityManager.getInstance().canSwipeBack()) {
            return false;
        }

        return true;
    }

    protected int getDragDirection(@NonNull SwipeBackLayout swipeBackLayout,
                                   @NonNull SwipeBackLayout.ViewMoveAction viewMoveAction,
                                   float downX, float downY, float dx, float dy, float slopTouch) {
        int targetDirection = dragBackDirection();
        if (!canDragBack(swipeBackLayout.getContext(), targetDirection, viewMoveAction.getEdge(targetDirection))) {
            return DRAG_DIRECTION_NONE;
        }
        int edgeSize = QMUIDisplayHelper.dp2px(swipeBackLayout.getContext(), 20);
        if (targetDirection == DRAG_DIRECTION_LEFT_TO_RIGHT) {
            if (downX < edgeSize && dx >= slopTouch) {
                return targetDirection;
            }
        } else if (targetDirection == DRAG_DIRECTION_RIGHT_TO_LEFT) {
            if (downX > swipeBackLayout.getWidth() - edgeSize && -dx >= slopTouch) {
                return targetDirection;
            }
        } else if (targetDirection == DRAG_DIRECTION_TOP_TO_BOTTOM) {
            if (downY < edgeSize && dy >= slopTouch) {
                return targetDirection;
            }
        } else if (targetDirection == DRAG_DIRECTION_BOTTOM_TO_TOP) {
            if (downY > swipeBackLayout.getHeight() - edgeSize && -dy >= slopTouch) {
                return targetDirection;
            }
        }

        return DRAG_DIRECTION_NONE;
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
            if (mDelayRenderRunnableList == null) {
                mDelayRenderRunnableList = new ArrayList<>(4);
            }
            mDelayRenderRunnableList.add(runnable);
        }
    }

    /**
     * some action, such as {@link #popBackStack()}, can not't invoked duration fragment-lifecycle,
     * then we can call this method to ensure these actions is invoked after resumed.
     * one use case is to call {@link #popBackStackAfterResume()} in {@link #onFragmentResult(int, int, Intent)}
     *
     * @param runnable
     */
    public void runAfterResumed(Runnable runnable) {
        Utils.assertInMainThread();
        if (isResumed()) {
            runnable.run();
        } else {
            if (mPostResumeRunnableList == null) {
                mPostResumeRunnableList = new ArrayList<>(4);
            }
            mPostResumeRunnableList.add(runnable);
        }
    }

    protected void onEnterAnimationStart(@Nullable Animation animation) {
        if (mCalled) {
            throw new IllegalAccessError("don't call #onEnterAnimationStart() directly");
        }
        mCalled = true;
        mEnterAnimationStatus = ANIMATION_ENTER_STATUS_STARTED;
        isInEnterAnimationLiveData.setValue(true);
    }

    protected void onEnterAnimationEnd(@Nullable Animation animation) {
        if (mCalled) {
            throw new IllegalAccessError("don't call #onEnterAnimationEnd() directly");
        }
        mCalled = true;
        mEnterAnimationStatus = ANIMATION_ENTER_STATUS_END;
        isInEnterAnimationLiveData.setValue(false);
        if (mDelayRenderRunnableList != null) {
            ArrayList<Runnable> list = mDelayRenderRunnableList;
            mDelayRenderRunnableList = null;
            if (!list.isEmpty()) {
                for (Runnable runnable : list) {
                    runnable.run();
                }
            }
        }
    }

    public LiveData<Boolean> getIsInEnterAnimationLiveData() {
        return isInEnterAnimationLiveData;
    }

    protected <T> LiveData<T> enterAnimationAvoidTransform(final LiveData<T> origin){
        return enterAnimationAvoidTransform(origin, isInEnterAnimationLiveData);
    }

    protected <T> LiveData<T> enterAnimationAvoidTransform(final LiveData<T> origin, LiveData<Boolean> enterAnimationLiveData){
        final MediatorLiveData<T> result = new MediatorLiveData<T>();
        result.addSource(enterAnimationLiveData, new Observer<Boolean>(){

            boolean isAdded = false;
            @Override
            public void onChanged(Boolean isInEnterAnimation) {
                if(isInEnterAnimation){
                    isAdded = false;
                    result.removeSource(origin);
                }else {
                    if(!isAdded){
                        isAdded = true;
                        result.addSource(origin, new Observer<T>() {
                            @Override
                            public void onChanged(T t) {
                                result.setValue(t);
                            }
                        });
                    }
                }
            }
        });
        return result;
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

        // help gc, sometimes user may hold fragment instance in somewhere,
        // then these objects can not be released in time.
        mCacheSwipeBackLayout = null;
        mCacheRootView = null;
        mDelayRenderRunnableList = null;
        mCheckPostResumeRunnable = null;
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

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
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

    @Override
    public boolean handleKeyboardInset(int inset) {
        return false;
    }

    @Override
    public void refreshFromScheme(@Nullable Bundle bundle) {

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


    public final boolean isStartedByScheme() {
        Bundle arguments = getArguments();
        return arguments != null && arguments.getBoolean(QMUISchemeHandler.ARG_FROM_SCHEME, false);
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

