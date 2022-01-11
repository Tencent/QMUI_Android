package com.qmuiteam.qmuidemo.fragment

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qmuiteam.compose.modal.QMUIDialogMsg
import com.qmuiteam.compose.modal.QMUIModalAction
import com.qmuiteam.compose.modal.qmuiDialog
import com.qmuiteam.compose.ui.*
import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmuidemo.R
import com.qmuiteam.qmuidemo.base.ComposeBaseFragment
import com.qmuiteam.qmuidemo.lib.annotation.Widget


@Widget(widgetClass = QMUIDialog::class, iconRes = R.mipmap.icon_grid_dialog)
@LatestVisitRecord
class QDDialogFragment(): ComposeBaseFragment() {

    @Composable
    override fun PageContent() {
        Column(modifier = Modifier.fillMaxSize()) {
            val scrollState = rememberLazyListState()
            QMUITopBarWithLazyScrollState(
                scrollState = scrollState,
                title = "QMUIDialog",
                leftItems = arrayListOf(
                    QMUITopBarBackIconItem{
                        popBackStack()
                    }
                ),
                rightItems = arrayListOf(
                    QMUITopBarTextItem("Test"){
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
            ){
                item {
                    QMUIItem(
                        title = "消息类型对话框",
                        drawBehind = {
                            drawBottomSeparator(insetStart = qmuiCommonHorSpace, insetEnd = qmuiCommonHorSpace)
                        }
                    ){
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
                        title = "列表类型对话框"
                    ){
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
            }
        }
    }
}