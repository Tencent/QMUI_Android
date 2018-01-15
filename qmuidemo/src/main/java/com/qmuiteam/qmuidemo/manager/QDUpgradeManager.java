package com.qmuiteam.qmuidemo.manager;

import android.content.Context;

/**
 * Created by cgspine on 2018/1/14.
 */

public class QDUpgradeManager {
    public static final int INVALIDATE_VERSION_CODE = -1;

    private static final int VERSION_1_0_7 = 107;

    private static final int sCurrentVersion = VERSION_1_0_7;
    private static QDUpgradeManager sQDUpgradeManager = null;

    private Context mContext;

    private QDUpgradeManager(Context context){
        mContext = context.getApplicationContext();
    }

    public static final QDUpgradeManager getInstance(Context context){
        if(sQDUpgradeManager == null){
            sQDUpgradeManager = new QDUpgradeManager(context);
        }
        return sQDUpgradeManager;
    }

    public void check(){
        int oldVersion = QDPreferenceManager.getInstance(mContext).getVersionCode();
        int currentVersion = sCurrentVersion;
        if(currentVersion > oldVersion){
            if(oldVersion == INVALIDATE_VERSION_CODE){
                onNewInstall(currentVersion);
            }else{
                onUpgrade(oldVersion, currentVersion);
            }
            QDPreferenceManager.getInstance(mContext).setAppVersionCode(currentVersion);
        }
    }

    private void onUpgrade(int oldVersion, int currentVersion){
        QDPreferenceManager.getInstance(mContext).setNeedShowUpgradeTip(true);
    }

    private void onNewInstall(int currentVersion){
        // 并无法判断是 1.0.7 版本之前升级上来的，还是新装的
        QDPreferenceManager.getInstance(mContext).setNeedShowUpgradeTip(true);
    }
}
