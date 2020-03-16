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

package com.qmuiteam.qmui.arch;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.LayoutInflaterCompat;
import androidx.lifecycle.Lifecycle;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;

import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord;
import com.qmuiteam.qmui.arch.record.LatestVisitArgumentCollector;
import com.qmuiteam.qmui.arch.record.RecordArgumentEditor;
import com.qmuiteam.qmui.skin.QMUISkinLayoutInflaterFactory;
import com.qmuiteam.qmui.skin.QMUISkinManager;


//Fix the bug: Only fullscreen activities can request orientation in Android version 26, 27
class InnerBaseActivity extends AppCompatActivity implements LatestVisitArgumentCollector {
    private static int NO_REQUESTED_ORIENTATION_SET = -100;
    private boolean mConvertToTranslucentCauseOrientationChanged = false;
    private int mPendingRequestedOrientation = NO_REQUESTED_ORIENTATION_SET;
    private QMUISkinManager mSkinManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if(useQMUISkinLayoutInflaterFactory()){
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            LayoutInflaterCompat.setFactory2(layoutInflater,
                    new QMUISkinLayoutInflaterFactory(this, layoutInflater));
        }
        super.onCreate(savedInstanceState);
        mSkinManager = QMUISkinManager.defaultInstance(this);
    }

    void convertToTranslucentCauseOrientationChanged() {
        Utils.convertActivityToTranslucent(this);
        mConvertToTranslucentCauseOrientationChanged = true;
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        if (mConvertToTranslucentCauseOrientationChanged && (Build.VERSION.SDK_INT == Build.VERSION_CODES.O
                || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1)) {
            Log.i("InnerBaseActivity", "setRequestedOrientation when activity is translucent");
            mPendingRequestedOrientation = requestedOrientation;
        } else {
            super.setRequestedOrientation(requestedOrientation);
        }
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mConvertToTranslucentCauseOrientationChanged) {
            mConvertToTranslucentCauseOrientationChanged = false;
            Utils.convertActivityFromTranslucent(this);
            if (mPendingRequestedOrientation != NO_REQUESTED_ORIENTATION_SET) {
                super.setRequestedOrientation(mPendingRequestedOrientation);
                mPendingRequestedOrientation = NO_REQUESTED_ORIENTATION_SET;
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(mSkinManager != null){
            mSkinManager.register(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mSkinManager != null){
            mSkinManager.unRegister(this);
        }
    }

    private void checkLatestVisitRecord() {
        Class<? extends InnerBaseActivity> cls = getClass();
        if (!cls.isAnnotationPresent(LatestVisitRecord.class)) {
            QMUILatestVisit.getInstance(this).clearActivityLatestVisitRecord();
            return;
        }
        QMUILatestVisit.getInstance(this).performLatestVisitRecord(this);
    }

    @Override
    public void onCollectLatestVisitArgument(RecordArgumentEditor editor) {

    }

    public void setSkinManager(@Nullable QMUISkinManager skinManager){
        if(mSkinManager != null){
            mSkinManager.unRegister(this);
        }
        mSkinManager = skinManager;
        if(skinManager != null){
            if(getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)){
                skinManager.register(this);
            }
        }
    }

    protected boolean useQMUISkinLayoutInflaterFactory(){
        return true;
    }
}
