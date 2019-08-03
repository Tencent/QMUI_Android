package com.qmuiteam.qmui.arch.record;

public class RecordMeta {

    private int mId;
    private Class<?> mClazz;
    private ArgumentType[] mArgumentTypes;

    public RecordMeta(int id, Class<?> clazz, ArgumentType[] argumentTypes) {
        mId = id;
        mClazz = clazz;
        mArgumentTypes = argumentTypes;
    }

    public int getId() {
        return mId;
    }

    public ArgumentType[] getArgumentTypes() {
        return mArgumentTypes;
    }

    public Class<?> getClazz() {
        return mClazz;
    }

    public static class ArgumentType {
        private String mName;
        private Class<?> mType;
        private Object mDefaultValue;

        public ArgumentType(String name,  Class<?> type, Object defaultValue){
            mName = name;
            mType = type;
            mDefaultValue = defaultValue;
        }

        public String getName() {
            return mName;
        }

        public Class<?> getType() {
            return mType;
        }

        public Object getDefaultValue() {
            return mDefaultValue;
        }
    }
}
