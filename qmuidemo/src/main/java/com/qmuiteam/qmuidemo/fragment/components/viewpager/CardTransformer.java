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

package com.qmuiteam.qmuidemo.fragment.components.viewpager;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * @author cginechen
 * @date 2017-09-13
 */

public class CardTransformer implements ViewPager.PageTransformer {
    @Override
    public void transformPage(View page, float position) {
        // 刷新数据notifyDataSetChange之后也会调用到transformPage，但此时的position可能不在[-1, 1]之间
        if (position <= -1 || position >= 1f) {
            page.setRotation(0);
        } else {
            page.setRotation(position * 30);
            page.setPivotX(page.getWidth() * .5f);
            page.setPivotY(page.getHeight() * 1f);
        }
    }
}
