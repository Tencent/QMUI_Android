plugins {
    `java-gradle-plugin`
    idea
    kotlin("jvm") version "1.5.31"
}


group = "com.qmuiteam.qmui.plugin"
version = "0.0.1"


gradlePlugin {
    plugins {
        create("qmui-dep"){
            id = "qmui-dep"
            implementationClass = "com.qmuiteam.plugin.QMUIDepPlugin"
        }
    }
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    api(gradleApi())
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}