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

package com.qmuiteam.qmui.type;

import androidx.annotation.NonNull;

import com.qmuiteam.qmui.type.element.Element;

import java.util.Map;

public class TypeModel {
    private CharSequence mOrigin;
    private final Map<Integer, Element> mElementMap;
    private Element mFirstElement;
    private Element mLastElement;

    public TypeModel(@NonNull Map<Integer, Element> elementMap,
                     Element firstElement,
                     Element lastElement) {
        mElementMap = elementMap;
        mFirstElement = firstElement;
        mLastElement = lastElement;
    }


    public void insertAfterElement(Element element, @NonNull Element toInsert) {
        Element next = element.getNext();
        element.setNext(toInsert);
        toInsert.setNext(next);
        if (next == null) {
            mLastElement = toInsert;
        }
    }

    public void insertBeforeElement(Element element, @NonNull Element toInsert) {
        Element prev = element.getPrev();
        element.setPrev(toInsert);
        toInsert.setPrev(prev);
        if (prev == null) {
            mFirstElement = toInsert;
        }
    }

    public Element firstElement() {
        return mFirstElement;
    }

    public Element lastElement() {
        return mLastElement;
    }
}