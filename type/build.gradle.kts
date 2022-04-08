import com.qmuiteam.plugin.Dep

plugins {
    id("com.android.library")
    kotlin("android")
    `maven-publish`
    signing
    id("qmui-publish")
}

version = Dep.QMUI.typeVer


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
}

dependencies {
    implementation(Dep.AndroidX.appcompat)
    implementation(Dep.AndroidX.annotation)
    implementation(Dep.AndroidX.coreKtx)
}