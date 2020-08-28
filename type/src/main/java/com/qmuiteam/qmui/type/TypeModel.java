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
import androidx.annotation.Nullable;

import com.qmuiteam.qmui.type.element.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TypeModel {
    private CharSequence mOrigin;
    private final Map<Integer, Element> mElementMap;
    private Element mFirstElement;
    private Element mLastElement;
    @Nullable
    private Element mFirstEffect;

    public TypeModel(
            CharSequence origin,
            @NonNull Map<Integer, Element> elementMap,
             Element firstElement,
             Element lastElement,
             @Nullable Element firstEffect) {
        mOrigin = origin;
        mElementMap = elementMap;
        mFirstElement = firstElement;
        mLastElement = lastElement;
        mFirstEffect = firstEffect;
    }

    @Nullable
    public Element getFirstEffect() {
        return mFirstEffect;
    }

    public CharSequence getOrigin() {
        return mOrigin;
    }


    public EffectRemover addBgEffect(int start, int end, final int bgColor){
        List<Integer> types = new ArrayList<>();
        types.add(TypeEnvironment.TYPE_BG_COLOR);
        return unsafeAddEffect(start, end, types, new EnvironmentUpdater() {
            @Override
            public void update(TypeEnvironment env) {
                env.setBackgroundColor(bgColor);
            }
        });
    }

    public EffectRemover addTextColorEffect(int start, int end, final int textColor){
        List<Integer> types = new ArrayList<>();
        types.add(TypeEnvironment.TYPE_TEXT_COLOR);
        return unsafeAddEffect(start, end, types, new EnvironmentUpdater() {
            @Override
            public void update(TypeEnvironment env) {
                env.setTextColor(textColor);
            }
        });
    }

    public EffectRemover addUnderLineEffect(int start, int end, final int underLineColor, final int underLineHeight){
        List<Integer> types = new ArrayList<>();
        types.add(TypeEnvironment.TYPE_BORDER_BOTTOM_WIDTH);
        types.add(TypeEnvironment.TYPE_BORDER_BOTTOM_COLOR);
        return unsafeAddEffect(start, end, types, new EnvironmentUpdater() {
            @Override
            public void update(TypeEnvironment env) {
                env.setBorderBottom(underLineHeight, underLineColor);
            }
        });
    }


    public EffectRemover unsafeAddEffect(int start, int end, List<Integer> types, EnvironmentUpdater environmentUpdater){
        Element elementStart = mElementMap.get(start);
        Element elementEnd = mElementMap.get(end);
        if(elementStart == null || elementEnd == null){
            return null;
        }
        for(Integer type: types){
            elementStart.addSaveType(type);
            elementEnd.addRestoreType(type);
        }
        elementStart.addEnvironmentUpdater(environmentUpdater);
        if(mFirstEffect == null){
            mFirstEffect = elementStart;
        }else{
            mFirstEffect.insetEffect(elementStart);
        }
        elementStart.insetEffect(elementEnd);
        return new DefaultEffectRemove(this, start, end, types, environmentUpdater);
    }

    public boolean unsafeRemoveEffect(int start, int end, List<Integer> types, EnvironmentUpdater environmentUpdater){
        Element elementStart = mElementMap.get(start);
        Element elementEnd = mElementMap.get(end);
        if(elementStart == null || elementEnd == null){
            return false;
        }
        for(Integer type: types){
            elementStart.removeSaveType(type);
            elementEnd.removeStoreType(type);
        }
        elementStart.removeEnvironmentUpdater(environmentUpdater);

        mFirstEffect = elementStart.removeFromEffectListIfNeeded(mFirstEffect);
        mFirstEffect = elementEnd.removeFromEffectListIfNeeded(mFirstEffect);
        return true;
    }

    public Element firstElement() {
        return mFirstElement;
    }

    public Element lastElement() {
        return mLastElement;
    }

    @Nullable
    public Element get(int pos){
        return mElementMap.get(pos);
    }

    public interface EffectRemover {
        void remove();
    }

    static class DefaultEffectRemove implements EffectRemover{

        private final int mStart;
        private final int mEnd;
        private final List<Integer> mTypes;
        private final EnvironmentUpdater mEnvironmentUpdater;
        private final TypeModel mTypeModel;

        public DefaultEffectRemove(
                TypeModel typeModel,
                int start,
                int end,
                List<Integer> types,
                EnvironmentUpdater environmentUpdater) {
            mTypeModel = typeModel;
            mStart = start;
            mEnd = end;
            mTypes = types;
            mEnvironmentUpdater = environmentUpdater;
        }

        @Override
        public void remove() {
            mTypeModel.unsafeRemoveEffect(mStart, mEnd, mTypes, mEnvironmentUpdater);
        }
    }
}