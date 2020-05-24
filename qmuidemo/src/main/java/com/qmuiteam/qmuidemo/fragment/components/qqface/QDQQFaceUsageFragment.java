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

package com.qmuiteam.qmuidemo.fragment.components.qqface;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord;
import com.qmuiteam.qmui.qqface.QMUIQQFaceView;
import com.qmuiteam.qmui.span.QMUITouchableSpan;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.lib.Group;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;

import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author cginechen
 * @date 2016-12-24
 */

@Widget(group = Group.Other, name = "QQ表情使用展示")
@LatestVisitRecord
public class QDQQFaceUsageFragment extends BaseFragment {
    @BindView(R.id.topbar) QMUITopBarLayout mTopBar;
    @BindView(R.id.qqface1) QMUIQQFaceView mQQFace1;
    @BindView(R.id.qqface2) QMUIQQFaceView mQQFace2;
    @BindView(R.id.qqface3) QMUIQQFaceView mQQFace3;
    @BindView(R.id.qqface4) QMUIQQFaceView mQQFace4;
    @BindView(R.id.qqface5) QMUIQQFaceView mQQFace5;
    @BindView(R.id.qqface6) QMUIQQFaceView mQQFace6;
    @BindView(R.id.qqface7) QMUIQQFaceView mQQFace7;
    @BindView(R.id.qqface8) QMUIQQFaceView mQQFace8;
    @BindView(R.id.qqface9) QMUIQQFaceView mQQFace9;
    @BindView(R.id.qqface10) QMUIQQFaceView mQQFace10;
    @BindView(R.id.qqface11) QMUIQQFaceView mQQFace11;
    @BindView(R.id.qqface12) QMUIQQFaceView mQQFace12;
    @BindView(R.id.qqface13) QMUIQQFaceView mQQFace13;
    @BindView(R.id.qqface14) QMUIQQFaceView mQQFace14;
    @BindView(R.id.qqface15) QMUIQQFaceView mQQFace15;
    @BindView(R.id.qqface16) QMUIQQFaceView mQQFace16;
    @BindView(R.id.qqface17) QMUIQQFaceView mQQFace17;
    @BindView(R.id.qqface18) QMUIQQFaceView mQQFace18;

    @Override
    protected View onCreateView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_qqface_layout, null);
        ButterKnife.bind(this, view);
        initTopBar();
        initData();
        return view;
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        mTopBar.setTitle(QDDataManager.getInstance().getName(this.getClass()));
    }

    private void initData() {
        mQQFace1.setText("这是一行很长很长[微笑][微笑][微笑][微笑]的文本，但是[微笑][微笑][微笑][微笑]只能单行显示");
        mQQFace2.setText("这是一段很长很长[微笑][微笑][微笑][微笑]的文本，但是最多只能显示三行；" +
                "这是一段很长很长[微笑][微笑][微笑][微笑]的文本，但是最多只能显示三行；" +
                "这是一段很长很长[微笑][微笑][微笑][微笑]的文本，但是最多只能显示三行。");
        mQQFace3.setText("这是一行很长很长[微笑][微笑][微笑][微笑]的文本，但是[微笑][微笑][微笑][微笑]只能单行显示");
        mQQFace4.setText("这是一段很长很长[微笑][微笑][微笑][微笑]的文本，但是最多只能显示三行；" +
                "这是一段很长很长[微笑][微笑][微笑][微笑]的文本，但是最多只能显示三行；" +
                "这是一段很长很长[微笑][微笑][微笑][微笑]的文本，但是最多只能显示三行。");
        mQQFace5.setText("这是一行很长很长[微笑][微笑][微笑][微笑]的文本，但是[微笑][微笑][微笑][微笑]只能单行显示");
        mQQFace6.setText("这是一段很长很长[微笑][微笑][微笑][微笑]的文本，但是最多只能显示三行；" +
                "这是一段很长很长[微笑][微笑][微笑][微笑]的文本，但是最多只能显示三行；" +
                "这是一段很长很长[微笑][微笑][微笑][微笑]的文本，但是最多只能显示三行。");

        mQQFace7.setText("[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]");
        mQQFace8.setText("[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]");
        mQQFace9.setText("[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]");
        mQQFace10.setText("[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑]");
        mQQFace11.setText("[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑]");
        mQQFace12.setText("[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑]");
        mQQFace13.setText("表情可以和字体一起变大[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑][微笑]" +
                "[微笑][微笑][微笑][微笑][微笑]");

        String topic = "#[发呆][微笑]话题";
        String text = "这是一段文本，为了测量 span 的点击在不同 Gravity 下能否正常工作。" + topic;


        SpannableString sb = new SpannableString(text);
        QMUITouchableSpan span = new QMUITouchableSpan(mQQFace14,
                R.attr.app_skin_span_normal_text_color,
                R.attr.app_skin_span_pressed_text_color,
                R.attr.app_skin_span_normal_bg_color,
                R.attr.app_skin_span_pressed_bg_color) {
            @Override
            public void onSpanClick(View widget) {
                Toast.makeText(widget.getContext(), "点击了话题", Toast.LENGTH_SHORT).show();
            }
        };
        span.setIsNeedUnderline(true);
        sb.setSpan(span, text.indexOf(topic), text.indexOf(topic) + topic.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        mQQFace14.setText(sb);
        mQQFace15.setText(sb);
        mQQFace15.setLinkUnderLineColor(Color.RED);
        mQQFace16.setText(sb);
        mQQFace16.setLinkUnderLineHeight(QMUIDisplayHelper.dp2px(getContext(), 4));
        mQQFace16.setLinkUnderLineColor(ContextCompat.getColorStateList(getContext(), R.color.s_app_color_blue_to_red));
        mQQFace15.setGravity(Gravity.CENTER);
        mQQFace16.setGravity(Gravity.RIGHT);

        mQQFace17.setLinkUnderLineColor(Color.RED);
        mQQFace17.setNeedUnderlineForMoreText(true);
        mQQFace17.setText("这是一段文本，为了测量更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多" +
                "更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多" +
                "更多更多更多更多更多更多更多更多更多更多更多更多更多更多更多的显示情况");

        mQQFace18.setParagraphSpace(QMUIDisplayHelper.dp2px(getContext(), 20));
        mQQFace18.setText("这是一段文本，为[微笑]了测量多段落[微笑]\n" +
                "这是一段文本，为[微笑]了测量多段落[微笑]\n这是一段文本，为[微笑]了测量多段落[微笑]");
    }
}
