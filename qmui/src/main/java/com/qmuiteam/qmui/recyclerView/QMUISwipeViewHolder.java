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

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qmuiteam.qmui.util.QMUIViewHelper;

import java.util.ArrayList;
import java.util.List;

import static com.qmuiteam.qmui.recyclerView.QMUIRVItemSwipeAction.SWIPE_DOWN;
import static com.qmuiteam.qmui.recyclerView.QMUIRVItemSwipeAction.SWIPE_LEFT;
import static com.qmuiteam.qmui.recyclerView.QMUIRVItemSwipeAction.SWIPE_NONE;
import static com.qmuiteam.qmui.recyclerView.QMUIRVItemSwipeAction.SWIPE_RIGHT;
import static com.qmuiteam.qmui.recyclerView.QMUIRVItemSwipeAction.SWIPE_UP;

public class QMUISwipeViewHolder extends RecyclerView.ViewHolder {

    List<ActionWrapper> mSwipeActions;
    int mActionTotalWidth = 0;
    int mActionTotalHeight = 0;
    int mSetupDirection = SWIPE_NONE;
    ActionWrapper mCurrentTouchAction = null;
    float mActionDownX = 0;
    float mActionDownY = 0;
    private QMUISwipeViewHolder.ActionWrapper.Callback mCallback = new ActionWrapper.Callback() {
        @Override
        public void invalidate() {
            ViewParent viewParent = itemView.getParent();
            if (viewParent instanceof RecyclerView) {
                ((RecyclerView) viewParent).invalidate();
            }
        }
    };

    public QMUISwipeViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void addSwipeAction(QMUISwipeAction action) {
        if (mSwipeActions == null) {
            mSwipeActions = new ArrayList<>();
        }
        ActionWrapper actionWrapper = new ActionWrapper(action, mCallback);
        mSwipeActions.add(actionWrapper);
    }

    public boolean hasAction() {
        return mSwipeActions != null && !mSwipeActions.isEmpty();
    }

    public void clearTouchInfo() {
        mCurrentTouchAction = null;
        mActionDownY = -1;
        mActionDownX = -1;
    }

    void setup(int swipeDirection, boolean swipeDeleteIfOnlyOneAction) {
        mActionTotalWidth = 0;
        mActionTotalHeight = 0;
        if (mSwipeActions == null || mSwipeActions.isEmpty()) {
            return;
        }
        mSetupDirection = swipeDirection;
        for (ActionWrapper wrapper : mSwipeActions) {
            QMUISwipeAction action = wrapper.action;
            if (swipeDirection == SWIPE_LEFT || swipeDirection == SWIPE_RIGHT) {
                wrapper.measureWidth = Math.max(action.mSwipeDirectionMiniSize,
                        action.contentWidth + 2 * action.mPaddingStartEnd);
                wrapper.measureHeight = itemView.getHeight();
                mActionTotalWidth += wrapper.measureWidth;
            } else if (swipeDirection == SWIPE_UP || swipeDirection == SWIPE_DOWN) {
                wrapper.measureHeight = Math.max(action.mSwipeDirectionMiniSize,
                        action.contentHeight + 2 * action.mPaddingStartEnd);
                wrapper.measureWidth = itemView.getWidth();
                mActionTotalHeight += wrapper.measureHeight;
            }
        }

        if (mSwipeActions.size() == 1 && swipeDeleteIfOnlyOneAction) {
            mSwipeActions.get(0).swipeDeleteMode = true;
        } else {
            for (ActionWrapper wrapper : mSwipeActions) {
                wrapper.swipeDeleteMode = false;
            }
        }

        if (swipeDirection == SWIPE_LEFT) {
            int targetLeft = itemView.getRight() - mActionTotalWidth;
            for (ActionWrapper wrapper : mSwipeActions) {
                wrapper.initLeft = itemView.getRight();
                wrapper.initTop = wrapper.targetTop = itemView.getTop();
                wrapper.targetLeft = targetLeft;
                targetLeft += wrapper.measureWidth;
            }
        } else if (swipeDirection == SWIPE_RIGHT) {
            int targetLeft = 0;
            for (ActionWrapper wrapper : mSwipeActions) {
                wrapper.initLeft = itemView.getLeft() - wrapper.measureWidth;
                wrapper.initTop = wrapper.targetTop = itemView.getTop();
                wrapper.targetLeft = targetLeft;
                targetLeft += wrapper.measureWidth;
            }
        } else if (swipeDirection == SWIPE_UP) {
            int targetTop = itemView.getBottom() - mActionTotalHeight;
            for (ActionWrapper wrapper : mSwipeActions) {
                wrapper.initLeft = wrapper.targetLeft = itemView.getLeft();
                wrapper.initTop = itemView.getBottom();
                wrapper.targetTop = targetTop;
                targetTop += wrapper.measureHeight;
            }
        } else if (swipeDirection == SWIPE_DOWN) {
            int targetTop = 0;
            for (ActionWrapper wrapper : mSwipeActions) {
                wrapper.initLeft = wrapper.targetLeft = itemView.getLeft();
                wrapper.initTop = itemView.getTop() - wrapper.measureHeight;
                wrapper.targetTop = targetTop;
                targetTop += wrapper.measureHeight;
            }
        }
    }

