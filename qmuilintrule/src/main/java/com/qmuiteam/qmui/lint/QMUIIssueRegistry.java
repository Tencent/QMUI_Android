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
