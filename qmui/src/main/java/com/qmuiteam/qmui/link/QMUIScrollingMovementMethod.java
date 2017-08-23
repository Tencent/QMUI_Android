package com.qmuiteam.qmui.link;

import android.text.Spannable;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.method.Touch;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * @author cginechen
 * @date 2017-03-20
 */

public class QMUIScrollingMovementMethod extends ScrollingMovementMethod {

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        return sHelper.onTouchEvent(widget, buffer, event)
                || Touch.onTouchEvent(widget, buffer, event);
    }

    public static MovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new QMUIScrollingMovementMethod();

        return sInstance;
    }

    private static QMUIScrollingMovementMethod sInstance;
    private static QMUILinkTouchDecorHelper sHelper = new QMUILinkTouchDecorHelper();
}
