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
                    BoolArgument[] boolArguments = element.getAnnotationsByType(BoolArgument.class);
                    FloatArgument[] floatArguments = element.getAnnotationsByType(FloatArgument.class);
                    LongArgument[] longArguments = element.getAnnotationsByType(LongArgument.class);
                    IntArgument[] intArguments = element.getAnnotationsByType(IntArgument.class);
                    StringArgument[] stringArguments = element.getAnnotationsByType(StringArgument.class);
                    int totalArgumentsCount = 0;
                    if (!isNullOrEmpty(boolArguments)) {
                        totalArgumentsCount += boolArguments.length;
                    }
                    if (!isNullOrEmpty(floatArguments)) {
                        totalArgumentsCount += floatArguments.length;
                    }
                    if (!isNullOrEmpty(longArguments)) {
                        totalArgumentsCount += longArguments.length;
                    }
                    if (!isNullOrEmpty(intArguments)) {
                        totalArgumentsCount += intArguments.length;
                    }
                    if (!isNullOrEmpty(stringArguments)) {
                        totalArgumentsCount += stringArguments.length;
                    }

                    if (totalArgumentsCount == 0) {
                        constructorBuilder.addStatement("$T record$L = new $T($L, $T.class, null)",
                                RecordMetaName, currentId, RecordMetaName, currentId, elementName);
                    } else {
                        CodeBlock.Builder builder = CodeBlock.builder();
                        builder.add("$T record$L = new $T($L, $T.class, new $T[]{\n",
                                RecordMetaName, currentId, RecordMetaName, currentId,
                                elementName, RecordMetaArgumentName);
                        builder.indent();
                        int i;
                        if (!isNullOrEmpty(boolArguments)) {
                            BoolArgument arg;
                            for (i = 0; i < boolArguments.length; i++) {
                                arg = boolArguments[i];
                                builder.add("new $T($S, Boolean.class, $L)",
                                        RecordMetaArgumentName, arg.name(), arg.defaultValue());
                                if(--totalArgumentsCount > 0){
                                    builder.add(",\n");
                                }
                            }
                        }
                        if (totalArgumentsCount > 0 && !isNullOrEmpty(intArguments)) {
                            IntArgument arg;
                            for (i = 0; i < intArguments.length; i++) {
                                arg = intArguments[i];
                                builder.add("new $T($S, Integer.class, $L)",
                                        RecordMetaArgumentName, arg.name(), arg.defaultValue());
                                if(--totalArgumentsCount > 0){
                                    builder.add(",\n");
                                }
                            }
                        }
                        if (totalArgumentsCount > 0 && !isNullOrEmpty(longArguments)) {
                            LongArgument arg;
                            for (i = 0; i < longArguments.length; i++) {
                                arg = longArguments[i];
                                builder.add("new $T($S, Long.class, Long.valueOf($L))",
                                        RecordMetaArgumentName, arg.name(), arg.defaultValue());
                                if(--totalArgumentsCount > 0){
                                    builder.add(",\n");
                                }
                            }
                        }
                        if (totalArgumentsCount >0 && !isNullOrEmpty(floatArguments)) {
                            FloatArgument arg;
                            for (i = 0; i < floatArguments.length; i++) {
                                arg = floatArguments[i];
                                builder.add("new $T($S, Float.class, Float.valueOf((float)$L))",
                                        RecordMetaArgumentName, arg.name(), arg.defaultValue());
                                if(--totalArgumentsCount > 0){
                                    builder.add(",\n");
                                }
                            }
                        }
                        if (totalArgumentsCount > 0 && !isNullOrEmpty(stringArguments)) {
                            StringArgument arg;
                            for (i = 0; i < stringArguments.length; i++) {
                                arg = stringArguments[i];
                                builder.add("new $T($S, $T.class, $S)",
                                        RecordMetaArgumentName, arg.name(), StringName, arg.defaultValue());
                                if(--totalArgumentsCount > 0){
                                    builder.add(",\n");
                                }
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

    private <T> boolean isNullOrEmpty(T[] array) {
        return array == null || array.length == 0;
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(LatestVisitRecord.class.getCanonicalName());
        return types;
    }
}
