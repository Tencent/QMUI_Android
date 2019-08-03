package com.qmuiteam.qmui.arch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.qmuiteam.qmui.arch.record.DefaultLatestVisitStorage;
import com.qmuiteam.qmui.arch.record.LatestVisitArgumentSaver;
import com.qmuiteam.qmui.arch.record.QMUILatestVisitStorage;
import com.qmuiteam.qmui.arch.record.RecordInfo;
import com.qmuiteam.qmui.arch.record.RecordMeta;
import com.qmuiteam.qmui.arch.record.RecordMetaMap;

import androidx.annotation.MainThread;

public class QMUILatestVisit {
    private static QMUILatestVisit sInstance;
    private QMUILatestVisitStorage mStorage;
    private Context mContext;
    private RecordMetaMap mRecordMap;

    private QMUILatestVisit(Context context) {
        mContext = context.getApplicationContext();
        try {
            Class<?> cls = Class.forName(RecordMetaMap.class.getCanonicalName() + "Impl");
            mRecordMap = (RecordMetaMap) cls.newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Not find the Class RecordMetaMapImpl. " +
                    "Did you add dependency of arch-compiler?");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can not access the Class RecordMetaMapImpl. " +
                    "Please file a issue to report this.");
        } catch (InstantiationException e) {
            throw new RuntimeException("Can not instance the Class RecordMetaMapImpl. " +
                    "Please file a issue to report this.");
        }
    }

    @MainThread
    public static QMUILatestVisit getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new QMUILatestVisit(context);
        }
        return sInstance;
    }

    public static Intent intentOfLatestVisit(Activity activity) {
        return getInstance(activity).getLatestVisitIntent(activity);
    }

    public void setStorage(QMUILatestVisitStorage storage) {
        mStorage = storage;
    }

    QMUILatestVisitStorage getStorage() {
        if (mStorage == null) {
            mStorage = new DefaultLatestVisitStorage(mContext);
        }
        return mStorage;
    }

    public RecordMeta getRecordMetaById(int id) {
        return mRecordMap.getRecordMetaById(id);
    }

    @SuppressWarnings("unchecked")
    private Intent getLatestVisitIntent(Context context) {
        int activityId = getStorage().getActivityRecordId();
        if (activityId == QMUILatestVisitStorage.NOT_EXIST) {
            Log.i("cgine", "1");
            return null;
        }
        RecordMeta activityMeta = mRecordMap.getRecordMetaById(activityId);
        if (activityMeta == null) {
            Log.i("cgine", "2");
            return null;
        }
        Class<?> activityCls = activityMeta.getClazz();
        Intent intent;
        if (QMUIFragmentActivity.class.isAssignableFrom(activityCls)) {
            Log.i("cgine", "3");
            int fragmentId = getStorage().getFragmentRecordId();
            if (fragmentId == QMUILatestVisitStorage.NOT_EXIST) {
                return null;
            }

            RecordMeta fragmentMeta = mRecordMap.getRecordMetaById(fragmentId);
            if (fragmentMeta == null) {
                return null;
            }
            Class<? extends QMUIFragmentActivity> activity = (Class<? extends QMUIFragmentActivity>) activityCls;
            Class<? extends QMUIFragment> fragment = (Class<? extends QMUIFragment>) fragmentMeta.getClazz();
            Bundle bundle = populateFragmentArgument(fragmentMeta.getArgumentTypes());
            intent = QMUIFragmentActivity.intentOf(context, activity, fragment, bundle);
        } else {
            intent = new Intent(context, activityMeta.getClazz());
        }
        populateActivityArgument(intent, activityMeta.getArgumentTypes());
        return intent;
    }

    private void populateActivityArgument(Intent intent, RecordMeta.ArgumentType[] argumentTypes) {
        if (argumentTypes == null || argumentTypes.length == 0) {
            return;
        }
        QMUILatestVisitStorage storage = getStorage();
        for (RecordMeta.ArgumentType argumentMeta : argumentTypes) {
            String name = argumentMeta.getName();
            Class<?> type = argumentMeta.getType();
            Object defaultValue = argumentMeta.getDefaultValue();
            if (type == Integer.TYPE || type == Integer.class) {
                intent.putExtra(name, storage.getActivityIntArgument(name, (Integer) defaultValue));
            } else if (type == Boolean.TYPE || type == Boolean.class) {
                intent.putExtra(name, storage.getActivityBoolArgument(name, (Boolean) defaultValue));
            } else if (type == Long.TYPE || type == Long.class) {
                intent.putExtra(name, storage.getActivityLongArgument(name, (Long) defaultValue));
            } else if (type == Float.TYPE || type == Float.class) {
                intent.putExtra(name, storage.getActivityFloatArgument(name, (Float) defaultValue));
            } else if (type == String.class) {
                intent.putExtra(name, storage.getActivityStringArgument(name, (String) defaultValue));
            }
        }
    }

    private Bundle populateFragmentArgument(RecordMeta.ArgumentType[] argumentTypes) {
        if (argumentTypes == null || argumentTypes.length == 0) {
            return null;
        }
        Bundle bundle = new Bundle();
        QMUILatestVisitStorage storage = getStorage();
        for (RecordMeta.ArgumentType argumentMeta : argumentTypes) {
            String name = argumentMeta.getName();
            Class<?> type = argumentMeta.getType();
            Object defaultValue = argumentMeta.getDefaultValue();
            if (type == Integer.TYPE || type == Integer.class) {
                bundle.putInt(name, storage.getFragmentIntArgument(name, (Integer) defaultValue));
            } else if (type == Boolean.TYPE || type == Boolean.class) {
                bundle.putBoolean(name, storage.getFragmentBoolArgument(name, (Boolean) defaultValue));
            } else if (type == Long.TYPE || type == Long.class) {
                bundle.putLong(name, storage.getFragmentLongArgument(name, (Long) defaultValue));
            } else if (type == Float.TYPE || type == Float.class) {
                bundle.putFloat(name, storage.getFragmentFloatArgument(name, (Float) defaultValue));
            } else if (type == String.class) {
                bundle.putString(name, storage.getFragmentStringArgument(name, (String) defaultValue));
            }
        }
        return bundle;
    }

    void performLatestVisitRecord(QMUIFragment fragment) {
        getStorage().saveFragmentRecordInfo(getRecordInfo(fragment.getClass(), fragment));
    }

    void performLatestVisitRecord(InnerBaseActivity activity) {
        getStorage().saveActivityRecordInfo(getRecordInfo(activity.getClass(), activity));
    }

    private RecordInfo getRecordInfo(Class<?> cls, LatestVisitArgumentSaver argumentSaver) {
        RecordMeta meta = mRecordMap.getRecordMetaByClass(cls);
        if (meta == null) {
            throw new RuntimeException(String.format(
                    "arch-compiler generate code for %s failed", cls.getSimpleName()));
        }
        RecordMeta.ArgumentType[] argumentTypes = meta.getArgumentTypes();
        RecordInfo recordInfo;
        if (argumentTypes == null || argumentTypes.length == 0) {
            recordInfo = new RecordInfo(meta.getId(), cls, null);
        } else {
            RecordInfo.Argument[] arguments = new RecordInfo.Argument[argumentTypes.length];
            for (int i = 0; i < arguments.length; i++) {
                RecordMeta.ArgumentType argMeta = argumentTypes[i];
                String argName = argMeta.getName();
                Class<?> argMetaType = argMeta.getType();
                Object argValue = argumentSaver.getArgumentValueForLatestVisit(argName);
                if(argValue instanceof Double){
                    argValue = ((Double) argValue).floatValue();
                }
                if (argValue == null) {
                    argValue = argMeta.getDefaultValue();
                } else if (argValue.getClass() != argMetaType) {
                    throw new RuntimeException(String.format("The argument value type(%s) for %s " +
                                    "not match the type provided by annotation(%s).",
                            argValue.getClass().getSimpleName(), argName, argMetaType.getSimpleName()));
                }
                RecordInfo.Argument argument = new RecordInfo.Argument(argName, argMetaType, argValue);
                arguments[i] = argument;
            }
            recordInfo = new RecordInfo(meta.getId(), cls, arguments);
        }
        return recordInfo;
    }
}
