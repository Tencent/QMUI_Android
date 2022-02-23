package com.qmuiteam.photo.compose.picker

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QMUIPhotoPickCheckBox(pickIndex: Int){
    val config = QMUILocalPickerConfig.current
    val strokeWidth = with(LocalDensity.current){
        2.dp.toPx()
    }
    AnimatedVisibility(
        visible = pickIndex < 0,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()){
            drawCircle(
                color = config.commonCheckIconNormalTintColor,
                radius = (size.minDimension - strokeWidth) / 2.0f,
                style = Stroke(strokeWidth)
            )
        }
    }
    AnimatedVisibility(
        visible = pickIndex >= 0,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(config.commonCheckIconCheckedTintColor),
            contentAlignment = Alignment.Center
        ){
            if(transition.targetState != EnterExitState.PostExit){
                Text(
                    text = "${pickIndex + 1}",
                    color = config.commonCheckIconCheckedTextColor,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun QMUIPhotoPickRadio(
    checked: Boolean,
    ratioSize: Dp = 18.dp,
    strokeWidthDp: Dp = 1.6.dp
){
    Box(modifier = Modifier.size(ratioSize)){
        val strokeWidth = with(LocalDensity.current){
            strokeWidthDp.toPx()
        }
        val config = QMUILocalPickerConfig.current
        AnimatedVisibility(
            visible = !checked,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Canvas(modifier = Modifier.size(ratioSize)){
                drawCircle(
                    color = config.commonCheckIconNormalTintColor,
                    radius = (size.minDimension - strokeWidth) / 2.0f,
                    style = Stroke(strokeWidth)
                )
            }
        }
        AnimatedVisibility(
            visible = checked,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Canvas(modifier = Modifier.size(ratioSize)){
                drawCircle(
                    color = config.commonCheckIconCheckedTintColor,
                    radius = (size.minDimension - strokeWidth) / 2.0f,
                    style = Stroke(strokeWidth)
                )

                drawCircle(
                    color = config.commonCheckIconCheckedTintColor,
                    radius = (size.minDimension - strokeWidth * 4) / 2.0f,
                )
            }
        }
    }
}