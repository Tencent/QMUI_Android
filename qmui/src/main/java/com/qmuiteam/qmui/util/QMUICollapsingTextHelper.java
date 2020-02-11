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

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qmuiteam.qmui.util;

import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Interpolator;

import com.qmuiteam.qmui.R;

import androidx.annotation.ColorInt;
import androidx.annotation.RequiresApi;
import androidx.core.text.TextDirectionHeuristicsCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;

public final class QMUICollapsingTextHelper {

    // Pre-JB-MR2 doesn't support HW accelerated canvas scaled text so we will workaround it
    // by using our own texture
    private static final boolean USE_SCALING_TEXTURE = Build.VERSION.SDK_INT < 18;

    private static final boolean DEBUG_DRAW = false;
    private static final Paint DEBUG_DRAW_PAINT;

    static {
        // 测试逻辑，不作检测
        // noinspection ConstantConditions
        DEBUG_DRAW_PAINT = DEBUG_DRAW ? new Paint() : null;
        // noinspection ConstantConditions
        if (DEBUG_DRAW_PAINT != null) {
            DEBUG_DRAW_PAINT.setAntiAlias(true);
            DEBUG_DRAW_PAINT.setColor(Color.MAGENTA);
        }
    }

    private final View mView;

    private boolean mDrawTitle;
    private float mExpandedFraction;

    private final Rect mExpandedBounds;
    private final Rect mCollapsedBounds;
    private final RectF mCurrentBounds;
    private int mExpandedTextGravity = Gravity.CENTER_VERTICAL;
    private int mCollapsedTextGravity = Gravity.CENTER_VERTICAL;
    private float mExpandedTextSize = 15;
    private float mCollapsedTextSize = 15;
    private ColorStateList mExpandedTextColor;
    private ColorStateList mCollapsedTextColor;

    private float mExpandedDrawY;
    private float mCollapsedDrawY;
    private float mExpandedDrawX;
    private float mCollapsedDrawX;
    private float mCurrentDrawX;
    private float mCurrentDrawY;
    private float mCollapsedTextWidth;
    private float mExpandedTextWidth;
    private float mCurrentTextWidth;
    private float mCollapsedTextHeight;
    private float mExpandedTextHeight;
    private float mCurrentTextHeight;
    private Typeface mCollapsedTypeface;
    private Typeface mExpandedTypeface;
    private Typeface mCurrentTypeface;

    private CharSequence mText;
    private CharSequence mTextToDraw;
    private boolean mIsRtl;

    private boolean mUseTexture;
    private Bitmap mExpandedTitleTexture;
    private Paint mTexturePaint;
    private float mTextureAscent;
    private float mTextureDescent;

    private float mScale;
    private float mCurrentTextSize;

    private int[] mState;

    private boolean mBoundsChanged;

    private final TextPaint mTextPaint;

    private Interpolator mPositionInterpolator;
    private Interpolator mTextSizeInterpolator;

    private float mCollapsedShadowRadius, mCollapsedShadowDx, mCollapsedShadowDy;
    private int mCollapsedShadowColor;

    private float mExpandedShadowRadius, mExpandedShadowDx, mExpandedShadowDy;
    private int mExpandedShadowColor;

    public QMUICollapsingTextHelper(View view){
        this(view, 0f);
    }

    public QMUICollapsingTextHelper(View view, float defaultExpanededFraction) {
        mView = view;

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);

