package com.qmuiteam.qmui.link;

import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.method.Touch;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * 配合 {@link QMUILinkTouchDecorHelper} 使用
 *
 * @author cginechen
 * @date 2017-03-20
 */

public class QMUILinkTouchMovementMethod extends LinkMovementMethod {

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        return sHelper.onTouchEvent(widget, buffer, event)
                || Touch.onTouchEvent(widget, buffer, event);
    }

    public static MovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new QMUILinkTouchMovementMethod();

        return sInstance;
    }

    private static QMUILinkTouchMovementMethod sInstance;
    private static QMUILinkTouchDecorHelper sHelper = new QMUILinkTouchDecorHelper();

}
