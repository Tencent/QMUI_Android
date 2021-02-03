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

package com.qmuiteam.qmui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SimpleArrayMap;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.layout.QMUILayoutHelper;
import com.qmuiteam.qmui.skin.QMUISkinHelper;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.skin.defaultAttr.IQMUISkinDefaultAttrProvider;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.util.QMUIViewOffsetHelper;

public class QMUISlider extends FrameLayout implements IQMUISkinDefaultAttrProvider {
    public static final int PROGRESS_NOT_SET = -1;
    private Paint mBarPaint;
    private int mBarHeight;
    private int mBarNormalColor;
    private int mBarProgressColor;
    private int mRecordProgressColor;
    private boolean mConstraintThumbInMoving = true;
    private Callback mCallback;
    private IThumbView mThumbView;
    private QMUIViewOffsetHelper mThumbViewOffsetHelper;

    private int mTickCount;
    private int mCurrentProgress = 0;
    private boolean mIsProgressFirstSet = false;
    private boolean mClickToChangeProgress = false;
    private boolean mLongTouchToChangeProgress = false;
    private int mRecordProgress = PROGRESS_NOT_SET;

    private int mDownTouchX = 0;
    private int mLastTouchX = 0;
    private boolean mIsThumbTouched = false;
    private boolean mIsMoving = false;
    private int mTouchSlop;
    private RectF mTempRect = new RectF();
    private LongPressAction mLongPressAction = new LongPressAction();

    private static SimpleArrayMap<String, Integer> sDefaultSkinAttrs;

    static {
        sDefaultSkinAttrs = new SimpleArrayMap<>(2);
        sDefaultSkinAttrs.put(QMUISkinValueBuilder.BACKGROUND, R.attr.qmui_skin_support_slider_bar_bg_color);
        sDefaultSkinAttrs.put(QMUISkinValueBuilder.PROGRESS_COLOR, R.attr.qmui_skin_support_slider_bar_progress_color);
        sDefaultSkinAttrs.put(QMUISkinValueBuilder.HINT_COLOR, R.attr.qmui_skin_support_slider_record_progress_color);
    }


    public QMUISlider(@NonNull Context context) {
        this(context, null);
    }

