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
import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qmuiteam.qmui.QMUILog;
import com.qmuiteam.qmui.util.QMUILangHelper;

import java.util.HashMap;
import java.util.Map;

public abstract class SchemeItem {
    private static HashMap<Class<? extends QMUISchemeMatcher>, QMUISchemeMatcher> sSchemeMatchers;
    private static HashMap<Class<? extends QMUISchemeValueConverter>, QMUISchemeValueConverter> sSchemeValueConverters;

    @Nullable
    private ArrayMap<String, String> mRequired;
    @Nullable
    private String[] mKeysForInt;
    @Nullable
    private String[] mKeysForBool;
    @Nullable
    private String[] mKeysForLong;
    @Nullable
    private String[] mKeysForFloat;
    @Nullable
    private String[] mKeysForDouble;
    @Nullable
    private Class<? extends QMUISchemeMatcher> mSchemeMatcherCls;

    @Nullable
    private Class<? extends QMUISchemeValueConverter> mSchemeValueConverterCls;

    private boolean mUseRefreshIfMatchedCurrent;

    public SchemeItem(@Nullable ArrayMap<String, String> required,
                      boolean useRefreshIfMatchedCurrent,
                      @Nullable String[] keysForInt,
                      @Nullable String[] keysForBool,
                      @Nullable String[] keysForLong,
                      @Nullable String[] keysForFloat,
                      @Nullable String[] keysForDouble,
                      @Nullable Class<? extends QMUISchemeMatcher> schemeMatcherCls,
                      @Nullable Class<? extends QMUISchemeValueConverter> schemeValueConverterCls) {
        mRequired = required;
        mUseRefreshIfMatchedCurrent = useRefreshIfMatchedCurrent;
        mKeysForInt = keysForInt;
        mKeysForBool = keysForBool;
        mKeysForLong = keysForLong;
        mKeysForFloat = keysForFloat;
        mKeysForDouble = keysForDouble;
        mSchemeMatcherCls = schemeMatcherCls;
        mSchemeValueConverterCls = schemeValueConverterCls;
    }

    public boolean isUseRefreshIfMatchedCurrent() {
        return mUseRefreshIfMatchedCurrent;
    }

    @Nullable
    public Map<String, SchemeValue> convertFrom(@Nullable Map<String, String> schemeParams) {
        if (schemeParams == null || schemeParams.isEmpty()) {
            return null;
        }

        Map<String, SchemeValue> queryMap = new HashMap<>();
        if (sSchemeValueConverters == null) {
            sSchemeValueConverters = new HashMap<>();
        }


        for (Map.Entry<String, String> param : schemeParams.entrySet()) {
            String name = param.getKey();
            String value = param.getValue();
            if (name == null || name.isEmpty()) {
                continue;
            }

            if (mSchemeValueConverterCls != null) {
                QMUISchemeValueConverter converter = sSchemeValueConverters.get(mSchemeValueConverterCls);
                if(converter == null){
                    try {
                        converter = mSchemeValueConverterCls.newInstance();
                        sSchemeValueConverters.put(mSchemeValueConverterCls, converter);
                    } catch (Exception e) {
                        QMUILog.printErrStackTrace(QMUISchemeHandler.TAG, e,
                                "error to instance QMUISchemeValueConverter: %d", mSchemeValueConverterCls.getSimpleName());
                    }
                }
                if(converter != null){
                    value = converter.convert(name, value, schemeParams);
                }
            }



            try {
                if (contains(mKeysForInt, name)) {
                    queryMap.put(name, new SchemeValue(value, Integer.valueOf(value), Integer.TYPE));
                } else if (isBoolKey(name)) {
                    queryMap.put(name, new SchemeValue(value, convertStringToBool(value), Boolean.TYPE));
                } else if (contains(mKeysForLong, name)) {
                    queryMap.put(name, new SchemeValue(value, Long.valueOf(value), Long.TYPE));
                } else if (contains(mKeysForFloat, name)) {
                    queryMap.put(name, new SchemeValue(value, Float.valueOf(value), Float.TYPE));
                } else if (contains(mKeysForDouble, name)) {
                    queryMap.put(name, new SchemeValue(value, Double.valueOf(value), Double.TYPE));
                } else {
                    queryMap.put(name, new SchemeValue(value, value, String.class));
                }
            } catch (Exception e) {
                QMUILog.printErrStackTrace(QMUISchemeHandler.TAG, e,
                        "error to parse scheme param: %s = %s", name, value);
            }
        }
        return queryMap;
    }

