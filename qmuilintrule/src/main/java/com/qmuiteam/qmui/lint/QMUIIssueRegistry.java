package com.qmuiteam.qmui.lint;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Issue;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public final class QMUIIssueRegistry extends IssueRegistry {
    @Override public List<Issue> getIssues() {
        return Arrays.asList(
                QMUIFWordDetector.ISSUE_F_WORD,
                QMUIJavaVectorDrawableDetector.ISSUE_JAVA_VECTOR_DRAWABLE,
                QMUIXmlVectorDrawableDetector.ISSUE_XML_VECTOR_DRAWABLE,
                QMUIImageSizeDetector.ISSUE_IMAGE_SIZE,
                QMUIImageScaleDetector.ISSUE_IMAGE_SCALE
        );
    }
}
