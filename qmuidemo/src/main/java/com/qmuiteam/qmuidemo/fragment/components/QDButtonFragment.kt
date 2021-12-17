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

import android.view.LayoutInflater
import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import butterknife.BindView
import butterknife.ButterKnife
import com.qmuiteam.compose.modal.QMUIDialogMessageContent
import com.qmuiteam.compose.modal.QMUIDialogTitle
import com.qmuiteam.compose.modal.qmuiDialog
import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord
import com.qmuiteam.qmui.arch.effect.MapEffect
import com.qmuiteam.qmui.kotlin.onClick
import com.qmuiteam.qmui.kotlin.skin
import com.qmuiteam.qmui.widget.QMUITopBarLayout
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton
import com.qmuiteam.qmuidemo.R
import com.qmuiteam.qmuidemo.base.BaseFragment
import com.qmuiteam.qmuidemo.lib.annotation.Widget
import com.qmuiteam.qmuidemo.manager.QDDataManager
import com.qmuiteam.qmuidemo.model.QDItemDescription

@LatestVisitRecord
@Widget(name = "QMUIRoundButton", iconRes = R.mipmap.icon_grid_button)
class QDButtonFragment : BaseFragment() {
    @BindView(R.id.topbar)
    internal lateinit var mTopBar: QMUITopBarLayout
    @BindView(R.id.alpha_button)
    internal lateinit var alphaButton: QMUIRoundButton
    @BindView(R.id.test_java_kotlin_skin)
    internal lateinit var kotlinSkinButton: QMUIRoundButton
    private lateinit var mQDItemDescription: QDItemDescription
    override fun onCreateView(): View {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_button, null)
        ButterKnife.bind(this, view)
        mQDItemDescription = QDDataManager.getInstance().getDescription(this.javaClass)
        alphaButton.setChangeAlphaWhenPress(true)
        initTopBar()

        kotlinSkinButton.skin {
            border(R.attr.app_skin_btn_test_border_single)
            background(R.attr.app_skin_btn_test_bg_single)
            textColor(R.attr.app_skin_btn_test_border_single)
        }
        return view
    }

    private fun initTopBar() {
        mTopBar.addLeftBackImageButton().onClick { popBackStack() }
        mTopBar.addRightTextButton("test", View.generateViewId()).onClick {
            it.qmuiDialog {
                Column(modifier = Modifier.fillMaxWidth()) {
                    QMUIDialogTitle(text = "这是标题")
                    QMUIDialogMessageContent(text = "这是一点点有意思的内容")
                }

            }.show()
        }
        mTopBar.setTitle(mQDItemDescription.name)


        notifyEffect(MapEffect(HashMap<String, Any>().apply {
            put("interested_type_key", 1)
            put("interested_value_key", "Did you received the change from other fragment?")
        }))
    }
}