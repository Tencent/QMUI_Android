package com.qmuiteam.qmui.arch.record;

import com.qmuiteam.qmui.arch.QMUILatestVisit;

public interface LatestVisitArgumentSaver {

    /**
     * Called by {@link QMUILatestVisit} to save argument value
     * Notice: This is called before onResume. So It can not used to save data
     * produced after fragment resumed.
     * @param argumentName
     * @return must be one of String, Boolean, Int, Long, Float. if null, then will use default value
     */
    Object getArgumentValueForLatestVisit(String argumentName);
}
