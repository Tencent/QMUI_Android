/*
 * Tencent is pleased to support the open source community by making QMUI_Android available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qmuiteam.qmuidemo.manager;

import android.app.Activity;
import android.content.Context;

/**
 * Created by cgspine on 2018/1/14.
 */

public class QDUpgradeManager {
    public static final int INVALIDATE_VERSION_CODE = 0;

    public static final int VERSION_1_1_0 = 110;
    public static final int VERSION_1_1_1 = 111;
    public static final int VERSION_1_1_2 = 112;
    public static final int VERSION_1_1_3 = 113;
    public static final int VERSION_1_1_4 = 114;
    public static final int VERSION_1_1_5 = 115;
    public static final int VERSION_1_1_6 = 116;
    public static final int VERSION_1_1_7 = 117;
    public static final int VERSION_1_1_8 = 118;
    public static final int VERSION_1_1_9 = 119;
    public static final int VERSION_1_1_10 = 1110;
    public static final int VERSION_1_1_11 = 1111;
    public static final int VERSION_1_1_12 = 1112;
    public static final int VERSION_1_2_0 = 120;
    public static final int VERSION_1_3_1 = 131;
    public static final int VERSION_1_4_0 = 140;
    public static final int VERSION_2_0_0_alpha1 = -2001;
    public static final int VERSION_2_0_0_alpha2 = -2002;
    public static final int VERSION_2_0_0_alpha3 = -2003;
    public static final int VERSION_2_0_0_alpha4 = -2004;
    public static final int VERSION_2_0_0_alpha5 = -2005;
    public static final int VERSION_2_0_0_alpha6 = -2006;
    public static final int VERSION_2_0_0_alpha7 = -2007;
    public static final int VERSION_2_0_0_alpha8 = -2008;
    public static final int VERSION_2_0_0_alpha9 = -2009;
    public static final int VERSION_2_0_0_alpha10 = -2010;
    public static final int VERSION_2_0_0_alpha11 = -2011;
    private static final int sCurrentVersion = VERSION_2_0_0_alpha11;
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
        boolean versionUpdated = false;
        if(currentVersion != oldVersion){
            if(currentVersion < 0){
                // alpha release
                if(-currentVersion > oldVersion){
                    versionUpdated = true;
                }
            }else if (currentVersion > oldVersion) {
                versionUpdated = true;
            }
        }

        if(versionUpdated){
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
