package com.qmuiteam.qmuidemo.fragment.lab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.QMUIAnimationListView;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmuidemo.QDDataManager;
import com.qmuiteam.qmuidemo.adaptor.QDSimpleAdapter;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.Group;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author cginechen
 * @date 2017-03-30
 */

@Widget(group = Group.Lab, widgetClass = QMUIAnimationListView.class, iconRes = R.mipmap.icon_grid_anim_list_view)
public class QDAnimationListViewFragment extends BaseFragment {

    @BindView(R.id.topbar) QMUITopBar mTopBar;
    @BindView(R.id.listview) QMUIAnimationListView mListView;

    private MyAdapter mAdapter;
    private List<String> mData = new ArrayList<>();

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getContext()).inflate(R.layout.fragment_animation_listview, null);
        ButterKnife.bind(this, root);
        initTopBar();
        initListView();
        return root;
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        mTopBar.setTitle(QDDataManager.getInstance().getName(this.getClass()));
        mTopBar.addRightTextButton("添加", QMUIViewHelper.generateViewId()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListView.manipulate(new QMUIAnimationListView.Manipulator<MyAdapter>() {
                    @Override
                    public void manipulate(MyAdapter adapter) {
                        int position = mListView.getFirstVisiblePosition();
                        long current = System.currentTimeMillis();
                        mData.add(position + 1, "item add" + (current + 1));
                        mData.add(position + 2, "item add" + (current + 2));
                        mData.add(position + 3, "item add" + (current + 3));
                        mData.add(position + 4, "item add" + (current + 4));
                        mData.add(position + 5, "item add" + (current + 5));
                        mData.add(position + 6, "item add" + (current + 6));
                        mData.add(position + 7, "item add" + (current + 7));
                        mData.add(position + 8, "item add" + (current + 8));
                        mData.add(position + 9, "item add" + (current + 9));
                        mData.add(position + 10, "item add" + (current + 10));
                        mData.add(position + 11, "item add" + (current + 11));
                    }
                });

            }
        });
        mTopBar.addRightTextButton("删除", QMUIViewHelper.generateViewId()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListView.manipulate(new QMUIAnimationListView.Manipulator<MyAdapter>() {
                    @Override
                    public void manipulate(MyAdapter adapter) {
                        int position = mListView.getFirstVisiblePosition();
                        mData.remove(position + 1);
                        mData.remove(position + 3);
                    }
                });
            }
        });
    }

    private void initListView() {
        for (int i = 0; i < 20; i++) {
            mData.add("item " + (i + 1));
        }
        mAdapter = new MyAdapter(getContext(), mData);
        mListView.setAdapter(mAdapter);
    }


    private static class MyAdapter extends QDSimpleAdapter {
        public MyAdapter(Context context, List<String> data) {
            super(context, data);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).hashCode();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
