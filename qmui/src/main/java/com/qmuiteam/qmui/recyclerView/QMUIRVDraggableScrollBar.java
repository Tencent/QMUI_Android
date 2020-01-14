package com.qmuiteam.qmui.recyclerView;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.util.QMUILangHelper;

public class QMUIRVDraggableScrollBar extends RecyclerView.ItemDecoration {
    private int[] STATE_PRESSED = new int[]{android.R.attr.state_pressed};
    private int[] STATE_NORMAL = new int[]{};
    private static final long DEFAULT_KEE_SHOW_DURATION = 800L;
    private static final long DEFAULT_TRANSITION_DURATION = 100L;

    RecyclerView mRecyclerView;
    private final int mStartMargin;
    private final int mEndMargin;
    private final int mInwardOffset;
    private final boolean mIsVerticalScroll;
    private final boolean mIsLocationInOppositeSide;
    private boolean mIsInDragging;
    private Drawable mScrollBarDrawable;
    private boolean mEnableScrollBarFadeInOut = false;
    private boolean mIsDraggable = true;
    private Callback mCallback;

    private long mKeepShownTime = DEFAULT_KEE_SHOW_DURATION;
    private long mTransitionDuration = DEFAULT_TRANSITION_DURATION;
    private long mStartTransitionTime = 0;
    private int mBeginAlpha = -1;
    private int mTargetAlpha = -1;
    private int mCurrentAlpha = 255;
    private float mPercent = 0f;
    private int mDragInnerStart = 0;

    public QMUIRVDraggableScrollBar(int startMargin,
                                    int endMargin,
                                    int inwardOffset,
                                    boolean isVerticalScroll,
                                    boolean isLocationInOppositeSide) {
        mStartMargin = startMargin;
        mEndMargin = endMargin;
        mInwardOffset = inwardOffset;
        mIsVerticalScroll = isVerticalScroll;
        mIsLocationInOppositeSide = isLocationInOppositeSide;
    }

    public QMUIRVDraggableScrollBar(int startMargin,
                                    int endMargin,
                                    int inwardOffset) {
        this(startMargin, endMargin, inwardOffset, true, false);
    }

    private Runnable mFadeScrollBarAction = new Runnable() {
        @Override
        public void run() {
            mTargetAlpha = 0;
            mBeginAlpha = mCurrentAlpha;
            mStartTransitionTime = System.currentTimeMillis();
            if (mRecyclerView != null) {
                mRecyclerView.invalidate();
            }
        }
    };

