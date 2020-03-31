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

import java.util.HashMap;
import java.util.LinkedList;

import androidx.annotation.NonNull;

public class QMUISkinValueBuilder {
    public static final String BACKGROUND = "background";
    public static final String TEXT_COLOR = "textColor";
    public static final String HINT_COLOR = "hintColor";
    public static final String SECOND_TEXT_COLOR = "secondTextColor";
    public static final String SRC = "src";
    public static final String BORDER = "border";
    public static final String TOP_SEPARATOR = "topSeparator";
    public static final String BOTTOM_SEPARATOR = "bottomSeparator";
    public static final String RIGHT_SEPARATOR = "rightSeparator";
    public static final String LEFT_SEPARATOR = "LeftSeparator";
    public static final String ALPHA = "alpha";
    public static final String TINT_COLOR = "tintColor";
    public static final String BG_TINT_COLOR = "bgTintColor";
    public static final String PROGRESS_COLOR = "progressColor";
    public static final String TEXT_COMPOUND_TINT_COLOR = "tcTintColor";
    public static final String TEXT_COMPOUND_LEFT_SRC = "tclSrc";
    public static final String TEXT_COMPOUND_RIGHT_SRC = "tcrSrc";
    public static final String TEXT_COMPOUND_TOP_SRC = "tctSrc";
    public static final String TEXT_COMPOUND_BOTTOM_SRC = "tcbSrc";
    public static final String UNDERLINE = "underline";
    public static final String MORE_TEXT_COLOR = "moreTextColor";
    public static final String MORE_BG_COLOR = "moreBgColor";
    private static LinkedList<QMUISkinValueBuilder> sValueBuilderPool;

    public static QMUISkinValueBuilder acquire() {
        if (sValueBuilderPool == null) {
            return new QMUISkinValueBuilder();
        }
        QMUISkinValueBuilder valueBuilder = sValueBuilderPool.poll();
        if (valueBuilder != null) {
            return valueBuilder;
        }
        return new QMUISkinValueBuilder();
    }

    public static void release(@NonNull QMUISkinValueBuilder valueBuilder) {
        valueBuilder.clear();
        if (sValueBuilderPool == null) {
            sValueBuilderPool = new LinkedList<>();
        }
        if (sValueBuilderPool.size() < 2) {
            sValueBuilderPool.push(valueBuilder);
        }
    }

    private QMUISkinValueBuilder() {

    }

    private HashMap<String, String> mValues = new HashMap<>();

