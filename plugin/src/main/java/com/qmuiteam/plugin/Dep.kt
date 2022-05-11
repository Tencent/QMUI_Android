package com.qmuiteam.plugin

import org.gradle.api.JavaVersion

object Dep {

    val javaVersion = JavaVersion.VERSION_11
    const val kotlinJvmTarget = "11"
    const val compileSdk = 31
    const val minSdk = 21
    const val targetSdk = 31


    object QMUI {
        const val group = "com.qmuiteam"
        const val qmuiVer = "2.1.0.4"
        const val archVer = "2.1.0.3"
        const val typeVer = "0.1.0.5"

        // composeMajor.composeMinor.qmuiReleaseNumber
        const val composeCoreVer = "1.1.1"
        const val composeVer = "1.1.1"
        const val photoVer = "1.1.1.1"
        const val editorVer = "1.1.1"
    }

    object Coroutines {
        private const val version = "1.6.0"
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
    }

    object AndroidX {
        val appcompat = "androidx.appcompat:appcompat:1.4.0"
        val annotation = "androidx.annotation:annotation:1.3.0"
        val coreKtx = "androidx.core:core-ktx:1.7.0"
        val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.2"
        val swiperefreshlayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
        val activity = "androidx.activity:activity-ktx:1.4.0"
        val fragment = "androidx.fragment:fragment:1.4.1"
    }

    object Compose {
        val version = "1.2.0-alpha08"
        val animation = "androidx.compose.animation:animation:$version"
        val ui = "androidx.compose.ui:ui:$version"
        val material = "androidx.compose.material:material:$version"
        val compiler = "androidx.compose.compiler:compiler:$version"
        val activity = "androidx.activity:activity-compose:1.4.0"
        val constraintlayout = "androidx.constraintlayout:constraintlayout-compose:1.0.0"

        val pager = "com.google.accompanist:accompanist-pager:0.23.1"
    }

    object Flipper {
        private const val version = "0.96.1"
        const val soLoader = "com.facebook.soloader:soloader:0.10.1"
        const val flipper = "com.facebook.flipper:flipper:$version"
    }

    object MaterialDesign {
        const val material = "com.google.android.material:material:1.4.0"
    }

    object CodeGen {
        const val javapoet = "com.squareup:javapoet:1.13.0"
        const val autoService = "com.google.auto.service:auto-service:1.0-rc2"
    }

    object ButterKnife {
        private const val ver = "10.1.0"
        const val butterknife = "com.jakewharton:butterknife:$ver"
        const val compiler = "com.jakewharton:butterknife-compiler:$ver"
    }

    object Coil {
        const val compose = "io.coil-kt:coil-compose:2.0.0-alpha06"
    }

    object Glide {
        private const val ver = "4.13.0"
        const val glide = "com.github.bumptech.glide:glide:$ver"
        const val compiler = "com.github.bumptech.glide:compiler:$ver"
    }
}