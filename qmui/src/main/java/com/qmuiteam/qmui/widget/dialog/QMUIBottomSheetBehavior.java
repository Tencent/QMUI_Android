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

package com.qmuiteam.qmui.widget.dialog;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class QMUIBottomSheetBehavior<V extends ViewGroup> extends BottomSheetBehavior<V> {
    private boolean mAllowDrag = true;
    private boolean mMotionEventCanDrag = true;
    private DownDragDecisionMaker mDownDragDecisionMaker;

    public void setAllowDrag(boolean allowDrag) {
        mAllowDrag = allowDrag;
    }

    public void setDownDragDecisionMaker(DownDragDecisionMaker downDragDecisionMaker) {
        mDownDragDecisionMaker = downDragDecisionMaker;
    }

    @Override
    public boolean onTouchEvent(@NonNull CoordinatorLayout parent,
                                @NonNull V child,
                                @NonNull MotionEvent event) {
        if(!mAllowDrag){
            return false;
        }

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            mMotionEventCanDrag = mDownDragDecisionMaker == null ||
                    mDownDragDecisionMaker.canDrag(parent, child, event);
        }

        if(!mMotionEventCanDrag){
            return false;
        }

        return super.onTouchEvent(parent, child, event);
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent,
                                         @NonNull V child,
                                         @NonNull MotionEvent event) {
        if(!mAllowDrag){
            return false;
        }

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            mMotionEventCanDrag = mDownDragDecisionMaker == null ||
                    mDownDragDecisionMaker.canDrag(parent, child, event);
        }
        if(!mMotionEventCanDrag){
            return false;
        }
        return super.onInterceptTouchEvent(parent, child, event);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull V child,
                                       @NonNull View directTargetChild,
                                       @NonNull View target, int axes, int type) {
        if(!mAllowDrag){
            return false;
        }
        return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type);
    }


    public interface DownDragDecisionMaker {
        boolean canDrag(@NonNull CoordinatorLayout parent,
                        @NonNull View child,
                        @NonNull MotionEvent event);
    }
}
