package com.qmuiteam.qmui.arch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.fragment.app.Fragment;

import com.qmuiteam.qmui.QMUILog;
import com.qmuiteam.qmui.arch.record.DefaultLatestVisitStorage;
import com.qmuiteam.qmui.arch.record.QMUILatestVisitStorage;
import com.qmuiteam.qmui.arch.record.RecordArgumentEditor;
import com.qmuiteam.qmui.arch.record.RecordArgumentEditorImpl;
import com.qmuiteam.qmui.arch.record.RecordIdClassMap;

import java.util.Map;

public class QMUILatestVisit {
    private static final String TAG = "QMUILatestVisit";
    private static String NAV_STORE_PREFIX = "_qmui_nav";
    private static String NAV_STORE_FRAGMENT_SUFFIX = ".class";
    private static QMUILatestVisit sInstance;
    private QMUILatestVisitStorage mStorage;
    private Context mContext;
    private RecordIdClassMap mRecordMap;
    private RecordArgumentEditor mRecordArgumentEditor;
    private RecordArgumentEditor mNavRecordArgumentEditor;

    private QMUILatestVisit(Context context) {
        mContext = context.getApplicationContext();
        mRecordArgumentEditor = new RecordArgumentEditorImpl();
        mNavRecordArgumentEditor = new RecordArgumentEditorImpl();
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
                Map<String, RecordArgumentEditor.Argument> arguments = getStorage().getFragmentArguments();
                if (arguments == null || arguments.isEmpty()) {
                    intent = QMUIFragmentActivity.intentOf(context, activity, fragment, null);
                } else {
                    Bundle bundle = new Bundle();
                    boolean hasNav = false;
                    for (String key : arguments.keySet()) {
                        if (key.startsWith(NAV_STORE_PREFIX)) {
                            hasNav = true;
                        } else {
                            RecordArgumentEditor.Argument argument = arguments.get(key);
                            if (argument != null) {
                                argument.putToBundle(bundle, key);
                            }
                        }
                    }
                    if (!hasNav) {
                        intent = QMUIFragmentActivity.intentOf(context, activity, fragment, bundle);
                    } else {
                        int navLevel = 0;
                        String fragmentClassName = fragment.getName();
                        while (true) {
                            String navPrefix = getNavFragmentStorePrefix(navLevel);
                            String navClassNameKey = navPrefix + NAV_STORE_FRAGMENT_SUFFIX;
                            RecordArgumentEditor.Argument navClassNameArg = arguments.get(navClassNameKey);
                            if (navClassNameArg == null) {
                                break;
                            }
                            bundle = QMUINavFragment.initArguments(fragmentClassName, bundle);
                            fragmentClassName = (String) navClassNameArg.getValue();
                            for (String key : arguments.keySet()) {
                                if (key.startsWith(navPrefix) && !key.equals(navClassNameKey)) {
                                    RecordArgumentEditor.Argument arg = arguments.get(key);
                                    if (arg != null) {
                                        arg.putToBundle(bundle, key.substring(navPrefix.length()));
                                    }
                                }
                            }
                            navLevel++;
                        }
                        intent = QMUIFragmentActivity.intentOf(context, activity, fragmentClassName, bundle);
                    }
                }
            } else {
                intent = new Intent(context, activityCls);
            }
            getStorage().getAndWriteActivityArgumentsToIntent(intent);
            return intent;
        } catch (Throwable throwable) {
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
        mNavRecordArgumentEditor.clear();
        fragment.onCollectLatestVisitArgument(mRecordArgumentEditor);
        Fragment parent = fragment.getParentFragment();
        int level = 0;
        while (parent instanceof QMUINavFragment) {
            String navInfo = getNavFragmentStorePrefix(level);
            QMUINavFragment nav = (QMUINavFragment) parent;
            mNavRecordArgumentEditor.clear();
            nav.onCollectLatestVisitArgument(mNavRecordArgumentEditor);
            Map<String, RecordArgumentEditor.Argument> args = mNavRecordArgumentEditor.getAll();
            mRecordArgumentEditor.putString(navInfo + NAV_STORE_FRAGMENT_SUFFIX, nav.getClass().getName());
            for (String arg : args.keySet()) {
                mRecordArgumentEditor.put(navInfo + arg, args.get(arg));
            }
            parent = parent.getParentFragment();
            level++;
        }
        getStorage().saveFragmentRecordInfo(id, mRecordArgumentEditor.getAll());
        mRecordArgumentEditor.clear();
        mNavRecordArgumentEditor.clear();
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


    private String getNavFragmentStorePrefix(int level) {
        return NAV_STORE_PREFIX + level + "_";
    }
}
