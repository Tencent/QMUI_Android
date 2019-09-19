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
package com.qmuiteam.qmui.skin;

import android.util.Pair;

import java.util.HashMap;

import androidx.annotation.Nullable;

public class QMUISkinValueBuilder {
    public static final String BACKGROUND = "background";
    public static final String TEXT_COLOR = "textColor";
    public static final String SRC = "src";
    public static final String BORDER = "border";
    public static final String SEPARATOR = "separator";

    private HashMap<String, Pair<Integer, String>> mValues = new HashMap<>();


    public QMUISkinValueBuilder background(int attr) {
        return background(attr, null);
    }

    public QMUISkinValueBuilder background(int attr, @Nullable String extra) {
        mValues.put(BACKGROUND, Pair.create(attr, extra));
        return this;
    }

    public QMUISkinValueBuilder textColor(int attr) {
        return textColor(attr, null);
    }

    public QMUISkinValueBuilder textColor(int attr, @Nullable String extra) {
        mValues.put(TEXT_COLOR, Pair.create(attr, extra));
        return this;
    }

    public QMUISkinValueBuilder src(int attr) {
        return src(attr, null);
    }

    public QMUISkinValueBuilder src(int attr, @Nullable String extra) {
        mValues.put(SRC, Pair.create(attr, extra));
        return this;
    }

    public QMUISkinValueBuilder border(int attr) {
        return border(attr, null);
    }

    public QMUISkinValueBuilder border(int attr, @Nullable String extra) {
        mValues.put(BORDER, Pair.create(attr, extra));
        return this;
    }

    public QMUISkinValueBuilder topSeparator(int attr) {
        return separator(attr, "top");
    }

    public QMUISkinValueBuilder rightSeparator(int attr) {
        return separator(attr, "right");
    }

    public QMUISkinValueBuilder bottomSeparator(int attr) {
        return separator(attr, "bottom");
    }

    public QMUISkinValueBuilder leftSeparator(int attr) {
        return separator(attr, "left");
    }

    public QMUISkinValueBuilder separator(int attr, @Nullable String extra) {
        mValues.put(SEPARATOR, Pair.create(attr, extra));
        return this;
    }

    public QMUISkinValueBuilder custom(String name, int attr, @Nullable String extra) {
        mValues.put(name, Pair.create(attr, extra));
        return this;
    }

    public QMUISkinValueBuilder clear(){
        mValues.clear();
        return this;
    }

    public boolean isEmpty(){
        return mValues.isEmpty();
    }

    public String build() {
        StringBuilder builder = new StringBuilder();
        boolean isFirstItem = true;
        for (String name : mValues.keySet()) {
            Pair<Integer, String> itemValue = mValues.get(name);
            if (itemValue == null) {
                continue;
            }
            if (!isFirstItem) {
                builder.append("|");
            }
            builder.append(name);
            builder.append(":");
            builder.append(itemValue.first);
            if (itemValue.second != null) {
                builder.append(":");
                builder.append(itemValue.second);
            }
            isFirstItem = false;
        }
        return builder.toString();
    }
}
