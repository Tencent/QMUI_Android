package com.qmuiteam.qmui.arch;

import com.google.auto.service.AutoService;
import com.qmuiteam.qmui.arch.annotation.BoolArgument;
import com.qmuiteam.qmui.arch.annotation.FloatArgument;
import com.qmuiteam.qmui.arch.annotation.IntArgument;
import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord;
import com.qmuiteam.qmui.arch.annotation.LongArgument;
import com.qmuiteam.qmui.arch.annotation.StringArgument;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import javafx.util.Pair;

@AutoService(Processor.class)
public class LatestVisitProcessor extends BaseProcessor {

    private static ClassName RecordMetaMapName = ClassName.get(
            "com.qmuiteam.qmui.arch.record", "RecordMetaMap");
    private static ClassName RecordMetaName = ClassName.get(
            "com.qmuiteam.qmui.arch.record", "RecordMeta");
    private static ClassName RecordMetaArgumentName = ClassName.get(
            "com.qmuiteam.qmui.arch.record", "RecordMeta.ArgumentType");

    private static TypeName MapByClassName = ParameterizedTypeName.get(MapName,
            OriginClassName, RecordMetaName);
    private static TypeName MapByIdName = ParameterizedTypeName.get(MapName,
            IntegerName, RecordMetaName);

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(LatestVisitRecord.class);
        if (elements.isEmpty()) {
            return true;
        }
        TypeSpec.Builder classBuilder = TypeSpec
                .classBuilder("RecordMetaMapImpl")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(RecordMetaMapName);
        classBuilder.addField(FieldSpec.builder(MapByClassName, "mMapByClass")
                .addModifiers(Modifier.PRIVATE)
                .build());

        classBuilder.addField(FieldSpec.builder(MapByIdName, "mMapById")
                .addModifiers(Modifier.PRIVATE)
                .build());

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("mMapByClass = new $T<>()", HashMapName)
                .addStatement("mMapById = new $T<>()", HashMapName);

        int currentId = 1000;
        for (Element element : elements) {
            if (element instanceof TypeElement) {
                TypeElement classElement = (TypeElement) element;
                TypeMirror elementType = classElement.asType();
                if (isSubtypeOfType(elementType, QMUI_FRAGMENT_ACTIVITY_TYPE)
                        || isSubtypeOfType(elementType, QMUI_FRAGMENT_TYPE)
                        || isSubtypeOfType(elementType, QMUI_ACTIVITY_TYPE)) {
                    ClassName elementName = ClassName.get(classElement);
                    List<Pair<String, String>> nameAndTypeList = new ArrayList<>();
                    BoolArgument boolAnnotation = element.getAnnotation(BoolArgument.class);
                    if (boolAnnotation != null) {
                        for (String name : boolAnnotation.names()) {
                            nameAndTypeList.add(new Pair<>(name, "Boolean"));
                        }
                    }
                    FloatArgument floatArguments = element.getAnnotation(FloatArgument.class);
                    if (floatArguments != null) {
                        for (String name : floatArguments.names()) {
                            nameAndTypeList.add(new Pair<>(name, "Float"));
                        }
                    }
                    LongArgument longArguments = element.getAnnotation(LongArgument.class);
                    if (longArguments != null) {
                        for (String name : longArguments.names()) {
                            nameAndTypeList.add(new Pair<>(name, "Long"));
                        }
                    }
                    IntArgument intArguments = element.getAnnotation(IntArgument.class);
                    if (intArguments != null) {
                        for (String name : intArguments.names()) {
                            nameAndTypeList.add(new Pair<>(name, "Integer"));
                        }
                    }
                    StringArgument stringArguments = element.getAnnotation(StringArgument.class);
                    if (stringArguments != null) {
                        for (String name : stringArguments.names()) {
                            nameAndTypeList.add(new Pair<>(name, "String"));
                        }
                    }


                    if (nameAndTypeList.size() == 0) {
                        constructorBuilder.addStatement("$T record$L = new $T($L, $T.class, null)",
                                RecordMetaName, currentId, RecordMetaName, currentId, elementName);
                    } else {
                        CodeBlock.Builder builder = CodeBlock.builder();
                        builder.add("$T record$L = new $T($L, $T.class, new $T[]{\n",
                                RecordMetaName, currentId, RecordMetaName, currentId,
                                elementName, RecordMetaArgumentName);
                        builder.indent();
                        String name;
                        String type;
                        for (int i = 0; i < nameAndTypeList.size(); i++) {
                            name = nameAndTypeList.get(i).getKey();
                            type = nameAndTypeList.get(i).getValue();
                            builder.add("new $T($S, $L.class)", RecordMetaArgumentName, name, type);
                            if (i < nameAndTypeList.size() - 1) {
                                builder.add(",\n");
                            }

                        }
                        builder.unindent();
                        builder.add("\n});");
                        constructorBuilder.addStatement(builder.build());
                    }

                    constructorBuilder.addStatement("mMapByClass.put($T.class, record$L)",
                            elementName,
                            currentId);
                    constructorBuilder.addStatement("mMapById.put($L, record$L)",
                            currentId,
                            currentId);
                    currentId++;
                } else {
                    error(element, "Must annotated on subclasses of QMUIFragmentActivity");
                }
            }
        }

        ExecutableElement iGetRecordMetaById = getOverrideMethod(
                RecordMetaMapName, "getRecordMetaById");
        MethodSpec.Builder getRecordMetaById = MethodSpec.overriding(iGetRecordMetaById)
                .addStatement("return mMapById.get($L)",
                        iGetRecordMetaById.getParameters().get(0).getSimpleName().toString());
        ExecutableElement iGetRecordMetaByClass = getOverrideMethod(
                RecordMetaMapName, "getRecordMetaByClass");
        MethodSpec.Builder getRecordMetaByClass = MethodSpec.overriding(iGetRecordMetaByClass)
                .addStatement("return mMapByClass.get($L)",
                        iGetRecordMetaByClass.getParameters().get(0).getSimpleName().toString());

        classBuilder
                .addMethod(constructorBuilder.build())
                .addMethod(getRecordMetaById.build())
                .addMethod(getRecordMetaByClass.build());
        try {
            JavaFile.builder(RecordMetaMapName.packageName(), classBuilder.build())
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
