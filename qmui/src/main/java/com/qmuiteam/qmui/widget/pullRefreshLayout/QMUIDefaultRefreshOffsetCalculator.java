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

package com.qmuiteam.qmui.widget.pullRefreshLayout;

/**
 * {@link QMUIPullRefreshLayout}的默认RefreshView偏移量计算器：
 * 偏移范围限定在[refreshInitOffset, refreshEndOffset]
 *
 * @author cginechen
 * @date 2017-06-07
 */

public class QMUIDefaultRefreshOffsetCalculator implements QMUIPullRefreshLayout.RefreshOffsetCalculator {

    @Override
    public int calculateRefreshOffset(int refreshInitOffset, int refreshEndOffset, int refreshViewHeight, int targetCurrentOffset, int targetInitOffset, int targetRefreshOffset) {
        int refreshOffset;
        if (targetCurrentOffset >= targetRefreshOffset) {
            refreshOffset = refreshEndOffset;
        } else if (targetCurrentOffset <= targetInitOffset) {
            refreshOffset = refreshInitOffset;
        } else {
            float percent = (targetCurrentOffset - targetInitOffset) * 1.0f / (targetRefreshOffset - targetInitOffset);
            refreshOffset = (int) (refreshInitOffset + percent * (refreshEndOffset - refreshInitOffset));
        }
        return refreshOffset;
    }
}
