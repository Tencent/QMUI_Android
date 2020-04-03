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

package com.qmuiteam.qmuidemo.fragment.components;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.qmuiteam.qmui.widget.QMUIRadiusImageView2;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;

import butterknife.BindView;
import butterknife.ButterKnife;


@Widget(name = "QMUIRadiusImageView2 ScaleType")
public class QDRadiusImageView2ScaleTypeFragment extends BaseFragment {

    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.image_1)
    QMUIRadiusImageView2 mRadius1ImageView;
    @BindView(R.id.image_2)
    QMUIRadiusImageView2 mRadius2ImageView;

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_radius_imageview2_scale_type, null);
        ButterKnife.bind(this, root);
        initTopBar();

        return root;
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        mTopBar.setTitle(QDDataManager.getInstance().getName(this.getClass()));

        // 动态修改效果按钮
        mTopBar.addRightImageButton(R.mipmap.icon_topbar_overflow, R.id.topbar_right_change_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showBottomSheetList();
                    }
                });
    }

    private void showBottomSheetList() {
        new QMUIBottomSheet.BottomListSheetBuilder(getActivity())
                .addItem("CENTER")
                .addItem("CENTER_INSIDE")
                .addItem("CENTER_CROP")
                .addItem("FIT_CENTER")
                .addItem("FIT_XY")
                .addItem("FIT_START")
                .addItem("FIT_END")
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        dialog.dismiss();
                        switch (position) {
                            case 0:
                                mRadius1ImageView.setScaleType(ImageView.ScaleType.CENTER);
                                mRadius2ImageView.setScaleType(ImageView.ScaleType.CENTER);
                                break;
                            case 1:
                                mRadius1ImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                mRadius2ImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                break;
                            case 2:
                                mRadius1ImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                mRadius2ImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                break;
                            case 3:
                                mRadius1ImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                mRadius2ImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                break;
                            case 4:
                                mRadius1ImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                mRadius2ImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                break;
                            case 5:
                                mRadius1ImageView.setScaleType(ImageView.ScaleType.FIT_START);
                                mRadius2ImageView.setScaleType(ImageView.ScaleType.FIT_START);
                                break;
                            case 6:
                                mRadius1ImageView.setScaleType(ImageView.ScaleType.FIT_END);
                                mRadius2ImageView.setScaleType(ImageView.ScaleType.FIT_END);
                            default:
                                break;
                        }
                    }
                })
                .build()
                .show();
    }
}
