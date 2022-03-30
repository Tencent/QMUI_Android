package com.qmuiteam.compose.modal

import android.view.View
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qmuiteam.compose.R
import com.qmuiteam.compose.core.ui.*

val DefaultDialogPaddingHor = 20.dp


@Composable
fun QMUIDialog(
    modal: QMUIModal,
    horEdge: Dp = qmuiCommonHorSpace,
    verEdge: Dp = qmuiDialogVerEdgeProtectionMargin,
    widthLimit: Dp = 360.dp,
    radius: Dp = 2.dp,
    background: Color = Color.White,
    content: @Composable (QMUIModal) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horEdge, vertical = verEdge),
        contentAlignment = Alignment.Center
    ) {
        var modifier = if (widthLimit < maxWidth) {
            Modifier.width(widthLimit)
        } else {
            Modifier.fillMaxWidth()
        }
        if (radius > 0.dp) {
            modifier = modifier.clip(RoundedCornerShape(radius))
        }
        modifier = modifier
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { }
        Box(modifier = modifier) {
            content(modal)
        }
    }
}

@Composable
fun QMUIDialogActions(
    modal: QMUIModal,
    actions: List<QMUIModalAction>
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp, end = 6.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.End
    ) {
        actions.forEach {
            QMUIDialogAction(
                text = it.text,
                enabled = it.enabled,
                color = it.color
            ) {
                it.onClick(modal)
            }
        }
    }
}

@Composable
fun QMUIDialogMsg(
    modal: QMUIModal,
    title: String,
    content: String,
    actions: List<QMUIModalAction>
) {
    Column {
        QMUIDialogTitle(title)
        QMUIDialogMsgContent(content)
        QMUIDialogActions(modal, actions)
    }
}

@Composable
fun QMUIDialogList(
    modal: QMUIModal,
    maxHeight: Dp = Dp.Unspecified,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(vertical = 8.dp),
    children: LazyListScope.(QMUIModal) -> Unit
) {
    LazyColumn(
        state = state,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(0.dp, maxHeight),
        contentPadding = contentPadding
    ) {
        children(modal)
    }
}

@Composable
fun QMUIDialogMarkList(
    modal: QMUIModal,
    list: List<String>,
    markIndex: Int,
    state: LazyListState = rememberLazyListState(markIndex),
    maxHeight: Dp = Dp.Unspecified,
    itemIndication: Indication = rememberRipple(color = qmuiIndicationColor),
    itemTextSize: TextUnit = 17.sp,
    itemTextColor: Color = qmuiTextMainColor,
    itemTextFontWeight: FontWeight = FontWeight.Medium,
    itemTextFontFamily: FontFamily? = null,
    itemMarkTintColor: Color = qmuiPrimaryColor,
    contentPadding: PaddingValues = PaddingValues(vertical = 8.dp),
    onItemClick: (modal: QMUIModal, index: Int) -> Unit
) {
    QMUIDialogList(modal, maxHeight, state, contentPadding) {
        itemsIndexed(list) { index, item ->
            QMUIItem(
                title = item,
                indication = itemIndication,
                titleOnlyFontSize = itemTextSize,
                titleColor = itemTextColor,
                titleFontSize = itemTextSize,
                titleFontWeight = itemTextFontWeight,
                titleFontFamily = itemTextFontFamily,
                accessory = {
                    if (markIndex == index) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_qmui_mark),
                            contentDescription = "",
                            colorFilter = ColorFilter.tint(itemMarkTintColor)
                        )
                    }
                }
            ) {
                onItemClick(modal, index)
            }
        }
    }
}


@Composable
fun QMUIDialogMutiCheckList(
    modal: QMUIModal,
    list: List<String>,
    checked: Set<Int>,
    disabled: Set<Int> = emptySet(),
    disableAlpha: Float = 0.5f,
    state: LazyListState = rememberLazyListState(0),
    maxHeight: Dp = Dp.Unspecified,
    itemIndication: Indication = rememberRipple(color = qmuiIndicationColor),
    itemTextSize: TextUnit = 17.sp,
    itemTextColor: Color = qmuiTextMainColor,
    itemTextFontWeight: FontWeight = FontWeight.Medium,
    itemTextFontFamily: FontFamily? = null,
    itemCheckNormalTint: Color = qmuiSeparatorColor,
    itemCheckCheckedTint: Color = qmuiPrimaryColor,
    contentPadding: PaddingValues = PaddingValues(vertical = 8.dp),
    onItemClick: (modal: QMUIModal, index: Int) -> Unit
) {
    QMUIDialogList(modal, maxHeight, state, contentPadding) {
        itemsIndexed(list) { index, item ->
            val isDisabled = disabled.contains(index)
            val onClick: (() -> Unit)? = if(isDisabled) null else {
                {
                    onItemClick(modal, index)
                }
            }
            QMUIItem(
                title = item,
                indication = itemIndication,
                titleOnlyFontSize = itemTextSize,
                titleColor = itemTextColor,
                titleFontSize = itemTextSize,
                titleFontWeight = itemTextFontWeight,
                titleFontFamily = itemTextFontFamily,
                alpha = if(isDisabled) disableAlpha else 1f,
                accessory = {
                    if (checked.contains(index)) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_qmui_checkbox_checked),
                            contentDescription = "",
                            colorFilter = ColorFilter.tint(itemCheckCheckedTint)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_qmui_checkbox_normal),
                            contentDescription = "",
                            colorFilter = ColorFilter.tint(itemCheckNormalTint)
                        )
                    }
                },
                onClick =  onClick
            )
        }
    }
}

