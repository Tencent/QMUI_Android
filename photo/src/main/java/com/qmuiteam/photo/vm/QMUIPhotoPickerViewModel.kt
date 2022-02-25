package com.qmuiteam.photo.vm

import android.app.Application
import androidx.annotation.Keep
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qmuiteam.compose.core.helper.LogTag
import com.qmuiteam.compose.core.helper.QMUILog
import com.qmuiteam.photo.activity.*
import com.qmuiteam.photo.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    var prevScene: QMUIPhotoPickerScene? = null
        private set

    private val _photoPickerDataFlow = MutableStateFlow(QMUIPhotoPickerData(QMUIPhotoPickerLoadState.permissionChecking, null))
    val photoPickerDataFlow = _photoPickerDataFlow.asStateFlow()

    private val _pickedMap = mutableMapOf<Long, QMUIMediaPhotoVO>()
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
        prevScene = _photoPickerSceneFlow.value
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

    fun togglePick(item: QMUIMediaPhotoVO) {
        if (_photoPickerDataFlow.value.state != QMUIPhotoPickerLoadState.dataLoaded) {
            QMUILog.w(TAG, "pick when data is not finish loaded, please check why this method called here?")
            return
        }
        val list = arrayListOf<Long>()
        list.addAll(_pickedListFlow.value)
        if (list.contains(item.model.id)) {
            _pickedMap.remove(item.model.id)
            list.remove(item.model.id)
            _pickedListFlow.value = list
            _pickedCountFlow.value = list.size
        } else {
            if (list.size >= pickLimitCount) {
                QMUILog.w(TAG, "can not pick more photo, please check why this method called here?")
                return
            }
            _pickedMap[item.model.id] = item
            list.add(item.model.id)
            _pickedListFlow.value = list
            _pickedCountFlow.value = list.size
        }
    }

    fun getPickedVOList(): List<QMUIMediaPhotoVO>{
        return _pickedListFlow.value.mapNotNull { id ->
            _pickedMap[id]
        }
    }

    fun getPickedResultList(): List<QMUIPhotoPickItemInfo> {
        return _pickedListFlow.value.mapNotNull { id ->
            _pickedMap[id]?.model?.let {
                QMUIPhotoPickItemInfo(it.width, it.height, it.uri)
            }
        }
    }
}

open class QMUIPhotoPickerScene

object QMUIPhotoPickerGridScene : QMUIPhotoPickerScene()

class QMUIPhotoPickerPreviewScene(
    val buckedId: String,
    val onlySelected: Boolean,
    val currentId: Long
) : QMUIPhotoPickerScene()

class QMUIPhotoPickerEditScene(
    val current: QMUIMediaPhotoVO
) : QMUIPhotoPickerScene()


enum class QMUIPhotoPickerLoadState {
    permissionChecking, permissionDenied, dataLoading, dataLoaded
}

class QMUIPhotoPickerData(
    val state: QMUIPhotoPickerLoadState,
    val data: List<QMUIMediaPhotoBucketVO>?,
    val error: Throwable? = null
)