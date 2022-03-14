package com.qmuiteam.photo.compose

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import com.qmuiteam.photo.data.QMUIBitmapRegionProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun QMUIBitmapRegionItem(bmRegion: QMUIBitmapRegionProvider, w: Dp, h: Dp) {
    var bitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }
    LaunchedEffect(key1 = bmRegion) {
        withContext(Dispatchers.IO) {
            bitmap = bmRegion.loader.load()
        }
    }
    Box(modifier = Modifier.size(w, h)) {
        val bm = bitmap
        if (bm != null) {
            Image(
                painter = BitmapPainter(bm.asImageBitmap()),
                contentDescription = "",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}