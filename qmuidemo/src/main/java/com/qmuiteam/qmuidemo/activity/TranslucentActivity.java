package com.qmuiteam.qmuidemo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmuidemo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Kayo on 2016/12/12.
 */

public class TranslucentActivity extends Activity {

    private final static String ARG_CHANGE_TRANSLUCENT = "ARG_CHANGE_TRANSLUCENT";
    private final static String ARG_STATUSBAR_MODE = "ARG_STATUSBAR_MODE";
    @BindView(R.id.topbar) QMUITopBar mTopBar;

    public static Intent createActivity(Context context, boolean isTranslucent) {
        Intent intent = new Intent(context, TranslucentActivity.class);
        intent.putExtra(ARG_CHANGE_TRANSLUCENT, isTranslucent);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            boolean isTranslucent = intent.getBooleanExtra(ARG_CHANGE_TRANSLUCENT, true);
            if (isTranslucent) {
                QMUIStatusBarHelper.translucent(this); // 沉浸式状态栏
            }
        }

        View root = LayoutInflater.from(this).inflate(R.layout.activity_translucent, null);
        ButterKnife.bind(this, root);
        initTopBar();
        setContentView(root);

    }

    private void initTopBar() {
        mTopBar.setBackgroundColor(getResources().getColor(R.color.app_color_theme_4));
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
