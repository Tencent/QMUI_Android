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
import android.content.DialogInterface;
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
import com.qmuiteam.qmui.skin.QMUISkinHelper;
import com.qmuiteam.qmui.skin.QMUISkinLayoutInflaterFactory;
import com.qmuiteam.qmui.skin.QMUISkinMaker;
import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
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

    private QMUISkinManager.OnSkinChangeListener mOnSkinChangeListener = new QMUISkinManager.OnSkinChangeListener() {
        @Override
        public void onSkinChange(int oldSkin, int newSkin) {
            if (newSkin == QDSkinManager.SKIN_WHITE) {
                QMUIStatusBarHelper.setStatusBarLightMode(QDMainActivity.this);
            } else {
                QMUIStatusBarHelper.setStatusBarDarkMode(QDMainActivity.this);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new QMUISkinLayoutInflaterFactory());
        super.onCreate(savedInstanceState);

        mSkinActionLayout = new QMUILinearLayout(this);
        mSkinActionLayout.setOrientation(LinearLayout.VERTICAL);
        mSkinActionLayout.setBackgroundColor(Color.WHITE);
        QMUISkinHelper.setSkinValue(mSkinActionLayout, "background:app_skin_common_background");
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

        QMUIButton changeThemeBtn = createFloatItemBtn("Change Theme", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] items = new String[]{"蓝色（默认）", "黑色", "白色"};
                new QMUIDialog.MenuDialogBuilder(QDMainActivity.this)
                        .addItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                QDSkinManager.changeSkin(which + 1);
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
            }
        });
        mSkinActionLayout.addView(changeThemeBtn,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, btnHeight));

        mSkinMakerBtn = createFloatItemBtn(null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QDApplication.openSkinMake = !QDApplication.openSkinMake;
                renderSkinMakerBtn();

            }
        });
        mSkinActionLayout.addView(mSkinMakerBtn,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, btnHeight));


        QMUIButton skinExportBtn = createFloatItemBtn("Export SkinMaker Result", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QMUISkinMaker.getInstance().export(QDMainActivity.this);
            }
        });
        mSkinActionLayout.addView(skinExportBtn,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, btnHeight));
    }

    private QMUIButton createFloatItemBtn(String text, View.OnClickListener onClickListener) {
        QMUIButton btn = new QMUIButton(this);
        btn.setChangeAlphaWhenPress(true);
        btn.setPadding(0, 0, 0, 0);
        btn.setGravity(Gravity.CENTER);
        btn.setBackground(null);
        btn.setText(text);
        btn.setOnClickListener(onClickListener);
        QMUISkinHelper.setSkinValue(btn, "textColor:app_skin_common_title_text_color");
        return btn;
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
            QMUISkinMaker.getInstance().unBindAll();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        QMUISkinManager.defaultInstance(this).addSkinChangeListener(mOnSkinChangeListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderSkinMakerBtn();
    }

    @Override
    protected void onStop() {
        super.onStop();
        QMUISkinManager.defaultInstance(this).removeSkinChangeListener(mOnSkinChangeListener);
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
