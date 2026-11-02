package com.moengage.gradle.android.library.plugin.configs

import com.android.build.api.dsl.LibraryExtension
import com.moengage.gradle.android.library.plugin.configuration.configurable.KotlinOptionConfiguration
import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory

/**
 * Extension of [ConfigurablePluginConfigExtension] with additional params which can be applied from
 * the hybrid platforms
 *
 * @author Abhishek Kumar
 */
public open class HybridConfigurablePluginConfig
@Inject
constructor(objects: ObjectFactory, project: Project, libraryExtension: LibraryExtension) :
    ConfigurablePluginConfigExtension(objects, project, libraryExtension) {

    private val kotlinOptionConfiguration =
        objects.newInstance(KotlinOptionConfiguration::class.java)

    public fun configurePlugin(action: Action<HybridConfigurablePluginConfig>) {
        action.execute(this)
        kotlinOptionConfiguration.apply(project, libraryExtension)
        buildFeatureConfiguration.apply(project, libraryExtension)
        instrumentationRunnerConfiguration.apply(project, libraryExtension)
        buildTypeConfiguration.apply(project, libraryExtension)
    }

    public fun kotlinOptions(action: Action<KotlinOptionConfiguration>) {
        action.execute(kotlinOptionConfiguration)
    }
}
