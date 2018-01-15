package com.qmuiteam.qmuidemo.fragment.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.lib.Group;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * {@link QMUIViewHelper#fadeIn(View, int, Animation.AnimationListener, boolean)} 与
 * {@link QMUIViewHelper#fadeOut(View, int, Animation.AnimationListener, boolean)} 的使用示例。
 * Created by Kayo on 2017/2/7.
 */

@Widget(group = Group.Other, name = "Fade 进退场动画")
public class QDViewHelperAnimationFadeFragment extends BaseFragment {

    @BindView(R.id.topbar) QMUITopBar mTopBar;
    @BindView(R.id.actiontBtn) QMUIRoundButton mActionButton;
    @BindView(R.id.popup) TextView mPopupView;

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_viewhelper_animation_show_and_hide, null);
        ButterKnife.bind(this, root);

        initTopBar();
        initContent();

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
    }

    private void initContent() {
        mActionButton.setText("点击显示浮层");
        mActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPopupView.getVisibility() == View.GONE) {
                    mActionButton.setText("点击关闭浮层");
                    mPopupView.setText("以 Fade 动画显示本浮层");
                    QMUIViewHelper.fadeIn(mPopupView, 500, null, true);
                } else {
                    mActionButton.setText("点击显示浮层");
                    mPopupView.setText("以 Fade 动画隐藏本浮层");
                    QMUIViewHelper.fadeOut(mPopupView, 500, null, true);

                }
            }
        });
    }
}
