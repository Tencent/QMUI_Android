package com.qmuiteam.photo.activity

import android.Manifest
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.view.WindowCompat
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.qmuiteam.compose.core.helper.QMUIGlobal
import com.qmuiteam.compose.core.provider.QMUIWindowInsetsProvider
import com.qmuiteam.compose.core.ui.QMUITopBar
import com.qmuiteam.compose.core.ui.QMUITopBarBackIconItem
import com.qmuiteam.compose.core.ui.QMUITopBarItem
import com.qmuiteam.compose.core.ui.QMUITopBarWithLazyScrollState
import com.qmuiteam.photo.compose.*
import com.qmuiteam.photo.compose.picker.*
import com.qmuiteam.photo.data.*
import com.qmuiteam.photo.vm.*
import kotlinx.coroutines.flow.*

const val QMUI_PHOTO_DEFAULT_PICK_LIMIT_COUNT = 9
internal const val QMUI_PHOTO_RESULT_URI_LIST = "qmui_photo_result_uri_list"
internal const val QMUI_PHOTO_RESULT_ORIGIN_OPEN = "qmui_photo_result_origin_open"
internal const val QMUI_PHOTO_ENABLE_ORIGIN = "qmui_photo_enable_origin"
internal const val QMUI_PHOTO_PICK_LIMIT_COUNT = "qmui_photo_pick_limit_count"
internal const val QMUI_PHOTO_PROVIDER_FACTORY = "qmui_photo_provider_factory"

class QMUIPhotoPickItemInfo(
    val width: Int,
    val height: Int,
    val uri: Uri
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readParcelable(Uri::class.java.classLoader)!!
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(width)
        dest.writeInt(height)
        dest.writeParcelable(uri, flags)
    }

    companion object CREATOR : Parcelable.Creator<QMUIPhotoPickItemInfo> {
        override fun createFromParcel(parcel: Parcel): QMUIPhotoPickItemInfo {
            return QMUIPhotoPickItemInfo(parcel)
        }

        override fun newArray(size: Int): Array<QMUIPhotoPickItemInfo?> {
            return arrayOfNulls(size)
        }
    }

}

class QMUIPhotoPickResult(val list: List<QMUIPhotoPickItemInfo>, val isOriginOpen: Boolean)

fun Intent.getQMUIPhotoPickResult(): QMUIPhotoPickResult? {
    val list = getParcelableArrayListExtra<QMUIPhotoPickItemInfo>(QMUI_PHOTO_RESULT_URI_LIST) ?: return null
    if (list.isEmpty()) {
        return null
    }
    val isOriginOpen = getBooleanExtra(QMUI_PHOTO_RESULT_ORIGIN_OPEN, false)
    return QMUIPhotoPickResult(list, isOriginOpen)
}


open class QMUIPhotoPickerActivity : AppCompatActivity() {

