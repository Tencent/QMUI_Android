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

import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;

import com.qmuiteam.qmui.R;

/**
 * @author cginechen
 * @date 2017-09-13
 */

public class QMUIWindowInsetHelper {

    public final static InsetHandler consumeInsetWithPaddingHandler = new InsetHandler() {
        @Override
        public void handleInset(View view, Insets insets) {
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        }
    };

    public final static InsetHandler consumeInsetWithPaddingIgnoreBottomHandler = new InsetHandler() {
        @Override
        public void handleInset(View view, Insets insets) {
            view.setPadding(insets.left, insets.top, insets.right, 0);
        }
    };

    public final static InsetHandler consumeInsetWithPaddingIgnoreTopHandler = new InsetHandler() {
        @Override
        public void handleInset(View view, Insets insets) {
            view.setPadding(insets.left, 0, insets.right, insets.bottom);
        }
    };

    public final static InsetHandler consumeInsetWithPaddingWithGravityHandler = new InsetHandler() {
        @Override
        public void handleInset(View view, Insets insets) {
            Insets toUsed = adapterInsetsWithGravity(view, insets);
            view.setPadding(toUsed.left, toUsed.top, toUsed.right, toUsed.bottom);
        }
    };

    private final static OnApplyWindowInsetsListener sStopDispatchListener = new OnApplyWindowInsetsListener() {
        @Override
        public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
            return WindowInsetsCompat.CONSUMED;
        }
    };

    private final static OnApplyWindowInsetsListener sOverrideWithNothingHandleListener = new OnApplyWindowInsetsListener() {
        @Override
        public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
            return insets;
        }
    };

    public static void handleWindowInsets(View v, @WindowInsetsCompat.Type.InsetsType final int insetsType){
        handleWindowInsets(v, insetsType, false);
    }

    public static void handleWindowInsets(View v, @WindowInsetsCompat.Type.InsetsType final int insetsType, boolean jumpSelfHandleIfMatchLast){
        handleWindowInsets(v, insetsType, jumpSelfHandleIfMatchLast, false);
    }

    public static void handleWindowInsets(View v, @WindowInsetsCompat.Type.InsetsType final int insetsType, boolean jumpSelfHandleIfMatchLast, boolean ignoreVisibility){
        handleWindowInsets(v, insetsType, consumeInsetWithPaddingWithGravityHandler, jumpSelfHandleIfMatchLast, ignoreVisibility, false);
    }

    public static void handleWindowInsets(View v, @WindowInsetsCompat.Type.InsetsType final int insetsType,
                                          boolean jumpSelfHandleIfMatchLast,
                                          boolean ignoreVisibility,
                                          boolean stopDispatch){
        handleWindowInsets(v, insetsType, consumeInsetWithPaddingWithGravityHandler, jumpSelfHandleIfMatchLast, ignoreVisibility, stopDispatch);
    }

    /**
     *
     * @param v the view to handle window insets.
     * @param insetsType the insets type
     * @param insetHandler insetHandler
     * @param jumpSelfHandleIfMatchLast if same as last, we do not dispatch window insets to v but return the last result directly.
     * @param stopDispatch it's dangerous to use this. if View.sBrokenInsetsDispatch is true, it will stop dispatching to siblings and children,
     *                     if View.sBrokenInsetsDispatch is false, it will only stop dispatching to children. But View.sBrokenInsetsDispatch is
     *                     not public.
     */
    public static void handleWindowInsets(View v,
                                          @WindowInsetsCompat.Type.InsetsType final int insetsType,
                                          @NonNull final InsetHandler insetHandler,
                                          boolean jumpSelfHandleIfMatchLast,
                                          final boolean ignoreVisibility,
                                          final boolean stopDispatch
    ){
        setOnApplyWindowInsetsListener(v, new androidx.core.view.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                if(v.getFitsSystemWindows()){
                    Insets toUsed = ignoreVisibility ? insets.getInsetsIgnoringVisibility(insetsType) : insets.getInsets(insetsType);
                    insetHandler.handleInset(v, toUsed);
                    if(stopDispatch){
                        return WindowInsetsCompat.CONSUMED;
                    }
                }
                return insets;
            }
        }, jumpSelfHandleIfMatchLast);
    }

    /**
     * it's dangerous to use this. if View.sBrokenInsetsDispatch is true, it will stop dispatching to siblings and children,
     * if View.sBrokenInsetsDispatch is false, it will only stop dispatching to children. But View.sBrokenInsetsDispatch is
     * not public.
     * @param v the view to stop
     */
    public static void stopDispatchWindowInsets(View v){
        setOnApplyWindowInsetsListener(v, sStopDispatchListener, true);
    }

    public static void overrideWithDoNotHandleWindowInsets(View v){
        setOnApplyWindowInsetsListener(v, sOverrideWithNothingHandleListener, false);
    }

    // copy from ViewCompat 1.5.0-beta01,  fix the re dispatch problem.
    public static void setOnApplyWindowInsetsListener(final @NonNull View v,
                                               final @Nullable OnApplyWindowInsetsListener listener,
                                               final boolean reuseIfInputIsSame
    ) {
        // For backward compatibility of WindowInsetsAnimation, we use an
        // OnApplyWindowInsetsListener. We use the view tags to keep track of both listeners
        if (Build.VERSION.SDK_INT < 30) {
            v.setTag(R.id.tag_on_apply_window_listener, listener);
        }

        if (listener == null) {
            // If the listener is null, we need to make sure our compat listener, if any, is
            // set in-lieu of the listener being removed.
            View.OnApplyWindowInsetsListener compatInsetsAnimationCallback =
                    (View.OnApplyWindowInsetsListener) v.getTag(
                            R.id.tag_window_insets_animation_callback);
            v.setOnApplyWindowInsetsListener(compatInsetsAnimationCallback);
            return;
        }

        v.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            WindowInsetsCompat mLastInsets = null;
            WindowInsets mReturnedInsets = null;

            @Override
            public WindowInsets onApplyWindowInsets(final View view,
                                                    final WindowInsets insets) {
                WindowInsetsCompat compatInsets = WindowInsetsCompat.toWindowInsetsCompat(
                        insets, view);
                // On API < 30,  we request dispatch again until the input is same with last.
                boolean needRequestApplyInsetsAgain = true;
                if (Build.VERSION.SDK_INT < 30) {
                    callCompatInsetAnimationCallback(insets, v);

                    if (compatInsets.equals(mLastInsets)) {
                        needRequestApplyInsetsAgain = false;
                        if (reuseIfInputIsSame) {
                            // We got the same insets we just return the previously computed insets.
                            return mReturnedInsets;
                        }
                    }
                    mLastInsets = compatInsets;
                }
                compatInsets = listener.onApplyWindowInsets(view, compatInsets);

                if (Build.VERSION.SDK_INT >= 30) {
                    return compatInsets.toWindowInsets();
                }

                // On API < 30, the visibleInsets, used to built WindowInsetsCompat, are
                // updated after the insets dispatch so we don't have the updated visible
                // insets at that point. As a workaround, we re-apply the insets so we know
                // that we'll have the right value the next time it's called.
                if(needRequestApplyInsetsAgain){
                    ViewCompat.requestApplyInsets(view);
                }

                // Keep a copy in case the insets haven't changed on the next call so we don't
                // need to call the listener again.
                mReturnedInsets = compatInsets.toWindowInsets();
                return mReturnedInsets;
            }
        });
    }

    /**
     * The backport of {@link WindowInsetsAnimationCompat.Callback} on API < 30 relies on
     * onApplyWindowInsetsListener, so if this callback is set, we'll call it in this method
     */
    private static void callCompatInsetAnimationCallback(final @NonNull WindowInsets insets,
                                                 final @NonNull View v) {
        // In case a WindowInsetsAnimationCompat.Callback is set, make sure to
        // call its compat listener.
        View.OnApplyWindowInsetsListener insetsAnimationCallback =
                (View.OnApplyWindowInsetsListener) v.getTag(
                        R.id.tag_window_insets_animation_callback);
        if (insetsAnimationCallback != null) {
            insetsAnimationCallback.onApplyWindowInsets(v, insets);
        }
    }

    public static Insets adapterInsetsWithGravity(View view, Insets insets){
        int left = insets.left;
        int right = insets.right;
        int top = insets.top;
        int bottom = insets.bottom;

        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if(lp instanceof ConstraintLayout.LayoutParams){
            ConstraintLayout.LayoutParams constraintLp = (ConstraintLayout.LayoutParams) lp;
            if (constraintLp.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
                if(constraintLp.leftToLeft == ConstraintLayout.LayoutParams.PARENT_ID){
                    right = 0;
                }else if(constraintLp.rightToRight == ConstraintLayout.LayoutParams.PARENT_ID){
                    left = 0;
                }
            }

            if (constraintLp.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                if(constraintLp.topToTop == ConstraintLayout.LayoutParams.PARENT_ID){
                    bottom = 0;
                }else if(constraintLp.bottomToBottom == ConstraintLayout.LayoutParams.PARENT_ID){
                    top = 0;
                }
            }
        }else{
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
                        right = 0;
                        break;
                    case Gravity.RIGHT:
                        left = 0;
                        break;
                }
            }

            if (lp.height != ViewGroup.LayoutParams.MATCH_PARENT) {
                int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
                switch (verticalGravity) {
                    case Gravity.TOP:
                        bottom = 0;
                        break;
                    case Gravity.BOTTOM:
                        top = 0;
                        break;
                }
            }
        }
        return Insets.of(left, top, right, bottom);
    }

    public interface InsetHandler{
        void handleInset(View view, Insets insets);
    }
}
