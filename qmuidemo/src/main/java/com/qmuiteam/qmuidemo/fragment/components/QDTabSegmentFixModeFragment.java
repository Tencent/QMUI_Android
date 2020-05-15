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

package com.qmuiteam.qmuidemo.fragment.components;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qmuiteam.qmui.arch.annotation.FragmentScheme;
import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.tab.QMUITab;
import com.qmuiteam.qmui.widget.tab.QMUITabBuilder;
import com.qmuiteam.qmui.widget.tab.QMUITabIndicator;
import com.qmuiteam.qmui.widget.tab.QMUITabSegment;
import com.qmuiteam.qmuidemo.QDMainActivity;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.Group;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.model.QDItemDescription;

import java.util.HashMap;
import java.util.Map;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author cginechen
 * @date 2017-04-28
 */

@LatestVisitRecord
@Widget(group = Group.Other, name = "固定宽度，内容均分")
@FragmentScheme(
        name = "tab",
        activities = {QDMainActivity.class},
        required = {"mode=1"},
        keysWithIntValue = {"mode"})
public class QDTabSegmentFixModeFragment extends BaseFragment {
    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.tabSegment)
    QMUITabSegment mTabSegment;
    @BindView(R.id.contentViewPager)
    ViewPager mContentViewPager;

    private Map<ContentPage, View> mPageMap = new HashMap<>();
    private ContentPage mDestPage = ContentPage.Item1;
    private QDItemDescription mQDItemDescription;
    private PagerAdapter mPagerAdapter = new PagerAdapter() {
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return ContentPage.SIZE;
        }

        @Override
        public Object instantiateItem(final ViewGroup container, int position) {
            ContentPage page = ContentPage.getPage(position);
            View view = getPageView(page);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            container.addView(view, params);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    };

    @Override
    protected View onCreateView() {
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_tab_viewpager_layout, null);
        ButterKnife.bind(this, rootView);

        mQDItemDescription = QDDataManager.getInstance().getDescription(this.getClass());
        initTopBar();
        initTabAndPager();

        return rootView;
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        mTopBar.setTitle(mQDItemDescription.getName());
        mTopBar.addRightImageButton(R.mipmap.icon_topbar_overflow, R.id.topbar_right_change_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showBottomSheetList();
                    }
                });
    }

    private void showBottomSheetList() {
        new QMUIBottomSheet.BottomListSheetBuilder(getActivity())
                .addItem(getResources().getString(R.string.tabSegment_mode_general))
                .addItem(getResources().getString(R.string.tabSegment_mode_bottom_indicator))
                .addItem(getResources().getString(R.string.tabSegment_mode_top_indicator))
                .addItem(getResources().getString(R.string.tabSegment_mode_indicator_with_content))
                .addItem(getResources().getString(R.string.tabSegment_mode_left_icon_and_auto_tint))
                .addItem(getResources().getString(R.string.tabSegment_mode_sign_count))
                .addItem(getResources().getString(R.string.tabSegment_mode_icon_change))
                .addItem(getResources().getString(R.string.tabSegment_mode_muti_color))
                .addItem(getResources().getString(R.string.tabSegment_mode_change_content_by_index))
                .addItem(getResources().getString(R.string.tabSegment_mode_replace_tab_by_index))
                .addItem(getResources().getString(R.string.tabSegment_mode_scale_selected))
                .addItem(getResources().getString(R.string.tabSegment_mode_change_gravity))
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        dialog.dismiss();
                        Context context = getContext();
                        QMUITabBuilder tabBuilder = mTabSegment.tabBuilder()
                                .setGravity(Gravity.CENTER);
                        int indicatorHeight = QMUIDisplayHelper.dp2px(context, 2);
                        switch (position) {
                            case 0:
                                mTabSegment.reset();
                                mTabSegment.setIndicator(null);
                                mTabSegment.addTab(tabBuilder.setText(getString(R.string.tabSegment_item_1_title)).build(getContext()));
                                mTabSegment.addTab(tabBuilder.setText(getString(R.string.tabSegment_item_2_title)).build(getContext()));
                                break;
                            case 1:
                                mTabSegment.reset();
                                mTabSegment.setIndicator(new QMUITabIndicator(
                                        indicatorHeight, false, true));
                                mTabSegment.addTab(tabBuilder.setText(getString(R.string.tabSegment_item_1_title)).build(getContext()));
                                mTabSegment.addTab(tabBuilder.setText(getString(R.string.tabSegment_item_2_title)).build(getContext()));
                                break;
                            case 2:
                                mTabSegment.reset();
                                mTabSegment.setIndicator(new QMUITabIndicator(
                                        indicatorHeight, true, true));
                                mTabSegment.addTab(tabBuilder.setText(getString(R.string.tabSegment_item_1_title)).build(getContext()));
                                mTabSegment.addTab(tabBuilder.setText(getString(R.string.tabSegment_item_2_title)).build(getContext()));
                                break;
                            case 3:
                                mTabSegment.reset();
                                mTabSegment.setIndicator(new QMUITabIndicator(
                                        indicatorHeight, false, false));
                                mTabSegment.addTab(tabBuilder.setText(getString(R.string.tabSegment_item_1_title)).build(getContext()));
                                mTabSegment.addTab(tabBuilder.setText(getString(R.string.tabSegment_item_2_title)).build(getContext()));
                                break;
                            case 4: {
                                mTabSegment.reset();
                                mTabSegment.setIndicator(null);
                                tabBuilder.setDynamicChangeIconColor(true);
                                QMUITab component = tabBuilder
                                        .setNormalDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_tabbar_component))
                                        .setSelectedDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_tabbar_component_selected))
                                        .setText("Components")
                                        .build(getContext());
                                QMUITab util = tabBuilder
                                        .setNormalDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_tabbar_util))
                                        .setSelectedDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_tabbar_util_selected))
                                        .setText("Helper")
                                        .build(getContext());
                                mTabSegment.addTab(component);
                                mTabSegment.addTab(util);
                                break;
                            }
                            case 5:
