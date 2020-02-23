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
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.QMUIWrapContentScrollView;
import com.qmuiteam.qmui.widget.textview.QMUISpanTouchFixTextView;

import androidx.annotation.Nullable;

/**
 * @author cginechen
 * @date 2015-12-12
 */
public class QMUIDialogBlockBuilder extends QMUIDialogBuilder<QMUIDialogBlockBuilder> {
    private CharSequence mContent;


    public QMUIDialogBlockBuilder(Context context) {
        super(context);
        setActionDivider(1, R.attr.qmui_skin_support_dialog_action_divider_color, 0, 0);
    }


    public QMUIDialogBlockBuilder setContent(CharSequence content) {
        mContent = content;
        return this;
    }

    public QMUIDialogBlockBuilder setContent(int contentRes) {
        mContent = getBaseContext().getResources().getString(contentRes);
        return this;
    }

    @Nullable
    @Override
    protected View onCreateTitle(QMUIDialog dialog, QMUIDialogView parent, Context context) {
        View result = super.onCreateTitle(dialog, parent, context);
        if(result != null && (mContent == null || mContent.length() == 0)){
            TypedArray a = context.obtainStyledAttributes(null,
                    R.styleable.QMUIDialogTitleTvCustomDef, R.attr.qmui_dialog_title_style, 0);
            int count = a.getIndexCount();
            for (int i = 0; i < count; i++) {
                int attr = a.getIndex(i);
                if (attr == R.styleable.QMUIDialogTitleTvCustomDef_qmui_paddingBottomWhenNotContent) {
                    result.setPadding(
                            result.getPaddingLeft(),
                            result.getPaddingTop(),
                            result.getPaddingRight(),
                            a.getDimensionPixelSize(attr, result.getPaddingBottom())
                    );
                }
            }
            a.recycle();
        }
        return result;
    }

    @Override
    @Nullable
    protected View onCreateContent(QMUIDialog dialog, QMUIDialogView parent, Context context) {
        if(mContent != null && mContent.length() > 0){
            TextView contentTv = new QMUISpanTouchFixTextView(context);
            QMUIResHelper.assignTextViewWithAttr(contentTv, R.attr.qmui_dialog_message_content_style);

            if (!hasTitle()) {
                TypedArray a = context.obtainStyledAttributes(null,
                        R.styleable.QMUIDialogMessageTvCustomDef,
                        R.attr.qmui_dialog_message_content_style, 0);
                int count = a.getIndexCount();
                for (int i = 0; i < count; i++) {
                    int attr = a.getIndex(i);
                    if (attr == R.styleable.QMUIDialogMessageTvCustomDef_qmui_paddingTopWhenNotTitle) {
                        contentTv.setPadding(
                                contentTv.getPaddingLeft(),
                                a.getDimensionPixelSize(attr, contentTv.getPaddingTop()),
                                contentTv.getPaddingRight(),
                                contentTv.getPaddingBottom()
                        );
                    }
                }
                a.recycle();
            }
            contentTv.setText(mContent);
            return wrapWithScroll(contentTv);
        }
        return null;
    }

    @Override
    public QMUIDialog create(int style) {
        setActionContainerOrientation(VERTICAL);
        return super.create(style);
    }
}