        mExpandedFraction = defaultExpanededFraction;
        mCollapsedBounds = new Rect();
        mExpandedBounds = new Rect();
        mCurrentBounds = new RectF();
    }

    public void setTextSizeInterpolator(Interpolator interpolator) {
        mTextSizeInterpolator = interpolator;
        recalculate();
    }

    public void setPositionInterpolator(Interpolator interpolator) {
        mPositionInterpolator = interpolator;
        recalculate();
    }

    public void setTextSize(float collapsedTextSize, float expandedTextSize, boolean recalculate){
        if(mExpandedTextSize != expandedTextSize || mCollapsedTextSize != collapsedTextSize){
            mExpandedTextSize = expandedTextSize;
            mCollapsedTextSize = collapsedTextSize;
            if(recalculate){
                recalculate();
            }
        }
    }

    public void setExpandedTextSize(float textSize) {
        if (mExpandedTextSize != textSize) {
            mExpandedTextSize = textSize;
            recalculate();
        }
    }

    public void setCollapsedTextSize(float textSize) {
        if (mCollapsedTextSize != textSize) {
            mCollapsedTextSize = textSize;
            recalculate();
        }
    }

    public void setCollapsedTextColor(ColorStateList textColor) {
        if (mCollapsedTextColor != textColor) {
            mCollapsedTextColor = textColor;
            recalculate();
        }
    }

    public void setExpandedTextColor(ColorStateList textColor) {
        if (mExpandedTextColor != textColor) {
            mExpandedTextColor = textColor;
            recalculate();
        }
    }

    public void setTextColor(ColorStateList collapsedTextColor, ColorStateList expandedTextColor,
                             boolean recalculate){
        if(mCollapsedTextColor != collapsedTextColor || mExpandedTextColor != expandedTextColor){
            mCollapsedTextColor = collapsedTextColor;
            mExpandedTextColor = expandedTextColor;
            if(recalculate){
                recalculate();
            }
        }
    }


    public void setCollapsedTextAppearance(int resId) {
        TypedArray a = mView.getContext().obtainStyledAttributes(resId, R.styleable.QMUITextAppearance);
        if (a.hasValue(R.styleable.QMUITextAppearance_android_textColor)) {
            mCollapsedTextColor = a.getColorStateList(R.styleable.QMUITextAppearance_android_textColor);
        }
        if (a.hasValue(R.styleable.QMUITextAppearance_android_textSize)) {
            mCollapsedTextSize = a.getDimensionPixelSize(R.styleable.QMUITextAppearance_android_textSize,
                    (int) mCollapsedTextSize);
        }
        mCollapsedShadowColor = a.getInt(R.styleable.QMUITextAppearance_android_shadowColor, 0);
        mCollapsedShadowDx = a.getFloat(R.styleable.QMUITextAppearance_android_shadowDx, 0);
        mCollapsedShadowDy = a.getFloat(R.styleable.QMUITextAppearance_android_shadowDy, 0);
        mCollapsedShadowRadius = a.getFloat(R.styleable.QMUITextAppearance_android_shadowRadius, 0);
        a.recycle();

        if (Build.VERSION.SDK_INT >= 16) {
            mCollapsedTypeface = readFontFamilyTypeface(resId);
        }

        recalculate();
    }

    public void setExpandedTextAppearance(int resId) {
        TypedArray a = mView.getContext().obtainStyledAttributes(resId, R.styleable.QMUITextAppearance);
        if (a.hasValue(R.styleable.QMUITextAppearance_android_textColor)) {
            mExpandedTextColor = a.getColorStateList(R.styleable.QMUITextAppearance_android_textColor);
        }
        if (a.hasValue(R.styleable.QMUITextAppearance_android_textSize)) {
            mExpandedTextSize = a.getDimensionPixelSize(R.styleable.QMUITextAppearance_android_textSize,
                    (int) mExpandedTextSize);
        }
        mExpandedShadowColor = a.getInt(
                R.styleable.QMUITextAppearance_android_shadowColor, 0);
        mExpandedShadowDx = a.getFloat(
                R.styleable.QMUITextAppearance_android_shadowDx, 0);
        mExpandedShadowDy = a.getFloat(
                R.styleable.QMUITextAppearance_android_shadowDy, 0);
        mExpandedShadowRadius = a.getFloat(
                R.styleable.QMUITextAppearance_android_shadowRadius, 0);
        a.recycle();

        if (Build.VERSION.SDK_INT >= 16) {
            mExpandedTypeface = readFontFamilyTypeface(resId);
        }

        recalculate();
    }

    public void setExpandedBounds(int left, int top, int right, int bottom) {
        if (!rectEquals(mExpandedBounds, left, top, right, bottom)) {
            mExpandedBounds.set(left, top, right, bottom);
            mBoundsChanged = true;
            onBoundsChanged();
        }
    }

    public void setCollapsedBounds(int left, int top, int right, int bottom) {
        if (!rectEquals(mCollapsedBounds, left, top, right, bottom)) {
            mCollapsedBounds.set(left, top, right, bottom);
            mBoundsChanged = true;
            onBoundsChanged();
        }
    }

    void onBoundsChanged() {
        mDrawTitle = mCollapsedBounds.width() > 0 && mCollapsedBounds.height() > 0
                && mExpandedBounds.width() > 0 && mExpandedBounds.height() > 0;
    }

    public void setExpandedTextGravity(int gravity) {
        if (mExpandedTextGravity != gravity) {
            mExpandedTextGravity = gravity;
            recalculate();
        }
    }

    public int getExpandedTextGravity() {
        return mExpandedTextGravity;
    }

    public void setCollapsedTextGravity(int gravity) {
        if (mCollapsedTextGravity != gravity) {
            mCollapsedTextGravity = gravity;
            recalculate();
        }
    }

    public int getCollapsedTextGravity() {
        return mCollapsedTextGravity;
    }

    public void setGravity(int collapsedGravity, int expandedGravity, boolean recalculate){
        if(mCollapsedTextGravity != collapsedGravity || mExpandedTextGravity != expandedGravity){
            mCollapsedTextGravity = collapsedGravity;
            mExpandedTextGravity = expandedGravity;
            if(recalculate){
                recalculate();
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private Typeface readFontFamilyTypeface(int resId) {
        final TypedArray a = mView.getContext().obtainStyledAttributes(resId,
                new int[]{android.R.attr.fontFamily});
        try {
            final String family = a.getString(0);
            if (family != null) {
                return Typeface.create(family, Typeface.NORMAL);
            }
        } finally {
            a.recycle();
        }
        return null;
    }

    public void setTypeface(Typeface collapsedTypeface, Typeface expandedTypeface, boolean recalculate){
        if(mCollapsedTypeface != collapsedTypeface || mExpandedTypeface != expandedTypeface){
            mCollapsedTypeface = collapsedTypeface;
            mExpandedTypeface = expandedTypeface;
            if(recalculate){
                recalculate();
            }
        }
    }

    public void setCollapsedTypeface(Typeface typeface) {
        if (mCollapsedTypeface != typeface) {
            mCollapsedTypeface = typeface;
            recalculate();
        }
    }

    public void setExpandedTypeface(Typeface typeface) {
        if (mExpandedTypeface != typeface) {
            mExpandedTypeface = typeface;
            recalculate();
        }
    }

    public void setTypefaces(Typeface typeface) {
        mCollapsedTypeface = mExpandedTypeface = typeface;
        recalculate();
    }

    public Typeface getCollapsedTypeface() {
        return mCollapsedTypeface != null ? mCollapsedTypeface : Typeface.DEFAULT;
    }

    public Typeface getExpandedTypeface() {
        return mExpandedTypeface != null ? mExpandedTypeface : Typeface.DEFAULT;
    }

    /**
     * Set the value indicating the current scroll value. This decides how much of the
     * background will be displayed, as well as the title metrics/positioning.
     * <p>
     * A value of {@code 0.0} indicates that the layout is fully expanded.
     * A value of {@code 1.0} indicates that the layout is fully collapsed.
     */
    public void setExpansionFraction(float fraction) {
        fraction = QMUILangHelper.constrain(fraction, 0f, 1f);

        if (fraction != mExpandedFraction) {
            mExpandedFraction = fraction;
            calculateCurrentOffsets();
        }
    }

    public final boolean setState(final int[] state) {
        mState = state;

        if (isStateful()) {
            recalculate();
            return true;
        }

        return false;
    }

    public final boolean isStateful() {
        return (mCollapsedTextColor != null && mCollapsedTextColor.isStateful())
                || (mExpandedTextColor != null && mExpandedTextColor.isStateful());
    }

    public float getExpansionFraction() {
        return mExpandedFraction;
    }

    public float getCollapsedTextSize() {
        return mCollapsedTextSize;
    }

    public float getExpandedTextSize() {
        return mExpandedTextSize;
    }

    public void calculateCurrentOffsets() {
        calculateOffsets(mExpandedFraction);
    }

    private void calculateOffsets(final float fraction) {
        interpolateBounds(fraction);
        mCurrentDrawX = lerp(mExpandedDrawX, mCollapsedDrawX, fraction,
                mPositionInterpolator);
        mCurrentDrawY = lerp(mExpandedDrawY, mCollapsedDrawY, fraction,
                mPositionInterpolator);
        mCurrentTextHeight = lerp(mExpandedTextHeight, mCollapsedTextHeight, fraction,
                mPositionInterpolator);
        mCurrentTextWidth = lerp(mExpandedTextWidth, mCollapsedTextWidth, fraction,
                mPositionInterpolator);

        setInterpolatedTextSize(lerp(mExpandedTextSize, mCollapsedTextSize,
                fraction, mTextSizeInterpolator));

        if (mCollapsedTextColor != mExpandedTextColor) {
            // If the collapsed and expanded text colors are different, blend them based on the
            // fraction
            mTextPaint.setColor(QMUIColorHelper.computeColor(
                    getCurrentExpandedTextColor(), getCurrentCollapsedTextColor(), fraction));
        } else {
            mTextPaint.setColor(getCurrentCollapsedTextColor());
        }

        mTextPaint.setShadowLayer(
                lerp(mExpandedShadowRadius, mCollapsedShadowRadius, fraction, null),
                lerp(mExpandedShadowDx, mCollapsedShadowDx, fraction, null),
                lerp(mExpandedShadowDy, mCollapsedShadowDy, fraction, null),
                QMUIColorHelper.computeColor(mExpandedShadowColor, mCollapsedShadowColor, fraction));

        ViewCompat.postInvalidateOnAnimation(mView);
    }

    @ColorInt
    private int getCurrentExpandedTextColor() {
        if(mExpandedTextColor == null){
            return 0;
        }
        if (mState != null) {
            return mExpandedTextColor.getColorForState(mState, 0);
        } else {
            return mExpandedTextColor.getDefaultColor();
        }
    }

    @ColorInt
    private int getCurrentCollapsedTextColor() {
        if(mCollapsedTextColor == null){
            return 0;
        }
        if (mState != null) {
            return mCollapsedTextColor.getColorForState(mState, 0);
        } else {
            return mCollapsedTextColor.getDefaultColor();
        }
    }

    public void calculateBaseOffsets() {
        final float currentTextSize = mCurrentTextSize;

        // We then calculate the collapsed text size, using the same logic
        calculateUsingTextSize(mCollapsedTextSize);
        mCollapsedTextWidth = mTextToDraw != null ?
                mTextPaint.measureText(mTextToDraw, 0, mTextToDraw.length()) : 0;
        mCollapsedTextHeight = mTextPaint.descent() - mTextPaint.ascent();
        final int collapsedAbsGravity = GravityCompat.getAbsoluteGravity(mCollapsedTextGravity,
                mIsRtl ? ViewCompat.LAYOUT_DIRECTION_RTL : ViewCompat.LAYOUT_DIRECTION_LTR);
        switch (collapsedAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.BOTTOM:
                mCollapsedDrawY = mCollapsedBounds.bottom - mTextPaint.descent();
                break;
            case Gravity.TOP:
                mCollapsedDrawY = mCollapsedBounds.top - mTextPaint.ascent();
                break;
            case Gravity.CENTER_VERTICAL:
            default:
                float textOffset = (mCollapsedTextHeight / 2) - mTextPaint.descent();
                mCollapsedDrawY = mCollapsedBounds.centerY() + textOffset;
                break;
        }
        switch (collapsedAbsGravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
            case Gravity.CENTER_HORIZONTAL:
                mCollapsedDrawX = mCollapsedBounds.centerX() - (mCollapsedTextWidth / 2);
                break;
            case Gravity.RIGHT:
                mCollapsedDrawX = mCollapsedBounds.right - mCollapsedTextWidth;
                break;
            case Gravity.LEFT:
            default:
                mCollapsedDrawX = mCollapsedBounds.left;
                break;
        }

        calculateUsingTextSize(mExpandedTextSize);
        mExpandedTextWidth = mTextToDraw != null
                ? mTextPaint.measureText(mTextToDraw, 0, mTextToDraw.length()) : 0;
        mExpandedTextHeight = mTextPaint.descent() - mTextPaint.ascent();
        final int expandedAbsGravity = GravityCompat.getAbsoluteGravity(mExpandedTextGravity,
                mIsRtl ? ViewCompat.LAYOUT_DIRECTION_RTL : ViewCompat.LAYOUT_DIRECTION_LTR);
        switch (expandedAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.BOTTOM:
                mExpandedDrawY = mExpandedBounds.bottom - mTextPaint.descent();
                break;
            case Gravity.TOP:
                mExpandedDrawY = mExpandedBounds.top - mTextPaint.ascent();
                break;
            case Gravity.CENTER_VERTICAL:
            default:
                float textOffset = (mExpandedTextHeight / 2) - mTextPaint.descent();
                mExpandedDrawY = mExpandedBounds.centerY() + textOffset;
                break;
        }
        switch (expandedAbsGravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
            case Gravity.CENTER_HORIZONTAL:
                mExpandedDrawX = mExpandedBounds.centerX() - (mExpandedTextWidth / 2);
                break;
            case Gravity.RIGHT:
                mExpandedDrawX = mExpandedBounds.right - mExpandedTextWidth;
                break;
            case Gravity.LEFT:
            default:
                mExpandedDrawX = mExpandedBounds.left;
                break;
        }

        // The bounds have changed so we need to clear the texture
        clearTexture();
        // Now reset the text size back to the original
        setInterpolatedTextSize(currentTextSize);
    }

    private void interpolateBounds(float fraction) {
        mCurrentBounds.left = lerp(mExpandedBounds.left, mCollapsedBounds.left,
                fraction, mPositionInterpolator);
        mCurrentBounds.top = lerp(mExpandedDrawY, mCollapsedDrawY,
                fraction, mPositionInterpolator);
        mCurrentBounds.right = lerp(mExpandedBounds.right, mCollapsedBounds.right,
                fraction, mPositionInterpolator);
        mCurrentBounds.bottom = lerp(mExpandedBounds.bottom, mCollapsedBounds.bottom,
                fraction, mPositionInterpolator);
    }

    // 系统类原有代码，不作检测
    @SuppressWarnings("UnusedAssignment")
    public void draw(Canvas canvas) {
        final int saveCount = canvas.save();

        if (mTextToDraw != null && mDrawTitle) {
            float x = mCurrentDrawX;
            float y = mCurrentDrawY;

            final boolean drawTexture = mUseTexture && mExpandedTitleTexture != null;

            final float ascent;
            final float descent;
            if (drawTexture) {
                ascent = mTextureAscent * mScale;
                descent = mTextureDescent * mScale;
            } else {
                ascent = mTextPaint.ascent() * mScale;
                descent = mTextPaint.descent() * mScale;
            }

            if (DEBUG_DRAW) {
                // Just a debug tool, which drawn a magenta rect in the text bounds
                canvas.drawRect(mCurrentBounds.left, y + ascent, mCurrentBounds.right, y + descent,
                        DEBUG_DRAW_PAINT);
            }

            if (drawTexture) {
                y += ascent;
            }

            if (mScale != 1f) {
                canvas.scale(mScale, mScale, x, y);
            }

            if (drawTexture) {
                // If we should use a texture, draw it instead of text
                canvas.drawBitmap(mExpandedTitleTexture, x, y, mTexturePaint);
            } else {
                canvas.drawText(mTextToDraw, 0, mTextToDraw.length(), x, y, mTextPaint);
            }
        }

        canvas.restoreToCount(saveCount);
    }

    private boolean calculateIsRtl(CharSequence text) {
        final boolean defaultIsRtl = ViewCompat.getLayoutDirection(mView)
                == ViewCompat.LAYOUT_DIRECTION_RTL;
        return (defaultIsRtl
                ? TextDirectionHeuristicsCompat.FIRSTSTRONG_RTL
                : TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR).isRtl(text, 0, text.length());
    }

    private void setInterpolatedTextSize(float textSize) {
        calculateUsingTextSize(textSize);

        // Use our texture if the scale isn't 1.0
        mUseTexture = USE_SCALING_TEXTURE && mScale != 1f;

        if (mUseTexture) {
            // Make sure we have an expanded texture if needed
            ensureExpandedTexture();
        }

        ViewCompat.postInvalidateOnAnimation(mView);
    }

    private void calculateUsingTextSize(final float textSize) {
        if (mText == null) return;

        final float collapsedWidth = mCollapsedBounds.width();
        final float expandedWidth = mExpandedBounds.width();

        final float availableWidth;
        final float newTextSize;
        boolean updateDrawText = false;

        if(mExpandedFraction == 1f){
            if (mCurrentTypeface != mCollapsedTypeface) {
                mCurrentTypeface = mCollapsedTypeface;
                updateDrawText = true;
            }
        }else if(mExpandedFraction == 0f){
            if (mCurrentTypeface != mExpandedTypeface) {
                mCurrentTypeface = mExpandedTypeface;
                updateDrawText = true;
            }
        }

        if (isClose(textSize, mCollapsedTextSize)) {
            newTextSize = mCollapsedTextSize;
            mScale = 1f;
            availableWidth = collapsedWidth;
        } else {
            newTextSize = mExpandedTextSize;
            if (isClose(textSize, mExpandedTextSize)) {
                // If we're close to the expanded text size, snap to it and use a scale of 1
                mScale = 1f;
            } else {
                // Else, we'll scale down from the expanded text size
                mScale = textSize / mExpandedTextSize;
            }

            final float textSizeRatio = mCollapsedTextSize / mExpandedTextSize;
            // This is the size of the expanded bounds when it is scaled to match the
            // collapsed text size
            final float scaledDownWidth = expandedWidth * textSizeRatio;

            if (scaledDownWidth > collapsedWidth) {
                // If the scaled down size is larger than the actual collapsed width, we need to
                // cap the available width so that when the expanded text scales down, it matches
                // the collapsed width
                availableWidth = Math.min(collapsedWidth / textSizeRatio, expandedWidth);
            } else {
                // Otherwise we'll just use the expanded width
                availableWidth = expandedWidth;
            }
        }

        if (availableWidth > 0) {
            updateDrawText = (mCurrentTextSize != newTextSize) || mBoundsChanged || updateDrawText;
            mCurrentTextSize = newTextSize;
            mBoundsChanged = false;
        }

        if (mTextToDraw == null || updateDrawText) {
            mTextPaint.setTextSize(mCurrentTextSize);
            mTextPaint.setTypeface(mCurrentTypeface);
            // Use linear text scaling if we're scaling the canvas
            mTextPaint.setLinearText(mScale != 1f);

            // If we don't currently have text to draw, or the text size has changed, ellipsize...
            final CharSequence title = TextUtils.ellipsize(mText, mTextPaint,
                    availableWidth, TextUtils.TruncateAt.END);
            if (!TextUtils.equals(title, mTextToDraw)) {
                mTextToDraw = title;
                mIsRtl = calculateIsRtl(mTextToDraw);
            }
        }
    }

    private void ensureExpandedTexture() {
        if (mExpandedTitleTexture != null || mExpandedBounds.isEmpty()
                || TextUtils.isEmpty(mTextToDraw)) {
            return;
        }

        calculateOffsets(0f);
        mTextureAscent = mTextPaint.ascent();
        mTextureDescent = mTextPaint.descent();

        final int w = Math.round(mTextPaint.measureText(mTextToDraw, 0, mTextToDraw.length()));
        final int h = Math.round(mTextureDescent - mTextureAscent);

        if (w <= 0 || h <= 0) {
            return; // If the width or height are 0, return
        }

        mExpandedTitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(mExpandedTitleTexture);
        c.drawText(mTextToDraw, 0, mTextToDraw.length(), 0, h - mTextPaint.descent(), mTextPaint);

        if (mTexturePaint == null) {
            // Make sure we have a paint
            mTexturePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        }
    }

    public void recalculate() {
        if (mView.getHeight() > 0 && mView.getWidth() > 0) {
            // If we've already been laid out, calculate everything now otherwise we'll wait
            // until a layout
            calculateBaseOffsets();
            calculateCurrentOffsets();
        }
    }

    /**
     * Set the title to display
     *
     * @param text content of title
     */
    public void setText(CharSequence text) {
        if (text == null || !text.equals(mText)) {
            mText = text;
            mTextToDraw = null;
            clearTexture();
            recalculate();
        }
    }

    public CharSequence getText() {
        return mText;
    }

    private void clearTexture() {
        if (mExpandedTitleTexture != null) {
            mExpandedTitleTexture.recycle();
            mExpandedTitleTexture = null;
        }
    }

    public float getExpandedTextWidth() {
        return mExpandedTextWidth;
    }

    public float getCollapsedTextWidth() {
        return mCollapsedTextWidth;
    }

    public float getExpandedTextHeight() {
        return mExpandedTextHeight;
    }

    public float getCollapsedTextHeight() {
        return mCollapsedTextHeight;
    }

    public float getExpandedDrawX() {
        return mExpandedDrawX;
    }

    public float getCollapsedDrawX() {
        return mCollapsedDrawX;
    }


    /**
     * Returns true if {@code value} is 'close' to it's closest decimal value. Close is currently
     * defined as it's difference being < 0.001.
     */
    private static boolean isClose(float value, float targetValue) {
        return Math.abs(value - targetValue) < 0.001f;
    }

    ColorStateList getExpandedTextColor() {
        return mExpandedTextColor;
    }

    ColorStateList getCollapsedTextColor() {
        return mCollapsedTextColor;
    }

    public static float lerp(float startValue, float endValue, float fraction,
                              Interpolator interpolator) {
        if (interpolator != null) {
            fraction = interpolator.getInterpolation(fraction);
        }
        return startValue + Math.round(fraction * (endValue - startValue));
    }

    private static boolean rectEquals(Rect r, int left, int top, int right, int bottom) {
        return !(r.left != left || r.top != top || r.right != right || r.bottom != bottom);
    }
}
