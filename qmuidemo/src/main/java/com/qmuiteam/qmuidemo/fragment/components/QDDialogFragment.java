package com.qmuiteam.qmuidemo.fragment.components;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIKeyboardHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;
import com.qmuiteam.qmuidemo.model.QDItemDescription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * {@link QMUIDialog} 的使用示例。
 * Created by cgspine on 15/9/15.
 */
@Widget(widgetClass = QMUIDialog.class, iconRes = R.mipmap.icon_grid_dialog)
public class QDDialogFragment extends BaseFragment {

    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.listview)
    ListView mListView;

    private QDItemDescription mQDItemDescription;
    private int mCurrentDialogStyle = com.qmuiteam.qmui.R.style.QMUI_Dialog;

    @Override
    protected View onCreateView() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_listview, null);
        ButterKnife.bind(this, view);
        mQDItemDescription = QDDataManager.getInstance().getDescription(this.getClass());
        initTopBar();
        initListView();
        return view;
    }

    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        mTopBar.addRightImageButton(R.mipmap.icon_topbar_overflow, R.id.topbar_right_change_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showBottomSheet();
                    }
                });

        mTopBar.setTitle(mQDItemDescription.getName());
    }


    private void initListView() {
        String[] listItems = new String[]{
                "消息类型对话框（蓝色按钮）",
                "消息类型对话框（红色按钮）",
                "消息类型对话框 (很长文案)",
                "菜单类型对话框",
                "带 Checkbox 的消息确认框",
                "单选菜单类型对话框",
                "多选菜单类型对话框",
                "多选菜单类型对话框(item 数量很多)",
                "带输入框的对话框",
                "高度适应键盘升降的对话框"
        };
        List<String> data = new ArrayList<>();

        Collections.addAll(data, listItems);

        mListView.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.simple_list_item, data));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        showMessagePositiveDialog();
                        break;
                    case 1:
                        showMessageNegativeDialog();
                        break;
                    case 2:
                        showLongMessageDialog();
                        break;
                    case 3:
                        showMenuDialog();
                        break;
                    case 4:
                        showConfirmMessageDialog();
                        break;
                    case 5:
                        showSingleChoiceDialog();
                        break;
                    case 6:
                        showMultiChoiceDialog();
                        break;
                    case 7:
                        showNumerousMultiChoiceDialog();
                        break;
                    case 8:
                        showEditTextDialog();
                        break;
                    case 9:
                        showAutoDialog();
                        break;
                }
            }
        });
    }

    // ================================ 生成不同类型的对话框
    private void showMessagePositiveDialog() {
        new QMUIDialog.MessageDialogBuilder(getActivity())
                .setTitle("标题")
                .setMessage("确定要发送吗？")
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("确定", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        Toast.makeText(getActivity(), "发送成功", Toast.LENGTH_SHORT).show();
                    }
                })
                .create(mCurrentDialogStyle).show();
    }

    private void showMessageNegativeDialog() {
        new QMUIDialog.MessageDialogBuilder(getActivity())
                .setTitle("标题")
                .setMessage("确定要删除吗？")
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(0, "删除", QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        Toast.makeText(getActivity(), "删除成功", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .create(mCurrentDialogStyle).show();
    }

    private void showLongMessageDialog() {
        new QMUIDialog.MessageDialogBuilder(getActivity())
                .setTitle("标题")
                .setMessage("这是一段很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长" +
                        "很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很" +
                        "很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很" +
                        "很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很" +
                        "长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长" +
                        "很长很长很长很长很很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长" +
                        "很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长" +
                        "很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长" +
                        "很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长" +
                        "很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长" +
                        "很长很长很长很长很长很长很长很长很长很长很长很长很长很长长很长的文案")
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .create(mCurrentDialogStyle).show();
    }

    private void showConfirmMessageDialog() {
        new QMUIDialog.CheckBoxMessageDialogBuilder(getActivity())
                .setTitle("退出后是否删除账号信息?")
                .setMessage("删除账号信息")
                .setChecked(true)
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("退出", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .create(mCurrentDialogStyle).show();
    }

    private void showMenuDialog() {
        final String[] items = new String[]{"选项1", "选项2", "选项3"};
        new QMUIDialog.MenuDialogBuilder(getActivity())
                .addItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "你选择了 " + items[which], Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .create(mCurrentDialogStyle).show();
    }

    private void showSingleChoiceDialog() {
        final String[] items = new String[]{"选项1", "选项2", "选项3"};
        final int checkedIndex = 1;
        new QMUIDialog.CheckableDialogBuilder(getActivity())
                .setCheckedIndex(checkedIndex)
                .addItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "你选择了 " + items[which], Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .create(mCurrentDialogStyle).show();
    }

    private void showMultiChoiceDialog() {
        final String[] items = new String[]{"选项1", "选项2", "选项3", "选项4", "选项5", "选项6"};
        final QMUIDialog.MultiCheckableDialogBuilder builder = new QMUIDialog.MultiCheckableDialogBuilder(getActivity())
                .setCheckedItems(new int[]{1, 3})
                .addItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.addAction("取消", new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                dialog.dismiss();
            }
        });
        builder.addAction("提交", new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                String result = "你选择了 ";
                for (int i = 0; i < builder.getCheckedItemIndexes().length; i++) {
                    result += "" + builder.getCheckedItemIndexes()[i] + "; ";
                }
                Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        builder.create(mCurrentDialogStyle).show();
    }

    private void showNumerousMultiChoiceDialog() {
        final String[] items = new String[]{
                "选项1", "选项2", "选项3", "选项4", "选项5", "选项6",
                "选项7", "选项8", "选项9", "选项10", "选项11", "选项12",
                "选项13", "选项14", "选项15", "选项16", "选项17", "选项18"
        };
        final QMUIDialog.MultiCheckableDialogBuilder builder = new QMUIDialog.MultiCheckableDialogBuilder(getActivity())
                .setCheckedItems(new int[]{1, 3})
                .addItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.addAction("取消", new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                dialog.dismiss();
            }
        });
        builder.addAction("提交", new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                String result = "你选择了 ";
                for (int i = 0; i < builder.getCheckedItemIndexes().length; i++) {
                    result += "" + builder.getCheckedItemIndexes()[i] + "; ";
                }
                Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        builder.create(mCurrentDialogStyle).show();
    }

    private void showEditTextDialog() {
        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getActivity());
        builder.setTitle("标题")
                .setPlaceholder("在此输入您的昵称")
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("确定", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        CharSequence text = builder.getEditText().getText();
                        if (text != null && text.length() > 0) {
                            Toast.makeText(getActivity(), "您的昵称: " + text, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getActivity(), "请填入昵称", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .create(mCurrentDialogStyle).show();
    }

    private void showAutoDialog() {
        QMAutoTestDialogBuilder autoTestDialogBuilder = (QMAutoTestDialogBuilder) new QMAutoTestDialogBuilder(getActivity())
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("确定", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        Toast.makeText(getActivity(), "你点了确定", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
        autoTestDialogBuilder.create(mCurrentDialogStyle).show();
        QMUIKeyboardHelper.showKeyboard(autoTestDialogBuilder.getEditText(), true);
    }

    class QMAutoTestDialogBuilder extends QMUIDialog.AutoResizeDialogBuilder {
        private Context mContext;
        private EditText mEditText;

        public QMAutoTestDialogBuilder(Context context) {
            super(context);
            mContext = context;
        }

        public EditText getEditText() {
            return mEditText;
        }

        @Override
        public View onBuildContent(QMUIDialog dialog, ScrollView parent) {
            LinearLayout layout = new LinearLayout(mContext);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            int padding = QMUIDisplayHelper.dp2px(mContext, 20);
            layout.setPadding(padding, padding, padding, padding);
            mEditText = new EditText(mContext);
            QMUIViewHelper.setBackgroundKeepingPadding(mEditText, QMUIResHelper.getAttrDrawable(mContext, R.attr.qmui_list_item_bg_with_border_bottom));
            mEditText.setHint("输入框");
            LinearLayout.LayoutParams editTextLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, QMUIDisplayHelper.dpToPx(50));
            editTextLP.bottomMargin = QMUIDisplayHelper.dp2px(getContext(), 15);
            mEditText.setLayoutParams(editTextLP);
            layout.addView(mEditText);
            TextView textView = new TextView(mContext);
            textView.setLineSpacing(QMUIDisplayHelper.dp2px(getContext(), 4), 1.0f);
            textView.setText("观察聚焦输入框后，键盘升起降下时 dialog 的高度自适应变化。\n\n" +
                    "QMUI Android 的设计目的是用于辅助快速搭建一个具备基本设计还原效果的 Android 项目，" +
                    "同时利用自身提供的丰富控件及兼容处理，让开发者能专注于业务需求而无需耗费精力在基础代码的设计上。" +
                    "不管是新项目的创建，或是已有项目的维护，均可使开发效率和项目质量得到大幅度提升。");
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.app_color_description));
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.addView(textView);
            return layout;
        }
    }


    private void showBottomSheet() {
        new QMUIBottomSheet.BottomListSheetBuilder(getContext())
                .addItem("使用 QMUI 默认 Dialog 样式")
                .addItem("自定义样式")
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        switch (position) {
                            case 0:
                                mCurrentDialogStyle = com.qmuiteam.qmui.R.style.QMUI_Dialog;
                                break;
                            case 1:
                                mCurrentDialogStyle = R.style.DialogTheme2;
                                break;
                        }
                        dialog.dismiss();
                    }
                })
                .build().show();
    }
}
