package com.moengage.gradle.android.library.plugin

import com.android.build.api.dsl.LibraryExtension
import com.moengage.gradle.android.library.plugin.configs.HybridConfigurablePluginConfig
import com.moengage.gradle.android.library.plugin.configuration.static.CompileOptionConfiguration
import com.moengage.gradle.android.library.plugin.configuration.static.SDKVersionConfiguration
import com.moengage.gradle.android.library.plugin.utils.PLUGIN_ID_ANDROID_LIBRARY
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plugin that applies a standard Android library configuration for hybrid platform modules
 * (e.g., React Native, Flutter, Cordova wrappers). It wires up the Android and Kotlin plugins,
 * static compile/SDK configurations, and exposes a [HybridConfigurablePluginConfig] for additional
 * per-module tweaks.
 *
 * Configurable options (via hybridModuleConfig DSL):
 * - buildFeature: Enable buildConfig with custom fields
 * - plugins: Additional Gradle plugins
 * - productFlavor: Product flavor configuration
 * - buildTypes: Build type configuration
 * - consumerProguard: Consumer proguard rules
 * - instrumentationRunner: Test instrumentation runner
 *
 * @author Abhishek Kumar
 */
public class HybridModuleConfigPlugin : Plugin<Project> {

    /** Set of static configuration classes applied to every hybrid Android library. */
    private val staticConfiguration =
        listOf(CompileOptionConfiguration::class, SDKVersionConfiguration::class)

    internal companion object {
        /** Name of the top-level extension used to expose configurable settings. */
        private const val EXTENSION_NAME = "hybridModuleConfig"

        /**
         * Returns an existing [HybridConfigurablePluginConfig] or creates a new one bound to the
         * target [Project] and its [LibraryExtension].
         */
        private fun Project.configurePluginConfigExtension(): HybridConfigurablePluginConfig =
            extensions.findByType(HybridConfigurablePluginConfig::class.java)
                ?: extensions.create(
                    EXTENSION_NAME,
                    HybridConfigurablePluginConfig::class.java,
                    this,
                    extensions.getByType(LibraryExtension::class.java))
    }

    /**
     * Applies the Android and Kotlin library plugins, registers the
     * [HybridConfigurablePluginConfig], and runs all static configuration classes against the
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
