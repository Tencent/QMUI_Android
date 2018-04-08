package com.qmuiteam.qmuidemo.manager;

import android.app.Activity;
import android.content.Context;

/**
 * Created by cgspine on 2018/1/14.
 */

public class QDUpgradeManager {
    public static final int INVALIDATE_VERSION_CODE = -1;

    public static final int VERSION_1_1_0 = 110;
    public static final int VERSION_1_1_1 = 111;
    private static final int sCurrentVersion = VERSION_1_1_1;
    private static QDUpgradeManager sQDUpgradeManager = null;
    private UpgradeTipTask mUpgradeTipTask;

    private Context mContext;

    private QDUpgradeManager(Context context) {
        mContext = context.getApplicationContext();
    }

    public static final QDUpgradeManager getInstance(Context context) {
        if (sQDUpgradeManager == null) {
            sQDUpgradeManager = new QDUpgradeManager(context);
        }
        return sQDUpgradeManager;
    }

    public void check() {
        int oldVersion = QDPreferenceManager.getInstance(mContext).getVersionCode();
        int currentVersion = sCurrentVersion;
        if (currentVersion > oldVersion) {
            if (oldVersion == INVALIDATE_VERSION_CODE) {
                onNewInstall(currentVersion);
            } else {
                onUpgrade(oldVersion, currentVersion);
            }
            QDPreferenceManager.getInstance(mContext).setAppVersionCode(currentVersion);
        }
    }

    private void onUpgrade(int oldVersion, int currentVersion) {
        mUpgradeTipTask = new UpgradeTipTask(oldVersion, currentVersion);
    }

    private void onNewInstall(int currentVersion) {
        mUpgradeTipTask = new UpgradeTipTask(INVALIDATE_VERSION_CODE, currentVersion);
    }

    public void runUpgradeTipTaskIfExist(Activity activity) {
        if (mUpgradeTipTask != null) {
            mUpgradeTipTask.upgrade(activity);
            mUpgradeTipTask = null;
        }
    }
}
