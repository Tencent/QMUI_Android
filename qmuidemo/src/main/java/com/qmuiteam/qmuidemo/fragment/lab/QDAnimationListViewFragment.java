package com.qmuiteam.qmuidemo.fragment.lab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.QMUIAnimationListView;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
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
                        if(mData.size() > position + 4){
                            mData.remove(position + 1);
                            mData.remove(position + 3);
                        }else{
                            Toast.makeText(getContext(), "item 已经很少了，不如先添加几个？", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
    }

    private void initListView() {
        for (int i = 0; i < 20; i++) {
            mData.add("item " + (i + 1));
        }
        MyAdapter adapter = new MyAdapter(getContext(), mData);
        mListView.setAdapter(adapter);
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
