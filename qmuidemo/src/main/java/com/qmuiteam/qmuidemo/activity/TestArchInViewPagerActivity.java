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

package com.qmuiteam.qmuidemo.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.qmuiteam.qmui.arch.QMUIFragment;
import com.qmuiteam.qmui.arch.QMUIFragmentPagerAdapter;
import com.qmuiteam.qmui.widget.QMUIViewPager;
import com.qmuiteam.qmui.widget.tab.QMUITabSegment;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseActivity;
import com.qmuiteam.qmuidemo.fragment.components.QDCollapsingTopBarLayoutFragment;
import com.qmuiteam.qmuidemo.fragment.components.QDTabSegmentScrollableModeFragment;
import com.qmuiteam.qmuidemo.fragment.components.viewpager.QDFitSystemWindowViewPagerFragment;
import com.qmuiteam.qmuidemo.fragment.components.viewpager.QDViewPagerFragment;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TestArchInViewPagerActivity extends BaseActivity {

    @BindView(R.id.pager) QMUIViewPager mViewPager;
    @BindView(R.id.tabs) QMUITabSegment mTabSegment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View root = LayoutInflater.from(this).inflate(R.layout.fragment_fsw_viewpager, null);
        ButterKnife.bind(this, root);
        setContentView(root);
        initPagers();
    }

    private void initPagers() {
        QMUIFragmentPagerAdapter pagerAdapter = new QMUIFragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public QMUIFragment createFragment(int position) {
                switch (position) {
                    case 0:
                        return new QDTabSegmentScrollableModeFragment();
                    case 1:
                        return new QDCollapsingTopBarLayoutFragment();
                    case 2:
                        return new QDFitSystemWindowViewPagerFragment();
                    case 3:
                    default:
                        return new QDViewPagerFragment();
                }
            }

            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return "TabSegment";
                    case 1:
                        return "CTopBar";
                    case 2:
                        return "IViewPager";
                    case 3:
                    default:
                        return "ViewPager";
                }
            }
        };
        mViewPager.setAdapter(pagerAdapter);
        mTabSegment.setupWithViewPager(mViewPager);
    }
}
