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

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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


    public QMUISwipeViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void addSwipeAction(QMUISwipeAction action) {
        if (mSwipeActions == null) {
            mSwipeActions = new ArrayList<>();
        }
        mSwipeActions.add(new ActionWrapper(action));
    }

    public boolean hasAction(){
        return mSwipeActions != null && !mSwipeActions.isEmpty();
    }

    public void clearTouchInfo(){
        mCurrentTouchAction = null;
        mActionDownY = -1;
        mActionDownX = -1;
    }

    void setup(int swipeDirection) {
        mActionTotalWidth = 0;
        mActionTotalHeight = 0;
        if(mSwipeActions == null || mSwipeActions.isEmpty()){
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
            } else if(swipeDirection == SWIPE_UP || swipeDirection == SWIPE_DOWN){
                wrapper.measureHeight = Math.max(action.mSwipeDirectionMiniSize,
                        action.contentHeight + 2 * action.mPaddingStartEnd);
                wrapper.measureWidth = itemView.getWidth();
                mActionTotalHeight += wrapper.measureHeight;
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

    QMUISwipeAction checkUp(float x, float y, int touchSlop){
        if(mCurrentTouchAction != null && mCurrentTouchAction.hitTest(x, y)){
            if(Math.abs(x - mActionDownX) < touchSlop && Math.abs(y - mActionDownY) < touchSlop){
                return mCurrentTouchAction.action;
            }
        }
        return null;
    }

    void draw(Canvas canvas, float dx, float dy){
        if (mSwipeActions.isEmpty()) {
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
            actionInfo.draw(canvas);
        }
    }
    
    static class ActionWrapper {
        final QMUISwipeAction action;

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

        public ActionWrapper(QMUISwipeAction action){
            this.action = action;
        }

        boolean hitTest(float x, float y) {
            return x > left && x < left + width && y > top && y < top + height;
        }

        void draw(Canvas canvas) {
            canvas.save();
            canvas.translate(left, top);
            action.paint.setStyle(Paint.Style.FILL);
            action.paint.setColor(action.mBackgroundColor);
            canvas.drawRect(0, 0, width, height, action.paint);
            canvas.translate((width - action.contentWidth) / 2f, 0);

            if (action.mIcon != null) {
                action.mIcon.setBounds(0, 0, action.mIcon.getIntrinsicWidth(), action.mIcon.getIntrinsicHeight());
                float translateY = (height - action.mIcon.getIntrinsicHeight()) / 2;
                canvas.translate(0, translateY);
                action.mIcon.draw(canvas);
                canvas.translate(0, -translateY);
            }
            if (action.mIcon != null && action.mText != null) {
                canvas.translate(action.mIconTextGap, 0);
            }
            if (action.mText != null) {
                action.paint.setColor(action.mTextColor);
                Paint.FontMetrics metricsInt = action.paint.getFontMetrics();
                float baseLine = (height - metricsInt.descent + metricsInt.ascent) / 2 - metricsInt.ascent;
                canvas.drawText(action.mText, 0, baseLine, action.paint);
            }
            canvas.restore();
        }
    }
}
