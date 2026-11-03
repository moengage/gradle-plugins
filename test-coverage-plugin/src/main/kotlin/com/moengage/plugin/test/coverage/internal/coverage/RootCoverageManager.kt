/*
 * Copyright (c) 2026 MoEngage, Inc.
 * All rights reserved.
 * Use of source code or binaries contained within MoEngage's SDKs is permitted only to enable use of the MoEngage platform by customers of MoEngage. The Licensee may not:
 * - permit any third party to use the Software;
 * - modify or translate the Software except as otherwise permitted;
 * - reverse engineer, decompile, or disassemble the Software;
 * - copy the Software, except as expressly provided above; or
 * - remove or obscure any proprietary rights notices or labels on the Software.
 * - Licensee may not transfer the Software or any rights under this Agreement without the Licensor's prior written consent.
 * - MoEngage owns the Software and all intellectual property rights embodied therein, including copyrights and valuable trade secrets embodied in the Software. The Licensee shall not alter or remove this copyright notice.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE USER HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.moengage.plugin.test.coverage.internal.coverage

import com.android.build.api.dsl.LibraryExtension
import com.moengage.plugin.test.coverage.TestCoverageExtension
import com.moengage.plugin.test.coverage.internal.CONVERTED_ANDROID_COVERAGE_PATH
import com.moengage.plugin.test.coverage.internal.CONVERT_TASK_NAME
import com.moengage.plugin.test.coverage.internal.COVERAGE_CLASS_EXCLUDES
import com.moengage.plugin.test.coverage.internal.COVERAGE_PROPERTY
import com.moengage.plugin.test.coverage.internal.SKIP_TEST_EXECUTION_PROPERTY
import com.moengage.plugin.test.coverage.internal.coverageClassIncludes
import com.moengage.plugin.test.coverage.internal.variantBuildType
import kotlinx.kover.gradle.plugin.KoverGradlePlugin
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension

/**
 * Wires the merged coverage setup onto the root project and every module: applies Kover everywhere,
 * converts on-device (androidTest) JaCoCo coverage into Kover format via
 * [AndroidTestCoverageToKoverTask], and aggregates all non-excluded modules into the merged root
 * report.
 *
 * @author Abhishek Kumar
 */
internal class RootCoverageManager(
    private val rootProject: Project,
    private val extension: TestCoverageExtension,
) {
    /** Applies Kover to the root project and every module, and sets up the merged report. */
    internal fun configure() {
        rootProject.pluginManager.apply(KoverGradlePlugin::class.java)
        rootProject.subprojects.forEach { subproject ->
            subproject.pluginManager.apply(KoverGradlePlugin::class.java)
            subproject.plugins.withId("com.android.library") { configureAndroidLibrary(subproject) }
        }

        // With -PskipTestExecution, reports are built from coverage data already present in the
        // build directories (e.g. restored from CI artifacts of earlier test jobs) instead of
        // re-running the tests; skipped test tasks keep the Kover task graph intact.
        if (rootProject.hasProperty(SKIP_TEST_EXECUTION_PROPERTY)) {
            rootProject.allprojects.forEach { project ->
                project.tasks.withType(Test::class.java).configureEach { onlyIf { false } }
            }
        }

        configureMergedReport()
    }

    /**
     * Enables androidTest coverage on the module (opt-in with -Pcoverage), registers its .ec -> .ic
     * conversion task, and merges the converted coverage into its Kover reports.
     */
    private fun configureAndroidLibrary(module: Project) {
        // Coverage instrumentation slows androidTest builds; opt in with -Pcoverage.
        // matching {} instead of getByName: custom build types may not be declared yet
        // when the android plugin is applied.
        val coverageBuildType = variantBuildType(extension.variant.get())
        module.extensions.configure(LibraryExtension::class.java) {
            buildTypes
                .matching { it.name == coverageBuildType }
                .configureEach { enableAndroidTestCoverage = module.hasProperty(COVERAGE_PROPERTY) }
        }

        // AGP applies the Gradle JaCoCo plugin when coverage is enabled, which attaches a
        // JaCoCo agent to unit-test JVMs; Kover instruments unit tests with its own agent,
        // so keep JaCoCo off unit-test tasks.
        module.plugins.withId("jacoco") {
            module.tasks.withType(Test::class.java).configureEach {
                extensions.findByType(JacocoTaskExtension::class.java)?.isEnabled = false
            }
        }

        val connectedTestTasks =
            module.tasks.matching {
                it.name.startsWith("connected") && it.name.endsWith("AndroidTest")
            }

        // The converter analyzes .ec files against the configured variant's class files;
        // other variants' .ec files would not match and are effectively ignored.
        val convertTask =
            module.tasks.register(
                CONVERT_TASK_NAME,
                AndroidTestCoverageToKoverTask::class.java,
            )
        convertTask.configure {
            execFiles.from(
                module
                    .fileTree(module.layout.buildDirectory.dir("outputs/code_coverage"))
                    .include("**/*.ec"),
            )
            classFiles.from(
                module
                    .fileTree(module.layout.buildDirectory)
                    .include(coverageClassIncludes(extension.variant.get()))
                    .exclude(COVERAGE_CLASS_EXCLUDES),
            )
            variant.set(extension.variant)
            outputFile.set(
                module.layout.buildDirectory.file(CONVERTED_ANDROID_COVERAGE_PATH),
            )

            // Ordering only (no dependsOn): read .ec files and class files if the
            // producing tasks are already in the task graph, without forcing them to run.
            mustRunAfter(connectedTestTasks)
            mustRunAfter(module.tasks.withType(JavaCompile::class.java))
            mustRunAfter(
                module.tasks.matching {
                    it.name.startsWith("compile") && it.name.contains("Kotlin")
                },
            )
        }

        // Merge this module's converted androidTest coverage into its Kover reports
        module.extensions.configure(KoverProjectExtension::class.java) {
            reports.total.additionalBinaryReports.add(
                convertTask.flatMap { it.outputFile }.map { it.asFile },
            )
        }

        // Reports read the converted androidTest coverage, so conversion (and the
        // on-device tests feeding it) must finish first when both are requested.
        listOf(module.tasks, rootProject.tasks).forEach { taskContainer ->
            taskContainer
                .matching { it.name.startsWith("kover") }
                .configureEach {
                    dependsOn(convertTask)
                    mustRunAfter(connectedTestTasks)
                }
        }
    }

    /** Merged (root) report: aggregates every non-excluded module. */
    private fun configureMergedReport() {
        rootProject.extensions.configure(KoverProjectExtension::class.java) {
            reports.total.additionalBinaryReports.addAll(
                rootProject.provider {
                    aggregatedModules().mapNotNull { module ->
                        module.layout.buildDirectory
                            .file(CONVERTED_ANDROID_COVERAGE_PATH)
                            .get()
                            .asFile
                            .takeIf { it.exists() }
                    }
                },
            )
        }

        // The excluded-module list is configured in the consuming build script, so the
        // aggregation dependencies can only be added after that script has been evaluated.
        rootProject.afterEvaluate {
            aggregatedModules().forEach { module -> rootProject.dependencies.add("kover", module) }
        }
    }

    /**
     * Modules included in the merged report, i.e. all but [TestCoverageExtension.excludedModules].
     */
    private fun aggregatedModules(): List<Project> =
        rootProject.subprojects.filter { it.name !in extension.excludedModules.get() }
}
