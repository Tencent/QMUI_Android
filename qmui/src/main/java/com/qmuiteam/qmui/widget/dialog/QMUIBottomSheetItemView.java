package com.qmuiteam.qmui.widget.dialog;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.alpha.QMUIAlphaLinearLayout;
import com.qmuiteam.qmui.alpha.QMUIAlphaViewHelper;

/**
 * QMUIBottomSheet 的ItemView
 * @author zander
 * @date 2017-12-05
 */
public class QMUIBottomSheetItemView extends QMUIAlphaLinearLayout {

    private AppCompatImageView mAppCompatImageView;
    private ViewStub mSubScript;
    private TextView mTextView;


    public QMUIBottomSheetItemView(Context context) {
        super(context);
    }

    public QMUIBottomSheetItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QMUIBottomSheetItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAppCompatImageView = (AppCompatImageView) findViewById(R.id.grid_item_image);
        mSubScript = (ViewStub) findViewById(R.id.grid_item_subscript);
        mTextView = (TextView) findViewById(R.id.grid_item_title);
    }

    public AppCompatImageView getAppCompatImageView() {
        return mAppCompatImageView;
    }

    public TextView getTextView() {
        return mTextView;
    }

    public ViewStub getSubScript() {
        return mSubScript;
    }
}
