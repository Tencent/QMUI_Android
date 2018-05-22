package com.qmuiteam.qmuidemo.view;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.qmuiteam.qmui.layout.QMUIFrameLayout;
import com.qmuiteam.qmuidemo.R;

/**
 * Created by cgspine on 2018/3/22.
 */

public class QDShadowAdjustLayout extends QMUIFrameLayout {
    ViewDragHelper viewDragHelper;

    public QDShadowAdjustLayout(Context context) {
        this(context, null);
    }

    public QDShadowAdjustLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        viewDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return child.getId() == R.id.layout_for_test;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return left;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                return top;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return viewDragHelper.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewDragHelper.processTouchEvent(event);
        return true;
    }
}
