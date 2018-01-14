package com.qmuiteam.qmuidemo.base;

import com.qmuiteam.qmui.arch.QMUIFragment;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIPackageHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.manager.QDPreferenceManager;

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
            String message = "1. 分离出 arch 模块，用于 fragment 管理，支持手势返回\n" +
                    "2. 整理 QMUITopbar 的 theme，能够对 QMUITopbar 做更多的差异化处理\n" +
                    "3. 其它 bugfix: #125、#127、#132、#141";
            new QMUIDialog.MessageDialogBuilder(getContext())
                    .setTitle(title)
                    .setMessage(message)
                    .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog qmuiDialog, int i) {
                            qmuiDialog.dismiss();
                        }
                    })
                    .show();
        }
    }
}
