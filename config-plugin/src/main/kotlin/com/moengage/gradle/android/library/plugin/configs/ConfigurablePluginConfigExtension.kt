package com.moengage.gradle.android.library.plugin.configs

import com.android.build.api.dsl.LibraryExtension
import com.moengage.gradle.android.library.plugin.configuration.configurable.BuildFeatureConfiguration
import com.moengage.gradle.android.library.plugin.configuration.configurable.BuildTypeConfiguration
import com.moengage.gradle.android.library.plugin.configuration.configurable.ConsumerProguardConfiguration
import com.moengage.gradle.android.library.plugin.configuration.configurable.InstrumentationRunnerConfiguration
import com.moengage.gradle.android.library.plugin.configuration.configurable.PluginConfiguration
import com.moengage.gradle.android.library.plugin.configuration.configurable.ProductFlavorConfiguration
import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory

/**
 * Central extension entry point exposing configurable Android and plugin settings.
 *
 * Configurations are collected via DSL and applied together when the [configure] block exits.
 *
 * @author Abhishek Kumar
 */
public open class ConfigurablePluginConfigExtension
@Inject
constructor(
    objects: ObjectFactory,
    public val project: Project,
    public val libraryExtension: LibraryExtension
) {

    internal val pluginConfiguration = objects.newInstance(PluginConfiguration::class.java)
    internal val productFlavorConfiguration =
        objects.newInstance(ProductFlavorConfiguration::class.java)
    internal val buildFeatureConfiguration =
        objects.newInstance(BuildFeatureConfiguration::class.java)
    internal val instrumentationRunnerConfiguration =
        objects.newInstance(InstrumentationRunnerConfiguration::class.java)
    internal val buildTypeConfiguration = objects.newInstance(BuildTypeConfiguration::class.java)
    internal val consumerProguardConfiguration =
        objects.newInstance(ConsumerProguardConfiguration::class.java)

    /**
     * Entry point for the DSL. Executes the provided [action] and applies all configurations
     * immediately after the block exits.
     */
    public fun configure(action: Action<ConfigurablePluginConfigExtension>) {
        action.execute(this)
        applyConfigurations()
    }

    /** Internal helper to apply all gathered configurations to the project. */
    private fun applyConfigurations() {
        pluginConfiguration.apply(project, libraryExtension)
        productFlavorConfiguration.apply(project, libraryExtension)
        buildFeatureConfiguration.apply(project, libraryExtension)
        instrumentationRunnerConfiguration.apply(project, libraryExtension)
        buildTypeConfiguration.apply(project, libraryExtension)
        consumerProguardConfiguration.apply(project, libraryExtension)
    }

    public fun plugins(action: Action<PluginConfiguration>) {
        action.execute(pluginConfiguration)
    }

    public fun productFlavor(action: Action<ProductFlavorConfiguration>) {
        action.execute(productFlavorConfiguration)
    }

    public fun buildFeature(action: Action<BuildFeatureConfiguration>) {
        action.execute(buildFeatureConfiguration)
    }

    public fun instrumentationRunner(action: Action<InstrumentationRunnerConfiguration>) {
        action.execute(instrumentationRunnerConfiguration)
    }

    public fun buildTypes(action: Action<BuildTypeConfiguration>) {
        action.execute(buildTypeConfiguration)
    }

    public fun consumerProguard(action: Action<ConsumerProguardConfiguration>) {
        action.execute(consumerProguardConfiguration)
    }
}
