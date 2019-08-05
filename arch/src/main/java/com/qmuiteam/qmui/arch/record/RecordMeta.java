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

        public ArgumentType(String name,  Class<?> type){
            mName = name;
            mType = type;
        }

        public String getName() {
            return mName;
        }

        public Class<?> getType() {
            return mType;
        }
    }
}
