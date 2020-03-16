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

package com.qmuiteam.qmui.widget.dialog;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.layout.QMUIConstraintLayout;
import com.qmuiteam.qmui.layout.QMUIFrameLayout;
import com.qmuiteam.qmui.skin.QMUISkinHelper;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.skin.defaultAttr.QMUISkinSimpleDefaultAttrProvider;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.textview.QMUISpanTouchFixTextView;

public class QMUIBottomSheetListItemView extends QMUIConstraintLayout {

    private AppCompatImageView mIconView;
    private QMUISpanTouchFixTextView mTextView;
    private QMUIFrameLayout mRedPointView;
    private AppCompatImageView mMarkView = null;
    private int mItemHeight;

    public QMUIBottomSheetListItemView(Context context, boolean markStyle, boolean gravityCenter) {
        super(context);
        setBackground(QMUIResHelper.getAttrDrawable(
                context, R.attr.qmui_skin_support_bottom_sheet_list_item_bg));
        int paddingHor = QMUIResHelper.getAttrDimen(context, R.attr.qmui_bottom_sheet_padding_hor);
        setPadding(paddingHor, 0, paddingHor, 0);
        QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
        builder.background(R.attr.qmui_skin_support_bottom_sheet_list_item_bg);
        QMUISkinHelper.setSkinValue(this, builder);
        builder.clear();

        mIconView = new AppCompatImageView(context);
        mIconView.setId(View.generateViewId());
        mIconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        mTextView = new QMUISpanTouchFixTextView(context);
        mTextView.setId(View.generateViewId());
        QMUISkinSimpleDefaultAttrProvider provider = new QMUISkinSimpleDefaultAttrProvider();
        provider.setDefaultSkinAttr(QMUISkinValueBuilder.TEXT_COLOR,
                R.attr.qmui_skin_support_bottom_sheet_list_item_text_color);
        QMUIResHelper.assignTextViewWithAttr(mTextView, R.attr.qmui_bottom_sheet_list_item_text_style);
        QMUISkinHelper.setSkinDefaultProvider(mTextView, provider);

        mRedPointView = new QMUIFrameLayout(context);
        mRedPointView.setId(View.generateViewId());
        mRedPointView.setBackgroundColor(QMUIResHelper.getAttrColor(
                context, R.attr.qmui_skin_support_bottom_sheet_list_red_point_color));
        builder.background(R.attr.qmui_skin_support_bottom_sheet_list_red_point_color);
        QMUISkinHelper.setSkinValue(mRedPointView, builder);
        builder.clear();

        if (markStyle) {
            mMarkView = new AppCompatImageView(context);
            mMarkView.setId(View.generateViewId());
            mMarkView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mMarkView.setImageDrawable(QMUIResHelper.getAttrDrawable(
                    context, R.attr.qmui_skin_support_bottom_sheet_list_mark));
            builder.src(R.attr.qmui_skin_support_bottom_sheet_list_mark);
            QMUISkinHelper.setSkinValue(mMarkView, builder);
        }
        builder.release();

        int iconSize = QMUIResHelper.getAttrDimen(
                context, R.attr.qmui_bottom_sheet_list_item_icon_size);
        LayoutParams lp = new ConstraintLayout.LayoutParams(iconSize, iconSize);
        lp.leftToLeft = LayoutParams.PARENT_ID;
        lp.topToTop = LayoutParams.PARENT_ID;
        lp.rightToLeft = mTextView.getId();
        lp.bottomToBottom = LayoutParams.PARENT_ID;
        lp.horizontalChainStyle = LayoutParams.CHAIN_PACKED;
        lp.horizontalBias = gravityCenter ? 0.5f : 0f;
        addView(mIconView, lp);

        lp = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftToRight = mIconView.getId();
        lp.rightToLeft = mRedPointView.getId();
        lp.topToTop = LayoutParams.PARENT_ID;
        lp.bottomToBottom = LayoutParams.PARENT_ID;
        lp.horizontalChainStyle = LayoutParams.CHAIN_PACKED;
        lp.horizontalBias = gravityCenter ? 0.5f : 0f;
        lp.leftMargin = QMUIResHelper.getAttrDimen(
                context, R.attr.qmui_bottom_sheet_list_item_icon_margin_right);
        lp.goneLeftMargin = 0;
        addView(mTextView, lp);

        int redPointSize = QMUIResHelper.getAttrDimen(
                context, R.attr.qmui_bottom_sheet_list_item_red_point_size);
        lp = new ConstraintLayout.LayoutParams(redPointSize, redPointSize);
        lp.leftToRight = mTextView.getId();
        if (markStyle) {
            lp.rightToLeft = mMarkView.getId();
            lp.rightMargin = QMUIResHelper.getAttrDimen(
                    context, R.attr.qmui_bottom_sheet_list_item_mark_margin_left);
        } else {
            lp.rightToRight = LayoutParams.PARENT_ID;
        }
        lp.topToTop = LayoutParams.PARENT_ID;
        lp.bottomToBottom = LayoutParams.PARENT_ID;
        lp.horizontalChainStyle = LayoutParams.CHAIN_PACKED;
        lp.horizontalBias = gravityCenter ? 0.5f : 0f;
        lp.leftMargin = QMUIResHelper.getAttrDimen(
                context, R.attr.qmui_bottom_sheet_list_item_tip_point_margin_left);
        addView(mRedPointView, lp);

        if (markStyle) {
            lp = new ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.rightToRight = LayoutParams.PARENT_ID;
            lp.topToTop = LayoutParams.PARENT_ID;
            lp.bottomToBottom = LayoutParams.PARENT_ID;
            addView(mMarkView, lp);
        }

        mItemHeight = QMUIResHelper.getAttrDimen(context, R.attr.qmui_bottom_sheet_list_item_height);
    }

