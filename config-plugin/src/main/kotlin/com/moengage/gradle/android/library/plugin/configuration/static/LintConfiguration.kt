package com.moengage.gradle.android.library.plugin.configuration.static

import com.android.build.api.dsl.LibraryExtension
import com.moengage.gradle.android.library.plugin.utils.ANDROID_BUILD_CONFIG_TARGET_SDK_VERSION
import org.gradle.api.Project

/**
 * Configures Android Lint options for library modules, aligning the lint target SDK with the
 * configured target SDK version.
 *
 * @author Abhishek Kumar
 */
internal abstract class LintConfiguration : StaticConfig {

    override fun apply(project: Project, libraryExtension: LibraryExtension) {
        with(libraryExtension) { lint { targetSdk = ANDROID_BUILD_CONFIG_TARGET_SDK_VERSION } }
    }
}