    protected boolean isBoolKey(String name){
        return QMUISchemeHandler.ARG_FORCE_TO_NEW_ACTIVITY.equals(name) ||
                QMUISchemeHandler.ARG_FINISH_CURRENT.equals(name) ||
                contains(mKeysForBool, name);
    }

    protected boolean convertStringToBool(String text){
        return !(QMUILangHelper.isNullOrEmpty(text) || "0".equals(text) || "false".equals(text.toLowerCase()));
    }

    protected boolean shouldFinishCurrent(@Nullable Map<String, SchemeValue> scheme){
        if(scheme == null || scheme.isEmpty()){
            return false;
        }
        SchemeValue schemeValue = scheme.get(QMUISchemeHandler.ARG_FINISH_CURRENT);
        return schemeValue != null && schemeValue.type == Boolean.TYPE && (boolean)schemeValue.value;
    }

    @Nullable
    private QMUISchemeMatcher getSchemeMatcher(@NonNull QMUISchemeHandler handler) {
        if (sSchemeMatchers == null) {
            sSchemeMatchers = new HashMap<>();
        }
        Class<? extends QMUISchemeMatcher> schemeMatcherCls = mSchemeMatcherCls;
        if (schemeMatcherCls == null) {
            schemeMatcherCls = handler.getDefaultSchemeMatcher();
        }

        QMUISchemeMatcher matcher = sSchemeMatchers.get(schemeMatcherCls);
        if (matcher == null) {
            try {
                matcher = schemeMatcherCls.newInstance();
                sSchemeMatchers.put(schemeMatcherCls, matcher);
            } catch (Exception e) {
                QMUILog.printErrStackTrace(QMUISchemeHandler.TAG, e,
                        "error to instance QMUISchemeMatcher: %d", schemeMatcherCls.getSimpleName());
            }
        }
        return matcher;
    }

    private static boolean contains(@Nullable String[] array, @NonNull String key) {
        if (array == null || array.length == 0) {
            return false;
        }
        for (String s : array) {
            if (key.equals(s)) {
                return true;
            }
        }
        return false;
    }

    // used by generated code(SchemeMapImpl)
    boolean match(@NonNull QMUISchemeHandler handler, @Nullable Map<String, String> params) {
        QMUISchemeMatcher matcher = getSchemeMatcher(handler);
        if (matcher != null) {
            return matcher.match(this, params);
        }
        return matchRequiredParam(params);
    }

    public boolean matchRequiredParam(@Nullable Map<String, String> params) {
        if (mRequired == null || mRequired.isEmpty()) {
            return true;
        }
        if (params == null || params.isEmpty()) {
            return false;
        }
        for (int i = 0; i < mRequired.size(); i++) {
            String key = mRequired.keyAt(i);
            if (!params.containsKey(key)) {
                return false;
            }
            String value = mRequired.valueAt(i);
            if (value == null) {
                // if no value. that means scheme must provide this key.
                continue;
            }
            String actual = params.get(key);
            if (actual == null || !actual.equals(value)) {
                return false;
            }
        }
        return true;
    }

    public abstract boolean handle(@NonNull QMUISchemeHandler handler,
                                   @NonNull Activity activity,
                                   @Nullable Map<String, SchemeValue> scheme,
                                   @NonNull String origin
    );
}
