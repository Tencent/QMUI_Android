import com.qmuiteam.plugin.Dep

plugins {
    `java-library`
}

version = Dep.QMUI.archVer

dependencies {
    implementation(project(":arch-annotation"))
    implementation(Dep.CodeGen.javapoet)
    annotationProcessor(Dep.CodeGen.autoService)
}

java {
    sourceCompatibility = Dep.javaVersion
    targetCompatibility = Dep.javaVersion
}

// deploy
if(rootProject.file("gradle/deploy.properties").exists()){
    apply(from = rootProject.file("gradle/deploy.gradle"))
}
