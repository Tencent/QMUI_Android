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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class DefaultLatestVisitStorage implements QMUILatestVisitStorage {

    private static final String SP_NAME = "qmui_latest_visit";
    private static final String SP_FRAGMENT_RECORD_ID = "id_qmui_f_r";
    private static final String SP_ACTIVITY_RECORD_ID = "id_qmui_a_r";
    private static final String SP_ACTIVITY_ARG_PREFIX = "a_a_";
    private static final String SP_FRAGMENT_ARG_PREFIX = "a_f_";
    private static final char SP_INT_ARG_TAG = 'i';
    private static final char SP_LONG_ARG_TAG = 'l';
    private static final char SP_FLOAT_ARG_TAG = 'f';
    private static final char SP_BOOLEAN_ARG_TAG = 'b';
    private static final char SP_STRING_ARG_TAG = 's';
    private SharedPreferences sp;

    public DefaultLatestVisitStorage(Context context) {
        sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public int getFragmentRecordId() {
        return sp.getInt(SP_FRAGMENT_RECORD_ID, NOT_EXIST);
    }

    @Nullable
    @Override
    public Map<String, RecordArgumentEditor.Argument> getFragmentArguments() {
        HashMap<String, RecordArgumentEditor.Argument> ret = new HashMap<>();
        for (Map.Entry<String, ?> entity : sp.getAll().entrySet()) {
            String key = entity.getKey();
            Object value = entity.getValue();
            String prefix = SP_FRAGMENT_ARG_PREFIX;
            if (key.startsWith(prefix)) {
                char tag = key.charAt(prefix.length());
                String realKey = key.substring(prefix.length() + 1);
                if (tag == SP_INT_ARG_TAG) {
                    ret.put(realKey, new RecordArgumentEditor.Argument(value, Integer.TYPE));
                } else if (tag == SP_BOOLEAN_ARG_TAG) {
                    ret.put(realKey, new RecordArgumentEditor.Argument(value, Boolean.TYPE));
                } else if (tag == SP_LONG_ARG_TAG) {
                    ret.put(realKey, new RecordArgumentEditor.Argument(value, Long.TYPE));
                } else if (tag == SP_FLOAT_ARG_TAG) {
                    ret.put(realKey, new RecordArgumentEditor.Argument(value, Float.TYPE));
                } else if (tag == SP_STRING_ARG_TAG) {
                    ret.put(realKey, new RecordArgumentEditor.Argument(value, String.class));
                }
            }
        }
        return ret;
    }

    @Override
    public int getActivityRecordId() {
        return sp.getInt(SP_ACTIVITY_RECORD_ID, NOT_EXIST);
    }

    @Override
    public void getAndWriteActivityArgumentsToIntent(@NonNull Intent intent) {
        for (Map.Entry<String, ?> entity : sp.getAll().entrySet()) {
            String key = entity.getKey();
            Object value = entity.getValue();
            String prefix = SP_ACTIVITY_ARG_PREFIX;
            if (key.startsWith(prefix)) {
                char tag = key.charAt(prefix.length());
                String realKey = key.substring(prefix.length() + 1);
                if (tag == SP_INT_ARG_TAG) {
                    intent.putExtra(realKey, (Integer) value);
                } else if (tag == SP_BOOLEAN_ARG_TAG) {
                    intent.putExtra(realKey, (Boolean) value);
                } else if (tag == SP_LONG_ARG_TAG) {
                    intent.putExtra(realKey, (Long) value);
                } else if (tag == SP_FLOAT_ARG_TAG) {
                    intent.putExtra(realKey, (Float) value);
                } else if (tag == SP_STRING_ARG_TAG) {
                    intent.putExtra(realKey, (String) value);
                }
            }
        }
    }

    @Override
    public void clearFragmentStorage() {
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(SP_FRAGMENT_RECORD_ID);
        clearArgument(editor, SP_FRAGMENT_ARG_PREFIX);
        editor.apply();
    }

    @Override
    public void clearActivityStorage() {
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(SP_ACTIVITY_RECORD_ID);
        clearArgument(editor, SP_ACTIVITY_ARG_PREFIX);
        editor.apply();
    }

    @Override
    public void saveFragmentRecordInfo(int id, Map<String, RecordArgumentEditor.Argument> arguments) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(SP_FRAGMENT_RECORD_ID, id);
        putArguments(editor, SP_FRAGMENT_ARG_PREFIX, arguments);
        editor.apply();
    }

    @Override
    public void saveActivityRecordInfo(int id, @Nullable Map<String, RecordArgumentEditor.Argument> arguments) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(SP_ACTIVITY_RECORD_ID, id);
        putArguments(editor, SP_ACTIVITY_ARG_PREFIX, arguments);
        editor.apply();
    }

    @Override
    public void clearAll() {
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }

    private void clearArgument(SharedPreferences.Editor editor, String prefix) {
        for (String key : sp.getAll().keySet()) {
            if (key.startsWith(prefix)) {
                editor.remove(key);
            }
        }
    }

    private void putArguments(SharedPreferences.Editor editor,
                              String prefix, Map<String, RecordArgumentEditor.Argument> arguments) {
        // clear first
        clearArgument(editor, prefix);

        if (arguments != null && arguments.size() > 0) {
            for (String name : arguments.keySet()) {
                RecordArgumentEditor.Argument argument = arguments.get(name);
                if (argument != null) {
                    Class<?> type = argument.getType();
                    Object value = argument.getValue();
                    if (type == Integer.TYPE || type == Integer.class) {
                        editor.putInt(prefix + SP_INT_ARG_TAG + name, (Integer) value);
                    } else if (type == Boolean.TYPE || type == Boolean.class) {
                        editor.putBoolean(prefix + SP_BOOLEAN_ARG_TAG + name, (Boolean) value);
                    } else if (type == Float.TYPE || type == Float.class) {
                        editor.putFloat(prefix + SP_FLOAT_ARG_TAG + name, (Float) value);
                    } else if (type == Long.TYPE || type == Long.class) {
                        editor.putLong(prefix + SP_LONG_ARG_TAG + name, (Long) value);
                    } else if (type == String.class) {
                        editor.putString(prefix + SP_STRING_ARG_TAG + name, (String) value);
                    } else {
                        throw new RuntimeException(String.format(
                                "Not support the type: %s", type.getSimpleName()));
                    }
                }
            }
        }
    }
}
