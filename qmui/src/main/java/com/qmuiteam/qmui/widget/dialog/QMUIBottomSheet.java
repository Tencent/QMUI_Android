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
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.layout.QMUIButton;
import com.qmuiteam.qmui.skin.QMUISkinHelper;
import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.textview.QMUISpanTouchFixTextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
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
                if(newState == BottomSheetBehavior.STATE_HIDDEN){
                    if(mAnimateToCancel){
                        // cancel() invoked
                        cancel();
                    }else if(mAnimateToDismiss){
                        // dismiss() invoked but it it not triggered by cancel()
                        dismiss();
                    }else{
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
        if(mBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
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


        private List<QMUIBottomSheetItemModel> mItems;
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

        public BottomListSheetBuilder addItem(QMUIBottomSheetItemModel itemModel) {
            mItems.add(itemModel);
            return this;
        }

        /**
         * @param textAndTag Item 的文字内容，同时会把内容设置为 tag。
         */
        public BottomListSheetBuilder addItem(String textAndTag) {
            mItems.add(new QMUIBottomSheetItemModel(textAndTag, textAndTag));
            return this;
        }

        /**
         * @param image      icon Item 的 icon。
         * @param textAndTag Item 的文字内容，同时会把内容设置为 tag。
         */
        public BottomListSheetBuilder addItem(Drawable image, String textAndTag) {
            mItems.add(new QMUIBottomSheetItemModel(textAndTag, textAndTag).image(image));
            return this;
        }

        /**
         * @param text Item 的文字内容。
         * @param tag  item 的 tag。
         */
        public BottomListSheetBuilder addItem(String text, String tag) {
            mItems.add(new QMUIBottomSheetItemModel(text, tag));
            return this;
        }

        /**
         * @param imageRes Item 的图标 Resource。
         * @param text     Item 的文字内容。
         * @param tag      Item 的 tag。
         */
        public BottomListSheetBuilder addItem(int imageRes, String text, String tag) {
            mItems.add(new QMUIBottomSheetItemModel(text, tag).image(imageRes));
            return this;
        }

        /**
         * @param imageRes    Item 的图标 Resource。
         * @param text        Item 的文字内容。
         * @param tag         Item 的 tag。
         * @param hasRedPoint 是否显示红点。
         */
        public BottomListSheetBuilder addItem(int imageRes, String text, String tag, boolean hasRedPoint) {
            mItems.add(new QMUIBottomSheetItemModel(text, tag).image(imageRes).redPoint(hasRedPoint));
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
                int imageRes, String text, String tag, boolean hasRedPoint, boolean disabled) {
            mItems.add(new QMUIBottomSheetItemModel(text, tag)
                    .image(imageRes).redPoint(hasRedPoint).disabled(disabled));
            return this;
        }


        @Deprecated
        public BottomListSheetBuilder addHeaderView(@NonNull View view) {
            return addContentHeaderView(view);
        }

        public BottomListSheetBuilder addContentHeaderView(@NonNull View view){
            if(mContentHeaderViews == null){
                mContentHeaderViews = new ArrayList<>();
            }
            mContentHeaderViews.add(view);
            return this;
        }

        public BottomListSheetBuilder addContentFooterView(@NonNull View view){
            if(mContentFooterViews == null){
                mContentFooterViews = new ArrayList<>();
            }
            mContentFooterViews.add(view);
            return this;
        }

        @Nullable
        @Override
        protected View onCreateContentView(final QMUIBottomSheet bottomSheet,
                                           QMUIBottomSheetRootLayout rootLayout,
                                           Context context) {
            RecyclerView recyclerView = new RecyclerView(context);
            QMUIBottomSheetListAdapter adapter = new QMUIBottomSheetListAdapter(
                    mNeedRightMark, mGravityCenter);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(context){
                @Override
                public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                    return new RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                }
            });
            recyclerView.addItemDecoration(new QMUIBottomSheetListItemDecoration(context));

            LinearLayout headerView = null;
            if(mContentHeaderViews != null && mContentHeaderViews.size() > 0){
                headerView = new LinearLayout(context);
                headerView.setOrientation(LinearLayout.VERTICAL);
                for(View view: mContentHeaderViews){
                    if(view.getParent() != null){
                        ((ViewGroup)view.getParent()).removeView(view);
                    }
                    headerView.addView(view, new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
            }
            LinearLayout footerView = null;
            if(mContentFooterViews != null && mContentHeaderViews.size() > 0){
                footerView = new LinearLayout(context);
                footerView.setOrientation(LinearLayout.VERTICAL);
                for(View view: mContentFooterViews){
                    if(view.getParent() != null){
                        ((ViewGroup)view.getParent()).removeView(view);
                    }
                    footerView.addView(view, new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
            }
            adapter.setData(headerView, footerView, mItems);
            adapter.setOnItemClickListener(new QMUIBottomSheetListAdapter.OnItemClickListener() {
                @Override
                public void onClick(QMUIBottomSheetListAdapter.VH vh, int dataPos, QMUIBottomSheetItemModel model) {
                    if(mOnSheetItemClickListener != null){
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
    public static class BottomGridSheetBuilder implements View.OnClickListener {
        public interface ItemViewFactory {
            View create(QMUIBottomSheet bottomSheet, int layoutRes, int style, View.OnClickListener onClickListener);
        }

        public static class DefaultItemViewFactory implements ItemViewFactory {
            private Drawable mDrawable;
            private CharSequence mText;
            private Object mTag;
            private int mSubscriptRes;
            private Typeface mTypeface;

            public DefaultItemViewFactory(Drawable drawable, CharSequence text, Object tag, int subscriptRes, Typeface typeface) {
                mDrawable = drawable;
                mText = text;
                mTag = tag;
                mSubscriptRes = subscriptRes;
                mTypeface = typeface;
            }

            @Override
            public View create(QMUIBottomSheet bottomSheet, int layoutRes, int style, View.OnClickListener onClickListener) {
                LayoutInflater inflater = LayoutInflater.from(bottomSheet.getContext());
                QMUIBottomSheetItemView itemView = (QMUIBottomSheetItemView) inflater.inflate(layoutRes, null, false);
                TextView titleTV = itemView.findViewById(R.id.grid_item_title);
                if (mTypeface != null) {
                    titleTV.setTypeface(mTypeface);
                }
                titleTV.setText(mText);

                itemView.setTag(mTag);
                itemView.setOnClickListener(onClickListener);
                AppCompatImageView imageView = itemView.findViewById(R.id.grid_item_image);
                imageView.setImageDrawable(mDrawable);

                if (mSubscriptRes != 0) {
                    ViewStub stub = itemView.findViewById(R.id.grid_item_subscript);
                    View inflated = stub.inflate();
                    ((ImageView) inflated).setImageResource(mSubscriptRes);
                }
                return itemView;
            }
        }

        /**
         * item 出现在第一行
         */
        public static final int FIRST_LINE = 0;
        /**
         * item 出现在第二行
         */
        public static final int SECOND_LINE = 1;
        private Context mContext;
        private QMUIBottomSheet mDialog;
        private SparseArray<ItemViewFactory> mFirstLineViews;
        private SparseArray<ItemViewFactory> mSecondLineViews;
        private int mMiniItemWidth = -1;
        private OnSheetItemClickListener mOnSheetItemClickListener;
        private ViewGroup mBottomButtonContainer;
        private TextView mBottomButton;
        private Typeface mBottomButtonTypeFace = null;
        private boolean mIsShowButton = true;
        private CharSequence mButtonText = null;
        private View.OnClickListener mButtonClickListener = null;

        public BottomGridSheetBuilder(Context context) {
            mContext = context;
            mFirstLineViews = new SparseArray<>();
            mSecondLineViews = new SparseArray<>();
        }

        public BottomGridSheetBuilder addItem(int imageRes, CharSequence textAndTag, @Style int style) {
            return addItem(imageRes, textAndTag, textAndTag, style, 0);
        }

        public BottomGridSheetBuilder addItem(int imageRes, CharSequence text, Object tag, @Style int style) {
            return addItem(imageRes, text, tag, style, 0);
        }

        public BottomGridSheetBuilder setIsShowButton(boolean isShowButton) {
            mIsShowButton = isShowButton;
            return this;
        }

        public BottomGridSheetBuilder setButtonText(CharSequence buttonText) {
            mButtonText = buttonText;
            return this;
        }

        public BottomGridSheetBuilder setButtonClickListener(View.OnClickListener buttonClickListener) {
            mButtonClickListener = buttonClickListener;
            return this;
        }

        public BottomGridSheetBuilder setBottomButtonTypeFace(Typeface bottomButtonTypeFace) {
            mBottomButtonTypeFace = bottomButtonTypeFace;
            return this;
        }

        public BottomGridSheetBuilder addItem(int imageRes, CharSequence text, Object tag,
                                              @Style int style, int subscriptRes) {
            return addItem(imageRes, text, tag, style, subscriptRes, null);
        }

        public BottomGridSheetBuilder addItem(int imageRes, CharSequence text, Object tag,
                                              @Style int style, int subscriptRes, Typeface typeface) {
            ItemViewFactory itemView = createItemViewFactory(
                    AppCompatResources.getDrawable(mContext, imageRes), text, tag, subscriptRes, typeface);
            return addItem(itemView, style);
        }

        public BottomGridSheetBuilder addItem(ItemViewFactory itemViewFactory, @Style int style) {
            switch (style) {
                case FIRST_LINE:
                    mFirstLineViews.append(mFirstLineViews.size(), itemViewFactory);
                    break;
                case SECOND_LINE:
                    mSecondLineViews.append(mSecondLineViews.size(), itemViewFactory);
                    break;
            }
            return this;
        }

        @Deprecated
        public BottomGridSheetBuilder addItem(final View view, @Style int style) {
            switch (style) {
                case FIRST_LINE:
                    mFirstLineViews.append(mFirstLineViews.size(), new ItemViewFactory() {
                        @Override
                        public View create(QMUIBottomSheet bottomSheet, int layoutRes, int style, View.OnClickListener onClickListener) {
                            return view;
                        }
                    });
                    break;
                case SECOND_LINE:
                    mSecondLineViews.append(mSecondLineViews.size(), new ItemViewFactory() {
                        @Override
                        public View create(QMUIBottomSheet bottomSheet, int layoutRes, int style, View.OnClickListener onClickListener) {
                            return view;
                        }
                    });
                    break;
            }
            return this;
        }

        public ItemViewFactory createItemViewFactory(Drawable drawable, CharSequence text, Object tag,
                                                     int subscriptRes) {
            return createItemViewFactory(drawable, text, tag, subscriptRes, null);
        }

        public ItemViewFactory createItemViewFactory(Drawable drawable, CharSequence text, Object tag,
                                                     int subscriptRes, Typeface typeface) {
            return new DefaultItemViewFactory(drawable, text, tag, subscriptRes, typeface);
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

        public QMUIBottomSheet build() {
            return build(R.style.QMUI_BottomSheet);
        }

        public QMUIBottomSheet build(int style) {
            mDialog = new QMUIBottomSheet(mContext, style);
            View contentView = buildViews(mDialog);
            mDialog.setContentView(contentView,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return mDialog;
        }

        public int getFirstLineContainerId() {
            return R.id.bottom_sheet_first_linear_layout;
        }

        public int getSecondLineContainerId() {
            return R.id.bottom_sheet_second_linear_layout;
        }

        private View buildViews(QMUIBottomSheet bottomSheet) {
            LinearLayout baseLinearLayout;
            baseLinearLayout = (LinearLayout) View.inflate(bottomSheet.getContext(), getContentViewLayoutId(), null);
            LinearLayout firstLine = baseLinearLayout.findViewById(getFirstLineContainerId());
            LinearLayout secondLine = baseLinearLayout.findViewById(getSecondLineContainerId());
            mBottomButtonContainer = baseLinearLayout.findViewById(R.id.bottom_sheet_button_container);
            mBottomButton = baseLinearLayout.findViewById(R.id.bottom_sheet_close_button);

            int maxItemCountEachLine = Math.max(mFirstLineViews.size(), mSecondLineViews.size());
            int screenWidth = QMUIDisplayHelper.getScreenWidth(bottomSheet.getContext());
            int screenHeight = QMUIDisplayHelper.getScreenHeight(bottomSheet.getContext());
            int width = screenWidth < screenHeight ? screenWidth : screenHeight;
            int itemWidth = calculateItemWidth(width, maxItemCountEachLine, firstLine.getPaddingLeft(), firstLine.getPaddingRight());

            addViewsInSection(bottomSheet, mFirstLineViews, firstLine, itemWidth, FIRST_LINE);
            addViewsInSection(bottomSheet, mSecondLineViews, secondLine, itemWidth, SECOND_LINE);

            boolean hasFirstLine = mFirstLineViews.size() > 0;
            boolean hasSecondLine = mSecondLineViews.size() > 0;
            if (!hasFirstLine) {
                firstLine.setVisibility(View.GONE);
            }
            if (!hasSecondLine) {
                if (hasFirstLine) {
                    firstLine.setPadding(
                            firstLine.getPaddingLeft(),
                            firstLine.getPaddingTop(),
                            firstLine.getPaddingRight(),
                            0);
                }
                secondLine.setVisibility(View.GONE);
            }

            // button 在用户自定义了contentView的情况下可能不存在
            if (mBottomButtonContainer != null) {
                if (mIsShowButton) {
                    mBottomButtonContainer.setVisibility(View.VISIBLE);
                    baseLinearLayout.setPadding(baseLinearLayout.getPaddingLeft(),
                            baseLinearLayout.getPaddingTop(),
                            baseLinearLayout.getPaddingRight(),
                            0);
                } else {
                    mBottomButtonContainer.setVisibility(View.GONE);
                }
                if (mBottomButtonTypeFace != null) {
                    mBottomButton.setTypeface(mBottomButtonTypeFace);
                }
                if (mButtonText != null) {
                    mBottomButton.setText(mButtonText);
                }

                if (mButtonClickListener != null) {
                    mBottomButton.setOnClickListener(mButtonClickListener);
                } else {
                    mBottomButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            mDialog.dismiss();
                        }
                    });
                }
            }

            return baseLinearLayout;
        }

        protected int getContentViewLayoutId() {
            return R.layout.qmui_bottom_sheet_grid;
        }

        protected int getItemViewLayoutId() {
            return R.layout.qmui_bottom_sheet_grid_item;
        }

        /**
         * 拿个数最多的一行，去决策item的平铺/拉伸策略
         *
         * @return item 宽度
         */
        private int calculateItemWidth(int width, int maxItemCountInEachLine, int paddingLeft, int paddingRight) {
            if (mMiniItemWidth == -1) {
                mMiniItemWidth = QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_bottom_sheet_grid_item_mini_width);
            }

            final int parentSpacing = width - paddingLeft - paddingRight;
            int itemWidth = mMiniItemWidth;
            // 看是否需要把 Item 拉伸平分 parentSpacing
            if (maxItemCountInEachLine >= 3
                    && parentSpacing - maxItemCountInEachLine * itemWidth > 0
                    && parentSpacing - maxItemCountInEachLine * itemWidth < itemWidth) {
                int count = parentSpacing / itemWidth;
                itemWidth = parentSpacing / count;
            }
            // 看是否需要露出半个在屏幕边缘
            if (itemWidth * maxItemCountInEachLine > parentSpacing) {
                int count = (width - paddingLeft) / itemWidth;
                itemWidth = (int) ((width - paddingLeft) / (count + .5f));
            }
            return itemWidth;
        }

        private void addViewsInSection(QMUIBottomSheet bottomSheet, SparseArray<ItemViewFactory> items,
                                       LinearLayout parent, int itemWidth, int style) {

            for (int i = 0; i < items.size(); i++) {
                View itemView = items.get(i).create(bottomSheet, getItemViewLayoutId(), style, this);
                setItemWidth(itemView, itemWidth);
                parent.addView(itemView);
            }
        }

        private void setItemWidth(View itemView, int itemWidth) {
            LinearLayout.LayoutParams itemLp;
            if (itemView.getLayoutParams() != null) {
                itemLp = (LinearLayout.LayoutParams) itemView.getLayoutParams();
                itemLp.width = itemWidth;
            } else {
                itemLp = new LinearLayout.LayoutParams(itemWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                itemView.setLayoutParams(itemLp);
            }
            itemLp.gravity = Gravity.TOP;
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
