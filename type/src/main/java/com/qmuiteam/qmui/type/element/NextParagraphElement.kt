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

package com.qmuiteam.qmui.type.element;

import android.graphics.Canvas;

import androidx.annotation.Nullable;

import com.qmuiteam.qmui.type.TypeEnvironment;

public class NextParagraphElement extends Element {

    public NextParagraphElement(int index, int originIndex) {
        this('\n', null, index, originIndex, "\n");
    }

    public NextParagraphElement(char singleChar, @Nullable String text, int index, int originIndex) {
        super(singleChar, null, index, originIndex);
    }

    public NextParagraphElement(char singleChar, @Nullable CharSequence text, int index, int originIndex, @Nullable String description) {
        super(singleChar, text, index, originIndex, description);
    }



    @Override
    protected void onMeasure(TypeEnvironment env) {
        setMeasureDimen(0, 0, 0);
    }

    @Override
    protected void onDraw(TypeEnvironment env, Canvas canvas) {

    }
}
