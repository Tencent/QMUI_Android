package com.qmuiteam.qmui.arch.record;

import android.os.Bundle;

import java.util.Map;

import androidx.annotation.Nullable;

public interface RecordArgumentEditor {

    RecordArgumentEditor putString(String key, @Nullable String value);

    RecordArgumentEditor putInt(String key, int value);

    RecordArgumentEditor putLong(String key, long value);

    RecordArgumentEditor putFloat(String key, float value);

    RecordArgumentEditor putBoolean(String key, boolean value);

    RecordArgumentEditor put(String key, RecordArgumentEditor.Argument argument);

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

        public void putToBundle(Bundle bundle, String key){
            if(type == Integer.TYPE){
                bundle.putInt(key, (Integer)value);
            }else if(type == Boolean.TYPE){
                bundle.putBoolean(key, (Boolean) value);
            }else if(type == Long.TYPE){
                bundle.putLong(key, (Long) value);
            }else if(type == Float.TYPE){
                bundle.putFloat(key, (Float) value);
            }else if(type == String.class){
                bundle.putString(key, (String) value);
            }
        }
    }
}
