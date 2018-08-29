package com.qmuiteam.qmui.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.view.DisplayCutout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.widget.INotchInsetConsumer;
import com.qmuiteam.qmui.widget.IWindowInsetLayout;

import java.lang.ref.WeakReference;

/**
 * @author cginechen
 * @date 2017-09-13
 */

public class QMUIWindowInsetHelper {
    private final int KEYBOARD_HEIGHT_BOUNDARY;
    private final WeakReference<IWindowInsetLayout> mWindowInsetLayoutWR;
    private int sApplySystemWindowInsetsCount = 0;

    public QMUIWindowInsetHelper(ViewGroup viewGroup, IWindowInsetLayout windowInsetLayout) {
        mWindowInsetLayoutWR = new WeakReference<>(windowInsetLayout);
        KEYBOARD_HEIGHT_BOUNDARY = QMUIDisplayHelper.dp2px(viewGroup.getContext(), 100);

        if (QMUINotchHelper.isNotchOfficialSupport()) {
            setOnApplyWindowInsetsListener28(viewGroup);
        } else {
            // some rom crash with WindowInsets...
            ViewCompat.setOnApplyWindowInsetsListener(viewGroup,
                    new android.support.v4.view.OnApplyWindowInsetsListener() {
                        @Override
                        public WindowInsetsCompat onApplyWindowInsets(View v,
                                                                      WindowInsetsCompat insets) {
                            if (Build.VERSION.SDK_INT >= 21 && mWindowInsetLayoutWR.get() != null) {
                                if (mWindowInsetLayoutWR.get().applySystemWindowInsets21(insets)) {
                                    return insets.consumeSystemWindowInsets();
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
                    return windowInsets;
                }
                return windowInsets;
            }
        });
    }

    @SuppressWarnings("deprecation")
    @TargetApi(19)
    public boolean defaultApplySystemWindowInsets19(ViewGroup viewGroup, Rect insets) {
        boolean consumed = false;
        if (insets.bottom >= KEYBOARD_HEIGHT_BOUNDARY) {
            QMUIViewHelper.setPaddingBottom(viewGroup, insets.bottom);
            insets.bottom = 0;
        } else {
            QMUIViewHelper.setPaddingBottom(viewGroup, 0);
        }

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (jumpDispatch(child)) {
                continue;
            }

            Rect childInsets = new Rect(insets);
            computeInsetsWithGravity(child, childInsets);

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
            return defaultApplySystemWindowInsetsComapt(viewGroup, (WindowInsetsCompat) insets);
        }
    }

    @TargetApi(21)
    public boolean defaultApplySystemWindowInsetsComapt(ViewGroup viewGroup, WindowInsetsCompat insets) {
        if (!insets.hasSystemWindowInsets()) {
            return false;
        }
        boolean consumed = false;
        boolean showKeyboard = false;
        if (insets.getSystemWindowInsetBottom() >= KEYBOARD_HEIGHT_BOUNDARY) {
            showKeyboard = true;
            QMUIViewHelper.setPaddingBottom(viewGroup, insets.getSystemWindowInsetBottom());
        } else {
            QMUIViewHelper.setPaddingBottom(viewGroup, 0);
        }

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);

            if (jumpDispatch(child)) {
                continue;
            }

            int insetLeft =  insets.getSystemWindowInsetLeft();
            int insetRight = insets.getSystemWindowInsetRight();
            if(QMUINotchHelper.needFixLandscapeNotchAreaFitSystemWindow(viewGroup) &&
                    viewGroup.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                insetLeft = Math.max(insetLeft, QMUINotchHelper.getSafeInsetLeft(viewGroup));
                insetRight = Math.max(insetRight, QMUINotchHelper.getSafeInsetRight(viewGroup));
            }

            Rect childInsets = new Rect(
                    insetLeft,
                    insets.getSystemWindowInsetTop(),
                    insetRight,
                    showKeyboard ? 0 : insets.getSystemWindowInsetBottom());

            computeInsetsWithGravity(child, childInsets);
            WindowInsetsCompat windowInsetsCompat = ViewCompat.dispatchApplyWindowInsets(child, insets.replaceSystemWindowInsets(childInsets));
            consumed = consumed || windowInsetsCompat.isConsumed();
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
        if (insets.hasSystemWindowInsets()) {
            boolean showKeyboard = false;
            if (insets.getSystemWindowInsetBottom() >= KEYBOARD_HEIGHT_BOUNDARY) {
                showKeyboard = true;
                QMUIViewHelper.setPaddingBottom(viewGroup, insets.getSystemWindowInsetBottom());
            } else {
                QMUIViewHelper.setPaddingBottom(viewGroup, 0);
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
                computeInsetsWithGravity(child, childInsets);
                WindowInsets childWindowInsets = insets.replaceSystemWindowInsets(childInsets);
                WindowInsets windowInsets = child.dispatchApplyWindowInsets(childWindowInsets);
                consumed = consumed || windowInsets.isConsumed();
            }
        }
        sApplySystemWindowInsetsCount--;
        return consumed;
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
        return child instanceof IWindowInsetLayout ||
                child instanceof CoordinatorLayout;
    }

    @SuppressLint("RtlHardcoded")
    private void computeInsetsWithGravity(View view, Rect insets) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
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

        if (lp.width != FrameLayout.LayoutParams.MATCH_PARENT) {
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

        if (lp.height != FrameLayout.LayoutParams.MATCH_PARENT) {
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
}
