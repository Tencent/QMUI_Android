package com.qmuiteam.qmui.exposure

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import com.qmuiteam.qmui.util.QMUIViewHelper
import java.util.*

private val rect = Rect()

interface ExposureChecker {

    fun canExpose(target: View): Boolean {
        return target.defaultCanExpose()
    }

    fun isExposedInContainer(container: ViewGroup, target: View): Boolean
}


class FastAreaExposureChecker(val percent: Float) : ExposureChecker {
    override fun isExposedInContainer(container: ViewGroup, target: View): Boolean {
        if (target.width <= 0 || target.height <= 0) {
            return false
        }
        QMUIViewHelper.getDescendantRect(container, target, rect)
        if (rect.left >= container.width || rect.top >= container.height || rect.right <= 0 || rect.bottom <= 0) {
            return false
        }
        if (rect.left < 0) {
            rect.left = 0
        }
        if (rect.right > container.width) {
            rect.right = container.width
        }
        if (rect.top < 0) {
            rect.top = 0
        }
        if (rect.bottom > container.height) {
            rect.bottom = container.height
        }
        return (rect.width() * rect.height() * 1f) / (target.width * target.height) >= percent
    }
}

class AreaExposureChecker(val percent: Float) : ExposureChecker {
    override fun isExposedInContainer(container: ViewGroup, target: View): Boolean {
        if (target.width <= 0 || target.height <= 0) {
            return false
        }
        val hasVisibleArea = QMUIViewHelper.getDescendantVisibleRect(container, target, rect)
        if (!hasVisibleArea) {
            return false
        }
        return (rect.width() * rect.height() * 1f) / (target.width * target.height) >= percent
    }
}

val fastFullExposureChecker = FastAreaExposureChecker(1f)
val fullExposureChecker = AreaExposureChecker(1f)

val defaultExposureChecker = AreaExposureChecker(0.80f)


fun interface CustomExposureTriggerListener {
    fun doCheck()
}


class CustomExposureTrigger {

    private val listeners = mutableListOf<CustomExposureTriggerListener>()
    private var isTriggering = false
    private val pendingActions = LinkedList<PendingAction>()

    fun addListener(listener: CustomExposureTriggerListener) {
        if (isTriggering) {
            pendingActions.add(PendingAction(listener, true))
        } else {
            listeners.add(listener)
        }

    }

    fun removeListener(listener: CustomExposureTriggerListener) {
        if (isTriggering) {
            pendingActions.add(PendingAction(listener, true))
        } else {
            listeners.remove(listener)
        }
    }

    fun trigger() {
        isTriggering = true
        listeners.forEach {
            it.doCheck()
        }
        isTriggering = false
        var pendingAction = pendingActions.poll()
        while (pendingAction != null) {
            if (pendingAction.isDelete) {
                removeListener(pendingAction.listener)
            } else {
                addListener(pendingAction.listener)
            }
            pendingAction = pendingActions.poll()
        }
    }

    private class PendingAction(
        val listener: CustomExposureTriggerListener,
        val isDelete: Boolean
    )
}
