package com.qmuiteam.qmui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.util.LongSparseArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 使 {@link ListView} 支持添加/删除 Item 的动画，支持自定义动画效果。
 * <p>
 * https://github.com/cypressious/AnimationListView/blob/master/AnimationListView/src/de/cypressworks/animationlistview/AnimationListView.java
 * <p>
 * 一个痛点：
 * 在LayoutTransition中有一个CHANGE_DISAPPEAR的概念：指由于添加或移动等操作导致子view消失的场景。
 * QMUIAnimationListView同样有这样的场景，但是ListView上LayoutTransition不生效，所以我们需要自己模拟实现。
 * 但是当layout后，消失的view已经被回收了，我们只能对 ListView 当前存在的view做动画，那么如何去做CHANGE_DISAPPEAR动画呢？
 * 这里采用了一个非常挫的实现方案（暂时没想到其它好的方案）：
 * 1.在layout前对当前屏幕的view都设置 setHasTransientState(true)。这样做后，view不会被直接被ListView回收到scrap中
 * 2.将当前屏幕的view都保存在mDetachViewsMap中
 * 3.在draw之前（这个时候我们已经能确定哪些item会完全离开屏幕了），剔除不会完全离开屏幕的item
 * 4.开启一个ValueAnimator,每次update时调用invalidate()触发 onDraw() 方法
 * 5.在 onDraw() 方法中根据animator的已动画时间计算view动画的位置，调用view.draw方法draw出来，因为是之前存在过的view，大小必定是确定的
 * 6.最后需要调用 setHasTransientState(false)，以便view最终可以被回收
 * <p>
 * 这种方法的代价就是：当前屏幕的item不能够被及时回收（最终还是会被回收的）
 * 所以增加 mOpenChangeDisappearAnimation 变量，如果你并不在意 CHANGE_DISAPPEAR 没有动画的那一点点不协调，那就不用开启它
 *
 * @author cginechen
 * @date 2017-03-30
 */

@SuppressWarnings({"rawtypes", "unchecked"})
public class QMUIAnimationListView extends ListView {
    private static final String TAG = "QMUIAnimationListView";
    private static final long DURATION_ALPHA = 300;
    private static final long DURATION_OFFSET_MIN = 0;
    private static final long DURATION_OFFSET_MAX = 1000;
    private static final float DEFAULT_OFFSET_DURATION_UNIT = 0.5f;

    protected final LongSparseArray<Integer> mTopMap = new LongSparseArray<>();
    protected final LongSparseArray<Integer> mPositionMap = new LongSparseArray<>();
    protected final LongSparseArray<View> mDetachViewsMap = new LongSparseArray<>();
    protected final Set<Long> mBeforeVisible = new HashSet<>();
    protected final Set<Long> mAfterVisible = new HashSet<>();
    private final List<Manipulator> mPendingManipulations = new ArrayList<>();
    private final List<Manipulator> mPendingManipulationsWithoutAnimation = new ArrayList<>();
    private long mChangeDisappearPlayTime = 0;
    private ValueAnimator mChangeDisappearAnimator;

    private ListAdapter mRealAdapter;
    private WrapperAdapter mWrapperAdapter;
    private boolean mIsAnimating = false;
    private int mAnimationManipulateDurationLimit = 0;
    private long mLastManipulateTime = 0;
    private float mOffsetDurationUnit = DEFAULT_OFFSET_DURATION_UNIT; // 移动1px的时间
    private Interpolator mOffsetInterpolator = new LinearInterpolator();
    private boolean mOpenChangeDisappearAnimation = false;


    public QMUIAnimationListView(Context context) {
        this(context, null);
    }

