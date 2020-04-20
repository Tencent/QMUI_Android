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

package com.qmuiteam.qmui.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.view.DisplayCutout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.widget.INotchInsetConsumer;
import com.qmuiteam.qmui.widget.IWindowInsetLayout;
import com.qmuiteam.qmui.widget.IWindowInsetKeyboardConsumer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

/**
 * @author cginechen
 * @date 2017-09-13
 */

public class QMUIWindowInsetHelper {
    public static final int KEYBOARD_HEIGHT_BOUNDARY_DP = 100;
    private static final Object KEYBOARD_CONSUMER = new Object();
    private static ArrayList<Class<? extends ViewGroup>> sCustomHandlerContainerList = new ArrayList<>();
    private final int KEYBOARD_HEIGHT_BOUNDARY;
    private final WeakReference<IWindowInsetLayout> mWindowInsetLayoutWR;
    private int sApplySystemWindowInsetsCount = 0;

    public QMUIWindowInsetHelper(ViewGroup viewGroup, IWindowInsetLayout windowInsetLayout) {
        mWindowInsetLayoutWR = new WeakReference<>(windowInsetLayout);
        KEYBOARD_HEIGHT_BOUNDARY = QMUIDisplayHelper.dp2px(viewGroup.getContext(), KEYBOARD_HEIGHT_BOUNDARY_DP);

        if (QMUINotchHelper.isNotchOfficialSupport()) {
            setOnApplyWindowInsetsListener28(viewGroup);
        } else {
            // some rom crash with WindowInsets...
            ViewCompat.setOnApplyWindowInsetsListener(viewGroup,
                    new OnApplyWindowInsetsListener() {
                        @Override
                        public WindowInsetsCompat onApplyWindowInsets(View v,
                                                                      WindowInsetsCompat insets) {
                            if (Build.VERSION.SDK_INT >= 21 && mWindowInsetLayoutWR.get() != null) {
                                if (mWindowInsetLayoutWR.get().applySystemWindowInsets21(insets)) {
                                    if(insets.isConsumed()){
                                        return insets;
                                    }
                                    insets = insets.consumeSystemWindowInsets();
                                    if(insets.isConsumed()){
                                        return insets;
                                    }
                                    return insets.consumeStableInsets();
                                }
                            }
                            return insets;
                        }
                    });
        }
    }

