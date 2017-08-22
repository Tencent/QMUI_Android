package com.qmuiteam.qmuidemo.fragment.components;

import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.popup.QMUIListPopup;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;
import com.qmuiteam.qmuidemo.QDDataManager;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * @author cginechen
 * @date 2017-03-27
 */

@Widget(widgetClass = QMUIPopup.class, iconRes = R.mipmap.icon_grid_popup)
public class QDPopupFragment extends BaseFragment {

    @BindView(R.id.topbar) QMUITopBar mTopBar;
    @BindView(R.id.actiontBtn1) Button mActionButton1;
    @BindView(R.id.actiontBtn2) Button mActionButton2;

    private QMUIPopup mNormalPopup;
    private QMUIListPopup mListPopup;

    @OnClick({R.id.actiontBtn1, R.id.actiontBtn2})
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.actiontBtn1:
                initNormalPopupIfNeed();
                mNormalPopup.setAnimStyle(QMUIPopup.ANIM_GROW_FROM_CENTER);
                mNormalPopup.setPreferredDirection(QMUIPopup.DIRECTION_TOP);
                mNormalPopup.show(v);
                mActionButton1.setText(getContext().getResources().getString(R.string.popup_normal_action_button_text_hide));
                break;
            case R.id.actiontBtn2:
                initListPopupIfNeed();
                mListPopup.setAnimStyle(QMUIPopup.ANIM_GROW_FROM_CENTER);
                mListPopup.setPreferredDirection(QMUIPopup.DIRECTION_TOP);
                mListPopup.show(v);
                mActionButton2.setText(getContext().getResources().getString(R.string.popup_list_action_button_text_hide));
                break;
        }
    }

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_popup, null);
        ButterKnife.bind(this, root);
        initTopBar();
        return root;
    }

    private void initNormalPopupIfNeed() {
        if (mNormalPopup == null) {
            mNormalPopup = new QMUIPopup(getContext(), QMUIPopup.DIRECTION_NONE);
            TextView textView = new TextView(getContext());
            textView.setLayoutParams(mNormalPopup.generateLayoutParam(
                    QMUIDisplayHelper.dp2px(getContext(), 250),
                    WRAP_CONTENT
            ));
            textView.setLineSpacing(QMUIDisplayHelper.dp2px(getContext(), 4), 1.0f);
            int padding = QMUIDisplayHelper.dp2px(getContext(), 20);
            textView.setPadding(padding, padding, padding, padding);
            textView.setText("Popup 可以设置其位置以及显示和隐藏的动画");
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.app_color_description));
            mNormalPopup.setContentView(textView);
            mNormalPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    mActionButton1.setText(getContext().getResources().getString(R.string.popup_normal_action_button_text_show));
                }
            });
        }
    }

    private void initListPopupIfNeed() {
        if (mListPopup == null) {

            String[] listItems = new String[]{
                    "Item 1",
                    "Item 2",
                    "Item 3",
                    "Item 4",
                    "Item 5",
            };
            List<String> data = new ArrayList<>();

            for (String listItem : listItems) {
                data.add(listItem);
            }

            ArrayAdapter adapter = new ArrayAdapter<>(getActivity(), R.layout.simple_list_item, data);

            mListPopup = new QMUIListPopup(getContext(), QMUIPopup.DIRECTION_NONE, adapter);
            mListPopup.create(QMUIDisplayHelper.dp2px(getContext(), 250), QMUIDisplayHelper.dp2px(getContext(), 200), new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Toast.makeText(getActivity(), "Item " + (i + 1), Toast.LENGTH_SHORT).show();
                    mListPopup.dismiss();
                }
            });
            mListPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    mActionButton2.setText(getContext().getResources().getString(R.string.popup_list_action_button_text_show));
                }
            });
        }
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        mTopBar.setTitle(QDDataManager.getInstance().getName(this.getClass()));
    }
}
