package com.qmuiteam.qmui.type

import com.qmuiteam.qmui.type.element.Element

interface LineIndentHandler {
    fun reset()
    fun processIndent(typeModel: TypeModel, firstElement: Element, newParagraph: Boolean): Int
}

class SerialLineIndentHandler(
    serials: List<Pair<Int, Int>>,
    private val followIndentForNewParagraphIfNeeded: Boolean = false): LineIndentHandler {

    private val sorted = serials.sortedBy {
        it.first
    }

    var currentIntend = 0
    private var pendingCalculatePair: Pair<Int, Int>? = null
    var nextIndex = 0

    override fun reset() {
        currentIntend = 0
        nextIndex = 0
    }

    override fun processIndent(typeModel: TypeModel, firstElement: Element, newParagraph: Boolean): Int {
        if(newParagraph){
            val pair = sorted.find { it.first == firstElement.index }
            if(pair != null){
                currentIntend = 0
            }else if(!followIndentForNewParagraphIfNeeded){
                currentIntend = 0
            } else {
                // make sure it's calculated.
                pendingCalculatePair?.let {
                    var indent = 0
                    for(i in it.first until it.second + 1){
                        indent += typeModel[i]?.measureWidth ?: 0
                    }
                    currentIntend = indent
                }
            }
            pendingCalculatePair = pair
        }else{
            pendingCalculatePair?.let {
                var indent = 0
                for(i in it.first until it.second + 1){
                    indent += typeModel[i]?.measureWidth ?: 0
                }
                currentIntend = indent
            }
            pendingCalculatePair = null
        }
        return currentIntend
    }
}