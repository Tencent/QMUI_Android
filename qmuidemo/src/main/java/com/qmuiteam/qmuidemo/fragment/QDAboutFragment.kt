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
package com.qmuiteam.qmuidemo.fragment

import android.os.Bundle
import com.qmuiteam.qmuidemo.base.BaseFragment
import butterknife.BindView
import com.qmuiteam.qmui.widget.QMUITopBarLayout
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import butterknife.ButterKnife
import com.qmuiteam.qmui.util.QMUIPackageHelper
import com.qmuiteam.qmuidemo.fragment.QDWebExplorerFragment
import com.qmuiteam.qmui.arch.QMUIFragment
import com.qmuiteam.qmui.arch.QMUIFragment.TransitionConfig
import com.qmuiteam.qmui.arch.SwipeBackLayout.ViewMoveAction
import com.qmuiteam.qmui.arch.SwipeBackLayout
import com.qmuiteam.qmuidemo.R
import com.qmuiteam.qmuidemo.manager.QDSchemeManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * 关于界面
 *
 *
 * Created by Kayo on 2016/11/18.
 */
class QDAboutFragment : BaseFragment() {
    @JvmField
    @BindView(R.id.topbar)
    var mTopBar: QMUITopBarLayout? = null

    @JvmField
    @BindView(R.id.version)
    var mVersionTextView: TextView? = null

    @JvmField
    @BindView(R.id.about_list)
    var mAboutGroupListView: QMUIGroupListView? = null

    @JvmField
    @BindView(R.id.copyright)
    var mCopyrightTextView: TextView? = null
    override fun onCreateView(): View {
        val root = LayoutInflater.from(activity).inflate(R.layout.fragment_about, null)
        ButterKnife.bind(this, root)
        initTopBar()
        mVersionTextView!!.text = QMUIPackageHelper.getAppVersion(context)
        QMUIGroupListView.newSection(context)
            .addItemView(mAboutGroupListView!!.createItemView(resources.getString(R.string.about_item_homepage))) {
                val url = "https://qmuiteam.com/android"
                val bundle = Bundle()
                bundle.putString(QDWebExplorerFragment.EXTRA_URL, url)
                bundle.putString(QDWebExplorerFragment.EXTRA_TITLE, resources.getString(R.string.about_item_homepage))
                val fragment: QMUIFragment = QDWebExplorerFragment()
                fragment.arguments = bundle
                startFragment(fragment)
            }
            .addItemView(mAboutGroupListView!!.createItemView(resources.getString(R.string.about_item_github))) {
                val url = "https://github.com/Tencent/QMUI_Android"
                val bundle = Bundle()
                bundle.putString(QDWebExplorerFragment.EXTRA_URL, url)
                bundle.putString(QDWebExplorerFragment.EXTRA_TITLE, resources.getString(R.string.about_item_github))
                val fragment: QMUIFragment = QDWebExplorerFragment()
                fragment.arguments = bundle
                startFragment(fragment)
            }
            .addTo(mAboutGroupListView)
        val dateFormat = SimpleDateFormat("yyyy", Locale.CHINA)
        val currentYear = dateFormat.format(Date())
        mCopyrightTextView!!.text = String.format(resources.getString(R.string.about_copyright), currentYear)
        return root
    }

    private fun initTopBar() {
        mTopBar!!.addLeftBackImageButton().setOnClickListener { popBackStack() }
        mTopBar!!.setTitle(resources.getString(R.string.about_title))
    }

    override fun onFetchTransitionConfig(): TransitionConfig {
        return SCALE_TRANSITION_CONFIG
    }

    override fun dragViewMoveAction(): ViewMoveAction {
        return SwipeBackLayout.MOVE_VIEW_TOP_TO_BOTTOM
    }
}