    boolean checkDown(float x, float y) {
        for (ActionWrapper actionInfo : mSwipeActions) {
            if (actionInfo.hitTest(x, y)) {
                mCurrentTouchAction = actionInfo;
                mActionDownX = x;
                mActionDownY = y;
                return true;
            }
        }
        return false;
    }

    QMUISwipeAction checkUp(float x, float y, int touchSlop) {
        if (mCurrentTouchAction != null && mCurrentTouchAction.hitTest(x, y)) {
            if (Math.abs(x - mActionDownX) < touchSlop && Math.abs(y - mActionDownY) < touchSlop) {
                return mCurrentTouchAction.action;
            }
        }
        return null;
    }

    void draw(Canvas canvas, boolean overSwipeThreshold, float dx, float dy) {
        if (mSwipeActions == null || mSwipeActions.isEmpty()) {
            return;
        }
        if (mActionTotalWidth > 0) {
            float absDx = Math.abs(dx);
            if (absDx <= mActionTotalWidth) {
                float percent = absDx / mActionTotalWidth;
                for (ActionWrapper actionInfo : mSwipeActions) {
                    actionInfo.width = actionInfo.measureWidth;
                    actionInfo.left = actionInfo.initLeft + (actionInfo.targetLeft - actionInfo.initLeft) * percent;
                }
            } else {
                float overDx = absDx - mActionTotalWidth;
                float eachOver = overDx / mSwipeActions.size();
                float startLeft = dx > 0 ? itemView.getLeft() : itemView.getRight() + dx;
                for (ActionWrapper actionInfo : mSwipeActions) {
                    actionInfo.width = actionInfo.measureWidth + eachOver;
                    actionInfo.left = startLeft;
                    startLeft += actionInfo.width;
                }
            }
        } else {
            for (ActionWrapper actionInfo : mSwipeActions) {
                actionInfo.width = actionInfo.measureWidth;
                actionInfo.left = actionInfo.initLeft;
            }
        }
        if (mActionTotalHeight > 0) {
            float absDy = Math.abs(dy);
            if (absDy <= mActionTotalHeight) {
                float percent = absDy / mActionTotalHeight;
                for (ActionWrapper actionInfo : mSwipeActions) {
                    actionInfo.height = actionInfo.measureHeight;
                    actionInfo.top = actionInfo.initTop + (actionInfo.targetTop - actionInfo.initTop) * percent;
                }
            } else {
                float overDy = absDy - mActionTotalHeight;
                float eachOver = overDy / mSwipeActions.size();
                float startTop = dy > 0 ? itemView.getTop() : itemView.getBottom() + dy;
                for (ActionWrapper actionInfo : mSwipeActions) {
                    actionInfo.height = actionInfo.measureHeight + eachOver + 0.5f;
                    actionInfo.top = startTop;
                    startTop += actionInfo.height;
                }
            }
        } else {
            for (ActionWrapper actionInfo : mSwipeActions) {
                actionInfo.height = actionInfo.measureHeight;
                actionInfo.top = actionInfo.initTop;
            }
        }
        for (ActionWrapper actionInfo : mSwipeActions) {
            actionInfo.draw(canvas, overSwipeThreshold, mSetupDirection);
        }
    }

