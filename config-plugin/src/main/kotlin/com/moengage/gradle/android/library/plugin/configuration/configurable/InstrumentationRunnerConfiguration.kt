package com.moengage.gradle.android.library.plugin.configuration.configurable

import com.android.build.api.dsl.LibraryExtension
import com.moengage.gradle.android.library.plugin.utils.DEFAULT_ANDROID_RUNNER
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

/**
 * Configures the Android test instrumentation runner for the library module. When enabled, it sets
 * the default instrumentation runner on the defaultConfig block using [DEFAULT_ANDROID_RUNNER].
 *
 * @author Abhishek Kumar
 */
public abstract class InstrumentationRunnerConfiguration
@Inject
constructor(objects: ObjectFactory) : ConfigurableConfig {

    /** Flag indicating whether the default instrumentation runner should be used. */
    public abstract val useDefaultRunner: Property<Boolean>

    init {
        useDefaultRunner.convention(true)
    }

    /** Applies the configured instrumentation runner to the given [LibraryExtension]. */
    override fun apply(project: Project, libraryExtension: LibraryExtension) {
        if (useDefaultRunner.get()) {
            libraryExtension.defaultConfig { testInstrumentationRunner = DEFAULT_ANDROID_RUNNER }
        }
    }
}
