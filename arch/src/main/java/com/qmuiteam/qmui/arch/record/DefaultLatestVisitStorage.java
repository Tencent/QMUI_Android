package com.qmuiteam.qmui.arch.record;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

public class DefaultLatestVisitStorage implements QMUILatestVisitStorage {

    private static final String SP_NAME = "qmui_latest_visit";
    private static final String SP_FRAGMENT_RECORD_ID = "id_qmui_f_r";
    private static final String SP_ACTIVITY_RECORD_ID = "id_qmui_a_r";
    private static final String SP_ACTIVITY_ARG_PREFIX = "a_qmui_a_";
    private static final String SP_FRAGMENT_ARG_PREFIX = "a_qmui_f_";
    private SharedPreferences sp;
    private RecordInfo mLastFragmentRecord;
    private RecordInfo mLastActivityRecord;

    public DefaultLatestVisitStorage(Context context) {
        sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public int getFragmentRecordId() {
        return sp.getInt(SP_FRAGMENT_RECORD_ID, NOT_EXIST);
    }

    @Override
    public String getFragmentStringArgument(String key, String defValue) {
        return sp.getString(SP_FRAGMENT_ARG_PREFIX + key, defValue);
    }

    @Override
    public int getFragmentIntArgument(String key, int defValue) {
        return sp.getInt(SP_FRAGMENT_ARG_PREFIX + key, defValue);
    }

    @Override
    public long getFragmentLongArgument(String key, long defValue) {
        return sp.getLong(SP_FRAGMENT_ARG_PREFIX + key, defValue);
    }

    @Override
    public float getFragmentFloatArgument(String key, float defValue) {
        return sp.getFloat(SP_FRAGMENT_ARG_PREFIX + key, defValue);
    }

    @Override
    public boolean getFragmentBoolArgument(String key, boolean defValue) {
        return sp.getBoolean(SP_FRAGMENT_ARG_PREFIX + key, defValue);
    }


    @Override
    public int getActivityRecordId() {
        return sp.getInt(SP_ACTIVITY_RECORD_ID, NOT_EXIST);
    }

    @Override
    public String getActivityStringArgument(String key, String defValue) {
        return sp.getString(SP_ACTIVITY_ARG_PREFIX + key, defValue);
    }

    @Override
    public int getActivityIntArgument(String key, int defValue) {
        return sp.getInt(SP_ACTIVITY_ARG_PREFIX + key, defValue);
    }

    @Override
    public long getActivityLongArgument(String key, long defValue) {
        return sp.getLong(SP_ACTIVITY_ARG_PREFIX + key, defValue);
    }

    @Override
    public float getActivityFloatArgument(String key, float defValue) {
        return sp.getFloat(SP_ACTIVITY_ARG_PREFIX + key, defValue);
    }

    @Override
    public boolean getActivityBoolArgument(String key, boolean defValue) {
        return sp.getBoolean(SP_ACTIVITY_ARG_PREFIX + key, defValue);
    }

    @Override
    public void saveFragmentRecordInfo(@NonNull RecordInfo recordInfo) {
        if (mLastFragmentRecord != null && mLastFragmentRecord.equals(recordInfo)) {
            return;
        }
        mLastFragmentRecord = recordInfo;
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(SP_FRAGMENT_RECORD_ID, recordInfo.getId());
        RecordInfo.Argument[] arguments = recordInfo.getArguments();
        putArguments(editor, SP_FRAGMENT_ARG_PREFIX, arguments);
        editor.apply();
    }

    @Override
    public void saveActivityRecordInfo(@NonNull RecordInfo recordInfo) {
        if (mLastActivityRecord != null && mLastActivityRecord.equals(recordInfo)) {
            return;
        }
        mLastActivityRecord = recordInfo;
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(SP_ACTIVITY_RECORD_ID, recordInfo.getId());
        RecordInfo.Argument[] arguments = recordInfo.getArguments();
        putArguments(editor, SP_ACTIVITY_ARG_PREFIX, arguments);
        editor.apply();
    }

    private void putArguments(SharedPreferences.Editor editor,
                              String prefix, RecordInfo.Argument[] arguments) {
        // clear first
        for (String key : sp.getAll().keySet()) {
            if (key.startsWith(prefix)) {
                editor.remove(key);
            }
        }

        if (arguments != null && arguments.length > 0) {
            for (RecordInfo.Argument argument : arguments) {
                String name = prefix + argument.getName();
                Class<?> type = argument.getType();
                Object value = argument.getValue();
                if (type == Integer.TYPE || type == Integer.class) {
                    editor.putInt(name, (Integer) value);
                } else if (type == Boolean.TYPE || type == Boolean.class) {
                    editor.putBoolean(name, (Boolean) value);
                } else if (type == Float.TYPE || type == Float.class) {
                    editor.putFloat(name, (Float) value);
                } else if (type == Long.TYPE || type == Long.class) {
                    editor.putLong(name, (Long) value);
                } else if (type == String.class) {
                    editor.putString(name, (String) value);
                } else {
                    throw new RuntimeException(String.format(
                            "Not support the type: %s", type.getSimpleName()));
                }
            }
        }
    }
}
