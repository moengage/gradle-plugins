package com.moengage.gradle.android.library.plugin.configuration.static

import com.android.build.api.dsl.LibraryExtension
import com.moengage.gradle.android.library.plugin.utils.ANDROID_BUILD_CONFIG_TARGET_SDK_VERSION
import com.moengage.gradle.android.library.plugin.utils.JACOCO_VERSION
import com.moengage.gradle.android.library.plugin.utils.PLUGIN_ID_JACOCO
import org.gradle.api.Project
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension

/**
 * Configures common test options for Android library modules, including target SDK for tests, JUnit
 * platform usage, and Jacoco coverage settings when the Jacoco plugin is present.
 *
 * @author Abhishek Kumar
 */
@Suppress("UnstableApiUsage")
internal abstract class TestOptionsConfiguration : StaticConfig {

    override fun apply(project: Project, libraryExtension: LibraryExtension) {
        with(libraryExtension) {
            testOptions {
                targetSdk = ANDROID_BUILD_CONFIG_TARGET_SDK_VERSION
                unitTests {
                    isIncludeAndroidResources = true
                    all { testTask ->
                        testTask.useJUnitPlatform()
                        if (project.plugins.hasPlugin(PLUGIN_ID_JACOCO)) {
                            testTask.extensions.configure<JacocoTaskExtension>(PLUGIN_ID_JACOCO) {
                                isIncludeNoLocationClasses = true
                                excludes = listOf("jdk.internal.*")
                            }
                        }
                    }
                }
            }

            if (project.plugins.hasPlugin(PLUGIN_ID_JACOCO)) {
                testCoverage.jacocoVersion = JACOCO_VERSION
            }
        }
    }
}
