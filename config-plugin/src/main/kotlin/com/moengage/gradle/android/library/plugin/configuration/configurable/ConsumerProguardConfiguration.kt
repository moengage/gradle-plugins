package com.moengage.gradle.android.library.plugin.configuration.configurable

import com.android.build.api.dsl.LibraryExtension
import com.moengage.gradle.android.library.plugin.utils.DEFAULT_CONSUMER_PROGUARD_FILE
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty

/**
 * Configures consumer ProGuard files for the library module. When enabled, this will add the
 * specified ProGuard rules files to the defaultConfig block as consumer ProGuard files.
 *
 * @author Abhishek Kumar
 */
public abstract class ConsumerProguardConfiguration @Inject constructor() : ConfigurableConfig {

    /** List of consumer ProGuard file names to be applied. */
    public abstract val consumerProguardFilesProperty: ListProperty<String>

    init {
        consumerProguardFilesProperty.convention(listOf(DEFAULT_CONSUMER_PROGUARD_FILE))
    }

    /** Adds a single consumer ProGuard file name. */
    public fun addFile(fileName: String) {
        consumerProguardFilesProperty.add(fileName)
    }

    /** Applies the configured consumer ProGuard files to the given [LibraryExtension]. */
    override fun apply(project: Project, libraryExtension: LibraryExtension) {
        if (consumerProguardFilesProperty.get().isEmpty()) return
        libraryExtension.apply {
            defaultConfig {
                consumerProguardFiles(*consumerProguardFilesProperty.get().toTypedArray())
            }
        }
    }
}
