package com.qmuiteam.qmui.lint;


import com.android.annotations.NonNull;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import java.util.Collections;
import java.util.List;

import lombok.ast.AstVisitor;
import lombok.ast.Expression;
import lombok.ast.MethodInvocation;
import lombok.ast.StrictListAccessor;
import lombok.ast.VariableReference;

public class FWordDetector extends Detector
        implements Detector.JavaScanner, Detector.ClassScanner {
    public static final Issue ISSUE_F_WORD =
            Issue.create("DontUseTheFWordInWRLog",
                    "Please, don't use the f word, type something more nicely.",
                    "Do I need to explain this? \uD83D\uDD95",
                    Category.MESSAGES, 5, Severity.ERROR,
                    new Implementation(FWordDetector.class, Scope.JAVA_FILE_SCOPE));

    @Override public List<String> getApplicableMethodNames() {
        return Collections.singletonList("log");
    }

    @Override public void visitMethod(@NonNull JavaContext context, AstVisitor visitor,
                                      @NonNull MethodInvocation node) {
        VariableReference ref = (VariableReference) node.astOperand();
        if (!"WRLog".equals(ref.astIdentifier().astValue())) {
            return;
        }

        StrictListAccessor<Expression, MethodInvocation> args = node.astArguments();
        if (args.isEmpty()) {
            return;
        }

        for (Expression expression : args) {
            String input = expression.toString();
            if (input != null && input.contains("fuck")) {
                context.report(
                        ISSUE_F_WORD, expression,
                        context.getLocation(expression), "\uD83D\uDD95");
            }
        }
    }
}
