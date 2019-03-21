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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class QMUIContinuousNestedTopRecyclerView extends RecyclerView implements IQMUIContinuousNestedTopView {

    private OnScrollNotifier mScrollNotifier;

    public QMUIContinuousNestedTopRecyclerView(@NonNull Context context) {
        super(context);
    }

    public QMUIContinuousNestedTopRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public QMUIContinuousNestedTopRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public int consumeScroll(int dyUnconsumed) {
        if (dyUnconsumed == Integer.MIN_VALUE) {
            scrollToPosition(0);
            return Integer.MIN_VALUE;
        } else if (dyUnconsumed == Integer.MAX_VALUE) {
            Adapter adapter = getAdapter();
            if (adapter != null) {
                scrollToPosition(adapter.getItemCount() - 1);
            }
            return Integer.MAX_VALUE;
        }
        int oldScroll = getCurrentScroll();
        scrollBy(0, dyUnconsumed);
        return dyUnconsumed - (getCurrentScroll() - oldScroll);
    }

    @Override
    public int getCurrentScroll() {
        return computeVerticalScrollOffset();
    }

    @Override
    public int getScrollRange() {
        return Math.max(0, computeVerticalScrollRange() - getHeight());
    }

    @Override
    public void injectScrollNotifier(OnScrollNotifier notifier) {
        mScrollNotifier = notifier;
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        mScrollNotifier.notify(getCurrentScroll(), getScrollRange());
    }
}
