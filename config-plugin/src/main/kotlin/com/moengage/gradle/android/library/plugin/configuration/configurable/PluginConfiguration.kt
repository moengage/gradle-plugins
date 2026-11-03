package com.moengage.gradle.android.library.plugin.configuration.configurable

import com.android.build.api.dsl.LibraryExtension
import com.moengage.gradle.android.library.plugin.utils.PLUGIN_ID_DOKKA
import com.moengage.gradle.android.library.plugin.utils.PLUGIN_ID_KOTLIN_SERIALIZATION
import com.moengage.gradle.android.library.plugin.utils.PLUGIN_ID_RELEASE
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.provider.Property

/**
 * Configures optional Gradle plugins for the Android library module. Individual boolean flags
 * control whether specific plugins such as Kotlin serialization, custom release publishing, and
 * Dokka are applied to the target project.
 *
 * @author Abhishek Kumar
 */
public abstract class PluginConfiguration @Inject constructor() : ConfigurableConfig {

    /** Enables the Kotlin serialization Gradle plugin when set to true. */
    public abstract val kotlinSerializationEnabled: Property<Boolean>

    /** Enables the custom release publishing plugin when set to true. */
    public abstract val releasePluginEnabled: Property<Boolean>

    /** Enables the Dokka documentation plugin when set to true. */
    public abstract val dokkaPluginEnabled: Property<Boolean>

    init {
        kotlinSerializationEnabled.convention(true)
        releasePluginEnabled.convention(true)
        dokkaPluginEnabled.convention(true)
    }

    /** Applies the selected plugins to the [Project] after it has been evaluated. */
    override fun apply(project: Project, libraryExtension: LibraryExtension) {
        val pluginMapping =
            mapOf(
                kotlinSerializationEnabled to PLUGIN_ID_KOTLIN_SERIALIZATION,
                releasePluginEnabled to PLUGIN_ID_RELEASE,
                dokkaPluginEnabled to PLUGIN_ID_DOKKA)

        pluginMapping.forEach { (property, pluginId) ->
            if (property.getOrElse(false)) {
                applyPlugin(project, pluginId)
            }
        }
    }

    private fun applyPlugin(project: Project, pluginId: String) {
        project.plugins.apply(pluginId)
    }
}
