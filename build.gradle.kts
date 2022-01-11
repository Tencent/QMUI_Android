// Top-level build file where you can add configuration options common to all sub-projects/modules.
import com.qmuiteam.plugin.Dep
buildscript {
    repositories {
        mavenCentral()
        google()
        mavenLocal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.0")
    }
}

plugins {
    id("qmui-dep")
}

subprojects {
    group = Dep.QMUI.group
}

allprojects {
    repositories {
        mavenCentral()
        google()
        mavenLocal()
    }
}
