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

package com.qmuiteam.qmui.widget.dialog;

import android.graphics.drawable.Drawable;

public class QMUIBottomSheetItemModel {
    Drawable image = null;
    int imageRes = 0;
    int imageSkinTintColorAttr = 0;
    int imageSkinSrcAttr = 0;
    int textSkinColorAttr = 0;
    String text;
    String tag = "";
    boolean hasRedPoint = false;
    boolean isDisabled = false;

    public QMUIBottomSheetItemModel(String text, String tag) {
        this.text = text;
        this.tag = tag;
    }

    public QMUIBottomSheetItemModel image(Drawable image) {
        this.image = image;
        return this;
    }

    public QMUIBottomSheetItemModel image(int imageRes) {
        this.imageRes = imageRes;
        return this;
    }

    public QMUIBottomSheetItemModel skinTextColorAttr(int attr) {
        this.textSkinColorAttr = attr;
        return this;
    }

    public QMUIBottomSheetItemModel skinImageTintColorAttr(int attr) {
        this.imageSkinTintColorAttr = attr;
        return this;
    }

    public QMUIBottomSheetItemModel skinImageSrcAttr(int attr) {
        this.imageSkinSrcAttr = attr;
        return this;
    }

    public QMUIBottomSheetItemModel redPoint(boolean hasRedPoint) {
        this.hasRedPoint = hasRedPoint;
        return this;
    }

    public QMUIBottomSheetItemModel disabled(boolean isDisabled) {
        this.isDisabled = isDisabled;
        return this;
    }
}
