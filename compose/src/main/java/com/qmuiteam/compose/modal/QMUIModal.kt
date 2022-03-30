package com.qmuiteam.compose.modal

import android.os.SystemClock
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.DisposableEffectResult
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import com.qmuiteam.compose.R
import com.qmuiteam.compose.core.ui.qmuiPrimaryColor

val DefaultMaskColor = Color.Black.copy(alpha = 0.5f)

enum class MaskTouchBehavior{
    dismiss, penetrate, none
}

private class ModalHolder(var current: QMUIModal? = null)

class QMUIModalAction(
    val text: String,
    val enabled: Boolean = true,
    val color: Color = qmuiPrimaryColor,
    val onClick: (QMUIModal) -> Unit
)

private class ShowingModals {
    val modals = mutableMapOf<Long, QMUIModal>()
}

@Composable
fun QMUIModal(
    isVisible: Boolean,
    mask: Color = DefaultMaskColor,
    enter: EnterTransition = fadeIn(tween(), 0f),
    exit: ExitTransition = fadeOut(tween(), 0f),
    systemCancellable: Boolean = true,
    maskTouchBehavior: MaskTouchBehavior = MaskTouchBehavior.dismiss,
    doOnShow: QMUIModal.Action? = null,
    doOnDismiss: QMUIModal.Action? = null,
    uniqueId: Long = SystemClock.elapsedRealtimeNanos(),
    modalHostProvider: ModalHostProvider = DefaultModalHostProvider,
    content: @Composable AnimatedVisibilityScope.(QMUIModal) -> Unit
) {
    val modalHolder = remember {
        ModalHolder(null)
    }
    if (isVisible) {
        if (modalHolder.current == null) {
            val modal = LocalView.current.qmuiModal(
                mask,
                systemCancellable,
                maskTouchBehavior,
                uniqueId,
                modalHostProvider,
                enter,
                exit,
                content
            )
            doOnShow?.let { modal.doOnShow(it) }
            doOnDismiss?.let { modal.doOnDismiss(it) }
            modalHolder.current = modal
        }
    } else {
        modalHolder.current?.dismiss()
    }
    DisposableEffect("") {
        object : DisposableEffectResult {
            override fun dispose() {
                modalHolder.current?.dismiss()
            }
        }
    }
}

interface QMUIModal {
    fun show(): QMUIModal
    fun dismiss()
    fun isShowing(): Boolean

    fun doOnShow(listener: Action): QMUIModal
    fun doOnDismiss(listener: Action): QMUIModal
    fun removeOnShowAction(listener: Action): QMUIModal
    fun removeOnDismissAction(listener: Action): QMUIModal

    fun interface Action {
        fun invoke(modal: QMUIModal)
    }
}

fun interface ModalHostProvider {
    fun provide(view: View): Pair<FrameLayout, OnBackPressedDispatcher>
}

class ActivityHostModalProvider : ModalHostProvider {
    override fun provide(view: View): Pair<FrameLayout, OnBackPressedDispatcher> {
        val contentLayout =
            view.rootView.findViewById<FrameLayout>(Window.ID_ANDROID_CONTENT) ?: throw RuntimeException("View is not attached to Activity")
        val activity = contentLayout.context as? AppCompatActivity ?: throw RuntimeException("view's rootView's context is not AppCompatActivity")
        return contentLayout to activity.onBackPressedDispatcher
    }
}

val DefaultModalHostProvider = ActivityHostModalProvider()

fun View.qmuiModal(
    mask: Color = DefaultMaskColor,
    systemCancellable: Boolean = true,
    maskTouchBehavior: MaskTouchBehavior = MaskTouchBehavior.dismiss,
    uniqueId: Long = SystemClock.elapsedRealtimeNanos(),
    modalHostProvider: ModalHostProvider = DefaultModalHostProvider,
    enter: EnterTransition = fadeIn(tween(), 0f),
    exit: ExitTransition = fadeOut(tween(), 0f),
    content: @Composable AnimatedVisibilityScope.(QMUIModal) -> Unit
): QMUIModal {
    if (!isAttachedToWindow) {
        throw RuntimeException("View is not attached to window")
    }
    val modalHost = modalHostProvider.provide(this)
    val modal = AnimateModalImpl(
        modalHost.first,
        modalHost.second,
        mask,
        systemCancellable,
        maskTouchBehavior,
        enter,
        exit,
        content
    )
    val hostView = modalHost.first
    handleModelUnique(hostView, modal, uniqueId)
    return modal
}

fun View.qmuiStillModal(
    mask: Color = DefaultMaskColor,
    systemCancellable: Boolean = true,
    maskTouchBehavior: MaskTouchBehavior = MaskTouchBehavior.dismiss,
    uniqueId: Long = SystemClock.elapsedRealtimeNanos(),
    modalHostProvider: ModalHostProvider = DefaultModalHostProvider,
    content: @Composable (QMUIModal) -> Unit
): QMUIModal {
    if (!isAttachedToWindow) {
        throw RuntimeException("View is not attached to window")
    }
    val modalHost = modalHostProvider.provide(this)
    val modal = StillModalImpl(modalHost.first, modalHost.second, mask, systemCancellable, maskTouchBehavior, content)
    val hostView = modalHost.first
    handleModelUnique(hostView, modal, uniqueId)
    return modal
}

private fun handleModelUnique(hostView: FrameLayout, modal: QMUIModal, uniqueId: Long) {
    val showingModals = (hostView.getTag(R.id.qmui_modals) as? ShowingModals) ?: ShowingModals().also {
        hostView.setTag(R.id.qmui_modals, it)
    }

    modal.doOnShow {
        showingModals.modals.put(uniqueId, it)?.dismiss()
    }

    modal.doOnDismiss {
        if (showingModals.modals[uniqueId] == it) {
            showingModals.modals.remove(uniqueId)
        }
    }
}