package com.qmuiteam.qmui.lint;

import com.android.annotations.NonNull;
import com.android.resources.ResourceFolderType;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Project;
import com.android.tools.lint.detector.api.ResourceXmlDetector;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.XmlContext;
import com.google.common.collect.Lists;

import org.w3c.dom.Attr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;

import static com.android.SdkConstants.ATTR_DRAWABLE_BOTTOM;
import static com.android.SdkConstants.ATTR_DRAWABLE_LEFT;
import static com.android.SdkConstants.ATTR_DRAWABLE_RIGHT;
import static com.android.SdkConstants.ATTR_DRAWABLE_TOP;

/**
 * 检测是否在 drawableLeft / drawableRight / drawableTop / drawableBottom 中传入了 Vector Drawable，在 4.0 及以下版本的系统中会导致 Crash
 * Created by Kayo on 2017/8/29.
 */

public class QMUIXmlVectorDrawableDetector extends ResourceXmlDetector {

    public static final Issue ISSUE_XML_VECTOR_DRAWABLE =
            Issue.create("QMUIGetVectorDrawableWithWrongProperty",
                    "Should use the corresponding property to get vector drawable.",
                    "Using the normal property to get the vector drawable will cause a crash on Android versions below 4.0.",
                    Category.CORRECTNESS, 8, Severity.ERROR,
                    new Implementation(QMUIXmlVectorDrawableDetector.class, Scope.RESOURCE_FILE_SCOPE));

    private static final Collection<String> mAttrList = Lists.newArrayList(ATTR_DRAWABLE_LEFT, ATTR_DRAWABLE_RIGHT, ATTR_DRAWABLE_TOP, ATTR_DRAWABLE_BOTTOM);

    @Override
    public boolean appliesTo(ResourceFolderType folderType) {
        return ResourceFolderType.LAYOUT == folderType;
    }

    @Override
    public Collection<String> getApplicableElements() {
        return ALL;
    }

    @Override
    public Collection<String> getApplicableAttributes() {
        return mAttrList;
    }

    @Override
    public void visitAttribute(@NonNull XmlContext context, @NonNull Attr attribute) {
        // 判断资源文件夹是否存在
        Project project = context.getProject();
        List<File> resourceFolder = project.getResourceFolders();
        if (resourceFolder.isEmpty()) {
            return;
        }

        // 获取项目资源文件夹路径
        String resourcePath = resourceFolder.get(0).getAbsolutePath();
        // 获取 drawable 名字
        String drawableName = attribute.getValue().replace("@drawable/", "");
        try {
            // 若 drawable 为 Vector Drawable，则文件后缀为 xml，根据 resource 路径，drawable 名字，文件后缀拼接出完整路径
            FileInputStream fileInputStream = new FileInputStream(resourcePath + "/drawable/" + drawableName + ".xml");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line = reader.readLine();
            if (line.contains("vector")) {
                // 若文件存在，并且包含首行包含 vector，则为 Vector Drawable，抛出警告
                context.report(ISSUE_XML_VECTOR_DRAWABLE, attribute, context.getLocation(attribute), attribute.getValue() + " 为 Vector Drawable，请使用 Vector 属性进行设置，避免 4.0 及以下版本的系统产生 Crash");
            }
            fileInputStream.close();
        } catch (Exception ignored) {
        }
    }
}
