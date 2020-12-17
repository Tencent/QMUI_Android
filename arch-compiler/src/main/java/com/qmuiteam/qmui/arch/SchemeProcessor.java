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
import com.qmuiteam.qmui.arch.annotation.ActivityScheme;
import com.qmuiteam.qmui.arch.annotation.FragmentScheme;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;

@AutoService(Processor.class)
public class SchemeProcessor extends BaseProcessor {
    private static String QMUISchemeIntentFactoryType = "com.qmuiteam.qmui.arch.scheme.QMUISchemeIntentFactory";
    private static String QMUISchemeFragmentFactoryType = "com.qmuiteam.qmui.arch.scheme.QMUISchemeFragmentFactory";
    private static String QMUISchemeMatcherType = "com.qmuiteam.qmui.arch.scheme.QMUISchemeMatcher";
    private static String QMUISchemeValueConverterType = "com.qmuiteam.qmui.arch.scheme.QMUISchemeValueConverter";

    private static ClassName SchemeMap = ClassName.get(
            "com.qmuiteam.qmui.arch.scheme", "SchemeMap");
    private static ClassName SchemeItem = ClassName.get(
            "com.qmuiteam.qmui.arch.scheme", "SchemeItem");
    private static ClassName ActivitySchemeItem = ClassName.get(
            "com.qmuiteam.qmui.arch.scheme", "ActivitySchemeItem");
    private static ClassName FragmentSchemeItem = ClassName.get(
            "com.qmuiteam.qmui.arch.scheme", "FragmentSchemeItem");

    private static TypeName SchemeItemList = ParameterizedTypeName.get(ListName, SchemeItem);
    private static TypeName MapByAction = ParameterizedTypeName.get(MapName,
            StringName, SchemeItemList);
    private static TypeName MapForSchemeRequired = ParameterizedTypeName.get(ArrayMapName,
            StringName, StringName);

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> activitySchemes = roundEnv.getElementsAnnotatedWith(ActivityScheme.class);
        Set<? extends Element> fragmentSchemes = roundEnv.getElementsAnnotatedWith(FragmentScheme.class);
        if (activitySchemes.isEmpty() && fragmentSchemes.isEmpty()) {
            return true;
        }
        TypeSpec.Builder classBuilder = TypeSpec
                .classBuilder(SchemeMap.simpleName() + "Impl")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(SchemeMap);
        classBuilder.addField(FieldSpec.builder(MapByAction, "mSchemeMap")
                .addModifiers(Modifier.PRIVATE)
                .build());


        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("mSchemeMap = new $T<>()", HashMapName);


        Map<String, List<Item>> schemeMap = new HashMap<>();
        for (Element element : activitySchemes) {
            if (element instanceof TypeElement) {
                TypeElement classElement = (TypeElement) element;
                TypeMirror elementType = classElement.asType();
                boolean isActivity = isSubtypeOfType(elementType, ACTIVITY_TYPE);
                if (!isActivity) {
                    error(element, "Must annotated on subclasses of Activity");
                } else {
                    ActivityScheme annotation = element.getAnnotation(ActivityScheme.class);
                    String name = annotation.name();
                    List<Item> elements = schemeMap.get(name);
                    if (elements == null) {
                        elements = new ArrayList<>();
                        schemeMap.put(name, elements);
                    }
                    elements.add(new Item(true, classElement, elementType, annotation.required()));

                }
            }
        }

        for (Element element : fragmentSchemes) {
            if (element instanceof TypeElement) {
                TypeElement classElement = (TypeElement) element;
                TypeMirror elementType = classElement.asType();
                boolean isQMUIFragment = isSubtypeOfType(elementType, QMUI_FRAGMENT_TYPE);
                if (!isQMUIFragment) {
                    error(element, "Must annotated on subclasses of QMUIFragment");
                } else {
                    FragmentScheme annotation = element.getAnnotation(FragmentScheme.class);
                    String name = annotation.name();
                    List<Item> elements = schemeMap.get(name);
                    if (elements == null) {
                        elements = new ArrayList<>();
                        schemeMap.put(name, elements);
                    }
                    elements.add(new Item(false, classElement, elementType, annotation.required()));

                }
            }
        }

