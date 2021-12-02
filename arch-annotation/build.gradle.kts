import com.qmuiteam.plugin.Dep

plugins {
    `java-library`
    kotlin("jvm")
}

version = Dep.QMUI.archVer

java {
    sourceCompatibility = Dep.javaVersion
    targetCompatibility = Dep.javaVersion
}

// deploy
if(rootProject.file("gradle/deploy.properties").exists()){
    apply(from = rootProject.file("gradle/deploy.gradle"))
}
