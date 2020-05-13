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

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;


public class RecordArgumentEditorImpl implements RecordArgumentEditor {

    private HashMap<String, Argument> mMap = new HashMap<>();

    @Override

    public synchronized RecordArgumentEditor putString(String key, @Nullable String value) {
        mMap.put(key, new Argument(value, String.class));
        return this;
    }

    @Override
    public synchronized RecordArgumentEditor putInt(String key, int value) {
        mMap.put(key, new Argument(value, Integer.TYPE));
        return this;
    }

    @Override
    public synchronized RecordArgumentEditor putLong(String key, long value) {
        mMap.put(key, new Argument(value, Long.TYPE));
        return this;
    }

    @Override
    public synchronized RecordArgumentEditor putFloat(String key, float value) {
        mMap.put(key, new Argument(value, Float.TYPE));
        return this;
    }

    @Override
    public synchronized RecordArgumentEditor putBoolean(String key, boolean value) {
        mMap.put(key, new Argument(value, Boolean.TYPE));
        return this;
    }

    @Override
    public RecordArgumentEditor put(String key, Argument argument) {
        mMap.put(key, argument);
        return this;
    }

    @Override
    public synchronized RecordArgumentEditor remove(String key) {
        mMap.remove(key);
        return this;
    }

    @Override
    public synchronized RecordArgumentEditor clear() {
        mMap.clear();
        return this;
    }

    @Override
    public Map<String, Argument> getAll() {
        return new HashMap<>(mMap);
    }
}
