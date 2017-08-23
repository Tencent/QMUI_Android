package com.qmuiteam.qmuidemo.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.QMUIWindowInsetLayout;
import com.qmuiteam.qmuidemo.R;

/**
 * Created by cgspine on 15/9/14.
 */
public abstract class BaseFragment extends Fragment {
    private static final String TAG = BaseFragment.class.getSimpleName();


    //============================= UI ================================

    private View mBaseView;

    public BaseFragment() {
        super();
    }


    public final BaseFragmentActivity getBaseFragmentActivity() {
        return (BaseFragmentActivity) getActivity();
    }

    public boolean isAttachedToActivity() {
        return !isRemoving() && mBaseView != null;
    }


    protected void startFragment(BaseFragment fragment) {
        BaseFragmentActivity baseFragmentActivity = this.getBaseFragmentActivity();
        if (baseFragmentActivity != null) {
            if (this.isAttachedToActivity()) {
                baseFragmentActivity.startFragment(fragment);
            } else {
                Log.e("BaseFragment", "fragment not attached:" + this);
            }
        } else {
            Log.e("BaseFragment", "startFragment null:" + this);
        }
    }


    /**
     * 显示键盘
     */
    protected void showKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethod.SHOW_FORCED);
    }

    /**
     * 隐藏键盘
     */
    protected boolean hideKeyBoard() {
//		return mBaseActivityImpl.hideKeyBoard();
        final InputMethodManager imm = (InputMethodManager) getActivity().getApplicationContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.hideSoftInputFromWindow(getActivity().findViewById(android.R.id.content)
                .getWindowToken(), 0);
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

    // 资源，放在业务初始化，会在业务层
    protected static final TransitionConfig SLIDE_TRANSITION_CONFIG = new TransitionConfig(
            R.anim.slide_in_right, R.anim.slide_out_left,
            R.anim.slide_in_left, R.anim.slide_out_right);
    protected static final TransitionConfig SCALE_TRANSITION_CONFIG = new TransitionConfig(
            R.anim.scale_enter, R.anim.slide_still, R.anim.slide_still,
            R.anim.scale_exit);


    //============================= 生命周期 ================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = onCreateView();
        if (translucentFull()) {
            if (mBaseView instanceof QMUIWindowInsetLayout) {
                view.setFitsSystemWindows(false);
                mBaseView = view;
            } else {
                mBaseView = new QMUIWindowInsetLayout(getActivity());
                ((QMUIWindowInsetLayout) mBaseView).addView(view, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
        } else {
            view.setFitsSystemWindows(true);
            mBaseView = view;
        }
        QMUIViewHelper.requestApplyInsets(getActivity().getWindow());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mBaseView.getParent() != null) {
            ((ViewGroup) mBaseView.getParent()).removeView(mBaseView);
        }
        return mBaseView;
    }

    protected void popBackStack() {
        getBaseFragmentActivity().popBackStack();
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
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
        } catch (Exception e) {

        }
        return null;
    }

    //============================= 新流程 ================================

    /**
     * onCreateView
     *
     * @return
     */
    protected abstract View onCreateView();


    /**
     * 沉浸式处理，返回false，则状态栏下为内容区域; 返回true, 则状态栏下为padding区域
     *
     * @return
     */
    protected boolean translucentFull() {
        return false;
    }


    /**
     * 如果是最后一个Fragment,finish后执行的方法
     *
     * @return
     */
    public Object onLastFragmentFinish() {
        return null;
    }

    /**
     * 转场动画控制
     *
     * @return
     */
    public TransitionConfig onFetchTransitionConfig() {
        return SLIDE_TRANSITION_CONFIG;
    }
}