    public void render(@NonNull QMUIBottomSheetListItemModel itemModel, boolean isChecked) {
        QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
        if (itemModel.imageSkinSrcAttr != 0) {
            builder.src(itemModel.imageSkinSrcAttr);
            QMUISkinHelper.setSkinValue(mIconView, builder);
            mIconView.setImageDrawable(
                    QMUISkinHelper.getSkinDrawable(this, itemModel.imageSkinSrcAttr));
            mIconView.setVisibility(View.VISIBLE);
        } else {
            Drawable drawable = itemModel.image;
            if (drawable == null && itemModel.imageRes != 0) {
                drawable = ContextCompat.getDrawable(getContext(), itemModel.imageRes);
            }
            if (drawable != null) {
                drawable.mutate();
                mIconView.setImageDrawable(drawable);
                if (itemModel.imageSkinTintColorAttr != 0) {
                    builder.tintColor(itemModel.imageSkinTintColorAttr);
                    QMUISkinHelper.setSkinValue(mIconView, builder);
                } else {
                    QMUISkinHelper.setSkinValue(mIconView, "");
                }
            } else {
                mIconView.setVisibility(View.GONE);
            }
        }
        builder.clear();

        mTextView.setText(itemModel.text);
        if (itemModel.typeface != null) {
            mTextView.setTypeface(itemModel.typeface);
        }
        if (itemModel.textSkinColorAttr != 0) {
            builder.textColor(itemModel.textSkinColorAttr);
            QMUISkinHelper.setSkinValue(mTextView, builder);
            ColorStateList color = QMUISkinHelper.getSkinColorStateList(mTextView, itemModel.textSkinColorAttr);
            if (color != null) {
                mTextView.setTextColor(color);
            }
        } else {
            QMUISkinHelper.setSkinValue(mTextView, "");
        }

        mRedPointView.setVisibility(itemModel.hasRedPoint ? View.VISIBLE : View.GONE);

        if (mMarkView != null) {
            mMarkView.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(mItemHeight, MeasureSpec.EXACTLY));
    }
}
