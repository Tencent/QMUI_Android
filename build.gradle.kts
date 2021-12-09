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
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
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

//    ext {
//        minSdkVersion = 21
//        targetSdkVersion = 30
//        compileSdkVersion = 30
//        coreVersion='1.6.0'
//        appcompatVersion= '1.3.1'
//        materialVersion='1.4.0'
//        annotationVersion='1.2.0'
//        butterknifeVersion = '10.1.0'
//        constraintLayoutVersion = "2.1.0"
//        mmkvVersion = '1.0.23'
//        junitVersion='4.13.2'
//    }
}
