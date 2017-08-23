package com.qmuiteam.qmui.widget.pullRefreshLayout;

import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout.RefreshOffsetCalculator;

/**
 * 当targetCurrentOffset < refreshViewHeight, refreshView跟随targetView，其距离为0
 * 当targetCurrentOffset >= targetRefreshOffset RefreshView垂直方向永远居中于在[0, targetCurrentOffset]
 *
 * @author cginechen
 * @date 2017-06-07
 */

public class QMUICenterGravityRefreshOffsetCalculator implements RefreshOffsetCalculator {

    @Override
    public int calculateRefreshOffset(int refreshInitOffset, int refreshEndOffset, int refreshViewHeight, int targetCurrentOffset, int targetInitOffset, int targetRefreshOffset) {
        if(targetCurrentOffset < refreshViewHeight){
            return targetCurrentOffset - refreshViewHeight;
        }
        return (targetCurrentOffset - refreshViewHeight) / 2;
    }
}
