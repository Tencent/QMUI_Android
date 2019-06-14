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

import com.google.auto.service.AutoService;
import com.qmuiteam.qmui.arch.annotation.MaybeFirstIn;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import static javax.lang.model.element.ElementKind.INTERFACE;

@AutoService(Processor.class)
public class ArchProcessor extends AbstractProcessor {
    private static final String QMUI_FRAGMENT_TYPE = "com.qmuiteam.qmui.arch.QMUIFragment";
    private static final String FinderSuffix = "_FragmentFinder";

    private static ClassName FirstFragmentFinderName = ClassName.get(
            "com.qmuiteam.qmui.arch.first", "FirstFragmentFinder");
    private static final ClassName QMUIFragmentName = ClassName.get(
            "com.qmuiteam.qmui.arch", "QMUIFragment");
    private static ClassName MapName = ClassName.get("java.util", "Map");
    private static ClassName HashMapName = ClassName.get("java.util", "HashMap");
    private static ClassName IntegerName = ClassName.get("java.lang", "Integer");
    private static ClassName OriginClassName = ClassName.get("java.lang", "Class");
    private static ParameterizedTypeName FragmQMUIFragmentClassName = ParameterizedTypeName.get(
            OriginClassName, WildcardTypeName.subtypeOf(QMUIFragmentName));
    private static TypeName ClassToIdMapName = ParameterizedTypeName.get(MapName,
            FragmQMUIFragmentClassName, IntegerName);
    private static TypeName IdToClassName = ParameterizedTypeName.get(MapName,
            IntegerName, FragmQMUIFragmentClassName);

    private Filer mFiler;
    private Elements mElementUtils;
    private Messager mMessager;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnv.getFiler();
        mElementUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        HashMap<TypeElement, List<TypeElement>> containerTypeElementMap = new HashMap<>();
        for (Element element : roundEnvironment.getElementsAnnotatedWith(MaybeFirstIn.class)) {
            if (element instanceof TypeElement) {
                TypeElement classElement = (TypeElement) element;
                TypeMirror elementType = classElement.asType();
                TypeElement[] containerElement = null;
                if (isSubtypeOfType(elementType, QMUI_FRAGMENT_TYPE)) {
                    MaybeFirstIn annotation = element.getAnnotation(MaybeFirstIn.class);
                    try {
                        annotation.container();
                    } catch (MirroredTypesException mte) {
                        List<? extends TypeMirror> containerMirrors = mte.getTypeMirrors();
                        containerElement = new TypeElement[containerMirrors.size()];
                        for (int i = 0; i < containerElement.length; i++) {
                            containerElement[i] = (TypeElement) ((DeclaredType) containerMirrors.get(i)).asElement();
                        }
                    }
                    if (containerElement == null) {
                        continue;
                    }

                    for (TypeElement container : containerElement) {
                        List<TypeElement> list = containerTypeElementMap.get(container);
                        if (list == null) {
                            list = new ArrayList<>();
                            containerTypeElementMap.put(container, list);
                        }
                        list.add(classElement);
                    }
                } else {
                    error(element, "Must annotated on subclasses of QMUIFragment");
                }
            }
        }
        for (Map.Entry<TypeElement, List<TypeElement>> entry : containerTypeElementMap.entrySet()) {
            TypeElement container = entry.getKey();
            List<TypeElement> fragments = entry.getValue();
            processCodeGeneration(container, fragments);
        }

        return true;
    }

    private void processCodeGeneration(TypeElement container, List<TypeElement> fragments) {
        TypeSpec.Builder finderClassBuilder = TypeSpec
                .classBuilder(container.getSimpleName() + FinderSuffix)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(FirstFragmentFinderName);

        FieldSpec classToIdMap = FieldSpec.builder(ClassToIdMapName, "mClassToIdMap")
                .addModifiers(Modifier.PRIVATE)
                .build();

        FieldSpec idToClassMap = FieldSpec.builder(IdToClassName, "mIdToClassMap")
                .addModifiers(Modifier.PRIVATE)
                .build();

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("mClassToIdMap = new $T<>()", HashMapName)
                .addStatement("mIdToClassMap = new $T<>()", HashMapName);

        int currentId = 100;
        for (TypeElement element : fragments) {
            ClassName elementName = ClassName.get(element);
            constructorBuilder.addStatement("mClassToIdMap.put($T.class, $L)",
                    elementName,
                    currentId);
            constructorBuilder.addStatement("mIdToClassMap.put($L, $T.class)",
                    currentId,
                    elementName);
            currentId++;
        }

        ExecutableElement iGetFragmentClassById = getOverrideMethod(
                FirstFragmentFinderName, "getFragmentClassById");
        MethodSpec.Builder getFragmentClassById = MethodSpec.overriding(iGetFragmentClassById)
                .addStatement("return mIdToClassMap.get($L)",
                        iGetFragmentClassById.getParameters().get(0).getSimpleName().toString());
        ExecutableElement iGetIdByFragmentClass = getOverrideMethod(
                FirstFragmentFinderName, "getIdByFragmentClass");
        MethodSpec.Builder getIdByFragmentClass = MethodSpec.overriding(iGetIdByFragmentClass)
                .addStatement("return mClassToIdMap.get($L)",
                        iGetIdByFragmentClass.getParameters().get(0).getSimpleName().toString());

        try {
            finderClassBuilder
                    .addField(classToIdMap)
                    .addField(idToClassMap)
                    .addMethod(constructorBuilder.build())
                    .addMethod(getFragmentClassById.build())
                    .addMethod(getIdByFragmentClass.build());
            JavaFile.builder(container.getQualifiedName().toString().replace("." + container.getSimpleName().toString(), ""), finderClassBuilder.build())
                    .build().writeTo(mFiler);
        } catch (IOException e) {
            error(container, "Unable to write finders for container %s: %s", container.getSimpleName(), e.getMessage());
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(MaybeFirstIn.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private ExecutableElement getOverrideMethod(ClassName creator, String methodName) {
        TypeElement element = mElementUtils.getTypeElement(creator.toString());
        List<? extends Element> elements = element.getEnclosedElements();
        for (Element ele : elements) {
            if (ele.getKind() != ElementKind.METHOD) continue;
            if (methodName.equals(ele.getSimpleName().toString())) {
                return (ExecutableElement) ele;
            }
        }
        throw new RuntimeException("method createHRouterRule of interface HRouterRuleCreator not found");
    }

    private void error(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.ERROR, element, message, args);
    }

    private void note(Element element, String message, Object... args) {
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

    private static boolean isTypeEqual(TypeMirror typeMirror, String otherType) {
        return otherType.equals(typeMirror.toString());
    }

    private boolean isInterface(TypeMirror typeMirror) {
        return typeMirror instanceof DeclaredType
                && ((DeclaredType) typeMirror).asElement().getKind() == INTERFACE;
    }

}
