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
package com.qmuiteam.qmui.arch;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.WildcardTypeName;

import java.util.List;
import java.util.Map;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import static javax.lang.model.element.ElementKind.INTERFACE;

public abstract class BaseProcessor extends AbstractProcessor {

    static final String ACTIVITY_TYPE = "android.app.Activity";
    static final String FRAGMENT_ACTIVITY_TYPE = "androidx.fragment.app.FragmentActivity";
    static final String FRAGMENT_TYPE = "androidx.fragment.app.Fragment";
    static final String QMUI_FRAGMENT_ACTIVITY_TYPE = "com.qmuiteam.qmui.arch.QMUIFragmentActivity";
    static final String QMUI_FRAGMENT_TYPE = "com.qmuiteam.qmui.arch.QMUIFragment";
    static final String QMUI_ACTIVITY_TYPE = "com.qmuiteam.qmui.arch.QMUIActivity";


    static final ClassName QMUIFragmentActivityName = ClassName.get(
            "com.qmuiteam.qmui.arch", "QMUIFragmentActivity");
    static final ClassName QMUIFragmentName = ClassName.get(
            "com.qmuiteam.qmui.arch", "QMUIFragment");
    static ClassName MapName = ClassName.get("java.util", "Map");
    static ClassName ListName = ClassName.get("java.util", "List");
    static ClassName ArrayMapName = ClassName.get("android.util", "ArrayMap");
    static ClassName ArrayListName = ClassName.get("java.util", "ArrayList");
    static ClassName HashMapName = ClassName.get("java.util", "HashMap");
    static ClassName IntegerName = ClassName.get("java.lang", "Integer");
    static ClassName StringName = ClassName.get("java.lang", "String");
    static ClassName OriginClassName = ClassName.get("java.lang", "Class");
    static ParameterizedTypeName QMUIFragmentClassName = ParameterizedTypeName.get(
            OriginClassName, WildcardTypeName.subtypeOf(QMUIFragmentName));

    protected Filer mFiler;
    protected Elements mElementUtils;
    protected Messager mMessager;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnv.getFiler();
        mElementUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    protected ExecutableElement getOverrideMethod(ClassName creator, String methodName) {
        TypeElement element = mElementUtils.getTypeElement(creator.toString());
        List<? extends Element> elements = element.getEnclosedElements();
        for (Element ele : elements) {
            if (ele.getKind() != ElementKind.METHOD) continue;
            if (methodName.equals(ele.getSimpleName().toString())) {
                return (ExecutableElement) ele;
            }
        }
        throw new RuntimeException(String.format("method %s of interface FirstFragmentFinder not found", methodName));
    }


    public void error(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.ERROR, element, message, args);
    }

    public void waring(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.WARNING, element, message, args);
    }

    public void note(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.NOTE, element, message, args);
    }

    private void printMessage(Diagnostic.Kind kind, Element element, String message, Object[] args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }

        mMessager.printMessage(kind, message, element);
    }

    static boolean isSubtypeOfType(TypeMirror typeMirror, String otherType) {
        if (isTypeEqual(typeMirror, otherType)) {
            return true;
        }
        if (typeMirror.getKind() != TypeKind.DECLARED) {
            return false;
        }
        DeclaredType declaredType = (DeclaredType) typeMirror;
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (typeArguments.size() > 0) {
            StringBuilder typeString = new StringBuilder(declaredType.asElement().toString());
            typeString.append('<');
            for (int i = 0; i < typeArguments.size(); i++) {
                if (i > 0) {
                    typeString.append(',');
                }
                typeString.append('?');
            }
            typeString.append('>');
            if (typeString.toString().equals(otherType)) {
                return true;
            }
        }
        Element element = declaredType.asElement();
        if (!(element instanceof TypeElement)) {
            return false;
        }
        TypeElement typeElement = (TypeElement) element;
        TypeMirror superType = typeElement.getSuperclass();
        if (isSubtypeOfType(superType, otherType)) {
            return true;
        }
        for (TypeMirror interfaceType : typeElement.getInterfaces()) {
            if (isSubtypeOfType(interfaceType, otherType)) {
                return true;
            }
        }
        return false;
    }

    static boolean isTypeEqual(TypeMirror typeMirror, String otherType) {
        return otherType.equals(typeMirror.toString());
    }

    static boolean isInterface(TypeMirror typeMirror) {
        return typeMirror instanceof DeclaredType
                && ((DeclaredType) typeMirror).asElement().getKind() == INTERFACE;
    }

    static AnnotationMirror getAnnotationMirror(Element element, Class<?> annotation) {
        List<? extends AnnotationMirror> list = element.getAnnotationMirrors();
        if (list == null || list.isEmpty()) {
            return null;
        }
        for (AnnotationMirror item : list) {
            if (item.getAnnotationType().toString().equals(annotation.getName())) {
                return item;
            }
        }
        return null;
    }

    static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> map = annotationMirror.getElementValues();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> item : map.entrySet()) {
            if (item.getKey().getSimpleName().toString().equals(key)) {
                return item.getValue();
            }
        }
        return null;
    }
}
