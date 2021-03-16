package com.qmuiteam.qmuidemo.fragment.lab;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentContainerView;

import com.qmuiteam.qmui.arch.QMUIFragment;
import com.qmuiteam.qmui.arch.QMUINavFragment;
import com.qmuiteam.qmui.arch.SwipeBackLayout;
import com.qmuiteam.qmui.arch.record.RecordArgumentEditor;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmuidemo.fragment.home.HomeFragment;

public class QDArchNavFragment extends QMUINavFragment {
    private static final String TAG = "QDArchNavFragment";

    public static QMUINavFragment getInstance(Class<? extends QMUIFragment> firstClass, @Nullable Bundle bundle) {
        QMUINavFragment navFragment = new QDArchNavFragment();
        navFragment.setArguments(initArguments(firstClass, bundle));
        return navFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        Log.i(TAG, "1");
        if(bundle != null){
            String navTest = bundle.getString("nav_test");
            if(navTest != null){
                Log.i(TAG, "latestVisit: " + navTest);
            }
        }
    }

    @Override
    protected View onCreateView() {
        FrameLayout root = new FrameLayout(getContext());
        FragmentContainerView fragmentContainerView = new FragmentContainerView(getContext());
        TextView tipView = new TextView(getContext());
        tipView.setText("Nav");
        tipView.setBackgroundColor(Color.RED);
        tipView.setTextColor(Color.WHITE);
        root.addView(fragmentContainerView);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        root.addView(tipView, lp);
        configFragmentContainerView(fragmentContainerView);
        return root;
    }

    @Override
    public void onCollectLatestVisitArgument(RecordArgumentEditor editor) {
        editor.putString("nav_test", "nav_test");
    }

    @Override
    public Object onLastFragmentFinish() {
        return new HomeFragment();
    }

    @Override
    protected int backViewInitOffset(Context context, int dragDirection, int moveEdge) {
        if (moveEdge == SwipeBackLayout.EDGE_TOP || moveEdge == SwipeBackLayout.EDGE_BOTTOM) {
            return 0;
        }
        return QMUIDisplayHelper.dp2px(context, 100);
    }
}
