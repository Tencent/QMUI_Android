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

package com.qmuiteam.qmui.recyclerView;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.qmuiteam.qmui.R;

import java.util.ArrayList;
import java.util.List;

public class QMUIRVItemSwipeAction extends RecyclerView.ItemDecoration
        implements RecyclerView.OnChildAttachStateChangeListener {
    public static final int SWIPE_NONE = 0;
    public static final int SWIPE_LEFT = 1;
    public static final int SWIPE_RIGHT = 2;
    public static final int SWIPE_UP = 3;
    public static final int SWIPE_DOWN = 4;

    public static final int ANIMATION_TYPE_SWIPE_SUCCESS = 1;
    public static final int ANIMATION_TYPE_SWIPE_CANCEL = 2;
    public static final int ANIMATION_TYPE_SWIPE_ACTION = 3;

    private static final int ACTIVE_POINTER_ID_NONE = -1;

    private static final int PIXELS_PER_SECOND = 1000;
    private static final int SWIPE_TRIGGERED_IMMEDIATELY = -1;

    private static final String TAG = "QMUIRVItemSwipeAction";

    private static final boolean DEBUG = false;

    /**
     * Views, whose state should be cleared after they are detached from RecyclerView.
     * This is necessary after swipe dismissing an item. We wait until animator finishes its job
     * to clean these views.
     */
    final List<View> mPendingCleanup = new ArrayList<>();

    /**
     * Re-use array to calculate dx dy for a ViewHolder
     */
    private final float[] mTmpPosition = new float[2];

    /**
     * The reference coordinates for the action start. For drag & drop, this is the time long
     * press is completed vs for swipe, this is the initial touch point.
     */
    float mInitialTouchX;

    float mInitialTouchY;

    long mDownTimeMillis = 0;

    /**
     * Set when ItemTouchHelper is assigned to a RecyclerView.
     */
    private float mSwipeEscapeVelocity;

    /**
     * Set when ItemTouchHelper is assigned to a RecyclerView.
     */
    private float mMaxSwipeVelocity;

    /**
     * The diff between the last event and initial touch.
     */
    float mDx;

    float mDy;

    /**
     * The pointer we are tracking.
     */
    int mActivePointerId = ACTIVE_POINTER_ID_NONE;

    /**
     * When a View is swiped and needs to go back to where it was, we create a Recover
     * Animation and animate it to its location using this custom Animator, instead of using
     * framework Animators.
     * Using framework animators has the side effect of clashing with ItemAnimator, creating
     * jumpy UIs.
     */
    List<RecoverAnimation> mRecoverAnimations = new ArrayList<>();

    private int mSlop;

    RecyclerView mRecyclerView;

    /**
     * Used for detecting fling swipe
     */
    VelocityTracker mVelocityTracker;

    private long mPressTimeToSwipe = SWIPE_TRIGGERED_IMMEDIATELY;

    /**
     * The coordinates of the selected view at the time it is selected. We record these values
     * when action starts so that we can consistently position it even if LayoutManager moves the
     * View.
     */
    float mSelectedStartX;
    float mSelectedStartY;

    int mSwipeDirection;

    private MotionEvent mCurrentDownEvent;
    private Runnable mLongPressToSwipe = new Runnable() {
        @Override
        public void run() {
            if (mCurrentDownEvent != null) {
                final int activePointerIndex = mCurrentDownEvent.findPointerIndex(mActivePointerId);
                if (activePointerIndex >= 0) {
                    checkSelectForSwipe(mCurrentDownEvent.getAction(), mCurrentDownEvent, activePointerIndex, true);
                }
            }
        }
    };

    /**
     * Currently selected view holder
     */
    RecyclerView.ViewHolder mSelected = null;

    private final RecyclerView.OnItemTouchListener mOnItemTouchListener = new RecyclerView.OnItemTouchListener() {
        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView,
                                             @NonNull MotionEvent event) {
            if (DEBUG) {
                Log.d(TAG, "intercept: x:" + event.getX() + ",y:" + event.getY() + ", " + event);
            }
            final int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                if (mCurrentDownEvent != null) {
                    mCurrentDownEvent.recycle();
                }
                mCurrentDownEvent = MotionEvent.obtain(event);
                if (mPressTimeToSwipe > 0 && mSelected == null) {
                    recyclerView.postDelayed(mLongPressToSwipe, mPressTimeToSwipe);
                }
                mActivePointerId = event.getPointerId(0);
                mInitialTouchX = event.getX();
                mInitialTouchY = event.getY();
                obtainVelocityTracker();
                mDownTimeMillis = System.currentTimeMillis();
                if (mSelected == null) {
                    final RecoverAnimation animation = findAnimation(event);
                    if (animation != null) {
                        mInitialTouchX -= animation.mX;
                        mInitialTouchY -= animation.mY;
                        endRecoverAnimation(animation.mViewHolder, true);
                        if (mPendingCleanup.remove(animation.mViewHolder.itemView)) {
                            mCallback.clearView(mRecyclerView, animation.mViewHolder);
                        }
                        select(animation.mViewHolder);
                        updateDxDy(event, mSwipeDirection, 0);
                    }
                } else {
                    if (mSelected instanceof QMUISwipeViewHolder) {
                        QMUISwipeViewHolder swipeViewHolder = (QMUISwipeViewHolder) mSelected;
                        boolean isDownToAction = swipeViewHolder.checkDown(mInitialTouchX, mInitialTouchY);
                        if (!isDownToAction) {
                            if (hitTest(mSelected.itemView,
                                    mInitialTouchX, mInitialTouchY,
                                    mSelectedStartX + mDx, mSelectedStartY + mDy)) {
                                mInitialTouchX -= mDx;
                                mInitialTouchY -= mDy;
                            } else {
                                select(null);
                                return true;
                            }
                        } else {
                            mInitialTouchX -= mDx;
                            mInitialTouchY -= mDy;
                        }
                    }
                }
            } else if (action == MotionEvent.ACTION_CANCEL) {
                mActivePointerId = ACTIVE_POINTER_ID_NONE;
                mRecyclerView.removeCallbacks(mLongPressToSwipe);
                select(null);
            } else if (action == MotionEvent.ACTION_UP) {
                mRecyclerView.removeCallbacks(mLongPressToSwipe);
                handleActionUp(event.getX(), event.getY(), mSlop);
                mActivePointerId = ACTIVE_POINTER_ID_NONE;
            } else if (mActivePointerId != ACTIVE_POINTER_ID_NONE) {
                // in a non scroll orientation, if distance change is above threshold, we
                // can select the item
                final int index = event.findPointerIndex(mActivePointerId);
                if (DEBUG) {
                    Log.d(TAG, "pointer index " + index);
                }
                if (index >= 0) {
                    checkSelectForSwipe(action, event, index, false);
                }
            }
            if (mVelocityTracker != null) {
                mVelocityTracker.addMovement(event);
            }
            return mSelected != null;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent event) {
            if (DEBUG) {
                Log.d(TAG,
                        "on touch: x:" + mInitialTouchX + ",y:" + mInitialTouchY + ", :" + event);
            }
            if (mVelocityTracker != null) {
                mVelocityTracker.addMovement(event);
            }
            if (mActivePointerId == ACTIVE_POINTER_ID_NONE) {
                return;
            }
            final int action = event.getActionMasked();
            final int activePointerIndex = event.findPointerIndex(mActivePointerId);
            if (activePointerIndex >= 0) {
                checkSelectForSwipe(action, event, activePointerIndex, false);
            }
            RecyclerView.ViewHolder viewHolder = mSelected;
            if (viewHolder == null) {
                return;
            }
            switch (action) {
                case MotionEvent.ACTION_MOVE: {
                    // Find the index of the active pointer and fetch its position
                    if (activePointerIndex >= 0) {
                        updateDxDy(event, mSwipeDirection, activePointerIndex);
                        mRecyclerView.invalidate();

                        final float x = event.getX(activePointerIndex);
                        final float y = event.getY(activePointerIndex);
                        if (Math.abs(x - mInitialTouchX) > mSlop ||
                                Math.abs(y - mInitialTouchY) > mSlop) {
                            mRecyclerView.removeCallbacks(mLongPressToSwipe);
                        }
                    }
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                    mRecyclerView.removeCallbacks(mLongPressToSwipe);
                    select(null);
                    if (mVelocityTracker != null) {
                        mVelocityTracker.clear();
                    }
                    mActivePointerId = ACTIVE_POINTER_ID_NONE;
                    break;
                case MotionEvent.ACTION_UP:
                    mRecyclerView.removeCallbacks(mLongPressToSwipe);
                    handleActionUp(event.getX(), event.getY(), mSlop);
                    if (mVelocityTracker != null) {
                        mVelocityTracker.clear();
                    }
                    mActivePointerId = ACTIVE_POINTER_ID_NONE;
                    break;
                case MotionEvent.ACTION_POINTER_UP: {
                    final int pointerIndex = event.getActionIndex();
                    final int pointerId = event.getPointerId(pointerIndex);
                    if (pointerId == mActivePointerId) {
                        // This was our active pointer going up. Choose a new
                        // active pointer and adjust accordingly.
                        final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                        mActivePointerId = event.getPointerId(newPointerIndex);
                        updateDxDy(event, mSwipeDirection, pointerIndex);
                    }
                    break;
                }
            }
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            if (!disallowIntercept) {
                return;
            }
            select(null);
        }
    };

    private Callback mCallback;
    private boolean mSwipeDeleteWhenOnlyOneAction = false;

    public QMUIRVItemSwipeAction(boolean swipeDeleteWhenOnlyOneAction, Callback callback) {
        mCallback = callback;
        mSwipeDeleteWhenOnlyOneAction = swipeDeleteWhenOnlyOneAction;
    }

    /**
     * Attaches the ItemTouchHelper to the provided RecyclerView. If TouchHelper is already
     * attached to a RecyclerView, it will first detach from the previous one. You can call this
     * method with {@code null} to detach it from the current RecyclerView.
     *
     * @param recyclerView The RecyclerView instance to which you want to add this helper or
     *                     {@code null} if you want to remove ItemTouchHelper from the current
     *                     RecyclerView.
     */
    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) {
        if (mRecyclerView == recyclerView) {
            return; // nothing to do
        }
        if (mRecyclerView != null) {
            destroyCallbacks();
        }
        mRecyclerView = recyclerView;
        if (recyclerView != null) {
            final Resources resources = recyclerView.getResources();
            mSwipeEscapeVelocity = resources.getDimension(R.dimen.qmui_rv_swipe_action_escape_velocity);
            mMaxSwipeVelocity = resources.getDimension(R.dimen.qmui_rv_swipe_action_escape_max_velocity);
            setupCallbacks();
        }
    }

    public void setPressTimeToSwipe(long pressTimeToSwipe) {
        mPressTimeToSwipe = pressTimeToSwipe;
    }

    private void setupCallbacks() {
        ViewConfiguration vc = ViewConfiguration.get(mRecyclerView.getContext());
        mSlop = vc.getScaledTouchSlop();
        mRecyclerView.addItemDecoration(this);
        mRecyclerView.addOnItemTouchListener(mOnItemTouchListener);
        mRecyclerView.addOnChildAttachStateChangeListener(this);
    }

    private void destroyCallbacks() {
        mRecyclerView.removeItemDecoration(this);
        mRecyclerView.removeOnItemTouchListener(mOnItemTouchListener);
        mRecyclerView.removeOnChildAttachStateChangeListener(this);
        // clean all attached
        final int recoverAnimSize = mRecoverAnimations.size();
        for (int i = recoverAnimSize - 1; i >= 0; i--) {
            final RecoverAnimation recoverAnimation = mRecoverAnimations.get(0);
            mCallback.clearView(mRecyclerView, recoverAnimation.mViewHolder);
        }
        mRecoverAnimations.clear();
        releaseVelocityTracker();
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        float dx = 0, dy = 0;
        if (mSelected != null) {
            getSelectedDxDy(mTmpPosition);
            dx = mTmpPosition[0];
            dy = mTmpPosition[1];
        }
        mCallback.onDrawOver(c, parent, mSelected, mRecoverAnimations, dx, dy);
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        float dx = 0, dy = 0;
        if (mSelected != null) {
            getSelectedDxDy(mTmpPosition);
            dx = mTmpPosition[0];
            dy = mTmpPosition[1];
        }
        mCallback.onDraw(c, parent, mSelected, mRecoverAnimations, dx, dy, mSwipeDirection);
    }

    @Override
    public void onChildViewAttachedToWindow(@NonNull View view) {

    }

    @Override
    public void onChildViewDetachedFromWindow(@NonNull View view) {
        final RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(view);
        if (holder == null) {
            return;
        }
        if (mSelected != null && holder == mSelected) {
            select(null);
        } else {
            endRecoverAnimation(holder, false); // this may push it into pending cleanup list.
            if (mPendingCleanup.remove(holder.itemView)) {
                mCallback.clearView(mRecyclerView, holder);
            }
        }
    }

    void updateDxDy(MotionEvent ev, int swipeDirection, int pointerIndex) {
        final float x = ev.getX(pointerIndex);
        final float y = ev.getY(pointerIndex);

        // Calculate the distance moved
        if (swipeDirection == SWIPE_RIGHT) {
            mDx = Math.max(0, x - mInitialTouchX);
            mDy = 0;
        } else if (swipeDirection == SWIPE_LEFT) {
            mDx = Math.min(0, x - mInitialTouchX);
            mDy = 0;
        } else if (swipeDirection == SWIPE_DOWN) {
            mDx = 0;
            mDy = Math.max(0, y - mInitialTouchY);
        } else if (swipeDirection == SWIPE_UP) {
            mDx = 0;
            mDy = Math.min(0, y - mInitialTouchY);
        }
    }


    /**
     * Checks whether we should select a View for swiping.
     */
    void checkSelectForSwipe(int action, MotionEvent motionEvent, int pointerIndex, boolean isLongPressToSwipe) {
        if (mSelected != null || (mPressTimeToSwipe == SWIPE_TRIGGERED_IMMEDIATELY && action != MotionEvent.ACTION_MOVE)) {
            return;
        }

        if (mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING) {
            return;
        }

        final RecyclerView.ViewHolder vh = findSwipedView(motionEvent, isLongPressToSwipe);
        if (vh == null) {
            return;
        }

        int swipeDirection = mCallback.getSwipeDirection(mRecyclerView, vh);
        if (swipeDirection == SWIPE_NONE) {
            return;
        }

        if (mPressTimeToSwipe == SWIPE_TRIGGERED_IMMEDIATELY) {
            // mDx and mDy are only set in allowed directions. We use custom x/y here instead of
            // updateDxDy to avoid swiping if user moves more in the other direction
            final float x = motionEvent.getX(pointerIndex);
            final float y = motionEvent.getY(pointerIndex);

            // Calculate the distance moved
            final float dx = x - mInitialTouchX;
            final float dy = y - mInitialTouchY;
            // swipe target is chose w/o applying flags so it does not really check if swiping in that
            // direction is allowed. This why here, we use mDx mDy to check slope value again.
            final float absDx = Math.abs(dx);
            final float absDy = Math.abs(dy);

            if (swipeDirection == SWIPE_LEFT) {
                if (absDx < mSlop || dx >= 0) {
                    return;
                }
            } else if (swipeDirection == SWIPE_RIGHT) {
                if (absDx < mSlop || dx <= 0) {
                    return;
                }
            } else if (swipeDirection == SWIPE_UP) {
                if (absDy < mSlop || dy >= 0) {
                    return;
                }
            } else if (swipeDirection == SWIPE_DOWN) {
                if (absDy < mSlop || dy <= 0) {
                    return;
                }
            }
        } else {
            if (mPressTimeToSwipe >= System.currentTimeMillis() - mDownTimeMillis) {
                return;
            }
        }

        mRecyclerView.removeCallbacks(mLongPressToSwipe);
        mDx = mDy = 0f;
        mActivePointerId = motionEvent.getPointerId(0);
        MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
        cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
        vh.itemView.dispatchTouchEvent(cancelEvent);
        cancelEvent.recycle();
        select(vh);
    }

    public void clear() {
        select(null, false);
    }

    void handleActionUp(float x, float y, int touchSlop) {
        if (mSelected != null) {
            if (mSelected instanceof QMUISwipeViewHolder) {
                QMUISwipeViewHolder swipeViewHolder = (QMUISwipeViewHolder) mSelected;
                if (!swipeViewHolder.hasAction()) {
                    select(null, true);
                } else if(swipeViewHolder.mSwipeActions.size() == 1 && mSwipeDeleteWhenOnlyOneAction){
                    if(mCallback.isOverThreshold(mRecyclerView, mSelected, mDx, mDy, mSwipeDirection)){
                        select(null, true);
                    }else{
                        handleSwipeActionActionUp(swipeViewHolder, x, y, touchSlop);
                    }
                } else {
                    handleSwipeActionActionUp(swipeViewHolder, x, y, touchSlop);
                }
            } else {
                select(null, true);
            }
        }
    }

    void handleSwipeActionActionUp( QMUISwipeViewHolder swipeViewHolder, float x, float y, int touchSlop){
        QMUISwipeAction action = swipeViewHolder.checkUp(x, y, touchSlop);
        if (action != null) {
            mCallback.onClickAction(this, mSelected, action);
            swipeViewHolder.clearTouchInfo();
            return;
        }
        swipeViewHolder.clearTouchInfo();
        final int swipeDir = checkSwipe(mSelected, mSwipeDirection, true);
        if (swipeDir == SWIPE_NONE) {
            select(null, true);
        } else {
            getSelectedDxDy(mTmpPosition);
            final float currentTranslateX = mTmpPosition[0];
            final float currentTranslateY = mTmpPosition[1];
            final float targetTranslateX, targetTranslateY;
            switch (swipeDir) {
                case SWIPE_LEFT:
                    targetTranslateY = 0;
                    targetTranslateX = -swipeViewHolder.mActionTotalWidth;
                    break;
                case SWIPE_RIGHT:
                    targetTranslateY = 0;
                    targetTranslateX = swipeViewHolder.mActionTotalWidth;
                    break;
                case SWIPE_UP:
                    targetTranslateX = 0;
                    targetTranslateY = -swipeViewHolder.mActionTotalHeight;
                    break;
                case SWIPE_DOWN:
                    targetTranslateX = 0;
                    targetTranslateY = swipeViewHolder.mActionTotalHeight;
                    break;
                default:
                    targetTranslateX = 0;
                    targetTranslateY = 0;
            }

            mDx += targetTranslateX - currentTranslateX;
            mDy += targetTranslateY - currentTranslateY;
            final RecoverAnimation rv = new RecoverAnimation(swipeViewHolder,
                    currentTranslateX, currentTranslateY,
                    targetTranslateX, targetTranslateY,
                    mCallback.getInterpolator(ANIMATION_TYPE_SWIPE_ACTION));
            final long duration = mCallback.getAnimationDuration(mRecyclerView,
                    ANIMATION_TYPE_SWIPE_ACTION,
                    targetTranslateX - currentTranslateX,
                    targetTranslateY - currentTranslateY);
            rv.setDuration(duration);
            mRecoverAnimations.add(rv);
            rv.start();
            mRecyclerView.invalidate();
        }
    }

    void select(@Nullable RecyclerView.ViewHolder selected) {
        select(selected, false);
    }

    void select(@Nullable RecyclerView.ViewHolder selected, boolean isActionUp) {
        if (selected == mSelected) {
            return;
        }
        // prevent duplicate animations
        endRecoverAnimation(selected, true);

        boolean preventLayout = false;

        if (mSelected != null) {
            final RecyclerView.ViewHolder prevSelected = mSelected;
            if (prevSelected.itemView.getParent() != null) {
                endRecoverAnimation(prevSelected, true);
                final int swipeDir = isActionUp ? checkSwipe(mSelected, mSwipeDirection, false) : SWIPE_NONE;
                getSelectedDxDy(mTmpPosition);
                final float currentTranslateX = mTmpPosition[0];
                final float currentTranslateY = mTmpPosition[1];
                final float targetTranslateX, targetTranslateY;
                switch (swipeDir) {
                    case SWIPE_LEFT:
                    case SWIPE_RIGHT:
                        targetTranslateY = 0;
                        targetTranslateX = Math.signum(mDx) * mRecyclerView.getWidth();
                        break;
                    case SWIPE_UP:
                    case SWIPE_DOWN:
                        targetTranslateX = 0;
                        targetTranslateY = Math.signum(mDy) * mRecyclerView.getHeight();
                        break;
                    default:
                        targetTranslateX = 0;
                        targetTranslateY = 0;
                }

                final int animType = swipeDir > 0 ? ANIMATION_TYPE_SWIPE_SUCCESS : ANIMATION_TYPE_SWIPE_CANCEL;
                if (swipeDir > 0) {
                    mCallback.onStartSwipeAnimation(mSelected, swipeDir);
                }
                final RecoverAnimation rv = new RecoverAnimation(prevSelected,
                        currentTranslateX, currentTranslateY,
                        targetTranslateX, targetTranslateY,
                        mCallback.getInterpolator(ANIMATION_TYPE_SWIPE_ACTION)) {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (this.mOverridden) {
                            return;
                        }
                        if (swipeDir == SWIPE_NONE) {
                            // this is a drag or failed swipe. recover immediately
                            mCallback.clearView(mRecyclerView, prevSelected);
                            // full cleanup will happen on onDrawOver
                        } else {
                            // wait until remove animation is complete.
                            mPendingCleanup.add(prevSelected.itemView);
                            mIsPendingCleanup = true;
                            if (swipeDir > 0) {
                                // Animation might be ended by other animators during a layout.
                                // We defer callback to avoid editing adapter during a layout.
                                postDispatchSwipe(this, swipeDir);
                            }
                        }
                    }
                };
                final long duration = mCallback.getAnimationDuration(mRecyclerView, animType,
                        targetTranslateX - currentTranslateX,
                        targetTranslateY - currentTranslateY);
                rv.setDuration(duration);
                mRecoverAnimations.add(rv);
                rv.start();
                preventLayout = true;
            } else {
                mCallback.clearView(mRecyclerView, prevSelected);
            }
            mSelected = null;
        }
        if (selected != null) {
            mSwipeDirection = mCallback.getSwipeDirection(mRecyclerView, selected);
            mSelectedStartX = selected.itemView.getLeft();
            mSelectedStartY = selected.itemView.getTop();
            mSelected = selected;
            if (selected instanceof QMUISwipeViewHolder) {
                QMUISwipeViewHolder qmuiSwipeViewHolder = (QMUISwipeViewHolder) selected;
                qmuiSwipeViewHolder.setup(mSwipeDirection, mSwipeDeleteWhenOnlyOneAction);
            }
        }
        final ViewParent rvParent = mRecyclerView.getParent();
        if (rvParent != null) {
            rvParent.requestDisallowInterceptTouchEvent(mSelected != null);
        }
        if (!preventLayout) {
            mRecyclerView.getLayoutManager().requestSimpleAnimationsInNextLayout();
        }
        mCallback.onSelectedChanged(mSelected);
        mRecyclerView.invalidate();
    }

    private void getSelectedDxDy(float[] outPosition) {
        if (mSwipeDirection == SWIPE_LEFT || mSwipeDirection == SWIPE_RIGHT) {
            outPosition[0] = mSelectedStartX + mDx - mSelected.itemView.getLeft();
        } else {
            outPosition[0] = mSelected.itemView.getTranslationX();
        }
        if (mSwipeDirection == SWIPE_UP || mSwipeDirection == SWIPE_DOWN) {
            outPosition[1] = mSelectedStartY + mDy - mSelected.itemView.getTop();
        } else {
            outPosition[1] = mSelected.itemView.getTranslationY();
        }
    }

    void postDispatchSwipe(final RecoverAnimation anim, final int swipeDir) {
        // wait until animations are complete.
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (mRecyclerView != null && mRecyclerView.isAttachedToWindow()
                        && !anim.mOverridden
                        && anim.mViewHolder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                    final RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();
                    // if animator is running or we have other active recover animations, we try
                    // not to call onSwiped because DefaultItemAnimator is not good at merging
                    // animations. Instead, we wait and batch.
                    if ((animator == null || !animator.isRunning(null))
                            && !hasRunningRecoverAnim()) {
                        mCallback.onSwiped(anim.mViewHolder, swipeDir);
                    } else {
                        mRecyclerView.post(this);
                    }
                }
            }
        });
    }

    boolean hasRunningRecoverAnim() {
        final int size = mRecoverAnimations.size();
        for (int i = 0; i < size; i++) {
            if (!mRecoverAnimations.get(i).mEnded) {
                return true;
            }
        }
        return false;
    }

    private int checkSwipe(RecyclerView.ViewHolder viewHolder, int swipeDirection, boolean checkAction) {
        if (swipeDirection == SWIPE_LEFT || swipeDirection == SWIPE_RIGHT) {
            final int dirFlag = mDx > 0 ? SWIPE_RIGHT : SWIPE_LEFT;
            if (mVelocityTracker != null && mActivePointerId > -1) {
                mVelocityTracker.computeCurrentVelocity(PIXELS_PER_SECOND,
                        mCallback.getSwipeVelocityThreshold(mMaxSwipeVelocity));
                final float xVelocity = mVelocityTracker.getXVelocity(mActivePointerId);
                final int velDirFlag = xVelocity > 0f ? SWIPE_RIGHT : SWIPE_LEFT;
                final float absXVelocity = Math.abs(xVelocity);
                if (dirFlag == velDirFlag &&
                        absXVelocity >= mCallback.getSwipeEscapeVelocity(mSwipeEscapeVelocity)) {
                    return velDirFlag;
                }
            }

            float threshold;
            if (checkAction && viewHolder instanceof QMUISwipeViewHolder) {
                threshold = ((QMUISwipeViewHolder) viewHolder).mActionTotalWidth;
            } else {
                threshold = mRecyclerView.getWidth() * mCallback.getSwipeThreshold(viewHolder);
            }

            if (Math.abs(mDx) >= threshold) {
                return dirFlag;
            }
        } else if (swipeDirection == SWIPE_UP || swipeDirection == SWIPE_DOWN) {
            final int dirFlag = mDy > 0 ? SWIPE_DOWN : SWIPE_UP;
            if (mVelocityTracker != null && mActivePointerId > -1) {
                mVelocityTracker.computeCurrentVelocity(PIXELS_PER_SECOND,
                        mCallback.getSwipeVelocityThreshold(mMaxSwipeVelocity));
                final float yVelocity = mVelocityTracker.getYVelocity(mActivePointerId);
                final int velDirFlag = yVelocity > 0f ? SWIPE_DOWN : SWIPE_UP;
                final float absYVelocity = Math.abs(yVelocity);
                if (velDirFlag == dirFlag &&
                        absYVelocity >= mCallback.getSwipeEscapeVelocity(mSwipeEscapeVelocity)) {
                    return velDirFlag;
                }
            }

            float threshold;
            if (checkAction && viewHolder instanceof QMUISwipeViewHolder) {
                threshold = ((QMUISwipeViewHolder) viewHolder).mActionTotalHeight;
            } else {
                threshold = mRecyclerView.getHeight() * mCallback.getSwipeThreshold(viewHolder);
            }
            if (Math.abs(mDy) >= threshold) {
                return dirFlag;
            }
        }
        return SWIPE_NONE;
    }

    @Nullable
    private RecyclerView.ViewHolder findSwipedView(MotionEvent motionEvent, boolean isLongPressToSwipe) {
        final RecyclerView.LayoutManager lm = mRecyclerView.getLayoutManager();
        if (mActivePointerId == ACTIVE_POINTER_ID_NONE || lm == null) {
            return null;
        }
        if (isLongPressToSwipe) {
            View child = findChildView(motionEvent);
            if (child == null) {
                return null;
            }
            return mRecyclerView.getChildViewHolder(child);
        }
        final int pointerIndex = motionEvent.findPointerIndex(mActivePointerId);
        final float dx = motionEvent.getX(pointerIndex) - mInitialTouchX;
        final float dy = motionEvent.getY(pointerIndex) - mInitialTouchY;
        final float absDx = Math.abs(dx);
        final float absDy = Math.abs(dy);

        if (absDx < mSlop && absDy < mSlop) {
            return null;
        }
        if (absDx > absDy && lm.canScrollHorizontally()) {
            return null;
        } else if (absDy > absDx && lm.canScrollVertically()) {
            return null;
        }
        View child = findChildView(motionEvent);
        if (child == null) {
            return null;
        }
        return mRecyclerView.getChildViewHolder(child);
    }

    void endRecoverAnimation(RecyclerView.ViewHolder viewHolder, boolean override) {
        final int recoverAnimSize = mRecoverAnimations.size();
        for (int i = recoverAnimSize - 1; i >= 0; i--) {
            final RecoverAnimation anim = mRecoverAnimations.get(i);
            if (anim.mViewHolder == viewHolder) {
                anim.mOverridden |= override;
                if (!anim.mEnded) {
                    anim.cancel();
                }
                mRecoverAnimations.remove(i);
                return;
            }
        }
    }

    View findChildView(MotionEvent event) {
        // first check elevated views, if none, then call RV
        final float x = event.getX();
        final float y = event.getY();
        if (mSelected != null) {
            final View selectedView = mSelected.itemView;
            if (hitTest(selectedView, x, y, mSelectedStartX + mDx, mSelectedStartY + mDy)) {
                return selectedView;
            }
        }
        for (int i = mRecoverAnimations.size() - 1; i >= 0; i--) {
            final RecoverAnimation anim = mRecoverAnimations.get(i);
            final View view = anim.mViewHolder.itemView;
            if (hitTest(view, x, y, anim.mX, anim.mY)) {
                return view;
            }
        }
        return mRecyclerView.findChildViewUnder(x, y);
    }

    @Nullable
    RecoverAnimation findAnimation(MotionEvent event) {
        if (mRecoverAnimations.isEmpty()) {
            return null;
        }
        View target = findChildView(event);
        for (int i = mRecoverAnimations.size() - 1; i >= 0; i--) {
            final RecoverAnimation anim = mRecoverAnimations.get(i);
            if (anim.mViewHolder.itemView == target) {
                return anim;
            }
        }
        return null;
    }

    void obtainVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
        }
        mVelocityTracker = VelocityTracker.obtain();
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private static boolean hitTest(View child, float x, float y, float left, float top) {
        return x >= left
                && x <= left + child.getWidth()
                && y >= top
                && y <= top + child.getHeight();
    }

    private static class RecoverAnimation implements Animator.AnimatorListener {

        final float mStartDx;

        final float mStartDy;

        final float mTargetX;

        final float mTargetY;

        final RecyclerView.ViewHolder mViewHolder;

        private final ValueAnimator mValueAnimator;

        boolean mIsPendingCleanup;

        float mX;

        float mY;

        // if user starts touching a recovering view, we put it into interaction mode again,
        // instantly.
        boolean mOverridden = false;

        boolean mEnded = false;

        private float mFraction;

        RecoverAnimation(RecyclerView.ViewHolder viewHolder,
                         float startDx, float startDy, float targetX, float targetY,
                         TimeInterpolator interpolator) {
            mViewHolder = viewHolder;
            mStartDx = startDx;
            mStartDy = startDy;
            mTargetX = targetX;
            mTargetY = targetY;
            mValueAnimator = ValueAnimator.ofFloat(0f, 1f);
            mValueAnimator.addUpdateListener(
                    new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            setFraction(animation.getAnimatedFraction());
                        }
                    });
            mValueAnimator.setTarget(viewHolder.itemView);
            mValueAnimator.addListener(this);
            mValueAnimator.setInterpolator(interpolator);
            setFraction(0f);
        }

        public void setDuration(long duration) {
            mValueAnimator.setDuration(duration);
        }

        public void start() {
            mViewHolder.setIsRecyclable(false);
            mValueAnimator.start();
        }

        public void cancel() {
            mValueAnimator.cancel();
        }

        public void setFraction(float fraction) {
            mFraction = fraction;
        }

        /**
         * We run updates on onDraw method but use the fraction from animator callback.
         * This way, we can sync translate x/y values w/ the animators to avoid one-off frames.
         */
        public void update() {
            if (mStartDx == mTargetX) {
                mX = mViewHolder.itemView.getTranslationX();
            } else {
                mX = mStartDx + mFraction * (mTargetX - mStartDx);
            }
            if (mStartDy == mTargetY) {
                mY = mViewHolder.itemView.getTranslationY();
            } else {
                mY = mStartDy + mFraction * (mTargetY - mStartDy);
            }
        }

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (!mEnded) {
                mViewHolder.setIsRecyclable(true);
            }
            mEnded = true;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            setFraction(1f); //make sure we recover the view's state.
        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }

    public static abstract class Callback {
        public static final int DEFAULT_SWIPE_ANIMATION_DURATION = 250;

        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            View view = viewHolder.itemView;
            view.setTranslationX(0);
            view.setTranslationY(0);
            if (viewHolder instanceof QMUISwipeViewHolder) {
                ((QMUISwipeViewHolder) viewHolder).clearTouchInfo();
            }
        }

        public int getSwipeDirection(@NonNull RecyclerView recyclerView,
                                     @NonNull RecyclerView.ViewHolder viewHolder) {
            return SWIPE_NONE;
        }

        public void onStartSwipeAnimation(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }

        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }


        public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
            return .5f;
        }

        public float getSwipeEscapeVelocity(float defaultValue) {
            return defaultValue;
        }


        public float getSwipeVelocityThreshold(float defaultValue) {
            return defaultValue;
        }


        public long getAnimationDuration(@NonNull RecyclerView recyclerView, int animationType,
                                         float animateDx, float animateDy) {
            return DEFAULT_SWIPE_ANIMATION_DURATION;
        }

        public void onSelectedChanged(RecyclerView.ViewHolder selected) {

        }

        public void onClickAction(QMUIRVItemSwipeAction swipeAction, RecyclerView.ViewHolder selected, QMUISwipeAction action) {

        }

        public TimeInterpolator getInterpolator(int animationType) {
            return null;
        }

        void onDraw(Canvas c, RecyclerView parent, RecyclerView.ViewHolder selected,
                    List<RecoverAnimation> recoverAnimationList, float dX, float dY, int swipeDirection) {
            final int recoverAnimSize = recoverAnimationList.size();
            for (int i = 0; i < recoverAnimSize; i++) {
                final RecoverAnimation anim = recoverAnimationList.get(i);
                anim.update();
                if (anim.mViewHolder == selected) {
                    dX = anim.mX;
                    dY = anim.mY;
                } else {
                    final int count = c.save();
                    onChildDraw(c, parent, anim.mViewHolder, anim.mX, anim.mY, false, swipeDirection);
                    c.restoreToCount(count);
                }

            }
            if (selected != null) {
                final int count = c.save();
                onChildDraw(c, parent, selected, dX, dY, true, swipeDirection);
                c.restoreToCount(count);
            }
        }

        void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.ViewHolder selected,
                        List<RecoverAnimation> recoverAnimationList, float dX, float dY) {
            final int recoverAnimSize = recoverAnimationList.size();
            for (int i = 0; i < recoverAnimSize; i++) {
                final RecoverAnimation anim = recoverAnimationList.get(i);
                final int count = c.save();
                onChildDrawOver(c, parent, anim.mViewHolder, anim.mX, anim.mY, false);
                c.restoreToCount(count);
            }
            if (selected != null) {
                final int count = c.save();
                onChildDrawOver(c, parent, selected, dX, dY, true);
                c.restoreToCount(count);
            }
            boolean hasRunningAnimation = false;
            for (int i = recoverAnimSize - 1; i >= 0; i--) {
                final RecoverAnimation anim = recoverAnimationList.get(i);
                if (anim.mEnded && !anim.mIsPendingCleanup) {
                    recoverAnimationList.remove(i);
                } else if (!anim.mEnded) {
                    hasRunningAnimation = true;
                }
            }
            if (hasRunningAnimation) {
                parent.invalidate();
            }
        }

        public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    boolean isCurrentlyActive) {
        }

        protected boolean isOverThreshold(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dx, float dy, int swipeDirection) {
            if (swipeDirection == SWIPE_LEFT || swipeDirection == SWIPE_RIGHT) {
                return Math.abs(dx) >= recyclerView.getWidth() * getSwipeThreshold(viewHolder);
            }
            return Math.abs(dy) >= recyclerView.getHeight() * getSwipeThreshold(viewHolder);
        }

        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                boolean isCurrentlyActive, int swipeDirection) {
            View view = viewHolder.itemView;
            view.setTranslationX(dX);
            view.setTranslationY(dY);
            if (viewHolder instanceof QMUISwipeViewHolder) {
                if (swipeDirection != SWIPE_NONE) {
                    ((QMUISwipeViewHolder) viewHolder).draw(c, isOverThreshold(recyclerView, viewHolder, dX, dY, swipeDirection), dX, dY);
                }
            }
        }
    }
}
