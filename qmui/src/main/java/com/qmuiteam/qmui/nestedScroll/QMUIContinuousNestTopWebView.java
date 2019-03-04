package com.qmuiteam.qmuidemo.richNest;

import android.content.Context;
import android.util.AttributeSet;

import com.qmuiteam.qmui.widget.webview.QMUIWebView;

public class QMUIRichNestTopWebView extends QMUIWebView implements IQMUIRichNestedTopView {
    public QMUIRichNestTopWebView(Context context) {
        super(context);
    }

    public QMUIRichNestTopWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QMUIRichNestTopWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int consumeScroll(int yUnconsumed) {
        // compute the consumed value
        int scrollY = getScrollY();
        int range = computeVerticalScrollRange();
        int viewHeight = getHeight();
        int maxScrollY = range - viewHeight;
        // the scrollY may be negative or larger than scrolling range
        scrollY = Math.max(0, Math.min(scrollY, maxScrollY));
        int dy = 0;
        if (yUnconsumed < 0) {
            dy = Math.max(yUnconsumed, -scrollY);
        } else if (yUnconsumed > 0) {
            dy = Math.min(yUnconsumed, maxScrollY - scrollY);
        }
        scrollBy(0, dy);
        return yUnconsumed - dy;
    }
}
