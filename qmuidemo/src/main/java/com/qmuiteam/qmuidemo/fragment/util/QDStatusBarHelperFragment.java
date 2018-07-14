package com.qmuiteam.qmuidemo.fragment.util;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.activity.TranslucentActivity;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.Group;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.model.QDItemDescription;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * {@link QMUIStatusBarHelper} 的使用示例。
 * Created by Kayo on 2016/12/12.
 */

@Widget(group = Group.Helper, widgetClass = QMUIStatusBarHelper.class, iconRes = R.mipmap.icon_grid_status_bar_helper)
public class QDStatusBarHelperFragment extends BaseFragment {

    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.groupListView)
    QMUIGroupListView mGroupListView;

    private QDItemDescription mQDItemDescription;

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_grouplistview, null);
        ButterKnife.bind(this, root);

        mQDItemDescription = QDDataManager.getInstance().getDescription(this.getClass());
        initTopBar();

        initGroupListView();

        return root;
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QMUIStatusBarHelper.setStatusBarDarkMode(getBaseFragmentActivity()); // 退出界面之前把状态栏还原为白色字体与图标
                popBackStack();
            }
        });

        mTopBar.setTitle(mQDItemDescription.getName());
    }

    private void initGroupListView() {

        QMUIGroupListView.newSection(getContext())
                .setDescription("支持 4.4 以上版本的 MIUI 和 Flyme，以及 5.0 以上版本的其他 Android")
                .addItemView(mGroupListView.createItemView("沉浸式状态栏"), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intentTranslucent = TranslucentActivity.createActivity(getContext(), true);
                        startActivity(intentTranslucent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_still);
                    }
                })
                .addTo(mGroupListView);

        QMUIGroupListView.newSection(getContext())
                .setDescription("支持 4.4 以上版本 MIUI 和 Flyme，以及 6.0 以上版本的其他 Android")
                .addItemView(mGroupListView.createItemView("设置状态栏黑色字体与图标"), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QMUIStatusBarHelper.setStatusBarLightMode(getBaseFragmentActivity());
                    }
                })
                .addItemView(mGroupListView.createItemView("设置状态栏白色字体与图标"), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QMUIStatusBarHelper.setStatusBarDarkMode(getBaseFragmentActivity());
                    }
                })
                .addTo(mGroupListView);

        QMUIGroupListView.newSection(getContext())
                .setDescription("不同机型下状态栏高度可能略有差异，并不是固定值，可以通过这个方法获取实际高度")
                .addItemView(mGroupListView.createItemView("获取状态栏的实际高度"), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String result = String.format(getResources().getString(R.string.statusBarHelper_statusBar_height_result), QMUIStatusBarHelper.getStatusbarHeight(getContext()));
                        final QMUITipDialog tipDialog = new QMUITipDialog.Builder(getContext()).setTipWord(result).create();
                        tipDialog.show();
                        mGroupListView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tipDialog.dismiss();
                            }
                        }, 1500);
                    }
                })
                .addTo(mGroupListView);

    }
}
