package com.qmuiteam.qmui.arch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;

import com.qmuiteam.qmui.QMUILog;
import com.qmuiteam.qmui.arch.record.DefaultLatestVisitStorage;
import com.qmuiteam.qmui.arch.record.QMUILatestVisitStorage;
import com.qmuiteam.qmui.arch.record.RecordArgumentEditor;
import com.qmuiteam.qmui.arch.record.RecordArgumentEditorImpl;
import com.qmuiteam.qmui.arch.record.RecordIdClassMap;

public class QMUILatestVisit {
    private static final String TAG = "QMUILatestVisit";
    private static QMUILatestVisit sInstance;
    private QMUILatestVisitStorage mStorage;
    private Context mContext;
    private RecordIdClassMap mRecordMap;
    private RecordArgumentEditor mRecordArgumentEditor;

    private QMUILatestVisit(Context context) {
        mContext = context.getApplicationContext();
        mRecordArgumentEditor = new RecordArgumentEditorImpl();
        try {
            Class<?> cls = Class.forName(RecordIdClassMap.class.getCanonicalName() + "Impl");
            mRecordMap = (RecordIdClassMap) cls.newInstance();
        } catch (ClassNotFoundException e) {
            mRecordMap = new RecordIdClassMap() {
                @Override
                public Class<?> getRecordClassById(int id) {
                    return null;
                }

                @Override
                public int getIdByRecordClass(Class<?> clazz) {
                    return QMUILatestVisitStorage.NOT_EXIST;
                }
            };
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
        Class<?> activityCls = mRecordMap.getRecordClassById(activityId);
        if (activityCls == null) {
            return null;
        }
        Intent intent;
        try {
            if (QMUIFragmentActivity.class.isAssignableFrom(activityCls)) {
                int fragmentId = getStorage().getFragmentRecordId();
                if (fragmentId == QMUILatestVisitStorage.NOT_EXIST) {
                    return null;
                }

                Class<?> fragmentCls = mRecordMap.getRecordClassById(fragmentId);
                if (fragmentCls == null) {
                    return null;
                }
                Class<? extends QMUIFragmentActivity> activity = (Class<? extends QMUIFragmentActivity>) activityCls;
                Class<? extends QMUIFragment> fragment = (Class<? extends QMUIFragment>) fragmentCls;
                Bundle bundle = new Bundle();
                getStorage().getAndWriteFragmentArgumentsToBundle(bundle);
                intent = QMUIFragmentActivity.intentOf(context, activity, fragment, bundle);
            } else {
                intent = new Intent(context, activityCls);
            }
            getStorage().getAndWriteActivityArgumentsToIntent(intent);
            return intent;
        }catch (Throwable throwable){
            QMUILog.e(TAG, "getLatestVisitIntent failed.", throwable);
            getStorage().clearAll();
        }
        return null;
    }


    void clearFragmentLatestVisitRecord() {
        getStorage().clearFragmentStorage();
    }

    void clearActivityLatestVisitRecord() {
        getStorage().clearActivityStorage();
    }

    void performLatestVisitRecord(QMUIFragment fragment) {
        int id = mRecordMap.getIdByRecordClass(fragment.getClass());
        if (id == QMUILatestVisitStorage.NOT_EXIST) {
            return;
        }
        mRecordArgumentEditor.clear();
        fragment.onCollectLatestVisitArgument(mRecordArgumentEditor);
        getStorage().saveFragmentRecordInfo(id, mRecordArgumentEditor.getAll());
        mRecordArgumentEditor.clear();
    }

    void performLatestVisitRecord(InnerBaseActivity activity) {
        int id = mRecordMap.getIdByRecordClass(activity.getClass());
        if (id == QMUILatestVisitStorage.NOT_EXIST) {
            return;
        }
        mRecordArgumentEditor.clear();
        activity.onCollectLatestVisitArgument(mRecordArgumentEditor);
        getStorage().saveActivityRecordInfo(id, mRecordArgumentEditor.getAll());
        mRecordArgumentEditor.clear();
    }
}