    companion object {

        fun intentOf(
            activity: ComponentActivity,
            cls: Class<out QMUIPhotoPickerActivity>,
            factoryCls: Class<out QMUIMediaPhotoProviderFactory>,
            pickLimitCount: Int = QMUI_PHOTO_DEFAULT_PICK_LIMIT_COUNT,
            enableOrigin: Boolean = true
        ): Intent {
            val intent = Intent(activity, cls)
            intent.putExtra(QMUI_PHOTO_PICK_LIMIT_COUNT, pickLimitCount)
            intent.putExtra(QMUI_PHOTO_PROVIDER_FACTORY, factoryCls.name)
            intent.putExtra(QMUI_PHOTO_ENABLE_ORIGIN, enableOrigin)
            return intent
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        onHandlePermissionResult(it)
    }

    private val viewModel by viewModels<QMUIPhotoPickerViewModel>(factoryProducer = {
        object : AbstractSavedStateViewModelFactory(this@QMUIPhotoPickerActivity, intent?.extras) {
            override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
                val constructor = modelClass.getDeclaredConstructor(
                    Application::class.java,
                    SavedStateHandle::class.java,
                    QMUIMediaDataProvider::class.java,
                    Array<String>::class.java
                )
                return constructor.newInstance(
                    this@QMUIPhotoPickerActivity.application,
                    handle,
                    dataProvider(),
                    supportedMimeTypes()
                )
            }

        }
    })

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
        setContent {
            PageContent(viewModel)
        }
        onStartCheckPermission()
        onBackPressedDispatcher.addCallback(object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentScene = viewModel.photoPickerSceneFlow.value
                when (currentScene) {
                    is QMUIPhotoPickerEditScene -> {
                        viewModel.updateScene(currentScene.prevScene)
                    }
                    is QMUIPhotoPickerPreviewScene -> {
                        viewModel.updateScene(QMUIPhotoPickerGridScene)
                    }
                    else -> {
                        isEnabled = false
                        onBackPressed()
                        isEnabled = true
                    }
                }
            }

        })
    }

    @Composable
    protected open fun PageContent(viewModel: QMUIPhotoPickerViewModel) {
        QMUIDefaultPickerConfigProvider {
            QMUIWindowInsetsProvider {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(QMUILocalPickerConfig.current.screenBgColor)
                ) {
                    PhotoPicker(viewModel)
                }
            }
        }
    }

    @Composable
    protected open fun BoxScope.PhotoPicker(viewModel: QMUIPhotoPickerViewModel) {
        val data by viewModel.photoPickerDataFlow.collectAsState()
        when (data.state) {
            QMUIPhotoPickerLoadState.dataLoading,
            QMUIPhotoPickerLoadState.permissionChecking -> {
                Loading()
            }
            QMUIPhotoPickerLoadState.permissionDenied -> {
                PermissionDenied()
            }
            QMUIPhotoPickerLoadState.dataLoaded -> {
                val error = data.error
                val list = data.data
                if (error != null) {
                    PageError(error)
                } else if (list == null || list.isEmpty()) {
                    PageEmpty()
                } else {
                    PhotoPickerContent(viewModel, list)
                }
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    protected open fun BoxScope.PhotoPickerContent(
        viewModel: QMUIPhotoPickerViewModel,
        data: List<QMUIMediaPhotoBucketVO>
    ) {
        val pickedItems by viewModel.pickedListFlow.collectAsState()
        val scene by viewModel.photoPickerSceneFlow.collectAsState()
        AnimatedVisibility(
            visible = scene is QMUIPhotoPickerGridScene,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PhotoPickerGridContent(viewModel, data, pickedItems)
        }
        AnimatedVisibility(
            visible = scene is QMUIPhotoPickerPreviewScene,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f)
        ) {
            val previewScheme = scene as? QMUIPhotoPickerPreviewScene
            if (previewScheme != null) {
                val list = remember(previewScheme) {
                    if (previewScheme.onlySelected) {
                        viewModel.getPickedVOList()
                    } else {
                        data.find { it.id == previewScheme.buckedId }?.list ?: emptyList<QMUIMediaPhotoVO>()
                    }
                }
                PhotoPickerPreviewContent(viewModel, list, pickedItems, previewScheme.currentId)
            }

        }
    }

    @Composable
    protected open fun BoxScope.PhotoPickerGridContent(
        viewModel: QMUIPhotoPickerViewModel,
        data: List<QMUIMediaPhotoBucketVO>,
        pickedItems: List<Long>,
        topBarBackItem: QMUITopBarItem = remember {
            QMUITopBarBackIconItem {
                finish()
            }
        }
    ) {
        var currentBucket by remember {
            mutableStateOf(data.first())
        }

        val scrollState = rememberLazyListState()

        val bucketFlow = remember {
            MutableStateFlow(currentBucket.name)
        }.apply {
            value = currentBucket.name
        }

        val isFocusBucketFlow = remember {
            MutableStateFlow(false)
        }

        val config = QMUILocalPickerConfig.current
        val topBarBucketItem = remember(config) {
            config.topBarBucketFactory(bucketFlow, isFocusBucketFlow) {
                isFocusBucketFlow.value = !isFocusBucketFlow.value
            }
        }

        val isFocusBucketChooser by isFocusBucketFlow.collectAsState()

        val topBarSendItem = remember(config) {
            config.topBarSendFactory(false, viewModel.pickLimitCount, viewModel.pickedCountFlow) {
                onHandleSend()
            }
        }

        val topBarLeftItems = remember(topBarBackItem, topBarBucketItem) {
            arrayListOf(topBarBackItem, topBarBucketItem)
        }

        val topBarRightItems = remember(topBarSendItem) {
            arrayListOf(topBarSendItem)
        }

        Column(modifier = Modifier.fillMaxSize()) {
            QMUITopBarWithLazyScrollState(
                scrollState = scrollState,
                paddingEnd = 16.dp,
                separatorHeight = 0.dp,
                backgroundColor = QMUILocalPickerConfig.current.topBarBgColor,
                leftItems = topBarLeftItems,
                rightItems = topBarRightItems
            )
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val (content, toolbar) = createRefs()
                QMUIPhotoPickerGrid(
                    data = currentBucket.list,
                    modifier = Modifier.constrainAs(content) {
                        width = Dimension.fillToConstraints
                        height = Dimension.fillToConstraints
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(toolbar.top)
                    },
                    state = scrollState,
                    pickedItems = pickedItems,
                    onPickItem = { _, model ->
                        viewModel.togglePick(model)
                    },
                    onPreview = {
                        viewModel.updateScene(QMUIPhotoPickerPreviewScene(currentBucket.id, false, it.id))
                    }
                )
                QMUIPhotoPickerGridPageToolBar(
                    modifier = Modifier
                        .constrainAs(toolbar) {
                            width = Dimension.fillToConstraints
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                        },
                    enableOrigin = viewModel.enableOrigin,
                    pickedItems = pickedItems,
                    isOriginOpenFlow = viewModel.isOriginOpenFlow,
                    onToggleOrigin = {
                        viewModel.toggleOrigin(it)
                    }
                ) {
                    viewModel.updateScene(QMUIPhotoPickerPreviewScene(currentBucket.id, true, currentBucket.list.first().model.id))
                }
                QMUIPhotoBucketChooser(
                    focus = isFocusBucketChooser,
                    data = data,
                    currentId = currentBucket.id,
                    onBucketClick = {
                        currentBucket = it
                        isFocusBucketFlow.value = false
                    }) {
                    isFocusBucketFlow.value = false
                }
            }
        }
    }


    @OptIn(ExperimentalPagerApi::class)
    @Composable
    protected open fun BoxScope.PhotoPickerPreviewContent(
        viewModel: QMUIPhotoPickerViewModel,
        data: List<QMUIMediaPhotoVO>,
        pickedItems: List<Long>,
        currentId: Long
    ) {
        val config = QMUILocalPickerConfig.current
        var isFullPageState by remember {
            mutableStateOf(false)
        }
        val pagerState = remember(data, currentId) {
            PagerState(
                currentPage = data.indexOfFirst { it.model.id == currentId }.coerceAtLeast(0),
            )
        }

        val topBarLeftItems = remember {
            arrayListOf<QMUITopBarItem>(QMUITopBarBackIconItem {
                viewModel.updateScene(QMUIPhotoPickerGridScene)
            })
        }

        val topBarRightItems = remember(config) {
            arrayListOf(config.topBarSendFactory(true, viewModel.pickLimitCount, viewModel.pickedCountFlow) {
                onHandleSend()
            })
        }

        QMUIPhotoPickerPreview(
            pagerState,
            data,
            loading = { Loading() },
            loadingFailed = {},
        ) {
            isFullPageState = !isFullPageState
        }

        QMUITopBar(
            title = "${pagerState.currentPage + 1}/${data.size}",
            separatorHeight = 0.dp,
            backgroundColor = QMUILocalPickerConfig.current.topBarBgColor,
            leftItems = topBarLeftItems,
            rightItems = topBarRightItems
        )
    }


    @Composable
    protected open fun BoxScope.PhotoPickerEditContent(
        viewModel: QMUIPhotoPickerViewModel,
        data: List<QMUIMediaPhotoVO>,
        index: Int
    ) {

    }

    @Composable
    protected open fun BoxScope.Loading() {
        Box(modifier = Modifier.align(Alignment.Center)) {
            QMUIPhotoLoading(lineColor = QMUILocalPickerConfig.current.loadingColor)
        }
    }

    @Composable
    protected open fun BoxScope.PermissionDenied() {
        CommonTip(text = "选择图片需要存储权限\n请先前往设置打开存储权限")
    }

    @Composable
    protected open fun BoxScope.PageError(throwable: Throwable) {
        val text = if (QMUIGlobal.debug) {
            "读取数据发生错误, ${throwable.message}"
        } else {
            "读取数据发生错误"
        }
        CommonTip(text = text)
    }

    @Composable
    protected open fun BoxScope.PageEmpty() {
        CommonTip(text = "你的相册空空如也~")
    }

    @Composable
    protected open fun BoxScope.CommonTip(text: String) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(20.dp)
        ) {
            Text(
                text,
                fontSize = 16.sp,
                color = QMUILocalPickerConfig.current.tipTextColor,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }

    protected open fun onHandleSend() {
        val pickedList = viewModel.getPickedResultList()
        setResult(RESULT_OK, Intent().apply {
            putParcelableArrayListExtra(QMUI_PHOTO_RESULT_URI_LIST, arrayListOf<QMUIPhotoPickItemInfo>().apply {
                addAll(pickedList)
            })
            putExtra(QMUI_PHOTO_RESULT_ORIGIN_OPEN, viewModel.isOriginOpenFlow.value)
        })
        finish()
    }

    protected open fun onStartCheckPermission() {
        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    protected open fun onHandlePermissionResult(granted: Boolean) {
        if (granted) {
            viewModel.permissionGranted()
        } else {
            viewModel.permissionDenied()
        }
    }

    protected open fun dataProvider(): QMUIMediaDataProvider {
        return QMUIMediaImagesProvider()
    }

    protected open fun supportedMimeTypes(): Array<String> {
        return QMUIMediaImagesProvider.DEFAULT_SUPPORT_MIMETYPES
    }
}
