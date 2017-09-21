package com.qmuiteam.qmui.widget.dialog;

import android.content.Context;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;

/**
 * @author cginechen
 * @date 2015-12-12
 */
public class QMUIDialogBlockBuilder extends QMUIDialogBuilder<QMUIDialogBlockBuilder> {
    private Context mContext;
    private CharSequence mContent;


    public QMUIDialogBlockBuilder(Context context) {
        super(context);
        mContext = context;
    }


    /**
     * 添加一个无图标的 Action
     */
    public QMUIDialogBlockBuilder addAction(int strRes, QMUIDialogAction.ActionListener listener) {
        return addAction(0, strRes, listener);
    }

    public QMUIDialogBlockBuilder addAction(String str, QMUIDialogAction.ActionListener listener) {
        return addAction(0, str, listener);
    }


    /**
     * 添加一个带图标的 Action
     */
    public QMUIDialogBlockBuilder addAction(int iconRes, int strRes, QMUIDialogAction.ActionListener listener) {
        return addAction(iconRes, strRes, QMUIDialogAction.ACTION_PROP_NEUTRAL, listener);
    }

    public QMUIDialogBlockBuilder addAction(int iconResId, String str, QMUIDialogAction.ActionListener listener) {
        return addAction(iconResId, str, QMUIDialogAction.ACTION_PROP_NEUTRAL, listener);
    }

    /**
     * 添加正常类型的 Action
     *
     * @param iconRes  图标
     * @param strRes   文案
     * @param prop     属性，具体请看 {@link QMUIDialogAction.Prop}
     * @param listener 事件监听
     * @return 返回 QMUIDialogBlockBuilder，可继续链式调用。
     */
    public QMUIDialogBlockBuilder addAction(int iconRes, int strRes, @QMUIDialogAction.Prop int prop, QMUIDialogAction.ActionListener listener) {
        return addAction(iconRes, mContext.getResources().getString(strRes), prop, QMUIDialogAction.ACTION_TYPE_BLOCK, listener);
    }


    public QMUIDialogBlockBuilder addAction(int iconRes, String str, @QMUIDialogAction.Prop int prop, QMUIDialogAction.ActionListener listener) {
        return addAction(iconRes, str, prop, QMUIDialogAction.ACTION_TYPE_BLOCK, listener);
    }


    public QMUIDialogBlockBuilder setContent(CharSequence content) {
        mContent = content;
        return this;
    }

    public QMUIDialogBlockBuilder setContent(int contentRes) {
        mContent = mContext.getResources().getString(contentRes);
        return this;
    }

    @Override
    protected void onCreateContent(QMUIDialog dialog, ViewGroup parent) {
        TextView contentTv = new TextView(mContext);
        contentTv.setTextColor(QMUIResHelper.getAttrColor(mContext, R.attr.qmui_config_color_gray_4));
        contentTv.setText(mContent);
        contentTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_block_content_text_size));
        contentTv.setLineSpacing(QMUIDisplayHelper.dpToPx(2), 1.0f);
        contentTv.setPadding(
                QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_padding_horizontal),
                QMUIResHelper.getAttrDimen(mContext, hasTitle() ? R.attr.qmui_dialog_content_padding_top : R.attr.qmui_dialog_content_padding_top_when_no_title),
                QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_padding_horizontal),
                QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_content_padding_bottom_when_action_block)
        );
        parent.addView(contentTv);
    }

    @Override
    protected void onCreateHandlerBar(QMUIDialog dialog, ViewGroup parent) {
        int size = mActions.size();
        if (size > 0) {
            LinearLayout layout = new LinearLayout(mContext);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.setPadding(
                    0,
                    0,
                    0,
                    QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_dialog_action_block_container_margin_bottom));


            for (int i = 0; i < mActions.size(); i++) {
                QMUIDialogAction action = mActions.get(i);
                layout.addView(action.generateActionView(mContext, dialog, i, true));
            }
            parent.addView(layout);

        }
    }

}
