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


package com.qmuiteam.qmuidemo.model;

import com.qmuiteam.qmui.widget.section.QMUISection;

import java.util.Objects;

public class SectionItem implements QMUISection.Model<SectionItem> {
    private final String text;

    public SectionItem(String text){
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public SectionItem cloneForDiff() {
        return new SectionItem(getText());
    }

    @Override
    public boolean isSameItem(SectionItem other) {
        return Objects.equals(text, other.text);
    }

    @Override
    public boolean isSameContent(SectionItem other) {
        return true;
    }
}
