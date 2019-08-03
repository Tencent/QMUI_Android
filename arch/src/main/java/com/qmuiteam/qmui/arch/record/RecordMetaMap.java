package com.qmuiteam.qmui.arch.record;

public interface RecordMetaMap {

    RecordMeta getRecordMetaById(int id);

    RecordMeta getRecordMetaByClass(Class<?> clazz);
}
