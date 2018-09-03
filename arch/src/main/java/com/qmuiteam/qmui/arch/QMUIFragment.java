package com.qmuiteam.qmui.arch;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.qmuiteam.qmui.QMUILog;
import com.qmuiteam.qmui.util.QMUIKeyboardHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * With the use of {@link QMUIFragmentActivity}, {@link QMUIFragment} brings more features,
 * such as swipe back, transition config, and so on.
 * <p>
 * Created by cgspine on 15/9/14.
 */
public abstract class QMUIFragment extends Fragment {
    private static final String SWIPE_BACK_VIEW = "swipe_back_view";
    private static final String TAG = QMUIFragment.class.getSimpleName();

    /**
     * Edge flag indicating that the left edge should be affected.
     */
    public static final int EDGE_LEFT = SwipeBackLayout.EDGE_LEFT;

    /**
     * Edge flag indicating that the right edge should be affected.
     */
    public static final int EDGE_RIGHT = SwipeBackLayout.EDGE_RIGHT;

    /**
     * Edge flag indicating that the bottom edge should be affected.
     */
    public static final int EDGE_BOTTOM = SwipeBackLayout.EDGE_BOTTOM;

    protected static final TransitionConfig SLIDE_TRANSITION_CONFIG = new TransitionConfig(
            R.anim.slide_in_right, R.anim.slide_out_left,
            R.anim.slide_in_left, R.anim.slide_out_right);

    protected static final TransitionConfig SCALE_TRANSITION_CONFIG = new TransitionConfig(
            R.anim.scale_enter, R.anim.slide_still,
            R.anim.slide_still, R.anim.scale_exit);


    public static final int RESULT_CANCELED = Activity.RESULT_CANCELED;
    public static final int RESULT_OK = Activity.RESULT_CANCELED;
    public static final int RESULT_FIRST_USER = Activity.RESULT_FIRST_USER;

    public static final int ANIMATION_ENTER_STATUS_NOT_START = -1;
    public static final int ANIMATION_ENTER_STATUS_STARTED = 0;
    public static final int ANIMATION_ENTER_STATUS_END = 1;


    private static final int NO_REQUEST_CODE = 0;
    private int mSourceRequestCode = NO_REQUEST_CODE;
    private Intent mResultData = null;
    private int mResultCode = RESULT_CANCELED;


    private View mBaseView;
    private SwipeBackLayout mCacheSwipeBackLayout;
    private View mCacheRootView;
    private boolean isCreateForSwipeBack = false;
    private int mBackStackIndex = 0;

    private int mEnterAnimationStatus = ANIMATION_ENTER_STATUS_NOT_START;
    private boolean mCalled = true;
    private ArrayList<Runnable> mDelayRenderRunnableList = new ArrayList<>();

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
    public void onDetach() {
        super.onDetach();
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
        fragment.setTargetFragment(this, requestCode);
        mSourceRequestCode = requestCode;
        startFragment(fragment);
    }


