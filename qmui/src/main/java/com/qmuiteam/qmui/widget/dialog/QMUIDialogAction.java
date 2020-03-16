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
import android.content.res.TypedArray;
import android.util.TypedValue;
import android.view.View;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.layout.QMUIButton;
import com.qmuiteam.qmui.skin.QMUISkinHelper;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.util.QMUISpanHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * @author cginechen
 * @date 2015-10-20
 */
public class QMUIDialogAction {

    @IntDef({ACTION_PROP_NEGATIVE, ACTION_PROP_NEUTRAL, ACTION_PROP_POSITIVE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Prop {
    }

    //用于标记positive/negative/neutral
    public static final int ACTION_PROP_POSITIVE = 0;
    public static final int ACTION_PROP_NEUTRAL = 1;
    public static final int ACTION_PROP_NEGATIVE = 2;


    private CharSequence mStr;
    private int mIconRes = 0;
    private int mActionProp = ACTION_PROP_NEUTRAL;
    private int mSkinTextColorAttr = 0;
    private int mSkinBackgroundAttr = 0;
    private int mSkinIconTintColorAttr = 0;
    private int mSkinSeparatorColorAttr = R.attr.qmui_skin_support_dialog_action_divider_color;
    private ActionListener mOnClickListener;
    private QMUIButton mButton;
    private boolean mIsEnabled = true;


    public QMUIDialogAction(Context context, int strRes) {
        this(context.getResources().getString(strRes));
    }

    public QMUIDialogAction(CharSequence str) {
        this(str, null);
    }

    public QMUIDialogAction(Context context, int strRes, @Nullable ActionListener onClickListener) {
        this(context.getResources().getString(strRes), onClickListener);
    }

    public QMUIDialogAction(CharSequence str, @Nullable ActionListener onClickListener) {
        mStr = str;
        mOnClickListener = onClickListener;
    }

    public QMUIDialogAction prop(@Prop int actionProp) {
        mActionProp = actionProp;
        return this;
    }

    public QMUIDialogAction iconRes(@Prop int iconRes) {
        mIconRes = iconRes;
        return this;
    }

    public QMUIDialogAction onClick(ActionListener onClickListener) {
        mOnClickListener = onClickListener;
        return this;
    }

    public QMUIDialogAction skinTextColorAttr(int skinTextColorAttr) {
        mSkinTextColorAttr = skinTextColorAttr;
        return this;
    }

    public QMUIDialogAction skinBackgroundAttr(int skinBackgroundAttr) {
        mSkinBackgroundAttr = skinBackgroundAttr;
        return this;
    }

    public QMUIDialogAction skinIconTintColorAttr(int skinIconTintColorAttr) {
        mSkinIconTintColorAttr = skinIconTintColorAttr;
        return this;
    }

    /**
     * inner usage
     * @param skinSeparatorColorAttr
     * @return
     */
    QMUIDialogAction skinSeparatorColorAttr(int skinSeparatorColorAttr){
        mSkinSeparatorColorAttr = skinSeparatorColorAttr;
        return this;
    }

    public QMUIDialogAction setEnabled(boolean enabled) {
        mIsEnabled = enabled;
        if (mButton != null) {
            mButton.setEnabled(enabled);
        }
        return this;
    }

    public QMUIButton buildActionView(final QMUIDialog dialog, final int index) {
        mButton = generateActionButton(dialog.getContext(), mStr, mIconRes,
                mSkinBackgroundAttr, mSkinTextColorAttr, mSkinIconTintColorAttr);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null && mButton.isEnabled()) {
                    mOnClickListener.onClick(dialog, index);
                }
            }
        });
        return mButton;
    }

    /**
     * 生成适用于对话框的按钮
     */
    private QMUIButton generateActionButton(Context context, CharSequence text, int iconRes,
                                            int skinBackgroundAttr, int skinTextColorAttr, int iconTintColor) {
        QMUIButton button = new QMUIButton(context);
        button.setBackground(null);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setChangeAlphaWhenDisable(true);
        button.setChangeAlphaWhenPress(true);
        TypedArray a = context.obtainStyledAttributes(
                null, R.styleable.QMUIDialogActionStyleDef, R.attr.qmui_dialog_action_style, 0);
        int count = a.getIndexCount();
        int paddingHor = 0, iconSpace = 0;
        ColorStateList negativeTextColor = null, positiveTextColor = null;
        for (int i = 0; i < count; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.QMUIDialogActionStyleDef_android_gravity) {
                button.setGravity(a.getInt(attr, -1));
            } else if (attr == R.styleable.QMUIDialogActionStyleDef_android_textColor) {
                button.setTextColor(a.getColorStateList(attr));
            } else if (attr == R.styleable.QMUIDialogActionStyleDef_android_textSize) {
                button.setTextSize(TypedValue.COMPLEX_UNIT_PX, a.getDimensionPixelSize(attr, 0));
            } else if (attr == R.styleable.QMUIDialogActionStyleDef_qmui_dialog_action_button_padding_horizontal) {
                paddingHor = a.getDimensionPixelSize(attr, 0);
            } else if (attr == R.styleable.QMUIDialogActionStyleDef_android_background) {
                button.setBackground(a.getDrawable(attr));
            } else if (attr == R.styleable.QMUIDialogActionStyleDef_android_minWidth) {
                int miniWidth = a.getDimensionPixelSize(attr, 0);
                button.setMinWidth(miniWidth);
                button.setMinimumWidth(miniWidth);
            } else if (attr == R.styleable.QMUIDialogActionStyleDef_qmui_dialog_positive_action_text_color) {
                positiveTextColor = a.getColorStateList(attr);
            } else if (attr == R.styleable.QMUIDialogActionStyleDef_qmui_dialog_negative_action_text_color) {
                negativeTextColor = a.getColorStateList(attr);
            } else if (attr == R.styleable.QMUIDialogActionStyleDef_qmui_dialog_action_icon_space) {
                iconSpace = a.getDimensionPixelSize(attr, 0);
            } else if (attr == R.styleable.QMUITextCommonStyleDef_android_textStyle) {
                int styleIndex = a.getInt(attr, -1);
                button.setTypeface(null, styleIndex);
            }
        }

        a.recycle();
        button.setPadding(paddingHor, 0, paddingHor, 0);
        if (iconRes <= 0) {
            button.setText(text);
        } else {
            button.setText(QMUISpanHelper.generateSideIconText(
                    true, iconSpace, text, ContextCompat.getDrawable(context, iconRes), iconTintColor, button));
        }


        button.setClickable(true);
        button.setEnabled(mIsEnabled);

        if (mActionProp == ACTION_PROP_NEGATIVE) {
            button.setTextColor(negativeTextColor);
            if (skinTextColorAttr == 0) {
                skinTextColorAttr = R.attr.qmui_skin_support_dialog_negative_action_text_color;
            }
        } else if (mActionProp == ACTION_PROP_POSITIVE) {
            button.setTextColor(positiveTextColor);
            if (skinTextColorAttr == 0) {
                skinTextColorAttr = R.attr.qmui_skin_support_dialog_positive_action_text_color;
            }
        } else {
            if (skinTextColorAttr == 0) {
                skinTextColorAttr = R.attr.qmui_skin_support_dialog_action_text_color;
            }
        }
        QMUISkinValueBuilder skinValueBuilder = QMUISkinValueBuilder.acquire();
        skinBackgroundAttr = skinBackgroundAttr == 0 ? R.attr.qmui_skin_support_dialog_action_bg : skinBackgroundAttr;
        skinValueBuilder.background(skinBackgroundAttr);
        skinValueBuilder.textColor(skinTextColorAttr);
        if(mSkinSeparatorColorAttr != 0){
            skinValueBuilder.topSeparator(mSkinSeparatorColorAttr);
            skinValueBuilder.leftSeparator(mSkinSeparatorColorAttr);
        }
        QMUISkinHelper.setSkinValue(button, skinValueBuilder);
        skinValueBuilder.release();
        return button;
    }

    public int getActionProp() {
        return mActionProp;
    }

    public interface ActionListener {
        void onClick(QMUIDialog dialog, int index);
    }
}
