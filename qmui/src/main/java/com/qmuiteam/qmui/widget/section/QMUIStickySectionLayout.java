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

package com.qmuiteam.qmui.widget.section;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.qmuiteam.qmui.layout.QMUIFrameLayout;

import java.util.ArrayList;
import java.util.List;

public class QMUIStickySectionLayout extends QMUIFrameLayout implements QMUIStickySectionAdapter.ViewCallback {

    private RecyclerView mRecyclerView;
    private QMUIFrameLayout mStickySectionWrapView;
    private QMUIStickySectionItemDecoration mStickySectionItemDecoration;
    private int mStickySectionViewHeight = -1;
    private List<DrawDecoration> mDrawDecorations;
    /**
     * if scrollToPosition happened before mStickySectionWrapView finished layout,
     * the target item may be covered by mStickySectionWrapView, so we delay to
     * execute the scroll action
     */
    private Runnable mPendingScrollAction = null;

    public QMUIStickySectionLayout(Context context) {
        this(context, null);
    }

    public QMUIStickySectionLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QMUIStickySectionLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mStickySectionWrapView = new QMUIFrameLayout(context);
        mRecyclerView = new RecyclerView(context);
        addView(mRecyclerView, new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(mStickySectionWrapView, new LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mStickySectionWrapView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mStickySectionViewHeight = bottom - top;
                if (mStickySectionViewHeight > 0 && mPendingScrollAction != null) {
                    mPendingScrollAction.run();
                    mPendingScrollAction = null;
                }
            }
        });
    }

    public void addDrawDecoration(@NonNull DrawDecoration drawDecoration){
        if(mDrawDecorations == null){
            mDrawDecorations = new ArrayList<>();
        }
        mDrawDecorations.add(drawDecoration);
    }

    public void removeDrawDecoration(@NonNull DrawDecoration drawDecoration){
        if(mDrawDecorations == null || mDrawDecorations.isEmpty()){
            return;
        }
        mDrawDecorations.remove(drawDecoration);
    }

    public void configStickySectionWrapView(StickySectionWrapViewConfig stickySectionWrapViewConfig) {
        if (stickySectionWrapViewConfig != null) {
            stickySectionWrapViewConfig.config(mStickySectionWrapView);
        }
    }

    public QMUIFrameLayout getStickySectionWrapView() {
        return mStickySectionWrapView;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public @Nullable
    View getStickySectionView() {
        if (mStickySectionWrapView.getVisibility() != View.VISIBLE
                || mStickySectionWrapView.getChildCount() == 0) {
            return null;
        }
        return mStickySectionWrapView.getChildAt(0);
    }

    /**
     * proxy to {@link RecyclerView#setLayoutManager(RecyclerView.LayoutManager)}
     *
     * @param layoutManager LayoutManager to use
     */
    public void setLayoutManager(@NonNull RecyclerView.LayoutManager layoutManager) {
        mRecyclerView.setLayoutManager(layoutManager);
    }

    /**
     * section header will be sticky when scrolling, see {@link #setAdapter(QMUIStickySectionAdapter, boolean)}
     *
     * @param adapter the adapter inherited from QMUIStickySectionAdapter
     * @param <H>     generic parameter of QMUIStickySectionAdapter, indicating the section header
     * @param <T>     generic parameter of QMUIStickySectionAdapter, indicating the section item
     * @param <VH>    generic parameter of QMUIStickySectionAdapter, indicating the view holder
     */
    public <H extends QMUISection.Model<H>,
            T extends QMUISection.Model<T>,
            VH extends QMUIStickySectionAdapter.ViewHolder> void setAdapter(
            QMUIStickySectionAdapter<H, T, VH> adapter) {
        setAdapter(adapter, true);
    }


    /**
     * set the adapter for recyclerView, the parameter sticky indicates whether
     * the section header is sticky or not when scrolling.
     *
     * @param adapter the adapter inherited from QMUIStickySectionAdapter
     * @param sticky  if true, make the section header sticky when scrolling
     * @param <H>     generic parameter of QMUIStickySectionAdapter, indicating the section header
     * @param <T>     generic parameter of QMUIStickySectionAdapter, indicating the section item
     * @param <VH>    generic parameter of QMUIStickySectionAdapter, indicating the view holder
     */
    public <H extends QMUISection.Model<H>,
            T extends QMUISection.Model<T>,
            VH extends QMUIStickySectionAdapter.ViewHolder> void setAdapter(
            final QMUIStickySectionAdapter<H, T, VH> adapter, boolean sticky) {
        if (sticky) {
            QMUIStickySectionItemDecoration.Callback<VH> callback = new QMUIStickySectionItemDecoration.Callback<VH>() {
                @Override
                public int getRelativeStickyItemPosition(int pos) {
                    return adapter.getRelativeStickyPosition(pos);
                }

                @Override
                public boolean isHeaderItem(int pos) {
                    return adapter.getItemViewType(pos) == QMUIStickySectionAdapter.ITEM_TYPE_SECTION_HEADER;
                }

                @Override
                public VH createViewHolder(ViewGroup parent, int viewType) {
                    return adapter.createViewHolder(parent, viewType);
                }

                @Override
                public void bindViewHolder(VH holder, int position) {
                    adapter.bindViewHolder(holder, position);
                }

                @Override
                public int getItemViewType(int position) {
                    return adapter.getItemViewType(position);
                }

                @Override
                public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
                    adapter.registerAdapterDataObserver(observer);
                }

                @Override
                public void onHeaderVisibilityChanged(boolean visible) {

                }

                @Override
                public void invalidate() {
                    mRecyclerView.invalidate();
                }
            };
            mStickySectionItemDecoration = new QMUIStickySectionItemDecoration<>(mStickySectionWrapView, callback);
            mRecyclerView.addItemDecoration(mStickySectionItemDecoration);
        }


        adapter.setViewCallback(this);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mStickySectionItemDecoration != null) {
            mStickySectionWrapView.layout(mStickySectionWrapView.getLeft(),
                    mStickySectionItemDecoration.getTargetTop(),
                    mStickySectionWrapView.getRight(),
                    mStickySectionItemDecoration.getTargetTop() + mStickySectionWrapView.getHeight());
        }
    }

    @Override
    public void scrollToPosition(final int position, boolean isSectionHeader, final boolean scrollToTop) {
        mPendingScrollAction = null;
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (adapter == null || position < 0 || position >= adapter.getItemCount()) {
            return;
        }

        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            int firstVPos = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
            int lastVPos = linearLayoutManager.findLastCompletelyVisibleItemPosition();
            int offset = 0;
            if (!isSectionHeader) {
                if (mStickySectionViewHeight <= 0) {
                    // delay to re scroll
                    mPendingScrollAction = new Runnable() {
                        @Override
                        public void run() {
                            scrollToPosition(position, false, scrollToTop);
                        }
                    };
                }
                offset = mStickySectionWrapView.getHeight();
            }
            if (position < firstVPos + 1 /* increase one to avoid being covered */ || position > lastVPos || scrollToTop) {
                linearLayoutManager.scrollToPositionWithOffset(position, offset);
            }
        } else {
            mRecyclerView.scrollToPosition(position);
        }
    }

    @Nullable
    @Override
    public RecyclerView.ViewHolder findViewHolderForAdapterPosition(int position) {
        return mRecyclerView.findViewHolderForAdapterPosition(position);
    }

    @Override
    public void requestChildFocus(View view) {
        mRecyclerView.requestChildFocus(view, null);
    }

    public interface StickySectionWrapViewConfig {
        void config(QMUIFrameLayout stickySectionWrapView);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if(mDrawDecorations != null){
            for(DrawDecoration drawDecoration: mDrawDecorations){
                drawDecoration.onDraw(canvas, this);
            }
        }
        super.dispatchDraw(canvas);
        if(mDrawDecorations != null){
            for(DrawDecoration drawDecoration: mDrawDecorations){
                drawDecoration.onDrawOver(canvas, this);
            }
        }
    }

    @Override
    public void onDescendantInvalidated(@NonNull View child, @NonNull View target) {
        super.onDescendantInvalidated(child, target);
        if(target == mRecyclerView && mDrawDecorations != null && !mDrawDecorations.isEmpty()){
            invalidate();
        }
    }

    public interface DrawDecoration {
        void onDraw(@NonNull Canvas c, @NonNull QMUIStickySectionLayout parent);
        void onDrawOver(@NonNull Canvas c, @NonNull QMUIStickySectionLayout parent);
    }
}
