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

package com.qmuiteam.qmui.widget.grouplist;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.layout.QMUIConstraintLayout;
import com.qmuiteam.qmui.skin.QMUISkinHelper;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Placeholder;

/**
 * 作为通用列表 {@link QMUIGroupListView} 里的 item 使用，也可以单独使用。
 * 支持以下样式:
 * <ul>
 * <li>通过 {@link #setText(CharSequence)} 设置一行文字</li>
 * <li>通过 {@link #setDetailText(CharSequence)} 设置一行说明文字, 并通过 {@link #setOrientation(int)} 设置说明文字的位置,
 * 也可以在 xml 中使用 {@link R.styleable#QMUICommonListItemView_qmui_orientation} 设置。</li>
 * <li>通过 {@link #setAccessoryType(int)} 设置右侧 View 的类型, 可选的类型见 {@link QMUICommonListItemAccessoryType},
 * 也可以在 xml 中使用 {@link R.styleable#QMUICommonListItemView_qmui_accessory_type} 设置。</li>
 * </ul>
 *
 * @author chantchen
 * @date 2015-1-8
 */
public class QMUICommonListItemView extends QMUIConstraintLayout {

    /**
     * 右侧不显示任何东西
     */
    public final static int ACCESSORY_TYPE_NONE = 0;
    /**
     * 右侧显示一个箭头
     */
    public final static int ACCESSORY_TYPE_CHEVRON = 1;
    /**
     * 右侧显示一个开关
     */
    public final static int ACCESSORY_TYPE_SWITCH = 2;
    /**
     * 自定义右侧显示的 View
     */
    public final static int ACCESSORY_TYPE_CUSTOM = 3;

    private final static int TIP_SHOW_NOTHING = 0;
    private final static int TIP_SHOW_RED_POINT = 1;
    private final static int TIP_SHOW_NEW = 2;

    /**
     * detailText 在 title 文字的下方
     */
    public final static int VERTICAL = 0;
    /**
     * detailText 在 item 的右方
     */
    public final static int HORIZONTAL = 1;

    /**
     * TIP 在左边
     */
    public final static int TIP_POSITION_LEFT = 0;
    /**
     * TIP 在右边
     */
    public final static int TIP_POSITION_RIGHT = 1;

