import com.qmuiteam.plugin.Dep

plugins {
    id("com.android.library")
    kotlin("android")
    `maven-publish`
    signing
    id("qmui-publish")
}

version = Dep.QMUI.photoVer


android {
    compileSdk = Dep.compileSdk

    defaultConfig {
        minSdk = Dep.minSdk
        targetSdk = Dep.targetSdk
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Dep.Compose.version
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
}

dependencies {
    api(project(":compose-core"))
    api(Dep.AndroidX.activity)
    api(Dep.Compose.activity)
    api(Dep.Compose.pager)
}