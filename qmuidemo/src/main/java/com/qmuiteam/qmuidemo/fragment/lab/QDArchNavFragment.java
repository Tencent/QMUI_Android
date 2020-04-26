package com.qmuiteam.qmuidemo.fragment.lab;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.qmuiteam.qmui.arch.QMUIFragment;
import com.qmuiteam.qmui.arch.QMUINavFragment;
import com.qmuiteam.qmui.arch.record.RecordArgumentEditor;
import com.qmuiteam.qmuidemo.fragment.home.HomeFragment;

public class QDArchNavFragment extends QMUINavFragment {
    private static final String TAG = "QDArchNavFragment";

    @Nullable
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
    public void onCollectLatestVisitArgument(RecordArgumentEditor editor) {
        editor.putString("nav_test", "nav_test");
    }

    @Override
    public Object onLastFragmentFinish() {
        return new HomeFragment();
    }
}
