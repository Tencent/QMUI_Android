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

package com.qmuiteam.qmui.lint;


import com.android.tools.lint.client.api.JavaEvaluator;
import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.google.common.collect.Lists;
import com.intellij.psi.PsiMethod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UExpression;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * 检测 QMUILog 中是否使用了 F Word。
 * Created by Kayo on 2017/9/19.
 */
public class QMUIFWordDetector extends Detector implements Detector.UastScanner {
    public static final Issue ISSUE_F_WORD =
            Issue.create("QMUIDontUseTheFWordInLog",
                    "Please, don't use the f word, type something more nicely.",
                    "Do I need to explain this? \uD83D\uDD95",
                    Category.CORRECTNESS, 5, Severity.WARNING,
                    new Implementation(QMUIFWordDetector.class, EnumSet.of(Scope.JAVA_FILE)));

    @Nullable
    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        return Lists.<Class<? extends UElement>>newArrayList(
                UCallExpression.class);
    }

    @Nullable
    @Override
    public UElementHandler createUastHandler(@NotNull JavaContext context) {
        return new FWordHandler(context);
    }

    static class FWordHandler extends UElementHandler {
        private static final List<String> checkMedthods = Arrays.asList("e", "w", "i", "d");
        private static final List<String> fWords = Arrays.asList("fuck", "bitch", "bullshit");

        private JavaContext mJavaContext;

        FWordHandler(@NotNull JavaContext context) {
            mJavaContext = context;
        }

        @Override
        public void visitCallExpression(@NotNull UCallExpression node) {
            JavaEvaluator evaluator = mJavaContext.getEvaluator();
            PsiMethod method = node.resolve();
            if (evaluator.isMemberInClass(method, "android.util.Log") ||
                    evaluator.isMemberInClass(method, "com.qmuiteam.qmui.QMUILog")) {
                String methodName = node.getMethodName();
                if (checkMedthods.contains(methodName)) {
                    List<UExpression> expressions = node.getValueArguments();
                    for (UExpression expression : expressions) {
                        String text = expression.asRenderString();
                        for(String fword: fWords){
                            int index = text.indexOf(fword);
                            if(index >= 0){
                                mJavaContext.report(
                                        ISSUE_F_WORD, expression,
                                        mJavaContext.getRangeLocation(expression, index, fword.length()), "\uD83D\uDD95");
                            }
                        }

                    }

                }
            }
        }
    }
}
