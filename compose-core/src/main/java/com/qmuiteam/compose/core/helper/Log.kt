package com.qmuiteam.compose.core.helper

import android.util.Log

interface QMUILogDelegate {
    fun e(tag: String, msg: String, throwable: Throwable? = null)
    fun w(tag: String, msg: String, throwable: Throwable? = null)
    fun i(tag: String, msg: String, throwable: Throwable? = null)
    fun d(tag: String, msg: String, throwable: Throwable? = null)
}

object SystemLogDelegate : QMUILogDelegate {

    override fun e(tag: String, msg: String, throwable: Throwable?) {
        Log.e(tag, msg, throwable)
    }

    override fun w(tag: String, msg: String, throwable: Throwable?) {
        Log.w(tag, msg, throwable)
    }

    override fun i(tag: String, msg: String, throwable: Throwable?) {
        Log.i(tag, msg, throwable)
    }

    override fun d(tag: String, msg: String, throwable: Throwable?) {
        Log.d(tag, msg, throwable)
    }
}

object QMUILog {

    var delegate: QMUILogDelegate? = SystemLogDelegate

    fun e(tag: String, msg: String, throwable: Throwable? = null) {
        delegate?.e(tag, msg, throwable)
    }

    fun w(tag: String, msg: String, throwable: Throwable? = null) {
        delegate?.w(tag, msg, throwable)
    }

    fun i(tag: String, msg: String, throwable: Throwable? = null) {
        delegate?.i(tag, msg, throwable)
    }

    fun d(tag: String, msg: String, throwable: Throwable? = null) {
        delegate?.d(tag, msg, throwable)
    }
}