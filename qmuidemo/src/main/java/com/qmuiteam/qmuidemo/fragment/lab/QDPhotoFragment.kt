package com.qmuiteam.qmuidemo.fragment.lab

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.qmuiteam.compose.ui.QMUITopBarBackIconItem
import com.qmuiteam.compose.ui.QMUITopBarWithLazyScrollState
import com.qmuiteam.photo.compose.QMUIPhotoThumbnail
import com.qmuiteam.photo.compose.QMUIPhotoThumbnailWithViewer
import com.qmuiteam.photo.data.PhotoTransitionProviderRecover
import com.qmuiteam.photo.data.QMUIPhoto
import com.qmuiteam.photo.data.QMUIPhotoProvider
import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord
import com.qmuiteam.qmuidemo.R
import com.qmuiteam.qmuidemo.base.ComposeBaseFragment
import com.qmuiteam.qmuidemo.lib.annotation.Widget

@Widget(name = "QMUI Photo", iconRes = R.mipmap.icon_grid_in_progress)
@LatestVisitRecord
class QDPhotoFragment : ComposeBaseFragment() {

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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        QMUIPhotoThumbnailWithViewer(
                            requireActivity(),
                            images = listOf(
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9979/31y68oGufDGL3zQ6TT.jpg",
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
                            requireActivity(),
                            images = listOf(
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png",
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
                            requireActivity(),
                            images = listOf(
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg",
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
                            requireActivity(),
                            images = listOf(
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg",
                                    0.749f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png",
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
                            requireActivity(),
                            images = listOf(
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg",
                                    0.749f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png",
                                    1.379f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9979/31y68oGufDGL3zQ6TT.jpg",
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
                            requireActivity(),
                            images = listOf(
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg",
                                    0.749f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png",
                                    1.379f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9979/31y68oGufDGL3zQ6TT.jpg",
                                    1f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg",
                                    0.749f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png",
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
                            requireActivity(),
                            images = listOf(
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg",
                                    0.749f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png",
                                    1.379f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9979/31y68oGufDGL3zQ6TT.jpg",
                                    1f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg",
                                    0.749f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png",
                                    1.379f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg",
                                    0.749f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9136/1yn0KLFwy6Vb0nE6Sg.png",
                                    1.379f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/8779/6WY7guGLeGfp0KK6Sb.jpeg",
                                    0.749f
                                ),
                                CoilPhotoProvider(
                                    "https://weread-picture-1258476243.file.myqcloud.com/9979/31y68oGufDGL3zQ6TT.jpg",
                                    1f
                                ),
                            )
                        )
                    }
                }
            }
        }
    }
}

class CoilPhoto(val url: String) : QMUIPhoto {

    @Composable
    override fun Compose(
        contentScale: ContentScale,
        isContainerFixed: Boolean,
        isLongImage: Boolean,
        onSuccess: ((Drawable) -> Unit)?,
        onError: ((Throwable) -> Unit)?
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .allowHardware(false)
                .listener(onError = { _, result ->
                    onError?.invoke(result.throwable)
                }) { _, result ->
                    onSuccess?.invoke(result.drawable)
                }.build(),
            contentDescription = "",
            contentScale = contentScale,
            alignment = if (isLongImage) Alignment.TopCenter else Alignment.Center,
            modifier = Modifier.let {
                if(isContainerFixed){
                    it.fillMaxSize()
                }else{
                    it
                }
            }
        )
    }

}

class CoilPhotoProvider(val url: String, val ratio: Float) : QMUIPhotoProvider {

    override fun thumbnail(): QMUIPhoto? {
        return photo()
    }

    override fun photo(): QMUIPhoto? {
        return CoilPhoto(url)
    }

    override fun ratio(): Float {
        return ratio
    }

    override fun meta(): Bundle? {
        return null
    }

    override fun recoverCls(): Class<in PhotoTransitionProviderRecover>? {
        return null
    }
}