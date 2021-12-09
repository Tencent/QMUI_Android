import com.qmuiteam.plugin.Dep

plugins {
    `java-library`
}

java {
    sourceCompatibility = Dep.javaVersion
    targetCompatibility = Dep.javaVersion
}
dependencies {
    implementation(project(":lib"))
    implementation(Dep.CodeGen.javapoet)
    implementation(Dep.CodeGen.autoService)
    annotationProcessor(Dep.CodeGen.autoService)
}
