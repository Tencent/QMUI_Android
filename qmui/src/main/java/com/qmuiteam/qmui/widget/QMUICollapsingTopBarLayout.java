/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qmuiteam.qmui.widget;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.StyleRes;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.QMUIInterpolatorStaticHolder;
import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.util.QMUICollapsingTextHelper;
import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.util.QMUIViewOffsetHelper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * 参考 {@link android.support.design.widget.CollapsingToolbarLayout}, 适配 QMUITopBar
 *
 * @author cginechen
 * @date 2017-09-02
 */

public class QMUICollapsingTopBarLayout extends FrameLayout implements IWindowInsetLayout {

    private static final int DEFAULT_SCRIM_ANIMATION_DURATION = 600;

    private boolean mRefreshToolbar = true;
    private int mTopBarId;
    private QMUITopBar mTopBar;
    private View mTopBarDirectChild;

    private int mExpandedMarginStart;
    private int mExpandedMarginTop;
    private int mExpandedMarginEnd;
    private int mExpandedMarginBottom;

    private final Rect mTmpRect = new Rect();
    final QMUICollapsingTextHelper mCollapsingTextHelper;
    private boolean mCollapsingTitleEnabled;

    private Drawable mContentScrim;
    Drawable mStatusBarScrim;
    private int mScrimAlpha;
    private boolean mScrimsAreShown;
    private ValueAnimator mScrimAnimator;
    private long mScrimAnimationDuration;
    private int mScrimVisibleHeightTrigger = -1;

    private AppBarLayout.OnOffsetChangedListener mOnOffsetChangedListener;
    private ValueAnimator.AnimatorUpdateListener mScrimUpdateListener;

    int mCurrentOffset;

    WindowInsetsCompat mLastInsets;
    Rect mLastInsetRect;

    public QMUICollapsingTopBarLayout(Context context) {
        this(context, null);
    }

    public QMUICollapsingTopBarLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QMUICollapsingTopBarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mCollapsingTextHelper = new QMUICollapsingTextHelper(this);
        mCollapsingTextHelper.setTextSizeInterpolator(QMUIInterpolatorStaticHolder.DECELERATE_INTERPOLATOR);

