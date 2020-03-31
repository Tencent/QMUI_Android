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

package com.qmuiteam.qmui.widget.tab;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SimpleArrayMap;

import com.qmuiteam.qmui.QMUIInterpolatorStaticHolder;
import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.layout.IQMUILayout;
import com.qmuiteam.qmui.layout.QMUILayoutHelper;
import com.qmuiteam.qmui.skin.IQMUISkinHandlerView;
import com.qmuiteam.qmui.skin.QMUISkinHelper;
import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.skin.defaultAttr.IQMUISkinDefaultAttrProvider;
import com.qmuiteam.qmui.util.QMUIColorHelper;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>用于横向多个 Tab 的布局，可以灵活配置 Tab</p>
 * <ul>
 * <li>可以用 xml 和 QMUITabSegment 提供的 set 方法统一配置文字颜色、icon 位置、是否要下划线等</li>
 * <li>每个 Tab 都可以非常灵活的配置，如果没有提供相关配置，则使用 QMUIBasicTabSegment 提供的配置，具体参考 {@link QMUITab}</li>
 * </ul>
 * <p>
 * <h3>使用case: </h3>
 * <ul>
 * <li>
 * 如果你希望自己设置 Tab 的文案或图片，那么通过{@link #addTab(QMUITab)}添加 Tab:
 * <code>
 * QMUIBasicTabSegment mTabSegment = new QMUIBasicTabSegment((getContext());
 * // config mTabSegment
 * QMUITabBuilder tabBuilder = mTabSegment.tabBuilder()
 * mTabSegment.addTab(tabBuilder.setText("item 1").build());
 * mTabSegment.addTab(tabBuilder.setText("item 2").build());
 * mTabSegment.notifyDataChanged();
 * </code>
 * </li>
 * <li>
 * 如果你想更改tab,则调用{@link #updateTabText(int, String)} 或者 {@link #replaceTab(int, QMUITab)}
 * <code>
 * mTabSegment.updateTabText(1, "update item content");
 * mTabSegment.replaceTab(1, tabBuilder.setText("replace item").build());
 * </code>
 * </li>
 * <li>
 * 如果你想更换全部Tab,需要在addTab前调用{@link #reset()}进行重置，addTab后调用{@link #notifyDataChanged()} 将数据应用到View上：
 * <code>
 * mTabSegment.reset();
 * // update mTabSegment with new config
 * QMUITabBuilder tabBuilder = mTabSegment.tabBuilder()
 * mTabSegment.addTab(tabBuilder.setText("new item 1").build());
 * mTabSegment.addTab(tabBuilder.setText("new item 1").build());
 * mTabSegment.notifyDataChanged();
 * </code>
 * </li>
 * </ul>
 *
 * @author cginechen
 * @date 2016-01-27
 */
public class QMUIBasicTabSegment extends HorizontalScrollView implements IQMUILayout, IQMUISkinHandlerView, IQMUISkinDefaultAttrProvider {

    private static final String TAG = "QMUIBasicTabSegment";

    // mode: wrap content and scroll / match parent and avg item width
    public static final int MODE_SCROLLABLE = 0;
    public static final int MODE_FIXED = 1;
    public static final int NO_POSITION = -1;


    private final ArrayList<OnTabSelectedListener> mSelectedListeners = new ArrayList<>();
    private Container mContentLayout;

    protected int mCurrentSelectedIndex = NO_POSITION;
    protected int mPendingSelectedIndex = NO_POSITION;

    private QMUITabIndicator mIndicator = null;
    private boolean mHideIndicatorWhenTabCountLessTwo = true;

    /**
     * TabSegmentMode
     */
    @Mode
    private int mMode = MODE_FIXED;
    /**
     * item gap in MODE_SCROLLABLE
     */
    private int mItemSpaceInScrollMode;


    private QMUITabAdapter mTabAdapter;

    protected QMUITabBuilder mTabBuilder;

    protected Animator mSelectAnimator;
    private OnTabClickListener mOnTabClickListener;

    private boolean mIsInSelectTab = false;
    private QMUILayoutHelper mLayoutHelper;
    private static SimpleArrayMap<String, Integer> sDefaultSkinAttrs;

    static {
        sDefaultSkinAttrs = new SimpleArrayMap<>(3);
        sDefaultSkinAttrs.put(QMUISkinValueBuilder.BOTTOM_SEPARATOR, R.attr.qmui_skin_support_tab_separator_color);
        sDefaultSkinAttrs.put(QMUISkinValueBuilder.TOP_SEPARATOR, R.attr.qmui_skin_support_tab_separator_color);
        sDefaultSkinAttrs.put(QMUISkinValueBuilder.BACKGROUND, R.attr.qmui_skin_support_tab_bg);
    }

    public QMUIBasicTabSegment(Context context) {
        this(context, null);
    }

    public QMUIBasicTabSegment(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.QMUITabSegmentStyle);
    }

    public QMUIBasicTabSegment(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        mLayoutHelper = new QMUILayoutHelper(context, attrs, defStyleAttr, this);
        init(context, attrs, defStyleAttr);
        setHorizontalScrollBarEnabled(false);
        setClipToPadding(false);
        setClipChildren(false);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {

        TypedArray array = context.obtainStyledAttributes(attrs,
                R.styleable.QMUITabSegment, defStyleAttr, 0);

        // indicator
        boolean hasIndicator = array.getBoolean(R.styleable.QMUITabSegment_qmui_tab_has_indicator, false);
        int indicatorHeight = array.getDimensionPixelSize(
                R.styleable.QMUITabSegment_qmui_tab_indicator_height,
                getResources().getDimensionPixelSize(R.dimen.qmui_tab_segment_indicator_height));
        boolean indicatorTop = array.getBoolean(R.styleable.QMUITabSegment_qmui_tab_indicator_top, false);
        boolean indicatorWidthFollowContent = array.getBoolean(
                R.styleable.QMUITabSegment_qmui_tab_indicator_with_follow_content, false);
        mIndicator = createTabIndicatorFromXmlInfo(hasIndicator, indicatorHeight,
                indicatorTop, indicatorWidthFollowContent);

        // tabBuilder
        int normalTextSize = array.getDimensionPixelSize(
                R.styleable.QMUITabSegment_android_textSize,
                getResources().getDimensionPixelSize(R.dimen.qmui_tab_segment_text_size));
        normalTextSize = array.getDimensionPixelSize(
                R.styleable.QMUITabSegment_qmui_tab_normal_text_size, normalTextSize);
        int selectedTextSize = normalTextSize;
        selectedTextSize = array.getDimensionPixelSize(
                R.styleable.QMUITabSegment_qmui_tab_selected_text_size, selectedTextSize);
        mTabBuilder = new QMUITabBuilder(context)
                .setTextSize(normalTextSize, selectedTextSize)
                .setIconPosition(array.getInt(R.styleable.QMUITabSegment_qmui_tab_icon_position,
                        QMUITab.ICON_POSITION_LEFT));
        mMode = array.getInt(R.styleable.QMUITabSegment_qmui_tab_mode, MODE_FIXED);
        mItemSpaceInScrollMode = array.getDimensionPixelSize(
                R.styleable.QMUITabSegment_qmui_tab_space, QMUIDisplayHelper.dp2px(context, 10));
        array.recycle();


        mContentLayout = new Container(context);
        addView(mContentLayout, new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mTabAdapter = createTabAdapter(mContentLayout);
    }

    public void setDefaultTextSize(int normalTextSize, int selectedTextSize) {
        mTabBuilder.setTextSize(normalTextSize, selectedTextSize);
    }

    public void setDefaultTabIconPosition(@QMUITab.IconPosition int iconPosition) {
        mTabBuilder.setIconPosition(iconPosition);
    }

    public void updateParentTabBuilder(TabBuilderUpdater updater) {
        updater.update(mTabBuilder);
    }

    protected QMUITabAdapter createTabAdapter(ViewGroup tabParentView) {
        return new QMUITabAdapter(this, tabParentView);
    }

    protected QMUITabIndicator createTabIndicatorFromXmlInfo(boolean hasIndicator,
                                                             int indicatorHeight,
                                                             boolean indicatorTop,
                                                             boolean indicatorWidthFollowContent) {
        if (!hasIndicator) {
            return null;
        }
        return new QMUITabIndicator(indicatorHeight, indicatorTop, indicatorWidthFollowContent);
    }

    public QMUITabBuilder tabBuilder() {
        // do not change mTabBuilder to keep common config not changed
        return new QMUITabBuilder(mTabBuilder);
    }

    /**
     * replace with custom indicator
     *
     * @param indicator if null, present there is not a indicator
     */
    public void setIndicator(@Nullable QMUITabIndicator indicator) {
        mIndicator = indicator;
        mContentLayout.requestLayout();
    }

    public void setHideIndicatorWhenTabCountLessTwo(boolean hideIndicatorWhenTabCountLessTwo) {
        mHideIndicatorWhenTabCountLessTwo = hideIndicatorWhenTabCountLessTwo;
    }

    public void setItemSpaceInScrollMode(int itemSpaceInScrollMode) {
        mItemSpaceInScrollMode = itemSpaceInScrollMode;
    }

    /**
     * clear all tabs
     */
    public void reset() {
        mTabAdapter.clear();
        mCurrentSelectedIndex = NO_POSITION;
        if (mSelectAnimator != null) {
            mSelectAnimator.cancel();
            mSelectAnimator = null;
        }
    }


    /**
     * add a tab to QMUITabSegment
     *
     * @param tab QMUITab
     * @return return this to chain
     */
    public QMUIBasicTabSegment addTab(QMUITab tab) {
        mTabAdapter.addItem(tab);
        return this;
    }


    /**
     * notify dataChanged event to QMUITabSegment
     */
    public void notifyDataChanged() {
        mTabAdapter.setup();
    }

    public void addOnTabSelectedListener(@NonNull OnTabSelectedListener listener) {
        if (!mSelectedListeners.contains(listener)) {
            mSelectedListeners.add(listener);
        }
    }

    public void removeOnTabSelectedListener(@NonNull OnTabSelectedListener listener) {
        mSelectedListeners.remove(listener);
    }

    public void clearOnTabSelectedListeners() {
        mSelectedListeners.clear();
    }

    public int getMode() {
        return mMode;
    }

    public void setMode(@Mode int mode) {
        if (mMode != mode) {
            mMode = mode;
            if (mode == MODE_SCROLLABLE) {
                mTabBuilder.setGravity(Gravity.LEFT);
            }
            mContentLayout.invalidate();
        }
    }


    void onClickTab(int index) {
        if (mSelectAnimator != null || needPreventEvent()) {
            return;
        }
        QMUITab model = mTabAdapter.getItem(index);
        if (model != null) {
            selectTab(index, false, true);
        }
        if (mOnTabClickListener != null) {
            mOnTabClickListener.onTabClick(index);
        }
    }

    protected boolean needPreventEvent() {
        return false;
    }


    void onDoubleClick(int index) {
        if (mSelectedListeners.isEmpty()) {
            return;
        }
        QMUITab model = mTabAdapter.getItem(index);
        if (model != null) {
            dispatchTabDoubleTap(index);
        }
    }


    private void dispatchTabSelected(int index) {
        for (int i = mSelectedListeners.size() - 1; i >= 0; i--) {
            mSelectedListeners.get(i).onTabSelected(index);
        }
    }

    private void dispatchTabUnselected(int index) {
        for (int i = mSelectedListeners.size() - 1; i >= 0; i--) {
            mSelectedListeners.get(i).onTabUnselected(index);
        }
    }

    private void dispatchTabReselected(int index) {
        for (int i = mSelectedListeners.size() - 1; i >= 0; i--) {
            mSelectedListeners.get(i).onTabReselected(index);
        }
    }

    private void dispatchTabDoubleTap(int index) {
        for (int i = mSelectedListeners.size() - 1; i >= 0; i--) {
            mSelectedListeners.get(i).onDoubleTap(index);
        }
    }

    public void selectTab(int index) {
        selectTab(index, false, false);
    }

    public void selectTab(final int index, boolean noAnimation, boolean fromTabClick) {
        if (mIsInSelectTab) {
            return;
        }
        mIsInSelectTab = true;

        List<QMUITabView> listViews = mTabAdapter.getViews();

        if (listViews.size() != mTabAdapter.getSize()) {
            mTabAdapter.setup();
            listViews = mTabAdapter.getViews();
        }

        if (listViews.size() == 0 || listViews.size() <= index) {
            mIsInSelectTab = false;
            return;
        }

        if (mSelectAnimator != null || needPreventEvent()) {
            mPendingSelectedIndex = index;
            mIsInSelectTab = false;
            return;
        }

        if (mCurrentSelectedIndex == index) {
            if (fromTabClick) {
                // dispatch re select only when click tab
                dispatchTabReselected(index);
            }
            mIsInSelectTab = false;
            // invalidate mContentLayout to sure indicator is drawn if needed
            mContentLayout.invalidate();
            return;
        }


        if (mCurrentSelectedIndex > listViews.size()) {
            Log.i(TAG, "selectTab: current selected index is bigger than views size.");
            mCurrentSelectedIndex = NO_POSITION;
        }

        // first time to select
        if (mCurrentSelectedIndex == NO_POSITION) {
            QMUITab model = mTabAdapter.getItem(index);
            layoutIndicator(model, true);
            listViews.get(index).setSelectFraction(1f);
            dispatchTabSelected(index);
            mCurrentSelectedIndex = index;
            mIsInSelectTab = false;
            return;
        }

        final int prev = mCurrentSelectedIndex;
        final QMUITab prevModel = mTabAdapter.getItem(prev);
        final QMUITabView prevView = listViews.get(prev);
        final QMUITab nowModel = mTabAdapter.getItem(index);
        final QMUITabView nowView = listViews.get(index);

        if (noAnimation) {
            dispatchTabUnselected(prev);
            dispatchTabSelected(index);
            prevView.setSelectFraction(0f);
            nowView.setSelectFraction(1f);
            if (mMode == MODE_SCROLLABLE) {
                int scrollX = getScrollX(),
                        w = getWidth(),
                        cw = mContentLayout.getWidth(),
                        nl = nowView.getLeft(),
                        nw = nowView.getWidth();
                int paddingHor = getPaddingLeft() + getPaddingRight();
                int size = mTabAdapter.getSize();
                int maxScrollX = cw - w + paddingHor;
                if (index > prev) {
                    if (index >= size - 2) {
                        smoothScrollBy(maxScrollX - scrollX, 0);
                    } else {
                        int nextWidth = listViews.get(index + 1).getWidth();
                        int targetScrollX = Math.min(maxScrollX, nl - (w - getPaddingRight() * 2 - nextWidth - nw - mItemSpaceInScrollMode));
                        targetScrollX -= nextWidth - nw;
                        if (scrollX < targetScrollX) {
                            smoothScrollBy(targetScrollX - scrollX, 0);
                        }
                    }
                } else {
                    if (index <= 1) {
                        smoothScrollBy(-scrollX, 0);
                    } else {
                        int prevWidth = listViews.get(index - 1).getWidth();
                        int targetScrollX = Math.max(0, nl - prevWidth - mItemSpaceInScrollMode);
                        if (targetScrollX < scrollX) {
                            smoothScrollBy(targetScrollX - scrollX, 0);
                        }
                    }
                }
            }

            mCurrentSelectedIndex = index;
            mIsInSelectTab = false;
            layoutIndicator(nowModel, true);
            return;
        }

        final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setInterpolator(QMUIInterpolatorStaticHolder.LINEAR_INTERPOLATOR);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animValue = (float) animation.getAnimatedValue();
                prevView.setSelectFraction(1 - animValue);
                nowView.setSelectFraction(animValue);
                layoutIndicatorInTransition(prevModel, nowModel, animValue);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mSelectAnimator = animation;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mSelectAnimator = null;
                prevView.setSelectFraction(0f);
                nowView.setSelectFraction(1f);
                dispatchTabSelected(index);
                dispatchTabUnselected(prev);
                mCurrentSelectedIndex = index;
                if (mPendingSelectedIndex != NO_POSITION && !needPreventEvent()) {
                    selectTab(mPendingSelectedIndex, true, false);
                    mPendingSelectedIndex = NO_POSITION;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mSelectAnimator = null;
                prevView.setSelectFraction(1f);
                nowView.setSelectFraction(0f);
                layoutIndicator(prevModel, true);

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setDuration(200);
        animator.start();
        mIsInSelectTab = false;
    }

    private void layoutIndicator(QMUITab model, boolean invalidate) {
        if (model == null || mIndicator == null) {
            return;
        }
        mIndicator.updateInfo(model.contentLeft, model.contentWidth, model.selectedColorAttr == 0 ? model.selectColor : QMUISkinHelper.getSkinColor(this, model.selectedColorAttr));
        if (invalidate) {
            mContentLayout.invalidate();
        }
    }

    private void layoutIndicatorInTransition(QMUITab preModel, QMUITab targetModel, float offsetPercent) {
        if (mIndicator == null) {
            return;
        }
        final int leftDistance = targetModel.contentLeft - preModel.contentLeft;
        final int widthDistance = targetModel.contentWidth - preModel.contentWidth;
        final int targetLeft = (int) (preModel.contentLeft + leftDistance * offsetPercent);
        final int targetWidth = (int) (preModel.contentWidth + widthDistance * offsetPercent);
        int indicatorColor = QMUIColorHelper.computeColor(
                preModel.selectedColorAttr == 0 ? preModel.selectColor : QMUISkinHelper.getSkinColor(this, preModel.selectedColorAttr),
                targetModel.selectedColorAttr == 0 ? targetModel.selectColor : QMUISkinHelper.getSkinColor(this, targetModel.selectedColorAttr),
                offsetPercent);
        mIndicator.updateInfo(targetLeft, targetWidth, indicatorColor);
        mContentLayout.invalidate();
    }

    public void updateIndicatorPosition(final int index, float offsetPercent) {
        if (mSelectAnimator != null || mIsInSelectTab || offsetPercent == 0) {
            return;
        }

        int targetIndex;
        if (offsetPercent < 0) {
            targetIndex = index - 1;
            offsetPercent = -offsetPercent;
        } else {
            targetIndex = index + 1;
        }

        final List<QMUITabView> listViews = mTabAdapter.getViews();
        if (listViews.size() <= index || listViews.size() <= targetIndex) {
            return;
        }
        QMUITab preModel = mTabAdapter.getItem(index);
        QMUITab targetModel = mTabAdapter.getItem(targetIndex);
        QMUITabView preView = listViews.get(index);
        QMUITabView targetView = listViews.get(targetIndex);
        preView.setSelectFraction(1 - offsetPercent);
        targetView.setSelectFraction(offsetPercent);
        layoutIndicatorInTransition(preModel, targetModel, offsetPercent);
    }

    /**
     * 改变 Tab 的文案
     *
     * @param index Tab 的 index
     * @param text  新文案
     */
    public void updateTabText(int index, String text) {
        QMUITab model = mTabAdapter.getItem(index);
        if (model == null) {
            return;
        }
        model.setText(text);
        notifyDataChanged();
    }

    /**
     * 整个 Tab 替换
     *
     * @param index 需要被替换的 Tab 的 index
     * @param model 新的 Tab
     */
    public void replaceTab(int index, QMUITab model) {
        try {
            if (mCurrentSelectedIndex == index) {
                // re select
                mCurrentSelectedIndex = NO_POSITION;
            }
            mTabAdapter.replaceItem(index, model);
            notifyDataChanged();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void setOnTabClickListener(OnTabClickListener onTabClickListener) {
        mOnTabClickListener = onTabClickListener;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        if (getChildCount() > 0) {
            final View child = getChildAt(0);
            int paddingHor = getPaddingLeft() + getPaddingRight();
            child.measure(MeasureSpec.makeMeasureSpec(widthSize - paddingHor, MeasureSpec.EXACTLY), heightMeasureSpec);
            if (widthMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(Math.min(widthSize, child.getMeasuredWidth() + paddingHor), heightMeasureSpec);
                return;
            }
        }
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }


    public int getSelectedIndex() {
        return mCurrentSelectedIndex;
    }

    public int getTabCount() {
        return mTabAdapter.getSize();
    }

    /**
     * get {@link QMUITab} by index
     *
     * @param index index
     * @return QMUITab
     */
    public QMUITab getTab(int index) {
        return mTabAdapter.getItem(index);
    }


    /**
     * show signCount/redPoint by index
     *
     * @param index the index of tab
     * @param count if count > 0, show signCount; else if count == 0 show redPoint; else show nothing
     */
    public void showSignCountView(Context context, int index, int count) {
        QMUITab tab = mTabAdapter.getItem(index);
        tab.setSignCount(count);
        notifyDataChanged();
    }

    /**
     * clear signCount/redPoint by index
     *
     * @param index the index of tab
     */
    public void clearSignCountView(int index) {
        QMUITab tab = mTabAdapter.getItem(index);
        tab.clearSignCountOrRedPoint();
        notifyDataChanged();
    }

    /**
     * get sign count by index
     *
     * @param index the index of tab
     */
    public int getSignCount(int index) {
        QMUITab tab = mTabAdapter.getItem(index);
        return tab.getSignCount();
    }

    /**
     * is redPoint showing ?
     *
     * @param index the index of tab
     * @return true if redPoint is showing
     */
    public boolean isRedPointShowing(int index) {
        return mTabAdapter.getItem(index).isRedPointShowing();
    }

    @IntDef(value = {MODE_SCROLLABLE, MODE_FIXED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }


    public interface OnTabClickListener {
        /**
         * 当某个 Tab 被点击时会触发
         *
         * @param index 被点击的 Tab 下标
         */
        void onTabClick(int index);
    }

    public interface OnTabSelectedListener {
        /**
         * 当某个 Tab 被选中时会触发
         *
         * @param index 被选中的 Tab 下标
         */
        void onTabSelected(int index);

        /**
         * 当某个 Tab 被取消选中时会触发
         *
         * @param index 被取消选中的 Tab 下标
         */
        void onTabUnselected(int index);

        /**
         * 当某个 Tab 处于被选中状态下再次被点击时会触发
         *
         * @param index 被再次点击的 Tab 下标
         */
        void onTabReselected(int index);

        /**
         * 当某个 Tab 被双击时会触发
         *
         * @param index 被双击的 Tab 下标
         */
        void onDoubleTap(int index);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mCurrentSelectedIndex != NO_POSITION && mMode == MODE_SCROLLABLE) {
            final QMUITabView view = mTabAdapter.getViews().get(mCurrentSelectedIndex);
            if (getScrollX() > view.getLeft()) {
                scrollTo(view.getLeft(), 0);
            } else {
                int realWidth = getWidth() - getPaddingRight() - getPaddingLeft();
                if (getScrollX() + realWidth < view.getRight()) {
                    scrollBy(view.getRight() - realWidth - getScrollX(), 0);
                }
            }
        }
    }


    private final class Container extends ViewGroup {

        public Container(Context context) {
            super(context);
            setClipChildren(false);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
            List<QMUITabView> childViews = mTabAdapter.getViews();
            int size = childViews.size();
            int i;

            int visibleChild = 0;
            for (i = 0; i < size; i++) {
                View child = childViews.get(i);
                if (child.getVisibility() == VISIBLE) {
                    visibleChild++;
                }
            }
            if (size == 0 || visibleChild == 0) {
                setMeasuredDimension(widthSpecSize, heightSpecSize);
                return;
            }

            int childHeight = heightSpecSize - getPaddingTop() - getPaddingBottom();
            int childWidthMeasureSpec, childHeightMeasureSpec, resultWidthSize = 0;
            if (mMode == MODE_FIXED) {
                resultWidthSize = widthSpecSize;
                int modeFixItemWidth = widthSpecSize / visibleChild;
                for (i = 0; i < size; i++) {
                    final View child = childViews.get(i);
                    if (child.getVisibility() != VISIBLE) {
                        continue;
                    }
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(modeFixItemWidth, MeasureSpec.EXACTLY);
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

                    // reset
                    QMUITab tab = mTabAdapter.getItem(i);
                    tab.leftAddonMargin = 0;
                    tab.rightAddonMargin = 0;
                }
            } else {
                float totalWeight = 0;
                for (i = 0; i < size; i++) {
                    final View child = childViews.get(i);
                    if (child.getVisibility() != VISIBLE) {
                        continue;
                    }
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSpecSize, MeasureSpec.AT_MOST);
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                    resultWidthSize += child.getMeasuredWidth() + mItemSpaceInScrollMode;

                    QMUITab tab = mTabAdapter.getItem(i);
                    totalWeight += tab.leftSpaceWeight + tab.rightSpaceWeight;

                    // reset first
                    tab.leftAddonMargin = 0;
                    tab.rightAddonMargin = 0;
                }

                resultWidthSize -= mItemSpaceInScrollMode;

                if (totalWeight > 0 && resultWidthSize < widthSpecSize) {
                    int remain = widthSpecSize - resultWidthSize;
                    resultWidthSize = widthSpecSize;
                    for (i = 0; i < size; i++) {
                        final View child = childViews.get(i);
                        if (child.getVisibility() != VISIBLE) {
                            continue;
                        }
                        QMUITab tab = mTabAdapter.getItem(i);
                        tab.leftAddonMargin = (int) (remain * tab.leftSpaceWeight / totalWeight);
                        tab.rightAddonMargin = (int) (remain * tab.rightSpaceWeight / totalWeight);
                    }
                }
            }

            setMeasuredDimension(resultWidthSize, heightSpecSize);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            List<QMUITabView> childViews = mTabAdapter.getViews();
            int size = childViews.size();
            int i;
            int visibleChild = 0;
            for (i = 0; i < size; i++) {
                View child = childViews.get(i);
                if (child.getVisibility() == VISIBLE) {
                    visibleChild++;
                }
            }

            if (size == 0 || visibleChild == 0) {
                return;
            }

            int usedLeft = getPaddingLeft();
            for (i = 0; i < size; i++) {
                QMUITabView childView = childViews.get(i);
                if (childView.getVisibility() != VISIBLE) {
                    continue;
                }
                final int childMeasureWidth = childView.getMeasuredWidth();
                QMUITab model = mTabAdapter.getItem(i);
                usedLeft += model.leftAddonMargin;
                childView.layout(usedLeft, getPaddingTop(),
                        usedLeft + childMeasureWidth, b - t - getPaddingBottom());


                int oldLeft, oldWidth, newLeft, newWidth;
                oldLeft = model.contentLeft;
                oldWidth = model.contentWidth;
                if (mMode == MODE_FIXED && (mIndicator != null && mIndicator.isIndicatorWidthFollowContent())) {
                    newLeft = usedLeft + childView.getContentViewLeft();
                    newWidth = childView.getContentViewWidth();
                } else {
                    newLeft = usedLeft;
                    newWidth = childMeasureWidth;
                }
                if (oldLeft != newLeft || oldWidth != newWidth) {
                    model.contentLeft = newLeft;
                    model.contentWidth = newWidth;
                }
                usedLeft = usedLeft + childMeasureWidth + model.rightAddonMargin +
                        (mMode == MODE_SCROLLABLE ? mItemSpaceInScrollMode : 0);
            }

            if (mCurrentSelectedIndex != NO_POSITION && mSelectAnimator == null
                    && !needPreventEvent()) {
                layoutIndicator(mTabAdapter.getItem(mCurrentSelectedIndex), false);
            }
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
            if (mIndicator != null && (!mHideIndicatorWhenTabCountLessTwo || mTabAdapter.getSize() > 1)) {
                mIndicator.draw(this, canvas, getPaddingTop(), getHeight() - getPaddingBottom());
            }
        }
    }

    @Override
    public void updateTopDivider(int topInsetLeft, int topInsetRight, int topDividerHeight, int topDividerColor) {
        mLayoutHelper.updateTopDivider(topInsetLeft, topInsetRight, topDividerHeight, topDividerColor);
        invalidate();
    }

    @Override
    public void updateBottomDivider(int bottomInsetLeft, int bottomInsetRight, int bottomDividerHeight, int bottomDividerColor) {
        mLayoutHelper.updateBottomDivider(bottomInsetLeft, bottomInsetRight, bottomDividerHeight, bottomDividerColor);
        invalidate();
    }

    @Override
    public void updateLeftDivider(int leftInsetTop, int leftInsetBottom, int leftDividerWidth, int leftDividerColor) {
        mLayoutHelper.updateLeftDivider(leftInsetTop, leftInsetBottom, leftDividerWidth, leftDividerColor);
        invalidate();
    }

    @Override
    public void updateRightDivider(int rightInsetTop, int rightInsetBottom, int rightDividerWidth, int rightDividerColor) {
        mLayoutHelper.updateRightDivider(rightInsetTop, rightInsetBottom, rightDividerWidth, rightDividerColor);
        invalidate();
    }

    @Override
    public void onlyShowTopDivider(int topInsetLeft, int topInsetRight,
                                   int topDividerHeight, int topDividerColor) {
        mLayoutHelper.onlyShowTopDivider(topInsetLeft, topInsetRight, topDividerHeight, topDividerColor);
        invalidate();
    }

    @Override
    public void onlyShowBottomDivider(int bottomInsetLeft, int bottomInsetRight,
                                      int bottomDividerHeight, int bottomDividerColor) {
        mLayoutHelper.onlyShowBottomDivider(bottomInsetLeft, bottomInsetRight, bottomDividerHeight, bottomDividerColor);
        invalidate();
    }

    @Override
    public void onlyShowLeftDivider(int leftInsetTop, int leftInsetBottom, int leftDividerWidth, int leftDividerColor) {
        mLayoutHelper.onlyShowLeftDivider(leftInsetTop, leftInsetBottom, leftDividerWidth, leftDividerColor);
        invalidate();
    }

    @Override
    public void onlyShowRightDivider(int rightInsetTop, int rightInsetBottom, int rightDividerWidth, int rightDividerColor) {
        mLayoutHelper.onlyShowRightDivider(rightInsetTop, rightInsetBottom, rightDividerWidth, rightDividerColor);
        invalidate();
    }


    @Override
    public void setTopDividerAlpha(int dividerAlpha) {
        mLayoutHelper.setTopDividerAlpha(dividerAlpha);
        invalidate();
    }

    @Override
    public void setBottomDividerAlpha(int dividerAlpha) {
        mLayoutHelper.setBottomDividerAlpha(dividerAlpha);
        invalidate();
    }

    @Override
    public void setLeftDividerAlpha(int dividerAlpha) {
        mLayoutHelper.setLeftDividerAlpha(dividerAlpha);
        invalidate();
    }

    @Override
    public void setRightDividerAlpha(int dividerAlpha) {
        mLayoutHelper.setRightDividerAlpha(dividerAlpha);
        invalidate();
    }

    @Override
    public void setRadiusAndShadow(int radius, int shadowElevation, final float shadowAlpha) {
        mLayoutHelper.setRadiusAndShadow(radius, shadowElevation, shadowAlpha);
    }

    @Override
    public void setRadiusAndShadow(int radius, @HideRadiusSide int hideRadiusSide, int shadowElevation, final float shadowAlpha) {
        mLayoutHelper.setRadiusAndShadow(radius, hideRadiusSide, shadowElevation, shadowAlpha);
    }

    @Override
    public void setRadiusAndShadow(int radius, int hideRadiusSide, int shadowElevation, int shadowColor, float shadowAlpha) {
        mLayoutHelper.setRadiusAndShadow(radius, hideRadiusSide, shadowElevation, shadowColor, shadowAlpha);
    }

    @Override
    public void setRadius(int radius) {
        mLayoutHelper.setRadius(radius);
    }

    @Override
    public void setRadius(int radius, @HideRadiusSide int hideRadiusSide) {
        mLayoutHelper.setRadius(radius, hideRadiusSide);
    }

    @Override
    public int getRadius() {
        return mLayoutHelper.getRadius();
    }

    @Override
    public void setOutlineInset(int left, int top, int right, int bottom) {
        mLayoutHelper.setOutlineInset(left, top, right, bottom);
    }

    @Override
    public void setHideRadiusSide(int hideRadiusSide) {
        mLayoutHelper.setHideRadiusSide(hideRadiusSide);
    }

    @Override
    public int getHideRadiusSide() {
        return mLayoutHelper.getHideRadiusSide();
    }

    @Override
    public void setBorderColor(@ColorInt int borderColor) {
        mLayoutHelper.setBorderColor(borderColor);
        invalidate();
    }

    @Override
    public void setBorderWidth(int borderWidth) {
        mLayoutHelper.setBorderWidth(borderWidth);
        invalidate();
    }

    @Override
    public void setShowBorderOnlyBeforeL(boolean showBorderOnlyBeforeL) {
        mLayoutHelper.setShowBorderOnlyBeforeL(showBorderOnlyBeforeL);
        invalidate();
    }

    @Override
    public boolean setWidthLimit(int widthLimit) {
        if (mLayoutHelper.setWidthLimit(widthLimit)) {
            requestLayout();
            invalidate();
        }
        return true;
    }

    @Override
    public boolean setHeightLimit(int heightLimit) {
        if (mLayoutHelper.setHeightLimit(heightLimit)) {
            requestLayout();
            invalidate();
        }
        return true;
    }

    @Override
    public void setUseThemeGeneralShadowElevation() {
        mLayoutHelper.setUseThemeGeneralShadowElevation();
    }

    @Override
    public void setOutlineExcludePadding(boolean outlineExcludePadding) {
        mLayoutHelper.setOutlineExcludePadding(outlineExcludePadding);
    }

    @Override
    public void setShadowElevation(int elevation) {
        mLayoutHelper.setShadowElevation(elevation);
    }

    @Override
    public int getShadowElevation() {
        return mLayoutHelper.getShadowElevation();
    }

    @Override
    public void setShadowAlpha(float shadowAlpha) {
        mLayoutHelper.setShadowAlpha(shadowAlpha);
    }

    @Override
    public float getShadowAlpha() {
        return mLayoutHelper.getShadowAlpha();
    }

    @Override
    public void setShadowColor(int shadowColor) {
        mLayoutHelper.setShadowColor(shadowColor);
    }

    @Override
    public int getShadowColor() {
        return mLayoutHelper.getShadowColor();
    }

    @Override
    public void setOuterNormalColor(int color) {
        mLayoutHelper.setOuterNormalColor(color);
    }

    @Override
    public void updateBottomSeparatorColor(int color) {
        mLayoutHelper.updateBottomSeparatorColor(color);
    }

    @Override
    public void updateLeftSeparatorColor(int color) {
        mLayoutHelper.updateLeftSeparatorColor(color);
    }

    @Override
    public void updateRightSeparatorColor(int color) {
        mLayoutHelper.updateRightSeparatorColor(color);
    }

    @Override
    public void updateTopSeparatorColor(int color) {
        mLayoutHelper.updateTopSeparatorColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mLayoutHelper.drawDividers(canvas, getWidth(), getHeight());
        mLayoutHelper.dispatchRoundBorderDraw(canvas);
        super.onDraw(canvas);
    }

    @Override
    public SimpleArrayMap<String, Integer> getDefaultSkinAttrs() {
        return sDefaultSkinAttrs;
    }

    @Override
    public void handle(@NotNull QMUISkinManager manager, int skinIndex, @NotNull Resources.Theme theme, @Nullable SimpleArrayMap<String, Integer> attrs) {
        manager.defaultHandleSkinAttrs(this, theme, attrs);
        if (mIndicator != null) {
            mIndicator.handleSkinChange(manager, skinIndex, theme, mTabAdapter.getItem(mCurrentSelectedIndex));
            mContentLayout.invalidate();
        }
    }

    @Override
    public boolean hasBorder() {
        return mLayoutHelper.hasBorder();
    }

    @Override
    public boolean hasLeftSeparator() {
        return mLayoutHelper.hasLeftSeparator();
    }

    @Override
    public boolean hasTopSeparator() {
        return mLayoutHelper.hasTopSeparator();
    }

    @Override
    public boolean hasRightSeparator() {
        return mLayoutHelper.hasRightSeparator();
    }

    @Override
    public boolean hasBottomSeparator() {
        return mLayoutHelper.hasBottomSeparator();
    }

    public interface TabBuilderUpdater {
        void update(QMUITabBuilder builder);
    }
}
