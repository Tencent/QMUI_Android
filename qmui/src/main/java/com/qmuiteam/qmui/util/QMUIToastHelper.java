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

package com.qmuiteam.qmui.util;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

// Modify from https://github.com/didi/booster/blob/master/booster-android-instrument-toast/src/main/java/com/didiglobal/booster/instrument/ShadowToast.java
public class QMUIToastHelper {
    private static final String TAG = "QMUIToastHelper";
    public static void show(Toast toast){
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1){
            fixToastForAndroidN(toast).show();
        }else{
            toast.show();
        }
    }

    private static Toast fixToastForAndroidN(Toast toast){
        Object tn = QMUIReflectHelper.getFieldValue(toast, "mTN");
        if(tn == null){
            Log.w(TAG, "The value of field mTN of " + toast + " is null");
            return toast;
        }
        Object handler = QMUIReflectHelper.getFieldValue(tn, "mHandler");
        if(handler instanceof Handler){
            if(QMUIReflectHelper.setFieldValue(
                    handler, "mCallback", new FixCallback((Handler) handler))){
                return toast;
            }
        }

        final Object show = QMUIReflectHelper.getFieldValue(tn, "mShow");
        if (show instanceof Runnable) {
            if (QMUIReflectHelper.setFieldValue(tn, "mShow", new FixRunnable((Runnable) show))) {
                return toast;
            }
        }
        Log.w(TAG, "Neither field mHandler nor mShow of " + tn + " is accessible");
        return toast;
    }

    public static class FixCallback implements Handler.Callback {

        private final Handler mHandler;

        public FixCallback(final Handler handler) {
            mHandler = handler;
        }

        @Override
        public boolean handleMessage(@NonNull Message msg) {
            try {
                mHandler.handleMessage(msg);
            } catch (Throwable e) {
                // ignore
            }
            return true;
        }
    }

    public static class FixRunnable implements Runnable {

        private final Runnable mRunnable;

        public FixRunnable(final Runnable runnable) {
            mRunnable = runnable;
        }

        @Override
        public void run() {
            try {
                mRunnable.run();
            } catch (final RuntimeException e) {
                // ignore
            }
        }
    }
}
