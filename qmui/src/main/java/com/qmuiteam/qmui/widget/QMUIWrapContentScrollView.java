package com.qmuiteam.qmui.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * height is wrapContent but limited by maxHeight
 * <p>
 * Created by cgspine on 2017/12/21.
 */

public class QMUIWrapContentScrollView extends QMUIObservableScrollView {
    private int mMaxHeight = Integer.MAX_VALUE >> 2;

    public QMUIWrapContentScrollView(Context context) {
        super(context);
    }

    public QMUIWrapContentScrollView(Context context, int maxHeight) {
        super(context);
        mMaxHeight = maxHeight;
    }

    public QMUIWrapContentScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QMUIWrapContentScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setMaxHeight(int maxHeight) {
        if (mMaxHeight != maxHeight) {
            mMaxHeight = maxHeight;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(mMaxHeight,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
