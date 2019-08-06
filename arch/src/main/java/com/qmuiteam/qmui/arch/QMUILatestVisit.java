package com.qmuiteam.qmui.arch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;

import com.qmuiteam.qmui.arch.record.DefaultLatestVisitStorage;
import com.qmuiteam.qmui.arch.record.LatestVisitArgumentSaver;
import com.qmuiteam.qmui.arch.record.QMUILatestVisitStorage;
import com.qmuiteam.qmui.arch.record.RecordInfo;
import com.qmuiteam.qmui.arch.record.RecordMeta;
import com.qmuiteam.qmui.arch.record.RecordMetaMap;

import java.util.ArrayList;
import java.util.List;

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

    @SuppressWarnings("unchecked")
    private Intent getLatestVisitIntent(Context context) {
        int activityId = getStorage().getActivityRecordId();
        if (activityId == QMUILatestVisitStorage.NOT_EXIST) {
            return null;
        }
        RecordMeta activityMeta = mRecordMap.getRecordMetaById(activityId);
        if (activityMeta == null) {
            return null;
        }
        Class<?> activityCls = activityMeta.getClazz();
        Intent intent;
        if (QMUIFragmentActivity.class.isAssignableFrom(activityCls)) {
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
            if (type == Integer.TYPE || type == Integer.class) {
                Integer value = storage.getActivityIntArgument(name);
                if (value != null) {
                    intent.putExtra(name, value);
                }
            } else if (type == Boolean.TYPE || type == Boolean.class) {
                Boolean value = storage.getActivityBoolArgument(name);
                if (value != null) {
                    intent.putExtra(name, value);
                }
            } else if (type == Long.TYPE || type == Long.class) {
                Long value = storage.getActivityLongArgument(name);
                if (value != null) {
                    intent.putExtra(name, value);
                }
            } else if (type == Float.TYPE || type == Float.class) {
                Float value = storage.getActivityFloatArgument(name);
                if (value != null) {
                    intent.putExtra(name, value);
                }
            } else if (type == String.class) {
                String value = storage.getActivityStringArgument(name);
                if (value != null) {
                    intent.putExtra(name, value);
                }
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
            if (type == Integer.TYPE || type == Integer.class) {
                Integer value = storage.getFragmentIntArgument(name);
                if (value != null) {
                    bundle.putInt(name, value);
                }

            } else if (type == Boolean.TYPE || type == Boolean.class) {
                Boolean value = storage.getFragmentBoolArgument(name);
                if (value != null) {
                    bundle.putBoolean(name, value);
                }
            } else if (type == Long.TYPE || type == Long.class) {
                Long value = storage.getFragmentLongArgument(name);
                if (value != null) {
                    bundle.putLong(name, value);
                }
            } else if (type == Float.TYPE || type == Float.class) {
                Float value = storage.getFragmentFloatArgument(name);
                if (value != null) {
                    bundle.putFloat(name, value);
                }
            } else if (type == String.class) {
                String value = storage.getFragmentStringArgument(name);
                if (value != null) {
                    bundle.putString(name, value);
                }
            }
        }
        return bundle;
    }

    void clearFragmentLatestVisitRecord(){
        getStorage().clearFragmentStorage();
    }

    void clearActivityLatestVisitRecord(){
        getStorage().clearActivityStorage();
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
            List<RecordInfo.Argument> arguments = new ArrayList<>();
            for (RecordMeta.ArgumentType argMeta : argumentTypes) {
                String argName = argMeta.getName();
                Class<?> argMetaType = argMeta.getType();
                Object argValue = argumentSaver.getArgumentValueForLatestVisit(argName);
                if (argValue == null) {
                    continue;
                }

                // compatibility type conversion
                if (argMetaType == Long.TYPE || argMeta.getType() == Long.class) {
                    if (argValue instanceof Integer) {
                        argValue = ((Integer) argValue).longValue();
                    }
                } else if (argMetaType == Float.TYPE || argMeta.getType() == Float.class) {
                    if (argValue instanceof Double) {
                        argValue = ((Double) argValue).floatValue();
                    } else if (argValue instanceof Integer) {
                        argValue = ((Integer) argValue).floatValue();
                    }
                }

                if (argValue.getClass() != argMetaType) {
                    throw new RuntimeException(String.format("The argument value type(%s) for %s " +
                                    "not match the type provided by annotation(%s).",
                            argValue.getClass().getSimpleName(), argName, argMetaType.getSimpleName()));
                }
                RecordInfo.Argument argument = new RecordInfo.Argument(argName, argMetaType, argValue);
                arguments.add(argument);
            }
            recordInfo = new RecordInfo(meta.getId(), cls,
                    arguments.toArray(new RecordInfo.Argument[0]));
        }
        return recordInfo;
    }
}