    @IntDef({ACCESSORY_TYPE_NONE, ACCESSORY_TYPE_CHEVRON, ACCESSORY_TYPE_SWITCH, ACCESSORY_TYPE_CUSTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface QMUICommonListItemAccessoryType {
    }

    @IntDef({VERTICAL, HORIZONTAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface QMUICommonListItemOrientation {
    }

    @IntDef({TIP_POSITION_LEFT, TIP_POSITION_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface QMUICommonListItemTipPosition {
    }

    /**
     * Item 右侧的 View 的类型
     */
    @QMUICommonListItemAccessoryType
    private int mAccessoryType;

    /**
     * 控制 detailText 是在 title 文字的下方还是 item 的右方
     */
    private int mOrientation = HORIZONTAL;

    /**
     * 控制红点的位置
     */
    @QMUICommonListItemTipPosition
    private int mTipPosition = TIP_POSITION_LEFT;


    protected ImageView mImageView;
    private ViewGroup mAccessoryView;
    protected TextView mTextView;
    protected TextView mDetailTextView;
    protected CheckBox mSwitch;
    private ImageView mRedDot;
    private ImageView mNewTipView;
    private Placeholder mAfterTitleHolder;
    private Placeholder mBeforeAccessoryHolder;
    private boolean mDisableSwitchSelf = false;

    private int mTipShown = TIP_SHOW_NOTHING;

    public QMUICommonListItemView(Context context) {
        this(context, null);
    }

    public QMUICommonListItemView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.QMUICommonListItemViewStyle);
    }

    public QMUICommonListItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    protected void init(Context context, AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.qmui_common_list_item, this, true);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.QMUICommonListItemView, defStyleAttr, 0);
        @QMUICommonListItemOrientation int orientation = array.getInt(R.styleable.QMUICommonListItemView_qmui_orientation, HORIZONTAL);
        @QMUICommonListItemAccessoryType int accessoryType = array.getInt(R.styleable.QMUICommonListItemView_qmui_accessory_type, ACCESSORY_TYPE_NONE);
        final int initTitleColor = array.getColor(R.styleable.QMUICommonListItemView_qmui_common_list_title_color, 0);
        final int initDetailColor = array.getColor(R.styleable.QMUICommonListItemView_qmui_common_list_detail_color, 0);
        array.recycle();

        mImageView = findViewById(R.id.group_list_item_imageView);
        mTextView = findViewById(R.id.group_list_item_textView);
        mRedDot = findViewById(R.id.group_list_item_tips_dot);
        mNewTipView = findViewById(R.id.group_list_item_tips_new);
        mDetailTextView = findViewById(R.id.group_list_item_detailTextView);
        mAfterTitleHolder = findViewById(R.id.group_list_item_holder_after_title);
        mBeforeAccessoryHolder = findViewById(R.id.group_list_item_holder_before_accessory);

        mAfterTitleHolder.setEmptyVisibility(View.GONE);
        mBeforeAccessoryHolder.setEmptyVisibility(View.GONE);
        mTextView.setTextColor(initTitleColor);
        mDetailTextView.setTextColor(initDetailColor);
        mAccessoryView = findViewById(R.id.group_list_item_accessoryView);
        setOrientation(orientation);
        setAccessoryType(accessoryType);
    }


    public void updateImageViewLp(LayoutParamConfig lpConfig) {
        if (lpConfig != null) {
            LayoutParams lp = (LayoutParams) mImageView.getLayoutParams();
            mImageView.setLayoutParams(lpConfig.onConfig(lp));
        }
    }

    public void setImageDrawable(Drawable drawable) {
        if (drawable == null) {
            mImageView.setVisibility(View.GONE);
        } else {
            mImageView.setImageDrawable(drawable);
            mImageView.setVisibility(View.VISIBLE);
        }
    }

    public void setTipPosition(@QMUICommonListItemTipPosition int tipPosition) {
        mTipPosition = tipPosition;
        if (mRedDot.getVisibility() == View.VISIBLE) {
            if (mTipPosition == TIP_POSITION_LEFT) {
                mAfterTitleHolder.setContentId(mRedDot.getId());
                mBeforeAccessoryHolder.setContentId(View.NO_ID);
            } else {
                mBeforeAccessoryHolder.setContentId(mRedDot.getId());
                mAfterTitleHolder.setContentId(View.NO_ID);
            }
            mNewTipView.setVisibility(View.GONE);
        } else if (mNewTipView.getVisibility() == View.VISIBLE) {
            if (mTipPosition == TIP_POSITION_LEFT) {
                mAfterTitleHolder.setContentId(mNewTipView.getId());
                mBeforeAccessoryHolder.setContentId(View.NO_ID);
            } else {
                mBeforeAccessoryHolder.setContentId(mNewTipView.getId());
                mAfterTitleHolder.setContentId(View.NO_ID);
            }
            mRedDot.setVisibility(View.GONE);
        }
        checkDetailLeftMargin();
    }

    public CharSequence getText() {
        return mTextView.getText();
    }

    public void setText(CharSequence text) {
        mTextView.setText(text);
        if (QMUILangHelper.isNullOrEmpty(text)) {
            mTextView.setVisibility(View.GONE);
        } else {
            mTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 切换是否显示小红点
     *
     * @param isShow 是否显示小红点
     */
    public void showRedDot(boolean isShow) {
        if(isShow){
            mTipShown = TIP_SHOW_RED_POINT;
        }else if(mTipShown == TIP_SHOW_RED_POINT){
            mTipShown = TIP_SHOW_NOTHING;
        }
        updateTipShown();
    }

    /**
     * 切换是否显示更新提示
     *
     * @param isShow 是否显示更新提示
     */
    public void showNewTip(boolean isShow) {
        if(isShow){
            mTipShown = TIP_SHOW_NEW;
        }else if(mTipShown == TIP_SHOW_NEW){
            mTipShown = TIP_SHOW_NOTHING;
        }
        updateTipShown();
    }

    private void updateTipShown(){
        if(mTipShown == TIP_SHOW_RED_POINT){
            if (mTipPosition == TIP_POSITION_LEFT) {
                mAfterTitleHolder.setContentId(mRedDot.getId());
                mBeforeAccessoryHolder.setContentId(View.NO_ID);
            } else {
                mBeforeAccessoryHolder.setContentId(mRedDot.getId());
                mAfterTitleHolder.setContentId(View.NO_ID);
            }
        }else if(mTipShown == TIP_SHOW_NEW){
            if (mTipPosition == TIP_POSITION_LEFT) {
                mAfterTitleHolder.setContentId(mNewTipView.getId());
                mBeforeAccessoryHolder.setContentId(View.NO_ID);
            } else {
                mBeforeAccessoryHolder.setContentId(mNewTipView.getId());
                mAfterTitleHolder.setContentId(View.NO_ID);
            }
        }else{
            mAfterTitleHolder.setContentId(View.NO_ID);
            mBeforeAccessoryHolder.setContentId(View.NO_ID);
        }
        mNewTipView.setVisibility(mTipShown == TIP_SHOW_NEW ? View.VISIBLE : View.GONE);
        mRedDot.setVisibility(mTipShown == TIP_SHOW_RED_POINT ? View.VISIBLE : View.GONE);
        checkDetailLeftMargin();
    }

    private void checkDetailLeftMargin() {
        LayoutParams detailLp = (LayoutParams) mDetailTextView.getLayoutParams();
        if (mOrientation == VERTICAL) {
            detailLp.leftMargin = 0;
        } else {
            if (mNewTipView.getVisibility() == View.GONE || mTipPosition == TIP_POSITION_LEFT) {
                detailLp.leftMargin = QMUIResHelper.getAttrDimen(
                        getContext(), R.attr.qmui_common_list_item_detail_h_margin_with_title);
            } else {
                detailLp.leftMargin = QMUIResHelper.getAttrDimen(
                        getContext(), R.attr.qmui_common_list_item_detail_h_margin_with_title_large);
            }
        }
    }

    public CharSequence getDetailText() {
        return mDetailTextView.getText();
    }


    public void setDetailText(CharSequence text) {
        mDetailTextView.setText(text);
        if (QMUILangHelper.isNullOrEmpty(text)) {
            mDetailTextView.setVisibility(View.GONE);
        } else {
            mDetailTextView.setVisibility(View.VISIBLE);
        }
    }

    public int getOrientation() {
        return mOrientation;
    }

    public void setOrientation(@QMUICommonListItemOrientation int orientation) {
        if (mOrientation == orientation) {
            return;
        }
        mOrientation = orientation;

        LayoutParams titleLp = (LayoutParams) mTextView.getLayoutParams();
        LayoutParams detailLp = (LayoutParams) mDetailTextView.getLayoutParams();
        if (orientation == VERTICAL) {
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    QMUIResHelper.getAttrDimen(getContext(), R.attr.qmui_common_list_item_title_v_text_size));
            mDetailTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    QMUIResHelper.getAttrDimen(getContext(), R.attr.qmui_common_list_item_detail_v_text_size));
            titleLp.horizontalChainStyle = LayoutParams.UNSET;
            titleLp.verticalChainStyle = LayoutParams.CHAIN_PACKED;
            titleLp.bottomToBottom = LayoutParams.UNSET;
            titleLp.bottomToTop = mDetailTextView.getId();

            detailLp.horizontalChainStyle = LayoutParams.UNSET;
            detailLp.verticalChainStyle = LayoutParams.CHAIN_PACKED;
            detailLp.leftToRight = LayoutParams.UNSET;
            detailLp.leftToLeft = mTextView.getId();
            detailLp.horizontalBias = 0f;
            detailLp.topToTop = LayoutParams.UNSET;
            detailLp.topToBottom = mTextView.getId();
            detailLp.leftMargin = 0;
            detailLp.topMargin = QMUIResHelper.getAttrDimen(
                    getContext(), R.attr.qmui_common_list_item_detail_v_margin_with_title);
        } else {
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    QMUIResHelper.getAttrDimen(getContext(), R.attr.qmui_common_list_item_title_h_text_size));
            mDetailTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    QMUIResHelper.getAttrDimen(getContext(), R.attr.qmui_common_list_item_detail_h_text_size));
            titleLp.horizontalChainStyle = LayoutParams.CHAIN_SPREAD_INSIDE;
            titleLp.verticalChainStyle = LayoutParams.UNSET;
            titleLp.bottomToBottom = LayoutParams.PARENT_ID;
            titleLp.bottomToTop = LayoutParams.UNSET;

            detailLp.horizontalChainStyle = LayoutParams.CHAIN_SPREAD_INSIDE;
            detailLp.verticalChainStyle = LayoutParams.UNSET;
            detailLp.leftToRight = mTextView.getId();
            detailLp.leftToLeft = LayoutParams.UNSET;
            detailLp.horizontalBias = 0f;
            detailLp.topToTop = LayoutParams.PARENT_ID;
            detailLp.topToBottom = LayoutParams.UNSET;
            detailLp.topMargin = 0;
            checkDetailLeftMargin();
        }
    }