        constructorBuilder.addStatement("$T elements", SchemeItemList);
        constructorBuilder.addStatement("$T required = null", MapForSchemeRequired);
        for (String key : schemeMap.keySet()) {
            List<Item> items = schemeMap.get(key);
            constructorBuilder.addStatement("elements = new $T<>()", ArrayListName);
            items.sort(new Comparator<Item>() {
                @Override
                public int compare(Item item, Item t1) {
                    int c1 = item.getRequiredCount();
                    int c2 = t1.getRequiredCount();
                    return Integer.compare(c2, c1);
                }
            });
            for (Item item : items) {
                ClassName elementName = ClassName.get(item.element);
                if (item.isActivity) {
                    ActivityScheme annotation = item.element.getAnnotation(ActivityScheme.class);
                    AnnotationMirror annotationMirror = getAnnotationMirror(item.element, ActivityScheme.class);
                    if (annotationMirror == null) {
                        continue;
                    }

                    appendRequired(constructorBuilder, annotation.required());

                    CodeBlock customFactory = generateCustomFactory(true, annotationMirror);
                    CodeBlock intParam = generateTypedParams(annotation.keysWithIntValue());
                    CodeBlock boolParam = generateTypedParams(annotation.keysWithBoolValue());
                    CodeBlock longParam = generateTypedParams(annotation.keysWithLongValue());
                    CodeBlock floatParam = generateTypedParams(annotation.keysWithFloatValue());
                    CodeBlock doubleParam = generateTypedParams(annotation.keysWithDoubleValue());
                    CodeBlock customMatcher = generateCustomMatcher(annotationMirror);
                    CodeBlock valueConverter = generateValueInterceptor(annotationMirror);

                    CodeBlock codeBlock = CodeBlock.builder()
                            .add("elements.add(")
                            /**/.add("new $T(", ActivitySchemeItem)
                            /*---*/.add("$T.class", elementName)
                            /*---*/.add(",")
                            /*---*/.add("$L", annotation.useRefreshIfCurrentMatched())
                            /*---*/.add(",")
                            /*---*/.add(customFactory)
                            /*---*/.add(",")
                            /*---*/.add("required")
                            /*---*/.add(",")
                            /*---*/.add(intParam)
                            /*---*/.add(",")
                            /*---*/.add(boolParam)
                            /*---*/.add(",")
                            /*---*/.add(longParam)
                            /*---*/.add(",")
                            /*---*/.add(floatParam)
                            /*---*/.add(",")
                            /*---*/.add(doubleParam)
                            /*---*/.add(",")
                            /*---*/.add(customMatcher)
                            /*---*/.add(",")
                            /*---*/.add(valueConverter)
                            /**/.add(")")
                            .add(")")
                            .build();
                    constructorBuilder.addStatement(codeBlock);
                } else {
                    FragmentScheme annotation = item.element.getAnnotation(FragmentScheme.class);
                    AnnotationMirror annotationMirror = getAnnotationMirror(item.element, FragmentScheme.class);
                    if (annotationMirror == null) {
                        continue;
                    }
                    appendRequired(constructorBuilder, annotation.required());
                    CodeBlock customFactory = generateCustomFactory(false, annotationMirror);
                    CodeBlock activities = generateFragmentHostActivityList(annotation);
                    CodeBlock intParam = generateTypedParams(annotation.keysWithIntValue());
                    CodeBlock boolParam = generateTypedParams(annotation.keysWithBoolValue());
                    CodeBlock longParam = generateTypedParams(annotation.keysWithLongValue());
                    CodeBlock floatParam = generateTypedParams(annotation.keysWithFloatValue());
                    CodeBlock doubleParam = generateTypedParams(annotation.keysWithDoubleValue());
                    CodeBlock customMatcher = generateCustomMatcher(annotationMirror);
                    CodeBlock valueConverter = generateValueInterceptor(annotationMirror);

                    CodeBlock codeBlock = CodeBlock.builder()
                            .add("elements.add(")
                            /**/.add("new $T(", FragmentSchemeItem)
                            /*---*/.add("$T.class", elementName)
                            /*---*/.add(",")
                            /*---*/.add("$L", annotation.useRefreshIfCurrentMatched())
                            /*---*/.add(",")
                            /*---*/.add(activities)
                            /*---*/.add(",")
                            /*---*/.add(customFactory)
                            /*---*/.add(",")
                            /*---*/.add("$L", annotation.forceNewActivity())
                            /*---*/.add(",")
                            /*---*/.add("$S", annotation.forceNewActivityKey())
                            /*---*/.add(",")
                            /*---*/.add("required")
                            /*---*/.add(",")
                            /*---*/.add(intParam)
                            /*---*/.add(",")
                            /*---*/.add(boolParam)
                            /*---*/.add(",")
                            /*---*/.add(longParam)
                            /*---*/.add(",")
                            /*---*/.add(floatParam)
                            /*---*/.add(",")
                            /*---*/.add(doubleParam)
                            /*---*/.add(",")
                            /*---*/.add(customMatcher)
                            /*---*/.add(",")
                            /*---*/.add(valueConverter)
                            /**/.add(")")
                            .add(")")
                            .build();
                    constructorBuilder.addStatement(codeBlock);
                }
            }

            constructorBuilder.addStatement("mSchemeMap.put($S, elements)", key);
        }

