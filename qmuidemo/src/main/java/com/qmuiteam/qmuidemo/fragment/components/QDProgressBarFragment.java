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

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;

import com.qmuiteam.qmui.widget.QMUIProgressBar;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.model.QDItemDescription;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * {@link QMUIProgressBar} 的使用示例。
 * Created by cgspine on 15/9/15.
 */
@Widget(widgetClass = QMUIProgressBar.class, iconRes = R.mipmap.icon_grid_progress_bar)
public class QDProgressBarFragment extends BaseFragment {

    protected static final int STOP = 0x10000;
    protected static final int NEXT = 0x10001;
    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.rectProgressBar)
    QMUIProgressBar mRectProgressBar;
    @BindView(R.id.circleProgressBar)
    QMUIProgressBar mCircleProgressBar;
    @BindView(R.id.startBtn)
    QMUIRoundButton mStartBtn;
    @BindView(R.id.backBtn)
    QMUIRoundButton mBackBtn;
    int count;

    private QDItemDescription mQDItemDescription;
    private ProgressHandler myHandler = new ProgressHandler();

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_progressbar, null);
        ButterKnife.bind(this, root);

        mQDItemDescription = QDDataManager.getInstance().getDescription(this.getClass());
        initTopBar();

        mRectProgressBar.setQMUIProgressBarTextGenerator(new QMUIProgressBar.QMUIProgressBarTextGenerator() {
            @Override
            public String generateText(QMUIProgressBar progressBar, int value, int maxValue) {
                return value + "/" + maxValue;
            }
        });

        mCircleProgressBar.setQMUIProgressBarTextGenerator(new QMUIProgressBar.QMUIProgressBarTextGenerator() {
            @Override
            public String generateText(QMUIProgressBar progressBar, int value, int maxValue) {
                return 100 * value / maxValue + "%";
            }
        });

        myHandler.setProgressBar(mRectProgressBar, mCircleProgressBar);

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count = 0;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i <= 100; i++) {
                            try {
                                count = i + 1;
                                if (i == 5) {
                                    Message msg = new Message();
                                    msg.what = STOP;
                                    myHandler.sendMessage(msg);
                                } else {
                                    Message msg = new Message();
                                    msg.what = NEXT;
                                    msg.arg1 = count;
                                    myHandler.sendMessage(msg);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        });

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRectProgressBar.setProgress(0);
                mCircleProgressBar.setProgress(0);
            }
        });
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
    }

    private static class ProgressHandler extends Handler {
        private WeakReference<QMUIProgressBar> weakRectProgressBar;
        private WeakReference<QMUIProgressBar> weakCircleProgressBar;

        void setProgressBar(QMUIProgressBar rectProgressBar, QMUIProgressBar circleProgressBar) {
            weakRectProgressBar = new WeakReference<>(rectProgressBar);
            weakCircleProgressBar = new WeakReference<>(circleProgressBar);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case STOP:
                    break;
                case NEXT:
                    if (!Thread.currentThread().isInterrupted()) {
                        if (weakRectProgressBar.get() != null && weakCircleProgressBar.get() != null) {
                            weakRectProgressBar.get().setProgress(msg.arg1);
                            weakCircleProgressBar.get().setProgress(msg.arg1);
                        }
                    }
            }

        }
    }

}
