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

package com.qmuiteam.qmui.arch.scheme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qmuiteam.qmui.arch.QMUIFragment;
import com.qmuiteam.qmui.arch.QMUIFragmentActivity;

import java.util.Map;

public interface QMUISchemeFragmentFactory {

    @Nullable
    QMUIFragment factory(@NonNull Class<? extends QMUIFragment> fragmentCls,
                         @Nullable Map<String, SchemeValue> scheme,
                         @NonNull String origin);

    @Nullable
    Bundle factory(@Nullable Map<String, SchemeValue> scheme, @NonNull String origin);

    @Nullable
    Intent factory(@NonNull Activity activity,
                   @NonNull Class<? extends QMUIFragmentActivity>[] activityClassList,
                   @NonNull Class<? extends QMUIFragment> fragmentCls,
                   @Nullable Map<String, SchemeValue> scheme,
                   @NonNull String origin);

    void startActivity(@NonNull Activity activity, @NonNull Intent intent);

    int startFragmentAndDestroyCurrent(QMUIFragmentActivity activity, QMUIFragment fragment);

    int startFragment(QMUIFragmentActivity activity, QMUIFragment fragment);

    boolean shouldBlockJump(@NonNull Activity activity,
                            @NonNull Class<? extends QMUIFragment> fragmentCls,
                            @Nullable Map<String, SchemeValue> scheme);
}
