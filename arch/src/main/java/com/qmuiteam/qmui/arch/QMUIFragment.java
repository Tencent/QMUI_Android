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
import android.arch.core.util.Function;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
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
import com.qmuiteam.qmui.arch.annotation.DefaultFirstFragment;
import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord;
import com.qmuiteam.qmui.arch.first.FirstFragmentFinder;
import com.qmuiteam.qmui.arch.first.FirstFragmentFinders;
import com.qmuiteam.qmui.arch.record.LatestVisitArgumentCollector;
import com.qmuiteam.qmui.arch.record.RecordArgumentEditor;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIKeyboardHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.qmuiteam.qmui.arch.SwipeBackLayout.DRAG_DIRECTION_BOTTOM_TO_TOP;
import static com.qmuiteam.qmui.arch.SwipeBackLayout.DRAG_DIRECTION_LEFT_TO_RIGHT;
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
        QMUIFragmentLazyLifecycleOwner.Callback, LatestVisitArgumentCollector {
    static final String SWIPE_BACK_VIEW = "swipe_back_view";
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
        mEnterAnimationStatus = ANIMATION_ENTER_STATUS_NOT_START;
    }

    @Override
    public void onResume() {
        checkLatestVisitRecord();
        super.onResume();
        if (mBaseView != null && mPostResumeRunnableList != null && !mPostResumeRunnableList.isEmpty()) {
            mBaseView.post(mCheckPostResumeRunnable);
        }
    }

    private void checkLatestVisitRecord() {
        Class<? extends QMUIFragment> cls = getClass();
        Activity activity = getActivity();
        if (getParentFragment() != null || !(activity instanceof QMUIFragmentActivity)) {
            return;
        }
        if (!cls.isAnnotationPresent(LatestVisitRecord.class)) {
            QMUILatestVisit.getInstance(getContext()).clearFragmentLatestVisitRecord();
            return;
        }
        if (!activity.getClass().isAnnotationPresent(LatestVisitRecord.class)) {
            throw new RuntimeException(String.format("Can not perform LatestVisitRecord, " +
                    "%s must be annotated by LatestVisitRecord", activity.getClass().getSimpleName()));
        }
        DefaultFirstFragment defaultFirstFragment = activity.getClass().getAnnotation(DefaultFirstFragment.class);
        if (defaultFirstFragment != null && defaultFirstFragment.value() == getClass()) {
            QMUILatestVisit.getInstance(getContext()).performLatestVisitRecord(this);
        } else {
            QMUIFragmentActivity qActivity = (QMUIFragmentActivity) activity;
            int id = FirstFragmentFinders.getInstance().get(qActivity.getClass()).getIdByFragmentClass(cls);
            if (id == FirstFragmentFinder.NO_ID) {
                throw new RuntimeException(String.format("Can not perform LatestVisitRecord, " +
                                "%s must be annotated by FirstFragments which contains %s",
                        activity.getClass().getSimpleName(), cls.getSimpleName()));
            }
            QMUILatestVisit.getInstance(getContext()).performLatestVisitRecord(this);
        }
    }


    @Override
    public void onCollectLatestVisitArgument(RecordArgumentEditor editor) {

    }

    protected void startFragmentAndDestroyCurrent(QMUIFragment fragment) {
        startFragmentAndDestroyCurrent(fragment, true);
    }

    /**
     * see {@link QMUIFragmentActivity#startFragmentAndDestroyCurrent(QMUIFragment, boolean)}
     *
     * @param fragment                      new fragment to start
     * @param useNewTransitionConfigWhenPop
     */
    protected void startFragmentAndDestroyCurrent(QMUIFragment fragment, boolean useNewTransitionConfigWhenPop) {
        if (!checkStateLoss("startFragmentAndDestroyCurrent")) {
            return;
        }
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
        if (!checkStateLoss("startFragment")) {
            return;
        }
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
        if (!checkStateLoss("startFragmentForResult")) {
            return;
        }
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
        if (mBaseView.getTag(R.id.qmui_arch_reused_layout) == null) {
            onViewCreated(mBaseView);
        }
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
            rootView.setTag(R.id.qmui_arch_reused_layout, true);
        }
        if (translucentFull()) {
            rootView.setFitsSystemWindows(false);
        } else {
            rootView.setFitsSystemWindows(true);
        }
        final SwipeBackLayout swipeBackLayout = SwipeBackLayout.wrap(rootView, dragBackDirection(),
                dragViewMoveAction(),
                new SwipeBackLayout.Callback() {
                    @Override
                    public boolean canSwipeBack(SwipeBackLayout layout, int dragDirection, int moveEdge) {
                        if (mEnterAnimationStatus != ANIMATION_ENTER_STATUS_END) {
                            return false;
                        }
                        if (!canDragBack(layout.getContext(), dragDirection, moveEdge)) {
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

                    @Override
                    public boolean shouldBeginDrag(SwipeBackLayout swipeBackLayout, float downX, float downY, int dragDirection) {
                        return QMUIFragment.this.shouldBeginDrag(swipeBackLayout, downX, downY, dragDirection);
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
                            popBackStack();
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
                    popBackStack();
                }
            }
        }

        @Override
        public void onScroll(int dragDirection, int moveEdge, float scrollPercent) {
            scrollPercent = Math.max(0f, Math.min(1f, scrollPercent));
            ViewGroup container = getBaseFragmentActivity().getFragmentContainer();
            int targetOffset = (int) (Math.abs(
                    backViewInitOffset(container.getContext(), dragDirection, moveEdge)) * (1 - scrollPercent));
            int childCount = container.getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                View view = container.getChildAt(i);
                Object tag = view.getTag(R.id.qmui_arch_swipe_layout_in_back);
                if (SWIPE_BACK_VIEW.equals(tag)) {
                    SwipeBackLayout.offsetInSwipeBack(view, moveEdge, targetOffset);
                }
            }
            if (mSwipeBackgroundView != null) {
                SwipeBackLayout.offsetInSwipeBack(mSwipeBackgroundView, moveEdge, targetOffset);
            }
        }

        @SuppressLint("PrivateApi")
        @Override
        public void onSwipeBackBegin(final int dragDirection, final int moveEdge) {
            Log.i(TAG, "SwipeListener:onSwipeBackBegin: moveEdge = " + moveEdge);
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager == null) {
                return;
            }
            QMUIKeyboardHelper.hideKeyboard(mBaseView);
            onDragStart();
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
                                Field popEnterAnimField = Utils.getOpPopEnterAnimField(op);
                                if (popEnterAnimField != null) {
                                    popEnterAnimField.setAccessible(true);
                                    popEnterAnimField.set(op, 0);
                                }


                                Field fragmentField = Utils.getOpFragmentField(op);
                                if (fragmentField != null) {
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
                                            handleChildFragmentListWhenSwipeBackStart(mModifiedFragment, baseView);
                                            SwipeBackLayout.offsetInSwipeBack(baseView, moveEdge,
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
                    SwipeBackLayout.offsetInSwipeBack(mSwipeBackgroundView, moveEdge,
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

    /**
     * pop back
     */
    protected void popBackStack() {
        if (checkPopBack()) {
            getBaseFragmentActivity().popBackStack();
        }
    }

    /**
     * pop back to a class type fragment
     *
     * @param cls the target fragment class type
     */
    protected void popBackStack(Class<? extends QMUIFragment> cls) {
        if (checkPopBack()) {
            getBaseFragmentActivity().popBackStack(cls);
        }
    }

    /**
     * pop back to a non-class type Fragment
     *
     * @param cls the target fragment class type
     */
    protected void popBackStackInclusive(Class<? extends QMUIFragment> cls) {
        if (checkPopBack()) {
            getBaseFragmentActivity().popBackStackInclusive(cls);
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
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager == null) {
            QMUILog.d(TAG, logName + " can not be invoked because fragmentManager == null");
            return false;
        }
        if (fragmentManager.isStateSaved()) {
            QMUILog.d(TAG, logName + " can not be invoked after onSaveInstanceState");
            return false;
        }
        return true;
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
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
     */
    protected void onFragmentResult(int requestCode, int resultCode, Intent data) {

    }

    /**
     * disable or enable drag back
     *
     * @return if true open dragBack, otherwise close dragBack
     * @deprecated Use {@link #canDragBack(Context, int, int)}
     */
    @Deprecated
    protected boolean canDragBack() {
        return true;
    }


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

    protected boolean shouldBeginDrag(SwipeBackLayout swipeBackLayout,
                                      float downX, float downY, int dragDirection) {
        int edgeSize = QMUIDisplayHelper.dp2px(swipeBackLayout.getContext(), 20);
        if (dragDirection == DRAG_DIRECTION_LEFT_TO_RIGHT) {
            return downX < edgeSize;
        } else if (dragDirection == DRAG_DIRECTION_RIGHT_TO_LEFT) {
            return downX > swipeBackLayout.getWidth() - edgeSize;
        } else if (dragDirection == DRAG_DIRECTION_TOP_TO_BOTTOM) {
            return downY < edgeSize;
        } else if (dragDirection == DRAG_DIRECTION_BOTTOM_TO_TOP) {
            return downY > swipeBackLayout.getHeight() - edgeSize;
        }
        return true;
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
        mEnterAnimationStatus = ANIMATION_ENTER_STATUS_STARTED;
    }

    protected void onEnterAnimationEnd(@Nullable Animation animation) {
        if (mCalled) {
            throw new IllegalAccessError("don't call #onEnterAnimationEnd() directly");
        }
        mCalled = true;
        mEnterAnimationStatus = ANIMATION_ENTER_STATUS_END;
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

