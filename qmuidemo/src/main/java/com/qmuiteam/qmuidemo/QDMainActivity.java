package com.qmuiteam.qmuidemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.base.BaseFragmentActivity;
import com.qmuiteam.qmuidemo.fragment.home.HomeFragment;
import com.qmuiteam.qmuidemo.fragment.util.QDNotchHelperFragment;

public class QDMainActivity extends BaseFragmentActivity {
    private static final String KEY_FRAGMENT = "key_fragment";
    private static final int VALUE_FRAGMENT_HOME = 0;
    private static final int VALUE_FRAGMENT_NOTCH_HELPER = 1;

    @Override
    protected int getContextViewId() {
        return R.id.qmuidemo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            BaseFragment fragment = getFirstFragment();

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(getContextViewId(), fragment, fragment.getClass().getSimpleName())
                    .addToBackStack(fragment.getClass().getSimpleName())
                    .commit();
        }
    }

    private BaseFragment getFirstFragment() {
        Intent intent = getIntent();
        int ret = intent.getIntExtra(KEY_FRAGMENT, 0);
        BaseFragment fragment;
        if (ret == VALUE_FRAGMENT_NOTCH_HELPER) {
            fragment = new QDNotchHelperFragment();
        } else {
            fragment = new HomeFragment();
        }

        return fragment;
    }

    public static Intent createNotchHelperIntent(Context context) {
        Intent intent = new Intent(context, QDMainActivity.class);
        intent.putExtra(KEY_FRAGMENT, VALUE_FRAGMENT_NOTCH_HELPER);
        return intent;
    }
}
