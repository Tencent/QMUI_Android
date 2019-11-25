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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewStub;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ListView;

import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author cginechen
 * @date 2016-03-17
 */
public class QMUIViewHelper {

    // copy from View.generateViewId for API <= 16
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);


    private static final int[] APPCOMPAT_CHECK_ATTRS = {
            androidx.appcompat.R.attr.colorPrimary
    };

    public static void checkAppCompatTheme(Context context) {
        TypedArray a = context.obtainStyledAttributes(APPCOMPAT_CHECK_ATTRS);
        final boolean failed = !a.hasValue(0);
        a.recycle();
        if (failed) {
            throw new IllegalArgumentException("You need to use a Theme.AppCompat theme "
                    + "(or descendant) with the design library.");
        }
    }

    /**
     * 获取activity的根view
     */
    public static View getActivityRoot(Activity activity) {
        return ((ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT)).getChildAt(0);
    }

    /**
     * 触发window的insets的广播，使得view的fitSystemWindows得以生效
     */
    @SuppressWarnings("deprecation")
    public static void requestApplyInsets(Window window) {
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            window.getDecorView().requestFitSystemWindows();
        } else if (Build.VERSION.SDK_INT >= 21) {
            window.getDecorView().requestApplyInsets();
        }
    }

    /**
     * 扩展点击区域的范围
     *
     * @param view       需要扩展的元素，此元素必需要有父级元素
     * @param expendSize 需要扩展的尺寸（以sp为单位的）
     */
    public static void expendTouchArea(final View view, final int expendSize) {
        if (view != null) {
            final View parentView = (View) view.getParent();

            parentView.post(new Runnable() {
                @Override
                public void run() {
                    Rect rect = new Rect();
                    view.getHitRect(rect); //如果太早执行本函数，会获取rect失败，因为此时UI界面尚未开始绘制，无法获得正确的坐标
                    rect.left -= expendSize;
                    rect.top -= expendSize;
                    rect.right += expendSize;
                    rect.bottom += expendSize;
                    parentView.setTouchDelegate(new TouchDelegate(rect, view));
                }
            });
        }
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void setBackground(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }

    public static void setBackgroundKeepingPadding(View view, Drawable drawable) {
        int[] padding = new int[]{view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom()};
        view.setBackground(drawable);
        view.setPadding(padding[0], padding[1], padding[2], padding[3]);
    }

    @SuppressWarnings("deprecation")
    public static void setBackgroundKeepingPadding(View view, int backgroundResId) {
        setBackgroundKeepingPadding(view, ContextCompat.getDrawable(view.getContext(), backgroundResId));
    }

    public static void setBackgroundColorKeepPadding(View view, @ColorInt int color) {
        int[] padding = new int[]{view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom()};
        view.setBackgroundColor(color);
        view.setPadding(padding[0], padding[1], padding[2], padding[3]);
    }

    /**
     * 对 View 的做背景闪动的动画
     */
    public static void playBackgroundBlinkAnimation(final View v, @ColorInt int bgColor) {
        if (v == null) {
            return;
        }
        int[] alphaArray = new int[]{0, 255, 0};
        playViewBackgroundAnimation(v, bgColor, alphaArray, 300);
    }

    /**
     * 对 View 做背景色变化的动作
     *
     * @param v            做背景色变化的View
     * @param bgColor      背景色
     * @param alphaArray   背景色变化的alpha数组，如 int[]{255,0} 表示从纯色变化到透明
     * @param stepDuration 每一步变化的时长
     * @param endAction    动画结束后的回调
     */
    public static Animator playViewBackgroundAnimation(final View v, @ColorInt int bgColor, int[] alphaArray, int stepDuration, final Runnable endAction) {
        int animationCount = alphaArray.length - 1;

        Drawable bgDrawable = new ColorDrawable(bgColor);
        final Drawable oldBgDrawable = v.getBackground();
        setBackgroundKeepingPadding(v, bgDrawable);

        List<Animator> animatorList = new ArrayList<>();
        for (int i = 0; i < animationCount; i++) {
            ObjectAnimator animator = ObjectAnimator.ofInt(v.getBackground(), "alpha", alphaArray[i], alphaArray[i + 1]);
            animatorList.add(animator);
        }

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(stepDuration);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setBackgroundKeepingPadding(v, oldBgDrawable);
                if (endAction != null) {
                    endAction.run();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animatorSet.playSequentially(animatorList);
        animatorSet.start();
        return animatorSet;
    }

    public static void playViewBackgroundAnimation(final View v, @ColorInt int bgColor, int[] alphaArray, int stepDuration) {
        playViewBackgroundAnimation(v, bgColor, alphaArray, stepDuration, null);
    }

    /**
     * 对 View 做背景色变化的动作
     *
     * @param v            做背景色变化的View
     * @param startColor   动画开始时 View 的背景色
     * @param endColor     动画结束时 View 的背景色
     * @param duration     动画总时长
     * @param repeatCount  动画重复次数
     * @param setAnimTagId 将动画设置tag给view,若为0则不设置
     * @param endAction    动画结束后的回调
     */
    public static void playViewBackgroundAnimation(final View v, @ColorInt int startColor, @ColorInt int endColor, long duration, int repeatCount, int setAnimTagId, final Runnable endAction) {
        final Drawable oldBgDrawable = v.getBackground(); // 存储旧的背景
        QMUIViewHelper.setBackgroundColorKeepPadding(v, startColor);
        final ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(startColor, endColor);
        anim.setDuration(duration / (repeatCount + 1));
        anim.setRepeatCount(repeatCount);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.setEvaluator(new ArgbEvaluator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                QMUIViewHelper.setBackgroundColorKeepPadding(v, (Integer) animation.getAnimatedValue());
            }
        });
        if (setAnimTagId != 0) {
            v.setTag(setAnimTagId, anim);
        }
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setBackgroundKeepingPadding(v, oldBgDrawable);
                if (endAction != null) {
                    endAction.run();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.start();
    }

    public static void playViewBackgroundAnimation(final View v, int startColor, int endColor, long duration) {
        playViewBackgroundAnimation(v, startColor, endColor, duration, 0, 0, null);
    }

    public static int generateViewId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return View.generateViewId();
        } else {
            for (; ; ) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        }
    }

    /**
     * <p>对 View 做透明度变化的进场动画。</p>
     * <p>相关方法 {@link #fadeOut(View, int, Animation.AnimationListener, boolean)}</p>
     *
     * @param view            做动画的 View
     * @param duration        动画时长(毫秒)
     * @param listener        动画回调
     * @param isNeedAnimation 是否需要动画
     */
    public static AlphaAnimation fadeIn(View view, int duration, Animation.AnimationListener listener, boolean isNeedAnimation) {
        if (view == null) {
            return null;
        }
        if (isNeedAnimation) {
            view.setVisibility(View.VISIBLE);
            AlphaAnimation alpha = new AlphaAnimation(0, 1);
            alpha.setInterpolator(new DecelerateInterpolator());
            alpha.setDuration(duration);
            alpha.setFillAfter(true);
            if (listener != null) {
                alpha.setAnimationListener(listener);
            }
            view.startAnimation(alpha);
            return alpha;
        } else {
            view.setAlpha(1);
            view.setVisibility(View.VISIBLE);
            return null;
        }
    }

    /**
     * <p>对 View 做透明度变化的退场动画</p>
     * <p>相关方法 {@link #fadeIn(View, int, Animation.AnimationListener, boolean)}</p>
     *
     * @param view            做动画的 View
     * @param duration        动画时长(毫秒)
     * @param listener        动画回调
     * @param isNeedAnimation 是否需要动画
     */
    public static AlphaAnimation fadeOut(final View view, int duration, final Animation.AnimationListener listener, boolean isNeedAnimation) {
        if (view == null) {
            return null;
        }
        if (isNeedAnimation) {
            AlphaAnimation alpha = new AlphaAnimation(1, 0);
            alpha.setInterpolator(new DecelerateInterpolator());
            alpha.setDuration(duration);
            alpha.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    if (listener != null) {
                        listener.onAnimationStart(animation);
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.setVisibility(View.GONE);
                    if (listener != null) {
                        listener.onAnimationEnd(animation);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    if (listener != null) {
                        listener.onAnimationRepeat(animation);
                    }
                }
            });
            view.startAnimation(alpha);
            return alpha;
        } else {
            view.setVisibility(View.GONE);
            return null;
        }
    }

    public static void clearValueAnimator(Animator animator) {
        if (animator != null) {
            animator.removeAllListeners();
            if (animator instanceof ValueAnimator) {
                ((ValueAnimator) animator).removeAllUpdateListeners();
            }

            if (Build.VERSION.SDK_INT >= 19) {
                animator.pause();
            }
            animator.cancel();
        }
    }

    public static Rect calcViewScreenLocation(View view) {
        int[] location = new int[2];
        // 获取控件在屏幕中的位置，返回的数组分别为控件左顶点的 x、y 的值
        view.getLocationOnScreen(location);
        return new Rect(location[0], location[1], location[0] + view.getWidth(),
                location[1] + view.getHeight());
    }

    /**
     * <p>对 View 做上下位移的进场动画</p>
     * <p>相关方法 {@link #slideOut(View, int, Animation.AnimationListener, boolean, QMUIDirection)}</p>
     *
     * @param view            做动画的 View
     * @param duration        动画时长(毫秒)
     * @param listener        动画回调
     * @param isNeedAnimation 是否需要动画
     * @param direction       进场动画的方向
     * @return 动画对应的 Animator 对象, 注意无动画时返回 null
     */
    public static
    @Nullable
    TranslateAnimation slideIn(final View view, int duration, final Animation.AnimationListener listener, boolean isNeedAnimation, QMUIDirection direction) {
        if (view == null) {
            return null;
        }
        if (isNeedAnimation) {
            TranslateAnimation translate = null;
            switch (direction) {
                case LEFT_TO_RIGHT:
                    translate = new TranslateAnimation(
                            Animation.RELATIVE_TO_SELF, -1f, Animation.RELATIVE_TO_SELF, 0f,
                            Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f
                    );
                    break;
                case TOP_TO_BOTTOM:
                    translate = new TranslateAnimation(
                            Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                            Animation.RELATIVE_TO_SELF, -1f, Animation.RELATIVE_TO_SELF, 0f
                    );
                    break;
                case RIGHT_TO_LEFT:
                    translate = new TranslateAnimation(
                            Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f,
                            Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f
                    );
                    break;
                case BOTTOM_TO_TOP:
                    translate = new TranslateAnimation(
                            Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                            Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f
                    );
                    break;
            }
            translate.setInterpolator(new DecelerateInterpolator());
            translate.setDuration(duration);
            translate.setFillAfter(true);
            if (listener != null) {
                translate.setAnimationListener(listener);
            }
            view.setVisibility(View.VISIBLE);
            view.startAnimation(translate);
            return translate;
        } else {
            view.clearAnimation();
            view.setVisibility(View.VISIBLE);

            return null;
        }
    }

    /**
     * <p>对 View 做上下位移的退场动画</p>
     * <p>相关方法 {@link #slideIn(View, int, Animation.AnimationListener, boolean, QMUIDirection)}</p>
     *
     * @param view            做动画的 View
     * @param duration        动画时长(毫秒)
     * @param listener        动画回调
     * @param isNeedAnimation 是否需要动画
     * @param direction       进场动画的方向
     * @return 动画对应的 Animator 对象, 注意无动画时返回 null
     */
    public static
    @Nullable
    TranslateAnimation slideOut(final View view, int duration, final Animation.AnimationListener listener, boolean isNeedAnimation, QMUIDirection direction) {
        if (view == null) {
            return null;
        }
        if (isNeedAnimation) {
            TranslateAnimation translate = null;
            switch (direction) {
                case LEFT_TO_RIGHT:
                    translate = new TranslateAnimation(
                            Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f,
                            Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f
                    );
                    break;
                case TOP_TO_BOTTOM:
                    translate = new TranslateAnimation(
                            Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                            Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f
                    );
                    break;
                case RIGHT_TO_LEFT:
                    translate = new TranslateAnimation(
                            Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, -1f,
                            Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f
                    );
                    break;
                case BOTTOM_TO_TOP:
                    translate = new TranslateAnimation(
                            Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                            Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, -1f
                    );
                    break;
            }
            translate.setInterpolator(new DecelerateInterpolator());
            translate.setDuration(duration);
            translate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    if (listener != null) {
                        listener.onAnimationStart(animation);
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.setVisibility(View.GONE);
                    if (listener != null) {
                        listener.onAnimationEnd(animation);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    if (listener != null) {
                        listener.onAnimationRepeat(animation);
                    }
                }
            });
            view.startAnimation(translate);
            return translate;
        } else {
            view.clearAnimation();
            view.setVisibility(View.GONE);
            return null;
        }

    }

    /**
     * 对 View 设置 paddingLeft
     *
     * @param view  需要被设置的 View
     * @param value 设置的值
     */
    public static void setPaddingLeft(View view, int value) {
        if (value != view.getPaddingLeft()) {
            view.setPadding(value, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
        }
    }

    /**
     * 对 View 设置 paddingTop
     *
     * @param view  需要被设置的 View
     * @param value 设置的值
     */
    public static void setPaddingTop(View view, int value) {
        if (value != view.getPaddingTop()) {
            view.setPadding(view.getPaddingLeft(), value, view.getPaddingRight(), view.getPaddingBottom());
        }
    }

    /**
     * 对 View 设置 paddingRight
     *
     * @param view  需要被设置的 View
     * @param value 设置的值
     */
    public static void setPaddingRight(View view, int value) {
        if (value != view.getPaddingRight()) {
            view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), value, view.getPaddingBottom());
        }
    }

    /**
     * 对 View 设置 paddingBottom
     *
     * @param view  需要被设置的 View
     * @param value 设置的值
     */
    public static void setPaddingBottom(View view, int value) {
        if (value != view.getPaddingBottom()) {
            view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), value);
        }
    }

    /**
     * 判断是否需要对 LineSpacingExtra 进行额外的兼容处理
     * 安卓 5.0 以下版本中，LineSpacingExtra 在最后一行也会产生作用，因此会多出一个 LineSpacingExtra 的空白，可以通过该方法判断后进行兼容处理
     * if (QMUIViewHelper.getISLastLineSpacingExtraError()) {
     * textView.bottomMargin = -3dp;
     * } else {
     * textView.bottomMargin = 0;
     * }
     */
    public static boolean getIsLastLineSpacingExtraError() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }


    /**
     * requestDisallowInterceptTouchEvent 的安全方法。存在它的原因是 QMUIPullRefreshLayout 会拦截这个事件
     *
     * @param view
     * @param value
     */
    public static void safeRequestDisallowInterceptTouchEvent(@NonNull View view, boolean value) {
        ViewParent viewParent = view.getParent();
        if (viewParent != null) {
            ViewParent layout = viewParent;
            while (layout != null) {
                if (layout instanceof QMUIPullRefreshLayout) {
                    ((QMUIPullRefreshLayout) layout).openSafeDisallowInterceptTouchEvent();
                }
                layout = layout.getParent();
            }
            viewParent.requestDisallowInterceptTouchEvent(value);
        }
    }

    /**
     * 把 ViewStub inflate 之后在其中根据 id 找 View
     *
     * @param parentView     包含 ViewStub 的 View
     * @param viewStubId     要从哪个 ViewStub 来 inflate
     * @param inflatedViewId 最终要找到的 View 的 id
     * @return id 为 inflatedViewId 的 View
     */
    public static View findViewFromViewStub(View parentView, int viewStubId, int inflatedViewId) {
        if (null == parentView) {
            return null;
        }
        View view = parentView.findViewById(inflatedViewId);
        if (null == view) {
            ViewStub vs = (ViewStub) parentView.findViewById(viewStubId);
            if (null == vs) {
                return null;
            }
            view = vs.inflate();
            if (null != view) {
                view = view.findViewById(inflatedViewId);
            }
        }
        return view;
    }

    /**
     * inflate ViewStub 并返回对应的 View。
     */
    public static View findViewFromViewStub(View parentView, int viewStubId, int inflatedViewId, int inflateLayoutResId) {
        if (null == parentView) {
            return null;
        }
        View view = parentView.findViewById(inflatedViewId);
        if (null == view) {
            ViewStub vs = (ViewStub) parentView.findViewById(viewStubId);
            if (null == vs) {
                return null;
            }
            if (vs.getLayoutResource() < 1 && inflateLayoutResId > 0) {
                vs.setLayoutResource(inflateLayoutResId);
            }
            view = vs.inflate();
            if (null != view) {
                view = view.findViewById(inflatedViewId);
            }
        }
        return view;
    }

    public static void safeSetImageViewSelected(ImageView imageView, boolean selected) {
        // imageView setSelected 实现有问题。
        // resizeFromDrawable 中判断 drawable size 是否改变而调用 requestLayout，看似合理，但不会被调用
        // 因为 super.setSelected(selected) 会调用 refreshDrawableState
        // 而从 android 6 以后， ImageView 会重载refreshDrawableState，并在里面处理了 drawable size 改变的问题,
        // 从而导致 resizeFromDrawable 的判断失效
        Drawable drawable = imageView.getDrawable();
        if (drawable == null) {
            return;
        }
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        imageView.setSelected(selected);
        if (drawable.getIntrinsicWidth() != drawableWidth || drawable.getIntrinsicHeight() != drawableHeight) {
            imageView.requestLayout();
        }
    }


    /**
     * please use ImageViewCompat.setImageTintList() replace this.
     */
    @Deprecated
    public static ColorFilter setImageViewTintColor(ImageView imageView, @ColorInt int tintColor) {
        LightingColorFilter colorFilter = new LightingColorFilter(Color.argb(255, 0, 0, 0), tintColor);
        imageView.setColorFilter(colorFilter);
        return colorFilter;
    }

    /**
     * 判断 ListView 是否已经滚动到底部。
     *
     * @param listView 需要被判断的 ListView。
     * @return ListView 已经滚动到底部则返回 true，否则返回 false。
     */
    public static boolean isListViewAlreadyAtBottom(ListView listView) {
        if (listView.getAdapter() == null || listView.getHeight() == 0) {
            return false;
        }

        if (listView.getLastVisiblePosition() == listView.getAdapter().getCount() - 1) {
            View lastItemView = listView.getChildAt(listView.getChildCount() - 1);
            if (lastItemView != null && lastItemView.getBottom() == listView.getHeight()) {
                return true;
            }
        }
        return false;
    }


    /**
     * Retrieve the transformed bounding rect of an arbitrary descendant view.
     * This does not need to be a direct child.
     *
     * @param descendant descendant view to reference
     * @param out        rect to set to the bounds of the descendant view
     */
    public static void getDescendantRect(ViewGroup parent, View descendant, Rect out) {
        out.set(0, 0, descendant.getWidth(), descendant.getHeight());
        ViewGroupHelper.offsetDescendantRect(parent, descendant, out);
    }


    private static class ViewGroupHelper {
        private static final ThreadLocal<Matrix> sMatrix = new ThreadLocal<>();
        private static final ThreadLocal<RectF> sRectF = new ThreadLocal<>();

        public static void offsetDescendantRect(ViewGroup group, View child, Rect rect) {
            Matrix m = sMatrix.get();
            if (m == null) {
                m = new Matrix();
                sMatrix.set(m);
            } else {
                m.reset();
            }

            offsetDescendantMatrix(group, child, m);

            RectF rectF = sRectF.get();
            if (rectF == null) {
                rectF = new RectF();
                sRectF.set(rectF);
            }
            rectF.set(rect);
            m.mapRect(rectF);
            rect.set((int) (rectF.left + 0.5f), (int) (rectF.top + 0.5f),
                    (int) (rectF.right + 0.5f), (int) (rectF.bottom + 0.5f));
        }

        static void offsetDescendantMatrix(ViewParent target, View view, Matrix m) {
            final ViewParent parent = view.getParent();
            if (parent instanceof View && parent != target) {
                final View vp = (View) parent;
                offsetDescendantMatrix(target, vp, m);
                m.preTranslate(-vp.getScrollX(), -vp.getScrollY());
            }

            m.preTranslate(view.getLeft(), view.getTop());

            if (!view.getMatrix().isIdentity()) {
                m.preConcat(view.getMatrix());
            }
        }
    }
}