    static class ActionWrapper {
        static int SWIPE_DELETE_BEFORE = 0;
        static int SWIPE_DELETE_ANIMATING_TO_AFTER = 1;
        static int SWIPE_DELETE_ANIMATING_TO_BEFORE = 2;
        static int SWIPE_DELETE_AFTER = 3;
        static int MAX_SWIPE_MOVE_DURATION = 250;
        final QMUISwipeAction action;
        final Callback callback;

        float measureWidth;
        float measureHeight;
        float targetLeft;
        float targetTop;
        float initLeft;
        float initTop;
        float left;
        float top;
        float width;
        float height;

        boolean swipeDeleteMode = false;
        private int swipeDeleteState = SWIPE_DELETE_BEFORE;
        private float currentAnimationProgress = 0;
        private ValueAnimator animator;
        private ValueAnimator.AnimatorUpdateListener listener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentAnimationProgress = (float) animation.getAnimatedValue();
                callback.invalidate();
            }
        };
        private float lastLeft = -1, lastTop = -1, animStartLeft = -1, animStartTop = -1;

        public ActionWrapper(@NonNull QMUISwipeAction action, @NonNull Callback callback) {
            this.action = action;
            this.callback = callback;
        }

        boolean hitTest(float x, float y) {
            return x > left && x < left + width && y > top && y < top + height;
        }

        void draw(Canvas canvas, boolean overSwipeThreshold, int direction) {
            canvas.save();
            canvas.translate(left, top);
            action.paint.setStyle(Paint.Style.FILL);
            action.paint.setColor(action.mBackgroundColor);
            canvas.drawRect(0, 0, width, height, action.paint);
            if (!swipeDeleteMode) {
                canvas.translate((width - action.contentWidth) / 2f, (height - action.contentHeight) / 2);
            } else {
                float anchorLeft = getAnchorDrawLeft(direction);
                float anchorTop = getAnchorDrawTop(direction);
                float followLeft = getFollowDrawLeft(direction);
                float followTop = getFollowDrawTop(direction);
                float drawLeft, drawTop;
                if (!overSwipeThreshold) {
                    if (swipeDeleteState == SWIPE_DELETE_BEFORE) {
                        drawLeft = anchorLeft;
                        drawTop = anchorTop;
                    } else if (swipeDeleteState == SWIPE_DELETE_AFTER) {
                        swipeDeleteState = SWIPE_DELETE_ANIMATING_TO_BEFORE;
                        drawLeft = followLeft;
                        drawTop = followTop;
                        startAnimator(drawLeft, drawTop, anchorLeft, anchorTop, direction);
                    } else if (swipeDeleteState == SWIPE_DELETE_ANIMATING_TO_AFTER) {
                        swipeDeleteState = SWIPE_DELETE_ANIMATING_TO_BEFORE;
                        drawLeft = lastLeft;
                        drawTop = lastTop;
                        startAnimator(drawLeft, drawTop, anchorLeft, anchorTop, direction);
                    } else {
                        if (isVer(direction)) {
                            drawLeft = anchorLeft;
                            drawTop = animStartTop + (anchorTop - animStartTop) * currentAnimationProgress;
                        } else {
                            drawLeft = animStartLeft + (anchorLeft - animStartLeft) * currentAnimationProgress;
                            drawTop = anchorTop;
                        }
                        if (currentAnimationProgress >= 1f) {
                            swipeDeleteState = SWIPE_DELETE_BEFORE;
                        }
                    }
                } else {
                    if (swipeDeleteState == SWIPE_DELETE_AFTER) {
                        drawLeft = followLeft;
                        drawTop = followTop;
                    } else if (swipeDeleteState == SWIPE_DELETE_ANIMATING_TO_BEFORE) {
                        swipeDeleteState = SWIPE_DELETE_ANIMATING_TO_AFTER;
                        drawLeft = lastLeft;
                        drawTop = lastTop;
                        startAnimator(drawLeft, drawTop, followLeft, followTop, direction);
                    } else if (swipeDeleteState == SWIPE_DELETE_BEFORE) {
                        swipeDeleteState = SWIPE_DELETE_ANIMATING_TO_AFTER;
                        drawLeft = anchorLeft;
                        drawTop = anchorTop;
                        startAnimator(drawLeft, drawTop, followLeft, followTop, direction);
                    } else {
                        if (isVer(direction)) {
                            drawLeft = followLeft;
                            drawTop = animStartTop + (followTop - animStartTop) * currentAnimationProgress;
                        } else {
                            drawLeft = animStartLeft + (followLeft - animStartLeft) * currentAnimationProgress;
                            drawTop = followTop;
                        }
                        if (currentAnimationProgress >= 1f) {
                            swipeDeleteState = SWIPE_DELETE_AFTER;
                        }
                    }
                }
                canvas.translate(drawLeft - left, drawTop - top);
                lastLeft = drawLeft;
                lastTop = drawTop;
            }
            action.paint.setColor(action.mTextColor);
            action.draw(canvas);
            canvas.restore();
        }

        private void startAnimator(float curLeft, float curTop, float targetLeft, float targetTop, int direction) {
            QMUIViewHelper.clearValueAnimator(animator);
            if (isVer(direction)) {
                animator = ValueAnimator.ofFloat(0, 1);
                animStartTop = curTop;
            } else {
                animator = ValueAnimator.ofFloat(0, 1);
                animStartLeft = curLeft;
            }
            float dis = isVer(direction) ? Math.abs(targetTop - curTop) : Math.abs(targetLeft - curLeft);
            int duration = Math.min(MAX_SWIPE_MOVE_DURATION, (int) (dis / action.mSwipePxPerMS));
            animator.setDuration(duration);
            animator.setInterpolator(action.mSwipeMoveInterpolator);
            animator.addUpdateListener(listener);
            animator.start();
        }

        private boolean isVer(int direction) {
            return direction == SWIPE_DOWN || direction == SWIPE_UP;
        }

        private float getAnchorDrawLeft(int direction) {
            if(direction == SWIPE_LEFT){
                if(left > targetLeft){
                    return getFollowDrawLeft(direction);
                }
            }else if(direction == SWIPE_RIGHT){
                if(left < targetLeft){
                    return getFollowDrawLeft(direction);
                }
            }
            return targetLeft + (measureWidth - action.contentWidth) / 2;
        }

        private float getAnchorDrawTop(int direction) {
            if(direction == SWIPE_UP){
                if(top > targetTop){
                    return getFollowDrawTop(direction);
                }
            }else if(direction == SWIPE_DOWN){
                if(top < targetTop){
                    return getFollowDrawTop(direction);
                }
            }
            return targetTop + (measureHeight - action.contentHeight) / 2;
        }

        private float getFollowDrawLeft(int direction) {
            float innerHor = (measureWidth - action.contentWidth) / 2;
            if (direction == SWIPE_LEFT) {
                return left + innerHor;
            } else if (direction == SWIPE_RIGHT) {
                return left + width - measureWidth + innerHor;
            }
            return left + (width - action.contentWidth) / 2f;
        }

        private float getFollowDrawTop(int direction) {
            float innerVer = (measureHeight - action.contentHeight) / 2;
            if (direction == SWIPE_UP) {
                return top + innerVer;
            } else if (direction == SWIPE_DOWN) {
                return top + height - measureHeight + innerVer;
            }
            return top + (height - action.contentHeight) / 2f;
        }

        interface Callback {
            void invalidate();
        }
    }
}
