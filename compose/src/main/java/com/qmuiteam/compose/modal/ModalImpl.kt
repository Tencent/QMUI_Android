package com.qmuiteam.compose.modal

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import kotlinx.coroutines.flow.MutableStateFlow

internal abstract class QMUIModalPresent(
    private val rootLayout: FrameLayout,
    private val onBackPressedDispatcher: OnBackPressedDispatcher,
    val mask: Color = DefaultMaskColor,
    val systemCancellable: Boolean = true,
    val maskTouchBehavior: MaskTouchBehavior = MaskTouchBehavior.dismiss,
) : QMUIModal {

    private val onShowListeners = arrayListOf<QMUIModal.Action>()
    private val onDismissListeners = arrayListOf<QMUIModal.Action>()
    private val visibleFlow = MutableStateFlow(false)
    private var isShown = false
    private var isDismissing = false

    private val composeLayout = ComposeView(rootLayout.context).apply {
        visibility = View.GONE
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(systemCancellable) {
        override fun handleOnBackPressed() {
            dismiss()
        }
    }

    init {
        composeLayout.setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                val visible by visibleFlow.collectAsState(initial = false)
                ModalContent(visible = visible) {
                    if (isDismissing) {
                        doAfterDismiss()
                    }
                }
            }
        }
    }

    private fun doAfterDismiss() {
        isDismissing = false
        composeLayout.visibility = View.GONE
        composeLayout.disposeComposition()
        rootLayout.removeView(composeLayout)
        onBackPressedCallback.remove()
        onDismissListeners.forEach {
            it.invoke(this)
        }
    }

    @Composable
    abstract fun ModalContent(visible: Boolean, dismissFinishAction: () -> Unit)

    override fun isShowing(): Boolean {
        return isShown
    }

    override fun show(): QMUIModal {
        if (isShown || isDismissing) {
            return this
        }
        isShown = true
        rootLayout.addView(composeLayout, generateLayoutParams())
        composeLayout.visibility = View.VISIBLE
        visibleFlow.value = true
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
        onShowListeners.forEach {
            it.invoke(this)
        }
        return this
    }

    open fun generateLayoutParams(): FrameLayout.LayoutParams {
        return FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun dismiss() {
        if (!isShown) {
            return
        }
        isShown = false
        isDismissing = true
        visibleFlow.value = false
    }

    override fun doOnShow(listener: QMUIModal.Action): QMUIModal {
        onShowListeners.add(listener)
        return this
    }

    override fun doOnDismiss(listener: QMUIModal.Action): QMUIModal {
        onDismissListeners.add(listener)
        return this
    }

    override fun removeOnShowAction(listener: QMUIModal.Action): QMUIModal {
        onShowListeners.remove(listener)
        return this
    }

    override fun removeOnDismissAction(listener: QMUIModal.Action): QMUIModal {
        onDismissListeners.remove(listener)
        return this
    }
}

internal class StillModalImpl(
    rootLayout: FrameLayout,
    onBackPressedDispatcher: OnBackPressedDispatcher,
    mask: Color = DefaultMaskColor,
    systemCancellable: Boolean = true,
    maskTouchBehavior: MaskTouchBehavior = MaskTouchBehavior.dismiss,
    val content: @Composable (modal: QMUIModal) -> Unit
) : QMUIModalPresent(rootLayout, onBackPressedDispatcher, mask, systemCancellable, maskTouchBehavior) {

    @Composable
    override fun ModalContent(visible: Boolean, dismissFinishAction: () -> Unit) {
        if (visible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(mask)
                    .let {
                        if (maskTouchBehavior == MaskTouchBehavior.penetrate) {
                            it
                        } else {
                            it.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                enabled = maskTouchBehavior == MaskTouchBehavior.dismiss
                            ) {
                                dismiss()
                            }
                        }
                    }
            )
            content(this)
        } else {
            DisposableEffect("") {
                onDispose {
                    dismissFinishAction()
                }
            }
        }
    }
}

internal class AnimateModalImpl(
    rootLayout: FrameLayout,
    onBackPressedDispatcher: OnBackPressedDispatcher,
    mask: Color = DefaultMaskColor,
    systemCancellable: Boolean = true,
    maskTouchBehavior: MaskTouchBehavior = MaskTouchBehavior.dismiss,
    val enter: EnterTransition = fadeIn(tween(), 0f),
    val exit: ExitTransition = fadeOut(tween(), 0f),
    val content: @Composable AnimatedVisibilityScope.(modal: QMUIModal) -> Unit
) : QMUIModalPresent(rootLayout, onBackPressedDispatcher, mask, systemCancellable, maskTouchBehavior) {

    @Composable
    override fun ModalContent(visible: Boolean, dismissFinishAction: () -> Unit) {
        AnimatedVisibility(
            visible = visible,
            enter = enter,
            exit = exit
        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(mask)
                .let {
                    if (maskTouchBehavior == MaskTouchBehavior.penetrate) {
                        it
                    } else {
                        it.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = maskTouchBehavior == MaskTouchBehavior.dismiss
                        ) {
                            dismiss()
                        }
                    }
                }
            )
            content(this@AnimateModalImpl)
            DisposableEffect("") {
                onDispose {
                    dismissFinishAction()
                }
            }
        }
    }
}

