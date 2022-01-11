package com.qmuiteam.qmui.arch.scheme

class SchemeInfo(
    val action: String,
    val params: MutableMap<String, String>,
    val origin: String
)


fun parseParamsToMap(schemeParams: String?, queryMap: MutableMap<String, String>) {
    if (schemeParams == null || schemeParams.isEmpty()) {
        return
    }
    var start = 0
    do {
        val next = schemeParams.indexOf('&', start)
        val end = if (next == -1) schemeParams.length else next
        if (start == end) {
            start += 1
            continue
        }
        var separator = schemeParams.indexOf('=', start)
        if (separator > end || separator == -1) {
            separator = end
        }
        if (separator == start) {
            start = end + 1
            continue
        }
        val name = schemeParams.substring(start, separator)
        val value = if (separator == end) "" else schemeParams.substring(separator + 1, end)
        queryMap[name] = value
        start = end + 1
    } while (start < schemeParams.length)
}