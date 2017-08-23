package com.qmuiteam.qmuidemo.fragment.home;

import android.content.Context;

import com.qmuiteam.qmuidemo.QDDataManager;

/**
 * Created by Kayo on 2016/11/21.
 */

public class HomeUtilController extends HomeController {

    public HomeUtilController(Context context) {
        super(context);
    }

    @Override
    protected String getTitle() {
        return "Helper";
    }

    @Override
    protected ItemAdapter getItemAdapter() {
        return new ItemAdapter(getContext(), QDDataManager.getInstance().getUtilDescriptions());
    }
}
