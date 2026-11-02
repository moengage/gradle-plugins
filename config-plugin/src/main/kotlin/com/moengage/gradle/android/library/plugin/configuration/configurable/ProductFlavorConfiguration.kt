package com.moengage.gradle.android.library.plugin.configuration.configurable

import com.android.build.api.dsl.LibraryExtension
import com.moengage.gradle.android.library.plugin.configs.FlavorConfig
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

/**
 * Configures Android product flavors for the library module. Flavors are collected via [flavor]
 * calls and applied under a single flavor dimension specified by [dimension].
 *
 * @author Abhishek Kumar
 */
public abstract class ProductFlavorConfiguration @Inject constructor(objects: ObjectFactory) :
    ConfigurableConfig {

    /** Internal list of flavor configurations to be registered. */
    private val flavors: ListProperty<FlavorConfig> =
        objects.listProperty(FlavorConfig::class.java).convention(emptyList())

    /** Name of the flavor dimension to which all configured flavors belong. */
    public abstract val dimension: Property<String>

    /**
     * Adds a single flavor configuration with optional [minSdk] and [matchingFallback] to the
     * internal list.
     */
    public fun flavor(name: String, minSdk: Int? = null, matchingFallback: String? = null) {
        flavors.add(FlavorConfig(name, minSdk, matchingFallback))
    }

    init {
        dimension.convention("")
    }

    /** Applies the configured flavors and dimension to the given [LibraryExtension]. */
    override fun apply(project: Project, libraryExtension: LibraryExtension) {
        if (dimension.get().isBlank() || flavors.get().isEmpty()) return
        with(libraryExtension) {
            flavorDimensions.add(dimension.get())
            productFlavors {
                flavors.get().forEach { flavorConfig ->
                    register(flavorConfig.name) {
                        if (flavorConfig.minSdk != null) {
                            minSdk = flavorConfig.minSdk
                        }
                        flavorConfig.matchingFallback?.let { matchingFallbacks.add(it) }
                    }
                }
            }
        }
    }
}