    public QMUISkinValueBuilder background(int attr) {
        mValues.put(BACKGROUND, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder background(String attrName) {
        mValues.put(BACKGROUND, attrName);
        return this;
    }

    public QMUISkinValueBuilder underline(int attr) {
        mValues.put(UNDERLINE, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder underline(String attrName) {
        mValues.put(UNDERLINE, attrName);
        return this;
    }

    public QMUISkinValueBuilder moreTextColor(int attr) {
        mValues.put(MORE_TEXT_COLOR, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder moreTextColor(String attrName) {
        mValues.put(MORE_TEXT_COLOR, attrName);
        return this;
    }

    public QMUISkinValueBuilder moreBgColor(int attr) {
        mValues.put(MORE_BG_COLOR, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder moreBgColor(String attrName) {
        mValues.put(MORE_BG_COLOR, attrName);
        return this;
    }

    public QMUISkinValueBuilder textCompoundTintColor(int attr) {
        mValues.put(TEXT_COMPOUND_TINT_COLOR, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder textCompoundTintColor(String attrName) {
        mValues.put(TEXT_COMPOUND_TINT_COLOR, attrName);
        return this;
    }

    public QMUISkinValueBuilder textCompoundTopSrc(int attr) {
        mValues.put(TEXT_COMPOUND_TOP_SRC, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder textCompoundTopSrc(String attrName) {
        mValues.put(TEXT_COMPOUND_TOP_SRC, attrName);
        return this;
    }

    public QMUISkinValueBuilder textCompoundRightSrc(int attr) {
        mValues.put(TEXT_COMPOUND_RIGHT_SRC, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder textCompoundRightSrc(String attrName) {
        mValues.put(TEXT_COMPOUND_RIGHT_SRC, attrName);
        return this;
    }

    public QMUISkinValueBuilder textCompoundBottomSrc(int attr) {
        mValues.put(TEXT_COMPOUND_BOTTOM_SRC, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder textCompoundBottomSrc(String attrName) {
        mValues.put(TEXT_COMPOUND_BOTTOM_SRC, attrName);
        return this;
    }

    public QMUISkinValueBuilder textCompoundLeftSrc(int attr) {
        mValues.put(TEXT_COMPOUND_LEFT_SRC, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder textCompoundLeftSrc(String attrName) {
        mValues.put(TEXT_COMPOUND_LEFT_SRC, attrName);
        return this;
    }

    public QMUISkinValueBuilder textColor(int attr) {
        mValues.put(TEXT_COLOR, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder textColor(String attrName) {
        mValues.put(TEXT_COLOR, attrName);
        return this;
    }

    public QMUISkinValueBuilder hintColor(int attr) {
        mValues.put(HINT_COLOR, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder hintColor(String attrName) {
        mValues.put(HINT_COLOR, attrName);
        return this;
    }

    public QMUISkinValueBuilder progressColor(int attr) {
        mValues.put(PROGRESS_COLOR, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder progressColor(String attrName) {
        mValues.put(PROGRESS_COLOR, attrName);
        return this;
    }

    public QMUISkinValueBuilder src(int attr) {
        mValues.put(SRC, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder src(String attrName) {
        mValues.put(SRC, attrName);
        return this;
    }

    public QMUISkinValueBuilder border(int attr) {
        mValues.put(BORDER, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder border(String attrName) {
        mValues.put(BORDER, attrName);
        return this;
    }

    public QMUISkinValueBuilder topSeparator(int attr) {
        mValues.put(TOP_SEPARATOR, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder topSeparator(String attrName) {
        mValues.put(TOP_SEPARATOR, attrName);
        return this;
    }

    public QMUISkinValueBuilder rightSeparator(int attr) {
        mValues.put(RIGHT_SEPARATOR, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder rightSeparator(String attrName) {
        mValues.put(RIGHT_SEPARATOR, attrName);
        return this;
    }

    public QMUISkinValueBuilder bottomSeparator(int attr) {
        mValues.put(BOTTOM_SEPARATOR, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder bottomSeparator(String attrName) {
        mValues.put(BOTTOM_SEPARATOR, attrName);
        return this;
    }

    public QMUISkinValueBuilder leftSeparator(int attr) {
        mValues.put(LEFT_SEPARATOR, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder leftSeparator(String attrName) {
        mValues.put(LEFT_SEPARATOR, attrName);
        return this;
    }

    public QMUISkinValueBuilder alpha(int attr) {
        mValues.put(ALPHA, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder alpha(String attrName) {
        mValues.put(ALPHA, attrName);
        return this;
    }

    public QMUISkinValueBuilder tintColor(int attr) {
        mValues.put(TINT_COLOR, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder tintColor(String attrName) {
        mValues.put(TINT_COLOR, attrName);
        return this;
    }

    public QMUISkinValueBuilder bgTintColor(int attr) {
        mValues.put(BG_TINT_COLOR, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder bgTintColor(String attrName) {
        mValues.put(BG_TINT_COLOR, attrName);
        return this;
    }

    public QMUISkinValueBuilder secondTextColor(int attr) {
        mValues.put(SECOND_TEXT_COLOR, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder secondTextColor(String attrName) {
        mValues.put(SECOND_TEXT_COLOR, attrName);
        return this;
    }

    public QMUISkinValueBuilder custom(String name, int attr) {
        mValues.put(name, String.valueOf(attr));
        return this;
    }

    public QMUISkinValueBuilder custom(String name, String attrName) {
        mValues.put(name, attrName);
        return this;
    }

    public QMUISkinValueBuilder clear() {
        mValues.clear();
        return this;
    }

    public QMUISkinValueBuilder convertFrom(String value) {
        String[] items = value.split("[|]");
        for (String item : items) {
            String[] kv = item.split(":");
            if (kv.length != 2) {
                continue;
            }
            mValues.put(kv[0].trim(), kv[1].trim());
        }
        return this;
    }

    public boolean isEmpty() {
        return mValues.isEmpty();
    }

    public String build() {
        StringBuilder builder = new StringBuilder();
        boolean isFirstItem = true;
        for (String name : mValues.keySet()) {
            String itemValue = mValues.get(name);
            if (itemValue == null || itemValue.isEmpty()) {
                continue;
            }
            if (!isFirstItem) {
                builder.append("|");
            }
            builder.append(name);
            builder.append(":");
            builder.append(itemValue);
            isFirstItem = false;
        }
        return builder.toString();
    }

    public void release() {
        QMUISkinValueBuilder.release(this);
    }
}
