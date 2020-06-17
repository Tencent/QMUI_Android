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
import android.graphics.Rect;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.DisplayCutoutCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.widget.INotchInsetConsumer;
import com.qmuiteam.qmui.widget.IWindowInsetKeyboardConsumer;
import com.qmuiteam.qmui.widget.IWindowInsetLayout;

import java.util.ArrayList;

/**
 * @author cginechen
 * @date 2017-09-13
 */

public class QMUIWindowInsetHelper {

    private static ArrayList<Class<? extends ViewGroup>> sCustomHandlerContainerList = new ArrayList<>();
    private static int sCurrentWindowInsetDepth = 0;

    public static void apply(@NonNull final ViewGroup viewGroup){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            // need to override fitSystemWindows().
            return;
        }
        if(!(viewGroup instanceof IWindowInsetLayout)){
            throw new IllegalStateException(viewGroup.getClass().getSimpleName() + " must implement IWindowInsetLayout");
        }
        ViewCompat.setOnApplyWindowInsetsListener(viewGroup,
                new OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v,
                                                                  WindowInsetsCompat insets) {

                        // avoid dispatching multiple times
                        boolean needToDispatchNotchInsetChange = QMUINotchHelper.isNotchOfficialSupport() && sCurrentWindowInsetDepth == 0;
                        if (sCurrentWindowInsetDepth == 0) {
                            // always consume display cutout!!
                            DisplayCutoutCompat displayCutout = insets.getDisplayCutout();
                            if (displayCutout != null) {
                                needToDispatchNotchInsetChange = true;
                                v.setTag(R.id.qmui_window_inset_layout_display_cutout, true);
                                insets = insets.consumeDisplayCutout();
                            }else{
                                insets = insets.consumeDisplayCutout();
                                Object lastHasNotchInfo = v.getTag(R.id.qmui_window_inset_layout_display_cutout);
                                if(lastHasNotchInfo != null && (Boolean)lastHasNotchInfo){
                                    needToDispatchNotchInsetChange = true;
                                    v.setTag(R.id.qmui_window_inset_layout_display_cutout, false);
                                }
                            }
                        }

                        if(!insets.isConsumed()){
                            sCurrentWindowInsetDepth ++;
                            if(v instanceof IWindowInsetLayout){
                                IWindowInsetLayout windowInsetLayout = (IWindowInsetLayout) v;
                                insets = windowInsetLayout.applySystemWindowInsets21(insets);
                            }
                            sCurrentWindowInsetDepth --;
                        }

                        if (needToDispatchNotchInsetChange) {
                            dispatchNotchInsetChange(v);
                        }
                        return insets;
                    }
                });
    }

    public static boolean defaultApplySystemWindowInsets19(ViewGroup viewGroup, Rect insets) {
        boolean consumed = false;
        if (insets.bottom >= QMUIDisplayHelper.dp2px(viewGroup.getContext(), QMUIWindowHelper.KEYBOARD_HEIGHT_BOUNDARY_DP)
                && shouldInterceptKeyboardInset(viewGroup)) {
            if(viewGroup instanceof IWindowInsetKeyboardConsumer){
                ((IWindowInsetKeyboardConsumer)viewGroup).onHandleKeyboard(insets.bottom);
            }else{
                QMUIViewHelper.setPaddingBottom(viewGroup, insets.bottom);
            }
            viewGroup.setTag(R.id.qmui_window_inset_keyboard_area_consumer, QMUIKeyboardHelper.KEYBOARD_CONSUMER);
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
    public static WindowInsetsCompat defaultApplySystemWindowInsets21(ViewGroup viewGroup, WindowInsetsCompat insets) {
        boolean showKeyboard = false;
        if (insets.getSystemWindowInsetBottom() >= QMUIDisplayHelper.dp2px(viewGroup.getContext(), QMUIWindowHelper.KEYBOARD_HEIGHT_BOUNDARY_DP) &&
                shouldInterceptKeyboardInset(viewGroup)) {
            showKeyboard = true;
            if(viewGroup instanceof IWindowInsetKeyboardConsumer){
                ((IWindowInsetKeyboardConsumer)viewGroup).onHandleKeyboard(insets.getSystemWindowInsetBottom());
            }else{
                QMUIViewHelper.setPaddingBottom(viewGroup, insets.getSystemWindowInsetBottom());
            }
            viewGroup.setTag(R.id.qmui_window_inset_keyboard_area_consumer, QMUIKeyboardHelper.KEYBOARD_CONSUMER);
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
            if (!isHandleContainer(child)) {
                child.setPadding(childInsets.left, childInsets.top, childInsets.right, childInsets.bottom);
            }else{
                WindowInsetsCompat childWindowInsets = new WindowInsetsCompat.Builder(insets)
                        .setSystemWindowInsets(Insets.of(childInsets))
                        .build();
                ViewCompat.dispatchApplyWindowInsets(child, childWindowInsets);
            }
        }
        return insets.consumeSystemWindowInsets();
    }


    private static void dispatchNotchInsetChange(View view) {
        if (view instanceof INotchInsetConsumer) {
            boolean stop = ((INotchInsetConsumer) view).notifyInsetMaybeChanged();
            if (stop) {
                return;
            }
        }
        if (view instanceof ViewGroup && view.getClass().getAnnotation(InterceptNotchInsetDispatch.class) == null) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                dispatchNotchInsetChange(viewGroup.getChildAt(i));
            }
        }
    }

    private static boolean shouldInterceptKeyboardInset(ViewGroup viewGroup){
        return viewGroup.getClass().getAnnotation(DoNotInterceptKeyboardInset.class) == null;
    }

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

    public static void computeInsets(View view, Rect insets) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if(lp instanceof ConstraintLayout.LayoutParams){
            computeInsetsWithConstraint(insets, (ConstraintLayout.LayoutParams) lp);
        }else{
            computeInsetsWithGravity(insets, lp);
        }
    }

    @SuppressLint("RtlHardcoded")
    public static void computeInsetsWithGravity(Rect insets, ViewGroup.LayoutParams lp) {
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

    public static void computeInsetsWithConstraint(Rect insets, ConstraintLayout.LayoutParams lp){
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
}
