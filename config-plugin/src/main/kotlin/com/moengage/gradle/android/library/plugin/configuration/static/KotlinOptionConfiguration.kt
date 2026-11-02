package com.moengage.gradle.android.library.plugin.configuration.static

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Configures Kotlin compiler options for Android library modules, enforcing a consistent JVM target
 * and strict explicit API mode.
 *
 * @author Abhishek Kumar
 */
internal abstract class KotlinOptionConfiguration : StaticConfig {

    override fun apply(project: Project, libraryExtension: LibraryExtension) {
        project.extensions.configure<KotlinAndroidProjectExtension> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_1_8)
                freeCompilerArgs.add("-Xexplicit-api=strict")
            }
        }
    }
}
