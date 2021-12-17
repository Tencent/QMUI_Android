import com.qmuiteam.plugin.Dep
import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.library")
    kotlin("android")
    `maven-publish`
}


android {
    compileSdk = Dep.compileSdk

    buildFeatures {
        compose = true
    }

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

    composeOptions {
        kotlinCompilerExtensionVersion = Dep.Compose.version
    }
}

dependencies {
    api(Dep.AndroidX.annotation)
    api(Dep.AndroidX.appcompat)
    api(Dep.AndroidX.coreKtx)
    api(Dep.Compose.ui)
    api(Dep.Compose.animation)
    api(Dep.Compose.material)
    api(Dep.Compose.compiler)
}

afterEvaluate {
    val file = rootProject.file("gradle/deploy.properties")
    if(file.exists()) {
        val properties = Properties()
        properties.load(FileInputStream(file))
        publishing {
            publications {
                create<MavenPublication>("release") {
                    from(components.getByName("release"))
                    groupId = Dep.QMUI.group
                    artifactId = project.name
                    version = Dep.QMUI.composeVer
                }
            }
            repositories {
                maven {
                    setUrl(properties.getProperty("maven.url"))
                    credentials {
                        username = properties.getProperty("maven.username")
                        password = properties.getProperty("maven.password")
                    }
                }
            }
        }
    }
}