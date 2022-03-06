import com.qmuiteam.plugin.Dep

plugins {
    `java-library`
    `maven-publish`
    signing
    id("qmui-publish")
}
version = Dep.QMUI.archVer

dependencies {
    implementation(project(":arch-annotation"))
    implementation(Dep.CodeGen.javapoet)
    implementation(Dep.CodeGen.autoService)
    annotationProcessor(Dep.CodeGen.autoService)
}

java {
    sourceCompatibility = Dep.javaVersion
    targetCompatibility = Dep.javaVersion
}