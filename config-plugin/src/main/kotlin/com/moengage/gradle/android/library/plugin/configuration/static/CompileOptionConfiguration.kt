package com.moengage.gradle.android.library.plugin.configuration.static

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project

/**
 * Configures Java compile options for Android library modules, enforcing a consistent source and
 * target compatibility.
 *
 * @author Abhishek Kumar
 */
internal abstract class CompileOptionConfiguration : StaticConfig {

    override fun apply(project: Project, libraryExtension: LibraryExtension) {
        with(libraryExtension) {
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }
        }
    }
}
