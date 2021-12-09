package com.qmuiteam.plugin

import org.gradle.api.JavaVersion

object Dep {

    val javaVersion = JavaVersion.VERSION_11
    const val kotlinJvmTarget = "11"
    const val kotlinVer = "1.5.31"
    const val compileSdk = 31
    const val minSdk = 21
    const val targetSdk = 31


    object QMUI {
        const val group = "com.qmuiteam"
        const val qmuiVer = "2.0.1"
        const val archVer = "2.0.1"
        const val typeVer = "0.0.14"
    }

    object AndroidX {
        const val appcompat = "androidx.appcompat:appcompat:1.4.0"
        const val annotation = "androidx.annotation:annotation:1.3.0"
        const val coreKtx = "androidx.core:core-ktx:1.7.0"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.2"
        const val swiperefreshlayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
        const val fragment = "androidx.fragment:fragment:1.3.6"
    }

    object MaterialDesign {
        const val material = "com.google.android.material:material:1.4.0"
    }

    object CodeGen {
        const val javapoet = "com.squareup:javapoet:1.13.0"
        const val autoService = "com.google.auto.service:auto-service:1.0.1"
    }

    object ButterKnife {
        private const val ver = "10.1.0"
        const val butterknife = "com.jakewharton:butterknife:$ver"
        const val compiler = "com.jakewharton:butterknife-compiler:$ver"
    }
}