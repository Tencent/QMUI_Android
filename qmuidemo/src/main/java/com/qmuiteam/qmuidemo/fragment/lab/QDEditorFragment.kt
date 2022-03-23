package com.qmuiteam.qmuidemo.fragment.lab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.qmuiteam.compose.core.ui.QMUITopBar
import com.qmuiteam.compose.core.ui.QMUITopBarBackIconItem
import com.qmuiteam.editor.*
import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord
import com.qmuiteam.qmuidemo.R
import com.qmuiteam.qmuidemo.base.ComposeBaseFragment
import com.qmuiteam.qmuidemo.lib.annotation.Widget
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@Widget(name = "QMUI Editor", iconRes = R.mipmap.icon_grid_in_progress)
@LatestVisitRecord
class QDEditorFragment : ComposeBaseFragment() {

    @Composable
    fun TextButton(text: String ,onClick: ()-> Unit){
        Text(text, modifier = Modifier
            .clickable {
                onClick()
            }
            .padding(8.dp))
    }

    @Composable
    fun QDEditor() {

        val channel = remember {
            Channel<EditorBehavior>()
        }
        val scope = rememberCoroutineScope()
        Column(modifier = Modifier.fillMaxSize()) {
            QMUIEditor(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                value = TextFieldValue(""),
                hint = AnnotatedString("写下这一刻的想法"),
                channel = channel
            ) {

            }

            Row(modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)) {
                TextButton("加粗"){
                    scope.launch {
                        channel.send(BoldBehavior(500))
                    }

                }

                TextButton("引用"){
                    scope.launch {
                        channel.send(QuoteBehavior)
                    }
                }

                TextButton("无序列表"){
                    scope.launch {
                        channel.send(UnOrderListBehavior)
                    }
                }

                TextButton("Header"){
                    scope.launch {
                        channel.send(HeaderBehavior(HeaderLevel.h2))
                    }
                }
            }
        }

    }

    @Composable
    override fun PageContent() {
        Column(modifier = Modifier.fillMaxSize()) {
            QMUITopBar(
                title = "QMUIEditor",
                leftItems = arrayListOf(
                    QMUITopBarBackIconItem {
                        popBackStack()
                    }
                )
            )
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)) {
                QDEditor()
            }
        }
    }
}