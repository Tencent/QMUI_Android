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
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.qmuiteam.qmui.arch.QMUIFragment;
import com.qmuiteam.qmui.arch.QMUIFragmentActivity;
import com.qmuiteam.qmui.arch.annotation.DefaultFirstFragment;
import com.qmuiteam.qmui.arch.annotation.FirstFragments;
import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord;
import com.qmuiteam.qmui.layout.QMUIButton;
import com.qmuiteam.qmui.layout.QMUILinearLayout;
import com.qmuiteam.qmui.skin.QMUISkinLayoutInflaterFactory;
import com.qmuiteam.qmui.skin.QMUISkinMaker;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.base.BaseFragmentActivity;
import com.qmuiteam.qmuidemo.fragment.QDWebExplorerFragment;
import com.qmuiteam.qmuidemo.fragment.components.QDPopupFragment;
import com.qmuiteam.qmuidemo.fragment.components.QDTabSegmentFixModeFragment;
import com.qmuiteam.qmuidemo.fragment.home.HomeFragment;
import com.qmuiteam.qmuidemo.fragment.lab.QDArchSurfaceTestFragment;
import com.qmuiteam.qmuidemo.fragment.lab.QDArchTestFragment;
import com.qmuiteam.qmuidemo.fragment.lab.QDContinuousNestedScroll1Fragment;
import com.qmuiteam.qmuidemo.fragment.util.QDNotchHelperFragment;
import com.qmuiteam.qmuidemo.manager.QDSkinManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.LayoutInflaterCompat;

import static com.qmuiteam.qmuidemo.fragment.QDWebExplorerFragment.EXTRA_TITLE;
import static com.qmuiteam.qmuidemo.fragment.QDWebExplorerFragment.EXTRA_URL;

@FirstFragments(
        value = {
                HomeFragment.class,
                QDArchTestFragment.class,
                QDArchSurfaceTestFragment.class,
                QDNotchHelperFragment.class,
                QDWebExplorerFragment.class,
                QDContinuousNestedScroll1Fragment.class,
                QDTabSegmentFixModeFragment.class,
                QDPopupFragment.class
        })
@DefaultFirstFragment(HomeFragment.class)
@LatestVisitRecord
public class QDMainActivity extends BaseFragmentActivity {

    private QMUILinearLayout mSkinActionLayout;
    private QMUIButton mSkinMakerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new QMUISkinLayoutInflaterFactory(this));
        super.onCreate(savedInstanceState);
        QDSkinManager.register(this);


        mSkinActionLayout = new QMUILinearLayout(this);
        mSkinActionLayout.setOrientation(LinearLayout.VERTICAL);
        mSkinActionLayout.setBackgroundColor(Color.WHITE);
        mSkinActionLayout.setShadowElevation(QMUIDisplayHelper.dp2px(this, 32));
        mSkinActionLayout.setShadowAlpha(0.8f);
        mSkinActionLayout.setRadius(QMUIDisplayHelper.dp2px(this, 6));
        FrameLayout.LayoutParams skinLp = new FrameLayout.LayoutParams(
                QMUIDisplayHelper.dp2px(this, 200),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        skinLp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        skinLp.bottomMargin = QMUIDisplayHelper.dp2px(this, 56);
        skinLp.rightMargin = QMUIDisplayHelper.dp2px(this, 16);
        getFragmentContainer().addView(mSkinActionLayout, skinLp);


        int btnHeight = QMUIDisplayHelper.dp2px(this, 48);
        mSkinMakerBtn = new QMUIButton(this);
        mSkinMakerBtn.setChangeAlphaWhenPress(true);
        mSkinMakerBtn.setBackground(null);
        mSkinMakerBtn.setPadding(0, 0, 0, 0);
        mSkinMakerBtn.setGravity(Gravity.CENTER);
        mSkinMakerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QDApplication.openSkinMake = !QDApplication.openSkinMake;
                renderSkinMakerBtn();

            }
        });
        mSkinActionLayout.addView(mSkinMakerBtn,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, btnHeight));


        QMUIButton skinExportBtn = new QMUIButton(this);
        skinExportBtn.setChangeAlphaWhenPress(true);
        skinExportBtn.setPadding(0, 0, 0, 0);
        skinExportBtn.setGravity(Gravity.CENTER);
        skinExportBtn.setBackground(null);
        skinExportBtn.setText("Export SkinMaker Result");
        skinExportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QMUISkinMaker.getInstance().export(QDMainActivity.this);

            }
        });
        mSkinActionLayout.addView(skinExportBtn,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, btnHeight));
    }

    private void renderSkinMakerBtn() {
        BaseFragment baseFragment = (BaseFragment) getCurrentFragment();
        if (QDApplication.openSkinMake) {
            mSkinMakerBtn.setText("Close SkinMaker");
            if (baseFragment != null) {
                baseFragment.openSkinMaker();
            }
        } else {
            mSkinMakerBtn.setText("Open SkinMaker");
            if (baseFragment != null) {
                baseFragment.closeSkinMaker();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        renderSkinMakerBtn();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        QDSkinManager.unRegister(this);
    }

    @Override
    protected int getContextViewId() {
        return R.id.qmuidemo;
    }


    public static Intent createWebExplorerIntent(Context context, String url, String title) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_URL, url);
        bundle.putString(EXTRA_TITLE, title);
        return of(context, QDWebExplorerFragment.class, bundle);
    }

    public static Intent of(@NonNull Context context,
                            @NonNull Class<? extends QMUIFragment> firstFragment) {
        return QMUIFragmentActivity.intentOf(context, QDMainActivity.class, firstFragment);
    }

    public static Intent of(@NonNull Context context,
                            @NonNull Class<? extends QMUIFragment> firstFragment,
                            @Nullable Bundle fragmentArgs) {
        return QMUIFragmentActivity.intentOf(context, QDMainActivity.class, firstFragment, fragmentArgs);
    }
}
