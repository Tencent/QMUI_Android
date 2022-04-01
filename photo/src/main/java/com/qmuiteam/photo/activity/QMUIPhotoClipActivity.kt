package com.qmuiteam.photo.activity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.qmuiteam.compose.core.provider.QMUIWindowInsetsProvider
import com.qmuiteam.photo.compose.QMUIPhotoClipper
import com.qmuiteam.photo.data.QMUIPhotoProvider
import com.qmuiteam.photo.util.saveToLocal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal const val QMUI_PHOTO_CLIP_URI = "qmui_photo_clip_uri"
internal const val QMUI_PHOTO_CLIP_SOURCE_RATIO = "qmui_photo_clip_source_ratio"

fun Intent.getQMUIPhotoClipResult(): Uri? {
    return getParcelableExtra(QMUI_PHOTO_CLIP_URI)
}

abstract class QMUIPhotoClipActivity : AppCompatActivity() {

    companion object {
        fun intentOf(
            activity: ComponentActivity,
            cls: Class<out QMUIPhotoClipActivity>,
            sourceUri: Uri,
            sourceRatio: Float = -1f
        ): Intent {
            val intent = Intent(activity, cls)

            intent.putExtra(QMUI_PHOTO_CLIP_URI, sourceUri)
            intent.putExtra(QMUI_PHOTO_CLIP_SOURCE_RATIO, sourceRatio)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView)?.let {
            it.isAppearanceLightNavigationBars = false
        }
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                window.navigationBarDividerColor = android.graphics.Color.TRANSPARENT
                window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        val uri = intent.getParcelableExtra<Uri>(QMUI_PHOTO_CLIP_URI)
        if (uri == null) {
            finish()
            return
        }
        val ratio = intent.getFloatExtra(QMUI_PHOTO_CLIP_SOURCE_RATIO, -1f)
        setContent {
            PageContent(uri, ratio)
        }
    }

    @Composable
    protected abstract fun photoProvider(uri: Uri, ratio: Float): QMUIPhotoProvider

    @Composable
    protected open fun PageContent(uri: Uri, ratio: Float) {
        Box(modifier = Modifier.background(Color.Black)) {
            QMUIWindowInsetsProvider {
                QMUIPhotoClipper(
                    photoProvider = photoProvider(uri, ratio)
                ) { doClip ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    ) {
                        Box(modifier = Modifier
                            .weight(1f)
                            .clickable {
                                finish()
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
                                doClip()?.let {
                                    handleResult(it)
                                }
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
                }
            }
        }
    }

    protected open fun handleResult(bitmap: Bitmap) {
        lifecycleScope.launch {
            val ret = kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    bitmap.saveToLocal(cacheDir)
                }
            }.getOrNull()
            setResult(RESULT_OK, Intent().apply {
                putExtra(QMUI_PHOTO_CLIP_URI, ret)
            })
        }
    }
}