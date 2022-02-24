package com.qmuiteam.compose.modal

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.isVisible
import kotlinx.coroutines.flow.MutableStateFlow

internal abstract class QMUIModalPresent(
    private val rootLayout: FrameLayout,
    private val onBackPressedDispatcher: OnBackPressedDispatcher,
    val mask: Color = DefaultMaskColor,
    val systemCancellable: Boolean = true,
    val maskCancellable: Boolean = true
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
        composeLayout.isVisible = false
        composeLayout.disposeComposition()
        rootLayout.removeView(composeLayout)
        onBackPressedCallback.remove()
    }

    @Composable
    abstract fun ModalContent(visible: Boolean, dismissFinishAction: () -> Unit)

    override fun isShowing(): Boolean {
        return isShown
    }

    override fun show() {
        if (isShown || isDismissing) {
            return
        }
        isShown = true
        rootLayout.addView(composeLayout, generateLayoutParams())
        composeLayout.visibility = View.VISIBLE
        visibleFlow.value = true
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
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

    override fun doOnShow(listener: QMUIModal.Action) {
        onShowListeners.add(listener)
    }

    override fun doOnDismiss(listener: QMUIModal.Action) {
        onDismissListeners.add(listener)
    }

    override fun removeOnShowAction(listener: QMUIModal.Action) {
        onShowListeners.remove(listener)
    }

    override fun removeOnDismissAction(listener: QMUIModal.Action) {
        onDismissListeners.remove(listener)
    }
}

internal class StillModalImpl(
    rootLayout: FrameLayout,
    onBackPressedDispatcher: OnBackPressedDispatcher,
    mask: Color = DefaultMaskColor,
    systemCancellable: Boolean = true,
    maskCancellable: Boolean = true,
    val content: @Composable (modal: QMUIModal) -> Unit
) : QMUIModalPresent(rootLayout, onBackPressedDispatcher, mask, systemCancellable, maskCancellable) {

    @Composable
    override fun ModalContent(visible: Boolean, dismissFinishAction: () -> Unit) {
        if (visible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(mask)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = maskCancellable
                    ) {
                        dismiss()
                    }
            )
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
    maskCancellable: Boolean = true,
    val durationMillis: Int = 300,
    val content: @Composable AnimatedVisibilityScope.(modal: QMUIModal) -> Unit
) : QMUIModalPresent(rootLayout, onBackPressedDispatcher, mask, systemCancellable, maskCancellable) {

    @Composable
    override fun ModalContent(visible: Boolean, dismissFinishAction: () -> Unit) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(durationMillis), 0f),
            exit = fadeOut(tween(durationMillis), 0f)
        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(mask)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = maskCancellable
                ) {
                    dismiss()
                })
            content(this@AnimateModalImpl)
            DisposableEffect("") {
                onDispose {
                    dismissFinishAction()
                }
            }
        }
    }
}

