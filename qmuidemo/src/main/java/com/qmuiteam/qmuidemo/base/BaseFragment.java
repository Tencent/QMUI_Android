package com.qmuiteam.qmuidemo.base;

import com.qmuiteam.qmui.arch.QMUIFragment;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIPackageHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.manager.QDPreferenceManager;
import com.qmuiteam.qmuidemo.manager.QDUpgradeManager;

/**
 * Created by cgspine on 2018/1/7.
 */

public abstract class BaseFragment extends QMUIFragment {


    public BaseFragment() {
    }

    @Override
    protected int backViewInitOffset() {
        return QMUIDisplayHelper.dp2px(getContext(), 100);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAndShowUpgradeTip();

    }

    private void checkAndShowUpgradeTip() {
        QDPreferenceManager preferenceManager = QDPreferenceManager.getInstance(getContext());
        if (preferenceManager.isNeedShowUpgradeTip()) {
            preferenceManager.setNeedShowUpgradeTip(false);
            String title = String.format(getString(R.string.app_upgrade_tip_title), QMUIPackageHelper.getAppVersion(getContext()));
            CharSequence message = QDUpgradeManager.getInstance(getContext()).getUpgradeWord(getActivity());
            new QMUIDialog.MessageDialogBuilder(getContext())
                    .setTitle(title)
                    .setMessage(message)
                    .create(R.style.ReleaseDialogTheme)
                    .show();
        }
    }
}