    public QMUIAnimationListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public QMUIAnimationListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public QMUIAnimationListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setWillNotDraw(false);
    }

    public ListAdapter getRealAdapter() {
        return mRealAdapter;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        mRealAdapter = adapter;
        mWrapperAdapter = new WrapperAdapter(mRealAdapter);
        super.setAdapter(mWrapperAdapter);
    }

    public void setAnimationManipulateDurationLimit(int animationManipulateDurationLimit) {
        mAnimationManipulateDurationLimit = animationManipulateDurationLimit;
    }

    public <T extends ListAdapter> void manipulate(final Manipulator<T> manipulator) {
        Log.i(TAG, "manipulate");
        if (!mWrapperAdapter.isAnimationEnabled()) {
            manipulateWithoutAnimation(manipulator);
            return;
        }
        long now = SystemClock.uptimeMillis();
        boolean notLimitedAnimation = now - mLastManipulateTime > mAnimationManipulateDurationLimit;
        mLastManipulateTime = now;
        if (!mIsAnimating) {
            if (notLimitedAnimation) {
                mIsAnimating = true;
                prepareAnimation();
                manipulator.manipulate((T) mRealAdapter);

                doAnimation();
            } else {
                manipulator.manipulate((T) mRealAdapter);
                mWrapperAdapter.notifyDataSetChanged();
            }
        } else {
            if (notLimitedAnimation) {
                mPendingManipulations.add(manipulator);
            } else {
                mPendingManipulationsWithoutAnimation.add(manipulator);
            }
        }
    }

    public <T extends ListAdapter> void manipulateWithoutAnimation(final Manipulator<T> manipulator) {
        Log.i(TAG, "manipulateWithoutAnimation");
        if (!mIsAnimating) {
            manipulator.manipulate((T) mRealAdapter);
            mWrapperAdapter.notifyDataSetChanged();
        } else {
            mPendingManipulationsWithoutAnimation.add(manipulator);
        }
    }

    public float getOffsetDurationUnit() {
        return mOffsetDurationUnit;
    }

    public void setOffsetDurationUnit(float offsetDurationUnit) {
        mOffsetDurationUnit = offsetDurationUnit;
    }

    private long getOffsetDuration(int start, int end) {
        long duration = (long) (Math.abs(start - end) * mOffsetDurationUnit);
        return Math.max(DURATION_OFFSET_MIN, Math.min(duration, DURATION_OFFSET_MAX));
    }

    /**
     * 是否启用 CHANGE-DISAPPEAR 动画。
     *
     * @param openChangeDisappearAnimation true 为启用 CHANGE-DISAPPEAR 动画，false 则不启用。
     */
    public void setOpenChangeDisappearAnimation(boolean openChangeDisappearAnimation) {
        mOpenChangeDisappearAnimation = openChangeDisappearAnimation;
    }

    public void setOffsetInterpolator(Interpolator offsetInterpolator) {
        mOffsetInterpolator = offsetInterpolator;
    }

    private void prepareAnimation() {
        mTopMap.clear();
        mPositionMap.clear();
        mBeforeVisible.clear();
        mAfterVisible.clear();
        mDetachViewsMap.clear();

        mWrapperAdapter.setShouldNotifyChange(false);
        int childCount = getChildCount();
        int firstVisiblePos = getFirstVisiblePosition();
        for (int i = 0; i < childCount; i++) {
            final View view = getChildAt(i);
            long id = mWrapperAdapter.getItemId(firstVisiblePos + i);
            mTopMap.put(id, view.getTop());
            mPositionMap.put(id, i);
        }

        for (int i = 0; i < firstVisiblePos; i++) {
            final long id = mWrapperAdapter.getItemId(i);
            mBeforeVisible.add(id);
        }

        final int count = mWrapperAdapter.getCount();

        for (int i = firstVisiblePos + childCount; i < count; i++) {
            final long id = mWrapperAdapter.getItemId(i);
            mAfterVisible.add(id);
        }
    }

    private void doAnimation() {
        setEnabled(false);
        setClickable(false);
        doPreLayoutAnimation(new ManipulateAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mWrapperAdapter.notifyDataSetChanged();
                getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                    @Override
                    public boolean onPreDraw() {
                        getViewTreeObserver().removeOnPreDrawListener(this);

                        doPostLayoutAnimation();

                        return true;
                    }

                });
            }
        });

    }

    private void doPreLayoutAnimation(Animator.AnimatorListener listener) {
        final AnimatorSet animatorSet = new AnimatorSet();
        ArrayList<Long> deleteIds = new ArrayList<>();
        int i;
        for (i = 0; i < mTopMap.size(); i++) {
            long id = mTopMap.keyAt(i);
            int newPos = getPositionForId(id);
            if (newPos < 0) {
                // delete
                int oldPos = mPositionMap.get(id);
                View child = getChildAt(oldPos);
                final Animator anim = getDeleteAnimator(child);
                mPositionMap.remove(id);
                animatorSet.play(anim);
                deleteIds.add(id);
            }
        }

        for (i = 0; i < deleteIds.size(); i++) {
            mTopMap.remove(deleteIds.get(i));
        }

        if (mOpenChangeDisappearAnimation) {
            for (i = 0; i < mPositionMap.size(); i++) {
                View view = getChildAt(mPositionMap.valueAt(i));
                ViewCompat.setHasTransientState(view, true);
                mDetachViewsMap.put(mPositionMap.keyAt(i), view);
            }
        }
        if (!animatorSet.getChildAnimations().isEmpty()) {
            animatorSet.addListener(listener);
            animatorSet.start();
        } else {
            listener.onAnimationEnd(animatorSet);
        }
    }

    private void doPostLayoutAnimation() {
        final AnimatorSet animatorSet = new AnimatorSet();
        int childCount = getChildCount();
        int firstVisiblePos = getFirstVisiblePosition();
        Animator anim = null;
        int addOccurTop = -1;
        int addOccurPosition = -1;
        if (mOpenChangeDisappearAnimation) {
            for (int i = 0; i < mDetachViewsMap.size(); i++) {
                ViewCompat.setHasTransientState(mDetachViewsMap.valueAt(i), false);
            }
        }
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            child.setAlpha(1f);
            int newTop = child.getTop();
            int position = firstVisiblePos + i;
            long id = mWrapperAdapter.getItemId(position);
            if (mTopMap.indexOfKey(id) > -1) {
                addOccurTop = -1;
                int oldTop = mTopMap.get(id);
                mTopMap.remove(id);
                mPositionMap.remove(id);
                if (mOpenChangeDisappearAnimation) {
                    mDetachViewsMap.remove(id);
                }
                if (oldTop != newTop) {
                    anim = getOffsetAnimator(child, oldTop, newTop);
                }
            } else if (mBeforeVisible.contains(id)) {
                addOccurTop = -1;
                int oldTop = -child.getHeight();
                anim = getOffsetAnimator(child, oldTop, newTop);
            } else if (mAfterVisible.contains(id)) {
                addOccurTop = -1;
                int oldTop = getHeight();
                anim = getOffsetAnimator(child, oldTop, newTop);
            } else {
                // new add item
                if (addOccurTop == -1) {
                    addOccurTop = newTop;
                    addOccurPosition = position;
                }
                anim = getAddAnimator(child, newTop, position, addOccurTop, addOccurPosition);
            }
            if (anim != null) {
                animatorSet.play(anim);
            }
        }

        if (mOpenChangeDisappearAnimation && mDetachViewsMap.size() > 0) {
            mChangeDisappearAnimator = ValueAnimator.ofFloat(0, 1);
            mChangeDisappearAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mChangeDisappearPlayTime = animation.getCurrentPlayTime();
                    invalidate();
                }
            });
            mChangeDisappearAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mChangeDisappearPlayTime = 0;
                }
            });
            mChangeDisappearAnimator.setDuration(getChangeDisappearDuration());
            mChangeDisappearAnimator.start();
        }
        animatorSet.addListener(new ManipulateAnimatorListener() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                finishAnimation();
            }
        });

        animatorSet.start();
    }

    protected long getChangeDisappearDuration() {
        return (long) (getHeight() * mOffsetDurationUnit);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mOpenChangeDisappearAnimation && mChangeDisappearAnimator != null &&
                mChangeDisappearAnimator.isStarted() && mDetachViewsMap.size() > 0 && mIsAnimating) {
            for (int i = 0; i < mDetachViewsMap.size(); i++) {
                long id = mDetachViewsMap.keyAt(i);
                View view = mDetachViewsMap.valueAt(i);
                int newPos = getPositionForId(id);
                int top, offset = (int) (mChangeDisappearPlayTime / mOffsetDurationUnit);
                if (newPos < getFirstVisiblePosition()) {
                    top = mTopMap.get(id) - offset;
                    if (top < -view.getHeight()) {
                        continue;
                    }
                } else {
                    top = mTopMap.get(id) + offset;
                    if (top > getHeight()) {
                        continue;
                    }
                }
                view.layout(0, top, view.getWidth(), top + view.getHeight());
                view.setAlpha(1f - mChangeDisappearPlayTime * 1f / getChangeDisappearDuration());
                // 不能直接调用view.draw(canvas), 在listview上由于缓存会冲突
                drawChild(canvas, view, getDrawingTime());
            }
        }
    }

    private void finishAnimation() {
        mWrapperAdapter.setShouldNotifyChange(true);
        mChangeDisappearAnimator = null;
        if (mOpenChangeDisappearAnimation) {
            for (int i = 0; i < mDetachViewsMap.size(); i++) {
                mDetachViewsMap.valueAt(i).setAlpha(1);
            }
            mDetachViewsMap.clear();
        }
        mIsAnimating = false;
        setEnabled(true);
        setClickable(true);

        manipulatePending();
    }

    private void manipulatePending() {

        if (!mPendingManipulationsWithoutAnimation.isEmpty()) {
            mIsAnimating = true;
            for (final Manipulator manipulator : mPendingManipulationsWithoutAnimation) {
                manipulator.manipulate(mRealAdapter);
            }
            mPendingManipulationsWithoutAnimation.clear();
            mWrapperAdapter.notifyDataSetChanged();

            post(new Runnable() {

                @Override
                public void run() {
                    mIsAnimating = false;
                    manipulatePending();
                }
            });
        } else {

            if (mPendingManipulations.isEmpty()) {
                return;
            }
            mIsAnimating = true;
            prepareAnimation();

            for (final Manipulator manipulator : mPendingManipulations) {
                manipulator.manipulate(mRealAdapter);
            }
            mPendingManipulations.clear();

            doAnimation();
        }
    }

    protected Animator getDeleteAnimator(View view) {
        return alphaObjectAnimator(view, false, DURATION_ALPHA, true);
    }

    protected Animator getOffsetAnimator(View view, int oldTop, int newTop) {
        return getOffsetAnimator(view, oldTop, newTop, getOffsetDuration(oldTop, newTop));
    }

    protected Animator getOffsetAnimator(View view, int oldTop, int newTop, long duration) {
        final ObjectAnimator anim = ObjectAnimator.ofFloat(view,
                "translationY", oldTop - newTop, 0);

        anim.setDuration(duration);
        anim.setInterpolator(mOffsetInterpolator);
        return anim;
    }

    protected Animator getAddAnimator(View view, int top, int position, int addOccurTop, int addOccurPosition) {
        view.setAlpha(0);
        view.clearAnimation();
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(alphaObjectAnimator(view, true, 50, false));
        if (addOccurTop != top) {
            animatorSet.play(getOffsetAnimator(view, addOccurTop, top));
        }
        animatorSet.setStartDelay((long) (view.getHeight() * mOffsetDurationUnit));
        return animatorSet;
    }

    protected ObjectAnimator alphaObjectAnimator(View view, final boolean fadeIn, long duration, boolean postBack) {
        final ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", fadeIn ? new float[]{
                0f, 1f} : new float[]{1f, 0f});

        anim.setDuration(duration);

        if (postBack) {
            final WeakReference<View> wr = new WeakReference<>(view);
            anim.addListener(new ManipulateAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (wr.get() != null) {
                        wr.get().setAlpha(fadeIn ? 0 : 1);
                    }
                }
            });
        }

        return anim;
    }

    protected int getPositionForId(final long id) {
        for (int i = 0; i < mWrapperAdapter.getCount(); i++) {
            if (mWrapperAdapter.getItemId(i) == id) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return isEnabled() && super.dispatchTouchEvent(ev);
    }

    public interface Manipulator<T extends ListAdapter> {
        void manipulate(T adapter);
    }

    private static class WrapperAdapter extends BaseAdapter {
        private ListAdapter mAdapter;
        private boolean mShouldNotifyChange = true;
        private final DataSetObserver mObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                if (mShouldNotifyChange) {
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onInvalidated() {
                notifyDataSetInvalidated();
            }
        };
        private boolean mIsAnimationEnabled = false;

        public WrapperAdapter(ListAdapter adapter) {
            mAdapter = adapter;
            mAdapter.registerDataSetObserver(mObserver);
        }

        public void setShouldNotifyChange(boolean shouldNotifyChange) {
            mShouldNotifyChange = shouldNotifyChange;
        }

        public boolean isAnimationEnabled() {
            return mIsAnimationEnabled;
        }

        @Override
        public int getCount() {
            return mAdapter.getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return mAdapter.getItemViewType(position);
        }

        @Override
        public int getViewTypeCount() {
            return mAdapter.getViewTypeCount();
        }

        @Override
        public Object getItem(int position) {
            return mAdapter.getItem(position);
        }

        @Override
        public long getItemId(int position) {
            return mAdapter.getItemId(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mAdapter.getView(position, convertView, parent);
        }

        @Override
        public boolean hasStableIds() {
            boolean stable = mAdapter.hasStableIds();
            mIsAnimationEnabled = stable;
            return stable;
        }
    }

    private abstract class ManipulateAnimatorListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
}