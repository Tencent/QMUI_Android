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
import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

@AutoService(Processor.class)
public class LatestVisitProcessor extends BaseProcessor {

    private static ClassName RecordIdClassMap = ClassName.get(
            "com.qmuiteam.qmui.arch.record", "RecordIdClassMap");

    private static TypeName MapByClassName = ParameterizedTypeName.get(MapName,
            OriginClassName, IntegerName);
    private static TypeName MapByIdName = ParameterizedTypeName.get(MapName,
            IntegerName, OriginClassName);

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(LatestVisitRecord.class);
        if (elements.isEmpty()) {
            return true;
        }
        TypeSpec.Builder classBuilder = TypeSpec
                .classBuilder(RecordIdClassMap.simpleName() + "Impl")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(RecordIdClassMap);
        classBuilder.addField(FieldSpec.builder(MapByClassName, "mClassToIdMap")
                .addModifiers(Modifier.PRIVATE)
                .build());

        classBuilder.addField(FieldSpec.builder(MapByIdName, "mIdToClassMap")
                .addModifiers(Modifier.PRIVATE)
                .build());

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("mClassToIdMap = new $T<>()", HashMapName)
                .addStatement("mIdToClassMap = new $T<>()", HashMapName);


        HashMap<Integer, String> hashCodes = new HashMap<>();
        for (Element element : elements) {
            if (element instanceof TypeElement) {
                TypeElement classElement = (TypeElement) element;
                TypeMirror elementType = classElement.asType();
                boolean isFragmentActivity = isSubtypeOfType(elementType, QMUI_FRAGMENT_ACTIVITY_TYPE);
                boolean isFragment = isSubtypeOfType(elementType, QMUI_FRAGMENT_TYPE);
                boolean isActivity = isSubtypeOfType(elementType, QMUI_ACTIVITY_TYPE);
                if (isFragmentActivity || isFragment || isActivity) {
                    ClassName elementName = ClassName.get(classElement);
                    String simpleName = elementName.simpleName();
                    int hashCode = simpleName.hashCode();
                    if(hashCodes.keySet().contains(hashCode)){
                        if(hashCodes.keySet().contains(hashCode)){
                            error(element, "The hashCode of " + simpleName + " conflict with "
                                    + hashCodes.get(hashCode) + "; Please consider changing the class name");
                            continue;
                        }
                    }
                    hashCodes.put(hashCode, simpleName);

                    constructorBuilder.addStatement("mClassToIdMap.put($T.class, $L)",
                            elementName,
                            hashCode);
                    constructorBuilder.addStatement("mIdToClassMap.put($L, $T.class)",
                            hashCode,
                            elementName);
                } else {
                    error(element, "Must annotated on subclasses of QMUIFragmentActivity");
                }
            }
        }

        ExecutableElement iGetClassById = getOverrideMethod(
                RecordIdClassMap, "getRecordClassById");
        MethodSpec.Builder getRecordMetaById = MethodSpec.overriding(iGetClassById)
                .addStatement("return mIdToClassMap.get($L)",
                        iGetClassById.getParameters().get(0).getSimpleName().toString());
        ExecutableElement iGetIdByClass = getOverrideMethod(
                RecordIdClassMap, "getIdByRecordClass");
        MethodSpec.Builder getRecordMetaByClass = MethodSpec.overriding(iGetIdByClass)
                .addStatement("return mClassToIdMap.get($L)",
                        iGetIdByClass.getParameters().get(0).getSimpleName().toString());

        classBuilder
                .addMethod(constructorBuilder.build())
                .addMethod(getRecordMetaById.build())
                .addMethod(getRecordMetaByClass.build());
        try {
            JavaFile.builder(RecordIdClassMap.packageName(), classBuilder.build())
                    .build().writeTo(mFiler);
        } catch (IOException e) {
            error(null, "Unable to generate RecordMetaMapImpl: %s", e.getMessage());
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(LatestVisitRecord.class.getCanonicalName());
        return types;
    }
}
