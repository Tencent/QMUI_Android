package com.qmuiteam.qmui.widget.textview;

/**
 * @author cginechen
 * @date 2017-08-07
 */

public interface ISpanTouchFix {
    /**
     * 记录当前 Touch 事件对应的点是不是点在了 span 上面
     */
    void setTouchSpanHit(boolean hit);
}
