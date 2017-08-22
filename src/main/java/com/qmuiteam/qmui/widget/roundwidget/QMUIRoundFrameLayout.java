package com.qmuiteam.qmui.widget.roundwidget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * 见 {@link QMUIRoundButton} 与 {@link QMUIRoundButtonDrawable}
 */
public class QMUIRoundFrameLayout extends FrameLayout {

    public QMUIRoundFrameLayout(Context context) {
        super(context);
        init(context, null);
    }

    public QMUIRoundFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public QMUIRoundFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        QMUIRoundButtonDrawable bg = QMUIRoundButtonDrawable.fromAttributeSet(context, attrs);
        setBackgroundDrawable(bg);
    }
}
