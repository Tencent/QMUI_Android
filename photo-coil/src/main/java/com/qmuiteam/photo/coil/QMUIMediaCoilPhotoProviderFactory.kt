package com.qmuiteam.photo.coil

import com.qmuiteam.photo.data.QMUIMediaModel
import com.qmuiteam.photo.data.QMUIMediaPhotoProviderFactory
import com.qmuiteam.photo.data.QMUIPhotoProvider

class QMUIMediaCoilPhotoProviderFactory : QMUIMediaPhotoProviderFactory {

    override fun factory(model: QMUIMediaModel): QMUIPhotoProvider {
        return QMUICoilPhotoProvider(
            model.uri,
            model.ratio()
        )
    }
}