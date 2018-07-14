package com.qmuiteam.qmuidemo.fragment.components;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUICenterGravityRefreshOffsetCalculator;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIDefaultRefreshOffsetCalculator;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIFollowRefreshOffsetCalculator;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.model.QDItemDescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author cginechen
 * @date 2016-12-14
 */

@Widget(widgetClass = QMUIPullRefreshLayout.class, iconRes = R.mipmap.icon_grid_pull_refresh_layout)
public class QDPullRefreshFragment extends BaseFragment {
    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.pull_to_refresh)
    QMUIPullRefreshLayout mPullRefreshLayout;
    @BindView(R.id.listview)
    ListView mListView;

    private QDItemDescription mQDItemDescription;

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_pull_refresh_listview, null);
        ButterKnife.bind(this, root);

        QDDataManager QDDataManager = com.qmuiteam.qmuidemo.manager.QDDataManager.getInstance();
        mQDItemDescription = QDDataManager.getDescription(this.getClass());
        initTopBar();
        initData();

        return root;
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        mTopBar.setTitle(mQDItemDescription.getName());

        // 切换其他情况的按钮
        mTopBar.addRightImageButton(R.mipmap.icon_topbar_overflow, R.id.topbar_right_change_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetList();
            }
        });
    }

    private void initData() {
        List<String> data = new ArrayList<>(Arrays.asList("Helps", "Maintain", "Liver", "Health", "Function", "Supports", "Healthy", "Fat",
                "Metabolism", "Nuturally", "Bracket", "Refrigerator", "Bathtub", "Wardrobe", "Comb", "Apron", "Carpet", "Bolster", "Pillow", "Cushion"));
        mListView.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, data));
        mPullRefreshLayout.setOnPullListener(new QMUIPullRefreshLayout.OnPullListener() {
            @Override
            public void onMoveTarget(int offset) {

            }

            @Override
            public void onMoveRefreshView(int offset) {

            }

            @Override
            public void onRefresh() {
                mPullRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullRefreshLayout.finishRefresh();
                    }
                }, 2000);
            }
        });
    }

    private void showBottomSheetList() {
        new QMUIBottomSheet.BottomListSheetBuilder(getActivity())
                .addItem(getResources().getString(R.string.pull_refresh_default_offset_calculator))
                .addItem(getResources().getString(R.string.pull_refresh_follow_offset_calculator))
                .addItem(getResources().getString(R.string.pull_refresh_center_gravity_offset_calculator))
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        dialog.dismiss();
                        switch (position) {
                            case 0:
                                mPullRefreshLayout.setRefreshOffsetCalculator(new QMUIDefaultRefreshOffsetCalculator());
                                break;
                            case 1:
                                mPullRefreshLayout.setRefreshOffsetCalculator(new QMUIFollowRefreshOffsetCalculator());
                                break;
                            case 2:
                                mPullRefreshLayout.setRefreshOffsetCalculator(new QMUICenterGravityRefreshOffsetCalculator());
                                break;
                            default:
                                break;
                        }
                    }
                })
                .build()
                .show();
    }
}
