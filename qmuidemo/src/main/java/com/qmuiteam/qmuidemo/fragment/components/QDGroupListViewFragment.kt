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
package com.qmuiteam.qmuidemo.fragment.components

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.ButterKnife
import com.qmuiteam.qmui.exposure.simpleExposure
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import com.qmuiteam.qmui.util.QMUIResHelper
import com.qmuiteam.qmui.widget.QMUILoadingView
import com.qmuiteam.qmui.widget.QMUITopBarLayout
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView.SkinConfig
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView
import com.qmuiteam.qmuidemo.R
import com.qmuiteam.qmuidemo.base.BaseFragment
import com.qmuiteam.qmuidemo.lib.annotation.Widget
import com.qmuiteam.qmuidemo.manager.QDDataManager
import com.qmuiteam.qmuidemo.model.QDItemDescription

/**
 * [QMUIGroupListView] 的使用示例。
 * Created by Kayo on 2016/11/21.
 */
@Widget(widgetClass = QMUIGroupListView::class, iconRes = R.mipmap.icon_grid_group_list_view)
class QDGroupListViewFragment : BaseFragment() {
    @JvmField
    @BindView(R.id.topbar)
    var mTopBar: QMUITopBarLayout? = null

    @JvmField
    @BindView(R.id.groupListView)
    var mGroupListView: QMUIGroupListView? = null
    private var mQDItemDescription: QDItemDescription? = null
    override fun onCreateView(): View {
        val root = LayoutInflater.from(activity).inflate(R.layout.fragment_grouplistview, null)
        ButterKnife.bind(this, root)
        mQDItemDescription = QDDataManager.getInstance().getDescription(this.javaClass)
        initTopBar()
        initGroupListView()
        return root
    }

    private fun initTopBar() {
        mTopBar!!.addLeftBackImageButton().setOnClickListener { popBackStack() }
        mTopBar!!.setTitle(mQDItemDescription!!.name)
    }

