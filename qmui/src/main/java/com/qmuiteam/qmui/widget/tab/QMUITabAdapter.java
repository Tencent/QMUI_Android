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

package com.qmuiteam.qmui.widget.tab;

import android.view.ViewGroup;

import com.qmuiteam.qmui.widget.QMUIItemViewsAdapter;

public class QMUITabAdapter extends QMUIItemViewsAdapter<QMUITab, QMUITabView> implements QMUITabView.Callback {
    private QMUIBasicTabSegment mTabSegment;

    public QMUITabAdapter(QMUIBasicTabSegment tabSegment, ViewGroup parentView) {
        super(parentView);
        mTabSegment = tabSegment;
    }

    @Override
    protected QMUITabView createView(ViewGroup parentView) {
        return new QMUITabView(parentView.getContext());
    }

    @Override
    protected final void bind(QMUITab item, QMUITabView view, int position) {
        onBindTab(item, view, position);
        view.setCallback(this);
    }

    protected void onBindTab(QMUITab item, QMUITabView view, int position) {
        view.bind(item);
    }

    @Override
    public void onClick(QMUITabView view) {
        int index = getViews().indexOf(view);
        mTabSegment.onClickTab(index);
    }

    @Override
    public void onDoubleClick(QMUITabView view) {
        int index = getViews().indexOf(view);
        mTabSegment.onDoubleClick(index);
    }

    @Override
    public void onLongClick(QMUITabView view) {
    }
}
