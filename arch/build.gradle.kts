import com.qmuiteam.plugin.Dep

plugins {
    id("com.android.library")
    kotlin("android")
}

version = Dep.QMUI.archVer

android {
    compileSdk = Dep.compileSdk

    defaultConfig {
        minSdk = Dep.minSdk
        targetSdk = Dep.targetSdk
    }

    buildTypes {
        getByName("release"){
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = Dep.javaVersion
        targetCompatibility = Dep.javaVersion
    }
    kotlinOptions {
        jvmTarget = Dep.kotlinJvmTarget
    }
    namespace = "com.qmuiteam.qmui.arch"
}

dependencies {
    api(Dep.AndroidX.appcompat)
    api(Dep.AndroidX.fragment)
    api(project(":arch-annotation"))
    compileOnly(project(":qmui"))
}

// deploy
if(rootProject.file("gradle/deploy.properties").exists()){
    apply(from = rootProject.file("gradle/deploy.gradle"))
}