        ExecutableElement findScheme = getOverrideMethod(
                SchemeMap, "findScheme");
        List<? extends VariableElement> findSchemeParams = findScheme.getParameters();
        String schemeHandler = findSchemeParams.get(0).getSimpleName().toString();
        String schemeAction = findSchemeParams.get(1).getSimpleName().toString();
        String schemeParam = findSchemeParams.get(2).getSimpleName().toString();
        MethodSpec.Builder getRecordMetaById = MethodSpec.overriding(findScheme)
                .addStatement("$T list = mSchemeMap.get($L)", SchemeItemList, schemeAction)
                .beginControlFlow("if(list == null || list.isEmpty())")
                /**/.addStatement("return null")
                .endControlFlow()
                .beginControlFlow("for (int i = 0; i < list.size(); i++)")
                /**/.addStatement("$T item = list.get(i)", SchemeItem)
                /**/.beginControlFlow("if(item.match($L, $L))", schemeHandler, schemeParam)
                /*--*/.addStatement("return item")
                /**/.endControlFlow()
                .endControlFlow()
                .addStatement("return null");
        ExecutableElement exists = getOverrideMethod(
                SchemeMap, "exists");
        MethodSpec.Builder getRecordMetaByClass = MethodSpec.overriding(exists)
                .addStatement("return mSchemeMap.containsKey($L)", exists.getParameters().get(1).getSimpleName().toString());

