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
package com.qmuiteam.qmui.arch.effect;

import android.content.Intent;

import androidx.annotation.Nullable;

public class FragmentResultEffect extends Effect {
    private final int mRequestFragmentUUid;
    private final int mResultCode;
    private final int mRequestCode;
    @Nullable
    private final Intent mIntent;

    public FragmentResultEffect(int requestFragmentUUid, int resultCode, int requestCode, @Nullable Intent intent) {
        mRequestFragmentUUid = requestFragmentUUid;
        mResultCode = resultCode;
        mRequestCode = requestCode;
        mIntent = intent;
    }

    public int getRequestCode() {
        return mRequestCode;
    }

    public int getResultCode() {
        return mResultCode;
    }

    public Intent getIntent() {
        return mIntent;
    }

    public int getRequestFragmentUUid() {
        return mRequestFragmentUUid;
    }
}