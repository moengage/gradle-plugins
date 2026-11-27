package com.moengage.gradle.android.library.plugin.utils

// Build Versions
internal const val ANDROID_BUILD_CONFIG_MINIMUM_SDK_VERSION = 23
internal const val ANDROID_BUILD_CONFIG_COMPILE_SDK_VERSION = 36
internal const val ANDROID_BUILD_CONFIG_TARGET_SDK_VERSION = 36

// Library Versions
internal const val JACOCO_VERSION = "0.8.14"

// Plugin IDs
internal const val PLUGIN_ID_ANDROID_LIBRARY = "com.android.library"
internal const val PLUGIN_ID_KOTLIN_SERIALIZATION = "org.jetbrains.kotlin.plugin.serialization"
internal const val PLUGIN_ID_RELEASE = "com.moengage.plugin.maven.publish"
internal const val PLUGIN_ID_DOKKA = "org.jetbrains.dokka"
internal const val PLUGIN_ID_JACOCO = "jacoco"

// Defaults
internal const val DEFAULT_ANDROID_RUNNER = "androidx.test.runner.AndroidJUnitRunner"
internal const val DEFAULT_PROGUARD_FILE_NAME = "proguard-rules.pro"
internal const val DEFAULT_ANDROID_PROGUARD_FILE_NAME = "proguard-android-optimize.txt"
internal const val DEFAULT_CONSUMER_PROGUARD_FILE = "consumer-rules.pro"
