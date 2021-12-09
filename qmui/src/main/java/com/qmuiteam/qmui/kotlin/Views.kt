package com.qmuiteam.qmui.kotlin

import android.os.SystemClock
import android.view.View
import com.qmuiteam.qmui.R
import com.qmuiteam.qmui.skin.QMUISkinHelper
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder

fun throttleClick(wait: Long = 200, block: ((View) -> Unit)): View.OnClickListener {

    return View.OnClickListener { v ->
        val current = SystemClock.uptimeMillis()
        val lastClickTime = (v.getTag(R.id.qmui_click_timestamp) as? Long) ?: 0
        if (current - lastClickTime > wait) {
            v.setTag(R.id.qmui_click_timestamp, current)
            block(v)
        }
    }
}

fun debounceClick(wait: Long = 200, block: ((View) -> Unit)): View.OnClickListener {
    return View.OnClickListener { v ->
        var action = (v.getTag(R.id.qmui_click_debounce_action) as? DebounceAction)
        if(action == null){
            action = DebounceAction(v, block)
            v.setTag(R.id.qmui_click_debounce_action, action)
        }else{
            action.block = block
        }
        v.removeCallbacks(action)
        v.postDelayed(action, wait)
    }
}

class DebounceAction(val view: View,  var block: ((View) -> Unit)): Runnable {
    override fun run() {
        if(view.isAttachedToWindow){
            block(view)
        }
    }
}

fun View.onClick(wait: Long = 200, block: ((View) -> Unit)) {
    setOnClickListener(throttleClick(wait, block))
}

fun View.onDebounceClick(wait: Long = 200, block: ((View) -> Unit)) {
    setOnClickListener(debounceClick(wait, block))
}

fun View.skin(increment: Boolean = false, block:(QMUISkinValueBuilder.() -> Unit)){
    val builder = QMUISkinValueBuilder.acquire()
    if(increment){
        val oldSkinValue = getTag(R.id.qmui_skin_value)
        if(oldSkinValue is String){
            builder.convertFrom(oldSkinValue)
        }
    }
    builder.block()
    QMUISkinHelper.setSkinValue(this, builder)
    builder.release()
}

fun View.clearSkin(){
    QMUISkinHelper.setSkinValue(this, "")
}
