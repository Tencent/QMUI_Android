package com.qmuiteam.photo.glide

import com.qmuiteam.photo.data.QMUIMediaModel
import com.qmuiteam.photo.data.QMUIMediaPhotoProviderFactory
import com.qmuiteam.photo.data.QMUIPhotoProvider

class QMUIMediaGlidePhotoProviderFactory : QMUIMediaPhotoProviderFactory {

    override fun factory(model: QMUIMediaModel): QMUIPhotoProvider {
        return QMUIGlidePhotoProvider(
            model.uri,
            model.ratio()
        )
    }
}