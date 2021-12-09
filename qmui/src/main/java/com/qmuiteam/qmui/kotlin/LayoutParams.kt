package com.qmuiteam.qmui.kotlin

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout

val matchParent: Int = ViewGroup.LayoutParams.MATCH_PARENT
val wrapContent: Int = ViewGroup.LayoutParams.WRAP_CONTENT
val matchConstraint: Int = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
val constraintParentId = ConstraintLayout.LayoutParams.PARENT_ID

fun ConstraintLayout.LayoutParams.alignParent4(){
    leftToLeft = constraintParentId
    rightToRight = constraintParentId
    topToTop = constraintParentId
    bottomToBottom = constraintParentId
}

fun ConstraintLayout.LayoutParams.alignParentHor(){
    leftToLeft = constraintParentId
    rightToRight = constraintParentId
}

fun ConstraintLayout.LayoutParams.alignParentVer(){
    topToTop = constraintParentId
    bottomToBottom = constraintParentId
}

fun ConstraintLayout.LayoutParams.alignParentLeftTop(){
    topToTop = constraintParentId
    leftToLeft = constraintParentId
}

fun ConstraintLayout.LayoutParams.alignParentLeftBottom(){
    bottomToBottom = constraintParentId
    leftToLeft = constraintParentId
}

fun ConstraintLayout.LayoutParams.alignParentRightTop(){
    topToTop = constraintParentId
    rightToRight = constraintParentId
}

fun ConstraintLayout.LayoutParams.alignParentRightBottom(){
    bottomToBottom = constraintParentId
    rightToRight = constraintParentId
}

fun ConstraintLayout.LayoutParams.alignView4(id: Int){
    leftToLeft = id
    rightToRight = id
    topToTop = id
    bottomToBottom = id
}

fun ConstraintLayout.LayoutParams.alignViewHor(id: Int){
    leftToLeft = id
    rightToRight = id
}

fun ConstraintLayout.LayoutParams.alignViewVer(id: Int){
    topToTop = id
    bottomToBottom = id
}

fun ConstraintLayout.LayoutParams.alignViewLeftTop(id: Int){
    topToTop = id
    leftToLeft = id
}

fun ConstraintLayout.LayoutParams.alignViewLeftBottom(id: Int){
    bottomToBottom = id
    leftToLeft = id
}

fun ConstraintLayout.LayoutParams.alignViewRightTop(id: Int){
    topToTop = id
    rightToRight = id
}

fun ConstraintLayout.LayoutParams.alignViewRightBottom(id: Int){
    bottomToBottom = id
    rightToRight = id
}