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
import com.qmuiteam.qmui.arch.annotation.FirstFragments;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;

@AutoService(Processor.class)
public class FirstFragmentProcessor extends BaseProcessor {

    private static final String FinderSuffix = "_FragmentFinder";

    private static ClassName FirstFragmentFinderName = ClassName.get(
            "com.qmuiteam.qmui.arch.first", "FirstFragmentFinder");
    private static TypeName ClassToIdMapName = ParameterizedTypeName.get(MapName,
            QMUIFragmentClassName, IntegerName);
    private static TypeName IdToClassName = ParameterizedTypeName.get(MapName,
            IntegerName, QMUIFragmentClassName);


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(FirstFragments.class)) {
            if (element instanceof TypeElement) {
                TypeElement classElement = (TypeElement) element;
                TypeMirror elementType = classElement.asType();
                TypeElement[] fragments = null;
                if (isSubtypeOfType(elementType, QMUI_FRAGMENT_ACTIVITY_TYPE)) {
                    FirstFragments annotation = element.getAnnotation(FirstFragments.class);
                    try {
                        annotation.value();
                    } catch (MirroredTypesException mte) {
                        List<? extends TypeMirror> containerMirrors = mte.getTypeMirrors();
                        fragments = new TypeElement[containerMirrors.size()];
                        for (int i = 0; i < fragments.length; i++) {
                            fragments[i] = (TypeElement) ((DeclaredType) containerMirrors.get(i)).asElement();
                        }
                    }
                    if (fragments == null) {
                        continue;
                    }

                    processCodeGeneration(classElement, fragments);
                } else {
                    error(element, "Must annotated on subclasses of QMUIFragmentActivity");
                }
            }
        }
        return true;
    }


    private void processCodeGeneration(TypeElement container, TypeElement[] fragments) {
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
                .addStatement("Integer id = mClassToIdMap.get($L)", iGetIdByFragmentClass.getParameters().get(0).getSimpleName().toString())
                .addStatement("return id != null ? id :$T.NO_ID", FirstFragmentFinderName);

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
        types.add(FirstFragments.class.getCanonicalName());
        return types;
    }
}
