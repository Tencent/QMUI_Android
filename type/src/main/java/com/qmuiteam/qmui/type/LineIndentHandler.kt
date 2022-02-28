package com.qmuiteam.qmui.type

import com.qmuiteam.qmui.type.element.Element

interface LineIndentHandler {
    fun getIndent(typeModel: TypeModel, firstElement: Element): Int
}

class SerialLineIndentHandler(serials: List<Pair<Int, Int>>): LineIndentHandler {

    private val sorted = serials.sortedBy {
        it.first
    }

    var prevIndexCache = -1

    override fun getIndent(typeModel: TypeModel, firstElement: Element): Int {
        if(sorted.isEmpty()){
            return 0
        }

        if(prevIndexCache >= 0 && firstElement.index > prevIndexCache){
            // check fast path
            if(prevIndexCache == sorted.size - 1 || sorted[prevIndexCache + 1].first > firstElement.index){
                return calculateIndent(typeModel, firstElement, prevIndexCache)
            } else if(prevIndexCache + 1 == sorted.size - 1 || sorted[prevIndexCache + 2].first > firstElement.index) {
                prevIndexCache += 1
                return calculateIndent(typeModel, firstElement, prevIndexCache)
            }else{
                prevIndexCache = -1
            }
        }

        val nextIndex = sorted.indexOfFirst { it.first > firstElement.index }
        if(nextIndex == 0){
            return 0
        }
        val prevIndex = if(nextIndex < 0) sorted.size - 1 else nextIndex - 1
        prevIndexCache = prevIndex
        return calculateIndent(typeModel, firstElement, prevIndex)
    }

    private fun calculateIndent(typeModel: TypeModel, firstElement: Element, index: Int): Int{
        val cur = sorted[index]
        if(firstElement.index <= cur.second){
            return 0
        }

        var indent = 0
        for(i in cur.first until cur.second + 1){
            indent += typeModel[i]?.measureWidth ?: 0
        }
        return indent
    }
}