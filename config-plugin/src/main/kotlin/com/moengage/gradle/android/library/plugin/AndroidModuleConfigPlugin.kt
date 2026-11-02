package com.moengage.gradle.android.library.plugin

import com.android.build.api.dsl.LibraryExtension
import com.moengage.gradle.android.library.plugin.configs.ConfigurablePluginConfigExtension
import com.moengage.gradle.android.library.plugin.configuration.static.CompileOptionConfiguration
import com.moengage.gradle.android.library.plugin.configuration.static.DebugBuildTypeConfiguration
import com.moengage.gradle.android.library.plugin.configuration.static.KotlinOptionConfiguration
import com.moengage.gradle.android.library.plugin.configuration.static.LintConfiguration
import com.moengage.gradle.android.library.plugin.configuration.static.PublishingConfiguration
import com.moengage.gradle.android.library.plugin.configuration.static.SDKVersionConfiguration
import com.moengage.gradle.android.library.plugin.configuration.static.TestOptionsConfiguration
import com.moengage.gradle.android.library.plugin.utils.PLUGIN_ID_ANDROID_LIBRARY
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plugin that applies a standard Android library configuration for native modules. It wires
 * up the Android and Kotlin plugins, static compile/SDK/publishing/test configurations, and exposes
 * a [ConfigurablePluginConfigExtension] for additional per-module tweaks.
 *
 * This plugin is intended to be applied to Android library projects only.
 *
 * @author Abhishek Kumar
 */
public class AndroidModuleConfigPlugin : Plugin<Project> {

    /** Set of static configuration classes applied to every Android library. */
    private val staticConfiguration =
        listOf(
            CompileOptionConfiguration::class,
            DebugBuildTypeConfiguration::class,
            KotlinOptionConfiguration::class,
            LintConfiguration::class,
            PublishingConfiguration::class,
            SDKVersionConfiguration::class,
            TestOptionsConfiguration::class)

    internal companion object {
        /** Name of the top-level extension used to expose configurable settings. */
        private const val EXTENSION_NAME = "moduleConfig"

        /**
         * Returns an existing [ConfigurablePluginConfigExtension] or creates a new one bound to the
         * target [Project] and its [LibraryExtension].
         */
        private fun Project.configurePluginConfigExtension(): ConfigurablePluginConfigExtension =
            extensions.findByType(ConfigurablePluginConfigExtension::class.java)
                ?: extensions.create(
                    EXTENSION_NAME,
                    ConfigurablePluginConfigExtension::class.java,
                    this,
                    extensions.getByType(LibraryExtension::class.java))
    }

    /**
     * Applies the Android and Kotlin library plugins, registers the
     * [ConfigurablePluginConfigExtension], and runs all static configuration classes against the
     * module's [LibraryExtension].
     */
    override fun apply(project: Project) {
        project.plugins.apply(PLUGIN_ID_ANDROID_LIBRARY)
        project.configurePluginConfigExtension()

        project.extensions.configure(LibraryExtension::class.java) {
            staticConfiguration.forEach { kClass ->
                project.objects.newInstance(kClass.java).apply(project, this)
            }
        }
    }
}
