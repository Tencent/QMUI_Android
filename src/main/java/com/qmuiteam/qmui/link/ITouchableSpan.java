package com.qmuiteam.qmui.link;

import android.view.View;

/**
 * @author cginechen
 * @date 2017-03-20
 */

public interface ITouchableSpan {
    void setPressed(boolean pressed);
    void onClick(View widget);
}
