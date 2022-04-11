package com.qmuiteam.plugin

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.plugins.signing.SigningExtension
import java.io.File
import java.util.*
import kotlin.io.*

class QMUIPublish : Plugin<Project> {
    override fun apply(project: Project) {
        val isAndroid = project.hasProperty("android")

        if (isAndroid) {
            println("android")
            val android = project.extensions.getByName("android") as LibraryExtension
            android.publishing {
                singleVariant("release") {
                    withJavadocJar()
                    withSourcesJar()
                }
            }

        } else {
            println("java/kotlin")
            project.configure<JavaPluginExtension> {
                withSourcesJar()
                withJavadocJar()
            }
        }

        project.afterEvaluate {
            val properties = Properties()
            val file = File(project.rootProject.file("gradle"), "deploy.properties")
            if (file.exists()) {
                properties.load(file.inputStream())
                val mavenUrl = properties.getProperty("maven.url")
                val mavenUsername = properties.getProperty("maven.username")
                val mavenPassword = properties.getProperty("maven.password")

                println("mavenUrl:$mavenUrl")

                project.configure<PublishingExtension> {
                    repositories {
                        maven {
                            setUrl(mavenUrl)
                            credentials {
                                username = mavenUsername
                                password = mavenPassword
                            }
                        }
                    }
                    publications {
                        create<MavenPublication>("release") {

                            project.configure<SigningExtension> {
                                sign(this@create)
                            }

                            if (isAndroid) {
                                from(components.getByName("release"))
                            } else {
                                from(components.getByName("java"))
                            }

                            groupId = project.group as String
                            artifactId = project.name
                            version = project.version as String

                            pom {
                                name.set("${project.group}:${project.name}")
                                url.set("https://github.com/Tencent/QMUI_Android")
                                description.set("qmui android library.")
                                licenses {
                                    license {
                                        name.set(properties.getProperty("license.name"))
                                        url.set(properties.getProperty("license.url"))
                                    }
                                }
                                developers {
                                    developer {
                                        id.set(properties.getProperty("developer.id"))
                                        name.set(properties.getProperty("developer.name"))
                                        email.set(properties.getProperty("developer.email"))
                                    }
                                }
                                scm {
                                    connection.set("scm:git:git://github.com/Tencent/QMUI_Android.git")
                                    developerConnection.set("scm:git:ssh://github.com/Tencent/QMUI_Android.git")
                                    url.set("https://qmuiteam.com/android")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun println(log: String) {
    kotlin.io.println("qmui config publish > $log")
}