package com.qmuiteam.qmuidemo.fragment.lab

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.qmuiteam.photo.coil.QMUICoilPhotoProvider
import com.qmuiteam.photo.compose.QMUIPhotoClipper
import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord
import com.qmuiteam.qmuidemo.R
import com.qmuiteam.qmuidemo.base.ComposeBaseFragment
import com.qmuiteam.qmuidemo.lib.annotation.Widget

@Widget(name = "QMUI Photo Clip", iconRes = R.mipmap.icon_grid_in_progress)
@LatestVisitRecord
class QDPhotoClipFragment : ComposeBaseFragment() {

    @Composable
    override fun PageContent() {
        var ret by remember {
            mutableStateOf<Bitmap?>(null)
        }
        QMUIPhotoClipper(
            photoProvider = QMUICoilPhotoProvider(
                "https://weread-picture-1258476243.file.myqcloud.com/9979/31y68oGufDGL3zQ6TT.jpg".toUri(),
                0f
            )
        ) { doClip ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Box(modifier = Modifier
                    .weight(1f)
                    .clickable {
                        popBackStack()
                    }
                    .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "取消",
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(modifier = Modifier
                    .weight(1f)
                    .clickable {
                        ret = doClip()
                    }
                    .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "确定",
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            ret?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = "")
            }

        }
    }
}