        QMUIViewHelper.checkAppCompatTheme(context);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.QMUICollapsingTopBarLayout, defStyleAttr, 0);

        mCollapsingTextHelper.setExpandedTextGravity(
                a.getInt(R.styleable.QMUICollapsingTopBarLayout_qmui_expandedTitleGravity,
                        Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM));
        mCollapsingTextHelper.setCollapsedTextGravity(
                a.getInt(R.styleable.QMUICollapsingTopBarLayout_qmui_collapsedTitleGravity,
                        GravityCompat.START | Gravity.CENTER_VERTICAL));


        mExpandedMarginStart = mExpandedMarginTop = mExpandedMarginEnd = mExpandedMarginBottom =
                a.getDimensionPixelSize(R.styleable.QMUICollapsingTopBarLayout_qmui_expandedTitleMargin, 0);

        if (a.hasValue(R.styleable.QMUICollapsingTopBarLayout_qmui_expandedTitleMarginStart)) {
            mExpandedMarginStart = a.getDimensionPixelSize(
                    R.styleable.QMUICollapsingTopBarLayout_qmui_expandedTitleMarginStart, 0);
        }
        if (a.hasValue(R.styleable.QMUICollapsingTopBarLayout_qmui_expandedTitleMarginEnd)) {
            mExpandedMarginEnd = a.getDimensionPixelSize(
                    R.styleable.QMUICollapsingTopBarLayout_qmui_expandedTitleMarginEnd, 0);
        }
        if (a.hasValue(R.styleable.QMUICollapsingTopBarLayout_qmui_expandedTitleMarginTop)) {
            mExpandedMarginTop = a.getDimensionPixelSize(
                    R.styleable.QMUICollapsingTopBarLayout_qmui_expandedTitleMarginTop, 0);
        }
        if (a.hasValue(R.styleable.QMUICollapsingTopBarLayout_qmui_expandedTitleMarginBottom)) {
            mExpandedMarginBottom = a.getDimensionPixelSize(
                    R.styleable.QMUICollapsingTopBarLayout_qmui_expandedTitleMarginBottom, 0);
        }

        mCollapsingTitleEnabled = a.getBoolean(R.styleable.QMUICollapsingTopBarLayout_qmui_titleEnabled, true);
        setTitle(a.getText(R.styleable.QMUICollapsingTopBarLayout_qmui_title));

        // First load the default text appearances
        mCollapsingTextHelper.setExpandedTextAppearance(R.style.QMUI_CollapsingTopBarLayoutExpanded);
        mCollapsingTextHelper.setCollapsedTextAppearance(R.style.QMUI_CollapsingTopBarLayoutCollapsed);

        // Now overlay any custom text appearances
        if (a.hasValue(R.styleable.QMUICollapsingTopBarLayout_qmui_expandedTitleTextAppearance)) {
            mCollapsingTextHelper.setExpandedTextAppearance(
                    a.getResourceId(R.styleable.QMUICollapsingTopBarLayout_qmui_expandedTitleTextAppearance, 0));
        }
        if (a.hasValue(R.styleable.QMUICollapsingTopBarLayout_qmui_collapsedTitleTextAppearance)) {
            mCollapsingTextHelper.setCollapsedTextAppearance(
                    a.getResourceId(R.styleable.QMUICollapsingTopBarLayout_qmui_collapsedTitleTextAppearance, 0));
        }

        mScrimVisibleHeightTrigger = a.getDimensionPixelSize(
                R.styleable.QMUICollapsingTopBarLayout_qmui_scrimVisibleHeightTrigger, -1);

        mScrimAnimationDuration = a.getInt(
                R.styleable.QMUICollapsingTopBarLayout_qmui_scrimAnimationDuration,
                DEFAULT_SCRIM_ANIMATION_DURATION);

        setContentScrim(a.getDrawable(R.styleable.QMUICollapsingTopBarLayout_qmui_contentScrim));
        setStatusBarScrim(a.getDrawable(R.styleable.QMUICollapsingTopBarLayout_qmui_statusBarScrim));

        mTopBarId = a.getResourceId(R.styleable.QMUICollapsingTopBarLayout_qmui_topBarId, -1);

        a.recycle();

        setWillNotDraw(false);

        ViewCompat.setOnApplyWindowInsetsListener(this,
                new android.support.v4.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                        return setWindowInsets(insets);
                    }
                });
    }

    private WindowInsetsCompat setWindowInsets(WindowInsetsCompat insets) {
        if (Build.VERSION.SDK_INT >= 21) {
            if (applySystemWindowInsets21(insets)) {
                return insets.consumeSystemWindowInsets();
            }
        }
        return insets;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Add an OnOffsetChangedListener if possible
        final ViewParent parent = getParent();
        if (parent instanceof AppBarLayout) {
            // Copy over from the ABL whether we should fit system windows
            ViewCompat.setFitsSystemWindows(this, ViewCompat.getFitsSystemWindows((View) parent));

            if (mOnOffsetChangedListener == null) {
                mOnOffsetChangedListener = new OffsetUpdateListener();
            }
            ((AppBarLayout) parent).addOnOffsetChangedListener(mOnOffsetChangedListener);

            // We're attached, so lets request an inset dispatch
            ViewCompat.requestApplyInsets(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        // Remove our OnOffsetChangedListener if possible and it exists
        final ViewParent parent = getParent();
        if (mOnOffsetChangedListener != null && parent instanceof AppBarLayout) {
            ((AppBarLayout) parent).removeOnOffsetChangedListener(mOnOffsetChangedListener);
        }

        super.onDetachedFromWindow();
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        // If we don't have a toolbar, the scrim will be not be drawn in drawChild() below.
        // Instead, we draw it here, before our collapsing text.
        ensureToolbar();
        if (mTopBar == null && mContentScrim != null && mScrimAlpha > 0) {
            mContentScrim.mutate().setAlpha(mScrimAlpha);
            mContentScrim.draw(canvas);
        }

        // Let the collapsing text helper draw its text
        if (mCollapsingTitleEnabled) {
            mCollapsingTextHelper.draw(canvas);
        }

        // Now draw the status bar scrim
        if (mStatusBarScrim != null && mScrimAlpha > 0) {
            final int topInset = getWindowInsetTop();
            if (topInset > 0) {
                mStatusBarScrim.setBounds(0, -mCurrentOffset, getWidth(),
                        topInset - mCurrentOffset);
                mStatusBarScrim.mutate().setAlpha(mScrimAlpha);
                mStatusBarScrim.draw(canvas);
            }
        }
    }

    private int getWindowInsetTop() {
        if (mLastInsets != null) {
            return mLastInsets.getSystemWindowInsetTop();
        }
        if (mLastInsetRect != null) {
            return mLastInsetRect.top;
        }
        return 0;
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        // This is a little weird. Our scrim needs to be behind the Toolbar (if it is present),
        // but in front of any other children which are behind it. To do this we intercept the
        // drawChild() call, and draw our scrim just before the Toolbar is drawn
        boolean invalidated = false;
        if (mContentScrim != null && mScrimAlpha > 0 && isToolbarChild(child)) {
            mContentScrim.mutate().setAlpha(mScrimAlpha);
            mContentScrim.draw(canvas);
            invalidated = true;
        }
        return super.drawChild(canvas, child, drawingTime) || invalidated;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mContentScrim != null) {
            mContentScrim.setBounds(0, 0, w, h);
        }
    }

    private void ensureToolbar() {
        if (!mRefreshToolbar) {
            return;
        }

        // First clear out the current Toolbar
        mTopBar = null;
        mTopBarDirectChild = null;

        if (mTopBarId != -1) {
            // If we have an ID set, try and find it and it's direct parent to us
            mTopBar = (QMUITopBar) findViewById(mTopBarId);
            if (mTopBar != null) {
                mTopBarDirectChild = findDirectChild(mTopBar);
            }
        }

        if (mTopBar == null) {
            // If we don't have an ID, or couldn't find a Toolbar with the correct ID, try and find
            // one from our direct children
            QMUITopBar topBar = null;
            for (int i = 0, count = getChildCount(); i < count; i++) {
                final View child = getChildAt(i);
                if (child instanceof QMUITopBar) {
                    topBar = (QMUITopBar) child;
                    break;
                }
            }
            mTopBar = topBar;
        }
        mRefreshToolbar = false;
    }

    private boolean isToolbarChild(View child) {
        return (mTopBarDirectChild == null || mTopBarDirectChild == this)
                ? child == mTopBar
                : child == mTopBarDirectChild;
    }

    /**
     * Returns the direct child of this layout, which itself is the ancestor of the
     * given view.
     */
    private View findDirectChild(final View descendant) {
        View directChild = descendant;
        for (ViewParent p = descendant.getParent(); p != this && p != null; p = p.getParent()) {
            if (p instanceof View) {
                directChild = (View) p;
            }
        }
        return directChild;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ensureToolbar();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mLastInsets != null || mLastInsetRect != null) {
            // Shift down any views which are not set to fit system windows
            final int insetTop = getWindowInsetTop();
            for (int i = 0, z = getChildCount(); i < z; i++) {
                final View child = getChildAt(i);
                if (ViewCompat.getFitsSystemWindows(child)) {
                    if (child.getTop() < insetTop) {
                        // If the child isn't set to fit system windows but is drawing within
                        // the inset offset it down
                        ViewCompat.offsetTopAndBottom(child, insetTop);
                    }
                }
            }
        }

        // Update our child view offset helpers. This needs to be done after the title has been
        // setup, so that any Toolbars are in their original position
        for (int i = 0, z = getChildCount(); i < z; i++) {
            getViewOffsetHelper(getChildAt(i)).onViewLayout();
        }

        // Update the collapsed bounds by getting it's transformed bounds
        if (mCollapsingTitleEnabled) {
            // Update the collapsed bounds
            final int maxOffset = getMaxOffsetForPinChild(
                    mTopBarDirectChild != null ? mTopBarDirectChild : mTopBar);
            QMUIViewHelper.getDescendantRect(this, mTopBar, mTmpRect);
//            mTmpRect.top = mTmpRect.top - topBarInsetAdjustTop;
            Rect rect = mTopBar.getTitleContainerRect();
            int horStart = mTmpRect.top + maxOffset;
            mCollapsingTextHelper.setCollapsedBounds(
                    mTmpRect.left + rect.left,
                    horStart + rect.top,
                    mTmpRect.left + rect.right,
                    horStart + rect.bottom);

            // Update the expanded bounds
            mCollapsingTextHelper.setExpandedBounds(
                    mExpandedMarginStart,
                    mTmpRect.top + mExpandedMarginTop,
                    right - left - mExpandedMarginEnd,
                    bottom - top - mExpandedMarginBottom);
            // Now recalculate using the new bounds
            mCollapsingTextHelper.recalculate();
        }

        // Finally, set our minimum height to enable proper AppBarLayout collapsing
        if (mTopBar != null) {
            if (mCollapsingTitleEnabled && TextUtils.isEmpty(mCollapsingTextHelper.getText())) {
                // If we do not currently have a title, try and grab it from the Toolbar
                mCollapsingTextHelper.setText(mTopBar.getTitle());
            }
            if (mTopBarDirectChild == null || mTopBarDirectChild == this) {
                setMinimumHeight(getHeightWithMargins(mTopBar));
            } else {
                setMinimumHeight(getHeightWithMargins(mTopBarDirectChild));
            }
        }

        updateScrimVisibility();
    }

    private static int getHeightWithMargins(@NonNull final View view) {
        final ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof MarginLayoutParams) {
            final MarginLayoutParams mlp = (MarginLayoutParams) lp;
            return view.getHeight() + mlp.topMargin + mlp.bottomMargin;
        }
        return view.getHeight();
    }

    static QMUIViewOffsetHelper getViewOffsetHelper(View view) {
        QMUIViewOffsetHelper offsetHelper = (QMUIViewOffsetHelper) view.getTag(R.id.qmui_view_offset_helper);
        if (offsetHelper == null) {
            offsetHelper = new QMUIViewOffsetHelper(view);
            view.setTag(R.id.qmui_view_offset_helper, offsetHelper);
        }
        return offsetHelper;
    }

    /**
     * Sets the title to be displayed by this view, if enabled.
     *
     * @see #setTitleEnabled(boolean)
     * @see #getTitle()
     */
    public void setTitle(@Nullable CharSequence title) {
        mCollapsingTextHelper.setText(title);
    }

    /**
     * Returns the title currently being displayed by this view. If the title is not enabled, then
     * this will return {@code null}.
     */
    @Nullable
    public CharSequence getTitle() {
        return mCollapsingTitleEnabled ? mCollapsingTextHelper.getText() : null;
    }

    /**
     * Sets whether this view should display its own title.
     * <p>
     * <p>The title displayed by this view will shrink and grow based on the scroll offset.</p>
     *
     * @see #setTitle(CharSequence)
     * @see #isTitleEnabled()
     */
    public void setTitleEnabled(boolean enabled) {
        if (enabled != mCollapsingTitleEnabled) {
            mCollapsingTitleEnabled = enabled;
            requestLayout();
        }
    }

    /**
     * Returns whether this view is currently displaying its own title.
     *
     * @see #setTitleEnabled(boolean)
     */
    public boolean isTitleEnabled() {
        return mCollapsingTitleEnabled;
    }

    /**
     * Set whether the content scrim and/or status bar scrim should be shown or not. Any change
     * in the vertical scroll may overwrite this value. Any visibility change will be animated if
     * this view has already been laid out.
     *
     * @param shown whether the scrims should be shown
     * @see #getStatusBarScrim()
     * @see #getContentScrim()
     */
    public void setScrimsShown(boolean shown) {
        setScrimsShown(shown, ViewCompat.isLaidOut(this) && !isInEditMode());
    }

    /**
     * Set whether the content scrim and/or status bar scrim should be shown or not. Any change
     * in the vertical scroll may overwrite this value.
     *
     * @param shown   whether the scrims should be shown
     * @param animate whether to animate the visibility change
     * @see #getStatusBarScrim()
     * @see #getContentScrim()
     */
    public void setScrimsShown(boolean shown, boolean animate) {
        if (mScrimsAreShown != shown) {
            if (animate) {
                animateScrim(shown ? 0xFF : 0x0);
            } else {
                setScrimAlpha(shown ? 0xFF : 0x0);
            }
            mScrimsAreShown = shown;
        }
    }

    /**
     * @param scrimUpdateListener 为 null 则是 removeUpdateListener
     */
    public void setScrimUpdateListener(ValueAnimator.AnimatorUpdateListener scrimUpdateListener) {
        if (mScrimUpdateListener != scrimUpdateListener) {
            if (mScrimAnimator == null) {
                mScrimUpdateListener = scrimUpdateListener;
            } else {
                if (mScrimUpdateListener != null) {
                    mScrimAnimator.removeUpdateListener(mScrimUpdateListener);
                }
                mScrimUpdateListener = scrimUpdateListener;
                if (mScrimUpdateListener != null) {
                    mScrimAnimator.addUpdateListener(mScrimUpdateListener);
                }
            }
        }
    }

    private void animateScrim(int targetAlpha) {
        ensureToolbar();
        if (mScrimAnimator == null) {
            mScrimAnimator = new ValueAnimator();
            mScrimAnimator.setDuration(mScrimAnimationDuration);
            mScrimAnimator.setInterpolator(
                    targetAlpha > mScrimAlpha
                            ? QMUIInterpolatorStaticHolder.FAST_OUT_LINEAR_IN_INTERPOLATOR
                            : QMUIInterpolatorStaticHolder.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
            mScrimAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    setScrimAlpha((Integer) animator.getAnimatedValue());
                }
            });
            if (mScrimUpdateListener != null) {
                mScrimAnimator.addUpdateListener(mScrimUpdateListener);
            }
        } else if (mScrimAnimator.isRunning()) {
            mScrimAnimator.cancel();
        }

        mScrimAnimator.setIntValues(mScrimAlpha, targetAlpha);
        mScrimAnimator.start();
    }

    void setScrimAlpha(int alpha) {
        if (alpha != mScrimAlpha) {
            final Drawable contentScrim = mContentScrim;
            if (contentScrim != null && mTopBar != null) {
                ViewCompat.postInvalidateOnAnimation(mTopBar);
            }
            mScrimAlpha = alpha;
            ViewCompat.postInvalidateOnAnimation(QMUICollapsingTopBarLayout.this);
        }
    }

    int getScrimAlpha() {
        return mScrimAlpha;
    }

    /**
     * Set the drawable to use for the content scrim from resources. Providing null will disable
     * the scrim functionality.
     *
     * @param drawable the drawable to display
     * @see #getContentScrim()
     */
    public void setContentScrim(@Nullable Drawable drawable) {
        if (mContentScrim != drawable) {
            if (mContentScrim != null) {
                mContentScrim.setCallback(null);
            }
            mContentScrim = drawable != null ? drawable.mutate() : null;
            if (mContentScrim != null) {
                mContentScrim.setBounds(0, 0, getWidth(), getHeight());
                mContentScrim.setCallback(this);
                mContentScrim.setAlpha(mScrimAlpha);
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * Set the color to use for the content scrim.
     *
     * @param color the color to display
     * @see #getContentScrim()
     */
    public void setContentScrimColor(@ColorInt int color) {
        setContentScrim(new ColorDrawable(color));
    }

    /**
     * Set the drawable to use for the content scrim from resources.
     *
     * @param resId drawable resource id
     * @see #getContentScrim()
     */
    public void setContentScrimResource(@DrawableRes int resId) {
        setContentScrim(ContextCompat.getDrawable(getContext(), resId));

    }

    /**
     * Returns the drawable which is used for the foreground scrim.
     *
     * @see #setContentScrim(Drawable)
     */
    @Nullable
    public Drawable getContentScrim() {
        return mContentScrim;
    }

    /**
     * Set the drawable to use for the status bar scrim from resources.
     * Providing null will disable the scrim functionality.
     * <p>
     * <p>This scrim is only shown when we have been given a top system inset.</p>
     *
     * @param drawable the drawable to display
     * @see #getStatusBarScrim()
     */
    public void setStatusBarScrim(@Nullable Drawable drawable) {
        if (mStatusBarScrim != drawable) {
            if (mStatusBarScrim != null) {
                mStatusBarScrim.setCallback(null);
            }
            mStatusBarScrim = drawable != null ? drawable.mutate() : null;
            if (mStatusBarScrim != null) {
                if (mStatusBarScrim.isStateful()) {
                    mStatusBarScrim.setState(getDrawableState());
                }
                DrawableCompat.setLayoutDirection(mStatusBarScrim,
                        ViewCompat.getLayoutDirection(this));
                mStatusBarScrim.setVisible(getVisibility() == VISIBLE, false);
                mStatusBarScrim.setCallback(this);
                mStatusBarScrim.setAlpha(mScrimAlpha);
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    // 从系统源码获取，不作检测
    @SuppressWarnings("ConstantConditions")
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        final int[] state = getDrawableState();
        boolean changed = false;

        Drawable d = mStatusBarScrim;
        if (d != null && d.isStateful()) {
            changed |= d.setState(state);
        }
        d = mContentScrim;
        if (d != null && d.isStateful()) {
            changed |= d.setState(state);
        }
        if (mCollapsingTextHelper != null) {
            changed |= mCollapsingTextHelper.setState(state);
        }

        if (changed) {
            invalidate();
        }
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return super.verifyDrawable(who) || who == mContentScrim || who == mStatusBarScrim;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        final boolean visible = visibility == VISIBLE;
        if (mStatusBarScrim != null && mStatusBarScrim.isVisible() != visible) {
            mStatusBarScrim.setVisible(visible, false);
        }
        if (mContentScrim != null && mContentScrim.isVisible() != visible) {
            mContentScrim.setVisible(visible, false);
        }
    }

    /**
     * Set the color to use for the status bar scrim.
     * <p>
     * <p>This scrim is only shown when we have been given a top system inset.</p>
     *
     * @param color the color to display
     * @see #getStatusBarScrim()
     */
    public void setStatusBarScrimColor(@ColorInt int color) {
        setStatusBarScrim(new ColorDrawable(color));
    }

    /**
     * Set the drawable to use for the content scrim from resources.
     *
     * @param resId drawable resource id
     * @see #getStatusBarScrim()
     */
    public void setStatusBarScrimResource(@DrawableRes int resId) {
        setStatusBarScrim(ContextCompat.getDrawable(getContext(), resId));
    }

    /**
     * Returns the drawable which is used for the status bar scrim.
     *
     * @see #setStatusBarScrim(Drawable)
     */
    @Nullable
    public Drawable getStatusBarScrim() {
        return mStatusBarScrim;
    }

    /**
     * Sets the text color and size for the collapsed title from the specified
     * TextAppearance resource.
     */
    public void setCollapsedTitleTextAppearance(@StyleRes int resId) {
        mCollapsingTextHelper.setCollapsedTextAppearance(resId);
    }

    /**
     * Sets the text color of the collapsed title.
     *
     * @param color The new text color in ARGB format
     */
    public void setCollapsedTitleTextColor(@ColorInt int color) {
        setCollapsedTitleTextColor(ColorStateList.valueOf(color));
    }

    /**
     * Sets the text colors of the collapsed title.
     *
     * @param colors ColorStateList containing the new text colors
     */
    public void setCollapsedTitleTextColor(@NonNull ColorStateList colors) {
        mCollapsingTextHelper.setCollapsedTextColor(colors);
    }

    /**
     * Sets the horizontal alignment of the collapsed title and the vertical gravity that will
     * be used when there is extra space in the collapsed bounds beyond what is required for
     * the title itself.
     */
    public void setCollapsedTitleGravity(int gravity) {
        mCollapsingTextHelper.setCollapsedTextGravity(gravity);
    }

    /**
     * Returns the horizontal and vertical alignment for title when collapsed.
     */
    public int getCollapsedTitleGravity() {
        return mCollapsingTextHelper.getCollapsedTextGravity();
    }

    /**
     * Sets the text color and size for the expanded title from the specified
     * TextAppearance resource.
     */
    public void setExpandedTitleTextAppearance(@StyleRes int resId) {
        mCollapsingTextHelper.setExpandedTextAppearance(resId);
    }

    /**
     * Sets the text color of the expanded title.
     *
     * @param color The new text color in ARGB format
     */
    public void setExpandedTitleColor(@ColorInt int color) {
        setExpandedTitleTextColor(ColorStateList.valueOf(color));
    }

    /**
     * Sets the text colors of the expanded title.
     *
     * @param colors ColorStateList containing the new text colors
     */
    public void setExpandedTitleTextColor(@NonNull ColorStateList colors) {
        mCollapsingTextHelper.setExpandedTextColor(colors);
    }

    /**
     * Sets the horizontal alignment of the expanded title and the vertical gravity that will
     * be used when there is extra space in the expanded bounds beyond what is required for
     * the title itself.
     */
    public void setExpandedTitleGravity(int gravity) {
        mCollapsingTextHelper.setExpandedTextGravity(gravity);
    }

    /**
     * Returns the horizontal and vertical alignment for title when expanded.
     */
    public int getExpandedTitleGravity() {
        return mCollapsingTextHelper.getExpandedTextGravity();
    }

    /**
     * Set the typeface to use for the collapsed title.
     *
     * @param typeface typeface to use, or {@code null} to use the default.
     */
    public void setCollapsedTitleTypeface(@Nullable Typeface typeface) {
        mCollapsingTextHelper.setCollapsedTypeface(typeface);
    }

    /**
     * Returns the typeface used for the collapsed title.
     */
    @NonNull
    public Typeface getCollapsedTitleTypeface() {
        return mCollapsingTextHelper.getCollapsedTypeface();
    }

    /**
     * Set the typeface to use for the expanded title.
     *
     * @param typeface typeface to use, or {@code null} to use the default.
     */
    public void setExpandedTitleTypeface(@Nullable Typeface typeface) {
        mCollapsingTextHelper.setExpandedTypeface(typeface);
    }

    /**
     * Returns the typeface used for the expanded title.
     */
    @NonNull
    public Typeface getExpandedTitleTypeface() {
        return mCollapsingTextHelper.getExpandedTypeface();
    }

    /**
     * Sets the expanded title margins.
     *
     * @param start  the starting title margin in pixels
     * @param top    the top title margin in pixels
     * @param end    the ending title margin in pixels
     * @param bottom the bottom title margin in pixels
     * @see #getExpandedTitleMarginStart()
     * @see #getExpandedTitleMarginTop()
     * @see #getExpandedTitleMarginEnd()
     * @see #getExpandedTitleMarginBottom()
     */
    public void setExpandedTitleMargin(int start, int top, int end, int bottom) {
        mExpandedMarginStart = start;
        mExpandedMarginTop = top;
        mExpandedMarginEnd = end;
        mExpandedMarginBottom = bottom;
        requestLayout();
    }

    /**
     * @return the starting expanded title margin in pixels
     * @see #setExpandedTitleMarginStart(int)
     */
    public int getExpandedTitleMarginStart() {
        return mExpandedMarginStart;
    }

    /**
     * Sets the starting expanded title margin in pixels.
     *
     * @param margin the starting title margin in pixels
     * @see #getExpandedTitleMarginStart()
     */
    public void setExpandedTitleMarginStart(int margin) {
        mExpandedMarginStart = margin;
        requestLayout();
    }

    /**
     * @return the top expanded title margin in pixels
     * @see #setExpandedTitleMarginTop(int)
     */
    public int getExpandedTitleMarginTop() {
        return mExpandedMarginTop;
    }

    /**
     * Sets the top expanded title margin in pixels.
     *
     * @param margin the top title margin in pixels
     * @see #getExpandedTitleMarginTop()
     */
    public void setExpandedTitleMarginTop(int margin) {
        mExpandedMarginTop = margin;
        requestLayout();
    }

    /**
     * @return the ending expanded title margin in pixels
     * @see #setExpandedTitleMarginEnd(int)
     */
    public int getExpandedTitleMarginEnd() {
        return mExpandedMarginEnd;
    }

    /**
     * Sets the ending expanded title margin in pixels.
     *
     * @param margin the ending title margin in pixels
     * @see #getExpandedTitleMarginEnd()
     */
    public void setExpandedTitleMarginEnd(int margin) {
        mExpandedMarginEnd = margin;
        requestLayout();
    }

    /**
     * @return the bottom expanded title margin in pixels
     * @see #setExpandedTitleMarginBottom(int)
     */
    public int getExpandedTitleMarginBottom() {
        return mExpandedMarginBottom;
    }

    /**
     * Sets the bottom expanded title margin in pixels.
     *
     * @param margin the bottom title margin in pixels
     * @see #getExpandedTitleMarginBottom()
     */
    public void setExpandedTitleMarginBottom(int margin) {
        mExpandedMarginBottom = margin;
        requestLayout();
    }

    /**
     * Set the amount of visible height in pixels used to define when to trigger a scrim
     * visibility change.
     * <p>
     * <p>If the visible height of this view is less than the given value, the scrims will be
     * made visible, otherwise they are hidden.</p>
     *
     * @param height value in pixels used to define when to trigger a scrim visibility change
     */
    public void setScrimVisibleHeightTrigger(@IntRange(from = 0) final int height) {
        if (mScrimVisibleHeightTrigger != height) {
            mScrimVisibleHeightTrigger = height;
            // Update the scrim visibility
            updateScrimVisibility();
        }
    }

    /**
     * Returns the amount of visible height in pixels used to define when to trigger a scrim
     * visibility change.
     *
     * @see #setScrimVisibleHeightTrigger(int)
     */
    public int getScrimVisibleHeightTrigger() {
        if (mScrimVisibleHeightTrigger >= 0) {
            // If we have one explicitly set, return it
            return mScrimVisibleHeightTrigger;
        }

        // Otherwise we'll use the default computed value
        final int insetTop = getWindowInsetTop();

        final int minHeight = ViewCompat.getMinimumHeight(this);
        if (minHeight > 0) {
            // If we have a minHeight set, lets use 2 * minHeight (capped at our height)
            return Math.min((minHeight * 2) + insetTop, getHeight());
        }

        // If we reach here then we don't have a min height set. Instead we'll take a
        // guess at 1/3 of our height being visible
        return getHeight() / 3;
    }

    /**
     * Set the duration used for scrim visibility animations.
     *
     * @param duration the duration to use in milliseconds
     */
    public void setScrimAnimationDuration(@IntRange(from = 0) final long duration) {
        mScrimAnimationDuration = duration;
    }

    /**
     * Returns the duration in milliseconds used for scrim visibility animations.
     */
    public long getScrimAnimationDuration() {
        return mScrimAnimationDuration;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof QMUICollapsingTopBarLayout.LayoutParams;
    }

    @Override
    protected QMUICollapsingTopBarLayout.LayoutParams generateDefaultLayoutParams() {
        return new QMUICollapsingTopBarLayout.LayoutParams(QMUICollapsingTopBarLayout.LayoutParams.MATCH_PARENT, QMUICollapsingTopBarLayout.LayoutParams.MATCH_PARENT);
    }

    @Override
    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new QMUICollapsingTopBarLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected FrameLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new QMUICollapsingTopBarLayout.LayoutParams(p);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected boolean fitSystemWindows(Rect insets) {
        return applySystemWindowInsets19(insets);
    }

    @Override
    public boolean applySystemWindowInsets19(Rect insets) {
        Rect newInsets = null;
        if (ViewCompat.getFitsSystemWindows(this)) {
            // If we're set to fit system windows, keep the insets
            newInsets = insets;
        }

        // If our insets have changed, keep them and invalidate the scroll ranges...
        if (!QMUILangHelper.objectEquals(mLastInsets, newInsets)) {
            mLastInsetRect = newInsets;
            requestLayout();
        }

        // Consume the insets. This is done so that child views with fitSystemWindows=true do not
        // get the default padding functionality from View
        return true;
    }

    @Override
    public boolean applySystemWindowInsets21(WindowInsetsCompat insets) {
        WindowInsetsCompat newInsets = null;

        if (ViewCompat.getFitsSystemWindows(this)) {
            // If we're set to fit system windows, keep the insets
            newInsets = insets;
        }

        // If our insets have changed, keep them and invalidate the scroll ranges...
        if (!QMUILangHelper.objectEquals(mLastInsets, newInsets)) {
            mLastInsets = newInsets;
            requestLayout();
        }
        return true;
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {

        private static final float DEFAULT_PARALLAX_MULTIPLIER = 0.5f;

        @RestrictTo(LIBRARY_GROUP)
        @IntDef({
                COLLAPSE_MODE_OFF,
                COLLAPSE_MODE_PIN,
                COLLAPSE_MODE_PARALLAX
        })
        @Retention(RetentionPolicy.SOURCE)
        @interface CollapseMode {
        }

        /**
         * The view will act as normal with no collapsing behavior.
         */
        public static final int COLLAPSE_MODE_OFF = 0;

        /**
         * The view will pin in place until it reaches the bottom of the
         * {@link QMUICollapsingTopBarLayout}.
         */
        public static final int COLLAPSE_MODE_PIN = 1;

        /**
         * The view will scroll in a parallax fashion. See {@link #setParallaxMultiplier(float)}
         * to change the multiplier used.
         */
        public static final int COLLAPSE_MODE_PARALLAX = 2;

        int mCollapseMode = COLLAPSE_MODE_OFF;
        float mParallaxMult = DEFAULT_PARALLAX_MULTIPLIER;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.obtainStyledAttributes(attrs,
                    R.styleable.QMUICollapsingTopBarLayout_Layout);
            mCollapseMode = a.getInt(
                    R.styleable.QMUICollapsingTopBarLayout_Layout_qmui_layout_collapseMode,
                    COLLAPSE_MODE_OFF);
            setParallaxMultiplier(a.getFloat(
                    R.styleable.QMUICollapsingTopBarLayout_Layout_qmui_layout_collapseParallaxMultiplier,
                    DEFAULT_PARALLAX_MULTIPLIER));
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height, gravity);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        @RequiresApi(19)
        @TargetApi(19)
        public LayoutParams(FrameLayout.LayoutParams source) {
            // The copy constructor called here only exists on API 19+.
            super(source);
        }

        /**
         * Set the collapse mode.
         *
         * @param collapseMode one of {@link #COLLAPSE_MODE_OFF}, {@link #COLLAPSE_MODE_PIN}
         *                     or {@link #COLLAPSE_MODE_PARALLAX}.
         */
        public void setCollapseMode(@CollapseMode int collapseMode) {
            mCollapseMode = collapseMode;
        }

        /**
         * Returns the requested collapse mode.
         *
         * @return the current mode. One of {@link #COLLAPSE_MODE_OFF}, {@link #COLLAPSE_MODE_PIN}
         * or {@link #COLLAPSE_MODE_PARALLAX}.
         */
        @CollapseMode
        public int getCollapseMode() {
            return mCollapseMode;
        }

        /**
         * Set the parallax scroll multiplier used in conjunction with
         * {@link #COLLAPSE_MODE_PARALLAX}. A value of {@code 0.0} indicates no movement at all,
         * {@code 1.0f} indicates normal scroll movement.
         *
         * @param multiplier the multiplier.
         * @see #getParallaxMultiplier()
         */
        public void setParallaxMultiplier(float multiplier) {
            mParallaxMult = multiplier;
        }

        /**
         * Returns the parallax scroll multiplier used in conjunction with
         * {@link #COLLAPSE_MODE_PARALLAX}.
         *
         * @see #setParallaxMultiplier(float)
         */
        public float getParallaxMultiplier() {
            return mParallaxMult;
        }
    }

    /**
     * Show or hide the scrims if needed
     */
    final void updateScrimVisibility() {
        if (mContentScrim != null || mStatusBarScrim != null) {
            setScrimsShown(getHeight() + mCurrentOffset < getScrimVisibleHeightTrigger());
        }
    }

    final int getMaxOffsetForPinChild(View child) {
        final QMUIViewOffsetHelper offsetHelper = getViewOffsetHelper(child);
        final QMUICollapsingTopBarLayout.LayoutParams lp = (QMUICollapsingTopBarLayout.LayoutParams) child.getLayoutParams();
        return getHeight()
                - offsetHelper.getLayoutTop()
                - child.getHeight()
                - lp.bottomMargin;
    }

    private class OffsetUpdateListener implements AppBarLayout.OnOffsetChangedListener {
        OffsetUpdateListener() {
        }

        @Override
        public void onOffsetChanged(AppBarLayout layout, int verticalOffset) {
            mCurrentOffset = verticalOffset;

            final int insetTop = getWindowInsetTop();

            for (int i = 0, z = getChildCount(); i < z; i++) {
                final View child = getChildAt(i);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final QMUIViewOffsetHelper offsetHelper = getViewOffsetHelper(child);

                switch (lp.mCollapseMode) {
                    case QMUICollapsingTopBarLayout.LayoutParams.COLLAPSE_MODE_PIN:
                        offsetHelper.setTopAndBottomOffset(
                                QMUILangHelper.constrain(-verticalOffset, 0, getMaxOffsetForPinChild(child)));
                        break;
                    case QMUICollapsingTopBarLayout.LayoutParams.COLLAPSE_MODE_PARALLAX:
                        offsetHelper.setTopAndBottomOffset(
                                Math.round(-verticalOffset * lp.mParallaxMult));
                        break;
                }
            }

            // Show or hide the scrims if needed
            updateScrimVisibility();

            if (mStatusBarScrim != null && insetTop > 0) {
                ViewCompat.postInvalidateOnAnimation(QMUICollapsingTopBarLayout.this);
            }

            // Update the collapsing text's fraction
            final int expandRange = getHeight() - ViewCompat.getMinimumHeight(
                    QMUICollapsingTopBarLayout.this) - insetTop;
            mCollapsingTextHelper.setExpansionFraction(
                    Math.abs(verticalOffset) / (float) expandRange);
        }
    }
}