    private final RecyclerView.OnItemTouchListener mOnItemTouchListener = new RecyclerView.OnItemTouchListener() {

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            if (!mIsDraggable || mScrollBarDrawable == null) {
                return false;
            }
            int action = e.getAction();
            final int x = (int) e.getX();
            final int y = (int) e.getY();
            if (action == MotionEvent.ACTION_DOWN) {
                Rect bounds = mScrollBarDrawable.getBounds();
                if (mCurrentAlpha > 0 && bounds.contains(x, y)) {
                    startDrag();
                    mDragInnerStart = mIsVerticalScroll ? y - bounds.top : x - bounds.left;
                }
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (mIsInDragging) {
                    onDragging(rv, mScrollBarDrawable, x, y);
                }
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                if (mIsInDragging) {
                    onDragging(rv, mScrollBarDrawable, x, y);
                    endDrag();
                }
            }
            Log.i("cgine", "mIsInDragging = " + mIsInDragging);
            return mIsInDragging;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            if (!mIsDraggable || mScrollBarDrawable == null) {
                return;
            }
            int action = e.getAction();
            final int x = (int) e.getX();
            final int y = (int) e.getY();
            if (action == MotionEvent.ACTION_DOWN) {
                Rect bounds = mScrollBarDrawable.getBounds();
                if (mCurrentAlpha > 0 && bounds.contains(x, y)) {
                    startDrag();
                    mDragInnerStart = mIsVerticalScroll ? y - bounds.top : x - bounds.left;
                }
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (mIsInDragging) {
                    onDragging(rv, mScrollBarDrawable, x, y);
                }
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                if (mIsInDragging) {
                    onDragging(rv, mScrollBarDrawable, x, y);
                    endDrag();
                }
            }
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            if (disallowIntercept && mIsInDragging) {
                endDrag();
            }
        }
    };

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {

        private int mPrevStatus = RecyclerView.SCROLL_STATE_IDLE;

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            if (mEnableScrollBarFadeInOut) {
                if (mPrevStatus == RecyclerView.SCROLL_STATE_IDLE && newState != RecyclerView.SCROLL_STATE_IDLE) {
                    mStartTransitionTime = System.currentTimeMillis();
                    mBeginAlpha = mCurrentAlpha;
                    mTargetAlpha = 255;
                    if (mRecyclerView != null) {
                        mRecyclerView.invalidate();
                    }
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    recyclerView.postDelayed(mFadeScrollBarAction, mKeepShownTime);
                }
            }
            mPrevStatus = newState;
        }
    };

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void setScrollBarDrawable(@Nullable Drawable scrollBarDrawable) {
        mScrollBarDrawable = scrollBarDrawable;
        if (scrollBarDrawable != null) {
            scrollBarDrawable.setState(mIsInDragging ? STATE_PRESSED : STATE_NORMAL);
        }
    }

    public void setDraggable(boolean draggable) {
        mIsDraggable = draggable;
    }

    public boolean isDraggable() {
        return mIsDraggable;
    }

    public void setEnableScrollBarFadeInOut(boolean enableScrollBarFadeInOut) {
        if (mEnableScrollBarFadeInOut != enableScrollBarFadeInOut) {
            mEnableScrollBarFadeInOut = enableScrollBarFadeInOut;
            if (!mEnableScrollBarFadeInOut) {
                mBeginAlpha = -1;
                mTargetAlpha = -1;
                mCurrentAlpha = 255;
            }else{
                if(mRecyclerView != null){
                    if(mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE){
                        mCurrentAlpha = 0;
                    }
                }else{
                    mCurrentAlpha = 0;
                }
            }
            if (mRecyclerView != null) {
                mRecyclerView.invalidate();
            }
        }
    }

    public boolean isEnableScrollBarFadeInOut() {
        return mEnableScrollBarFadeInOut;
    }

    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) {
        if (mRecyclerView == recyclerView) {
            return; // nothing to do
        }
        if (mRecyclerView != null) {
            destroyCallbacks();
        }
        mRecyclerView = recyclerView;
        if (recyclerView != null) {
            final Resources resources = recyclerView.getResources();
            setupCallbacks();
        }
    }

    private void setupCallbacks() {
        mRecyclerView.addItemDecoration(this);
        mRecyclerView.addOnItemTouchListener(mOnItemTouchListener);
        mRecyclerView.addOnScrollListener(mScrollListener);
    }

    private void destroyCallbacks() {
        mRecyclerView.removeItemDecoration(this);
        mRecyclerView.removeOnItemTouchListener(mOnItemTouchListener);
        mRecyclerView.removeCallbacks(mFadeScrollBarAction);
        mRecyclerView.removeOnScrollListener(mScrollListener);
    }

    private void startDrag() {
        mIsInDragging = true;
        if (mScrollBarDrawable != null) {
            mScrollBarDrawable.setState(STATE_PRESSED);
        }
        if (mCallback != null) {
            mCallback.onDragStarted();
        }
        if (mRecyclerView != null) {
            mRecyclerView.removeCallbacks(mFadeScrollBarAction);
            mRecyclerView.invalidate();
        }
    }

    private void endDrag() {
        mIsInDragging = false;
        if (mScrollBarDrawable != null) {
            mScrollBarDrawable.setState(STATE_NORMAL);
        }
        if (mCallback != null) {
            mCallback.onDragEnd();
        }
        if(mRecyclerView != null){
            mRecyclerView.invalidate();
        }
    }

    private void onDragging(RecyclerView recyclerView, Drawable drawable, int x, int y) {
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        int usefulSpace = getUsefulSpace(recyclerView) - (mIsVerticalScroll ? drawableHeight : drawableWidth);
        int useValue = mIsVerticalScroll ? y : x;
        float percent = (useValue - mStartMargin - mDragInnerStart) * 1f / usefulSpace;
        percent = QMUILangHelper.constrain(percent, 0f, 1f);
        if (mCallback != null) {
            mCallback.onDragToPercent(percent);
        }
        mPercent = percent;

        if (percent <= 0) {
            recyclerView.scrollToPosition(0);
        } else if (percent >= 1f) {
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            if (adapter != null) {
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        } else {
            int range = getScrollRange(recyclerView);
            int offset = getCurrentOffset(recyclerView);
            int delta = (int) (range * mPercent - offset);
            if (mIsVerticalScroll) {
                recyclerView.scrollBy(0, delta);
            } else {
                recyclerView.scrollBy(delta, 0);
            }
        }
        recyclerView.invalidate();
    }


    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        Drawable drawable = ensureScrollBar(parent);
        if (drawable == null) {
            return;
        }

        if (mTargetAlpha != -1 && mBeginAlpha != -1) {
            long transitionTime = System.currentTimeMillis() - mStartTransitionTime;
            long duration = mTransitionDuration * Math.abs(mTargetAlpha - mBeginAlpha) / 255;
            if (transitionTime >= duration) {
                mCurrentAlpha = mTargetAlpha;
                mTargetAlpha = -1;
                mBeginAlpha = -1;
            } else {
                mCurrentAlpha = (int) (mBeginAlpha + (mTargetAlpha - mBeginAlpha) * transitionTime * 1f / duration);
                ViewCompat.postInvalidateOnAnimation(parent);
            }
        }

        drawable.setAlpha(mCurrentAlpha);

        if (!mIsInDragging) {
            mPercent = calculatePercent(parent);
        }
        setScrollBarBounds(parent, drawable);
        drawable.draw(c);
    }

    private int getUsefulSpace(@NonNull RecyclerView recyclerView) {
        if (mIsVerticalScroll) {
            return recyclerView.getHeight() - mStartMargin - mEndMargin;
        }
        return recyclerView.getWidth() - mStartMargin - mEndMargin;
    }

    private void setScrollBarBounds(@NonNull RecyclerView recyclerView, @NonNull Drawable drawable) {
        int usefulSpace = getUsefulSpace(recyclerView);
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        int left, top;
        if (mIsVerticalScroll) {
            top = (int) ((usefulSpace - drawableHeight) * mPercent);
            left = mIsLocationInOppositeSide ? mInwardOffset : (recyclerView.getWidth() - drawableWidth - mInwardOffset);

        } else {
            left = (int) ((usefulSpace - drawableWidth) * mPercent);
            top = mIsLocationInOppositeSide ? mInwardOffset : (recyclerView.getHeight() - drawableHeight - mInwardOffset);
        }
        drawable.setBounds(left, top, left + drawableWidth, top + drawableHeight);
    }

    private int getScrollRange(@NonNull RecyclerView recyclerView) {
        if (mIsVerticalScroll) {
            return recyclerView.computeVerticalScrollRange() - recyclerView.getHeight();
        } else {
            return recyclerView.computeHorizontalScrollRange() - recyclerView.getWidth();
        }
    }

    private int getCurrentOffset(@NonNull RecyclerView recyclerView) {
        if (mIsVerticalScroll) {
            return recyclerView.computeVerticalScrollOffset();
        }
        return recyclerView.computeHorizontalScrollOffset();
    }

    private float calculatePercent(@NonNull RecyclerView recyclerView) {
        return getCurrentOffset(recyclerView) * 1f / getScrollRange(recyclerView);
    }

    public Drawable ensureScrollBar(RecyclerView recyclerView) {
        if (mScrollBarDrawable == null) {
            mScrollBarDrawable = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.qmui_icon_scroll_bar);
        }
        return mScrollBarDrawable;
    }

    interface Callback {
        void onDragStarted();

        void onDragToPercent(float percent);

        void onDragEnd();
    }
}
