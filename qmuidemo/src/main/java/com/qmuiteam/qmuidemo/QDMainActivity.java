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

package com.qmuiteam.qmuidemo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.base.BaseFragmentActivity;
import com.qmuiteam.qmuidemo.fragment.home.HomeFragment;
import com.qmuiteam.qmuidemo.fragment.lab.QDArchTestFragment;
import com.qmuiteam.qmuidemo.fragment.lab.QDArchWebViewTestFragment;
import com.qmuiteam.qmuidemo.fragment.lab.QDArchSurfaceTestFragment;
import com.qmuiteam.qmuidemo.fragment.util.QDNotchHelperFragment;

import static com.qmuiteam.qmuidemo.fragment.QDWebExplorerFragment.EXTRA_TITLE;
import static com.qmuiteam.qmuidemo.fragment.QDWebExplorerFragment.EXTRA_URL;

public class QDMainActivity extends BaseFragmentActivity {
    private static final String KEY_FRAGMENT = "key_fragment";
    private static final String KEY_URL = "key_url";
    private static final String KEY_TITLE = "key_title";
    private static final int VALUE_FRAGMENT_HOME = 0;
    private static final int VALUE_FRAGMENT_NOTCH_HELPER = 1;
    private static final int VALUE_FRAGMENT_ARCH_TEST = 2;
    private static final int VALUE_FRAGMENT_WEB_EXPLORER_TEST = 3;
    private static final int VALUE_FRAGMENT_SURFACE_TEST = 4;

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
        } else if (ret == VALUE_FRAGMENT_ARCH_TEST) {
            fragment = new QDArchTestFragment();
        } else if (ret == VALUE_FRAGMENT_WEB_EXPLORER_TEST) {
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_URL, intent.getStringExtra(KEY_URL));
            bundle.putString(EXTRA_TITLE, intent.getStringExtra(KEY_TITLE));
            fragment = new QDArchWebViewTestFragment();
            fragment.setArguments(bundle);
        } else if (ret == VALUE_FRAGMENT_SURFACE_TEST) {
            fragment = new QDArchSurfaceTestFragment();
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

    public static Intent createArchTestIntent(Context context) {
        Intent intent = new Intent(context, QDMainActivity.class);
        intent.putExtra(KEY_FRAGMENT, VALUE_FRAGMENT_ARCH_TEST);
        return intent;
    }

    public static Intent createWebExplorerIntent(Context context, String url, String title) {
        Intent intent = new Intent(context, QDMainActivity.class);
        intent.putExtra(KEY_FRAGMENT, VALUE_FRAGMENT_WEB_EXPLORER_TEST);
        intent.putExtra(KEY_URL, url);
        intent.putExtra(KEY_TITLE, title);
        return intent;
    }

    public static Intent createSurfaceTestIntent(Context context) {
        Intent intent = new Intent(context, QDMainActivity.class);
        intent.putExtra(KEY_FRAGMENT, VALUE_FRAGMENT_SURFACE_TEST);
        return intent;
    }
}
