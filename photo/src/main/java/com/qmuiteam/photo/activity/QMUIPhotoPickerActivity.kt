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
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
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
import androidx.lifecycle.viewModelScope
import com.qmuiteam.compose.core.helper.LogTag
import com.qmuiteam.compose.core.helper.QMUIGlobal
import com.qmuiteam.compose.core.helper.QMUILog
import com.qmuiteam.compose.core.provider.QMUIWindowInsetsProvider
import com.qmuiteam.compose.core.ui.QMUITopBarBackIconItem
import com.qmuiteam.compose.core.ui.QMUITopBarItem
import com.qmuiteam.compose.core.ui.QMUITopBarWithLazyScrollState
import com.qmuiteam.photo.compose.*
import com.qmuiteam.photo.compose.picker.*
import com.qmuiteam.photo.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val QMUI_PHOTO_DEFAULT_PICK_LIMIT_COUNT = 9
private const val QMUI_PHOTO_RESULT_URI_LIST = "qmui_photo_result_uri_list"
private const val QMUI_PHOTO_RESULT_ORIGIN_OPEN = "qmui_photo_result_origin_open"
private const val QMUI_PHOTO_ENABLE_ORIGIN = "qmui_photo_enable_origin"
private const val QMUI_PHOTO_PICK_LIMIT_COUNT = "qmui_photo_pick_limit_count"
private const val QMUI_PHOTO_PROVIDER_FACTORY = "qmui_photo_provider_factory"

class QMUIPhotoPickItemInfo(
    val width: Int,
    val height: Int,
    val uri: Uri
): Parcelable {
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
    if(list.isEmpty()){
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

    @Composable
    protected open fun BoxScope.PhotoPickerContent(
        viewModel: QMUIPhotoPickerViewModel,
        data: List<QMUIMediaPhotoBucketVO>
    ) {
        val pickedItems by viewModel.pickedListFlow.collectAsState()
        val scene by viewModel.photoPickerSceneFlow.collectAsState()
        if (scene is QMUIPhotoPickerGridScene) {
            PhotoPickerGridContent(viewModel, data, pickedItems)
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
            config.topBarSendFactory(viewModel.pickLimitCount, viewModel.pickedCountFlow) {
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
                    modifier = Modifier.constrainAs(content){
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
                        // onPreview
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
                ){
                    // onPreview
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
    protected open fun BoxScope.PhotoPickerPagerContent(
        viewModel: QMUIPhotoPickerViewModel,
        data: List<QMUIMediaPhotoVO>,
        index: Int
    ) {

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
        val pickedList = viewModel.getPickedList()
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


class QMUIPhotoPickerViewModel @Keep constructor(
    val application: Application,
    val state: SavedStateHandle,
    val dataProvider: QMUIMediaDataProvider,
    val supportedMimeTypes: Array<String>
) : ViewModel(), LogTag {

    val pickLimitCount = state.get<Int>(QMUI_PHOTO_PICK_LIMIT_COUNT) ?: QMUI_PHOTO_DEFAULT_PICK_LIMIT_COUNT

    val enableOrigin = state.get<Boolean>(QMUI_PHOTO_ENABLE_ORIGIN) ?: true

    private val photoProviderFactory: QMUIMediaPhotoProviderFactory

    private val _photoPickerSceneFlow = MutableStateFlow<QMUIPhotoPickerScene>(QMUIPhotoPickerGridScene)
    val photoPickerSceneFlow = _photoPickerSceneFlow.asStateFlow()

    private val _photoPickerDataFlow = MutableStateFlow(QMUIPhotoPickerData(QMUIPhotoPickerLoadState.permissionChecking, null))
    val photoPickerDataFlow = _photoPickerDataFlow.asStateFlow()

    private val _pickedMap = mutableMapOf<Long, QMUIMediaModel>()
    private val _pickedListFlow = MutableStateFlow<List<Long>>(emptyList())
    val pickedListFlow = _pickedListFlow.asStateFlow()

    private val _pickedCountFlow = MutableStateFlow(0)
    val pickedCountFlow = _pickedCountFlow.asStateFlow()

    private val _isOriginOpenFlow = MutableStateFlow(false)
    val isOriginOpenFlow = _isOriginOpenFlow.asStateFlow()

    init {
        val photoProviderFactoryClsName =
            state.get<String>(QMUI_PHOTO_PROVIDER_FACTORY) ?: throw RuntimeException("no QMUIMediaPhotoProviderFactory is provided.")
        photoProviderFactory = Class.forName(photoProviderFactoryClsName).newInstance() as QMUIMediaPhotoProviderFactory
    }

    fun updateScene(scene: QMUIPhotoPickerScene) {
        _photoPickerSceneFlow.value = scene
    }

    fun permissionDenied() {
        _photoPickerDataFlow.value = QMUIPhotoPickerData(QMUIPhotoPickerLoadState.permissionDenied, null)
    }

    fun permissionGranted() {
        _photoPickerDataFlow.value = QMUIPhotoPickerData(QMUIPhotoPickerLoadState.dataLoading, null)
        viewModelScope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    dataProvider.provide(application, supportedMimeTypes).map { bucket ->
                        QMUIMediaPhotoBucketVO(bucket.id, bucket.name, bucket.list.map {
                            QMUIMediaPhotoVO(it, photoProviderFactory.factory(it))
                        })
                    }
                }
                _photoPickerDataFlow.value = QMUIPhotoPickerData(QMUIPhotoPickerLoadState.dataLoaded, data)
            } catch (e: Throwable) {
                _photoPickerDataFlow.value = QMUIPhotoPickerData(QMUIPhotoPickerLoadState.dataLoaded, null, e)
            }
        }
    }

    fun toggleOrigin(toOpen: Boolean) {
        _isOriginOpenFlow.value = toOpen
    }

    fun togglePick(model: QMUIMediaModel) {
        if (_photoPickerDataFlow.value.state != QMUIPhotoPickerLoadState.dataLoaded) {
            QMUILog.w(TAG, "pick when data is not finish loaded, please check why this method called here?")
            return
        }
        val list = arrayListOf<Long>()
        list.addAll(_pickedListFlow.value)
        if (list.contains(model.id)) {
            _pickedMap.remove(model.id)
            list.remove(model.id)
            _pickedListFlow.value = list
            _pickedCountFlow.value = list.size
        } else {
            if (list.size >= pickLimitCount) {
                QMUILog.w(TAG, "can not pick more photo, please check why this method called here?")
                return
            }
            _pickedMap[model.id] = model
            list.add(model.id)
            _pickedListFlow.value = list
            _pickedCountFlow.value = list.size
        }
    }

    fun getPickedList(): List<QMUIPhotoPickItemInfo> {
        return _pickedListFlow.value.mapNotNull { id ->
            _pickedMap[id]?.let {
                QMUIPhotoPickItemInfo(it.width, it.height, it.uri)
            }
        }
    }
}

open class QMUIPhotoPickerScene

object QMUIPhotoPickerGridScene : QMUIPhotoPickerScene()

class QMUIPhotoPickerPagerScene(
    val buckedId: String,
    val current: QMUIMediaModel
) : QMUIPhotoPickerScene()

class QMUIPhotoPickerEditScene(
    val prevScene: QMUIPhotoPickerScene,
    val current: QMUIMediaModel
) : QMUIPhotoPickerScene()

enum class QMUIPhotoPickerLoadState {
    permissionChecking, permissionDenied, dataLoading, dataLoaded
}

class QMUIPhotoPickerData(
    val state: QMUIPhotoPickerLoadState,
    val data: List<QMUIMediaPhotoBucketVO>?,
    val error: Throwable? = null
)
