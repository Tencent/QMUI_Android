package com.qmuiteam.compose.core.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.InspectorValueInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import androidx.core.view.WindowInsetsCompat
import com.qmuiteam.compose.core.R
import com.qmuiteam.compose.core.helper.OnePx
import com.qmuiteam.compose.core.provider.QMUILocalWindowInsets
import com.qmuiteam.compose.core.provider.dp

fun interface QMUITopBarItem {
    @Composable
    fun Compose(topBarHeight: Dp)
}

interface QMUITopBarTitleLayout {
    @Composable
    fun Compose(title: CharSequence, subTitle: CharSequence, alignTitleCenter: Boolean)
}

class DefaultQMUITopBarTitleLayout(
    val titleColor: Color = Color.White,
    val titleFontWeight: FontWeight = FontWeight.Bold,
    val titleFontFamily: FontFamily? = null,
    val titleFontSize: TextUnit = 16.sp,
    val titleOnlyFontSize: TextUnit = 17.sp,
    val subTitleColor: Color = Color.White.copy(alpha = 0.8f),
    val subTitleFontWeight: FontWeight = FontWeight.Normal,
    val subTitleFontFamily: FontFamily? = null,
    val subTitleFontSize: TextUnit = 11.sp

) : QMUITopBarTitleLayout {
    @Composable
    override fun Compose(title: CharSequence, subTitle: CharSequence, alignTitleCenter: Boolean) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = if (alignTitleCenter) Alignment.CenterHorizontally else Alignment.Start
        ) {
            Text(
                title.toString(),
                color = titleColor,
                fontWeight = titleFontWeight,
                fontFamily = titleFontFamily,
                fontSize = if (subTitle.isNotEmpty()) titleFontSize else titleOnlyFontSize,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            if (subTitle.isNotEmpty()) {
                Text(
                    subTitle.toString(),
                    color = subTitleColor,
                    fontWeight = subTitleFontWeight,
                    fontFamily = subTitleFontFamily,
                    fontSize = subTitleFontSize,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
        }
    }
}

open class QMUITopBarBackIconItem(
    tint: Color = Color.White,
    pressAlpha: Float = 0.5f,
    disableAlpha: Float = 0.5f,
    enable: Boolean = true,
    onClick: () -> Unit
) : QMUITopBarIconItem(
    R.drawable.ic_qmui_topbar_back,
    "返回",
    tint,
    pressAlpha,
    disableAlpha,
    enable,
    onClick
)

open class QMUITopBarIconItem(
    @DrawableRes val icon: Int,
    val contentDescription: String = "",
    val tint: Color = Color.White,
    val pressAlpha: Float = 0.5f,
    val disableAlpha: Float = 0.5f,
    val enable: Boolean = true,
    val onClick: () -> Unit
) : QMUITopBarItem {

    @Composable
    override fun Compose(topBarHeight: Dp) {
        PressWithAlphaBox(
            modifier = Modifier.size(topBarHeight),
            enable = enable,
            pressAlpha = pressAlpha,
            disableAlpha = disableAlpha,
            onClick = onClick
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(icon),
                contentDescription = contentDescription,
                colorFilter = ColorFilter.tint(tint),
                contentScale = ContentScale.Inside
            )
        }
    }

}


open class QMUITopBarTextItem(
    val text: String,
    val paddingHor: Dp = 12.dp,
    val fontSize: TextUnit = 14.sp,
    val fontWeight: FontWeight = FontWeight.Medium,
    val color: Color = Color.White,
    val pressAlpha: Float = 0.5f,
    val disableAlpha: Float = 0.5f,
    val enable: Boolean = true,
    val onClick: () -> Unit
) : QMUITopBarItem {

    @Composable
    override fun Compose(topBarHeight: Dp) {
        PressWithAlphaBox(
            modifier = Modifier
                .height(topBarHeight)
                .padding(horizontal = paddingHor),
            enable = enable,
            pressAlpha = pressAlpha,
            disableAlpha = disableAlpha,
            onClick = onClick
        ) {
            Text(
                text = text,
                modifier = Modifier.align(Alignment.Center),
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight
            )
        }
    }

}

