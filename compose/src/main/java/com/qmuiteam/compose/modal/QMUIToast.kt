package com.qmuiteam.compose.modal

import android.view.View
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qmuiteam.compose.core.ui.qmuiCommonHorSpace
import com.qmuiteam.compose.core.ui.qmuiToastVerEdgeProtectionMargin
import kotlinx.coroutines.*

private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

@Composable
fun QMUIToast(
    modal: QMUIModal,
    radius: Dp = 8.dp,
    background: Color = Color.DarkGray,
    content: @Composable BoxScope.(QMUIModal) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(radius))
            .background(background)
    ) {
        content(modal)
    }
}

fun View.qmuiToast(
    text: String,
    textColor: Color = Color.White,
    fontSize: TextUnit = 16.sp,
    duration: Long = 1000,
    modalHostProvider: ModalHostProvider = DefaultModalHostProvider,
    alignment: Alignment = Alignment.BottomCenter,
    horEdge: Dp = qmuiCommonHorSpace,
    verEdge: Dp = qmuiToastVerEdgeProtectionMargin,
    radius: Dp = 8.dp,
    background: Color = Color.Black,
    enter: EnterTransition = slideInVertically(initialOffsetY = { it }) + fadeIn(),
    exit: ExitTransition = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
): QMUIModal {
    return qmuiToast(
        duration,
        modalHostProvider,
        alignment,
        horEdge,
        verEdge,
        radius,
        background,
        enter,
        exit
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .align(Alignment.Center)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun View.qmuiToast(
    duration: Long = 1000,
    modalHostProvider: ModalHostProvider = DefaultModalHostProvider,
    alignment: Alignment = Alignment.BottomCenter,
    horEdge: Dp = qmuiCommonHorSpace,
    verEdge: Dp = qmuiToastVerEdgeProtectionMargin,
    radius: Dp = 8.dp,
    background: Color = Color.Black,
    enter: EnterTransition = slideInVertically(initialOffsetY = { it }) + fadeIn(),
    exit: ExitTransition = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    content: @Composable BoxScope.(QMUIModal) -> Unit
): QMUIModal {
    var job: Job? = null
    return qmuiModal(
        Color.Transparent,
        false,
        MaskTouchBehavior.penetrate,
        -1,
        modalHostProvider,
        enter = EnterTransition.None,
        exit = ExitTransition.None
    ) { modal ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horEdge, vertical = verEdge),
            contentAlignment = alignment
        ) {
            Box(
                modifier = Modifier
                    .animateEnterExit(
                        enter = enter,
                        exit = exit
                    )
            ) {
                QMUIToast(modal, radius, background, content)
            }
        }
    }.doOnShow {
        job = scope.launch {
            delay(duration)
            job = null
            it.dismiss()
        }
    }.doOnDismiss {
        job?.cancel()
        job = null
    }.show()
}

fun View.qmuiStillToast(
    text: String,
    textColor: Color = Color.White,
    fontSize: TextUnit = 16.sp,
    duration: Long = 1000,
    modalHostProvider: ModalHostProvider = DefaultModalHostProvider,
    alignment: Alignment = Alignment.BottomCenter,
    horEdge: Dp = qmuiCommonHorSpace,
    verEdge: Dp = qmuiToastVerEdgeProtectionMargin,
    radius: Dp = 8.dp,
    background: Color = Color.Black
): QMUIModal {
    return qmuiStillToast(
        duration,
        modalHostProvider,
        alignment,
        horEdge,
        verEdge,
        radius,
        background
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .align(Alignment.Center)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun View.qmuiStillToast(
    duration: Long = 1000,
    modalHostProvider: ModalHostProvider = DefaultModalHostProvider,
    alignment: Alignment = Alignment.BottomCenter,
    horEdge: Dp = qmuiCommonHorSpace,
    verEdge: Dp = qmuiToastVerEdgeProtectionMargin,
    radius: Dp = 8.dp,
    background: Color = Color.Black,
    content: @Composable BoxScope.(QMUIModal) -> Unit
): QMUIModal {
    var job: Job? = null
    return qmuiStillModal(
        Color.Transparent,
        false,
        MaskTouchBehavior.penetrate,
        -1,
        modalHostProvider,
    ) { modal ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horEdge, vertical = verEdge),
            contentAlignment = alignment
        ) {
            QMUIToast(modal, radius, background, content)
        }
    }.doOnShow {
        job = scope.launch {
            delay(duration)
            job = null
            it.dismiss()
        }
    }.doOnDismiss {
        job?.cancel()
        job = null
    }.show()
}