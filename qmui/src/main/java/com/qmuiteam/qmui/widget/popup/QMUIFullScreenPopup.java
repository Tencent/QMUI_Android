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

package com.qmuiteam.qmui.widget.popup;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.qmuiteam.qmui.QMUIInterpolatorStaticHolder;
import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.alpha.QMUIAlphaImageButton;
import com.qmuiteam.qmui.skin.QMUISkinHelper;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.util.QMUIViewOffsetHelper;
import com.qmuiteam.qmui.widget.IBlankTouchDetector;
import com.qmuiteam.qmui.widget.IWindowInsetKeyboardConsumer;
import com.qmuiteam.qmui.widget.QMUIWindowInsetLayout2;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GestureDetectorCompat;

import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

public class QMUIFullScreenPopup extends QMUIBasePopup<QMUIFullScreenPopup> {

    private static OnKeyBoardListener sOffsetKeyboardHeightListener;
    private static OnKeyBoardListener sOffsetHalfKeyboardHeightListener;

    public static OnKeyBoardListener getOffsetKeyboardHeightListener() {
        if (sOffsetKeyboardHeightListener == null) {
            sOffsetKeyboardHeightListener = new KeyboardPercentOffsetListener(1f);
        }
        return sOffsetKeyboardHeightListener;
    }

    public static OnKeyBoardListener getOffsetHalfKeyboardHeightListener() {
        if (sOffsetHalfKeyboardHeightListener == null) {
            sOffsetHalfKeyboardHeightListener = new KeyboardPercentOffsetListener(0.5f);
        }
        return sOffsetHalfKeyboardHeightListener;
    }


    private OnBlankClickListener mOnBlankClickListener;
    private boolean mAddCloseBtn = false;
    private int mCloseIconAttr = R.attr.qmui_skin_support_popup_close_icon;
    private Drawable mCloseIcon = null;
    private ConstraintLayout.LayoutParams mCloseIvLayoutParams;
    private int mAnimStyle = NOT_SET;
    private ArrayList<ViewInfo> mViews = new ArrayList<>();

    public QMUIFullScreenPopup(Context context) {
        super(context);
        mWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        dimAmount(0.6f);
    }

    public QMUIFullScreenPopup onBlankClick(OnBlankClickListener onBlankClickListener) {
        mOnBlankClickListener = onBlankClickListener;
        return this;
    }

    public QMUIFullScreenPopup closeBtn(boolean close) {
        mAddCloseBtn = close;
        return this;
    }

    public QMUIFullScreenPopup closeIcon(Drawable drawable) {
        mCloseIcon = drawable;
        return this;
    }

    public QMUIFullScreenPopup closeIconAttr(int closeIconAttr) {
        mCloseIconAttr = closeIconAttr;
        return this;
    }

    public QMUIFullScreenPopup closeLp(ConstraintLayout.LayoutParams contentLayoutParams) {
        mCloseIvLayoutParams = contentLayoutParams;
        return this;
    }

    public int getCloseBtnId() {
        return R.id.qmui_popup_close_btn_id;
    }

    public QMUIFullScreenPopup animStyle(int animStyle) {
        mAnimStyle = animStyle;
        return this;
    }

    public QMUIFullScreenPopup addView(View view, ConstraintLayout.LayoutParams lp, OnKeyBoardListener onKeyBoardListener) {
        mViews.add(new ViewInfo(view, lp, onKeyBoardListener));
        return this;
    }

    public QMUIFullScreenPopup addView(View view, ConstraintLayout.LayoutParams lp) {
        return addView(view, lp, null);
    }

    public QMUIFullScreenPopup addView(View view, OnKeyBoardListener onKeyBoardListener) {
        mViews.add(new ViewInfo(view, defaultContentLp(), onKeyBoardListener));
        return this;
    }

    public QMUIFullScreenPopup addView(View view) {
        return addView(view, defaultContentLp());
    }

    private ConstraintLayout.LayoutParams defaultContentLp() {
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        lp.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        return lp;
    }

    private ConstraintLayout.LayoutParams defaultCloseIvLp() {
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        lp.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.bottomMargin = QMUIDisplayHelper.dp2px(mContext, 48);
        return lp;
    }

    private QMUIAlphaImageButton createCloseIv() {
        QMUIAlphaImageButton closeBtn = new QMUIAlphaImageButton(mContext);
        closeBtn.setPadding(0, 0, 0, 0);
        closeBtn.setScaleType(ImageView.ScaleType.CENTER);
        closeBtn.setId(R.id.qmui_popup_close_btn_id);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        closeBtn.setFitsSystemWindows(true);
        Drawable drawable = null;
        if(mCloseIcon != null){
            drawable = mCloseIcon;
        }else if(mCloseIconAttr != 0){
            QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire().src(mCloseIconAttr);
            QMUISkinHelper.setSkinValue(closeBtn, builder);
            builder.release();
            drawable = QMUIResHelper.getAttrDrawable(mContext, mCloseIconAttr);
        }
        closeBtn.setImageDrawable(drawable);
        return closeBtn;
    }

    public void show(View parent) {
        if (mViews.isEmpty()) {
            throw new RuntimeException("you should call addView() to add content view");
        }
        ArrayList<ViewInfo> views = new ArrayList<>(mViews);
        RootView rootView = new RootView(mContext);
        for (int i = 0; i < views.size(); i++) {
            ViewInfo info = mViews.get(i);
            View view = info.view;
            if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
            }

            rootView.addView(view, info.lp);
        }
        if (mAddCloseBtn) {
            if (mCloseIvLayoutParams == null) {
                mCloseIvLayoutParams = defaultCloseIvLp();
            }
            rootView.addView(createCloseIv(), mCloseIvLayoutParams);
        }
        mWindow.setContentView(rootView);
        if (mAnimStyle != NOT_SET) {
            mWindow.setAnimationStyle(mAnimStyle);
        }

