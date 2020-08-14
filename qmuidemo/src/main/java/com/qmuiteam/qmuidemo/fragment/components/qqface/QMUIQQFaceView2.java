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

package com.qmuiteam.qmuidemo.fragment.components.qqface;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.qmuiteam.qmui.type.LineLayout;
import com.qmuiteam.qmui.type.TypeEnvironment;
import com.qmuiteam.qmui.type.TypeModel;
import com.qmuiteam.qmui.type.parser.EmojiTextParser;
import com.qmuiteam.qmui.type.parser.TextParser;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmuidemo.QDQQFaceManager;

import java.util.ArrayList;
import java.util.List;

public class QMUIQQFaceView2 extends View {

    private TypeEnvironment mTypeEnvironment = new TypeEnvironment();
    private LineLayout mLineLayout = new LineLayout(mTypeEnvironment)
            .setCalculateWholeLines(true)
            .setEllipsize(TextUtils.TruncateAt.MIDDLE)
            .setMoreText("更多", Color.RED, Typeface.DEFAULT_BOLD)
            .setMaxLines(10);
    private TextParser mTextParser = new EmojiTextParser(QDQQFaceManager.getInstance());
    private List<TypeModel.EffectRemover> mEffectRemovers = new ArrayList<>();

    public QMUIQQFaceView2(Context context) {
        this(context, null);
    }

    public QMUIQQFaceView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mTypeEnvironment.setTextSize(QMUIDisplayHelper.dp2px(context, 16));
        mTypeEnvironment.setLineSpace(QMUIDisplayHelper.dp2px(context, 5));
        mTypeEnvironment.setParagraphSpace(QMUIDisplayHelper.dp2px(context, 15));
        mTypeEnvironment.setAlignment(TypeEnvironment.Alignment.JUSTIFY);
        TypeModel typeModel = mTextParser.parse("QMUI 换肤最原始是为了适配 Dark Mode。" +
                "但作为框架的实现者，就需要考虑到更通用的使用形式，并且要尽可能保证 API 的简" +
                "洁性。因而 QMUI 是支持多套肤色的切换，而 [微笑]Dark Mode 只是其中的一种。\n" +
                "在无需重启 Activity 的前提下，我们做[流泪]换肤框架的实现思路其实是很简单的：" +
                "就是当触发换肤时，遍历 View 树 来更新 View 的肤色相关的属性。基于这一" +
                "思路，组件的意义就在于利用数据结构、设[色]计模式、系统 API等来简化封装出一套" +
                "足够方便的使用接口，避免业务使用时为了完成功[大哭]能而堆砌一堆 if else 代码。" +
                "写组件也不是一个高大尚的事情，在业务开发过程中，我们应该尽可能多思考，构建一" +
                "些好用的组件，持续锻炼，才能逐渐 Hold 住越来越强大的组件。\n此外，在业务开发之" +
                "余，我们需要多读一些源码，如果你一直[大哭]走业务线，可能不会发[大哭]觉阅读源码的作用，而" +
                "如果你有尝试封装组件，那[大哭]么这些优秀的库往[大哭]往会给你思路的启迪。如果是两年前的我" +
                "来写换肤框架，我写出来的框[大哭]架可能比现在差得很远。如果你阅[大哭]读本文，也期望能给你以" +
                "启迪。");

        mLineLayout.setTypeModel(typeModel);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mEffectRemovers.isEmpty()){
                    for(TypeModel.EffectRemover remover: mEffectRemovers){
                        remover.remove();
                    }
                    mEffectRemovers.clear();
                    invalidate();
                }else{
                    addBgEffect();
                    addTextColorEffect();
                    addUnderLineEffect();
                }
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        mTypeEnvironment.setMeasureLimit(widthSize, heightSize);
        mLineLayout.measureAndLayout();
        setMeasuredDimension(widthSize, mLineLayout.getContentHeight());
    }

    public void addBgEffect(){
        TypeModel typeModel = mLineLayout.getTypeModel();
        if(typeModel != null){
            TypeModel.EffectRemover remover = typeModel.addBgEffect(10, 40, Color.YELLOW);
            mEffectRemovers.add(remover);
            invalidate();
        }
    }

    public void addTextColorEffect(){
        TypeModel typeModel = mLineLayout.getTypeModel();
        if(typeModel != null){
            TypeModel.EffectRemover remover = typeModel.addTextColorEffect(20, 50, Color.RED);
            mEffectRemovers.add(remover);
            invalidate();
        }
    }

    public void addUnderLineEffect(){
        TypeModel typeModel = mLineLayout.getTypeModel();
        if(typeModel != null){
            TypeModel.EffectRemover remover = typeModel.addUnderLineEffect(25, 60, Color.BLUE, QMUIDisplayHelper.dp2px(getContext(), 3));
            mEffectRemovers.add(remover);
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mLineLayout.draw(canvas);
    }
}
