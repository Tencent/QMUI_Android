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

package com.qmuiteam.qmui.type.element;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qmuiteam.qmui.type.EnvironmentUpdater;
import com.qmuiteam.qmui.type.TypeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Element {
    public static final int VISIBLE = 0;
    public static final int INVISIBLE = 1;
    public static final int GONE = 2;

    public static final int WORD_PART_WHOLE = 0;
    public static final int WORD_PART_START = 1;
    public static final int WORD_PART_MIDDLE = 2;
    public static final int WORD_PART_END = 3;


    public static final int LINE_BREAK_TYPE_NORMAL = 0;
    public static final int LINE_BREAK_TYPE_NOT_START = 1;
    public static final int LINE_BREAK_TYPE_NOT_END = 2;
    public static final int LINE_BREAK_WORD_BREAK_ALLOWED = 3;

    private static final char[] NOT_START_CHARS =new char[]{
            ',', '.', ';', ']', '>', ')', '?', '"', '\'', '!', ':', '}', '」',
            '，', '。', '；', '、', '】', '》', '）', '？', '”', '！', '：', '』',
    };
    private static final char[] NOT_END_CHARS = new char[]{
            '(', '<', '[', '{', '“', '「', '『', '（', '《'
    };

    static {
        Arrays.sort(NOT_START_CHARS);
        Arrays.sort(NOT_END_CHARS);
    }

    private final char mChar;
    private final CharSequence mText;
    private final int mIndex;
    private final int mOriginIndex;
    private final String mDescription;
    private Element mPrevEffect;
    private Element mNextEffect;
    private Element mPrev;
    private Element mNext;
    private int mWordPart = WORD_PART_WHOLE;
    private int mLineBreakType = LINE_BREAK_TYPE_NORMAL;
    private int mVisible = VISIBLE;


    private List<Integer> mSaveType;
    private List<Integer> mRestoreType;
    @Nullable
    private List<EnvironmentUpdater> mEnvironmentUpdater;

    private float mMeasureWidth;
    private float mMeasureHeight;
    private float mX;
    private float mY;
    private float mBaseLine;
    private float mNextGapWidth;

    public Element(Character singleChar, @Nullable CharSequence text, int index, int originIndex) {
        this(singleChar, text, index, originIndex, null);
    }

    public Element(char singleChar, @Nullable CharSequence text, int index, int originIndex, @Nullable String description) {
        mChar = singleChar;
        mText = text;
        mIndex = index;
        mOriginIndex = originIndex;
        mDescription = description;
        if(Arrays.binarySearch(NOT_START_CHARS, singleChar) >= 0){
            mLineBreakType = LINE_BREAK_TYPE_NOT_START;
        } else if(Arrays.binarySearch(NOT_END_CHARS, singleChar) >= 0){
            mLineBreakType = LINE_BREAK_TYPE_NOT_END;
        }
    }

    public void insetEffect(Element element) {
        if(element == this){
            return;
        }
        if(element.mIndex < mIndex){
            Element prev = mPrevEffect;
            Element next = this;
            while (prev != null && element.mIndex <= prev.mIndex){
                next = prev;
                prev = prev.mPrevEffect;
            }
            if(prev == element){
                return;
            }
            if(prev != null){
                prev.mNextEffect = element;
                element.mPrevEffect = prev;
            }
            element.mNextEffect = next;
            next.mPrevEffect = element;
        }else{
            Element prev = this;
            Element next = mNextEffect;
            while (next != null && element.mIndex >= next.mIndex){
                prev = next;
                next = next.mNextEffect;
            }
            if(next == element){
                return;
            }
            if(next != null){
                element.mNextEffect = next;
                next.mPrevEffect = element;
            }
            prev.mNextEffect = element;
            element.mPrevEffect = prev;
        }
    }

    public Element removeFromEffectListIfNeeded(Element head){
        boolean noSaveType = mSaveType == null || mSaveType.isEmpty();
        boolean noRestoreType = mRestoreType == null || mRestoreType.isEmpty();
        if(noSaveType && noRestoreType){
            Element prev = mPrevEffect;
            Element next = mNextEffect;
            if(prev != null){
                prev.mNextEffect = next;
                mPrevEffect = null;
            }
            if(next != null){
                next.mPrevEffect = prev;
                mNextEffect = null;
            }
            if(head == this){
                return next;
            }
        }
        return head;
    }

    public Element getNextEffect() {
        return mNextEffect;
    }

    public Element getPrevEffect() {
        return mPrevEffect;
    }

    public int getLineBreakType() {
        return mLineBreakType;
    }

    public void setLineBreakType(int lineBreakType) {
        mLineBreakType = lineBreakType;
    }

    public void setPrev(Element element) {
        this.mPrev = element;
        if (element != null) {
            element.mNext = this;
        }
    }

    public void setNext(Element element) {
        this.mNext = element;
        if (element != null) {
            element.mPrev = this;
        }
    }

    public void setWordPart(int wordPart) {
        this.mWordPart = wordPart;
    }

    public int getWordPart() {
        return mWordPart;
    }

    public void addSaveType(int type) {
        if (mSaveType == null) {
            mSaveType = new ArrayList<>();
        }
        mSaveType.add(type);
    }

    public void removeSaveType(int type) {
        if (mSaveType != null) {
            for (int i = 0; i < mSaveType.size(); i++) {
                if (mSaveType.get(i) == type) {
                    mSaveType.remove(i);
                    break;
                }
            }
        }
    }

    public void addRestoreType(int type) {
        if (mRestoreType == null) {
            mRestoreType = new ArrayList<>();
        }
        mRestoreType.add(type);
    }

    public void removeStoreType(int type) {
        if (mRestoreType != null) {
            for (int i = 0; i < mRestoreType.size(); i++) {
                if (mRestoreType.get(i) == type) {
                    mRestoreType.remove(i);
                    break;
                }
            }
        }
    }

    public boolean hasEnvironmentUpdater() {
        return mEnvironmentUpdater != null && mEnvironmentUpdater.size() > 0;
    }

    public void addEnvironmentUpdater(@NonNull EnvironmentUpdater environmentUpdater) {
        if (mEnvironmentUpdater == null) {
            mEnvironmentUpdater = new ArrayList<>();
        }
        mEnvironmentUpdater.add(environmentUpdater);
    }

    public void removeEnvironmentUpdater(@NonNull EnvironmentUpdater environmentUpdater) {
        if (mEnvironmentUpdater != null) {
            mEnvironmentUpdater.remove(environmentUpdater);
        }
    }

    public void addSingleEnvironmentUpdater(@Nullable List<Integer> changedTypes, @NonNull EnvironmentUpdater environmentUpdater) {
        if (changedTypes != null) {
            for (Integer type : changedTypes) {
                addRestoreType(type);
                addSaveType(type);
            }
        }
        addEnvironmentUpdater(environmentUpdater);
    }

    public void move(TypeEnvironment environment) {
        if (hasEnvironmentUpdater()) {
            updateEnv(environment);
            restoreEnv(environment);
        }
    }

    public int getIndex() {
        return mIndex;
    }

    public int getOriginIndex() {
        return mOriginIndex;
    }

    public CharSequence getText() {
        return mText;
    }

    public char getChar() {
        return mChar;
    }

    public boolean isSingleChar() {
        return mText == null;
    }

    @NonNull
    @Override
    public String toString() {
        return mText != null ? mText.toString() : String.valueOf(mChar);
    }

    public int getLength() {
        return mText != null ? mText.length() : 1;
    }

    public String getDescription() {
        return mDescription != null ? mDescription : toString();
    }

    protected void setMeasureDimen(float measureWidth, float measureHeight, float baseline) {
        mMeasureWidth = measureWidth;
        mMeasureHeight = measureHeight;
        mBaseLine = baseline;
    }

    public void setVisible(int visible) {
        mVisible = visible;
    }

    public int getVisible() {
        return mVisible;
    }

    public void setX(float x) {
        mX = x;
    }

    public void setY(float y) {
        mY = y;
    }

    public void setNextGapWidth(float nextGapWidth) {
        mNextGapWidth = nextGapWidth;
    }

    public float getBaseLine() {
        return mBaseLine;
    }

    public float getMeasureWidth() {
        return mMeasureWidth;
    }

    public float getMeasureHeight() {
        return mMeasureHeight;
    }

    public float getX() {
        return mX;
    }

    public float getY() {
        return mY;
    }

    public Element getNext() {
        return mNext;
    }

    public Element getPrev() {
        return mPrev;
    }

    public final void measure(TypeEnvironment env) {
        updateEnv(env);
        onMeasure(env);
        restoreEnv(env);
    }

    public final void draw(TypeEnvironment env, Canvas canvas) {
        updateEnv(env);
        if (mVisible == VISIBLE) {
            onDraw(env, canvas);
        }
        restoreEnv(env);
    }

    void updateEnv(TypeEnvironment env) {
        if (mSaveType != null) {
            for (Integer type : mSaveType) {
                env.save(type);
            }
        }
        if (mEnvironmentUpdater != null) {
            for (EnvironmentUpdater updater : mEnvironmentUpdater) {
                updater.update(env);
            }
        }
    }

    void restoreEnv(TypeEnvironment env) {
        if (mRestoreType != null) {
            for (Integer type : mRestoreType) {
                env.restore(type);
            }
        }
    }

    protected abstract void onMeasure(TypeEnvironment env);

    protected abstract void onDraw(TypeEnvironment env, Canvas canvas);

    protected void drawBg(TypeEnvironment env, Canvas canvas){
        if(env.getBackgroundColor() != Color.TRANSPARENT){
            canvas.drawRect(mX, mY, getRightWithGap(), mY + mMeasureHeight, env.getBgPaint());
        }
    }

    protected float getRightWithGap(){
        return mX + mMeasureWidth + mNextGapWidth + 0.5f;
    }

    protected void drawBorder(TypeEnvironment env, Canvas canvas){
        Paint paint = env.getBorderPaint();

        if(env.getBorderLeftWidth() > 0){
            paint.setColor(env.getBorderLeftColor());
            canvas.drawRect(mX, mY, mX + env.getBorderLeftWidth(), mY + mMeasureHeight, paint);
        }

        if(env.getBorderTopWidth() > 0) {
            paint.setColor(env.getBorderTopColor());
            canvas.drawRect(mX, mY, getRightWithGap(), mY + env.getBorderTopWidth(), paint);
        }

        if(env.getBorderRightWidth() > 0){
            paint.setColor(env.getBorderRightColor());
            canvas.drawRect(mX + mMeasureWidth - env.getBorderRightWidth(), mY,
                    mX + mMeasureWidth, mY + mMeasureHeight, paint);
        }

        if(env.getBorderBottomWidth() > 0){
            paint.setColor(env.getBorderBottomColor());
            canvas.drawRect(mX, mY + mMeasureHeight - env.getBorderBottomWidth(),
                    getRightWithGap(), mY + mMeasureHeight, paint);
        }
    }
}