    public QMUISlider(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.QMUISliderStyle);
    }

    public QMUISlider(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = getContext().obtainStyledAttributes(attrs,
                R.styleable.QMUISlider, defStyleAttr, 0);
        mBarHeight = array.getDimensionPixelSize(R.styleable.QMUISlider_qmui_slider_bar_height,
                QMUIDisplayHelper.dp2px(context, 2));
        mBarNormalColor = array.getColor(R.styleable.QMUISlider_qmui_slider_bar_normal_color, Color.WHITE);
        mBarProgressColor = array.getColor(R.styleable.QMUISlider_qmui_slider_bar_progress_color, Color.BLUE);
        mRecordProgressColor = array.getColor(R.styleable.QMUISlider_qmui_slider_bar_record_progress_color, Color.GRAY);
        mTickCount = array.getInt(R.styleable.QMUISlider_qmui_slider_bar_tick_count, 100);
        mConstraintThumbInMoving = array.getBoolean(R.styleable.QMUISlider_qmui_slider_bar_constraint_thumb_in_moving, true);
        int thumbSize = array.getDimensionPixelSize(
                R.styleable.QMUISlider_qmui_slider_bar_thumb_size,
                QMUIDisplayHelper.dp2px(getContext(), 24));
        int thumbStyleAttr = 0;
        String thumbStyleAttrString = array.getString(R.styleable.QMUISlider_qmui_slider_bar_thumb_style_attr);
        if (thumbStyleAttrString != null) {
            thumbStyleAttr = getResources().getIdentifier(
                    thumbStyleAttrString, "attr", context.getPackageName());
        }

        boolean useClipChildrenByDeveloper = array.getBoolean(
                R.styleable.QMUISlider_qmui_slider_bar_use_clip_children_by_developer, false);
        if (!useClipChildrenByDeveloper) {
            int paddingHor = array.getDimensionPixelOffset(
                    R.styleable.QMUISlider_qmui_slider_bar_padding_hor_for_thumb_shadow, 0);
            int paddingVer = array.getDimensionPixelOffset(
                    R.styleable.QMUISlider_qmui_slider_bar_padding_ver_for_thumb_shadow, 0);
            setPadding(paddingHor, paddingVer, paddingHor, paddingVer);
        }
        array.recycle();
        mBarPaint = new Paint();
        mBarPaint.setStyle(Paint.Style.FILL);
        mBarPaint.setAntiAlias(true);
        mTouchSlop = QMUIDisplayHelper.dp2px(context, 2);
        setWillNotDraw(false);
        setClipToPadding(false);
        setClipChildren(false);
        IThumbView thumbView = onCreateThumbView(context, thumbSize, thumbStyleAttr);
        if (!(thumbView instanceof View)) {
            throw new IllegalArgumentException("thumbView must be a instance of View");
        }
        mThumbView = thumbView;
        View thumbAsView = (View) thumbView;
        mThumbViewOffsetHelper = new QMUIViewOffsetHelper(thumbAsView);
        addView(thumbAsView, onCreateThumbLayoutParams());
        thumbView.render(mCurrentProgress, mTickCount);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void setCurrentProgress(int currentProgress) {
        if (!mIsMoving) {
            int progress = QMUILangHelper.constrain(currentProgress, 0, mTickCount);
            if (mCurrentProgress != progress || !mIsProgressFirstSet) {
                mIsProgressFirstSet = true;
                safeSetCurrentProgress(progress);
                if (mCallback != null) {
                    mCallback.onProgressChange(this, progress, mTickCount, false);
                }
                invalidate();
            }
        }
    }

    public void setRecordProgress(int recordProgress) {
        if (recordProgress != mRecordProgress) {
            if (recordProgress != PROGRESS_NOT_SET) {
                recordProgress = QMUILangHelper.constrain(recordProgress, 0, mTickCount);
            }
            mRecordProgress = recordProgress;
            invalidate();
        }
    }

    public int getCurrentProgress() {
        return mCurrentProgress;
    }

    public void setTickCount(int tickCount) {
        if (mTickCount != tickCount) {
            mTickCount = tickCount;
            setCurrentProgress(QMUILangHelper.constrain(mCurrentProgress, 0, mTickCount));
            mThumbView.render(mCurrentProgress, mTickCount);
            invalidate();
        }
    }

    public int getTickCount() {
        return mTickCount;
    }

    public void setThumbSkin(QMUISkinValueBuilder valueBuilder) {
        QMUISkinHelper.setSkinValue(convertThumbToView(), valueBuilder);
    }

    private void safeSetCurrentProgress(int currentProgress) {
        mCurrentProgress = currentProgress;
        mThumbView.render(currentProgress, mTickCount);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getMeasuredHeight() < mBarHeight) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(
                    mBarHeight + getPaddingTop() + getPaddingBottom(), MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected final void onLayout(boolean changed, int left, int top, int right, int bottom) {
        onLayoutCustomChildren(changed, left, top, right, bottom);
        View thumbView = convertThumbToView();
        int paddingTop = getPaddingTop(),
                thumbHeight = thumbView.getMeasuredHeight(),
                thumbWidth = thumbView.getMeasuredWidth();
        int l = getPaddingLeft() + mThumbView.getLeftRightMargin();
        int t = paddingTop +
                (bottom - top - paddingTop - getPaddingBottom() - thumbView.getMeasuredHeight()) / 2;
        thumbView.layout(l, t, l + thumbWidth, t + thumbHeight);
        mThumbViewOffsetHelper.onViewLayout();
    }

    protected void onLayoutCustomChildren(boolean changed, int left, int top, int right, int bottom) {

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mDownTouchX = (int) event.getX();
            mLastTouchX = mDownTouchX;
            mIsThumbTouched = isThumbTouched(event.getX(), event.getY());
            if (mIsThumbTouched) {
                mThumbView.setPress(true);
            }else if(mLongTouchToChangeProgress){
                removeCallbacks(mLongPressAction);
                postOnAnimationDelayed(mLongPressAction, 300);
            }

            if (mCallback != null) {
                mCallback.onTouchDown(this, mCurrentProgress, mTickCount, mIsThumbTouched);
            }

        } else if (action == MotionEvent.ACTION_MOVE) {
            int x = (int) event.getX();
            int dx = x - mLastTouchX;
            mLastTouchX = x;
            if (!mIsMoving && mIsThumbTouched) {
                if (Math.abs(mLastTouchX - mDownTouchX) > mTouchSlop) {
                    removeCallbacks(mLongPressAction);
                    mIsMoving = true;
                    if (mCallback != null) {
                        mCallback.onStartMoving(this, mCurrentProgress, mTickCount);
                    }
                    if (dx > 0) {
                        dx -= mTouchSlop;
                    } else {
                        dx += mTouchSlop;
                    }
                }
            }

            if (mIsMoving) {
                QMUIViewHelper.safeRequestDisallowInterceptTouchEvent(this, true);
                int maxOffset = getMaxThumbOffset();

                int oldProgress = mCurrentProgress;
                if (mConstraintThumbInMoving) {
                    checkTouch(x, maxOffset);
                } else {
                    mThumbViewOffsetHelper.setLeftAndRightOffset(
                            QMUILangHelper.constrain(
                                    mThumbViewOffsetHelper.getLeftAndRightOffset() + dx,
                                    0,
                                    maxOffset)
                    );
                    calculateByThumbPosition(maxOffset);
                }
                if (mCallback != null && oldProgress != mCurrentProgress) {
                    mCallback.onProgressChange(this, mCurrentProgress, mTickCount, true);
                }
                invalidate();
            }
        } else if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_CANCEL) {
            removeCallbacks(mLongPressAction);
            mLastTouchX = -1;
            QMUIViewHelper.safeRequestDisallowInterceptTouchEvent(this, false);
            if (mIsMoving) {
                mIsMoving = false;
                if (mCallback != null) {
                    mCallback.onStopMoving(this, mCurrentProgress, mTickCount);
                }
            }

            if (mIsThumbTouched) {
                mIsThumbTouched = false;
                mThumbView.setPress(false);
            } else if (action == MotionEvent.ACTION_UP) {
                int x = (int) event.getX();
                boolean isRecordProgressClicked = isRecordProgressClicked(x);
                if (Math.abs(x - mDownTouchX) < mTouchSlop && (mClickToChangeProgress || isRecordProgressClicked)) {
                    int oldProgress = mCurrentProgress;
                    if (isRecordProgressClicked) {
                        safeSetCurrentProgress(mRecordProgress);
                    } else {
                        checkTouch(x, getMaxThumbOffset());
                    }
                    invalidate();
                    if (mCallback != null && oldProgress != mCurrentProgress) {
                        mCallback.onProgressChange(this, mCurrentProgress, mTickCount, true);
                    }
                }

            }
            if (mCallback != null) {
                mCallback.onTouchUp(this, mCurrentProgress, mTickCount);
            }
        } else {
            removeCallbacks(mLongPressAction);
        }

        return true;
    }

    private void checkTouch(int touchX, int maxOffset) {
        if(mThumbView == null){
            return;
        }
        int moveX = touchX - getPaddingLeft() - mThumbView.getLeftRightMargin();
        float step = (float) maxOffset / mTickCount;
        if (moveX <= step / 2) {
            mThumbViewOffsetHelper.setLeftAndRightOffset(0);
            safeSetCurrentProgress(0);
        } else if (touchX >= getWidth() - getPaddingRight() - mThumbView.getLeftRightMargin() - step / 2) {
            mThumbViewOffsetHelper.setLeftAndRightOffset(maxOffset);
            safeSetCurrentProgress(mTickCount);
        } else {
            float percent = (float) moveX / (getWidth() - getPaddingLeft() - getPaddingRight() - 2 * mThumbView.getLeftRightMargin());
            int target = (int) (mTickCount * percent + 0.5f);
            mThumbViewOffsetHelper.setLeftAndRightOffset((int) (target * step));
            safeSetCurrentProgress(target);
        }
    }


    public void setClickToChangeProgress(boolean clickToChangeProgress) {
        mClickToChangeProgress = clickToChangeProgress;
    }

    public void setLongTouchToChangeProgress(boolean longTouchToChangeProgress) {
        mLongTouchToChangeProgress = longTouchToChangeProgress;
    }

    public boolean isLongTouchToChangeProgress() {
        return mLongTouchToChangeProgress;
    }

    public boolean isClickToChangeProgress() {
        return mClickToChangeProgress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int l = getPaddingLeft();
        int r = getWidth() - getPaddingRight();
        int bt = getPaddingTop() + (getHeight() - getPaddingTop() - getPaddingBottom() - mBarHeight) / 2;
        int bb = bt + mBarHeight;
        mBarPaint.setColor(mBarNormalColor);
        mTempRect.set(l, bt, r, bb);
        drawRect(canvas, mTempRect, mBarHeight, mBarPaint, false);

        float step = (float) getMaxThumbOffset() / mTickCount;
        int progressOffset = (int) (step * mCurrentProgress);
        mBarPaint.setColor(mBarProgressColor);

        View thumb = convertThumbToView();
        if (thumb != null && thumb.getVisibility() == View.VISIBLE) {
            if (!mIsMoving) {
                mThumbViewOffsetHelper.setLeftAndRightOffset(progressOffset);
            }
            mTempRect.set(l, bt, (thumb.getRight() + thumb.getLeft()) / 2f, bb);
            drawRect(canvas, mTempRect, mBarHeight, mBarPaint, true);
        } else {
            mTempRect.set(l, bt, l + progressOffset, bb);
            drawRect(canvas, mTempRect, mBarHeight, mBarPaint, true);
        }

        drawTick(canvas, mCurrentProgress, mTickCount, l, r, mTempRect.centerY(), mBarPaint, mBarNormalColor, mBarProgressColor);
        if (mRecordProgress != PROGRESS_NOT_SET && thumb != null) {
            mBarPaint.setColor(mRecordProgressColor);
            float recordPos = getPaddingLeft() + mThumbView.getLeftRightMargin() + (int) (step * mRecordProgress);
            mTempRect.set(recordPos, thumb.getTop(), recordPos + thumb.getWidth(), thumb.getBottom());
            drawRecordProgress(canvas, mTempRect, mBarPaint);
        }

    }

    protected void drawRect(Canvas canvas, RectF rect, int barHeight, Paint paint, boolean forProgress) {
        int radius = barHeight / 2;
        canvas.drawRoundRect(rect, radius, radius, paint);
    }

    protected void drawRecordProgress(Canvas canvas, RectF rect, Paint paint) {
        float radius = rect.height() / 2;
        canvas.drawRoundRect(rect, radius, radius, paint);
    }

    protected void drawTick(Canvas canvas, int currentTickCount, int totalTickCount,
                            int left, int right, float y,
                            Paint paint, int barNormalColor, int barProgressColor) {
    }

    public void setBarHeight(int barHeight) {
        if (mBarHeight != barHeight) {
            mBarHeight = barHeight;
            requestLayout();
        }
    }

    public int getBarHeight() {
        return mBarHeight;
    }

    public void setBarNormalColor(int barNormalColor) {
        if (mBarNormalColor != barNormalColor) {
            mBarNormalColor = barNormalColor;
            invalidate();
        }
    }

    public int getBarNormalColor() {
        return mBarNormalColor;
    }

    public void setBarProgressColor(int barProgressColor) {
        if (mBarProgressColor != barProgressColor) {
            mBarProgressColor = barProgressColor;
            invalidate();
        }
    }

    public int getBarProgressColor() {
        return mBarProgressColor;
    }

    public void setRecordProgressColor(int recordProgressColor) {
        if (mRecordProgressColor != recordProgressColor) {
            mRecordProgressColor = recordProgressColor;
            invalidate();
        }
    }

    public int getRecordProgressColor() {
        return mRecordProgressColor;
    }

    public int getRecordProgress() {
        return mRecordProgress;
    }

    public void setConstraintThumbInMoving(boolean constraintThumbInMoving) {
        mConstraintThumbInMoving = constraintThumbInMoving;
    }

    private void calculateByThumbPosition(int maxOffset) {
        View thumbView = convertThumbToView();
        float percent = mThumbViewOffsetHelper.getLeftAndRightOffset() * 1f / maxOffset;
        safeSetCurrentProgress(QMUILangHelper.constrain(
                (int) (mTickCount * percent + 0.5f),
                0,
                mTickCount
        ));
    }

    @NonNull
    protected IThumbView onCreateThumbView(Context context, int thumbSize, int thumbStyleAttr) {
        return new DefaultThumbView(context, thumbSize, thumbStyleAttr);
    }

    protected LayoutParams onCreateThumbLayoutParams() {
        return new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private View convertThumbToView() {
        return (View) mThumbView;
    }

    private boolean isThumbTouched(float x, float y) {
        return isThumbViewTouched(convertThumbToView(), x, y);
    }

    protected boolean isThumbViewTouched(View thumbView, float x, float y) {
        return thumbView.getVisibility() == View.VISIBLE &&
                thumbView.getLeft() <= x && thumbView.getRight() >= x &&
                thumbView.getTop() <= y && thumbView.getBottom() >= y;
    }

    protected boolean isRecordProgressClicked(int x) {
        if (mRecordProgress == PROGRESS_NOT_SET) {
            return false;
        }
        View thumbView = convertThumbToView();
        float percent = mRecordProgress * 1f / mTickCount;
        float left = (getWidth() - getPaddingLeft() - getPaddingRight()) * percent - thumbView.getWidth() / 2f;
        float right = left + thumbView.getWidth();
        return x >= left && x <= right;
    }

    private int getMaxThumbOffset() {
        return getWidth() - getPaddingLeft() - getPaddingRight()
                - mThumbView.getLeftRightMargin() * 2
                - convertThumbToView().getWidth();
    }

    public interface IThumbView {
        void render(int progress, int tickCount);

        void setPress(boolean isPressed);

        int getLeftRightMargin();
    }

    @Override
    public SimpleArrayMap<String, Integer> getDefaultSkinAttrs() {
        return sDefaultSkinAttrs;
    }

    public interface Callback {

        void onProgressChange(QMUISlider slider, int progress, int tickCount, boolean fromUser);

        void onTouchDown(QMUISlider slider, int progress, int tickCount, boolean hitThumb);

        void onTouchUp(QMUISlider slider, int progress, int tickCount);

        void onStartMoving(QMUISlider slider, int progress, int tickCount);

        void onStopMoving(QMUISlider slider, int progress, int tickCount);

        void onLongTouch(QMUISlider slider, int progress, int tickCount);
    }

    public static class DefaultCallback implements Callback {

        @Override
        public void onProgressChange(QMUISlider slider, int progress, int tickCount, boolean fromUser) {

        }

        @Override
        public void onTouchDown(QMUISlider slider, int progress, int tickCount, boolean hitThumb) {

        }

        @Override
        public void onTouchUp(QMUISlider slider, int progress, int tickCount) {

        }

        @Override
        public void onStartMoving(QMUISlider slider, int progress, int tickCount) {

        }

        @Override
        public void onStopMoving(QMUISlider slider, int progress, int tickCount) {

        }

        @Override
        public void onLongTouch(QMUISlider slider, int progress, int tickCount) {

        }
    }


    public static class DefaultThumbView extends View implements IThumbView, IQMUISkinDefaultAttrProvider {

        private final QMUILayoutHelper mLayoutHelper;
        private final int mSize;
        private static SimpleArrayMap<String, Integer> sDefaultSkinAttrs;

        static {
            sDefaultSkinAttrs = new SimpleArrayMap<>(2);
            sDefaultSkinAttrs.put(QMUISkinValueBuilder.BACKGROUND, R.attr.qmui_skin_support_slider_thumb_bg_color);
            sDefaultSkinAttrs.put(QMUISkinValueBuilder.BORDER, R.attr.qmui_skin_support_slider_thumb_border_color);
        }

        public DefaultThumbView(Context context, int size, int defAttr) {
            super(context, null, defAttr);
            mSize = size;
            mLayoutHelper = new QMUILayoutHelper(context, null, defAttr, this);
            mLayoutHelper.setRadius(size / 2);
            setPress(false);
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
            mLayoutHelper.drawDividers(canvas, getWidth(), getHeight());
            mLayoutHelper.dispatchRoundBorderDraw(canvas);
        }

        public void setBorderColor(int color) {
            mLayoutHelper.setBorderColor(color);
            invalidate();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(mSize, mSize);
        }


        @Override
        public void render(int progress, int tickCount) {

        }

        @Override
        public void setPress(boolean isPressed) {

        }

        @Override
        public int getLeftRightMargin() {
            return 0;
        }

        @Override
        public SimpleArrayMap<String, Integer> getDefaultSkinAttrs() {
            return sDefaultSkinAttrs;
        }
    }

    class LongPressAction implements Runnable {

        @Override
        public void run() {
            mIsMoving = true;
            int oldProgress = mCurrentProgress;
            checkTouch(mLastTouchX, getMaxThumbOffset());
            mIsThumbTouched = true;
            mThumbView.setPress(true);
            if (mCallback != null && oldProgress != mCurrentProgress) {
                mCallback.onLongTouch(QMUISlider.this, mCurrentProgress, mTickCount);
            }
        }
    }
}
