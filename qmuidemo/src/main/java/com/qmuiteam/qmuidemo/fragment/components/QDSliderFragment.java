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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.qmuiteam.qmui.arch.annotation.FragmentScheme;
import com.qmuiteam.qmui.widget.QMUISeekBar;
import com.qmuiteam.qmui.widget.QMUISlider;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmuidemo.QDMainActivity;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.model.QDItemDescription;

import butterknife.BindView;
import butterknife.ButterKnife;


@Widget(widgetClass = QMUISlider.class, iconRes = R.mipmap.icon_grid_slider)
@FragmentScheme(
        name = "slider",
        activities = {QDMainActivity.class},
        customMatcher = SliderSchemeMatcher.class
)
public class QDSliderFragment extends BaseFragment implements QMUISlider.Callback {

    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;

    @BindView(R.id.slider)
    QMUISlider mSlider;

    @BindView(R.id.seekBar)
    QMUISeekBar mSeekBar;

    private QDItemDescription mQDItemDescription;

    @Override
    protected View onCreateView() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_slider, null);
        ButterKnife.bind(this, view);
        mQDItemDescription = QDDataManager.getInstance().getDescription(this.getClass());

        initTopBar();
//        QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
//        builder.background(R.attr.qmui_config_color_black);
//        builder.progressColor(R.attr.qmui_config_color_gray_9);
//        QMUISkinHelper.setSkinValue(mSlider, builder);
//        builder.clear();
//        builder.background(R.attr.qmui_config_color_blue);
//        builder.border(R.attr.app_skin_btn_test_border);
//        mSlider.setThumbSkin(builder);
//        builder.clear();
        mSlider.setCallback(this);
        mSeekBar.setCallback(this);

        return view;
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        mTopBar.setTitle(mQDItemDescription.getName());
    }

    @Override
    public void onProgressChange(QMUISlider slider, int progress, int tickCount, boolean fromUser) {
        Log.i("QDSliderFragment", "progress = " + progress + "; fromUser = " + fromUser);
    }

    @Override
    public void onStartMoving(QMUISlider slider, int progress, int tickCount) {

    }

    @Override
    public void onStopMoving(QMUISlider slider, int progress, int tickCount) {

    }

    @Override
    public void onTouchDown(QMUISlider slider, int progress, int tickCount, boolean hitThumb) {

    }

    @Override
    public void onTouchUp(QMUISlider slider, int progress, int tickCount) {

    }

    @Override
    public void onLongTouch(QMUISlider slider, int progress, int tickCount) {

    }
}
