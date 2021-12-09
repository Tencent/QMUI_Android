import com.qmuiteam.plugin.Dep

plugins {
    id("com.android.library")
    kotlin("android")
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
    namespace = "com.qmuiteam.qmui.type"
}

dependencies {
    api(Dep.AndroidX.annotation)
    api(Dep.AndroidX.coreKtx)
}

// deploy
if(rootProject.file("gradle/deploy.properties").exists()){
    apply(from = rootProject.file("gradle/deploy.gradle"))
}