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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.alpha.QMUIAlphaImageButton;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.QMUIWindowInsetLayout2;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GestureDetectorCompat;

import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

public class QMUIFullScreenPopup extends QMUIBasePopup<QMUIFullScreenPopup> {
    private OnBlankClickListener mOnBlankClickListener;
    private boolean mAddCloseBtn = false;
    private Drawable mCloseIcon = null;
    private ConstraintLayout.LayoutParams mContentLayoutParams;
    private ConstraintLayout.LayoutParams mCloseIvLayoutParams;
    private int mAnimStyle = NOT_SET;

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

    public QMUIFullScreenPopup animStyle(int animStyle) {
        mAnimStyle = animStyle;
        return this;
    }

    public QMUIFullScreenPopup contentLp(ConstraintLayout.LayoutParams contentLayoutParams) {
        mContentLayoutParams = contentLayoutParams;
        return this;
    }

    public QMUIFullScreenPopup closeIvLp(ConstraintLayout.LayoutParams contentLayoutParams) {
        mCloseIvLayoutParams = contentLayoutParams;
        return this;
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
        if (mCloseIcon == null) {
            mCloseIcon = QMUIResHelper.getAttrDrawable(mContext, R.attr.qmui_popup_fullscreen_close_icon);
        }
        QMUIAlphaImageButton closeBtn = new QMUIAlphaImageButton(mContext);
        closeBtn.setPadding(0, 0, 0, 0);
        closeBtn.setImageDrawable(mCloseIcon);
        closeBtn.setScaleType(ImageView.ScaleType.CENTER);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        closeBtn.setFitsSystemWindows(true);
        return closeBtn;
    }

    public void show(View parent) {
        if (mContentView == null) {
            throw new RuntimeException("you should call view() to set your content view");
        }
        RootView rootView = new RootView(mContext);
        if (mContentLayoutParams == null) {
            mContentLayoutParams = defaultContentLp();
        }
        if (mContentView.getParent() != null) {
            ((ViewGroup) mContentView.getParent()).removeView(mContentView);
        }
        rootView.addView(mContentView, mContentLayoutParams);
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

    class RootView extends QMUIWindowInsetLayout2 {
        private GestureDetectorCompat mGestureDetector;

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
                if (!isBlank && (childView instanceof IPopupTouchInterceptor)) {
                    MotionEvent e = MotionEvent.obtain(event);
                    int offsetX = getScrollX() - childView.getLeft();
                    int offsetY = getScrollY() - childView.getTop();
                    e.offsetLocation(offsetX, offsetY);
                    isBlank = ((IPopupTouchInterceptor) childView).isTouchInBlank(e);
                    e.recycle();
                }
                if (isBlank && mOnBlankClickListener != null) {
                    mOnBlankClickListener.onBlankClick();
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
    }

    public interface OnBlankClickListener {
        void onBlankClick();
    }
}
