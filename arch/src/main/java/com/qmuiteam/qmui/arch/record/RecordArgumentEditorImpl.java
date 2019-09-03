package com.qmuiteam.qmui.arch.record;

import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;


public class RecordArgumentEditorImpl implements RecordArgumentEditor {

    private HashMap<String, Argument> mMap = new HashMap<>();

    @Override

    public synchronized RecordArgumentEditor putString(String key, @Nullable String value) {
        mMap.put(key, new Argument(value, String.class));
        return this;
    }

    @Override
    public synchronized RecordArgumentEditor putInt(String key, int value) {
        mMap.put(key, new Argument(value, Integer.TYPE));
        return this;
    }

    @Override
    public synchronized RecordArgumentEditor putLong(String key, long value) {
        mMap.put(key, new Argument(value, Long.TYPE));
        return this;
    }

    @Override
    public synchronized RecordArgumentEditor putFloat(String key, float value) {
        mMap.put(key, new Argument(value, Float.TYPE));
        return this;
    }

    @Override
    public synchronized RecordArgumentEditor putBoolean(String key, boolean value) {
        mMap.put(key, new Argument(value, Boolean.TYPE));
        return this;
    }

    @Override
    public synchronized RecordArgumentEditor remove(String key) {
        mMap.remove(key);
        return this;
    }

    @Override
    public synchronized RecordArgumentEditor clear() {
        mMap.clear();
        return this;
    }

    @Override
    public Map<String, Argument> getAll() {
        return new HashMap<>(mMap);
    }
}
