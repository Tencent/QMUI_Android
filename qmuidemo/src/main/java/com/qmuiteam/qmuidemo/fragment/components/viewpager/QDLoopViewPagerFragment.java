package com.qmuiteam.qmuidemo.fragment.components.viewpager;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUIPagerAdapter;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.QMUIViewPager;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author cginechen
 * @date 2017-09-13
 */

@Widget(name = "QDLoopViewPagerFragment")
public class QDLoopViewPagerFragment extends BaseFragment {
    @BindView(R.id.topbar) QMUITopBar mTopBar;
    @BindView(R.id.pager) QMUIViewPager mViewPager;

    private List<String> mItems = new ArrayList<>();


    @Override
    protected View onCreateView() {
        FrameLayout layout = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_loop_viewpager, null);
        ButterKnife.bind(this, layout);
        initData(5);
        initTopBar();
        initPagers();
        return layout;
    }

    private void initData(int count) {
        for (int i = 0; i < count; i++) {
            mItems.add(String.valueOf(i));
        }
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


    private void initPagers() {
        QMUIPagerAdapter pagerAdapter = new QMUIPagerAdapter() {

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public int getCount() {
                return mItems.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mItems.get(position);
            }

            @Override
            protected Object hydrate(ViewGroup container, int position) {
                return new ItemView(getContext());
            }

            @Override
            protected void populate(ViewGroup container, Object item, int position) {
                ItemView itemView = (ItemView) item;
                itemView.setText(mItems.get(position));
                container.addView(itemView);
            }

            @Override
            protected void destroy(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        };
        //setPageTransformer默认采用ViewCompat.LAYER_TYPE_HARDWARE， 但它在某些4.x的国产机下会crash
        boolean canUseHardware = Build.VERSION.SDK_INT >= 21;
        mViewPager.setPageTransformer(false, new CardTransformer(),
                canUseHardware ? ViewCompat.LAYER_TYPE_HARDWARE : ViewCompat.LAYER_TYPE_SOFTWARE);
        mViewPager.setInfiniteRatio(500);
        mViewPager.setEnableLoop(true);
        mViewPager.setAdapter(pagerAdapter);
    }

    static class ItemView extends FrameLayout {
        private TextView mTextView;

        public ItemView(Context context) {
            super(context);
            mTextView = new TextView(context);
            mTextView.setTextSize(20);
            mTextView.setTextColor(ContextCompat.getColor(context, R.color.app_color_theme_5));
            mTextView.setGravity(Gravity.CENTER);
            mTextView.setBackgroundColor(ContextCompat.getColor(context, R.color.qmui_config_color_white));
            int size = QMUIDisplayHelper.dp2px(context, 300);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);
            lp.gravity = Gravity.CENTER;
            addView(mTextView, lp);
        }

        public void setText(CharSequence text) {
            mTextView.setText(text);
        }
    }
}
