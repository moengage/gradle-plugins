package com.moengage.gradle.android.library.plugin.configuration.configurable

import com.android.build.api.dsl.LibraryExtension
import com.moengage.gradle.android.library.plugin.configs.BuildConfigField
import com.moengage.gradle.android.library.plugin.configs.BuildConfigType
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty

/**
 * Configures Android BuildConfig generation and fields for the library module. When enabled, this
 * will turn on the `buildConfig` feature and add the configured fields to the defaultConfig block.
 *
 * @author Abhishek Kumar
 */
public abstract class BuildFeatureConfiguration @Inject constructor() : ConfigurableConfig {

    /** List of BuildConfig fields that should be generated. */
    public abstract val buildConfigFields: ListProperty<BuildConfigField>

    init {
        buildConfigFields.convention(emptyList())
    }

    /** Adds a single BuildConfig field definition to be generated. */
    public fun buildConfigField(type: BuildConfigType, name: String, value: String) {
        buildConfigFields.add(BuildConfigField(type, name, value))
    }

    /** Applies the configured BuildConfig settings to the given [LibraryExtension]. */
    override fun apply(project: Project, libraryExtension: LibraryExtension) {
        if (buildConfigFields.get().isEmpty()) return

        libraryExtension.apply {
            buildFeatures.buildConfig = true

            defaultConfig {
                buildConfigFields.get().forEach { field ->
                    buildConfigField(field.type.value, field.name, field.value)
                }
            }
        }
    }
}
