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

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.qmuiteam.compose.core.ex.drawBottomSeparator
import com.qmuiteam.compose.modal.*
import com.qmuiteam.compose.core.ui.*
import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmuidemo.R
import com.qmuiteam.qmuidemo.base.ComposeBaseFragment
import com.qmuiteam.qmuidemo.lib.annotation.Widget


@Widget(widgetClass = QMUIDialog::class, iconRes = R.mipmap.icon_grid_dialog)
@LatestVisitRecord
class QDDialogFragment() : ComposeBaseFragment() {

    @Composable
    override fun PageContent() {
        Column(modifier = Modifier.fillMaxSize()) {
            val scrollState = rememberLazyListState()
            QMUITopBarWithLazyScrollState(
                scrollState = scrollState,
                title = "QMUIDialog",
                leftItems = arrayListOf(
                    QMUITopBarBackIconItem {
                        popBackStack()
                    }
                ),
                rightItems = arrayListOf(
                    QMUITopBarTextItem("Test") {
                        startFragment(QDAboutFragment())
                    }
                )
            )
            val view = LocalView.current
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White)
            ) {
                item {
                    QMUIItem(
                        title = "消息类型对话框",
                        drawBehind = {
                            drawBottomSeparator(insetStart = qmuiCommonHorSpace, insetEnd = qmuiCommonHorSpace)
                        }
                    ) {
                        view.qmuiDialog { modal ->
                            QMUIDialogMsg(modal,
                                "这是标题",
                                "这是一丢丢有趣但是没啥用的内容",
                                listOf(
                                    QMUIModalAction("取 消") {
                                        it.dismiss()
                                    },
                                    QMUIModalAction("确 定") {
                                        Toast
                                            .makeText(view.context, "确定啦!!!", Toast.LENGTH_SHORT)
                                            .show()
                                        it.dismiss()
                                    }
                                )
                            )
                        }.show()
                    }
                }

                item {
                    QMUIItem(
                        title = "列表类型对话框",
                        drawBehind = {
                            drawBottomSeparator(insetStart = qmuiCommonHorSpace, insetEnd = qmuiCommonHorSpace)
                        }
                    ) {
                        view.qmuiDialog { modal ->
                            QMUIDialogList(modal, maxHeight = 500.dp) {
                                items(200){ index ->
                                    QMUIItem(title = "第${index + 1}项") {
                                        Toast.makeText(view.context, "你点了第${index + 1}项", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }.show()
                    }
                }

                item {
                    QMUIItem(
                        title = "单选类型浮层",
                        drawBehind = {
                            drawBottomSeparator(insetStart = qmuiCommonHorSpace, insetEnd = qmuiCommonHorSpace)
                        }
                    ) {
                        view.qmuiDialog { modal ->
                            val list = remember {
                                val items = arrayListOf<String>()
                                for(i in 0 until 500){
                                    items.add("Item $i")
                                }
                                items
                            }
                            val markIndex by remember {
                                mutableStateOf(20)
                            }
                            QMUIDialogMarkList(
                                modal,
                                maxHeight = 500.dp,
                                list = list,
                                markIndex = markIndex
                            ) { _, index ->
                                Toast.makeText(view.context, "你点了第${index + 1}项", Toast.LENGTH_SHORT).show()
//                                modal.dismiss()
                            }
                        }.show()
                    }
                }

                item {
                    QMUIItem(
                        title = "多选类型浮层",
                        drawBehind = {
                            drawBottomSeparator(insetStart = qmuiCommonHorSpace, insetEnd = qmuiCommonHorSpace)
                        }
                    ) {
                        view.qmuiDialog { modal ->
                            val list = remember {
                                val items = arrayListOf<String>()
                                for(i in 0 until 500){
                                    items.add("Item $i")
                                }
                                items
                            }
                            val checked = remember {
                                mutableStateListOf(0, 5, 10, 20)
                            }
                            val disable = remember {
                                mutableStateListOf(5, 10)
                            }
                            Column() {
                                QMUIDialogMutiCheckList(
                                    modal,
                                    maxHeight = 500.dp,
                                    list = list,
                                    checked = checked.toSet(),
                                    disabled = disable.toSet()
                                ) { _, index ->
                                    if(checked.contains(index)){
                                        checked.remove(index)
                                    }else{
                                        checked.add(index)
                                    }
                                }
                                QMUIDialogActions(modal = modal, actions = listOf(
                                    QMUIModalAction("取 消") {
                                        it.dismiss()
                                    },
                                    QMUIModalAction("确 定") {
                                        Toast
                                            .makeText(view.context, "你选择了: ${checked.joinToString(",")}", Toast.LENGTH_SHORT)
                                            .show()
                                        it.dismiss()
                                    }
                                ))
                            }
                        }.show()
                    }
                }

                item {
                    QMUIItem(
                        title = "Toast",
                        drawBehind = {
                            drawBottomSeparator(insetStart = qmuiCommonHorSpace, insetEnd = qmuiCommonHorSpace)
                        }
                    ) {
                        view.qmuiToast("这只是个 Toast!")
                    }
                }

                item {
                    QMUIItem(
                        title = "BottomSheet(list)",
                        drawBehind = {
                            drawBottomSeparator(insetStart = qmuiCommonHorSpace, insetEnd = qmuiCommonHorSpace)
                        }
                    ) {
                        view.qmuiBottomSheet {
                            QMUIBottomSheetList(it) {
                                items(200){ index ->
                                    QMUIItem(title = "第${index + 1}项") {
                                        Toast.makeText(view.context, "你点了第${index + 1}项", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }.show()
                    }
                }
            }
        }
    }
}