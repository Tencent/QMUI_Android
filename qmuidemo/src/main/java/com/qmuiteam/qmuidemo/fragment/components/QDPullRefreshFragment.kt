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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.qmuiteam.qmui.exposure.Exposure
import com.qmuiteam.qmui.exposure.ExposureType
import com.qmuiteam.qmui.exposure.bindExposure
import com.qmuiteam.qmui.exposure.registerExposure
import com.qmuiteam.qmui.widget.QMUITopBarLayout
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet.BottomListSheetBuilder
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUICenterGravityRefreshOffsetCalculator
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIDefaultRefreshOffsetCalculator
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIFollowRefreshOffsetCalculator
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout.OnPullListener
import com.qmuiteam.qmuidemo.R
import com.qmuiteam.qmuidemo.base.BaseFragment
import com.qmuiteam.qmuidemo.base.BaseRecyclerAdapter
import com.qmuiteam.qmuidemo.base.RecyclerViewHolder
import com.qmuiteam.qmuidemo.lib.annotation.Widget
import com.qmuiteam.qmuidemo.manager.QDDataManager
import com.qmuiteam.qmuidemo.model.QDItemDescription
import java.util.*

class ListItemExposure(val text: String): Exposure {
    override fun same(data: Exposure): Boolean {
        return data is ListItemExposure && data.text == text
    }

    override fun expose(view: View, type: ExposureType) {
        Log.i("exposure", "list: $text; $text")
    }

    override fun toString(): String {
        return "ListItemExposure: $text"
    }
}

/**
 * @author cginechen
 * @date 2016-12-14
 */
@Widget(widgetClass = QMUIPullRefreshLayout::class, iconRes = R.mipmap.icon_grid_pull_refresh_layout)
class QDPullRefreshFragment : BaseFragment() {
    @JvmField
    @BindView(R.id.topbar)
    var mTopBar: QMUITopBarLayout? = null

    @JvmField
    @BindView(R.id.pull_to_refresh)
    var mPullRefreshLayout: QMUIPullRefreshLayout? = null

    @JvmField
    @BindView(R.id.listview)
    var mListView: RecyclerView? = null
    private var mAdapter: BaseRecyclerAdapter<String>? = null
    private var mQDItemDescription: QDItemDescription? = null
    override fun onCreateView(): View {
        val root = LayoutInflater.from(activity).inflate(R.layout.fragment_pull_refresh_listview, null)
        ButterKnife.bind(this, root)
        val QDDataManager = QDDataManager.getInstance()
        mQDItemDescription = QDDataManager.getDescription(this.javaClass)
        initTopBar()
        initData()
        return root
    }

    private fun initTopBar() {
        mTopBar!!.addLeftBackImageButton().setOnClickListener { popBackStack() }
        mTopBar!!.setTitle(mQDItemDescription!!.name)

        // 切换其他情况的按钮
        mTopBar!!.addRightImageButton(R.mipmap.icon_topbar_overflow, R.id.topbar_right_change_button).setOnClickListener { showBottomSheetList() }
    }

    private fun initData() {
        mListView!!.layoutManager = object : LinearLayoutManager(context) {
            override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
                return RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }
        mAdapter = object : BaseRecyclerAdapter<String>(context, null) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
                return super.onCreateViewHolder(parent, viewType).apply {
                    itemView.registerExposure()
                }
            }

            override fun getItemLayoutId(viewType: Int): Int {
                return android.R.layout.simple_list_item_1
            }


            override fun bindData(holder: RecyclerViewHolder, position: Int, item: String) {
                holder.setText(android.R.id.text1, item)
                holder.itemView.bindExposure(ListItemExposure(item))
            }
        }
        mAdapter?.setOnItemClickListener(BaseRecyclerAdapter.OnItemClickListener { _, pos ->
            Toast.makeText(
                context,
                "click position=$pos",
                Toast.LENGTH_SHORT
            ).show()
        })
        mListView!!.adapter = mAdapter
        onDataLoaded()
        mPullRefreshLayout!!.setOnPullListener(object : OnPullListener {
            override fun onMoveTarget(offset: Int) {}
            override fun onMoveRefreshView(offset: Int) {}
            override fun onRefresh() {
                mPullRefreshLayout!!.postDelayed({
                    onDataLoaded()
                    // for test exposure
                    count++
                    val data = when (count) {
                        1 -> {
                            listOf(
                                "Maintain", "Helps",  "Liver", "Health", "Function", "Supports", "Healthy", "Fat",
                                "Metabolism", "Nuturally", "Bracket", "Refrigerator", "Bathtub", "Wardrobe", "Comb", "Apron", "Carpet", "Bolster", "Pillow", "Cushion"
                            )
                        }
                        2 -> {
                            listOf(
                                "hehe","Helps", "Maintain", "Liver", "Health", "Function", "Supports", "Healthy", "Fat",
                                "Metabolism", "Nuturally", "Bracket", "Refrigerator", "Bathtub", "Wardrobe", "Comb", "Apron", "Carpet", "Bolster", "Pillow", "Cushion"
                            )
                        }
                        else -> {
                            listOf(
                                "xixi","Health", "Function", "Supports", "Healthy", "Fat",
                                "Metabolism", "Nuturally", "Bracket", "Refrigerator", "Bathtub", "Wardrobe", "Comb", "Apron", "Carpet", "Bolster", "Pillow", "Cushion"
                            )
                        }
                    }
                    mAdapter!!.setData(data)
                    mPullRefreshLayout!!.finishRefresh()
                }, 2000)
            }
        })
    }

    private fun onDataLoaded() {
        val data = listOf(
            "Helps", "Maintain", "Liver", "Health", "Function", "Supports", "Healthy", "Fat",
            "Metabolism", "Nuturally", "Bracket", "Refrigerator", "Bathtub", "Wardrobe", "Comb", "Apron", "Carpet", "Bolster", "Pillow", "Cushion"
        )
        mAdapter!!.setData(data)
    }

    private var count = 0

    private fun showBottomSheetList() {
        BottomListSheetBuilder(activity)
            .addItem(resources.getString(R.string.pull_refresh_default_offset_calculator))
            .addItem(resources.getString(R.string.pull_refresh_follow_offset_calculator))
            .addItem(resources.getString(R.string.pull_refresh_center_gravity_offset_calculator))
            .setOnSheetItemClickListener { dialog, _, position, _ ->
                dialog.dismiss()
                when (position) {
                    0 -> mPullRefreshLayout!!.setRefreshOffsetCalculator(QMUIDefaultRefreshOffsetCalculator())
                    1 -> mPullRefreshLayout!!.setRefreshOffsetCalculator(QMUIFollowRefreshOffsetCalculator())
                    2 -> mPullRefreshLayout!!.setRefreshOffsetCalculator(QMUICenterGravityRefreshOffsetCalculator())
                    else -> {}
                }
            }
            .build()
            .show()
    }
}