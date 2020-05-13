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

package com.qmuiteam.qmui.arch.record;

import android.content.Intent;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface QMUILatestVisitStorage {

    int NOT_EXIST = -1;

    // Fragment stuff
    void saveFragmentRecordInfo(int id, @Nullable Map<String, RecordArgumentEditor.Argument> arguments);

    int getFragmentRecordId();

    @Nullable
    Map<String, RecordArgumentEditor.Argument> getFragmentArguments();


    void clearFragmentStorage();


    // Activity Stuff
    void saveActivityRecordInfo(int id, @Nullable Map<String, RecordArgumentEditor.Argument> arguments);

    int getActivityRecordId();

    void getAndWriteActivityArgumentsToIntent(@NonNull Intent intent);

    void clearActivityStorage();

    void clearAll();
}
