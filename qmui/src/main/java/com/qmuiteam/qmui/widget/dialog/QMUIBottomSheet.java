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

package com.qmuiteam.qmui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.qmuiteam.qmui.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.qmuiteam.qmui.layout.IQMUILayout.HIDE_RADIUS_SIDE_BOTTOM;

/**
 * QMUIBottomSheet 在 {@link Dialog} 的基础上重新定制了 {@link #show()} 和 {@link #hide()} 时的动画效果, 使 {@link Dialog} 在界面底部升起和降下。
 * <p>
 * 提供了以下两种面板样式:
 * <ul>
 * <li>列表样式, 使用 {@link QMUIBottomSheet.BottomListSheetBuilder} 生成。</li>
 * <li>宫格类型, 使用 {@link QMUIBottomSheet.BottomGridSheetBuilder} 生成。</li>
 * </ul>
 * </p>
 */
public class QMUIBottomSheet extends QMUIBaseDialog {
    private static final String TAG = "QMUIBottomSheet";
    private QMUIBottomSheetRootLayout mRootView;
    private OnBottomSheetShowListener mOnBottomSheetShowListener;
    private QMUIBottomSheetBehavior<QMUIBottomSheetRootLayout> mBehavior;
    private boolean mAnimateToCancel = false;
    private boolean mAnimateToDismiss = false;


    public QMUIBottomSheet(Context context) {
        this(context, R.style.QMUI_BottomSheet);
    }