@Composable
fun QMUITopBarWithLazyScrollState(
    scrollState: LazyListState,
    title: CharSequence = "",
    subTitle: CharSequence = "",
    alignTitleCenter: Boolean = true,
    height: Dp = qmuiTopBarHeight,
    zIndex: Float = qmuiTopBarZIndex,
    backgroundColor: Color = qmuiPrimaryColor,
    changeWithBackground: Boolean = false,
    scrollAlphaChangeMaxOffset: Dp = qmuiScrollAlphaChangeMaxOffset,
    shadowElevation: Dp = 16.dp,
    shadowAlpha: Float = 0.6f,
    separatorHeight: Dp = OnePx(),
    separatorColor: Color = qmuiSeparatorColor,
    paddingStart: Dp = 4.dp,
    paddingEnd: Dp = 4.dp,
    titleBoxPaddingHor: Dp = 8.dp,
    leftItems: List<QMUITopBarItem> = emptyList(),
    rightItems: List<QMUITopBarItem> = emptyList(),
    titleLayout: QMUITopBarTitleLayout = remember { DefaultQMUITopBarTitleLayout() }
){
    val percent = with(LocalDensity.current){
        if(scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset.toDp() > scrollAlphaChangeMaxOffset){
            1f
        } else scrollState.firstVisibleItemScrollOffset.toDp() / scrollAlphaChangeMaxOffset
    }
    QMUITopBar(
        title, subTitle,
        alignTitleCenter, height, zIndex,
        if(changeWithBackground) backgroundColor.copy(backgroundColor.alpha * percent) else backgroundColor,
        shadowElevation, shadowAlpha * percent,
        separatorHeight, separatorColor.copy(separatorColor.alpha * percent),
        paddingStart, paddingEnd,
        titleBoxPaddingHor, leftItems, rightItems, titleLayout
    )
}

@Composable
fun QMUITopBar(
    title: CharSequence,
    subTitle: CharSequence = "",
    alignTitleCenter: Boolean = true,
    height: Dp = qmuiTopBarHeight,
    zIndex: Float = qmuiTopBarZIndex,
    backgroundColor: Color = qmuiPrimaryColor,
    shadowElevation: Dp = 16.dp,
    shadowAlpha: Float = 0.4f,
    separatorHeight: Dp = OnePx(),
    separatorColor: Color = qmuiSeparatorColor,
    paddingStart: Dp = 4.dp,
    paddingEnd: Dp = 4.dp,
    titleBoxPaddingHor: Dp = 8.dp,
    leftItems: List<QMUITopBarItem> = emptyList(),
    rightItems: List<QMUITopBarItem> = emptyList(),
    titleLayout: QMUITopBarTitleLayout = remember { DefaultQMUITopBarTitleLayout() }
) {
    val insets = QMUILocalWindowInsets.current.getInsetsIgnoringVisibility(
        WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout()
    ).dp()
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Max)
        .zIndex(zIndex)
    ){
        Box(modifier = Modifier.fillMaxSize().graphicsLayer {
            this.alpha = shadowAlpha
            this.shadowElevation = shadowElevation.toPx()
            this.shape =  RectangleShape
            this.clip = shadowElevation > 0.dp
        })
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(top = insets.top)
                .height(height)
        ) {
            QMUITopBarContent(
                title,
                subTitle,
                alignTitleCenter,
                height,
                paddingStart,
                paddingEnd,
                titleBoxPaddingHor,
                leftItems,
                rightItems,
                titleLayout
            )
            if(separatorHeight > 0.dp && separatorColor != Color.Transparent){
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(separatorHeight)
                    .align(Alignment.BottomStart)
                    .background(separatorColor)
                )
            }
        }
    }

}

