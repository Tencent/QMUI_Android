package com.qmuiteam.qmuidemo.base;

import android.annotation.SuppressLint;

import com.qmuiteam.qmui.arch.QMUIActivity;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmuidemo.manager.QDUpgradeManager;

import static com.qmuiteam.qmuidemo.QDApplication.getContext;

@SuppressLint("Registered")
public class BaseActivity extends QMUIActivity {

    @Override
    protected int backViewInitOffset() {
        return QMUIDisplayHelper.dp2px(getContext(), 100);
    }

    @Override
    public void onResume() {
        super.onResume();
        QDUpgradeManager.getInstance(getContext()).runUpgradeTipTaskIfExist(this);

    }
}
