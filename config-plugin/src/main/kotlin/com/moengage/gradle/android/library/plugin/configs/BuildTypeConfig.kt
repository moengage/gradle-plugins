package com.moengage.gradle.android.library.plugin.configs

import com.moengage.gradle.android.library.plugin.utils.DEFAULT_ANDROID_PROGUARD_FILE_NAME
import com.moengage.gradle.android.library.plugin.utils.DEFAULT_PROGUARD_FILE_NAME

/**
 * Defines how a build type should be obtained or created.
 *
 * @author Abhishek Kumar
 */
public enum class BuildTypeKind {
    /** Use an existing build type or register it if missing. */
    REGISTER,
    /** Always create a new build type entry. */
    CREATE
}

/**
 * Configuration for a single Android build type.
 *
 * @author Abhishek Kumar
 */
public data class BuildTypeConfig(
    /** Name of the build type (e.g. debug, release). */
    val name: String,
    /** Strategy for obtaining the build type (register or create). */
    val kind: BuildTypeKind,
    /** Whether code shrinking/obfuscation is enabled for this build type. */
    val isMinifyEnabled: Boolean = false,
    /** Whether resource shrinking is enabled for this build type. */
    val isShrinkResources: Boolean = false,
    /** Default ProGuard file to be used for this build type. */
    val defaultAndroidProguardFile: String = DEFAULT_ANDROID_PROGUARD_FILE_NAME,
    /** ProGuard/R8 rules files to be applied for this build type. */
    val proguardFiles: List<String> = listOf(DEFAULT_PROGUARD_FILE_NAME)
)
