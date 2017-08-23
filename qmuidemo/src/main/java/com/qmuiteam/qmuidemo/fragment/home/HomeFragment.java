package com.qmuiteam.qmuidemo.fragment.home;

import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.QMUITabSegment;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.R;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author cginechen
 * @date 2016-10-19
 */

public class HomeFragment extends BaseFragment {
    private final static String TAG = HomeFragment.class.getSimpleName();

    @BindView(R.id.pager) ViewPager mViewPager;
    @BindView(R.id.tabs) QMUITabSegment mTabSegment;
    private HashMap<Pager, HomeController> mPages;
    private PagerAdapter mPagerAdapter = new PagerAdapter() {

        private int mChildCount = 0;

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return mPages.size();
        }

        @Override
        public Object instantiateItem(final ViewGroup container, int position) {
            HomeController page = mPages.get(Pager.getPagerFromPositon(position));
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            container.addView(page, params);
            return page;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
            if (mChildCount == 0) {
                return POSITION_NONE;
            }
            return super.getItemPosition(object);
        }

        @Override
        public void notifyDataSetChanged() {
            mChildCount = getCount();
            super.notifyDataSetChanged();
        }
    };


    @Override
    protected View onCreateView() {
        FrameLayout layout = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_home, null);
        ButterKnife.bind(this, layout);
        initTabs();
        initPagers();
        return layout;
    }

    private void initTabs() {
        int normalColor = QMUIResHelper.getAttrColor(getActivity(), R.attr.qmui_config_color_gray_6);
        int selectColor = QMUIResHelper.getAttrColor(getActivity(), R.attr.qmui_config_color_blue);
        mTabSegment.setDefaultNormalColor(normalColor);
        mTabSegment.setDefaultSelectedColor(selectColor);
        QMUITabSegment.Tab component = new QMUITabSegment.Tab(
                ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_component),
                ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_component_selected),
                "Components", false
        );
        QMUITabSegment.Tab util = new QMUITabSegment.Tab(
                ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_util),
                ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_util_selected),
                "Helper", false
        );
        QMUITabSegment.Tab lab = new QMUITabSegment.Tab(
                ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_lab),
                ContextCompat.getDrawable(getContext(), R.mipmap.icon_tabbar_lab_selected),
                "Lab", false
        );
        mTabSegment.addTab(component)
                .addTab(util)
                .addTab(lab);
    }

    private void initPagers() {

        HomeController.HomeControlListener listener = new HomeController.HomeControlListener() {
            @Override
            public void startFragment(BaseFragment fragment) {
                HomeFragment.this.startFragment(fragment);
            }
        };

        mPages = new HashMap<>();

        HomeController homeComponentsController = new HomeComponentsController(getActivity());
        homeComponentsController.setHomeControlListener(listener);
        mPages.put(Pager.COMPONENT, homeComponentsController);

        HomeController homeUtilController = new HomeUtilController(getActivity());
        homeUtilController.setHomeControlListener(listener);
        mPages.put(Pager.UTIL, homeUtilController);

        HomeController homeLabController = new HomeLabController(getActivity());
        homeLabController.setHomeControlListener(listener);
        mPages.put(Pager.LAB, homeLabController);

        mViewPager.setAdapter(mPagerAdapter);
        mTabSegment.setupWithViewPager(mViewPager, false);
    }

    enum Pager {
        COMPONENT, UTIL, LAB;

        public static Pager getPagerFromPositon(int position) {
            switch (position) {
                case 0:
                    return COMPONENT;
                case 1:
                    return UTIL;
                case 2:
                    return LAB;
                default:
                    return COMPONENT;
            }
        }
    }
}