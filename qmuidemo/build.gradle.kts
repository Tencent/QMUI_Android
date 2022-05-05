import com.qmuiteam.plugin.Dep
import java.io.ByteArrayOutputStream
import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

fun runCommand(project: Project, command: String): String {
    val stdout = ByteArrayOutputStream()
    project.exec {
        commandLine = command.split(" ")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}


val gitVersion = runCommand(project, "git rev-list HEAD --count").toIntOrNull() ?: 1


android {
    signingConfigs {
        val properties = Properties()
        val propFile = project.file("release.properties")
        if (propFile.exists()) {
            properties.load(propFile.inputStream())
        }
        create("release"){
            keyAlias = properties.getProperty("RELEASE_KEY_ALIAS")
            keyPassword = properties.getProperty("RELEASE_KEY_PASSWORD")
            storeFile = file("qmuidemo.keystore")
            storePassword = properties.getProperty("RELEASE_STORE_PASSWORD")
            enableV2Signing = true
        }
    }

    compileSdk = Dep.compileSdk
    compileOptions {
        sourceCompatibility = Dep.javaVersion
        targetCompatibility = Dep.javaVersion
    }

    kotlinOptions {
        jvmTarget = Dep.kotlinJvmTarget
        freeCompilerArgs += "-Xjvm-default=all"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Dep.Compose.version
    }

    defaultConfig {
        applicationId = "com.qmuiteam.qmuidemo"
        minSdk = Dep.minSdk
        targetSdk = Dep.targetSdk
        versionCode = gitVersion
        versionName = Dep.QMUI.qmuiVer

        ndk {
            abiFilters.add("arm64-v8a")
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

dependencies {
    implementation(Dep.AndroidX.appcompat)
    implementation(Dep.AndroidX.annotation)
    implementation(Dep.AndroidX.activity)
    implementation(Dep.MaterialDesign.material)
    implementation(Dep.ButterKnife.butterknife)
    implementation(Dep.Compose.activity)
    implementation(Dep.Compose.constraintlayout)
    kapt(Dep.ButterKnife.compiler)
    implementation(project(":lib"))
    implementation(project(":qmui"))
    implementation(project(":arch"))
    implementation(project(":type"))
    implementation(project(":compose"))
    implementation(project(":photo"))
    implementation(project(":photo-coil"))
    implementation(project(":photo-glide"))
    implementation(project(":editor"))
    implementation(Dep.Flipper.soLoader)
    implementation(Dep.Flipper.flipper)
    kapt(project(":compiler"))
    kapt(project(":arch-compiler"))
    kapt(Dep.Glide.compiler)

    implementation("com.iqiyi.xcrash:xcrash-android-lib:3.1.0")
}
