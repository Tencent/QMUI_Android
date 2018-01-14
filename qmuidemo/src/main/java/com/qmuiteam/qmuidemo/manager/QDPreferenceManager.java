package com.qmuiteam.qmuidemo.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by cgspine on 2018/1/14.
 */

public class QDPreferenceManager {
    private static SharedPreferences sPreferences;
    private static QDPreferenceManager sQDPerferenceManager = null;

    private static final String APP_VERSION_CODE = "app_version_code";
    private static final String APP_NNED_SHOW_UPGRADE_TIP = "app_has_show_upgrade_tip";

    private QDPreferenceManager(Context context) {
        sPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public static final QDPreferenceManager getInstance(Context context) {
        if (sQDPerferenceManager == null) {
            sQDPerferenceManager = new QDPreferenceManager(context);
        }
        return sQDPerferenceManager;
    }

    public void setAppVersionCode(int code) {
        final SharedPreferences.Editor editor = sPreferences.edit();
        editor.putInt(APP_VERSION_CODE, code);
        editor.apply();
    }

    public int getVersionCode() {
        return sPreferences.getInt(APP_VERSION_CODE, QDUpgradeManager.INVALIDATE_VERSION_CODE);
    }

    public void setNeedShowUpgradeTip(boolean needShowUpgradeTip) {
        final SharedPreferences.Editor editor = sPreferences.edit();
        editor.putBoolean(APP_NNED_SHOW_UPGRADE_TIP, needShowUpgradeTip);
        editor.apply();
    }

    public boolean isNeedShowUpgradeTip() {
        return sPreferences.getBoolean(APP_NNED_SHOW_UPGRADE_TIP, false);
    }
}
