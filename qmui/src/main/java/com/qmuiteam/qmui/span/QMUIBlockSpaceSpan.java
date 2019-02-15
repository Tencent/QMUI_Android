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

package com.qmuiteam.qmui.span;

import android.graphics.Canvas;
import android.graphics.Paint;
import androidx.annotation.NonNull;
import android.text.style.ReplacementSpan;

import com.qmuiteam.qmui.util.QMUIDeviceHelper;

/**
 * 提供一个整行的空白的Span，可用来用于制作段间距
 *
 * @author cginechen
 * @date 2016-02-17
 */
public class QMUIBlockSpaceSpan extends ReplacementSpan {
    private int mHeight;

    public QMUIBlockSpaceSpan(int height) {
        mHeight = height;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        if (fm != null && !QMUIDeviceHelper.isMeizu()) {
            //return后宽度为0，因此实际空隙和段落开始在同一行，需要加上一行的高度
            fm.ascent = fm.top = -mHeight - paint.getFontMetricsInt(fm);
            fm.descent = fm.bottom = 0;
        }
        return 0;
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {

    }
}
