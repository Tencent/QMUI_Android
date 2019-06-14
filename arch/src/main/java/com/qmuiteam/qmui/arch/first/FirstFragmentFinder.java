package com.qmuiteam.qmui.arch.first;

import com.qmuiteam.qmui.arch.QMUIFragment;

public interface FirstFragmentFinder {
    int NO_ID = -1;
    Class<? extends QMUIFragment> getFragmentClassById(int id);
    int getIdByFragmentClass(Class<? extends QMUIFragment> clazz);
}