    public QMUIBottomSheet(Context context, int style) {
        super(context, style);
        ViewGroup container = (ViewGroup) View.inflate(context, R.layout.qmui_bottom_sheet_dialog, null);
        mRootView = container.findViewById(R.id.bottom_sheet);
        mBehavior = new QMUIBottomSheetBehavior<>();
        mBehavior.setHideable(cancelable);
        mBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    if (mAnimateToCancel) {
                        // cancel() invoked
                        cancel();
                    } else if (mAnimateToDismiss) {
                        // dismiss() invoked but it it not triggered by cancel()
                        dismiss();
                    } else {
                        // drag to cancel
                        cancel();
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        mBehavior.setPeekHeight(0);
        mBehavior.setAllowDrag(false);
        mBehavior.setSkipCollapsed(true);
        CoordinatorLayout.LayoutParams rootViewLp = (CoordinatorLayout.LayoutParams) mRootView.getLayoutParams();
        rootViewLp.setBehavior(mBehavior);

        // We treat the CoordinatorLayout as outside the dialog though it is technically inside
        container.findViewById(R.id.touch_outside)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(mBehavior.getState() == BottomSheetBehavior.STATE_SETTLING){
                                    return;
                                }
                                if (cancelable && isShowing() && shouldWindowCloseOnTouchOutside()) {
                                    cancel();
                                }
                            }
                        });
        mRootView.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        // Consume the event and prevent it from falling through
                        return true;
                    }
                });

        super.setContentView(container, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void onSetCancelable(boolean cancelable) {
        super.onSetCancelable(cancelable);
        mBehavior.setHideable(cancelable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        if (window != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            }
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        ViewCompat.requestApplyInsets(mRootView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    @Override
    public void cancel() {
        if (mBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            mAnimateToCancel = false;
            super.cancel();
        } else {
            mAnimateToCancel = true;
            mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    @Override
    public void dismiss() {
        if (mBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            mAnimateToDismiss = false;
            super.dismiss();
        } else {
            mAnimateToDismiss = true;
            mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    public void setOnBottomSheetShowListener(OnBottomSheetShowListener onBottomSheetShowListener) {
        mOnBottomSheetShowListener = onBottomSheetShowListener;
    }

    public void setRadius(int radius) {
        mRootView.setRadius(radius, HIDE_RADIUS_SIDE_BOTTOM);
    }

    public QMUIBottomSheetRootLayout getRootView() {
        return mRootView;
    }

    public QMUIBottomSheetBehavior<QMUIBottomSheetRootLayout> getBehavior() {
        return mBehavior;
    }

    @Override
    public void show() {
        super.show();
        if (mOnBottomSheetShowListener != null) {
            mOnBottomSheetShowListener.onShow();
        }
        if (mBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            mRootView.postOnAnimation(new Runnable() {
                @Override
                public void run() {
                    mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            });
        }
        mAnimateToCancel = false;
        mAnimateToDismiss = false;
    }

    public interface OnBottomSheetShowListener {
        void onShow();
    }

    @Override
    public void setContentView(View view) {
        throw new IllegalStateException(
                "Use addContentView(View, ConstraintLayout.LayoutParams) for replacement");
    }

    @Override
    public void setContentView(int layoutResId) {
        throw new IllegalStateException(
                "Use addContentView(int) for replacement");
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        throw new IllegalStateException(
                "Use addContentView(View, LinearLayout.LayoutParams) for replacement");
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        throw new IllegalStateException(
                "Use addContentView(View, LinearLayout.LayoutParams) for replacement");
    }

    public void addContentView(View view, LinearLayout.LayoutParams layoutParams) {
        mRootView.addView(view, layoutParams);
    }

    public void addContentView(View view) {
        mRootView.addView(view, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void addContentView(int layoutResId) {
        LayoutInflater.from(mRootView.getContext()).inflate(layoutResId, mRootView, true);
    }


    /**
     * 生成列表类型的 {@link QMUIBottomSheet} 对话框。
     */
    public static class BottomListSheetBuilder extends QMUIBottomSheetBaseBuilder<BottomListSheetBuilder> {


        private List<QMUIBottomSheetListItemModel> mItems;
        private List<View> mContentHeaderViews;
        private List<View> mContentFooterViews;
        private boolean mNeedRightMark; //是否需要rightMark,标识当前项
        private int mCheckedIndex;
        private boolean mGravityCenter = false;
        private OnSheetItemClickListener mOnSheetItemClickListener;


        public BottomListSheetBuilder(Context context) {
            this(context, false);
        }

        /**
         * @param needRightMark 是否需要在被选中的 Item 右侧显示一个勾(使用 {@link #setCheckedIndex(int)} 设置选中的 Item)
         */
        public BottomListSheetBuilder(Context context, boolean needRightMark) {
            super(context);
            mItems = new ArrayList<>();
            mNeedRightMark = needRightMark;
        }

        /**
         * 设置要被选中的 Item 的下标。
         * <p>
         * 注意:仅当 {@link #mNeedRightMark} 为 true 时才有效。
         */
        public BottomListSheetBuilder setCheckedIndex(int checkedIndex) {
            mCheckedIndex = checkedIndex;
            return this;
        }

        public BottomListSheetBuilder setNeedRightMark(boolean needRightMark) {
            mNeedRightMark = needRightMark;
            return this;
        }

        public BottomListSheetBuilder setGravityCenter(boolean gravityCenter) {
            mGravityCenter = gravityCenter;
            return this;
        }

        public BottomListSheetBuilder setOnSheetItemClickListener(
                OnSheetItemClickListener onSheetItemClickListener) {
            mOnSheetItemClickListener = onSheetItemClickListener;
            return this;
        }

        public BottomListSheetBuilder addItem(QMUIBottomSheetListItemModel itemModel) {
            mItems.add(itemModel);
            return this;
        }

        /**
         * @param textAndTag Item 的文字内容，同时会把内容设置为 tag。
         */
        public BottomListSheetBuilder addItem(String textAndTag) {
            mItems.add(new QMUIBottomSheetListItemModel(textAndTag, textAndTag));
            return this;
        }

        /**
         * @param image      icon Item 的 icon。
         * @param textAndTag Item 的文字内容，同时会把内容设置为 tag。
         */
        public BottomListSheetBuilder addItem(Drawable image, String textAndTag) {
            mItems.add(new QMUIBottomSheetListItemModel(textAndTag, textAndTag).image(image));
            return this;
        }

        /**
         * @param text Item 的文字内容。
         * @param tag  item 的 tag。
         */
        public BottomListSheetBuilder addItem(String text, String tag) {
            mItems.add(new QMUIBottomSheetListItemModel(text, tag));
            return this;
        }

        /**
         * @param imageRes Item 的图标 Resource。
         * @param text     Item 的文字内容。
         * @param tag      Item 的 tag。
         */
        public BottomListSheetBuilder addItem(int imageRes, String text, String tag) {
            mItems.add(new QMUIBottomSheetListItemModel(text, tag).image(imageRes));
            return this;
        }

        /**
         * @param imageRes    Item 的图标 Resource。
         * @param text        Item 的文字内容。
         * @param tag         Item 的 tag。
         * @param hasRedPoint 是否显示红点。
         */
        public BottomListSheetBuilder addItem(int imageRes, String text, String tag, boolean hasRedPoint) {
            mItems.add(new QMUIBottomSheetListItemModel(text, tag).image(imageRes).redPoint(hasRedPoint));
            return this;
        }

        /**
         * @param imageRes    Item 的图标 Resource。
         * @param text        Item 的文字内容。
         * @param tag         Item 的 tag。
         * @param hasRedPoint 是否显示红点。
         * @param disabled    是否显示禁用态。
         */
        public BottomListSheetBuilder addItem(
                int imageRes, CharSequence text, String tag, boolean hasRedPoint, boolean disabled) {
            mItems.add(new QMUIBottomSheetListItemModel(text, tag)
                    .image(imageRes).redPoint(hasRedPoint).disabled(disabled));
            return this;
        }


        @Deprecated
        public BottomListSheetBuilder addHeaderView(@NonNull View view) {
            return addContentHeaderView(view);
        }

        public BottomListSheetBuilder addContentHeaderView(@NonNull View view) {
            if (mContentHeaderViews == null) {
                mContentHeaderViews = new ArrayList<>();
            }
            mContentHeaderViews.add(view);
            return this;
        }

        public BottomListSheetBuilder addContentFooterView(@NonNull View view) {
            if (mContentFooterViews == null) {
                mContentFooterViews = new ArrayList<>();
            }
            mContentFooterViews.add(view);
            return this;
        }

        @Nullable
        @Override
        protected View onCreateContentView(@NonNull final QMUIBottomSheet bottomSheet,
                                           @NonNull QMUIBottomSheetRootLayout rootLayout,
                                           @NonNull Context context) {
            RecyclerView recyclerView = new RecyclerView(context);
            recyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
            QMUIBottomSheetListAdapter adapter = new QMUIBottomSheetListAdapter(
                    mNeedRightMark, mGravityCenter);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(context) {
                @Override
                public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                    return new RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                }
            });
            recyclerView.addItemDecoration(new QMUIBottomSheetListItemDecoration(context));

            LinearLayout headerView = null;
            if (mContentHeaderViews != null && mContentHeaderViews.size() > 0) {
                headerView = new LinearLayout(context);
                headerView.setOrientation(LinearLayout.VERTICAL);
                for (View view : mContentHeaderViews) {
                    if (view.getParent() != null) {
                        ((ViewGroup) view.getParent()).removeView(view);
                    }
                    headerView.addView(view, new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
            }
            LinearLayout footerView = null;
            if (mContentFooterViews != null && mContentFooterViews.size() > 0) {
                footerView = new LinearLayout(context);
                footerView.setOrientation(LinearLayout.VERTICAL);
                for (View view : mContentFooterViews) {
                    if (view.getParent() != null) {
                        ((ViewGroup) view.getParent()).removeView(view);
                    }
                    footerView.addView(view, new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
            }
            adapter.setData(headerView, footerView, mItems);
            adapter.setOnItemClickListener(new QMUIBottomSheetListAdapter.OnItemClickListener() {
                @Override
                public void onClick(QMUIBottomSheetListAdapter.VH vh, int dataPos, QMUIBottomSheetListItemModel model) {
                    if (mOnSheetItemClickListener != null) {
                        mOnSheetItemClickListener.onClick(bottomSheet, vh.itemView, dataPos, model.tag);
                    }
                }
            });
            adapter.setCheckedIndex(mCheckedIndex);
            recyclerView.scrollToPosition(mCheckedIndex + (headerView == null ? 0 : 1));
            return recyclerView;
        }


        public interface OnSheetItemClickListener {
            void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag);
        }
    }

    /**
     * 生成宫格类型的 {@link QMUIBottomSheet} 对话框。
     */
    public static class BottomGridSheetBuilder extends QMUIBottomSheetBaseBuilder<BottomGridSheetBuilder>
            implements View.OnClickListener {

        public static final int FIRST_LINE = 0;
        public static final int SECOND_LINE = 1;
        public static final ItemViewFactory DEFAULT_ITEM_VIEW_FACTORY = new DefaultItemViewFactory();

        public interface ItemViewFactory {
            QMUIBottomSheetGridItemView create(QMUIBottomSheet bottomSheet, QMUIBottomSheetGridItemModel model);
        }

        public static class DefaultItemViewFactory implements ItemViewFactory {

            @Override
            public QMUIBottomSheetGridItemView create(@NonNull QMUIBottomSheet bottomSheet, @NonNull QMUIBottomSheetGridItemModel model) {
                QMUIBottomSheetGridItemView itemView = new QMUIBottomSheetGridItemView(bottomSheet.getContext());
                itemView.render(model);
                return itemView;
            }
        }

        private ArrayList<QMUIBottomSheetGridItemModel> mFirstLineItems;
        private ArrayList<QMUIBottomSheetGridItemModel> mSecondLineItems;
        private ItemViewFactory mItemViewFactory = DEFAULT_ITEM_VIEW_FACTORY;
        private OnSheetItemClickListener mOnSheetItemClickListener;

        public BottomGridSheetBuilder(Context context) {
            super(context);
            mFirstLineItems = new ArrayList<>();
            mSecondLineItems = new ArrayList<>();
        }

        public BottomGridSheetBuilder addItem(@NonNull QMUIBottomSheetGridItemModel model, @Style int style) {
            switch (style) {
                case FIRST_LINE:
                    mFirstLineItems.add(model);
                    break;
                case SECOND_LINE:
                    mSecondLineItems.add(model);
                    break;
            }
            return this;
        }

        public BottomGridSheetBuilder addItem(int imageRes, CharSequence textAndTag, @Style int style) {
            return addItem(imageRes, textAndTag, textAndTag, style, 0);
        }

        public BottomGridSheetBuilder addItem(int imageRes, CharSequence text, Object tag, @Style int style) {
            return addItem(imageRes, text, tag, style, 0);
        }

        public BottomGridSheetBuilder addItem(int imageRes, CharSequence text, Object tag,
                                              @Style int style, int subscriptRes) {
            return addItem(imageRes, text, tag, style, subscriptRes, null);
        }

        public BottomGridSheetBuilder addItem(int imageRes, CharSequence text, Object tag,
                                              @Style int style, int subscriptRes, Typeface typeface) {
            return addItem(new QMUIBottomSheetGridItemModel(text, tag)
                    .image(imageRes)
                    .subscript(subscriptRes)
                    .typeface(typeface), style);
        }


        public void setItemViewFactory(ItemViewFactory itemViewFactory) {
            mItemViewFactory = itemViewFactory;
        }

        public BottomGridSheetBuilder setOnSheetItemClickListener(OnSheetItemClickListener onSheetItemClickListener) {
            mOnSheetItemClickListener = onSheetItemClickListener;
            return this;
        }

        @Override
        public void onClick(View v) {
            if (mOnSheetItemClickListener != null) {
                mOnSheetItemClickListener.onClick(mDialog, v);
            }
        }

        @Nullable
        @Override
        protected View onCreateContentView(@NonNull QMUIBottomSheet bottomSheet,
                                           @NonNull QMUIBottomSheetRootLayout rootLayout,
                                           @NonNull Context context) {
            if (mFirstLineItems.isEmpty() && mSecondLineItems.isEmpty()) {
                return null;
            }
            List<Pair<View, LinearLayout.LayoutParams>> firstLines = null;
            List<Pair<View, LinearLayout.LayoutParams>> secondLines = null;
            int wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT;

            if (!mFirstLineItems.isEmpty()) {
                firstLines = new ArrayList<>();
                for (QMUIBottomSheetGridItemModel model : mFirstLineItems) {
                    QMUIBottomSheetGridItemView itemView = mItemViewFactory.create(bottomSheet, model);
                    itemView.setOnClickListener(this);
                    firstLines.add(new Pair<View, LinearLayout.LayoutParams>(
                            itemView,
                            new LinearLayout.LayoutParams(wrapContent, wrapContent)));
                }
            }
            if (!mSecondLineItems.isEmpty()) {
                secondLines = new ArrayList<>();
                for (QMUIBottomSheetGridItemModel model : mSecondLineItems) {
                    QMUIBottomSheetGridItemView itemView = mItemViewFactory.create(bottomSheet, model);
                    itemView.setOnClickListener(this);
                    secondLines.add(new Pair<View, LinearLayout.LayoutParams>(
                            itemView,
                            new LinearLayout.LayoutParams(wrapContent, wrapContent)));
                }
            }
            return new QMUIBottomSheetGridLineLayout(mDialog, firstLines, secondLines);
        }

        public interface OnSheetItemClickListener {
            void onClick(QMUIBottomSheet dialog, View itemView);
        }

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({FIRST_LINE, SECOND_LINE})
        public @interface Style {
        }
    }
}
