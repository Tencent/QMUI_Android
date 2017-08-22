package com.qmuiteam.qmui.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;

/**
 * From: https://github.com/oxoooo/earth/blob/30bd82fac7867be596bddf3bd0b32d8be3800665/app/src/main/java/ooo/oxo/apps/earth/widget/WindowInsetsFrameLayout.java
 * 教程(英文): https://medium.com/google-developers/why-would-i-want-to-fitssystemwindows-4e26d9ce1eec#.6i7s7gyam
 * 教程翻译: https://github.com/bboyfeiyu/android-tech-frontier/blob/master/issue-35/%E4%B8%BA%E4%BB%80%E4%B9%88%E6%88%91%E4%BB%AC%E8%A6%81%E7%94%A8fitsSystemWindows.md
 * <p>
 * 对于Keyboard的处理我们需要格外小心，这个组件不能只是处理状态栏，因为android还存在NavBar
 * 当windowInsets.bottom > 100dp的时候，我们认为是弹起了键盘。一旦弹起键盘，那么将由QMUIWindowInsetLayout消耗掉，其子view的windowInsets.bottom传递为0
 *
 * @author cginechen
 * @date 2016-03-25
 */
public class QMUIWindowInsetLayout extends FrameLayout {
    private final int KEYBOARD_HEIGHT_BOUNDARY;

    public QMUIWindowInsetLayout(Context context) {
        this(context, null);
    }

    public QMUIWindowInsetLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QMUIWindowInsetLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        KEYBOARD_HEIGHT_BOUNDARY = QMUIDisplayHelper.dp2px(context, 100);
        ViewCompat.setOnApplyWindowInsetsListener(this,
                new android.support.v4.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v,
                                                                  WindowInsetsCompat insets) {
                        return setWindowInsets(insets);
                    }
                });
    }

    private WindowInsetsCompat setWindowInsets(WindowInsetsCompat insets) {
        if (Build.VERSION.SDK_INT >= 21 && insets.hasSystemWindowInsets()) {
            if (applySystemWindowInsets21(insets)) {
                return insets.consumeSystemWindowInsets();
            }
        }
        return insets;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected boolean fitSystemWindows(Rect insets) {
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            return applySystemWindowInsets19(insets);
        }
        return super.fitSystemWindows(insets);
    }


    @SuppressWarnings("deprecation")
    @TargetApi(19)
    private boolean applySystemWindowInsets19(Rect insets) {
        boolean consumed = false;
        if (insets.bottom >= KEYBOARD_HEIGHT_BOUNDARY) {
            QMUIViewHelper.setPaddingBottom(this, insets.bottom);
            insets.bottom = 0;
        } else {
            QMUIViewHelper.setPaddingBottom(this, 0);
        }

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (!child.getFitsSystemWindows()) {
                continue;
            }

            Rect childInsets = new Rect(insets);
            computeInsetsWithGravity(child, childInsets);

            child.setPadding(childInsets.left, childInsets.top, childInsets.right, childInsets.bottom);

            consumed = true;
        }

        return consumed;
    }

    @TargetApi(21)
    private boolean applySystemWindowInsets21(WindowInsetsCompat insets) {
        boolean consumed = false;
        boolean showKeyboard = false;
        if (insets.getSystemWindowInsetBottom() >= KEYBOARD_HEIGHT_BOUNDARY) {
            showKeyboard = true;
            QMUIViewHelper.setPaddingBottom(this, insets.getSystemWindowInsetBottom());
        } else {
            QMUIViewHelper.setPaddingBottom(this, 0);
        }

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);

            if (!child.getFitsSystemWindows()) {
                continue;
            }

            Rect childInsets = new Rect(
                    insets.getSystemWindowInsetLeft(),
                    insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(),
                    showKeyboard ? 0 : insets.getSystemWindowInsetBottom());

            computeInsetsWithGravity(child, childInsets);
            ViewCompat.dispatchApplyWindowInsets(child, insets.replaceSystemWindowInsets(childInsets));

            consumed = true;
        }

        return consumed;
    }

    @SuppressLint("RtlHardcoded")
    private void computeInsetsWithGravity(View view, Rect insets) {
        LayoutParams lp = (LayoutParams) view.getLayoutParams();

        int gravity = lp.gravity;

        /**
         * 因为该方法执行时机早于 FrameLayout.layoutChildren，
         * 而在 {FrameLayout#layoutChildren} 中当 gravity == -1 时会设置默认值为 Gravity.TOP | Gravity.LEFT，
         * 所以这里也要同样设置
         */
        if (gravity == -1) {
            gravity = Gravity.TOP | Gravity.LEFT;
        }

        if (lp.width != LayoutParams.MATCH_PARENT) {
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

        if (lp.height != LayoutParams.MATCH_PARENT) {
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
