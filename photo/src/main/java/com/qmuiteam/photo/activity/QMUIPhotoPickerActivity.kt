package com.qmuiteam.photo.activity

import android.Manifest
import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qmuiteam.compose.core.helper.LogTag
import com.qmuiteam.compose.core.helper.QMUIGlobal
import com.qmuiteam.compose.core.helper.QMUILog
import com.qmuiteam.photo.compose.QMUIDefaultPickerConfigProvider
import com.qmuiteam.photo.compose.QMUILocalPickerConfig
import com.qmuiteam.photo.compose.QMUIPhotoLoading
import com.qmuiteam.photo.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val QMUI_PHOTO_DEFAULT_PICK_LIMIT_COUNT = 9
private const val QMUI_PHOTO_PICK_LIMIT_COUNT = "qmui_photo_pick_limit_count"
private const val QMUI_PHOTO_PROVIDER_FACTORY = "qmui_photo_provider_factory"


open class QMUIPhotoPickerActivity : AppCompatActivity() {

    companion object {

        fun intentOf(
            activity: ComponentActivity,
            cls: Class<out QMUIPhotoPickerActivity>,
            factoryCls: Class<out QMUIMediaPhotoProviderFactory>,
            pickLimitCount: Int = QMUI_PHOTO_DEFAULT_PICK_LIMIT_COUNT
        ): Intent {
            val intent = Intent(activity, cls)
            intent.putExtra(QMUI_PHOTO_PICK_LIMIT_COUNT, pickLimitCount)
            intent.putExtra(QMUI_PHOTO_PROVIDER_FACTORY, factoryCls.name)
            return intent
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        onHandlePermissionResult(it)
    }

    private val viewModel by viewModels<QMUIPhotoPickerViewModel>(factoryProducer = {
        object : AbstractSavedStateViewModelFactory(this@QMUIPhotoPickerActivity, null) {
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(QMUILocalPickerConfig.current.backgroundColor)
            ) {
                PhotoPicker(viewModel)
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
        data: List<QMUIMediaPhotoVO>
    ) {
        val scene by viewModel.photoPickerSceneFlow.collectAsState()
        if (scene is QMUIPhotoPickerGridScene) {
            PhotoPickerGridContent(viewModel, data)
        }
    }

    @Composable
    protected open fun BoxScope.PhotoPickerGridContent(
        viewModel: QMUIPhotoPickerViewModel,
        data: List<QMUIMediaPhotoVO>
    ) {


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


class QMUIPhotoPickerViewModel(
    val application: Application,
    val state: SavedStateHandle,
    val dataProvider: QMUIMediaDataProvider,
    val supportedMimeTypes: Array<String>
) : ViewModel(), LogTag {
    val pickLimitCount = state.get<Int>(QMUI_PHOTO_PICK_LIMIT_COUNT) ?: QMUI_PHOTO_DEFAULT_PICK_LIMIT_COUNT
    private val photoProviderFactory: QMUIMediaPhotoProviderFactory

    private val _photoPickerSceneFlow = MutableStateFlow<QMUIPhotoPickerScene>(QMUIPhotoPickerGridScene)
    val photoPickerSceneFlow: StateFlow<QMUIPhotoPickerScene>
        get() = _photoPickerSceneFlow

    private val _photoPickerDataFlow = MutableStateFlow(QMUIPhotoPickerData(QMUIPhotoPickerLoadState.permissionChecking, null))
    val photoPickerDataFlow: StateFlow<QMUIPhotoPickerData>
        get() = _photoPickerDataFlow

    private val _pickedIndexList = mutableListOf<Int>()
    val pickedIndexList: List<Int>
        get() = _pickedIndexList

    private val _enablePickMoreFlow = MutableStateFlow(true)
    val enablePickMoreFlow: StateFlow<Boolean>
        get() = _enablePickMoreFlow

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
                    dataProvider.provide(application, supportedMimeTypes).map {
                        QMUIMediaPhotoVO(it, photoProviderFactory.factory(it))
                    }
                }
                _photoPickerDataFlow.value = QMUIPhotoPickerData(QMUIPhotoPickerLoadState.dataLoaded, data)
            } catch (e: Throwable) {
                _photoPickerDataFlow.value = QMUIPhotoPickerData(QMUIPhotoPickerLoadState.dataLoaded, null, e)
            }
        }
    }

    fun togglePick(index: Int) {
        if (_photoPickerDataFlow.value.state != QMUIPhotoPickerLoadState.dataLoaded) {
            QMUILog.w(TAG, "pick when data is not finish loaded, please check why this method called here?")
            return
        }
        if (_pickedIndexList.contains(index)) {
            _pickedIndexList.remove(index)
            _enablePickMoreFlow.value = true
        } else {
            if (!_enablePickMoreFlow.value) {
                QMUILog.w(TAG, "can not pick more photo, please check why this method called here?")
                return
            }
            _pickedIndexList.add(index)
            if (_pickedIndexList.size >= pickLimitCount) {
                _enablePickMoreFlow.value = false
            }
        }
    }
}

open class QMUIPhotoPickerScene

object QMUIPhotoPickerGridScene : QMUIPhotoPickerScene()
class QMUIPhotoPickerPagerScene(val currentIndex: Int) : QMUIPhotoPickerScene()
class QMUIPhotoPickerEditScene(val currentIndex: Int) : QMUIPhotoPickerScene()

enum class QMUIPhotoPickerLoadState {
    permissionChecking, permissionDenied, dataLoading, dataLoaded
}

class QMUIPhotoPickerData(
    val state: QMUIPhotoPickerLoadState,
    val data: List<QMUIMediaPhotoVO>?,
    val error: Throwable? = null
)