    public void setFragmentResult(int resultCode, Intent data) {
        int targetRequestCode = getTargetRequestCode();
        if (targetRequestCode == 0) {
            QMUILog.w(TAG, "call setFragmentResult, but not requestCode exists");
            return;
        }
        Fragment fragment = getTargetFragment();
        if (fragment == null || !(fragment instanceof QMUIFragment)) {
            return;
        }
        QMUIFragment targetFragment = (QMUIFragment) fragment;

        if (targetFragment.mSourceRequestCode == targetRequestCode) {
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
    public void onStart() {
        super.onStart();
        int requestCode = mSourceRequestCode;
        int resultCode = mResultCode;
        Intent data = mResultData;

        mSourceRequestCode = NO_REQUEST_CODE;
        mResultCode = RESULT_CANCELED;
        mResultData = null;

        if (requestCode != NO_REQUEST_CODE) {
            onFragmentResult(requestCode, resultCode, data);
        }
    }

    private SwipeBackLayout newSwipeBackLayout() {
        View rootView = mCacheRootView;
        if(rootView == null){
            rootView = onCreateView();
            mCacheRootView = rootView;
        }else{
            if(rootView.getParent() != null){
                ((ViewGroup)rootView.getParent()).removeView(rootView);
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
                        return canDragBack();
                    }
                });
        swipeBackLayout.addSwipeListener(new SwipeBackLayout.SwipeListener() {

            private QMUIFragment mModifiedFragment = null;

            @Override
            public void onScrollStateChange(int state, float scrollPercent) {
                Log.i(TAG, "SwipeListener:onScrollStateChange: state = " + state + " ;scrollPercent = " + scrollPercent);
                ViewGroup container = getBaseFragmentActivity().getFragmentContainer();
                int childCount = container.getChildCount();
                if (state == SwipeBackLayout.STATE_IDLE) {
                    if (scrollPercent <= 0.0F) {
                        for (int i = childCount - 1; i >= 0; i--) {
                            View view = container.getChildAt(i);
                            Object tag = view.getTag(R.id.qmui_arch_swipe_layout_in_back);
                            if (tag != null && SWIPE_BACK_VIEW.equals(tag)) {
                                container.removeView(view);
                                if (mModifiedFragment != null) {
                                    // give up swipe back, we should reset the revise
                                    try {
                                        Field viewField = Fragment.class.getDeclaredField("mView");
                                        viewField.setAccessible(true);
                                        viewField.set(mModifiedFragment, null);
                                        FragmentManager childFragmentManager = mModifiedFragment.getChildFragmentManager();
                                        Method dispatchCreatedMethod = childFragmentManager.getClass().getMethod("dispatchCreate");
                                        dispatchCreatedMethod.setAccessible(true);
                                        dispatchCreatedMethod.invoke(childFragmentManager);
                                    } catch (NoSuchFieldException e) {
                                        e.printStackTrace();
                                    } catch (NoSuchMethodException e) {
                                        e.printStackTrace();
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                    mModifiedFragment = null;
                                }

                            }
                        }
                    } else if (scrollPercent >= 1.0F) {
                        for (int i = childCount - 1; i >= 0; i--) {
                            View view = container.getChildAt(i);
                            Object tag = view.getTag(R.id.qmui_arch_swipe_layout_in_back);
                            if (tag != null && SWIPE_BACK_VIEW.equals(tag)) {
                                container.removeView(view);
                            }
                        }
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
                                        Field popEnterAnimField = op.getClass().getDeclaredField("popEnterAnim");
                                        popEnterAnimField.setAccessible(true);
                                        popEnterAnimField.set(op, 0);
                                    } else if (cmd == 3) {
                                        Field popExitAnimField = op.getClass().getDeclaredField("popExitAnim");
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
                        });
                        popBackStack();
                    }
                }
            }

            @Override
            public void onScroll(int edgeFlag, float scrollPercent) {
                int targetOffset = (int) (Math.abs(backViewInitOffset()) * (1 - scrollPercent));
                ViewGroup container = getBaseFragmentActivity().getFragmentContainer();
                int childCount = container.getChildCount();
                for (int i = childCount - 1; i >= 0; i--) {
                    View view = container.getChildAt(i);
                    Object tag = view.getTag(R.id.qmui_arch_swipe_layout_in_back);
                    if (tag != null && SWIPE_BACK_VIEW.equals(tag)) {
                        if (edgeFlag == EDGE_BOTTOM) {
                            ViewCompat.offsetTopAndBottom(view, targetOffset - view.getTop());
                        } else if (edgeFlag == EDGE_RIGHT) {
                            ViewCompat.offsetLeftAndRight(view, targetOffset - view.getLeft());
                        } else {
                            Log.i(TAG, "targetOffset = " + targetOffset + " ; view.getLeft() = " + view.getLeft());
                            ViewCompat.offsetLeftAndRight(view, -targetOffset - view.getLeft());
                        }
                    }
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
                QMUIKeyboardHelper.hideKeyboard(swipeBackLayout);
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
                                            baseView.setTag(R.id.qmui_arch_swipe_layout_in_back, SWIPE_BACK_VIEW);
                                            container.addView(baseView, 0);

                                            // handle issue #235
                                            Field viewField = Fragment.class.getDeclaredField("mView");
                                            viewField.setAccessible(true);
                                            viewField.set(mModifiedFragment, baseView);
                                            FragmentManager childFragmentManager = mModifiedFragment.getChildFragmentManager();
                                            Method dispatchCreatedMethod = childFragmentManager.getClass().getMethod("dispatchActivityCreated");
                                            dispatchCreatedMethod.setAccessible(true);
                                            dispatchCreatedMethod.invoke(childFragmentManager);

                                            int offset = Math.abs(backViewInitOffset());
                                            if (edgeFlag == EDGE_BOTTOM) {
                                                ViewCompat.offsetTopAndBottom(baseView, offset);
                                            } else if (edgeFlag == EDGE_RIGHT) {
                                                ViewCompat.offsetLeftAndRight(baseView, offset);
                                            } else {
                                                ViewCompat.offsetLeftAndRight(baseView, -1 * offset);
                                            }
                                        }
                                    }
                                }
                            }
                        }


                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } else if(getParentFragment() == null){
                    if (getActivity() != null) {
                        getActivity().getWindow().getDecorView().setBackgroundColor(0);
                        Utils.convertActivityToTranslucent(getActivity());
                    }
                }

            }

            @Override
            public void onScrollOverThreshold() {
                Log.i(TAG, "SwipeListener:onEdgeTouch:onScrollOverThreshold");
            }
        });
        return swipeBackLayout;
    }

    private boolean canNotUseCacheViewInCreateView(){
        return mCacheSwipeBackLayout.getParent() != null || ViewCompat.isAttachedToWindow(mCacheSwipeBackLayout);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SwipeBackLayout swipeBackLayout;
        if (mCacheSwipeBackLayout == null) {
            swipeBackLayout = newSwipeBackLayout();
            mCacheSwipeBackLayout = swipeBackLayout;
        } else if (isCreateForSwipeBack) {
            // in swipe back, exactly not in animation
            swipeBackLayout = mCacheSwipeBackLayout;
        } else {

            if (canNotUseCacheViewInCreateView()) {
                // try removeView first
                container.removeView(mCacheSwipeBackLayout);
            }

            if(canNotUseCacheViewInCreateView()){
                // give up!!!
                Log.i(TAG, "can not use cache swipeBackLayout, this may happen " +
                        "if invoke popBackStack duration fragment transition");
                swipeBackLayout = newSwipeBackLayout();
                mCacheSwipeBackLayout = swipeBackLayout;
            }else{
                swipeBackLayout = mCacheSwipeBackLayout;
            }
        }


        if (!isCreateForSwipeBack) {
            mBaseView = swipeBackLayout.getContentView();
            swipeBackLayout.setTag(R.id.qmui_arch_swipe_layout_in_back, null);
        }

        ViewCompat.setTranslationZ(swipeBackLayout, mBackStackIndex);

        swipeBackLayout.setFitsSystemWindows(false);

        if (getActivity() != null) {
            QMUIViewHelper.requestApplyInsets(getActivity().getWindow());
        }

        return swipeBackLayout;
    }

    protected void popBackStack() {
        if (mEnterAnimationStatus != ANIMATION_ENTER_STATUS_END) {
            return;
        }
        getBaseFragmentActivity().popBackStack();
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

