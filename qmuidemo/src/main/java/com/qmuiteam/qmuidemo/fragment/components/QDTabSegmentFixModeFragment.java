package com.qmuiteam.qmuidemo.fragment.components;

import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.QMUITabSegment;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmuidemo.QDDataManager;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.model.QDItemDescription;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.lib.Group;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author cginechen
 * @date 2017-04-28
 */

@Widget(group = Group.Other, name = "固定宽度，内容均分")
public class QDTabSegmentFixModeFragment extends BaseFragment {
    @BindView(R.id.topbar) QMUITopBar mTopBar;
    @BindView(R.id.tabSegment) QMUITabSegment mTabSegment;
    @BindView(R.id.contentViewPager) ViewPager mContentViewPager;

    private View mRootView;
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
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            container.addView(view, params);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return super.getPageTitle(position);
        }
    };

    @Override
    protected View onCreateView() {
        mRootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_tab_viewpager_layout, null);
        ButterKnife.bind(this, mRootView);

        mQDItemDescription = QDDataManager.getInstance().getDescription(this.getClass());
        initTopBar();
        initTabAndPager();

        return mRootView;
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
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        dialog.dismiss();
                        switch (position) {
                            case 0:
                                mTabSegment.reset();
                                mTabSegment.setHasIndicator(false);
                                mTabSegment.addTab(new QMUITabSegment.Tab(getString(R.string.tabSegment_item_1_title)));
                                mTabSegment.addTab(new QMUITabSegment.Tab(getString(R.string.tabSegment_item_2_title)));
                                break;
                            case 1:
                                mTabSegment.reset();
                                mTabSegment.setHasIndicator(true);
                                mTabSegment.setIndicatorPosition(false);
                                mTabSegment.setIndicatorWidthAdjustContent(true);
                                mTabSegment.addTab(new QMUITabSegment.Tab(getString(R.string.tabSegment_item_1_title)));
                                mTabSegment.addTab(new QMUITabSegment.Tab(getString(R.string.tabSegment_item_2_title)));
                                break;
                            case 2:
                                mTabSegment.reset();
                                mTabSegment.setHasIndicator(true);
                                mTabSegment.setIndicatorPosition(true);
                                mTabSegment.setIndicatorWidthAdjustContent(true);
                                mTabSegment.addTab(new QMUITabSegment.Tab(getString(R.string.tabSegment_item_1_title)));
                                mTabSegment.addTab(new QMUITabSegment.Tab(getString(R.string.tabSegment_item_2_title)));
                                break;
                            case 3:
                                mTabSegment.reset();
                                mTabSegment.setHasIndicator(true);
                                mTabSegment.setIndicatorPosition(false);
                                mTabSegment.setIndicatorWidthAdjustContent(false);
                                mTabSegment.addTab(new QMUITabSegment.Tab(getString(R.string.tabSegment_item_1_title)));
                                mTabSegment.addTab(new QMUITabSegment.Tab(getString(R.string.tabSegment_item_2_title)));
                                break;
                            case 4:
                                mTabSegment.reset();
                                mTabSegment.setHasIndicator(false);
                                QMUITabSegment.Tab component = new QMUITabSegment.Tab(
                                        ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_component),
                                        null,
                                        "Components", true
                                );
                                QMUITabSegment.Tab util = new QMUITabSegment.Tab(
                                        ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_util),
                                        null,
                                        "Helper", true
                                );
                                mTabSegment.addTab(component);
                                mTabSegment.addTab(util);
                                break;
                            case 5:
                                QMUITabSegment.Tab tab = mTabSegment.getTab(0);
                                tab.setSignCountMargin(0, -QMUIDisplayHelper.dp2px(getContext(), 4));
                                tab.showSignCountView(getContext(), 1);
                                break;
                            case 6:
                                mTabSegment.reset();
                                mTabSegment.setHasIndicator(false);
                                QMUITabSegment.Tab component2 = new QMUITabSegment.Tab(
                                        ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_component),
                                        ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_component_selected),
                                        "Components", false
                                );
                                QMUITabSegment.Tab util2 = new QMUITabSegment.Tab(
                                        ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_util),
                                        ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_util_selected),
                                        "Helper", false
                                );
                                mTabSegment.addTab(component2);
                                mTabSegment.addTab(util2);
                                break;
                            case 7:
                                mTabSegment.reset();
                                mTabSegment.setHasIndicator(true);
                                mTabSegment.setIndicatorWidthAdjustContent(true);
                                mTabSegment.setIndicatorPosition(false);
                                QMUITabSegment.Tab component3 = new QMUITabSegment.Tab(
                                        ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_component),
                                        null,
                                        "Components", true
                                );
                                component3.setTextColor(QMUIResHelper.getAttrColor(getContext(), R.attr.qmui_config_color_blue),
                                        QMUIResHelper.getAttrColor(getContext(), R.attr.qmui_config_color_red));
                                QMUITabSegment.Tab util3 = new QMUITabSegment.Tab(
                                        ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_util),
                                        null,
                                        "Helper", true
                                );
                                util3.setTextColor(QMUIResHelper.getAttrColor(getContext(), R.attr.qmui_config_color_gray_1),
                                        QMUIResHelper.getAttrColor(getContext(), R.attr.qmui_config_color_red));
                                mTabSegment.addTab(component3);
                                mTabSegment.addTab(util3);
                                break;
                            case 8:
                                mTabSegment.updateTabText(0, "动态更新文案");
                                break;
                            case 9:
                                QMUITabSegment.Tab component4 = new QMUITabSegment.Tab(
                                        ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_component),
                                        null,
                                        "动态更新", true
                                );
                                mTabSegment.replaceTab(0, component4);
                                break;

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
        mTabSegment.addTab(new QMUITabSegment.Tab(getString(R.string.tabSegment_item_1_title)));
        mTabSegment.addTab(new QMUITabSegment.Tab(getString(R.string.tabSegment_item_2_title)));
        mTabSegment.setupWithViewPager(mContentViewPager, false);
        mTabSegment.setMode(QMUITabSegment.MODE_FIXED);
        mTabSegment.addOnTabSelectedListener(new QMUITabSegment.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int index) {
                mTabSegment.hideSignCountView(index);
            }

            @Override
            public void onTabUnselected(int index) {

            }

            @Override
            public void onTabReselected(int index) {
                mTabSegment.hideSignCountView(index);
            }

            @Override
            public void onDoubleTap(int index) {

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
