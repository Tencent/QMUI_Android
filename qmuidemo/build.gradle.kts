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
    val propFile = project.file("release.properties")
    signingConfigs {
        val properties = Properties()
        if (propFile.exists()) {
            properties.load(propFile.inputStream())
            getByName("release"){
                keyAlias = properties.getProperty("RELEASE_KEY_ALIAS")
                keyPassword = properties.getProperty("RELEASE_KEY_PASSWORD")
                storeFile = file("qmuidemo.keystore")
                storePassword = properties.getProperty("RELEASE_STORE_PASSWORD")
                enableV2Signing = false
            }
        }
    }

    compileSdk = Dep.compileSdk
    compileOptions {
        sourceCompatibility = Dep.javaVersion
        targetCompatibility = Dep.javaVersion
    }

    defaultConfig {
        applicationId = "com.qmuiteam.qmuidemo"
        minSdk = Dep.minSdk
        targetSdk = Dep.targetSdk
        versionCode = gitVersion
        versionName = Dep.QMUI.qmuiVer
    }
    buildTypes {
        if (propFile.exists()){
            getByName("release") {
                isMinifyEnabled = true
                proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    namespace = "com.qmuiteam.qmuidemo"
}

dependencies {
    implementation(Dep.AndroidX.appcompat)
    implementation(Dep.AndroidX.annotation)
    implementation(Dep.MaterialDesign.material)

    implementation(Dep.ButterKnife.butterknife)
    kapt(Dep.ButterKnife.compiler)
    implementation(project(":lib"))
    implementation(project(":qmui"))
    implementation(project(":arch"))
    implementation(project(":type"))
    kapt(project(":compiler"))
    kapt(project(":arch-compiler"))
}
