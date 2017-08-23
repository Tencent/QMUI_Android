package com.qmuiteam.qmuidemo.fragment.components.qqface.pageView;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;

import com.qmuiteam.qmui.qqface.QMUIQQFaceCompiler;
import com.qmuiteam.qmui.qqface.QMUIQQFaceView;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmuidemo.QDQQFaceManager;
import com.qmuiteam.qmuidemo.R;

/**
 * @author cginechen
 * @date 2017-06-08
 */

public class QDQQFacePagerView extends QDQQFaceBasePagerView {
    public QDQQFacePagerView(Context context) {
        super(context);
    }

    @Override
    protected View getView(int position, View convertView, ViewGroup parent) {
        QMUIQQFaceView qmuiqqFaceView;
        if (convertView == null || !(convertView instanceof QMUIQQFaceView)) {
            qmuiqqFaceView = new QMUIQQFaceView(getContext());
            qmuiqqFaceView.setCompiler(QMUIQQFaceCompiler.getInstance(
                    QDQQFaceManager.getInstance()));
            int padding = QMUIDisplayHelper.dp2px(getContext(), 16);
            ViewCompat.setBackground(qmuiqqFaceView, QMUIResHelper.getAttrDrawable(
                    getContext(), R.attr.qmui_s_list_item_bg_with_border_bottom));
            qmuiqqFaceView.setPadding(padding, padding, padding, padding);
            qmuiqqFaceView.setLineSpace(QMUIDisplayHelper.dp2px(getContext(), 10));
            qmuiqqFaceView.setTextColor(Color.BLACK);
            qmuiqqFaceView.setMaxLine(8);
            convertView = qmuiqqFaceView;
        } else {
            qmuiqqFaceView = (QMUIQQFaceView) convertView;
        }
        qmuiqqFaceView.setText(getItem(position));
        return convertView;
    }
}
