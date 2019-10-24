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

package com.qmuiteam.qmui.skin;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import com.qmuiteam.qmui.layout.QMUIButton;
import com.qmuiteam.qmui.qqface.QMUIQQFaceView;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.popup.QMUIFullScreenPopup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

class SkinAttrChooseMakerPopup extends QMUIFullScreenPopup {
    private final QMUISkinMaker.ValueWriter mValueWriter;
    private QMUIButton mAddNewAttrBtn;
    private RecyclerView mRecyclerView;
    private List<String> mAttrs;

    public SkinAttrChooseMakerPopup(Context context, List<String> attrs, QMUISkinMaker.ValueWriter valueWriter) {
        super(context);
        mValueWriter = valueWriter;
        mAttrs = attrs;
        closeBtn(true);

        int btnHeight = QMUIDisplayHelper.dp2px(context, 54);
        mAddNewAttrBtn = new QMUIButton(context);
        mAddNewAttrBtn.setText(R.string.app_new_attr);
        mAddNewAttrBtn.setId(View.generateViewId());
        mAddNewAttrBtn.setRadius(btnHeight / 2);
        mAddNewAttrBtn.setBackgroundColor(Color.WHITE);
        mAddNewAttrBtn.setChangeAlphaWhenPress(true);

        int marginHor = QMUIDisplayHelper.dp2px(context, 24);
        ConstraintLayout.LayoutParams newAttrLp = new ConstraintLayout.LayoutParams(0, btnHeight);
        newAttrLp.leftMargin = marginHor;
        newAttrLp.rightMargin = marginHor;
        newAttrLp.topMargin = QMUIDisplayHelper.dp2px(context, 60);
        newAttrLp.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        newAttrLp.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        newAttrLp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        addView(mAddNewAttrBtn, newAttrLp);

        mRecyclerView = new RecyclerView(context);
        mRecyclerView.setId(View.generateViewId());
        mRecyclerView.setBackgroundColor(Color.WHITE);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setAdapter(new Adapter());

        ConstraintLayout.LayoutParams recyclerViewLp = new ConstraintLayout.LayoutParams(
                0, 0);
        recyclerViewLp.leftMargin = marginHor;
        recyclerViewLp.rightMargin = marginHor;
        recyclerViewLp.topMargin = QMUIDisplayHelper.dp2px(context, 20);
        recyclerViewLp.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        recyclerViewLp.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        recyclerViewLp.topToBottom = mAddNewAttrBtn.getId();
        recyclerViewLp.bottomMargin = QMUIDisplayHelper.dp2px(context, 20);
        recyclerViewLp.bottomToTop = getCloseBtnId();
        addView(mRecyclerView, recyclerViewLp);
    }

    class VH extends RecyclerView.ViewHolder {
        private QMUIQQFaceView mQMUIQQFaceView;
        private String mAttrName;

        public VH(@NonNull QMUIQQFaceView itemView) {
            super(itemView);
            mQMUIQQFaceView = itemView;
            mQMUIQQFaceView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAttrName != null) {
                        mValueWriter.write(mAttrName);
                        dismiss();
                    }
                }
            });
        }

        public void bind(@NonNull String attr) {
            mAttrName = attr;
            mQMUIQQFaceView.setText(mAttrName);
        }
    }

    class Adapter extends RecyclerView.Adapter<VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            QMUIQQFaceView qmuiqqFaceView = new QMUIQQFaceView(parent.getContext());
            qmuiqqFaceView.setTextSize(QMUIDisplayHelper.sp2px(parent.getContext(), 15));
            qmuiqqFaceView.setTextColor(Color.BLACK);
            int paddingHor = QMUIDisplayHelper.dp2px(parent.getContext(), 20);
            int paddingVer = QMUIDisplayHelper.dp2px(parent.getContext(), 12);
            qmuiqqFaceView.setBackground(QMUIResHelper.getAttrDrawable(
                    parent.getContext(), R.attr.qmui_skin_support_s_list_item_bg_1));
            qmuiqqFaceView.setPadding(paddingHor, paddingVer, paddingHor, paddingVer);
            return new VH(qmuiqqFaceView);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.bind(mAttrs.get(position));
        }

        @Override
        public int getItemCount() {
            return mAttrs.size();
        }
    }
}
