package com.moengage.gradle.android.library.plugin.configuration.static

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project

/**
 * Configures the debug build type for Android library modules, enabling unit and instrumentation
 * test coverage reports.
 *
 * @author Abhishek Kumar
 */
internal abstract class DebugBuildTypeConfiguration : StaticConfig {

    override fun apply(project: Project, libraryExtension: LibraryExtension) {
        with(libraryExtension) {
            buildTypes {
                debug {
                    enableUnitTestCoverage = true
                    enableAndroidTestCoverage = true
                }
            }
        }
    }
}