    @TargetApi(28)
    private void setOnApplyWindowInsetsListener28(ViewGroup viewGroup) {
        // WindowInsetsCompat does not exist DisplayCutout stuff...
        viewGroup.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                if (mWindowInsetLayoutWR.get() != null &&
                        mWindowInsetLayoutWR.get().applySystemWindowInsets21(windowInsets)) {
                    windowInsets = windowInsets.consumeSystemWindowInsets();

                    DisplayCutout displayCutout = windowInsets.getDisplayCutout();
                    if (displayCutout != null) {
                        windowInsets = windowInsets.consumeDisplayCutout();
                    }
                    if(windowInsets.isConsumed()){
                        return windowInsets;
                    }
                    return windowInsets.consumeStableInsets();
                }
                return windowInsets;
            }
        });
    }

    @SuppressWarnings("deprecation")
    @TargetApi(19)
    public boolean defaultApplySystemWindowInsets19(ViewGroup viewGroup, Rect insets) {
        boolean consumed = false;
        if (insets.bottom >= KEYBOARD_HEIGHT_BOUNDARY && shouldInterceptKeyboardInset(viewGroup)) {
            if(viewGroup instanceof IWindowInsetKeyboardConsumer){
                ((IWindowInsetKeyboardConsumer)viewGroup).onHandleKeyboard(insets.bottom);
            }else{
                QMUIViewHelper.setPaddingBottom(viewGroup, insets.bottom);
            }
            viewGroup.setTag(R.id.qmui_window_inset_keyboard_area_consumer, KEYBOARD_CONSUMER);
            insets.bottom = 0;
        } else {
            viewGroup.setTag(R.id.qmui_window_inset_keyboard_area_consumer, null);
            if(viewGroup instanceof IWindowInsetKeyboardConsumer){
                ((IWindowInsetKeyboardConsumer)viewGroup).onHandleKeyboard(0);
            }else{
                QMUIViewHelper.setPaddingBottom(viewGroup, 0);
            }
        }

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (jumpDispatch(child)) {
                continue;
            }

            Rect childInsets = new Rect(insets);
            computeInsets(child, childInsets);

            if (!isHandleContainer(child)) {
                child.setPadding(childInsets.left, childInsets.top, childInsets.right, childInsets.bottom);
            } else {
                if (child instanceof IWindowInsetLayout) {
                    boolean output = ((IWindowInsetLayout) child).applySystemWindowInsets19(childInsets);
                    consumed = consumed || output;
                } else {
                    boolean output = defaultApplySystemWindowInsets19((ViewGroup) child, childInsets);
                    consumed = consumed || output;
                }
            }
        }

        return consumed;
    }

    @TargetApi(21)
    public boolean defaultApplySystemWindowInsets21(ViewGroup viewGroup, Object insets) {
        if (QMUINotchHelper.isNotchOfficialSupport()) {
            return defaultApplySystemWindowInsets(viewGroup, (WindowInsets) insets);
        } else {
            return defaultApplySystemWindowInsetsCompat(viewGroup, (WindowInsetsCompat) insets);
        }
    }

    @TargetApi(21)
    public boolean defaultApplySystemWindowInsetsCompat(ViewGroup viewGroup, WindowInsetsCompat insets) {
        boolean consumed = false;
        boolean showKeyboard = false;
        if (insets.getSystemWindowInsetBottom() >= KEYBOARD_HEIGHT_BOUNDARY &&
                shouldInterceptKeyboardInset(viewGroup)) {
            showKeyboard = true;
            if(viewGroup instanceof IWindowInsetKeyboardConsumer){
                ((IWindowInsetKeyboardConsumer)viewGroup).onHandleKeyboard(insets.getSystemWindowInsetBottom());
            }else{
                QMUIViewHelper.setPaddingBottom(viewGroup, insets.getSystemWindowInsetBottom());
            }
            viewGroup.setTag(R.id.qmui_window_inset_keyboard_area_consumer, KEYBOARD_CONSUMER);
        } else {
            if(viewGroup instanceof IWindowInsetKeyboardConsumer){
                ((IWindowInsetKeyboardConsumer)viewGroup).onHandleKeyboard(0);
            }else{
                QMUIViewHelper.setPaddingBottom(viewGroup, 0);
            }
            viewGroup.setTag(R.id.qmui_window_inset_keyboard_area_consumer, null);
        }

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);

            if (jumpDispatch(child)) {
                continue;
            }

            int insetLeft = insets.getSystemWindowInsetLeft();
            int insetRight = insets.getSystemWindowInsetRight();
            if (QMUINotchHelper.needFixLandscapeNotchAreaFitSystemWindow(viewGroup) &&
                    viewGroup.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                insetLeft = Math.max(insetLeft, QMUINotchHelper.getSafeInsetLeft(viewGroup));
                insetRight = Math.max(insetRight, QMUINotchHelper.getSafeInsetRight(viewGroup));
            }

            Rect childInsets = new Rect(
                    insetLeft,
                    insets.getSystemWindowInsetTop(),
                    insetRight,
                    showKeyboard ? 0 : insets.getSystemWindowInsetBottom());

            computeInsets(child, childInsets);
            WindowInsetsCompat windowInsetsCompat = ViewCompat.dispatchApplyWindowInsets(child, insets.replaceSystemWindowInsets(childInsets));
            consumed = consumed || (windowInsetsCompat != null && windowInsetsCompat.isConsumed());
        }

        return consumed;
    }

    @TargetApi(28)
    public boolean defaultApplySystemWindowInsets(ViewGroup viewGroup, WindowInsets insets) {
        sApplySystemWindowInsetsCount++;
        if (QMUINotchHelper.isNotchOfficialSupport()) {
            if (sApplySystemWindowInsetsCount == 1) {
                // avoid dispatching multiple times
                dispatchNotchInsetChange(viewGroup);
            }
            // always consume display cutout!!
            insets = insets.consumeDisplayCutout();
        }

        boolean consumed = false;
        boolean showKeyboard = false;
        if (insets.getSystemWindowInsetBottom() >= KEYBOARD_HEIGHT_BOUNDARY &&
                shouldInterceptKeyboardInset(viewGroup)) {
            showKeyboard = true;
            if(viewGroup instanceof IWindowInsetKeyboardConsumer){
                ((IWindowInsetKeyboardConsumer)viewGroup).onHandleKeyboard(insets.getSystemWindowInsetBottom());
            }else{
                QMUIViewHelper.setPaddingBottom(viewGroup, insets.getSystemWindowInsetBottom());
            }
            viewGroup.setTag(R.id.qmui_window_inset_keyboard_area_consumer, KEYBOARD_CONSUMER);
        } else {
            if(viewGroup instanceof IWindowInsetKeyboardConsumer){
                ((IWindowInsetKeyboardConsumer)viewGroup).onHandleKeyboard(0);
            }else{
                QMUIViewHelper.setPaddingBottom(viewGroup, 0);
            }
            viewGroup.setTag(R.id.qmui_window_inset_keyboard_area_consumer, null);
        }
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);

            if (jumpDispatch(child)) {
                continue;
            }

            Rect childInsets = new Rect(
                    insets.getSystemWindowInsetLeft(),
                    insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(),
                    showKeyboard ? 0 : insets.getSystemWindowInsetBottom());
            computeInsets(child, childInsets);
            WindowInsets childWindowInsets = insets.replaceSystemWindowInsets(childInsets);
            WindowInsets windowInsets = child.dispatchApplyWindowInsets(childWindowInsets);
            consumed = consumed || windowInsets.isConsumed();
        }
        sApplySystemWindowInsetsCount--;
        return consumed;
    }

    private boolean shouldInterceptKeyboardInset(ViewGroup viewGroup){
        return viewGroup.getClass().getAnnotation(DoNotInterceptKeyboardInset.class) == null;
    }

    private void dispatchNotchInsetChange(View view) {
        if (view instanceof INotchInsetConsumer) {
            boolean stop = ((INotchInsetConsumer) view).notifyInsetMaybeChanged();
            if (stop) {
                return;
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                dispatchNotchInsetChange(viewGroup.getChildAt(i));
            }
        }
    }

    @SuppressWarnings("deprecation")
    @TargetApi(19)
    public static boolean jumpDispatch(View child) {
        return !child.getFitsSystemWindows() && !isHandleContainer(child);
    }

    public static boolean isHandleContainer(View child) {
        boolean ret = child instanceof IWindowInsetLayout ||
                child instanceof CoordinatorLayout ||
                child instanceof DrawerLayout;
        if (ret) {
            return true;
        }
        for (Class<? extends View> clz : sCustomHandlerContainerList) {
            if (clz.isInstance(child)) {
                return true;
            }
        }
        return false;
    }

    public static void addHandleContainer(Class<? extends ViewGroup> clazz) {
        sCustomHandlerContainerList.add(clazz);
    }

    public void computeInsets(View view, Rect insets) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if(lp instanceof ConstraintLayout.LayoutParams){
            computeInsetsWithConstraint(view, insets, (ConstraintLayout.LayoutParams) lp);
        }else{
            computeInsetsWithGravity(view, insets, lp);
        }
    }

    @SuppressLint("RtlHardcoded")
    public void computeInsetsWithGravity(View view, Rect insets, ViewGroup.LayoutParams lp) {
        int gravity = -1;
        if (lp instanceof FrameLayout.LayoutParams) {
            gravity = ((FrameLayout.LayoutParams) lp).gravity;
        }

        /**
         * 因为该方法执行时机早于 FrameLayout.layoutChildren，
         * 而在 {FrameLayout#layoutChildren} 中当 gravity == -1 时会设置默认值为 Gravity.TOP | Gravity.LEFT，
         * 所以这里也要同样设置
         */
        if (gravity == -1) {
            gravity = Gravity.TOP | Gravity.LEFT;
        }

        if (lp.width != ViewGroup.LayoutParams.MATCH_PARENT) {
            int horizontalGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
            switch (horizontalGravity) {
                case Gravity.LEFT:
                    insets.right = 0;
                    break;
                case Gravity.RIGHT:
                    insets.left = 0;
                    break;
            }
        }

        if (lp.height != ViewGroup.LayoutParams.MATCH_PARENT) {
            int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
            switch (verticalGravity) {
                case Gravity.TOP:
                    insets.bottom = 0;
                    break;
                case Gravity.BOTTOM:
                    insets.top = 0;
                    break;
            }
        }
    }

    public void computeInsetsWithConstraint(View view, Rect insets, ConstraintLayout.LayoutParams lp){
        if (lp.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            if(lp.leftToLeft == ConstraintLayout.LayoutParams.PARENT_ID){
                insets.right = 0;
            }else if(lp.rightToRight == ConstraintLayout.LayoutParams.PARENT_ID){
                insets.left = 0;
            }
        }

        if (lp.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            if(lp.topToTop == ConstraintLayout.LayoutParams.PARENT_ID){
                insets.bottom = 0;
            }else if(lp.bottomToBottom == ConstraintLayout.LayoutParams.PARENT_ID){
                insets.top = 0;
            }
        }
    }

    public static View findKeyboardAreaConsumer(@NonNull View view) {
        while (view != null) {
            Object tag = view.getTag(R.id.qmui_window_inset_keyboard_area_consumer);
            if (KEYBOARD_CONSUMER == tag) {
                return view;
            }
            ViewParent viewParent = view.getParent();
            if (viewParent instanceof View) {
                view = (View) viewParent;
            } else {
                view = null;
            }
        }
        return null;
    }
}