    public int getAccessoryType() {
        return mAccessoryType;
    }

    /**
     * 设置右侧 View 的类型。
     *
     * @param type 见 {@link QMUICommonListItemAccessoryType}
     */
    public void setAccessoryType(@QMUICommonListItemAccessoryType int type) {
        mAccessoryView.removeAllViews();
        mAccessoryType = type;

        switch (type) {
            // 向右的箭头
            case ACCESSORY_TYPE_CHEVRON: {
                ImageView tempImageView = getAccessoryImageView();
                tempImageView.setImageDrawable(QMUIResHelper.getAttrDrawable(getContext(), R.attr.qmui_common_list_item_chevron));
                mAccessoryView.addView(tempImageView);
                mAccessoryView.setVisibility(VISIBLE);
            }
            break;
            // switch开关
            case ACCESSORY_TYPE_SWITCH: {
                if (mSwitch == null) {
                    mSwitch = new AppCompatCheckBox(getContext());
                    mSwitch.setBackground(null);
                    mSwitch.setButtonDrawable(QMUIResHelper.getAttrDrawable(getContext(), R.attr.qmui_common_list_item_switch));
                    mSwitch.setLayoutParams(getAccessoryLayoutParams());
                    if(mDisableSwitchSelf){
                        mSwitch.setClickable(false);
                        mSwitch.setEnabled(false);
                    }
                }
                mAccessoryView.addView(mSwitch);
                mAccessoryView.setVisibility(VISIBLE);
            }
            break;
            // 自定义View
            case ACCESSORY_TYPE_CUSTOM:
                mAccessoryView.setVisibility(VISIBLE);
                break;
            // 清空所有accessoryView
            case ACCESSORY_TYPE_NONE:
                mAccessoryView.setVisibility(GONE);
                break;
        }
        LayoutParams titleLp = (LayoutParams) mTextView.getLayoutParams();
        LayoutParams detailLp = (LayoutParams) mDetailTextView.getLayoutParams();
        if (mAccessoryView.getVisibility() != View.GONE) {
            detailLp.goneRightMargin = detailLp.rightMargin;
            titleLp.goneRightMargin = titleLp.rightMargin;
        } else {
            detailLp.goneRightMargin = 0;
            titleLp.goneRightMargin = 0;
        }
    }

