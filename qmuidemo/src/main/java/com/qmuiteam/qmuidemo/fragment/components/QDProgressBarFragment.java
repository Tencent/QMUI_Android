package com.qmuiteam.qmuidemo.fragment.components;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.qmuiteam.qmui.widget.QMUIProgressBar;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmuidemo.QDDataManager;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.model.QDItemDescription;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by cgspine on 15/9/15.
 */
@Widget(widgetClass = QMUIProgressBar.class, iconRes = R.mipmap.icon_grid_progress_bar)
public class QDProgressBarFragment extends BaseFragment {

    protected static final int STOP = 0x10000;
    protected static final int NEXT = 0x10001;
    @BindView(R.id.topbar) QMUITopBar mTopBar;
    @BindView(R.id.rectProgressBar) QMUIProgressBar mRectProgressBar;
    @BindView(R.id.circleProgressBar) QMUIProgressBar mCircleProgressBar;
    @BindView(R.id.startBtn) Button mStartBtn;
    @BindView(R.id.backBtn) Button mBackBtn;
    int count;

    private QDItemDescription mQDItemDescription;
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case STOP:
                    break;
                case NEXT:
                    if (!Thread.currentThread().isInterrupted()) {
                        mRectProgressBar.setProgress(count);
                        mCircleProgressBar.setProgress(count);
                    }
            }
        }
    };

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

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count = 0;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 20; i++) {
                            try {
                                count = (i + 1) * 5;
                                if (i == 19) {
                                    Message msg = new Message();
                                    msg.what = STOP;
                                    myHandler.sendMessage(msg);
                                } else {
                                    Message msg = new Message();
                                    msg.what = NEXT;
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

}
