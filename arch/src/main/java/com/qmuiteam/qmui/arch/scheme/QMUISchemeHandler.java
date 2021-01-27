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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qmuiteam.qmui.arch.QMUISwipeBackActivityManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QMUISchemeHandler {
    static final String TAG = "QMUISchemeHandler";
    public final static String ARG_FROM_SCHEME = "__qmui_arg_from_scheme";
    public final static String ARG_ORIGIN_SCHEME = "__qmui_arg_origin_scheme";
    public final static String ARG_FORCE_TO_NEW_ACTIVITY = "__qmui_force_to_new_activity";
    public final static String ARG_FINISH_CURRENT = "__qmui_finish_current";

    private static SchemeMap sSchemeMap;

    static {
        try {
            Class<?> cls = Class.forName(SchemeMap.class.getCanonicalName() + "Impl");
            sSchemeMap = (SchemeMap) cls.newInstance();
        } catch (ClassNotFoundException e) {
            sSchemeMap = new SchemeMap() {
                @Override
                public SchemeItem findScheme(QMUISchemeHandler handler, String schemeAction, Map<String, String> params) {
                    return null;
                }

                @Override
                public boolean exists(QMUISchemeHandler handler, String schemeAction) {
                    return false;
                }
            };
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can not access the Class SchemeMapImpl. " +
                    "Please file a issue to report this.");
        } catch (InstantiationException e) {
            throw new RuntimeException("Can not instance the Class SchemeMapImpl. " +
                    "Please file a issue to report this.");
        }
    }

    private final String mPrefix;
    private final List<QMUISchemeHandleInterpolator> mInterpolatorList;
    private final long mBlockSameSchemeTimeout;
    private final Class<? extends QMUISchemeIntentFactory> mDefaultIntentFactory;
    private final Class<? extends QMUISchemeFragmentFactory> mDefaultFragmentFactory;
    private final Class<? extends QMUISchemeMatcher> mDefaultSchemeMatcher;
    private final QMUISchemeHandleInterpolator mFallbackInterceptor;

    private String mLastHandledScheme = null;
    private long mLastSchemeHandledTime = 0;

    private QMUISchemeHandler(Builder builder) {
        mPrefix = builder.mPrefix;
        List<QMUISchemeHandleInterpolator> interpolatorList = builder.mInterpolatorList;
        if (interpolatorList != null && !interpolatorList.isEmpty()) {
            mInterpolatorList = new ArrayList<>(interpolatorList);
        } else {
            mInterpolatorList = null;
        }
        mBlockSameSchemeTimeout = builder.mBlockSameSchemeTimeout;
        mDefaultIntentFactory = builder.mDefaultIntentFactory;
        mDefaultFragmentFactory = builder.mDefaultFragmentFactory;
        mDefaultSchemeMatcher = builder.mDefaultSchemeMatcher;
        mFallbackInterceptor = builder.mFallbackInterceptor;
    }

    public String getPrefix() {
        return mPrefix;
    }

    public Class<? extends QMUISchemeFragmentFactory> getDefaultFragmentFactory() {
        return mDefaultFragmentFactory;
    }

    public Class<? extends QMUISchemeIntentFactory> getDefaultIntentFactory() {
        return mDefaultIntentFactory;
    }

    public Class<? extends QMUISchemeMatcher> getDefaultSchemeMatcher() {
        return mDefaultSchemeMatcher;
    }

    @Nullable
    public SchemeItem getSchemeItem(String action, Map<String, String> params) {
        return sSchemeMap.findScheme(this, action, params);
    }

    public boolean handle(String scheme) {
        if (scheme == null || !scheme.startsWith(mPrefix)) {
            return false;
        }

        if (scheme.equals(mLastHandledScheme) && System.currentTimeMillis() - mLastSchemeHandledTime < mBlockSameSchemeTimeout) {
            return true;
        }

        Activity currentActivity = QMUISwipeBackActivityManager.getInstance().getCurrentActivity();
        if (currentActivity == null) {
            return false;
        }

        scheme = scheme.substring(mPrefix.length());
        String[] elements = scheme.split("\\?");
        if (elements.length == 0 || elements[0] == null || elements[0].isEmpty()) {
            return false;
        }
        String action = elements[0];

        Map<String, String> params = new HashMap<>();
        if (elements.length > 1) {
            parseParams(elements[1], params);
        }

        boolean handled = false;
        if (mInterpolatorList != null && !mInterpolatorList.isEmpty()) {
            for (QMUISchemeHandleInterpolator interpolator : mInterpolatorList) {
                if (interpolator.intercept(this, currentActivity, action, params, scheme)) {
                    handled = true;
                    break;
                }
            }
        }

        if (!handled) {
            SchemeItem schemeItem = sSchemeMap.findScheme(this, action, params);
            if (schemeItem != null) {
                handled = schemeItem.handle(this, currentActivity, schemeItem.convertFrom(params), scheme);
            }
        }

        if(!handled && mFallbackInterceptor != null){
            handled = mFallbackInterceptor.intercept(this, currentActivity, action, params, scheme);
        }

        if (handled) {
            mLastHandledScheme = scheme;
            mLastSchemeHandledTime = System.currentTimeMillis();
        }

        return handled;
    }


    public static void parseParams(@Nullable String schemeParams, @NonNull Map<String, String> queryMap) {
        if (schemeParams == null || schemeParams.isEmpty()) {
            return;
        }

        int start = 0;
        do {
            int next = schemeParams.indexOf('&', start);
            int end = (next == -1) ? schemeParams.length() : next;
            if (start == end) {
                start += 1;
                continue;
            }

            int separator = schemeParams.indexOf('=', start);
            if (separator > end || separator == -1) {
                separator = end;
            }
            if (separator == start) {
                start = end + 1;
                continue;
            }

            String name = schemeParams.substring(start, separator);
            String value = separator == end ? "" : schemeParams.substring(separator + 1, end);
            queryMap.put(name, value);
            start = end + 1;
        } while (start < schemeParams.length());
    }


    public static class Builder {
        public static final long BLOCK_SAME_SCHEME_DEFAULT_TIMEOUT = 500;
        private String mPrefix;
        private List<QMUISchemeHandleInterpolator> mInterpolatorList;
        private long mBlockSameSchemeTimeout = BLOCK_SAME_SCHEME_DEFAULT_TIMEOUT;
        private Class<? extends QMUISchemeIntentFactory> mDefaultIntentFactory = QMUIDefaultSchemeIntentFactory.class;
        private Class<? extends QMUISchemeFragmentFactory> mDefaultFragmentFactory = QMUIDefaultSchemeFragmentFactory.class;
        private Class<? extends QMUISchemeMatcher> mDefaultSchemeMatcher = QMUIDefaultSchemeMatcher.class;
        private QMUISchemeHandleInterpolator mFallbackInterceptor = null;

        public Builder(@NonNull String prefix) {
            mPrefix = prefix;
        }

        public Builder addInterpolator(QMUISchemeHandleInterpolator interpolator) {
            if (mInterpolatorList == null) {
                mInterpolatorList = new ArrayList<>();
            }
            mInterpolatorList.add(interpolator);
            return this;
        }

        public Builder blockSameSchemeTimeout(long blockSameSchemeTimeout) {
            mBlockSameSchemeTimeout = blockSameSchemeTimeout;
            return this;
        }

        public Builder setFallbackInterceptor(QMUISchemeHandleInterpolator fallbackInterceptor) {
            mFallbackInterceptor = fallbackInterceptor;
            return this;
        }

        public Builder defaultFragmentFactory(Class<? extends QMUISchemeFragmentFactory> defaultFragmentFactory) {
            mDefaultFragmentFactory = defaultFragmentFactory;
            return this;
        }

        public Builder defaultIntentFactory(Class<? extends QMUISchemeIntentFactory> defaultIntentFactory) {
            mDefaultIntentFactory = defaultIntentFactory;
            return this;
        }

        public Builder defaultSchemeMatcher(Class<? extends QMUISchemeMatcher> defaultSchemeMatcher){
            mDefaultSchemeMatcher = defaultSchemeMatcher;
            return this;
        }

        public QMUISchemeHandler build() {
            return new QMUISchemeHandler(this);
        }
    }
}
