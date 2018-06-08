package com.qmuiteam.qmuidemo.fragment.components;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.textview.QMUILinkTextView;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author cginechen
 * @date 2017-05-05
 */

@Widget(widgetClass = QMUILinkTextView.class, iconRes = R.mipmap.icon_grid_link_text_view)
public class QDLinkTextViewFragment extends BaseFragment {

    @BindView(R.id.topbar) QMUITopBar mTopBar;
    @BindView(R.id.link_text_view) QMUILinkTextView mLinkTextView;


    @Override
    protected View onCreateView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_link_texview_layout, null);
        ButterKnife.bind(this, view);
        initTopBar();
        mLinkTextView.setOnLinkClickListener(mOnLinkClickListener);
        mLinkTextView.setOnLinkLongClickListener(new QMUILinkTextView.OnLinkLongClickListener() {
            @Override
            public void onLongClick(String text) {
                Toast.makeText(getContext(), "long click: " + text, Toast.LENGTH_SHORT).show();
            }
        });
        mLinkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "click TextView", Toast.LENGTH_SHORT).show();
            }
        });
        // if parent click event should be triggered when TextView area is clicked
//        mLinkTextView.setNeedForceEventToParent(true);
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getContext(), "forceEventToParent", Toast.LENGTH_SHORT).show();
//            }
//        });
        return view;
    }

    private void initTopBar() {
        mTopBar.setTitle(QDDataManager.getInstance().getDescription(this.getClass()).getName());
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });
    }

    private QMUILinkTextView.OnLinkClickListener mOnLinkClickListener = new QMUILinkTextView.OnLinkClickListener() {
        @Override
        public void onTelLinkClick(String phoneNumber) {
            Toast.makeText(getContext(), "识别到电话号码是：" + phoneNumber, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onMailLinkClick(String mailAddress) {
            Toast.makeText(getContext(), "识别到邮件地址是：" + mailAddress, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onWebUrlLinkClick(String url) {
            Toast.makeText(getContext(), "识别到网页链接是：" + url, Toast.LENGTH_SHORT).show();
        }
    };
}
