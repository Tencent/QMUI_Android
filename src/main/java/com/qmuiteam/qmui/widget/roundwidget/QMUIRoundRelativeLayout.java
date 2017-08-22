package com.qmuiteam.qmui.widget.roundwidget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * 见 {@link QMUIRoundButton} 与 {@link QMUIRoundButtonDrawable}
 */
public class QMUIRoundRelativeLayout extends RelativeLayout {

    public QMUIRoundRelativeLayout(Context context) {
        super(context);
        init(context, null);
    }

    public QMUIRoundRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public QMUIRoundRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        QMUIRoundButtonDrawable bg = QMUIRoundButtonDrawable.fromAttributeSet(context, attrs);
        setBackgroundDrawable(bg);
    }
}
