package com.qmuiteam.qmuidemo.fragment.components.qqface;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qmuiteam.qmui.widget.QMUITabSegment;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.fragment.components.QDTabSegmentFixModeFragment;
import com.qmuiteam.qmuidemo.fragment.components.qqface.pageView.QDEmojiconPagerView;
import com.qmuiteam.qmuidemo.fragment.components.qqface.pageView.QDQQFacePagerView;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.lib.Group;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author cginechen
 * @date 2017-06-08
 */

@Widget(group = Group.Other, name = "性能观测")
public class QDQQFacePerformanceTestFragment extends BaseFragment {
    @BindView(R.id.topbar) QMUITopBarLayout mTopBar;
    @BindView(R.id.tabSegment) QMUITabSegment mTabSegment;
    @BindView(R.id.contentViewPager) ViewPager mContentViewPager;

    private Map<Page, View> mPageMap = new HashMap<>();
    private Page mDestPage = Page.QMUIQQFaceView;
    private PagerAdapter mPagerAdapter = new PagerAdapter() {
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return QDTabSegmentFixModeFragment.ContentPage.SIZE;
        }

        @Override
        public Object instantiateItem(final ViewGroup container, int position) {
            Page page = Page.getPage(position);
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
            Page page = Page.getPage(position);
            if (page == Page.QMUIQQFaceView) {
                return "QMUI实现方案性能";
            } else {
                return "ImageSpan实现方案性能";
            }
        }
    };

    @Override
    protected View onCreateView() {
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_tab_viewpager_layout, null);
        ButterKnife.bind(this, rootView);

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

        mTopBar.setTitle(QDDataManager.getInstance().getDescription(this.getClass()).getName());
    }

    private void initTabAndPager() {
        mContentViewPager.setAdapter(mPagerAdapter);
        mContentViewPager.setCurrentItem(mDestPage.getPosition(), false);
        mTabSegment.setupWithViewPager(mContentViewPager, true);
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

            }
        });
    }

    private View getPageView(Page page) {
        View view = mPageMap.get(page);
        if (view == null) {
            if (page == Page.QMUIQQFaceView) {
                view = new QDQQFacePagerView(getContext());
            } else if (page == Page.EmojiconTextView) {
                view = new QDEmojiconPagerView(getContext());
            }
            mPageMap.put(page, view);
        }
        return view;
    }

    public enum Page {
        QMUIQQFaceView(0),
        EmojiconTextView(1);
        private final int position;

        Page(int pos) {
            position = pos;
        }

        public static Page getPage(int position) {
            switch (position) {
                case 0:
                    return QMUIQQFaceView;
                case 1:
                    return EmojiconTextView;
                default:
                    return QMUIQQFaceView;
            }
        }

        public int getPosition() {
            return position;
        }
    }
}
