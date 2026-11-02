package com.moengage.gradle.android.library.plugin.configuration.configurable

import com.android.build.api.dsl.LibraryExtension
import com.moengage.gradle.android.library.plugin.configs.BuildTypeConfig
import com.moengage.gradle.android.library.plugin.configs.BuildTypeKind
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty

/**
 * Configures Android build types (e.g. debug, release) for the library module based on a list of
 * [BuildTypeConfig] entries.
 *
 * @author Abhishek Kumar
 */
public abstract class BuildTypeConfiguration @Inject constructor() : ConfigurableConfig {

    /** List of build type configurations to apply to the Android block. */
    public abstract val buildTypes: ListProperty<BuildTypeConfig>

    init {
        buildTypes.convention(
            listOf(BuildTypeConfig(name = "release", kind = BuildTypeKind.REGISTER)))
    }

    /** Applies the configured build types to the given [LibraryExtension]. */
    override fun apply(project: Project, libraryExtension: LibraryExtension) {
        libraryExtension.buildTypes {
            buildTypes.get().forEach { config ->
                val buildType =
                    when (config.kind) {
                        BuildTypeKind.REGISTER -> maybeCreate(config.name)
                        BuildTypeKind.CREATE -> create(config.name)
                    }
                buildType.isMinifyEnabled = config.isMinifyEnabled
                buildType.isShrinkResources = config.isShrinkResources
                if (config.proguardFiles.isNotEmpty()) {
                    buildType.proguardFiles(
                        config.defaultAndroidProguardFile, *config.proguardFiles.toTypedArray())
                }
            }
        }
    }
}
