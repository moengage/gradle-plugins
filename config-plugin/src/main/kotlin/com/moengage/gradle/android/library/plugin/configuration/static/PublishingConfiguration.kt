package com.moengage.gradle.android.library.plugin.configuration.static

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project

/** Gradle property used by [com.moengage.plugin.maven.publish] and this configuration. */
private const val RELEASE_VARIANT_PROPERTY = "RELEASE_VARIANT"

/** Default when [RELEASE_VARIANT_PROPERTY] is unset (e.g. plugin-base, segment). */
private const val DEFAULT_PUBLISH_VARIANT = "release"

/**
 * Configures Maven publishing for Android library modules by enabling a single publishable variant
 * with an attached sources JAR.
 *
 * The variant name comes from [RELEASE_VARIANT_PROPERTY] when set (e.g. `defaultRelease` for
 * flavored native SDK modules), otherwise [DEFAULT_PUBLISH_VARIANT].
 *
 * @author Abhishek Kumar
 */
internal abstract class PublishingConfiguration : StaticConfig {

    override fun apply(project: Project, libraryExtension: LibraryExtension) {
        val publishVariant =
            project.findProperty(RELEASE_VARIANT_PROPERTY) as? String ?: DEFAULT_PUBLISH_VARIANT
        with(libraryExtension) { publishing { singleVariant(publishVariant) { withSourcesJar() } } }
    }
}