@Composable
fun QMUITopBarContent(
    title: CharSequence,
    subTitle: CharSequence,
    alignTitleCenter: Boolean,
    height: Dp = qmuiTopBarHeight,
    paddingStart: Dp = 4.dp,
    paddingEnd: Dp = 4.dp,
    titleBoxPaddingHor: Dp = 8.dp,
    leftItems: List<QMUITopBarItem> = emptyList(),
    rightItems: List<QMUITopBarItem> = emptyList(),
    titleLayout: QMUITopBarTitleLayout = remember { DefaultQMUITopBarTitleLayout() }
) {

    val measurePolicy = remember(alignTitleCenter) {
        MeasurePolicy { measurables, constraints ->
            var centerMeasurable: Measurable? = null
            var leftPlaceable: Placeable? = null
            var rightPlaceable: Placeable? = null
            var centerPlaceable: Placeable? = null
            val usedConstraints = constraints.copy(minWidth = 0)
            measurables
                .forEach {
                when ((it.parentData as? QMUITopBarAreaParentData)?.area ?: QMUITopBarArea.Left) {
                    QMUITopBarArea.Left -> {
                        leftPlaceable = it.measure(usedConstraints)
                    }
                    QMUITopBarArea.Right -> {
                        rightPlaceable = it.measure(usedConstraints)
                    }
                    QMUITopBarArea.Center -> {
                        centerMeasurable = it
                    }
                }
            }
            val leftItemsWidth = leftPlaceable?.measuredWidth ?: 0
            val rightItemsWidth = rightPlaceable?.measuredWidth ?: 0
            val itemsWidthMax = maxOf(leftItemsWidth, rightItemsWidth)
            val titleContainerWidth = if (alignTitleCenter) {
                constraints.maxWidth - itemsWidthMax * 2
            } else {
                constraints.maxWidth - leftItemsWidth - rightItemsWidth
            }
            if (titleContainerWidth > 0) {
                centerPlaceable = centerMeasurable?.measure(constraints.copy(minWidth = 0, maxWidth = titleContainerWidth))
            }

            layout(constraints.maxWidth, constraints.maxHeight) {
                leftPlaceable?.place(0, 0, 0f)
                rightPlaceable?.let {
                    it.place(constraints.maxWidth - it.measuredWidth, 0, 1f)
                }
                centerPlaceable?.let {
                    if (alignTitleCenter) {
                        it.place(itemsWidthMax, 0, 2f)
                    } else {
                        it.place(leftItemsWidth, 0, 2f)
                    }
                }
            }
        }
    }
    Layout(
        content = {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .qmuiTopBarArea(QMUITopBarArea.Left),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leftItems.forEach {
                    it.Compose(height)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .qmuiTopBarArea(QMUITopBarArea.Center)
                    .padding(horizontal = titleBoxPaddingHor),
                contentAlignment = Alignment.CenterStart
            ) {
                titleLayout.Compose(title, subTitle, alignTitleCenter)
            }

            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .qmuiTopBarArea(QMUITopBarArea.Right),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rightItems.forEach {
                    it.Compose(height)
                }
            }

        },
        measurePolicy = measurePolicy,
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(start = paddingStart, end = paddingEnd)
    )
}


internal enum class QMUITopBarArea { Left, Center, Right }

internal data class QMUITopBarAreaParentData(
    var area: QMUITopBarArea = QMUITopBarArea.Left
)

internal fun Modifier.qmuiTopBarArea(area: QMUITopBarArea) = this.then(
    QMUITopBarAreaModifier(
        area = area,
        inspectorInfo = debugInspectorInfo {
            name = "area"
            value = area.name
        }
    )
)

internal class QMUITopBarAreaModifier(
    val area: QMUITopBarArea,
    inspectorInfo: InspectorInfo.() -> Unit
) : ParentDataModifier, InspectorValueInfo(inspectorInfo) {
    override fun Density.modifyParentData(parentData: Any?): QMUITopBarAreaParentData {
        return ((parentData as? QMUITopBarAreaParentData) ?: QMUITopBarAreaParentData()).also {
            it.area = area
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val otherModifier = other as? QMUITopBarAreaParentData ?: return false
        return area == otherModifier.area
    }

    override fun hashCode(): Int {
        return area.hashCode()
    }

    override fun toString(): String =
        "QMUITopBarAreaModifier(area=$area)"
}