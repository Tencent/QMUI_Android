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

import android.os.Bundle;

import java.util.Map;

import androidx.annotation.Nullable;

public interface RecordArgumentEditor {

    RecordArgumentEditor putString(String key, @Nullable String value);

    RecordArgumentEditor putInt(String key, int value);

    RecordArgumentEditor putLong(String key, long value);

    RecordArgumentEditor putFloat(String key, float value);

    RecordArgumentEditor putBoolean(String key, boolean value);

    RecordArgumentEditor put(String key, RecordArgumentEditor.Argument argument);

    RecordArgumentEditor remove(String key);

    RecordArgumentEditor clear();

    Map<String, Argument> getAll();

    class Argument {
        private Object value;
        private Class<?> type;

        public Argument(Object value, Class<?> type) {
            this.value = value;
            this.type = type;
        }

        public Object getValue() {
            return value;
        }

        public Class<?> getType() {
            return type;
        }

        public void putToBundle(Bundle bundle, String key){
            if(type == Integer.TYPE){
                bundle.putInt(key, (Integer)value);
            }else if(type == Boolean.TYPE){
                bundle.putBoolean(key, (Boolean) value);
            }else if(type == Long.TYPE){
                bundle.putLong(key, (Long) value);
            }else if(type == Float.TYPE){
                bundle.putFloat(key, (Float) value);
            }else if(type == String.class){
                bundle.putString(key, (String) value);
            }
        }
    }
}
