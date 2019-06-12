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
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.webview.QMUIWebView;

import androidx.annotation.NonNull;

public class QMUIContinuousNestedTopWebView extends QMUIWebView implements IQMUIContinuousNestedTopView {

    public static final String KEY_SCROLL_INFO = "@qmui_scroll_info_top_webview";

    private OnScrollNotifier mScrollNotifier;

    public QMUIContinuousNestedTopWebView(Context context) {
        super(context);
        init();
    }

    public QMUIContinuousNestedTopWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public QMUIContinuousNestedTopWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        setVerticalScrollBarEnabled(false);
    }

    @Override
    public int consumeScroll(int yUnconsumed) {
        // compute the consumed value
        int scrollY = getScrollY();
        int maxScrollY = getScrollOffsetRange();
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
        int scrollRange = getScrollOffsetRange();
        return Math.max(0, Math.min(scrollY, scrollRange));
    }

    @Override
    public int getScrollOffsetRange() {
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
            mScrollNotifier.notify(getCurrentScroll(), getScrollOffsetRange());
        }
    }

    @Override
    public void saveScrollInfo(@NonNull Bundle bundle) {
        bundle.putInt(KEY_SCROLL_INFO, getScrollY());
    }

    @Override
    public void restoreScrollInfo(@NonNull Bundle bundle) {
        int scrollY = QMUIDisplayHelper.px2dp(getContext(),
                bundle.getInt(KEY_SCROLL_INFO, 0));
        exec("javascript:scrollTo(0, " + scrollY + ")");
    }

    private void exec(final String jsCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(jsCode, null);
        } else {
            loadUrl(jsCode);
        }
    }
}
