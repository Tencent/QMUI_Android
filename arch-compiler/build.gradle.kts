import com.qmuiteam.plugin.Dep
import java.io.FileInputStream
import java.util.*

plugins {
    `java-library`
    `maven-publish`
}


dependencies {
    implementation(project(":arch-annotation"))
    implementation(Dep.CodeGen.javapoet)
    implementation(Dep.CodeGen.autoService)
    annotationProcessor(Dep.CodeGen.autoService)
}

java {
    sourceCompatibility = Dep.javaVersion
    targetCompatibility = Dep.javaVersion
}

afterEvaluate {
    val file = rootProject.file("gradle/deploy.properties")
    if(file.exists()) {
        val properties = Properties()
        properties.load(FileInputStream(file))
        publishing {
            publications {
                create<MavenPublication>("release") {
                    from(components["java"])
                    groupId = Dep.QMUI.group
                    artifactId = project.name
                    version = Dep.QMUI.archVer
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
