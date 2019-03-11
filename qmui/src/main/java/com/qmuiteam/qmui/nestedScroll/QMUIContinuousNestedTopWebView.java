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

package com.qmuiteam.qmui.nestedScroll;

import android.content.Context;
import android.util.AttributeSet;

import com.qmuiteam.qmui.widget.webview.QMUIWebView;

public class QMUIContinuousNestedTopWebView extends QMUIWebView implements IQMUIContinuousNestedTopView {

    private OnScrollNotifier mScrollNotifier;

    public QMUIContinuousNestedTopWebView(Context context) {
        super(context);
    }

    public QMUIContinuousNestedTopWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QMUIContinuousNestedTopWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int consumeScroll(int yUnconsumed) {
        // compute the consumed value
        int scrollY = getScrollY();
        int maxScrollY = getScrollRange();
        // the scrollY may be negative or larger than scrolling range
        scrollY = Math.max(0, Math.min(scrollY, maxScrollY));
        int dy = 0;
        if (yUnconsumed < 0) {
            dy = Math.max(yUnconsumed, -scrollY);
        } else if (yUnconsumed > 0) {
            dy = Math.min(yUnconsumed, maxScrollY - scrollY);
        }
        scrollBy(0, dy);
        return yUnconsumed - dy;
    }

    @Override
    public int getCurrentScroll() {
        int scrollY = getScrollY();
        int scrollRange = getScrollRange();
        return Math.max(0, Math.min(scrollY, scrollRange));
    }

    @Override
    public int getScrollRange() {
        return computeVerticalScrollRange() - getHeight();
    }

    @Override
    public void injectScrollNotifier(OnScrollNotifier notifier) {
        mScrollNotifier = notifier;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mScrollNotifier != null) {
            mScrollNotifier.notify(getCurrentScroll(), getScrollRange());
        }
    }
}
