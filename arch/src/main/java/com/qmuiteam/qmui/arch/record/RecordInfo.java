package com.qmuiteam.qmui.arch.record;

import android.support.annotation.Nullable;

import java.util.Arrays;

public class RecordInfo {
    private int mId;
    private Class<?> mClazz;
    private Argument[] mArguments;

    public RecordInfo(int id, Class<?> clazz, Argument[] arguments) {
        mId = id;
        mClazz = clazz;
        mArguments = arguments;
    }

    public int getId() {
        return mId;
    }

    public Class<?> getClazz() {
        return mClazz;
    }

    public Argument[] getArguments() {
        return mArguments;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(!(obj instanceof RecordInfo)){
            return false;
        }
        RecordInfo other = (RecordInfo) obj;
        return other.mId == mId && other.mClazz == mClazz &&
                Arrays.equals(mArguments, other.mArguments);
    }

    public static class Argument {
        private String mName;
        private Class<?> mType;
        private Object mValue;

        public Argument(String name, Class<?> type, Object value){
            mName = name;
            mType = type;
            mValue = value;
        }

        public Class<?> getType() {
            return mType;
        }

        public String getName() {
            return mName;
        }

        public Object getValue() {
            return mValue;
        }
    }
}
