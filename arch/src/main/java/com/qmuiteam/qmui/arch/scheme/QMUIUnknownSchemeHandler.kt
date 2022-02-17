package com.qmuiteam.qmui.arch.scheme

interface QMUIUnknownSchemeHandler {
    fun handle(handler: QMUISchemeHandler, handleContext: SchemeHandleContext, schemeInfo: SchemeInfo): Boolean
}