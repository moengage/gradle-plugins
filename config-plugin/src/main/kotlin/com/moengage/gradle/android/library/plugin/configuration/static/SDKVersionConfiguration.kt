package com.moengage.gradle.android.library.plugin.configuration.static

import com.android.build.api.dsl.LibraryExtension
import com.moengage.gradle.android.library.plugin.utils.ANDROID_BUILD_CONFIG_COMPILE_SDK_VERSION
import com.moengage.gradle.android.library.plugin.utils.ANDROID_BUILD_CONFIG_MINIMUM_SDK_VERSION
import org.gradle.api.Project

/**
 * Configures core Android SDK versions for library modules, setting a shared `compileSdk` and
 * `minSdk` based on central constants.
 *
 * This ensures that all MoEngage SDK libraries target the same Android SDK levels for compilation
 * and minimum supported devices.
 *
 * @author Abhishek Kumar
 */
internal abstract class SDKVersionConfiguration : StaticConfig {

    override fun apply(project: Project, libraryExtension: LibraryExtension) {
        with(libraryExtension) {
            compileSdk = ANDROID_BUILD_CONFIG_COMPILE_SDK_VERSION
            defaultConfig { minSdk = ANDROID_BUILD_CONFIG_MINIMUM_SDK_VERSION }
        }
    }
}
