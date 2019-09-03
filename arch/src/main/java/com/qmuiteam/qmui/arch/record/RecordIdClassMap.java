package com.qmuiteam.qmui.arch.record;

public interface RecordIdClassMap {

    Class<?> getRecordClassById(int id);

    int getIdByRecordClass(Class<?> clazz);
}