        classBuilder
                .addMethod(constructorBuilder.build())
                .addMethod(getRecordMetaById.build())
                .addMethod(getRecordMetaByClass.build());
        try {
            JavaFile.builder(SchemeMap.packageName(), classBuilder.build())
                    .build().writeTo(mFiler);
        } catch (IOException e) {
            error(null, "Unable to generate RecordMetaMapImpl: %s", e.getMessage());
        }
        return true;
    }

    private void appendRequired(MethodSpec.Builder constructorBuilder, String[] required) {
        if (required == null || required.length == 0) {
            constructorBuilder.addStatement("required =null");
            return;
        }
        constructorBuilder.addStatement("required = new $T<>()", ArrayMapName);
        for (int i = 0; i < required.length; i++) {
            String condition = required[i];
            if (condition == null || condition.isEmpty()) {
                continue;
            }
            int index = condition.indexOf("=");
            if (index < 0 || index >= condition.length()) {
                constructorBuilder.addStatement("required.put($S, null)", condition);
            } else {
                String key = condition.substring(0, index);
                String value = index == condition.length() - 1 ? "" : condition.substring(index + 1);
                constructorBuilder.addStatement("required.put($S, $S)", key, value);
            }
        }
    }

    private CodeBlock generateTypedParams(String[] keys) {
        CodeBlock.Builder builder = CodeBlock.builder();
        if (keys == null || keys.length == 0) {
            builder.add("null");
        } else {
            builder.add("new $T[]{", StringName);
            for (int i = 0; i < keys.length; i++) {
                if (i != 0) {
                    builder.add(",");
                }
                builder.add("$S", keys[i]);
            }
            builder.add("}");
        }
        return builder.build();
    }


    private CodeBlock generateCustomFactory(boolean isActivity, AnnotationMirror annotationMirror){
        AnnotationValue customFactory = getAnnotationValue(annotationMirror, "customFactory");
        if (customFactory == null) {
            return CodeBlock.of("null");
        }
        TypeMirror typeMirror = (TypeMirror) customFactory.getValue();
        if(isActivity){
            if (!isSubtypeOfType(typeMirror, QMUISchemeIntentFactoryType)) {
                throw new IllegalStateException("customFactory must implement interface QMUISchemeIntentFactory.");
            }
        }else{
            if (!isSubtypeOfType(typeMirror, QMUISchemeFragmentFactoryType)) {
                throw new IllegalStateException("customFactory must implement interface QMUISchemeFragmentFactory.");
            }
        }

        return CodeBlock.of("$T.class", typeMirror);
    }

    private CodeBlock generateCustomMatcher(AnnotationMirror annotationMirror){
        AnnotationValue customFactory = getAnnotationValue(annotationMirror, "customMatcher");
        if (customFactory == null) {
            return CodeBlock.of("null");
        }
        TypeMirror typeMirror = (TypeMirror) customFactory.getValue();
        if (!isSubtypeOfType(typeMirror, QMUISchemeMatcherType)) {
            throw new IllegalStateException("customMatcher must implement interface QMUISchemeMatcher.");
        }

        return CodeBlock.of("$T.class", typeMirror);
    }

    private CodeBlock generateValueInterceptor(AnnotationMirror annotationMirror){
        AnnotationValue valueConverter = getAnnotationValue(annotationMirror, "valueConverter");
        if (valueConverter == null) {
            return CodeBlock.of("null");
        }
        TypeMirror typeMirror = (TypeMirror) valueConverter.getValue();
        if (!isSubtypeOfType(typeMirror, QMUISchemeValueConverterType)) {
            throw new IllegalStateException("customMatcher must implement interface QMUISchemeMatcher.");
        }

        return CodeBlock.of("$T.class", typeMirror);
    }

    private CodeBlock generateFragmentHostActivityList(FragmentScheme fragmentScheme){
        CodeBlock.Builder builder = CodeBlock.builder();
        TypeMirror[] activities = null;
        try {
            fragmentScheme.activities();
        } catch (MirroredTypesException mte) {
            List<? extends TypeMirror> containerMirrors = mte.getTypeMirrors();
            activities = new TypeMirror[containerMirrors.size()];
            for (int i = 0; i < activities.length; i++) {
                activities[i] = containerMirrors.get(i);
            }
        }
        if(activities == null || activities.length == 0){
            throw new IllegalStateException("FragmentScheme#activities can not be empty.");
        }
        builder.add("new $T[]{", OriginClassName);
        for(int i=0; i < activities.length; i++){
            TypeMirror item = activities[i];
            if(!isSubtypeOfType(item, QMUI_FRAGMENT_ACTIVITY_TYPE)){
                throw new IllegalStateException("FragmentScheme#activities must be QMUIFragmentActivity.");
            }
            if(i > 0){
                builder.add(",");
            }
            builder.add("$T.class", ClassName.get(item));
        }
        builder.add("}");
        return builder.build();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(ActivityScheme.class.getCanonicalName());
        types.add(FragmentScheme.class.getCanonicalName());
        return types;
    }

    static class Item {
        boolean isActivity = false;
        TypeElement element;
        TypeMirror type;
        String[] required;

        public Item(boolean isActivity, TypeElement element, TypeMirror type, String[] required) {
            this.isActivity = isActivity;
            this.element = element;
            this.type = type;
            this.required = required;
        }

        int getRequiredCount(){
            return required == null ? 0 : required.length;
        }
    }
}
