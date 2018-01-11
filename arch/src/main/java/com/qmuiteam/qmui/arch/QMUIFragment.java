package com.qmuiteam.qmui.arch;

import android.os.Bundle;
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

import com.qmuiteam.qmui.util.QMUIViewHelper;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 基础 Fragment 类，提供各种基础功能。
 * Created by cgspine on 15/9/14.
 */
public abstract class QMUIFragment extends Fragment {
    private static final String SWIPE_BACK_VIEW = "swipe_back_view";

    private static final String TAG = QMUIFragment.class.getSimpleName();

    // 资源，放在业务初始化，会在业务层
    protected static final TransitionConfig SLIDE_TRANSITION_CONFIG = new TransitionConfig(
            com.qmuiteam.qmui.arch.R.anim.slide_in_right, com.qmuiteam.qmui.arch.R.anim.slide_out_left,
            com.qmuiteam.qmui.arch.R.anim.slide_in_left, com.qmuiteam.qmui.arch.R.anim.slide_out_right);


    //============================= UI ================================
    protected static final TransitionConfig SCALE_TRANSITION_CONFIG = new TransitionConfig(
            com.qmuiteam.qmui.arch.R.anim.scale_enter, com.qmuiteam.qmui.arch.R.anim.slide_still, com.qmuiteam.qmui.arch.R.anim.slide_still,
            com.qmuiteam.qmui.arch.R.anim.scale_exit);

    private View mBaseView;
    private View mSwipeBackCacheView;
    private boolean mCreateForSwipBack = false;

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


