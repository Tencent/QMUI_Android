/*
 * Tencent is pleased to support the open source community by making QMUI_Android available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qmuiteam.qmuidemo.fragment.components;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsCompat;

import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord;
import com.qmuiteam.qmui.layout.QMUIFrameLayout;
import com.qmuiteam.qmui.skin.QMUISkinHelper;
import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIKeyboardHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.util.QMUIWindowInsetHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.popup.QMUIFullScreenPopup;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;
import com.qmuiteam.qmui.widget.popup.QMUIPopups;
import com.qmuiteam.qmui.widget.popup.QMUIQuickAction;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.lib.annotation.Widget;
import com.qmuiteam.qmuidemo.manager.QDDataManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author cginechen
 * @date 2017-03-27
 */

@Widget(widgetClass = QMUIPopups.class, iconRes = R.mipmap.icon_grid_popup)
@LatestVisitRecord
public class QDPopupFragment extends BaseFragment {

    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;

    private QMUIPopup mNormalPopup;

    @OnClick(R.id.actionBtn1)
    void onClickBtn1(View v) {
        TextView textView = new TextView(getContext());
        textView.setLineSpacing(QMUIDisplayHelper.dp2px(getContext(), 4), 1.0f);
        int padding = QMUIDisplayHelper.dp2px(getContext(), 20);
        textView.setPadding(padding, padding, padding, padding);
        textView.setText("QMUIBasePopup 可以设置其位置以及显示和隐藏的动画");
        textView.setTextColor(
                QMUIResHelper.getAttrColor(getContext(), R.attr.app_skin_common_title_text_color));
        QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
        builder.textColor(R.attr.app_skin_common_title_text_color);
        QMUISkinHelper.setSkinValue(textView, builder);
        builder.release();
        mNormalPopup = QMUIPopups.popup(getContext(), QMUIDisplayHelper.dp2px(getContext(), 250))
                .preferredDirection(QMUIPopup.DIRECTION_BOTTOM)
                .view(textView)
                .skinManager(QMUISkinManager.defaultInstance(getContext()))
                .edgeProtection(QMUIDisplayHelper.dp2px(getContext(), 20))
                .offsetX(QMUIDisplayHelper.dp2px(getContext(), 20))
                .offsetYIfBottom(QMUIDisplayHelper.dp2px(getContext(), 5))
                .shadow(true)
                .arrow(true)
                .animStyle(QMUIPopup.ANIM_GROW_FROM_CENTER)
                .onDismiss(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        Toast.makeText(getContext(), "onDismiss", Toast.LENGTH_SHORT).show();
                    }
                })
                .show(v);
    }

    @OnClick(R.id.actionBtn2)
    void onClickBtn2(View v) {
        String[] listItems = new String[]{
                "Item 1",
                "Item 2",
                "Item 3",
                "Item 4",
                "Item 5",
                "Item 6",
                "Item 7",
                "Item 8",
        };
        List<String> data = new ArrayList<>();

        Collections.addAll(data, listItems);

        ArrayAdapter adapter = new ArrayAdapter<>(getContext(), R.layout.simple_list_item, data);
        AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getActivity(), "Item " + (i + 1), Toast.LENGTH_SHORT).show();
                if (mNormalPopup != null) {
                    mNormalPopup.dismiss();
                }
            }
        };
        mNormalPopup = QMUIPopups.listPopup(getContext(),
                QMUIDisplayHelper.dp2px(getContext(), 250),
                QMUIDisplayHelper.dp2px(getContext(), 300),
                adapter,
                onItemClickListener)
                .animStyle(QMUIPopup.ANIM_GROW_FROM_CENTER)
                .preferredDirection(QMUIPopup.DIRECTION_TOP)
                .shadow(true)
                .offsetYIfTop(QMUIDisplayHelper.dp2px(getContext(), 5))
                .skinManager(QMUISkinManager.defaultInstance(getContext()))
                .onDismiss(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        Toast.makeText(getContext(), "onDismiss", Toast.LENGTH_SHORT).show();
                    }
                })
                .show(v);
    }

    @OnClick(R.id.actionBtn3)
    void onClickBtn3(View v) {
        TextView textView = new TextView(getContext());
        textView.setLineSpacing(QMUIDisplayHelper.dp2px(getContext(), 4), 1.0f);
        int padding = QMUIDisplayHelper.dp2px(getContext(), 20);
        textView.setPadding(padding, padding, padding, padding);
        textView.setText("通过 dimAmount() 设置背景遮罩");
        textView.setTextColor(
                QMUIResHelper.getAttrColor(getContext(), R.attr.app_skin_common_title_text_color));
        QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
        builder.textColor(R.attr.app_skin_common_title_text_color);
        QMUISkinHelper.setSkinValue(textView, builder);
        builder.release();
        mNormalPopup = QMUIPopups.popup(getContext(), QMUIDisplayHelper.dp2px(getContext(), 250))
                .preferredDirection(QMUIPopup.DIRECTION_BOTTOM)
                .view(textView)
                .edgeProtection(QMUIDisplayHelper.dp2px(getContext(), 20))
                .dimAmount(0.6f)
                .animStyle(QMUIPopup.ANIM_GROW_FROM_CENTER)
                .skinManager(QMUISkinManager.defaultInstance(getContext()))
                .onDismiss(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        Toast.makeText(getContext(), "onDismiss", Toast.LENGTH_SHORT).show();
                    }
                })
                .show(v);
    }

    @OnClick(R.id.actionBtn4)
    void onClickBtn4(View v) {
        final TextView textView = new TextView(getContext());
        textView.setLineSpacing(QMUIDisplayHelper.dp2px(getContext(), 4), 1.0f);
        int padding = QMUIDisplayHelper.dp2px(getContext(), 20);
        textView.setPadding(padding, padding, padding, padding);
        textView.setText("加载中...");
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.app_color_description));
        mNormalPopup = QMUIPopups.popup(getContext(), QMUIDisplayHelper.dp2px(getContext(), 250))
                .preferredDirection(QMUIPopup.DIRECTION_BOTTOM)
                .view(textView)
                .edgeProtection(QMUIDisplayHelper.dp2px(getContext(), 20))
                .dimAmount(0.6f)
                .skinManager(QMUISkinManager.defaultInstance(getContext()))
                .animStyle(QMUIPopup.ANIM_GROW_FROM_CENTER)
                .onDismiss(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        Toast.makeText(getContext(), "onDismiss", Toast.LENGTH_SHORT).show();
                    }
                })
                .show(v);

        // 这里只是演示，实际情况应该考虑数据加载完成而 Popup 被 dismiss 的情况
        textView.postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.setText("使用 Popup 最好是一开始就确定内容宽高，" +
                        "如果宽高位置会变化，系统会有一个的移动动画不受控制，体验并不好");
            }
        }, 2000);
    }

    @OnClick(R.id.actionBtn5)
    void onClickBtn5(View v) {
        QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
        QMUIFrameLayout frameLayout = new QMUIFrameLayout(getContext());
        frameLayout.setBackground(
                QMUIResHelper.getAttrDrawable(getContext(), R.attr.qmui_skin_support_popup_bg));
        builder.background(R.attr.qmui_skin_support_popup_bg);
        QMUISkinHelper.setSkinValue(frameLayout, builder);
        frameLayout.setRadius(QMUIDisplayHelper.dp2px(getContext(), 12));
        int padding = QMUIDisplayHelper.dp2px(getContext(), 20);
        frameLayout.setPadding(padding, padding, padding, padding);

        TextView textView = new TextView(getContext());
        textView.setLineSpacing(QMUIDisplayHelper.dp2px(getContext(), 4), 1.0f);
        textView.setPadding(padding, padding, padding, padding);
        textView.setText("这是自定义显示的内容");
        textView.setTextColor(
                QMUIResHelper.getAttrColor(getContext(), R.attr.app_skin_common_title_text_color));

        builder.clear();
        builder.textColor(R.attr.app_skin_common_title_text_color);
        QMUISkinHelper.setSkinValue(textView, builder);
        textView.setGravity(Gravity.CENTER);

        builder.release();

        int size = QMUIDisplayHelper.dp2px(getContext(), 200);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);
        frameLayout.addView(textView, lp);

        QMUIPopups.fullScreenPopup(getContext())
                .addView(frameLayout)
                .closeBtn(true)
                .skinManager(QMUISkinManager.defaultInstance(getContext()))
                .onBlankClick(new QMUIFullScreenPopup.OnBlankClickListener() {
                    @Override
                    public void onBlankClick(QMUIFullScreenPopup popup) {
                        Toast.makeText(getContext(), "点击到空白区域", Toast.LENGTH_SHORT).show();
                    }
                })
                .onDismiss(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        Toast.makeText(getContext(), "onDismiss", Toast.LENGTH_SHORT).show();
                    }
                })
                .show(v);
    }

    @OnClick(R.id.actionBtn6)
    void onClickBtn6(View v) {
        QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
        QMUIFrameLayout frameLayout = new QMUIFrameLayout(getContext());
        frameLayout.setBackground(
                QMUIResHelper.getAttrDrawable(getContext(), R.attr.qmui_skin_support_popup_bg));
        builder.background(R.attr.qmui_skin_support_popup_bg);
        QMUISkinHelper.setSkinValue(frameLayout, builder);
        frameLayout.setRadius(QMUIDisplayHelper.dp2px(getContext(), 12));
        int padding = QMUIDisplayHelper.dp2px(getContext(), 20);
        frameLayout.setPadding(padding, padding, padding, padding);
        QMUIKeyboardHelper.listenKeyBoardWithOffsetSelfHalf(frameLayout, true);

        TextView textView = new TextView(getContext());
        textView.setLineSpacing(QMUIDisplayHelper.dp2px(getContext(), 4), 1.0f);
        textView.setPadding(padding, padding, padding, padding);
        textView.setText("这是自定义显示的内容");
        builder.clear();
        builder.textColor(R.attr.app_skin_common_title_text_color);
        QMUISkinHelper.setSkinValue(textView, builder);
        textView.setGravity(Gravity.CENTER);
        int size = QMUIDisplayHelper.dp2px(getContext(), 200);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);
        frameLayout.addView(textView, lp);

        final FrameLayout editFitSystemWindowWrapped = new FrameLayout(getContext());
        editFitSystemWindowWrapped.setFitsSystemWindows(true);
        QMUIWindowInsetHelper.handleWindowInsets(editFitSystemWindowWrapped,
                WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.displayCutout(), true);
        QMUIKeyboardHelper.listenKeyBoardWithOffsetSelf(editFitSystemWindowWrapped, true);

        int minHeight = QMUIDisplayHelper.dp2px(getContext(), 48);
        QMUIFrameLayout editParent = new QMUIFrameLayout(getContext());
        editParent.setMinimumHeight(minHeight);
        editParent.setRadius(minHeight / 2);
        editParent.setBackground(
                QMUIResHelper.getAttrDrawable(getContext(), R.attr.qmui_skin_support_popup_bg));
        builder.clear();
        builder.background(R.attr.qmui_skin_support_popup_bg);
        QMUISkinHelper.setSkinValue(editParent, builder);


        EditText editText = new EditText(getContext());
        editText.setHint("请输入...");
        editText.setBackground(null);
        builder.clear();
        builder.hintColor(R.attr.app_skin_common_desc_text_color);
        builder.textColor(R.attr.app_skin_common_title_text_color);
        QMUISkinHelper.setSkinValue(editText, builder);
        int paddingHor = QMUIDisplayHelper.dp2px(getContext(), 20);
        int paddingVer = QMUIDisplayHelper.dp2px(getContext(), 10);
        editText.setPadding(paddingHor, paddingVer, paddingHor, paddingVer);
        editText.setMaxHeight(QMUIDisplayHelper.dp2px(getContext(), 100));

        FrameLayout.LayoutParams editLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editLp.gravity = Gravity.CENTER_HORIZONTAL;
        editParent.addView(editText, editLp);
        editFitSystemWindowWrapped.addView(editParent,  new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ConstraintLayout.LayoutParams eLp = new ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        int mar = QMUIDisplayHelper.dp2px(getContext(), 20);
        eLp.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        eLp.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        eLp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        eLp.leftMargin = mar;
        eLp.rightMargin = mar;
        eLp.bottomMargin = mar;

        QMUIPopups.fullScreenPopup(getContext())
                .addView(frameLayout)
                .addView(editFitSystemWindowWrapped, eLp)
                .skinManager(QMUISkinManager.defaultInstance(getContext()))
                .onBlankClick(new QMUIFullScreenPopup.OnBlankClickListener() {
                    @Override
                    public void onBlankClick(QMUIFullScreenPopup popup) {
                        popup.dismiss();
                        Toast.makeText(getContext(), "点击到空白区域", Toast.LENGTH_SHORT).show();
                    }
                })
                .onDismiss(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        Toast.makeText(getContext(), "onDismiss", Toast.LENGTH_SHORT).show();
                    }
                })
                .show(v);
    }

    @OnClick(R.id.actionBtn7)
    void onClickBtn7(View v) {
        QMUIPopups.quickAction(getContext(),
                QMUIDisplayHelper.dp2px(getContext(), 56),
                QMUIDisplayHelper.dp2px(getContext(), 56))
                .shadow(true)
                .skinManager(QMUISkinManager.defaultInstance(getContext()))
                .edgeProtection(QMUIDisplayHelper.dp2px(getContext(), 20))
                .addAction(new QMUIQuickAction.Action().icon(R.drawable.icon_quick_action_copy).text("复制").onClick(
                        new QMUIQuickAction.OnClickListener() {
                            @Override
                            public void onClick(QMUIQuickAction quickAction, QMUIQuickAction.Action action, int position) {
                                quickAction.dismiss();
                                Toast.makeText(getContext(), "复制成功", Toast.LENGTH_SHORT).show();
                            }
                        }
                ))
                .addAction(new QMUIQuickAction.Action().icon(R.drawable.icon_quick_action_line).text("划线").onClick(
                        new QMUIQuickAction.OnClickListener() {
                            @Override
                            public void onClick(QMUIQuickAction quickAction, QMUIQuickAction.Action action, int position) {
                                quickAction.dismiss();
                                Toast.makeText(getContext(), "划线成功", Toast.LENGTH_SHORT).show();
                            }
                        }
                ))
                .addAction(new QMUIQuickAction.Action().icon(R.drawable.icon_quick_action_share).text("分享").onClick(
                        new QMUIQuickAction.OnClickListener() {
                            @Override
                            public void onClick(QMUIQuickAction quickAction, QMUIQuickAction.Action action, int position) {
                                quickAction.dismiss();
                                Toast.makeText(getContext(), "分享成功", Toast.LENGTH_SHORT).show();
                            }
                        }
                ))
                .show(v);
    }

    @OnClick(R.id.actionBtn8)
    void onClickBtn8(View v) {
        QMUIPopups.quickAction(getContext(),
                QMUIDisplayHelper.dp2px(getContext(), 56),
                QMUIDisplayHelper.dp2px(getContext(), 56))
                .shadow(true)
                .skinManager(QMUISkinManager.defaultInstance(getContext()))
                .edgeProtection(QMUIDisplayHelper.dp2px(getContext(), 20))
                .addAction(new QMUIQuickAction.Action().icon(R.drawable.icon_quick_action_copy).text("复制").onClick(
                        new QMUIQuickAction.OnClickListener() {
                            @Override
                            public void onClick(QMUIQuickAction quickAction, QMUIQuickAction.Action action, int position) {
                                quickAction.dismiss();
                                Toast.makeText(getContext(), "复制成功", Toast.LENGTH_SHORT).show();
                            }
                        }
                ))
                .addAction(new QMUIQuickAction.Action().icon(R.drawable.icon_quick_action_line).text("划线").onClick(
                        new QMUIQuickAction.OnClickListener() {
                            @Override
                            public void onClick(QMUIQuickAction quickAction, QMUIQuickAction.Action action, int position) {
                                quickAction.dismiss();
                                Toast.makeText(getContext(), "划线成功", Toast.LENGTH_SHORT).show();
                            }
                        }
                ))
                .addAction(new QMUIQuickAction.Action().icon(R.drawable.icon_quick_action_share).text("分享").onClick(
                        new QMUIQuickAction.OnClickListener() {
                            @Override
                            public void onClick(QMUIQuickAction quickAction, QMUIQuickAction.Action action, int position) {
                                quickAction.dismiss();
                                Toast.makeText(getContext(), "分享成功", Toast.LENGTH_SHORT).show();
                            }
                        }
                ))
                .addAction(new QMUIQuickAction.Action().icon(R.drawable.icon_quick_action_delete_line).text("删除划线").onClick(
                        new QMUIQuickAction.OnClickListener() {
                            @Override
                            public void onClick(QMUIQuickAction quickAction, QMUIQuickAction.Action action, int position) {
                                quickAction.dismiss();
                                Toast.makeText(getContext(), "删除划线成功", Toast.LENGTH_SHORT).show();
                            }
                        }
                ))
                .addAction(new QMUIQuickAction.Action().icon(R.drawable.icon_quick_action_dict).text("词典").onClick(
                        new QMUIQuickAction.OnClickListener() {
                            @Override
                            public void onClick(QMUIQuickAction quickAction, QMUIQuickAction.Action action, int position) {
                                quickAction.dismiss();
                                Toast.makeText(getContext(), "打开词典", Toast.LENGTH_SHORT).show();
                            }
                        }
                ))
                .addAction(new QMUIQuickAction.Action().icon(R.drawable.icon_quick_action_share).text("圈圈").onClick(
                        new QMUIQuickAction.OnClickListener() {
                            @Override
                            public void onClick(QMUIQuickAction quickAction, QMUIQuickAction.Action action, int position) {
                                quickAction.dismiss();
                                Toast.makeText(getContext(), "查询成功", Toast.LENGTH_SHORT).show();
                            }
                        }
                ))
                .addAction(new QMUIQuickAction.Action().icon(R.drawable.icon_quick_action_dict).text("查询").onClick(
                        new QMUIQuickAction.OnClickListener() {
                            @Override
                            public void onClick(QMUIQuickAction quickAction, QMUIQuickAction.Action action, int position) {
                                quickAction.dismiss();
                                Toast.makeText(getContext(), "查询成功", Toast.LENGTH_SHORT).show();
                            }
                        }
                ))
                .show(v);
    }

    @Override
    protected View onCreateView() {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_popup, null);
        ButterKnife.bind(this, root);
        initTopBar();
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
    }
}
