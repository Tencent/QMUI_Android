package com.qmuiteam.qmuidemo.fragment.lab

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.qmuiteam.compose.core.ui.QMUITopBarBackIconItem
import com.qmuiteam.compose.core.ui.QMUITopBarTextItem
import com.qmuiteam.compose.core.ui.QMUITopBarWithLazyScrollState
import com.qmuiteam.photo.activity.QMUIPhotoPickResult
import com.qmuiteam.photo.activity.QMUIPhotoPickerActivity
import com.qmuiteam.photo.activity.getQMUIPhotoPickResult
import com.qmuiteam.photo.coil.QMUICoilPhotoProvider
import com.qmuiteam.photo.coil.QMUIMediaCoilPhotoProviderFactory
import com.qmuiteam.photo.compose.QMUIPhotoThumbnailWithViewer
import com.qmuiteam.photo.util.QMUIPhotoHelper
import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord
import com.qmuiteam.qmuidemo.R
import com.qmuiteam.qmuidemo.base.ComposeBaseFragment
import com.qmuiteam.qmuidemo.lib.annotation.Widget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Widget(name = "QMUI Photo", iconRes = R.mipmap.icon_grid_in_progress)
@LatestVisitRecord
class QDPhotoFragment : ComposeBaseFragment() {

    val pickerFlow = MutableStateFlow<QMUIPhotoPickResult?>(null)

    private val pickLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val pickerResult = it.data?.getQMUIPhotoPickResult() ?: return@registerForActivityResult
            pickerFlow.value = pickerResult
        }
    }

    @Composable
    override fun PageContent() {
        Column(modifier = Modifier.fillMaxSize()) {
            val scrollState = rememberLazyListState()
            QMUITopBarWithLazyScrollState(
                scrollState = scrollState,
                title = "QMUIPhoto",
                leftItems = arrayListOf(
                    QMUITopBarBackIconItem {
                        popBackStack()
                    }
                ),
                rightItems = arrayListOf(
                    QMUITopBarTextItem("Pick a Picture") {
                        val activity = activity ?: return@QMUITopBarTextItem
                        pickLauncher.launch(
                            QMUIPhotoPickerActivity.intentOf(
                                activity,
                                QMUIPhotoPickerActivity::class.java,
                                QMUIMediaCoilPhotoProviderFactory::class.java
                            )
                        )

                    }
                )
            )
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White),
                contentPadding = PaddingValues(start = 44.dp)
            ) {

                item {
                    PickerResult()
                }

//                item {
//                    TestImageCompress()
//                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        QMUIPhotoThumbnailWithViewer(
                            activity = requireActivity(),
                            images = listOf(
                                QMUICoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9979/31y68oGufDGL3zQ6TT.jpg".toUri(),
                                    1f
                                )
                            )
                        )
                    }

                }


                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        QMUIPhotoThumbnailWithViewer(
                            activity = requireActivity(),
                            images = listOf(
                                QMUICoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png".toUri(),
                                    1.379f
                                )
                            )
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        QMUIPhotoThumbnailWithViewer(
                            activity = requireActivity(),
                            images = listOf(
                                QMUICoilPhotoProvider(
                                    "file:///android_asset/test.png".toUri(),
                                    0.0125f
                                )
                            )
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        QMUIPhotoThumbnailWithViewer(
                            activity = requireActivity(),
                            images = listOf(
                                QMUICoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg".toUri(),
                                    0.749f
                                )
                            )
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        QMUIPhotoThumbnailWithViewer(
                            activity = requireActivity(),
                            images = listOf(
                                QMUICoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg".toUri(),
                                    0.749f
                                ),
                                QMUICoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png".toUri(),
                                    1.379f
                                )
                            )
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        QMUIPhotoThumbnailWithViewer(
                            activity = requireActivity(),
                            images = listOf(
                                QMUICoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg".toUri(),
                                    0.749f
                                ),
                                QMUICoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png".toUri(),
                                    1.379f
                                ),
                                QMUICoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9979/31y68oGufDGL3zQ6TT.jpg".toUri(),
                                    1f
                                )
                            )
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        QMUIPhotoThumbnailWithViewer(
                            activity = requireActivity(),
                            images = listOf(
                                QMUICoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg".toUri(),
                                    0.749f
                                ),
                                QMUICoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png".toUri(),
                                    1.379f
                                ),
                                QMUICoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9979/31y68oGufDGL3zQ6TT.jpg".toUri(),
                                    1f
                                ),
                                QMUICoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg".toUri(),
                                    0.749f
                                ),
                                QMUICoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png".toUri(),
                                    1.379f
                                ),
                            )
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        QMUIPhotoThumbnailWithViewer(
                            activity = requireActivity(),
                            images = listOf(
                                QMUICoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg".toUri(),
                                    0.749f
                                ),
                                QMUICoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png".toUri(),
                                    1.379f
                                ),
                                QMUICoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9979/31y68oGufDGL3zQ6TT.jpg".toUri(),
                                    1f
                                ),
                                QMUICoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg".toUri(),
                                    0.749f
                                ),
                                QMUICoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png".toUri(),
                                    1.379f
                                ),
                                QMUICoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg".toUri(),
                                    0.749f
                                ),
                                QMUICoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png".toUri(),
                                    1.379f
                                ),
                                QMUICoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg".toUri(),
                                    0.749f
                                ),
                                QMUICoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9979/31y68oGufDGL3zQ6TT.jpg".toUri(),
                                    1f
                                ),
                            )
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun PickerResult() {
        val pickResultState = pickerFlow.collectAsState()
        val pickResult = pickResultState.value
        if (pickResult == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .clickable {
                        val activity = activity ?: return@clickable
                        pickLauncher.launch(
                            QMUIPhotoPickerActivity.intentOf(
                                activity,
                                QMUIPhotoPickerActivity::class.java,
                                QMUIMediaCoilPhotoProviderFactory::class.java
                            )
                        )
                    }
            ) {
                Text("No Picked Images, click to pick")
            }
        } else {
            val images = remember(pickResult) {
                pickResult.list.map {
                    QMUICoilPhotoProvider(
                        it.uri,
                        it.ratio()
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Text(text = "原图：${pickResult.isOriginOpen}")
                QMUIPhotoThumbnailWithViewer(
                    activity = requireActivity(),
                    images = images
                )
            }
        }


    }

    @Composable
    fun TestImageCompress() {
        var bitmap by remember {
            mutableStateOf<Bitmap?>(null)
        }
        LaunchedEffect("") {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    QMUIPhotoHelper.compressByShortEdgeWidthAndByteSize(
                        requireContext(),
                        {
                            it.assets.open("test.png")
                        },
                        500
                    )?.inputStream().use {
                        if (it != null) {
                            bitmap = BitmapFactory.decodeStream(it)
                        }
                    }
                }
            }
        }

        if (bitmap != null) {
            Image(painter = BitmapPainter(bitmap!!.asImageBitmap()), contentDescription = "")
        }
    }
}