    private ViewGroup.LayoutParams getAccessoryLayoutParams() {
        return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private ImageView getAccessoryImageView() {
        AppCompatImageView resultImageView = new AppCompatImageView(getContext());
        resultImageView.setLayoutParams(getAccessoryLayoutParams());
        resultImageView.setScaleType(ImageView.ScaleType.CENTER);
        QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
        builder.tintColor(R.attr.qmui_skin_support_common_list_chevron_color);
        QMUISkinHelper.setSkinValue(resultImageView, builder);
        QMUISkinValueBuilder.release(builder);
        return resultImageView;
    }

    public TextView getTextView() {
        return mTextView;
    }

    public TextView getDetailTextView() {
        return mDetailTextView;
    }

    public CheckBox getSwitch() {
        return mSwitch;
    }

    public ViewGroup getAccessoryContainerView() {
        return mAccessoryView;
    }

    /**
     * 添加自定义的 Accessory View
     *
     * @param view 自定义的 Accessory View
     */
    public void addAccessoryCustomView(View view) {
        if (mAccessoryType == ACCESSORY_TYPE_CUSTOM) {
            mAccessoryView.addView(view);
        }
    }

    public void setDisableSwitchSelf(boolean disableSwitchSelf) {
        mDisableSwitchSelf = disableSwitchSelf;
        if(mSwitch != null){
            mSwitch.setClickable(!disableSwitchSelf);
            mSwitch.setEnabled(!disableSwitchSelf);
        }
    }

    public void setSkinConfig(SkinConfig skinConfig) {
        QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
        if (skinConfig.iconTintColorRes != 0) {
            builder.tintColor(skinConfig.iconTintColorRes);
        }
        if (skinConfig.iconSrcRes != 0) {
            builder.src(skinConfig.iconSrcRes);
        }
        QMUISkinHelper.setSkinValue(mImageView, builder);

        builder.clear();
        if (skinConfig.titleTextColorRes != 0) {
            builder.textColor(skinConfig.titleTextColorRes);
        }
        QMUISkinHelper.setSkinValue(mTextView, builder);

        builder.clear();
        if (skinConfig.detailTextColorRes != 0) {
            builder.textColor(skinConfig.detailTextColorRes);
        }
        QMUISkinHelper.setSkinValue(mDetailTextView, builder);

        builder.clear();
        if (skinConfig.newTipSrcRes != 0) {
            builder.src(skinConfig.newTipSrcRes);
        }
        QMUISkinHelper.setSkinValue(mNewTipView, builder);

        builder.clear();
        if (skinConfig.tipDotColorRes != 0) {
            builder.bgTintColor(skinConfig.tipDotColorRes);
        }
        QMUISkinHelper.setSkinValue(mRedDot, builder);
        builder.release();
    }


    public interface LayoutParamConfig {
        ConstraintLayout.LayoutParams onConfig(ConstraintLayout.LayoutParams lp);
    }

    public static class SkinConfig {

        public int iconTintColorRes = R.attr.qmui_skin_support_common_list_icon_tint_color;
        public int iconSrcRes = 0;
        public int titleTextColorRes = R.attr.qmui_skin_support_common_list_title_color;
        public int detailTextColorRes = R.attr.qmui_skin_support_common_list_detail_color;
        public int newTipSrcRes = R.attr.qmui_skin_support_common_list_new_drawable;
        public int tipDotColorRes = R.attr.qmui_skin_support_common_list_red_point_tint_color;
    }
}
