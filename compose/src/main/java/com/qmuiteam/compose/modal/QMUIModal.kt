package com.qmuiteam.compose.modal

import android.view.View
import android.view.Window
import android.widget.FrameLayout
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.DisposableEffectResult
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import com.qmuiteam.compose.ui.qmuiPrimaryColor

val DefaultMaskColor = Color.Black.copy(alpha = 0.5f)

private class ModalHolder(var current: QMUIModal? = null)

class QMUIModalAction(
    val text: String,
    val enabled: Boolean = true,
    val color: Color = qmuiPrimaryColor,
    val onClick: (QMUIModal) -> Unit
)

@Composable
fun QMUIModal(
    isVisible: Boolean,
    mask: Color = DefaultMaskColor,
    durationMillis: Int = 300,
    systemCancellable: Boolean = true,
    maskCancellable: Boolean = true,
    doOnShow: QMUIModal.Action? = null,
    doOnDismiss: QMUIModal.Action? = null,
    modalHostProvider: ModalHostProvider = DefaultModalHostProvider,
    content: @Composable AnimatedVisibilityScope.(QMUIModal) -> Unit
) {
    val modalHolder = remember {
        ModalHolder(null)
    }
    if (isVisible) {
        if (modalHolder.current == null) {
            val modal = LocalView.current.qmuiModal(mask, systemCancellable, maskCancellable, durationMillis, modalHostProvider, content)
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
    fun show()
    fun dismiss()
    fun isShowing(): Boolean

    fun doOnShow(listener: Action)
    fun doOnDismiss(listener: Action)
    fun removeOnShowAction(listener: Action)
    fun removeOnDismissAction(listener: Action)

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
    maskCancellable: Boolean = true,
    durationMillis: Int = 300,
    modalHostProvider: ModalHostProvider = DefaultModalHostProvider,
    content: @Composable AnimatedVisibilityScope.(QMUIModal) -> Unit
): QMUIModal {
    if (!isAttachedToWindow) {
        throw RuntimeException("View is not attached to window")
    }
    val modalHost = modalHostProvider.provide(this)
    return AnimateModalImpl(
        modalHost.first,
        modalHost.second,
        mask, systemCancellable, maskCancellable, durationMillis, content)
}

fun View.qmuiStillModal(
    mask: Color = DefaultMaskColor,
    systemCancellable: Boolean = true,
    maskCancellable: Boolean = true,
    modalHostProvider: ModalHostProvider = DefaultModalHostProvider,
    content: @Composable (QMUIModal) -> Unit
): QMUIModal {
    if (!isAttachedToWindow) {
        throw RuntimeException("View is not attached to window")
    }
    val modalHost = modalHostProvider.provide(this)
    return StillModalImpl(modalHost.first, modalHost.second, mask, systemCancellable, maskCancellable, content)
}