    private fun initGroupListView() {
        val normalItem = mGroupListView!!.createItemView(
            ContextCompat.getDrawable(requireContext(), R.mipmap.about_logo),
            "Item 1",
            null,
            QMUICommonListItemView.HORIZONTAL,
            QMUICommonListItemView.ACCESSORY_TYPE_NONE
        )
        normalItem.orientation = QMUICommonListItemView.VERTICAL
        val itemWithDetail = mGroupListView!!.createItemView(
            ContextCompat.getDrawable(requireContext(), R.mipmap.example_image0),
            "Item 2",
            null,
            QMUICommonListItemView.HORIZONTAL,
            QMUICommonListItemView.ACCESSORY_TYPE_NONE
        )

        // 去除 icon 的 tintColor 换肤设置
        val skinConfig = SkinConfig()
        skinConfig.iconTintColorRes = 0
        itemWithDetail.setSkinConfig(skinConfig)
        itemWithDetail.detailText = "在右方的详细信息"
        val itemWithDetailBelow = mGroupListView!!.createItemView("Item 3")
        itemWithDetailBelow.simpleExposure(key = "") { type ->
            Log.i("exposure", "simple exposure: $type")
        }
        itemWithDetailBelow.orientation = QMUICommonListItemView.VERTICAL
        itemWithDetailBelow.detailText = "在标题下方的详细信息"
        val itemWithChevron = mGroupListView!!.createItemView("Item 4")
        itemWithChevron.accessoryType = QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON
        val itemWithSwitch = mGroupListView!!.createItemView("Item 5")
        itemWithSwitch.accessoryType = QMUICommonListItemView.ACCESSORY_TYPE_SWITCH
        itemWithSwitch.switch.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(
                activity,
                "checked = $isChecked",
                Toast.LENGTH_SHORT
            ).show()
        }
        val itemWithDetailBelowWithChevron = mGroupListView!!.createItemView("Item 6")
        itemWithDetailBelowWithChevron.orientation = QMUICommonListItemView.VERTICAL
        itemWithDetailBelowWithChevron.accessoryType = QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON
        itemWithDetailBelowWithChevron.detailText = "在标题下方的详细信息"
        val longTitleAndDetail = mGroupListView!!.createItemView(
            null,
            "标题有点长；标题有点长；标题有点长；标题有点长；标题有点长；标题有点长",
            "详细信息有点长; 详细信息有点长；详细信息有点长；详细信息有点长;详细信息有点长",
            QMUICommonListItemView.VERTICAL,
            QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val paddingVer = QMUIDisplayHelper.dp2px(context, 12)
        longTitleAndDetail.setPadding(
            longTitleAndDetail.paddingLeft, paddingVer,
            longTitleAndDetail.paddingRight, paddingVer
        )
        val height = QMUIResHelper.getAttrDimen(context, com.qmuiteam.qmui.R.attr.qmui_list_item_height)
        val itemWithDetailBelowWithChevronWithIcon = mGroupListView!!.createItemView(
            ContextCompat.getDrawable(requireContext(), R.mipmap.about_logo),
            "Item 7",
            "在标题下方的详细信息",
            QMUICommonListItemView.VERTICAL,
            QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON,
            height
        )
        val itemWithCustom = mGroupListView!!.createItemView("右方自定义 View")
        itemWithCustom.accessoryType = QMUICommonListItemView.ACCESSORY_TYPE_CUSTOM
        val loadingView = QMUILoadingView(activity)
        itemWithCustom.addAccessoryCustomView(loadingView)
        val itemRedPoint1 = mGroupListView!!.createItemView(
            ContextCompat.getDrawable(requireContext(), R.mipmap.about_logo),
            "红点显示在左边",
            "在标题下方的详细信息",
            QMUICommonListItemView.VERTICAL,
            QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON,
            height
        )
        itemRedPoint1.setTipPosition(QMUICommonListItemView.TIP_POSITION_LEFT)
        itemRedPoint1.showRedDot(true)
        val itemRedPoint2 = mGroupListView!!.createItemView(
            ContextCompat.getDrawable(requireContext(), R.mipmap.about_logo),
            "红点显示在右边",
            "在标题下方的详细信息",
            QMUICommonListItemView.VERTICAL,
            QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON,
            height
        )
        itemRedPoint2.setTipPosition(QMUICommonListItemView.TIP_POSITION_RIGHT)
        itemRedPoint2.showRedDot(true)
        val itemRedPoint3 = mGroupListView!!.createItemView(
            ContextCompat.getDrawable(requireContext(), R.mipmap.about_logo),
            "红点显示在左边",
            "在右方的详细信息",
            QMUICommonListItemView.HORIZONTAL,
            QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON,
            height
        )
        itemRedPoint3.setTipPosition(QMUICommonListItemView.TIP_POSITION_LEFT)
        itemRedPoint3.showRedDot(true)
        val itemRedPoint4 = mGroupListView!!.createItemView(
            ContextCompat.getDrawable(requireContext(), R.mipmap.about_logo),
            "红点显示在右边",
            "在右方的详细信息",
            QMUICommonListItemView.HORIZONTAL,
            QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON,
            height
        )
        itemRedPoint4.setTipPosition(QMUICommonListItemView.TIP_POSITION_RIGHT)
        itemRedPoint4.showRedDot(true)
        val itemNew1 = mGroupListView!!.createItemView(
            ContextCompat.getDrawable(requireContext(), R.mipmap.about_logo),
            "new 标识显示在左边",
            "在标题下方的详细信息",
            QMUICommonListItemView.VERTICAL,
            QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON,
            height
        )
        itemNew1.setTipPosition(QMUICommonListItemView.TIP_POSITION_LEFT)
        itemNew1.showNewTip(true)
        val itemNew2 = mGroupListView!!.createItemView(
            ContextCompat.getDrawable(requireContext(), R.mipmap.about_logo),
            "new 标识显示在右边",
            "在标题下方的详细信息",
            QMUICommonListItemView.VERTICAL,
            QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON,
            height
        )
        itemNew2.setTipPosition(QMUICommonListItemView.TIP_POSITION_RIGHT)
        itemNew2.showNewTip(true)
        val itemNew3 = mGroupListView!!.createItemView(
            ContextCompat.getDrawable(requireContext(), R.mipmap.about_logo),
            "new 标识显示在左边",
            "在右方的详细信息",
            QMUICommonListItemView.HORIZONTAL,
            QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON,
            height
        )
        itemNew3.setTipPosition(QMUICommonListItemView.TIP_POSITION_LEFT)
        itemNew3.showNewTip(true)
        val itemNew4 = mGroupListView!!.createItemView(
            ContextCompat.getDrawable(requireContext(), R.mipmap.about_logo),
            "new 标识显示在右边",
            "在右方的详细信息",
            QMUICommonListItemView.HORIZONTAL,
            QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON,
            height
        )
        itemNew4.setTipPosition(QMUICommonListItemView.TIP_POSITION_RIGHT)
        itemNew4.showNewTip(true)
        val onClickListener = View.OnClickListener { v ->
            if (v is QMUICommonListItemView) {
                val text = v.text
                Toast.makeText(activity, "$text is Clicked", Toast.LENGTH_SHORT).show()
                if (v.accessoryType == QMUICommonListItemView.ACCESSORY_TYPE_SWITCH) {
                    v.switch.toggle()
                }
            }
        }
        val size = QMUIDisplayHelper.dp2px(context, 20)
        QMUIGroupListView.newSection(context)
            .setTitle("Section 1: 默认提供的样式")
            .setDescription("Section 1 的描述")
            .setLeftIconSize(size, ViewGroup.LayoutParams.WRAP_CONTENT)
            .addItemView(normalItem, onClickListener)
            .addItemView(itemWithDetail, onClickListener)
            .addItemView(itemWithDetailBelow, onClickListener)
            .addItemView(itemWithChevron, onClickListener)
            .addItemView(itemWithSwitch, onClickListener)
            .addItemView(itemWithDetailBelowWithChevron, onClickListener)
            .addItemView(itemWithDetailBelowWithChevronWithIcon, onClickListener)
            .addItemView(longTitleAndDetail, onClickListener)
            .setMiddleSeparatorInset(QMUIDisplayHelper.dp2px(context, 16), 0)
            .addTo(mGroupListView)
        QMUIGroupListView.newSection(context)
            .setTitle("Section 2: 自定义右侧 View/红点/new 提示")
            .setLeftIconSize(size, ViewGroup.LayoutParams.WRAP_CONTENT)
            .addItemView(itemWithCustom, onClickListener)
            .addItemView(itemRedPoint1, onClickListener)
            .addItemView(itemRedPoint2, onClickListener)
            .addItemView(itemRedPoint3, onClickListener)
            .addItemView(itemRedPoint4, onClickListener)
            .addItemView(itemNew1, onClickListener)
            .addItemView(itemNew2, onClickListener)
            .addItemView(itemNew3, onClickListener)
            .addItemView(itemNew4, onClickListener)
            .setOnlyShowStartEndSeparator(true)
            .addTo(mGroupListView)
    }
}