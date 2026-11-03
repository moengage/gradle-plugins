package com.moengage.gradle.android.library.plugin.configuration

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project

/**
 * Interface defining the contract for applying specific build configurations
 *
 * @author Abhishek Kumar
 */
internal interface Config {

    /** Applies the configuration to the given project and library extension. */
    fun apply(project: Project, libraryExtension: LibraryExtension)
}
