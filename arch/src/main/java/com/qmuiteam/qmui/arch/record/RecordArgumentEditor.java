package com.qmuiteam.qmui.arch.record;

import android.support.annotation.Nullable;

import java.util.Map;

public interface RecordArgumentEditor {

    RecordArgumentEditor putString(String key, @Nullable String value);

    RecordArgumentEditor putInt(String key, int value);

    RecordArgumentEditor putLong(String key, long value);

    RecordArgumentEditor putFloat(String key, float value);

    RecordArgumentEditor putBoolean(String key, boolean value);

    RecordArgumentEditor remove(String key);

    RecordArgumentEditor clear();

    Map<String, Argument> getAll();

    class Argument {
        private Object value;
        private Class<?> type;

        public Argument(Object value, Class<?> type) {
            this.value = value;
            this.type = type;
        }

        public Object getValue() {
            return value;
        }

        public Class<?> getType() {
            return type;
        }
    }
}
