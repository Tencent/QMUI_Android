package com.qmuiteam.compose.modal

import android.util.Log
import android.view.View
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp

@Composable
fun QMUIBottomSheetList(
    modal: QMUIModal,
    state: LazyListState = rememberLazyListState(),
    children: LazyListScope.(QMUIModal) -> Unit
) {
    LazyColumn(
        state = state,
        modifier = Modifier.fillMaxWidth()
    ) {
        children(modal)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedVisibilityScope.QMUIBottomSheet(
    modal: QMUIModal,
    draggable: Boolean,
    widthLimit: (maxWidth: Dp) -> Dp,
    heightLimit: (maxHeight: Dp) -> Dp,
    radius: Dp = 2.dp,
    background: Color = Color.White,
    mask: Color = DefaultMaskColor,
    modifier: Modifier,
    content: @Composable (QMUIModal) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {


        val wl = widthLimit(maxWidth)
        val wh = heightLimit(maxHeight)

        var contentModifier = if (wl < maxWidth) {
            Modifier.width(wl)
        } else {
            Modifier.fillMaxWidth()
        }

        contentModifier = contentModifier
            .heightIn(max = wh.coerceAtMost(maxHeight))


        if (radius > 0.dp) {
            contentModifier =
                contentModifier.clip(RoundedCornerShape(topStart = radius, topEnd = radius))
        }
        contentModifier = contentModifier
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {

            }

        if (draggable) {
            NestScrollWrapper(modal, modifier, mask) {
                Box(modifier = contentModifier) {
                    content(modal)
                }
            }
        } else {
            if (mask != Color.Transparent) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .animateEnterExit(
                            enter = fadeIn(tween()),
                            exit = fadeOut(tween())
                        )
                        .background(mask)
                )
            }
            Box(modifier = modifier.then(contentModifier)) {
                content(modal)
            }
        }

    }
}


private class MutableHeight(var height: Float)

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedVisibilityScope.NestScrollWrapper(
    modal: QMUIModal,
    modifier: Modifier,
    mask: Color,
    content: @Composable () -> Unit
) {
    val yOffsetState = remember {
        mutableStateOf(0f)
    }

    val mutableContentHeight = remember {
        MutableHeight(0f)
    }
    val contentHeight = mutableContentHeight.height

    val percent = if (contentHeight <= 0f) 1f else {
        ((contentHeight - yOffsetState.value) / contentHeight)
            .coerceAtMost(1f)
            .coerceAtLeast(0f)
    }

    val nestedScrollConnection = remember(modal, yOffsetState) {
        BottomSheetNestedScrollConnection(modal, yOffsetState, mutableContentHeight)
    }

    val yOffset = yOffsetState.value

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (mask != Color.Transparent) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(percent)
                    .animateEnterExit(
                        enter = fadeIn(tween()),
                        exit = fadeOut(tween())
                    )
                    .background(mask)
            )
            Box(modifier = modifier
                .graphicsLayer { translationY = yOffset }
                .nestedScroll(nestedScrollConnection)
                .onGloballyPositioned {
                    mutableContentHeight.height = it.size.height.toFloat()
                }
            ) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun View.qmuiBottomSheet(
    mask: Color = DefaultMaskColor,
    systemCancellable: Boolean = true,
    draggable: Boolean = true,
    maskTouchBehavior: MaskTouchBehavior = MaskTouchBehavior.dismiss,
    modalHostProvider: ModalHostProvider = DefaultModalHostProvider,
    enter: EnterTransition = slideInVertically(tween()) { it },
    exit: ExitTransition = slideOutVertically(tween())  { it },
    widthLimit: (maxWidth: Dp) -> Dp = { it.coerceAtMost(420.dp) },
    heightLimit: (maxHeight: Dp) -> Dp = { if (it < 640.dp) it - 40.dp else it * 0.85f },
    radius: Dp = 12.dp,
    background: Color = Color.White,
    content: @Composable (QMUIModal) -> Unit
): QMUIModal {
    return qmuiModal(
        Color.Transparent,
        systemCancellable,
        maskTouchBehavior,
        modalHostProvider = modalHostProvider,
        enter = EnterTransition.None,
        exit = ExitTransition.None,
    ) { modal ->
        QMUIBottomSheet(
            modal,
            draggable,
            widthLimit,
            heightLimit,
            radius,
            background,
            mask,
            Modifier.animateEnterExit(
                enter = enter,
                exit = exit
            ),
            content
        )
    }
}

private class BottomSheetNestedScrollConnection(
    val modal: QMUIModal,
    val yOffsetStateFlow: MutableState<Float>,
    val contentHeight: MutableHeight
) : NestedScrollConnection {

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if(source == NestedScrollSource.Fling){
            return Offset.Zero
        }
        val currentOffset = yOffsetStateFlow.value
        if(available.y < 0 && currentOffset > 0){
            val consume = available.y.coerceAtLeast(-currentOffset)
            yOffsetStateFlow.value = currentOffset + consume
            return Offset(0f, consume)
        }
        return super.onPreScroll(available, source)
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        if(source == NestedScrollSource.Fling){
            return Offset.Zero
        }
        if (available.y > 0) {
            yOffsetStateFlow.value = yOffsetStateFlow.value + available.y
            return Offset(0f, available.y)
        }
        return super.onPostScroll(consumed, available, source)
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        if (yOffsetStateFlow.value > 0) {
            if (available.y > 0 || (available.y == 0f && yOffsetStateFlow.value > contentHeight.height / 2)) {
                modal.dismiss()
            } else {
                val animated = Animatable(yOffsetStateFlow.value, Float.VectorConverter)
                animated.asState()
                animated.animateTo(0f, tween()){
                    yOffsetStateFlow.value = value
                }
            }
            return available
        }
        return Velocity.Zero
    }
}
