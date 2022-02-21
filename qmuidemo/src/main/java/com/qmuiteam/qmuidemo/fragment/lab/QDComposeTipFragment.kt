package com.qmuiteam.qmuidemo.fragment.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.qmuiteam.compose.core.ui.QMUITopBarBackIconItem
import com.qmuiteam.compose.core.ui.QMUITopBarWithLazyScrollState
import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord
import com.qmuiteam.qmuidemo.R
import com.qmuiteam.qmuidemo.base.ComposeBaseFragment
import com.qmuiteam.qmuidemo.lib.annotation.Widget

@Widget(name = "QMUI Compose Tip", iconRes = R.mipmap.icon_grid_in_progress)
@LatestVisitRecord
class QDComposeTipFragment : ComposeBaseFragment() {
    @Composable
    override fun PageContent() {
        Column(modifier = Modifier.fillMaxSize()) {
            val scrollState = rememberLazyListState()
            QMUITopBarWithLazyScrollState(
                scrollState = scrollState,
                title = "QMUIPhoto",
                leftItems = arrayListOf(
                    QMUITopBarBackIconItem {
                        popBackStack()
                    }
                )
            )
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "在 UI 开发过程中，经常会遇到如下一个需求：\n" +
                                    "假设一个布局是 【头像】【人名】【推荐信息】，正常用 LinearLayout 实现， " +
                                    "是没有任何问题的，但是要求在人名过长，整体内容会超过容器宽度时，" +
                                    "不要省略推荐信息，而是省略人名信息。",
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                        ConstraintLayout(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(Color.LightGray)
                        ) {
                            val (one, two, three, four) = createRefs()
                            val horChain = createHorizontalChain(one, two, three, chainStyle = ChainStyle.Packed(0f))
                            constrain(horChain) {
                                start.linkTo(parent.start)
                                end.linkTo(four.start)
                            }
                            Text(
                                "此处不压缩",
                                color = Color.White,
                                maxLines = 1,
                                modifier = Modifier
                                    .background(Color.Red)
                                    .constrainAs(one) {
                                        top.linkTo(parent.top)
                                        bottom.linkTo(parent.bottom)
                                    })
                            Text(
                                "此处如果内容有那么一点点过长，那就压缩省略压缩省略压缩省略",
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .background(Color.Green)
                                    .constrainAs(two) {
                                        width = Dimension.preferredWrapContent
                                        top.linkTo(parent.top)
                                        bottom.linkTo(parent.bottom)
                                    })
                            Text(
                                "此处也不压缩",
                                color = Color.White,
                                maxLines = 1,
                                modifier = Modifier
                                    .background(Color.Black)
                                    .constrainAs(three) {
                                        top.linkTo(parent.top)
                                        bottom.linkTo(parent.bottom)
                                    })
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(50.dp)
                                    .background(Color.Blue)
                                    .constrainAs(four) {
                                        top.linkTo(parent.top)
                                        bottom.linkTo(parent.bottom)
                                        end.linkTo(parent.end)
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}