@Composable
fun QMUIDialogTitle(
    text: String,
    fontSize: TextUnit = 16.sp,
    textAlign: TextAlign? = null,
    color: Color = Color.Black,
    fontWeight: FontWeight? = FontWeight.Bold,
    fontFamily: FontFamily? = null,
    maxLines: Int = Int.MAX_VALUE,
    lineHeight: TextUnit = 20.sp,
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 24.dp,
                start = DefaultDialogPaddingHor,
                end = DefaultDialogPaddingHor,
            ),
        textAlign = textAlign,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        maxLines = maxLines,
        lineHeight = lineHeight
    )
}

@Composable
fun QMUIDialogMsgContent(
    text: String,
    fontSize: TextUnit = 14.sp,
    textAlign: TextAlign? = null,
    color: Color = Color.Black,
    fontWeight: FontWeight? = FontWeight.Normal,
    fontFamily: FontFamily? = null,
    maxLines: Int = Int.MAX_VALUE,
    lineHeight: TextUnit = 16.sp,
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = DefaultDialogPaddingHor,
                end = DefaultDialogPaddingHor,
                top = 16.dp,
                bottom = 24.dp
            ),
        textAlign = textAlign,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        maxLines = maxLines,
        lineHeight = lineHeight
    )
}

@Composable
fun QMUIDialogAction(
    text: String,
    fontSize: TextUnit = 14.sp,
    color: Color = qmuiPrimaryColor,
    fontWeight: FontWeight? = FontWeight.Bold,
    fontFamily: FontFamily? = null,
    paddingVer: Dp = 9.dp,
    paddingHor: Dp = 14.dp,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    Text(
        text = text,
        modifier = Modifier
            .padding(horizontal = paddingHor, vertical = paddingVer)
            .alpha(if (isPressed.value) 0.5f else 1f)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick.invoke()
            },
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontFamily = fontFamily
    )
}


fun View.qmuiDialog(
    mask: Color = DefaultMaskColor,
    systemCancellable: Boolean = true,
    maskTouchBehavior: MaskTouchBehavior = MaskTouchBehavior.dismiss,
    modalHostProvider: ModalHostProvider = DefaultModalHostProvider,
    enter: EnterTransition = fadeIn(tween(), 0f),
    exit: ExitTransition = fadeOut(tween(), 0f),
    horEdge: Dp = qmuiCommonHorSpace,
    verEdge: Dp = qmuiDialogVerEdgeProtectionMargin,
    widthLimit: Dp = 360.dp,
    radius: Dp = 12.dp,
    background: Color = Color.White,
    content: @Composable (QMUIModal) -> Unit
): QMUIModal {
    return qmuiModal(
        mask,
        systemCancellable,
        maskTouchBehavior,
        modalHostProvider = modalHostProvider,
        enter = enter,
        exit = exit
    ) { modal ->
        QMUIDialog(modal, horEdge, verEdge, widthLimit, radius, background, content)
    }
}

fun View.qmuiStillDialog(
    mask: Color = DefaultMaskColor,
    systemCancellable: Boolean = true,
    maskTouchBehavior: MaskTouchBehavior = MaskTouchBehavior.dismiss,
    modalHostProvider: ModalHostProvider = DefaultModalHostProvider,
    horEdge: Dp = 20.dp,
    verEdge: Dp = 20.dp,
    widthLimit: Dp = 360.dp,
    radius: Dp = 12.dp,
    background: Color = Color.White,
    content: @Composable (QMUIModal) -> Unit
): QMUIModal {
    return qmuiStillModal(mask, systemCancellable, maskTouchBehavior, modalHostProvider = modalHostProvider) { modal ->
        QMUIDialog(modal, horEdge, verEdge, widthLimit, radius, background, content)
    }
}