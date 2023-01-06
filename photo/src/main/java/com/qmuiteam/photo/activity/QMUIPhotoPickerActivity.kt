package com.qmuiteam.photo.activity

import android.Manifest
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
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
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
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
import kotlinx.coroutines.launch

const val QMUI_PHOTO_DEFAULT_PICK_LIMIT_COUNT = 9
internal const val QMUI_PHOTO_RESULT_URI_LIST = "qmui_photo_result_uri_list"
internal const val QMUI_PHOTO_RESULT_ORIGIN_OPEN = "qmui_photo_result_origin_open"
internal const val QMUI_PHOTO_ENABLE_ORIGIN = "qmui_photo_enable_origin"
internal const val QMUI_PHOTO_PICK_LIMIT_COUNT = "qmui_photo_pick_limit_count"
internal const val QMUI_PHOTO_PICKED_ITEMS = "qmui_photo_picked_items"
internal const val QMUI_PHOTO_PROVIDER_FACTORY = "qmui_photo_provider_factory"

class QMUIPhotoPickItemInfo(
    val id: Long,
    val name: String,
    val width: Int,
    val height: Int,
    val uri: Uri,
    val rotation: Int
) : Parcelable {

    fun ratio(): Float {
        if(height <= 0 || width <= 0){
            return -1f
        }
        if(rotation == 90 || rotation == 270){
            return height.toFloat() / width
        }
        return width.toFloat() / height
    }

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readParcelable(Uri::class.java.classLoader)!!,
        parcel.readInt()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeString(name)
        dest.writeInt(width)
        dest.writeInt(height)
        dest.writeParcelable(uri, flags)
        dest.writeInt(rotation)

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
            pickedItems: ArrayList<Uri> = arrayListOf(),
            pickLimitCount: Int = QMUI_PHOTO_DEFAULT_PICK_LIMIT_COUNT,
            enableOrigin: Boolean = true
        ): Intent {
            val intent = Intent(activity, cls)
            intent.putExtra(QMUI_PHOTO_PICK_LIMIT_COUNT, pickLimitCount)
            intent.putParcelableArrayListExtra(QMUI_PHOTO_PICKED_ITEMS, pickedItems)
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
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (viewModel.photoPickerSceneFlow.value) {
                    is QMUIPhotoPickerEditScene -> {
                        viewModel.updateScene(viewModel.prevScene ?: QMUIPhotoPickerGridScene)
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
        val sceneState = viewModel.photoPickerSceneFlow.collectAsState()
        val scene = sceneState.value

        AnimatedVisibility(
            visible = scene is QMUIPhotoPickerGridScene,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PhotoPickerGridScene(viewModel, data, pickedItems)
        }
        AnimatedVisibility(
            visible = scene is QMUIPhotoPickerPreviewScene,
            enter = if(viewModel.prevScene !is QMUIPhotoPickerEditScene) fadeIn() + scaleIn(initialScale = 0.8f) else fadeIn(initialAlpha = 1f),
            exit = if(scene !is QMUIPhotoPickerEditScene) fadeOut() + scaleOut(targetScale = 0.8f) else fadeOut(targetAlpha = 1f)
        ) {
            // For exit animation
            val previewSceneHolder = remember {
                SceneHolder(scene as? QMUIPhotoPickerPreviewScene)
            }
            if(scene is QMUIPhotoPickerPreviewScene){
                previewSceneHolder.scene = scene
            }
            val previewScene = previewSceneHolder.scene
            if (previewScene != null) {
                PhotoPickerPreviewScene(viewModel, previewScene, data, pickedItems)
            }
        }
        AnimatedVisibility(
            visible = scene is QMUIPhotoPickerEditScene,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f)
        ) {
            val editSceneHolder = remember {
                SceneHolder(scene as? QMUIPhotoPickerEditScene)
            }
            if(scene is QMUIPhotoPickerEditScene){
                editSceneHolder.scene = scene
            }
            val editScene = editSceneHolder.scene
            if (editScene != null) {
                PhotoPickerEditScene(viewModel, editScene)
            }
        }
    }

    @Composable
    protected open fun BoxScope.PhotoPickerGridScene(
        viewModel: QMUIPhotoPickerViewModel,
        data: List<QMUIMediaPhotoBucketVO>,
        pickedItems: List<Long>,
        topBarBackItem: QMUITopBarItem = remember {
            QMUITopBarBackIconItem {
                finish()
            }
        }
    ) {

        LaunchedEffect("") {
            WindowCompat.getInsetsController(window, window.decorView)?.show(WindowInsetsCompat.Type.statusBars())
        }

        var currentBucket by remember {
            mutableStateOf(data.first())
        }

        val scrollState = viewModel.gridSceneScrollState

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
                onHandleSend(viewModel.getPickedResultList())
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
                QMUIPhotoPickerGridToolBar(
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

    @Composable
    protected open fun BoxScope.PhotoPickerPreviewScene(
        viewModel: QMUIPhotoPickerViewModel,
        scene: QMUIPhotoPickerPreviewScene,
        data: List<QMUIMediaPhotoBucketVO>,
        pickedItems: List<Long>
    ) {
        val list = remember(scene) {
            if (scene.onlySelected) {
                viewModel.getPickedVOList()
            } else {
                data.find { it.id == scene.buckedId }?.list ?: emptyList<QMUIMediaPhotoVO>()
            }
        }
        PhotoPickerPreviewContent(viewModel, list, pickedItems, scene)
    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    protected open fun BoxScope.PhotoPickerPreviewContent(
        viewModel: QMUIPhotoPickerViewModel,
        data: List<QMUIMediaPhotoVO>,
        pickedItems: List<Long>,
        scene: QMUIPhotoPickerPreviewScene
    ) {
        val config = QMUILocalPickerConfig.current
        var isFullPageState by remember {
            mutableStateOf(false)
        }
        LaunchedEffect(isFullPageState) {
            WindowCompat.getInsetsController(window, window.decorView)?.let {
                if (!isFullPageState) {
                    it.show(WindowInsetsCompat.Type.statusBars())
                } else {
                    it.hide(WindowInsetsCompat.Type.statusBars())
                }

            }
        }
        val pagerState = remember(data, scene.currentId) {
            PagerState(
                currentPage = data.indexOfFirst { it.model.id == scene.currentId }.coerceAtLeast(0),
            )
        }

        val topBarLeftItems = remember {
            arrayListOf<QMUITopBarItem>(QMUITopBarBackIconItem {
                viewModel.updateScene(QMUIPhotoPickerGridScene)
            })
        }

        val topBarRightItems = remember(config) {
            arrayListOf(config.topBarSendFactory(true, viewModel.pickLimitCount, viewModel.pickedCountFlow) {
                val pickedList = viewModel.getPickedResultList()
                if(pickedList.isEmpty()){
                    onHandleSend(listOf(data[pagerState.currentPage].let {
                        QMUIPhotoPickItemInfo(
                            it.model.id,
                            it.model.name,
                            it.model.width,
                            it.model.height,
                            it.model.uri,
                            it.model.rotation
                        )
                    }))
                }else{
                    onHandleSend(pickedList)
                }

            })
        }

        val scope = rememberCoroutineScope()

        Box(modifier = Modifier.fillMaxSize()) {
            QMUIPhotoPickerPreview(
                pagerState,
                data,
                loading = { Loading() },
                loadingFailed = {},
            ) {
                isFullPageState = !isFullPageState
            }

            AnimatedVisibility(
                visible = !isFullPageState,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it })
            ) {
                QMUITopBar(
                    title = "${pagerState.currentPage + 1}/${data.size}",
                    separatorHeight = 0.dp,
                    paddingEnd = 16.dp,
                    backgroundColor = QMUILocalPickerConfig.current.topBarBgColor,
                    leftItems = topBarLeftItems,
                    rightItems = topBarRightItems
                )
            }

            AnimatedVisibility(
                visible = !isFullPageState,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    QMUIPhotoPickerPreviewPickedItems(data, pickedItems, data[pagerState.currentPage].model.id) {
                        scope.launch {
                            pagerState.scrollToPage(data.indexOf(it))
                        }
                    }

                    val isCurrentPicked = remember(data, pickedItems, pagerState.currentPage) {
                        pickedItems.indexOf(data[pagerState.currentPage].model.id) >= 0
                    }

                    QMUIPhotoPickerPreviewToolBar(
                        modifier = Modifier.fillMaxWidth(),
                        current = data[pagerState.currentPage],
                        isCurrentPicked = isCurrentPicked,
                        enableOrigin = viewModel.enableOrigin,
                        isOriginOpenFlow = viewModel.isOriginOpenFlow,
                        onToggleOrigin = {
                            viewModel.toggleOrigin(it)
                        },
                        onEdit = {
                            viewModel.updateScene(QMUIPhotoPickerEditScene(data[pagerState.currentPage]))
                        },
                        onToggleSelect = {
                            viewModel.togglePick(data[pagerState.currentPage])
                        }
                    )

                }
            }
        }
    }


    @Composable
    protected open fun BoxScope.PhotoPickerEditScene(
        viewModel: QMUIPhotoPickerViewModel,
        scene: QMUIPhotoPickerEditScene
    ) {
        LaunchedEffect("") {
            WindowCompat.getInsetsController(window, window.decorView)?.hide(WindowInsetsCompat.Type.statusBars())
        }
        QMUIPhotoPickerEdit(onBackPressedDispatcher, scene.current) {
            viewModel.updateScene(viewModel.prevScene ?: QMUIPhotoPickerGridScene)
        }
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

    protected open fun onHandleSend(pickedList: List<QMUIPhotoPickItemInfo>) {
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

    private class SceneHolder<T:QMUIPhotoPickerScene>(var scene: T? = null)
}


