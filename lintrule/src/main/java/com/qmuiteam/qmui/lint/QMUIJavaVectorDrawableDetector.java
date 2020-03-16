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

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Project;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiExpressionList;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

/**
 * 检测是否在 getDrawable 方法中传入了 Vector Drawable，在 4.0 及以下版本的系统中会导致 Crash
 * Created by Kayo on 2017/8/24.
 */

public class QMUIJavaVectorDrawableDetector extends Detector implements Detector.UastScanner {

    public static final Issue ISSUE_JAVA_VECTOR_DRAWABLE =
            Issue.create("QMUIGetVectorDrawableWithWrongFunction",
                    "Should use the corresponding method to get vector drawable.",
                    "Using the normal method to get the vector drawable will cause a crash on Android versions below 4.0",
                    Category.CORRECTNESS, 8, Severity.ERROR,
                    new Implementation(QMUIJavaVectorDrawableDetector.class, Scope.JAVA_FILE_SCOPE));

    @Override
    public List<String> getApplicableMethodNames() {
        return Collections.singletonList("getDrawable");
    }

    @Override
    public void visitMethod(@NotNull JavaContext context,
                            @Nullable JavaElementVisitor visitor,
                            @NotNull PsiMethodCallExpression call,
                            @NotNull PsiMethod method) {
        super.visitMethod(context, visitor, call, method);
        PsiExpressionList args = call.getArgumentList();
        if (args.getExpressions().length == 0) {
            return;
        }

        Project project = context.getProject();
        List<File> resourceFolder = project.getResourceFolders();
        if (resourceFolder.isEmpty()) {
            return;
        }

        String resourcePath = resourceFolder.get(0).getAbsolutePath();
        for (PsiExpression expression : args.getExpressions()) {
            String input = expression.toString();
            if (input != null && input.contains("R.drawable")) {
                // 找出 drawable 相关的参数

                // 获取 drawable 名字
                String drawableName = input.replace("R.drawable.", "");
                try {
                    // 若 drawable 为 Vector Drawable，则文件后缀为 xml，根据 resource 路径，drawable 名字，文件后缀拼接出完整路径
                    FileInputStream fileInputStream = new FileInputStream(resourcePath + "/drawable/" + drawableName + ".xml");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
                    String line = reader.readLine();
                    if (line.contains("vector")) {
                        // 若文件存在，并且包含首行包含 vector，则为 Vector Drawable，抛出警告
                        context.report(ISSUE_JAVA_VECTOR_DRAWABLE, method, context.getLocation(method), expression.toString() + " 为 Vector Drawable，请使用 getVectorDrawable 方法获取，避免 4.0 及以下版本的系统产生 Crash");
                    }
                    reader.close();
                    fileInputStream.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