//                                mTabSegment.showSignCountView(getContext(), 0, 20); // 也可以直接调用这个
                                QMUITab tab = mTabSegment.getTab(0);
                                tab.setSignCount(20);

                                QMUITab tab1 = mTabSegment.getTab(1);
                                tab1.setRedPoint();
                                break;
                            case 6: {
                                mTabSegment.reset();
                                mTabSegment.setIndicator(null);
                                tabBuilder.setDynamicChangeIconColor(false);
                                QMUITab component = tabBuilder
                                        .setNormalDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_tabbar_component))
                                        .setSelectedDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_tabbar_component_selected))
                                        .setText("Components")
                                        .build(getContext());
                                QMUITab util = tabBuilder
                                        .setNormalDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_tabbar_util))
                                        .setSelectedDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_tabbar_util_selected))
                                        .setText("Helper")
                                        .build(getContext());
                                mTabSegment.addTab(component);
                                mTabSegment.addTab(util);
                                break;
                            }
                            case 7: {
                                mTabSegment.reset();
                                mTabSegment.setIndicator(new QMUITabIndicator(
                                        indicatorHeight, false, true));
                                tabBuilder.setDynamicChangeIconColor(true);
                                QMUITab component = tabBuilder
                                        .setNormalDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_tabbar_component))
                                        .setSelectedDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_tabbar_component_selected))
                                        .setText("Components")
                                        .setColorAttr(R.attr.qmui_config_color_gray_1, R.attr.qmui_config_color_blue)
                                        .build(getContext());
                                QMUITab util = tabBuilder
                                        .setNormalDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_tabbar_util))
                                        .setSelectedDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_tabbar_util_selected))
                                        .setText("Helper")
                                        .setColorAttr(R.attr.qmui_config_color_gray_1, R.attr.qmui_config_color_red)
                                        .build(getContext());
                                mTabSegment.addTab(component);
                                mTabSegment.addTab(util);
                                break;
                            }
                            case 8:
                                mTabSegment.updateTabText(0, "动态更新文案");
                                break;
                            case 9: {
                                QMUITab newTab = tabBuilder.setText("动态更新")
                                        .setNormalDrawable(ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_component))
                                        .setDynamicChangeIconColor(true)
                                        .build(getContext());
                                mTabSegment.replaceTab(0, newTab);
                                break;
                            }
                            case 10: {
                                mTabSegment.reset();
                                mTabSegment.setIndicator(new QMUITabIndicator(
                                        indicatorHeight, false, true));
                                tabBuilder.setDynamicChangeIconColor(true)
                                        .setTextSize(
                                                QMUIDisplayHelper.sp2px(context, 13),
                                                QMUIDisplayHelper.sp2px(context, 15))
                                        .setSelectedIconScale(1.5f);
                                QMUITab component = tabBuilder
                                        .setNormalDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_tabbar_component))
                                        .setSelectedDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_tabbar_component_selected))
                                        .setText("Components")
                                        .setColorAttr(R.attr.qmui_config_color_blue, R.attr.qmui_config_color_red)
                                        .build(getContext());
                                QMUITab util = tabBuilder
                                        .setNormalDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_tabbar_util))
                                        .setSelectedDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_tabbar_util_selected))
                                        .setText("Helper")
                                        .setColorAttr(R.attr.qmui_config_color_gray_1, R.attr.qmui_config_color_red)
                                        .build(getContext());
                                mTabSegment.addTab(component);
                                mTabSegment.addTab(util);
                                break;
                            }
                            case 11: {
                                mTabSegment.reset();
                                mTabSegment.setIndicator(new QMUITabIndicator(
                                        indicatorHeight, false, true));
                                tabBuilder.setDynamicChangeIconColor(true)
                                        .setTextSize(
                                                QMUIDisplayHelper.sp2px(context, 13),
                                                QMUIDisplayHelper.sp2px(context, 15))
                                        .setSelectedIconScale(1.5f)
                                        .setGravity(Gravity.LEFT | Gravity.BOTTOM);
                                QMUITab component = tabBuilder
                                        .setNormalDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_tabbar_component))
                                        .setSelectedDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_tabbar_component_selected))
                                        .setText("Components")
                                        .setColorAttr(R.attr.qmui_config_color_blue, R.attr.qmui_config_color_red)
                                        .build(getContext());
                                QMUITab util = tabBuilder
                                        .setNormalDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_tabbar_util))
                                        .setSelectedDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_tabbar_util_selected))
                                        .setText("Helper")
                                        .setColorAttr(R.attr.qmui_config_color_gray_1, R.attr.qmui_config_color_red)
                                        .build(getContext());
                                mTabSegment.addTab(component);
                                mTabSegment.addTab(util);
                                break;
                            }
                            default:
                                break;
                        }
                        mTabSegment.notifyDataChanged();
                    }
                })
                .build()
                .show();
    }

    private void initTabAndPager() {
        mContentViewPager.setAdapter(mPagerAdapter);
        mContentViewPager.setCurrentItem(mDestPage.getPosition(), false);
        QMUITabBuilder builder = mTabSegment.tabBuilder();
        mTabSegment.addTab(builder.setText(getString(R.string.tabSegment_item_1_title)).build(getContext()));
        mTabSegment.addTab(builder.setText(getString(R.string.tabSegment_item_2_title)).build(getContext()));
        mTabSegment.setupWithViewPager(mContentViewPager, false);
        mTabSegment.setMode(QMUITabSegment.MODE_FIXED);
        mTabSegment.addOnTabSelectedListener(new QMUITabSegment.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int index) {

            }

            @Override
            public void onTabUnselected(int index) {

            }

            @Override
            public void onTabReselected(int index) {
            }

            @Override
            public void onDoubleTap(int index) {
                mTabSegment.clearSignCountView(index);
            }
        });
    }

    private View getPageView(ContentPage page) {
        View view = mPageMap.get(page);
        if (view == null) {
            TextView textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.app_color_description));

            if (page == ContentPage.Item1) {
                textView.setText(R.string.tabSegment_item_1_content);
            } else if (page == ContentPage.Item2) {
                textView.setText(R.string.tabSegment_item_2_content);
            }

            view = textView;
            mPageMap.put(page, view);
        }
        return view;
    }

    public enum ContentPage {
        Item1(0),
        Item2(1);
        public static final int SIZE = 2;
        private final int position;

        ContentPage(int pos) {
            position = pos;
        }

        public static ContentPage getPage(int position) {
            switch (position) {
                case 0:
                    return Item1;
                case 1:
                    return Item2;
                default:
                    return Item1;
            }
        }

        public int getPosition() {
            return position;
        }
    }
}