        showAtLocation(parent, 0, 0);
    }

    @Override
    protected void modifyWindowLayoutParams(WindowManager.LayoutParams lp) {
        lp.flags |= FLAG_LAYOUT_IN_SCREEN | FLAG_LAYOUT_INSET_DECOR;
        super.modifyWindowLayoutParams(lp);
    }

    public interface OnBlankClickListener {
        void onBlankClick(QMUIFullScreenPopup popup);
    }

    class RootView extends QMUIWindowInsetLayout2 implements IWindowInsetKeyboardConsumer {
        private GestureDetectorCompat mGestureDetector;
        private int mLastKeyboardShowHeight = 0;

        public RootView(Context context) {
            super(context);
            mGestureDetector = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (mGestureDetector.onTouchEvent(event)) {
                View childView = findChildViewUnder(event.getX(), event.getY());
                boolean isBlank = childView == null;
                if (!isBlank && (childView instanceof IBlankTouchDetector)) {
                    MotionEvent e = MotionEvent.obtain(event);
                    int offsetX = getScrollX() - childView.getLeft();
                    int offsetY = getScrollY() - childView.getTop();
                    e.offsetLocation(offsetX, offsetY);
                    isBlank = ((IBlankTouchDetector) childView).isTouchInBlank(e);
                    e.recycle();
                }
                if (isBlank && mOnBlankClickListener != null) {
                    mOnBlankClickListener.onBlankClick(QMUIFullScreenPopup.this);
                }
            }
            return true;
        }


        private View findChildViewUnder(float x, float y) {
            final int count = getChildCount();
            for (int i = count - 1; i >= 0; i--) {
                final View child = getChildAt(i);
                final float translationX = child.getTranslationX();
                final float translationY = child.getTranslationY();
                if (x >= child.getLeft() + translationX
                        && x <= child.getRight() + translationX
                        && y >= child.getTop() + translationY
                        && y <= child.getBottom() + translationY) {
                    return child;
                }
            }
            return null;
        }

        @Override
        public boolean applySystemWindowInsets19(Rect insets) {
            super.applySystemWindowInsets19(insets);
            return true;
        }

        @Override
        @TargetApi(21)
        public boolean applySystemWindowInsets21(Object insets) {
            super.applySystemWindowInsets21(insets);
            return true;
        }

        @Override
        public void onHandleKeyboard(int keyboardInset) {
            if (keyboardInset > 0) {
                mLastKeyboardShowHeight = keyboardInset;
                for (ViewInfo viewInfo : mViews) {
                    if (viewInfo.onKeyBoardListener != null) {
                        viewInfo.onKeyBoardListener.onKeyboardToggle(viewInfo.view, true, keyboardInset, getHeight());
                    }
                }
            } else {
                for (ViewInfo viewInfo : mViews) {
                    if (viewInfo.onKeyBoardListener != null) {
                        viewInfo.onKeyBoardListener.onKeyboardToggle(viewInfo.view, false, mLastKeyboardShowHeight, getHeight());
                    }
                }
            }
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            for (ViewInfo viewInfo : mViews) {
                View view = viewInfo.view;
                QMUIViewOffsetHelper offsetHelper = (QMUIViewOffsetHelper) view.getTag(R.id.qmui_view_offset_helper);
                if (offsetHelper != null) {
                    offsetHelper.onViewLayout();
                }
            }
        }
    }

    class ViewInfo {
        private OnKeyBoardListener onKeyBoardListener;
        private View view;
        private ConstraintLayout.LayoutParams lp;

        public ViewInfo(View view, ConstraintLayout.LayoutParams lp, @Nullable OnKeyBoardListener onKeyBoardListener) {
            this.view = view;
            this.lp = lp;
            this.onKeyBoardListener = onKeyBoardListener;
        }
    }

    public static QMUIViewOffsetHelper getOrCreateViewOffsetHelper(View view) {
        QMUIViewOffsetHelper offsetHelper = (QMUIViewOffsetHelper) view.getTag(R.id.qmui_view_offset_helper);
        if (offsetHelper == null) {
            offsetHelper = new QMUIViewOffsetHelper(view);
            view.setTag(R.id.qmui_view_offset_helper, offsetHelper);
        }
        return offsetHelper;
    }

    public interface OnKeyBoardListener {
        void onKeyboardToggle(View view, boolean toShow, int keyboardHeight, int rootViewHeight);
    }

    public static class KeyboardPercentOffsetListener implements OnKeyBoardListener {
        private float mPercent;
        private ValueAnimator mAnimator;

        public KeyboardPercentOffsetListener(float percent) {
            mPercent = percent;
        }

        @Override
        public void onKeyboardToggle(View view, boolean toShow, int keyboardHeight, int rootViewHeight) {
            final QMUIViewOffsetHelper offsetHelper = QMUIFullScreenPopup.getOrCreateViewOffsetHelper(view);
            if (mAnimator != null) {
                QMUIViewHelper.clearValueAnimator(mAnimator);
            }
            int target = toShow ? (int) (-keyboardHeight * mPercent) : 0;
            mAnimator = ValueAnimator.ofInt(offsetHelper.getTopAndBottomOffset(), target);
            mAnimator.setInterpolator(QMUIInterpolatorStaticHolder.FAST_OUT_SLOW_IN_INTERPOLATOR);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    offsetHelper.setTopAndBottomOffset((Integer) animation.getAnimatedValue());
                }
            });
            mAnimator.start();
        }
    }
}
