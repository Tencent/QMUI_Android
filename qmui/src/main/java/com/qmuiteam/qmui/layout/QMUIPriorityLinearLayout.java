package com.qmuiteam.qmui.layout;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.qmuiteam.qmui.R;

import java.util.ArrayList;

public class QMUIPriorityLinearLayout extends QMUILinearLayout {
    private ArrayList<View> mTempMiniWidthChildList = new ArrayList<>();
    private ArrayList<View> mTempDisposableChildList = new ArrayList<>();

    public QMUIPriorityLinearLayout(Context context) {
        super(context);
    }

    public QMUIPriorityLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int orientation = getOrientation();
        if (orientation == HORIZONTAL) {
            handleHorizontal(widthMeasureSpec, heightMeasureSpec);
        } else {
            handleVertical(widthMeasureSpec, heightMeasureSpec);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void handleHorizontal(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int visibleChildCount = getVisibleChildCount();
        if (widthMode == MeasureSpec.UNSPECIFIED || visibleChildCount == 0 || widthSize <= 0) {
            return;
        }
        int usedWidth = handlePriorityIncompressible(widthMeasureSpec, heightMeasureSpec);
        if (usedWidth >= widthSize) {
            for (View view : mTempMiniWidthChildList) {
                LayoutParams lp = (LayoutParams) view.getLayoutParams();
                view.measure(MeasureSpec.makeMeasureSpec(lp.miniContentProtectionSize, MeasureSpec.AT_MOST), heightMeasureSpec);
                lp.width = view.getMeasuredWidth();
            }
            for (View view : mTempDisposableChildList) {
                LayoutParams lp = (LayoutParams) view.getLayoutParams();
                lp.width = 0;
            }
        } else {
            int usefulWidth = widthSize - usedWidth;
            int miniNeedWidth = 0, miniWidthChildTotalWidth = 0, marginHor;
            for (View view : mTempMiniWidthChildList) {
                LayoutParams lp = (LayoutParams) view.getLayoutParams();
                view.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.AT_MOST), heightMeasureSpec);
                marginHor = lp.leftMargin + lp.rightMargin;
                miniWidthChildTotalWidth += view.getMeasuredWidth() + marginHor;
                miniNeedWidth += Math.min(view.getMeasuredWidth(), lp.miniContentProtectionSize) + marginHor;
            }
            if (miniNeedWidth >= usefulWidth) {
                for (View view : mTempMiniWidthChildList) {
                    LayoutParams lp = (LayoutParams) view.getLayoutParams();
                    lp.width = Math.min(view.getMeasuredWidth(), lp.miniContentProtectionSize);
                }
                for (View view : mTempDisposableChildList) {
                    LayoutParams lp = (LayoutParams) view.getLayoutParams();
                    lp.width = 0;
                }
            } else if (miniWidthChildTotalWidth < usefulWidth) {
                // there is a space for disposableChildList
                if (!mTempDisposableChildList.isEmpty()) {
                    dispatchSpaceToDisposableChildList(mTempDisposableChildList,
                            usefulWidth - miniWidthChildTotalWidth);
                }
            } else {
                // no space for disposableChild
                for (View view : mTempDisposableChildList) {
                    LayoutParams lp = (LayoutParams) view.getLayoutParams();
                    lp.width = 0;
                }
                if (usefulWidth < miniWidthChildTotalWidth && !mTempMiniWidthChildList.isEmpty()) {
                    dispatchSpaceToMiniWidthChildList(mTempMiniWidthChildList, usefulWidth, miniWidthChildTotalWidth);
                }
            }
        }
    }

    private void handleVertical(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSize = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int visibleChildCount = getVisibleChildCount();
        if (heightMode == MeasureSpec.UNSPECIFIED || visibleChildCount == 0 || heightSize <= 0) {
            return;
        }
        int usedHeight = handlePriorityIncompressible(widthMeasureSpec, heightMeasureSpec);
        if (usedHeight >= heightSize) {
            for (View view : mTempMiniWidthChildList) {
                LayoutParams lp = (LayoutParams) view.getLayoutParams();
                view.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(lp.miniContentProtectionSize, MeasureSpec.AT_MOST));
                lp.height = view.getMeasuredHeight();
            }
            for (View view : mTempDisposableChildList) {
                LayoutParams lp = (LayoutParams) view.getLayoutParams();
                lp.height = 0;
            }
        } else {
            int usefulSpace = heightSize - usedHeight;
            int miniNeedSpace = 0, miniSizeChildTotalLength = 0, marginVer;
            for (View view : mTempMiniWidthChildList) {
                LayoutParams lp = (LayoutParams) view.getLayoutParams();
                view.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST));
                marginVer = lp.topMargin + lp.bottomMargin;
                miniSizeChildTotalLength += view.getMeasuredHeight() + marginVer;
                miniNeedSpace += Math.min(view.getMeasuredHeight(), lp.miniContentProtectionSize) + marginVer;
            }
            if (miniNeedSpace >= usefulSpace) {
                for (View view : mTempMiniWidthChildList) {
                    LayoutParams lp = (LayoutParams) view.getLayoutParams();
                    lp.height = Math.min(view.getMeasuredHeight(), lp.miniContentProtectionSize);
                }
                for (View view : mTempDisposableChildList) {
                    LayoutParams lp = (LayoutParams) view.getLayoutParams();
                    lp.height = 0;
                }
            } else if (miniSizeChildTotalLength < usefulSpace) {
                // there is a space for disposableChildList
                if (!mTempDisposableChildList.isEmpty()) {
                    dispatchSpaceToDisposableChildList(mTempDisposableChildList,
                            usefulSpace - miniSizeChildTotalLength);
                }
            } else {
                // no space for disposableChild
                for (View view : mTempDisposableChildList) {
                    LayoutParams lp = (LayoutParams) view.getLayoutParams();
                    lp.width = 0;
                }
                if (usefulSpace < miniSizeChildTotalLength && !mTempMiniWidthChildList.isEmpty()) {
                    dispatchSpaceToMiniWidthChildList(mTempMiniWidthChildList, usefulSpace, miniSizeChildTotalLength);
                }
            }
        }
    }

    private int handlePriorityIncompressible(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int heightSize = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
        int usedSize = 0;
        mTempMiniWidthChildList.clear();
        mTempDisposableChildList.clear();
        int orientation = getOrientation();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            lp.backupOrRestore();
            int priority = lp.getPriority(orientation);
            int margin = orientation == HORIZONTAL ? lp.leftMargin + lp.rightMargin :
                    lp.topMargin + lp.bottomMargin;
            if (priority == LayoutParams.PRIORITY_INCOMPRESSIBLE) {
                if (orientation == HORIZONTAL) {
                    if (lp.width >= 0) {
                        usedSize += lp.width + margin;
                    } else {
                        child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.AT_MOST), heightMeasureSpec);
                        usedSize += child.getMeasuredWidth() + margin;
                    }
                } else {
                    if (lp.height >= 0) {
                        usedSize += lp.height + margin;
                    } else {
                        child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST));
                        usedSize += child.getMeasuredHeight() + margin;
                    }
                }
            } else if (priority == LayoutParams.PRIORITY_MINI_CONTENT_PROTECTION) {
                mTempMiniWidthChildList.add(child);
            } else {
                if (lp.weight == 0) {
                    mTempDisposableChildList.add(child);
                }
            }
        }
        return usedSize;
    }

    protected void dispatchSpaceToDisposableChildList(ArrayList<View> childList, int usefulSpace) {
        for (View view : childList) {
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (getOrientation() == HORIZONTAL) {
                usefulSpace -= lp.leftMargin - lp.rightMargin;
            } else {
                usefulSpace -= lp.topMargin - lp.bottomMargin;
            }
        }
        int avgSpace = Math.max(0, usefulSpace / childList.size());
        for (View view : childList) {
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (getOrientation() == HORIZONTAL) {
                lp.width = avgSpace;
            } else {
                lp.height = avgSpace;
            }
        }
    }

    protected void dispatchSpaceToMiniWidthChildList(ArrayList<View> childList, int usefulSpace,
                                                     int calculateTotalLength) {
        int extra = calculateTotalLength - usefulSpace;
        if (extra > 0) {
            for (View view : childList) {
                LayoutParams lp = (LayoutParams) view.getLayoutParams();
                if (getOrientation() == HORIZONTAL) {
                    float radio = (view.getMeasuredWidth() + lp.leftMargin + lp.rightMargin)
                            * 1f / calculateTotalLength;
                    int width = (int) (view.getMeasuredWidth() - extra * radio);
                    lp.width = Math.max(0, width);
                } else {
                    float radio = (view.getMeasuredHeight() + lp.topMargin + lp.bottomMargin)
                            * 1f / calculateTotalLength;
                    int height = (int) (view.getMeasuredHeight() - extra * radio);
                    lp.height = Math.max(0, height);
                }
            }
        }
    }

    private int getVisibleChildCount() {
        int childCount = getChildCount();
        int visibleChildCount = 0;
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i).getVisibility() == VISIBLE) {
                visibleChildCount++;
            }
        }
        return visibleChildCount;
    }

    @Override
    protected LinearLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        return new LayoutParams(lp);
    }

    @Override
    public LinearLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LinearLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams && super.checkLayoutParams(p);
    }

    public static class LayoutParams extends LinearLayout.LayoutParams {
        static final int PRIORITY_DISPOSABLE = 1;
        static final int PRIORITY_MINI_CONTENT_PROTECTION = 2;
        static final int PRIORITY_INCOMPRESSIBLE = 3;

        private int priority = PRIORITY_MINI_CONTENT_PROTECTION;
        public int miniContentProtectionSize = 0;

        private int backupWidth = Integer.MIN_VALUE;
        private int backupHeight = Integer.MIN_VALUE;


        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            final TypedArray a = c.obtainStyledAttributes(attrs,
                    R.styleable.QMUIPriorityLinearLayout_Layout);
            priority = a.getInteger(R.styleable.QMUIPriorityLinearLayout_Layout_qmui_layout_priority,
                    PRIORITY_MINI_CONTENT_PROTECTION);
            miniContentProtectionSize = a.getDimensionPixelSize(
                    R.styleable.QMUIPriorityLinearLayout_Layout_qmui_layout_miniContentProtectionSize,
                    0);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, float weight) {
            super(width, height, weight);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        @TargetApi(19)
        public LayoutParams(LinearLayout.LayoutParams source) {
            super(source);
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public int getPriority(int orientation) {
            if (weight > 0) {
                return PRIORITY_DISPOSABLE;
            }
            if (orientation == LinearLayout.HORIZONTAL) {
                if (width >= 0) {
                    return PRIORITY_INCOMPRESSIBLE;
                }
            } else {
                if (height >= 0) {
                    return PRIORITY_INCOMPRESSIBLE;
                }
            }
            return priority;
        }

        void backupOrRestore() {
            if (backupWidth == Integer.MIN_VALUE) {
                backupWidth = width;
            } else {
                width = backupWidth;
            }
            if (backupHeight == Integer.MIN_VALUE) {
                backupHeight = height;
            } else {
                height = backupHeight;
            }
        }
    }
}
