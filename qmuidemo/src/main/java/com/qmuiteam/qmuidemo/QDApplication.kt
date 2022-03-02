/*
 * Tencent is pleased to support the open source community by making QMUI_Android available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qmuiteam.qmuidemo

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.res.Configuration
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.soloader.SoLoader
import com.qmuiteam.qmui.QMUILog
import com.qmuiteam.qmui.QMUILog.QMUILogDelegate
import com.qmuiteam.qmui.arch.QMUISwipeBackActivityManager
import com.qmuiteam.qmui.qqface.QMUIQQFaceCompiler
import com.qmuiteam.qmuidemo.manager.QDSkinManager
import com.qmuiteam.qmuidemo.manager.QDUpgradeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcrash.TombstoneManager
import xcrash.XCrash
import java.io.File


/**
 * Demo 的 Application 入口。
 * Created by cgine on 16/3/22.
 */
class QDApplication : Application(), ImageLoaderFactory {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        XCrash.init(this)
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        QMUILog.setDelegete(object : QMUILogDelegate {
            override fun e(tag: String, msg: String, vararg obj: Any) {
                Log.e(tag, msg)
            }

            override fun w(tag: String, msg: String, vararg obj: Any) {
                Log.w(tag, msg)
            }

            override fun i(tag: String, msg: String, vararg obj: Any) {
                Log.i(tag, msg)
            }

            override fun d(tag: String, msg: String, vararg obj: Any) {
                Log.d(tag, msg)
            }

            override fun printErrStackTrace(tag: String, tr: Throwable, format: String, vararg obj: Any) {}
        })
        QDUpgradeManager.getInstance(this).check()
        QMUISwipeBackActivityManager.init(this)
        QMUIQQFaceCompiler.setDefaultQQFaceManager(QDQQFaceManager.getInstance())
        QDSkinManager.install(this)
        if(BuildConfig.DEBUG){
            SoLoader.init(this, false)
            val client = AndroidFlipperClient.getInstance(this)
            client.addPlugin(InspectorFlipperPlugin(context, DescriptorMapping.withDefaults()))
            client.start()
        }

        GlobalScope.launch(Dispatchers.IO) {
            delay(5000)
            for (file in TombstoneManager.getAllTombstones()) {
                try {
                    val contentValues = ContentValues()
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "txt")
                    val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues) ?: continue
                    contentResolver.openOutputStream(uri)?.use { out ->
                        file.inputStream().use { ins ->
                            ins.copyTo(out)
                        }
                    }
                    file.delete()
                }catch (ignore: Throwable){

                }

            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            QDSkinManager.changeSkin(QDSkinManager.SKIN_DARK)
        } else if (QDSkinManager.getCurrentSkin() == QDSkinManager.SKIN_DARK) {
            QDSkinManager.changeSkin(QDSkinManager.SKIN_BLUE)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(applicationContext)
            .crossfade(true)
            .build()
    }

    companion object {
        @JvmStatic
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
            private set
    }
}