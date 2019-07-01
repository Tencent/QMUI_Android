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

package com.qmuiteam.qmui.arch.first;

import android.util.Log;

import com.qmuiteam.qmui.arch.QMUIFragment;
import com.qmuiteam.qmui.arch.QMUIFragmentActivity;

import java.util.HashMap;

import androidx.annotation.MainThread;

public class FirstFragmentFinders {
    private static FirstFragmentFinders instance;
    private static boolean debug = false;
    private static final String TAG = "FirstFragmentFinders";
    private static final FirstFragmentFinder EMPTY_FINDER = new FirstFragmentFinder() {
        @Override
        public Class<? extends QMUIFragment> getFragmentClassById(int id) {
            return null;
        }

        @Override
        public int getIdByFragmentClass(Class<? extends QMUIFragment> clazz) {
            return FirstFragmentFinder.NO_ID;
        }
    };

    public static void setDebug(boolean debug) {
        FirstFragmentFinders.debug = debug;
    }

    @MainThread
    public static FirstFragmentFinders getInstance() {
        if (instance == null) {
            instance = new FirstFragmentFinders();
        }
        return instance;
    }

    private HashMap<Class<?>, FirstFragmentFinder> mCache = new HashMap<>();

    private FirstFragmentFinders() {

    }

    public FirstFragmentFinder get(Class<? extends QMUIFragmentActivity> cls) {
        FirstFragmentFinder finder = mCache.get(cls);
        if (finder != null) {
            return finder;
        }

        ClassLoader classLoader = cls.getClassLoader();
        if (classLoader == null) {
            return null;
        }
        String clsName = cls.getName();

        try {
            Class<?> finderClass = classLoader.loadClass(clsName + "_FragmentFinder");
            if (FirstFragmentFinder.class.isAssignableFrom(finderClass)) {
                finder = (FirstFragmentFinder) finderClass.newInstance();
            }
        } catch (ClassNotFoundException e) {
            Class<?> superClass = cls.getSuperclass();
            if (superClass != null && QMUIFragmentActivity.class.isAssignableFrom(superClass)) {
                if(debug){
                    Log.d(TAG, "Not found. Trying superclass" + superClass.getName());
                }
                finder = get((Class<? extends QMUIFragmentActivity>) superClass);
            }
        } catch (IllegalAccessException e) {
            if (debug) {
                Log.d(TAG, "Access exception.");
                e.printStackTrace();
            }
        } catch (InstantiationException e) {
            if (debug) {
                Log.d(TAG, "Instantiation exception.");
                e.printStackTrace();
            }
        }

        if (finder == null) {
            finder = EMPTY_FINDER;
        }
        mCache.put(cls, finder);
        return finder;
    }
}
