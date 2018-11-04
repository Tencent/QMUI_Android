package com.qmuiteam.qmuidemo.activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;

import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 沉浸式状态栏的调用示例。
 * Created by Kayo on 2016/12/12.
 */

public class TranslucentActivity extends BaseActivity {

    @BindView(R.id.topbar) QMUITopBar mTopBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View root = LayoutInflater.from(this).inflate(R.layout.activity_translucent, null);
        ButterKnife.bind(this, root);
        initTopBar();
        setContentView(root);

    }

    private void initTopBar() {
        mTopBar.setBackgroundColor(ContextCompat.getColor(this, R.color.app_color_theme_4));
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_still, R.anim.slide_out_right);
            }
        });

        mTopBar.setTitle("沉浸式状态栏示例");
    }
}
