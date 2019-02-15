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
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;


//Fix the bug: Only fullscreen activities can request orientation in Android version 26, 27
class InnerBaseActivity extends AppCompatActivity {
    private static int NO_REQUESTED_ORIENTATION_SET = -100;
    private boolean mConvertToTranslucentCauseOrientationChanged = false;
    private int mPendingRequestedOrientation = NO_REQUESTED_ORIENTATION_SET;

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
}
