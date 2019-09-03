package com.qmuiteam.qmui.arch.record;

import com.qmuiteam.qmui.arch.QMUILatestVisit;

public interface LatestVisitArgumentCollector {

    /**
     * Called by {@link QMUILatestVisit} to collect argument value
     * Notice: This is called before onResume. So It can not used to save data
     * produced after fragment resumed.
     * @param editor RecordArgumentEditor
     */
    void onCollectLatestVisitArgument(RecordArgumentEditor editor);
}
