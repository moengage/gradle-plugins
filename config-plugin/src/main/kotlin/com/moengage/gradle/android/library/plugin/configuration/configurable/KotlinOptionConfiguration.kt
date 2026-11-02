package com.moengage.gradle.android.library.plugin.configuration.configurable

import com.android.build.api.dsl.LibraryExtension
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Configures Kotlin compiler options for Android library modules, enforcing a consistent JVM target
 * and strict explicit API mode.
 *
 * @author Abhishek Kumar
 */
public abstract class KotlinOptionConfiguration @Inject constructor() : ConfigurableConfig {

    public abstract val enableJvmTarget: Property<Boolean>
    public abstract val configuredJvmTarget: Property<JvmTarget>
    public abstract val compilerArgs: ListProperty<String>

    init {
        enableJvmTarget.convention(true)
        configuredJvmTarget.convention(JvmTarget.JVM_1_8)
        compilerArgs.convention(listOf("-Xexplicit-api=strict"))
    }

    override fun apply(project: Project, libraryExtension: LibraryExtension) {
        project.extensions.configure<KotlinAndroidProjectExtension> {
            compilerOptions {
                if (enableJvmTarget.get()) {
                    jvmTarget.set(configuredJvmTarget.get())
                }
                compilerArgs.get().forEach { freeCompilerArgs.add(it) }
            }
        }
    }
}