    //============================= 生命周期 ================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mSwipeBackCacheView != null) {
            View swipeBackView = mSwipeBackCacheView;
            if (!mCreateForSwipBack) {
                mSwipeBackCacheView = null;
            }
            return swipeBackView;
        }
        mBaseView = onCreateView();
        SwipeBackLayout swipeBackLayout = SwipeBackLayout.wrap(mBaseView);
        swipeBackLayout.addSwipeListener(new SwipeBackLayout.SwipeListener() {
            @Override
            public void onScrollStateChange(int state, float scrollPercent) {
                Log.i(TAG, "SwipeListener:onScrollStateChange: state = " + state + " ;scrollPercent = " + scrollPercent);
                ViewGroup container = getBaseFragmentActivity().getFragmentContainer();
                int childCount = container.getChildCount();
                if (state == SwipeBackLayout.STATE_IDLE) {
                    if (scrollPercent <= 0.0F) {
                        for (int i = childCount - 1; i >= 0; i--) {
                            View view = container.getChildAt(i);
                            Object tag = view.getTag(-1);
                            if (tag != null && SWIPE_BACK_VIEW.equals(tag)) {
                                container.removeView(view);
                            }
                        }
                    } else if (scrollPercent >= 1.0F) {
                        for (int i = childCount - 1; i >= 0; i--) {
                            View view = container.getChildAt(i);
                            Object tag = view.getTag(-1);
                            if (tag != null && SWIPE_BACK_VIEW.equals(tag)) {
                                container.removeView(view);
                            }
                        }
                        FragmentManager fragmentManager = getFragmentManager();
                        if (fragmentManager == null) {
                            return;
                        }
                        int backstackCount = fragmentManager.getBackStackEntryCount();
                        if (backstackCount > 0) {
                            try {
                                FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(backstackCount - 1);

                                Field opsField = backStackEntry.getClass().getDeclaredField("mOps");
                                opsField.setAccessible(true);
                                Object opsObj = opsField.get(backStackEntry);
                                if (opsObj instanceof List<?>) {
                                    List<?> ops = (List<?>) opsObj;
                                    for (Object op : ops) {
                                        Field cmdField = op.getClass().getDeclaredField("cmd");
                                        cmdField.setAccessible(true);
                                        int cmd = (int) cmdField.get(op);
                                        if (cmd == 1) {
                                            Field popEnterAnimField = op.getClass().getDeclaredField("popExitAnim");
                                            popEnterAnimField.setAccessible(true);
                                            popEnterAnimField.set(op, 0);
                                        }
                                    }
                                }
                            } catch (NoSuchFieldException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }


                        popBackStack();
                    }
                }

            }

            @Override
            public void onEdgeTouch(int edgeFlag) {
                Log.i(TAG, "SwipeListener:onEdgeTouch: edgeFlag = " + edgeFlag);
                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager == null) {
                    return;
                }
                int backstackCount = fragmentManager.getBackStackEntryCount();
                if (backstackCount > 1) {
                    try {
                        FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(backstackCount - 1);

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
                                        QMUIFragment fragment = (QMUIFragment) fragmentObject;
                                        ViewGroup container = getBaseFragmentActivity().getFragmentContainer();
                                        fragment.mCreateForSwipBack = true;
                                        View baseView = fragment.onCreateView(LayoutInflater.from(getContext()), container, null);
                                        fragment.mCreateForSwipBack = false;
                                        if (baseView != null) {
                                            if (baseView.getParent() != null) {
                                                ((ViewGroup) baseView.getParent()).removeView(baseView);
                                            }
                                            baseView.setTag(-1, SWIPE_BACK_VIEW);
                                            container.addView(baseView, 0);
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
                }

            }

            @Override
            public void onScrollOverThreshold() {
                Log.i(TAG, "SwipeListener:onEdgeTouch:onScrollOverThreshold");
            }
        });
        swipeBackLayout.setFitsSystemWindows(false);
        if (translucentFull()) {
            mBaseView.setFitsSystemWindows(false);
        } else {
            mBaseView.setFitsSystemWindows(true);
        }
        if (getActivity() != null) {
            QMUIViewHelper.requestApplyInsets(getActivity().getWindow());
        }

        if (mCreateForSwipBack) {
            mSwipeBackCacheView = swipeBackLayout;
        }

        return swipeBackLayout;
    }

    protected void popBackStack() {
        getBaseFragmentActivity().popBackStack();
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (!enter && getParentFragment() != null && getParentFragment().isRemoving()) {
            // This is a workaround for the bug where child fragments disappear when
            // the parent is removed (as all children are first removed from the parent)
            // See https://code.google.com/p/android/issues/detail?id=55228
            Animation doNothingAnim = new AlphaAnimation(1, 1);
            doNothingAnim.setDuration(R.integer.qmui_anim_duration);
            return doNothingAnim;
        }

        // bugfix: 使用scale enter时看不到效果， 因为两个fragment的动画在同一个层级，被退出动画遮挡了
        // http://stackoverflow.com/questions/13005961/fragmenttransaction-animation-to-slide-in-over-top#33816251
        if (nextAnim != R.anim.scale_enter || !enter) {
            return super.onCreateAnimation(transit, enter, nextAnim);
        }
        try {
            Animation nextAnimation = AnimationUtils.loadAnimation(getContext(), nextAnim);
            nextAnimation.setAnimationListener(new Animation.AnimationListener() {

                private float mOldTranslationZ;

                @Override
                public void onAnimationStart(Animation animation) {
                    if (getView() != null) {
                        mOldTranslationZ = ViewCompat.getTranslationZ(getView());
                        ViewCompat.setTranslationZ(getView(), 100.f);
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (getView() != null) {
                        getView().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //延迟回复z-index,如果退出动画更长，这里可能会失效
                                ViewCompat.setTranslationZ(getView(), mOldTranslationZ);
                            }
                        }, 100);

                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            return nextAnimation;
        } catch (Exception ignored) {

        }
        return null;
    }

    /**
     * onCreateView
     */
    protected abstract View onCreateView();

    //============================= 新流程 ================================

    /**
     * 沉浸式处理，返回 false，则状态栏下为内容区域，返回 true, 则状态栏下为 padding 区域
     */
    protected boolean translucentFull() {
        return false;
    }

    /**
     * 如果是最后一个Fragment，finish后执行的方法
     */
    @SuppressWarnings("SameReturnValue")
    public Object onLastFragmentFinish() {
        return null;
    }

    /**
     * 转场动画控制
     */
    public TransitionConfig onFetchTransitionConfig() {
        return SLIDE_TRANSITION_CONFIG;
    }

    ////////界面跳转动画
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

