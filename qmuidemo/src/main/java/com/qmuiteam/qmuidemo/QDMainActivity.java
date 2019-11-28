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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.qmuiteam.qmui.arch.QMUIFragment;
import com.qmuiteam.qmui.arch.QMUIFragmentActivity;
import com.qmuiteam.qmui.arch.annotation.DefaultFirstFragment;
import com.qmuiteam.qmui.arch.annotation.FirstFragments;
import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord;
import com.qmuiteam.qmui.layout.QMUIButton;
import com.qmuiteam.qmui.layout.QMUIFrameLayout;
import com.qmuiteam.qmui.layout.QMUILinearLayout;
import com.qmuiteam.qmui.skin.QMUISkinHelper;
import com.qmuiteam.qmui.skin.QMUISkinLayoutInflaterFactory;
import com.qmuiteam.qmui.skin.QMUISkinMaker;
import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView2;
import com.qmuiteam.qmui.widget.QMUIWindowInsetLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;
import com.qmuiteam.qmui.widget.popup.QMUIPopups;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.LayoutInflaterCompat;
import androidx.customview.widget.ViewDragHelper;

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

    private QMUIPopup mGlobalAction;

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
    }

    @Override
    protected RootView onCreateRootView(int fragmentContainerId) {
        return new CustomRootView(this, fragmentContainerId);
    }


    private void renderSkinMakerBtn() {
        BaseFragment baseFragment = (BaseFragment) getCurrentFragment();
        if (QDApplication.openSkinMake) {
            if (baseFragment != null) {
                baseFragment.openSkinMaker();
            }
        } else {
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

    private void showGlobalActionPopup(View v){
        String[] listItems = new String[]{
                "Change Theme",
                QDApplication.openSkinMake ? "Close SkinMaker" : "Open SkinMaker",
                "Export SkinMaker Result"
        };
        List<String> data = new ArrayList<>();

        Collections.addAll(data, listItems);

        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.simple_list_item, data);
        AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 0){
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
                }else if(i == 1){
                    QDApplication.openSkinMake = !QDApplication.openSkinMake;
                    renderSkinMakerBtn();
                }else if(i == 2){
                    QMUISkinMaker.getInstance().export(QDMainActivity.this);
                }
                if (mGlobalAction != null) {
                    mGlobalAction.dismiss();
                }
            }
        };
        mGlobalAction = QMUIPopups.listPopup(this,
                QMUIDisplayHelper.dp2px(this, 250),
                QMUIDisplayHelper.dp2px(this, 300),
                adapter,
                onItemClickListener)
                .animStyle(QMUIPopup.ANIM_GROW_FROM_CENTER)
                .preferredDirection(QMUIPopup.DIRECTION_TOP)
                .shadow(true)
                .offsetYIfTop(QMUIDisplayHelper.dp2px(this, 5))
                .show(v);
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

    class CustomRootView extends RootView {

        private QMUIWindowInsetLayout fragmentContainer;
        private ViewDragHelper viewDragHelper;
        private QMUIRadiusImageView2  globalBtn;
        private int btnSize;

        public CustomRootView(Context context, int fragmentContainerId) {
            super(context);

            btnSize = QMUIDisplayHelper.dp2px(context, 56);

            fragmentContainer = new QMUIWindowInsetLayout(context);
            fragmentContainer.setId(fragmentContainerId);
            addView(fragmentContainer, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


            globalBtn = new QMUIRadiusImageView2(context);
            globalBtn.setRadiusAndShadow(btnSize / 2,
                    QMUIDisplayHelper.dp2px(getContext(), 16), 0.4f);
            globalBtn.setBorderWidth(1);
            globalBtn.setBorderColor(QMUIResHelper.getAttrColor(context, R.attr.qmui_skin_support_color_separator));
            globalBtn.setBackgroundColor(QMUIResHelper.getAttrColor(context, R.attr.app_skin_common_background));
            globalBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showGlobalActionPopup(v);
                }
            });
            FrameLayout.LayoutParams globalBtnLp = new FrameLayout.LayoutParams(btnSize, btnSize);
            globalBtnLp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
            globalBtnLp.bottomMargin = QMUIDisplayHelper.dp2px(context, 60);
            globalBtnLp.rightMargin = QMUIDisplayHelper.dp2px(context, 24);
            QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
            builder.background(R.attr.app_skin_common_background);
            builder.border(R.attr.qmui_skin_support_color_separator);
            QMUISkinHelper.setSkinValue(globalBtn, builder);
            builder.release();
            addView(globalBtn, globalBtnLp);


            viewDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
                @Override
                public boolean tryCaptureView(View child, int pointerId) {
                    return child == globalBtn;
                }

                @Override
                public int clampViewPositionHorizontal(View child, int left, int dx) {
                    return left;
                }

                @Override
                public int clampViewPositionVertical(View child, int top, int dy) {
                    return top;
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            return viewDragHelper.shouldInterceptTouchEvent(event);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            viewDragHelper.processTouchEvent(event);
            return true;
        }
    }
}
