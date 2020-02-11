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
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.layout.QMUIConstraintLayout;
import com.qmuiteam.qmui.skin.QMUISkinHelper;
import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView2;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class QMUIQuickAction extends QMUINormalPopup<QMUIQuickAction> {

    private ArrayList<Action> mActions = new ArrayList<>();
    private int mActionWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
    private int mActionHeight;
    private boolean mShowMoreArrowIfNeeded = true;
    private int mMoreArrowWidth;
    private int mPaddingHor;

    public QMUIQuickAction(Context context, int width, int height) {
        super(context, width, height);
        mActionHeight = height;
        mMoreArrowWidth = QMUIResHelper.getAttrDimen(context, R.attr.qmui_quick_action_more_arrow_width);
        mPaddingHor = QMUIResHelper.getAttrDimen(context, R.attr.qmui_quick_action_padding_hor);
    }

    public QMUIQuickAction moreArrowWidth(int moreArrowWidth) {
        mMoreArrowWidth = moreArrowWidth;
        return this;
    }

    public QMUIQuickAction paddingHor(int paddingHor) {
        mPaddingHor = paddingHor;
        return this;
    }

    public QMUIQuickAction actionWidth(int actionWidth) {
        mActionWidth = actionWidth;
        return this;
    }

    public QMUIQuickAction actionHeight(int actionHeight) {
        mActionHeight = actionHeight;
        return this;
    }

    public QMUIQuickAction addAction(Action action) {
        mActions.add(action);
        return this;
    }

    public QMUIQuickAction showMoreArrowIfNeeded(boolean showMoreArrowIfNeeded) {
        mShowMoreArrowIfNeeded = showMoreArrowIfNeeded;
        return this;
    }

    @Override
    protected int proxyWidth(int width) {
        if (width > 0 && mActionWidth > 0) {
            if (width >= mActionWidth * mActions.size() + 2 * mPaddingHor) {
                return super.proxyWidth(width);
            }
            width = width - mPaddingHor - mMoreArrowWidth;
            return mActionWidth * (width / mActionWidth) + mPaddingHor + mMoreArrowWidth;
        }
        return super.proxyWidth(width);
    }


    @Override
    public QMUIQuickAction show(@NonNull View anchor) {
        view(createContentView());
        return super.show(anchor);
    }

    private ConstraintLayout createContentView() {
        ConstraintLayout wrapper = new ConstraintLayout(mContext);
        final RecyclerView recyclerView = new RecyclerView(mContext);
        final LayoutManager layoutManager = new LayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setId(View.generateViewId());
        recyclerView.setPadding(mPaddingHor, 0, mPaddingHor, 0);
        recyclerView.setClipToPadding(false);
        final Adapter adapter = new Adapter();
        adapter.submitList(mActions);
        recyclerView.setAdapter(adapter);
        wrapper.addView(recyclerView);
        if (mShowMoreArrowIfNeeded) {
            AppCompatImageView leftMoreArrow = createMoreArrowView(true);
            AppCompatImageView rightMoreArrow = createMoreArrowView(false);

            leftMoreArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recyclerView.smoothScrollToPosition(0);
                }
            });
            rightMoreArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                }
            });

            ConstraintLayout.LayoutParams leftLp = new ConstraintLayout.LayoutParams(mMoreArrowWidth, 0);
            leftLp.leftToLeft = recyclerView.getId();
            leftLp.topToTop = recyclerView.getId();
            leftLp.bottomToBottom = recyclerView.getId();
            wrapper.addView(leftMoreArrow, leftLp);


            ConstraintLayout.LayoutParams rightLp = new ConstraintLayout.LayoutParams(mMoreArrowWidth, 0);
            rightLp.rightToRight = recyclerView.getId();
            rightLp.topToTop = recyclerView.getId();
            rightLp.bottomToBottom = recyclerView.getId();
            wrapper.addView(rightMoreArrow, rightLp);

            recyclerView.addItemDecoration(new ItemDecoration(leftMoreArrow, rightMoreArrow));
        }
        return wrapper;
    }


    protected AppCompatImageView createMoreArrowView(boolean isLeft) {
        QMUIRadiusImageView2 arrowView = new QMUIRadiusImageView2(mContext);
        QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
        if (isLeft) {
            arrowView.setPadding(mPaddingHor, 0, 0, 0);
            builder.src(R.attr.qmui_skin_support_quick_action_more_left_arrow);
        } else {
            arrowView.setPadding(0, 0, mPaddingHor, 0);
            builder.src(R.attr.qmui_skin_support_quick_action_more_right_arrow);
        }
        builder.tintColor(R.attr.qmui_skin_support_quick_action_more_tint_color);
        int bgColor = getBgColor();
        int bgColorAttr = getBgColorAttr();
        if (bgColor != NOT_SET) {
            arrowView.setBackgroundColor(bgColor);
        } else if (bgColorAttr != 0) {
            builder.background(bgColorAttr);
        }
        QMUISkinHelper.setSkinValue(arrowView, builder);
        arrowView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        arrowView.setVisibility(View.GONE);
        arrowView.setAlpha(0f);
        builder.release();
        return arrowView;
    }

    private class ItemDecoration extends RecyclerView.ItemDecoration {
        private AppCompatImageView leftMoreArrowView;
        private AppCompatImageView rightMoreArrowView;
        private boolean isLeftMoreShown = false;
        private boolean isRightMoreShown = false;
        private boolean isFirstDraw = true;
        private int TOGGLE_DURATION = 60;

        public ItemDecoration(AppCompatImageView leftMoreArrowView,
                              AppCompatImageView rightMoreArrowView) {
            this.leftMoreArrowView = leftMoreArrowView;
            this.rightMoreArrowView = rightMoreArrowView;
        }

        private Runnable leftHideEndAction = new Runnable() {
            @Override
            public void run() {
                leftMoreArrowView.setVisibility(View.GONE);
            }
        };

        private Runnable rightHideEndAction = new Runnable() {
            @Override
            public void run() {
                rightMoreArrowView.setVisibility(View.GONE);
            }
        };


        @Override
        public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            if (parent.canScrollHorizontally(-1)) {
                if (!isLeftMoreShown) {
                    isLeftMoreShown = true;
                    leftMoreArrowView.setVisibility(View.VISIBLE);
                    if (isFirstDraw) {
                        leftMoreArrowView.setAlpha(1F);
                    } else {
                        leftMoreArrowView.animate()
                                .alpha(1f)
                                .setDuration(TOGGLE_DURATION)
                                .start();
                    }

                }
            } else {
                if (isLeftMoreShown) {
                    isLeftMoreShown = false;
                    leftMoreArrowView.animate()
                            .alpha(0f)
                            .setDuration(TOGGLE_DURATION)
                            .withEndAction(leftHideEndAction)
                            .start();
                }
            }
            if (parent.canScrollHorizontally(1)) {
                if (!isRightMoreShown) {
                    isRightMoreShown = true;
                    rightMoreArrowView.setVisibility(View.VISIBLE);
                    if (isFirstDraw) {
                        rightMoreArrowView.setAlpha(1F);
                    } else {
                        rightMoreArrowView.animate()
                                .setDuration(TOGGLE_DURATION)
                                .alpha(1f)
                                .start();
                    }
                }
            } else {
                if (isRightMoreShown) {
                    isRightMoreShown = false;
                    rightMoreArrowView.animate()
                            .alpha(0f)
                            .setDuration(TOGGLE_DURATION)
                            .withEndAction(rightHideEndAction)
                            .start();
                }
            }
            isFirstDraw = false;
        }
    }

    private class LayoutManager extends LinearLayoutManager {

        private static final float MILLISECONDS_PER_INCH = 0.01f;

        public LayoutManager(Context context) {
            super(context, LinearLayoutManager.HORIZONTAL, false);
        }

        @Override
        public RecyclerView.LayoutParams generateDefaultLayoutParams() {
            return new RecyclerView.LayoutParams(mActionWidth, mActionHeight);
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            final LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {

                @Override
                protected int calculateTimeForScrolling(int dx) {
                    return 100;
                }
            };

            linearSmoothScroller.setTargetPosition(position);
            startSmoothScroll(linearSmoothScroller);
        }
    }

    private static class VH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Callback callback;

        public VH(@NonNull ItemView itemView, @NonNull Callback callback) {
            super(itemView);
            itemView.setOnClickListener(this);
            this.callback = callback;
        }

        @Override
        public void onClick(View v) {
            callback.onClick(v, getAdapterPosition());
        }

        interface Callback {
            void onClick(View v, int adapterPosition);
        }
    }

    private class DiffCallback extends DiffUtil.ItemCallback<Action> {


        @Override
        public boolean areItemsTheSame(@NonNull Action action, @NonNull Action t1) {
            return Objects.equals(action.text, t1.text) &&
                    action.icon == t1.icon &&
                    action.iconAttr == t1.iconAttr &&
                    action.onClickListener == t1.onClickListener;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Action action, @NonNull Action t1) {
            return action.textColorAttr == t1.textColorAttr &&
                    action.iconTintColorAttr == t1.iconTintColorAttr;
        }
    }

    protected ItemView createItemView() {
        return new DefaultItemView(mContext);
    }


    private class Adapter extends ListAdapter<Action, VH> implements VH.Callback {

        protected Adapter() {
            super(new DiffCallback());
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new VH(createItemView(), this);
        }

        @Override
        public void onClick(View v, int adapterPosition) {
            Action action = getItem(adapterPosition);
            OnClickListener onClickListener = action.onClickListener;
            if (onClickListener != null) {
                onClickListener.onClick(QMUIQuickAction.this, action, adapterPosition);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull VH vh, int i) {
            ItemView view = (ItemView) vh.itemView;
            view.render(getItem(i));
        }
    }

    public static class Action {
        @Nullable Drawable icon;
        int iconRes;
        @Nullable OnClickListener onClickListener;
        @Nullable CharSequence text;
        int iconAttr = 0;
        int textColorAttr = R.attr.qmui_skin_support_quick_action_item_tint_color;
        int iconTintColorAttr = R.attr.qmui_skin_support_quick_action_item_tint_color;

        public Action iconAttr(int iconAttr) {
            this.iconAttr = iconAttr;
            return this;
        }

        public Action icon(Drawable icon) {
            this.icon = icon;
            return this;
        }

        public Action icon(int iconRes) {
            this.iconRes = iconRes;
            return this;
        }

        public Action onClick(OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
            return this;
        }

        public Action text(CharSequence text) {
            this.text = text;
            return this;
        }

        public Action textColorAttr(int textColorAttr) {
            this.textColorAttr = textColorAttr;
            return this;
        }

        public Action iconTintColorAttr(int iconTintColorAttr) {
            this.iconTintColorAttr = iconTintColorAttr;
            return this;
        }
    }

    public interface OnClickListener {
        void onClick(QMUIQuickAction quickAction, Action action, int position);
    }

    public abstract static class ItemView extends QMUIConstraintLayout {

        public ItemView(Context context) {
            super(context);
        }

        public ItemView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public abstract void render(Action action);
    }

    public static class DefaultItemView extends ItemView {
        private AppCompatImageView mIconView;
        private TextView mTextView;

        public DefaultItemView(Context context) {
            this(context, null);
        }

        public DefaultItemView(Context context, AttributeSet attrs) {
            super(context, attrs);
            int paddingHor = QMUIResHelper.getAttrDimen(
                    context, R.attr.qmui_quick_action_item_padding_hor);
            int paddingVer = QMUIResHelper.getAttrDimen(
                    context, R.attr.qmui_quick_action_item_padding_ver);
            setPadding(paddingHor, paddingVer, paddingHor, paddingVer);
            mIconView = new AppCompatImageView(context);
            mIconView.setId(QMUIViewHelper.generateViewId());
            mTextView = new TextView(context);
            mTextView.setId(QMUIViewHelper.generateViewId());
            mTextView.setTextSize(10);
            mTextView.setTypeface(Typeface.DEFAULT_BOLD);
            setChangeAlphaWhenPress(true);
            setChangeAlphaWhenDisable(true);

            int wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT;
            LayoutParams iconLp = new LayoutParams(wrapContent, wrapContent);
            iconLp.leftToLeft = LayoutParams.PARENT_ID;
            iconLp.rightToRight = LayoutParams.PARENT_ID;
            iconLp.topToTop = LayoutParams.PARENT_ID;
            iconLp.bottomToTop = mTextView.getId();
            iconLp.verticalChainStyle = LayoutParams.CHAIN_PACKED;
            addView(mIconView, iconLp);

            LayoutParams textLp = new LayoutParams(wrapContent, wrapContent);
            textLp.leftToLeft = LayoutParams.PARENT_ID;
            textLp.rightToRight = LayoutParams.PARENT_ID;
            textLp.topToBottom = mIconView.getId();
            textLp.bottomToBottom = LayoutParams.PARENT_ID;
            textLp.topMargin = QMUIResHelper.getAttrDimen(
                    context, R.attr.qmui_quick_action_item_middle_space);
            textLp.verticalChainStyle = LayoutParams.CHAIN_PACKED;
            textLp.goneTopMargin = 0;
            addView(mTextView, textLp);
        }

        @Override
        public void render(Action action) {
            QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
            if (action.icon != null || action.iconRes != 0) {
                if (action.icon != null) {
                    mIconView.setImageDrawable(action.icon.mutate());
                } else {
                    mIconView.setImageResource(action.iconRes);
                }

                if (action.iconTintColorAttr != 0) {
                    builder.tintColor(action.iconTintColorAttr);
                }
                mIconView.setVisibility(View.VISIBLE);
                QMUISkinHelper.setSkinValue(mIconView, builder);
            } else if (action.iconAttr != 0) {
                builder.src(action.iconAttr);
                mIconView.setVisibility(View.VISIBLE);
                QMUISkinHelper.setSkinValue(mIconView, builder);
            } else {
                mIconView.setVisibility(View.GONE);
            }

            mTextView.setText(action.text);
            builder.clear();
            builder.textColor(action.textColorAttr);
            QMUISkinHelper.setSkinValue(mTextView, builder);
            builder.release();
        }
    }
}