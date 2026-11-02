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

package com.moengage.plugin.test.coverage.internal.verification

import com.moengage.plugin.test.coverage.TestCoverageExtension
import com.moengage.plugin.test.coverage.internal.MERGED_XML_REPORT_PATH
import com.moengage.plugin.test.coverage.internal.XML_REPORT_TASK_NAME
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Gradle task to verify the coverage of the lines added/modified in a PR. The unified diff is
 * supplied as a patch file (`-PpatchFile=...`), the coverage of those lines is read from the merged
 * Kover XML report of the current build, and the task fails when the changed-lines coverage is
 * below [minChangedLinesCoverage] (default 80%).
 *
 * @author Abhishek Kumar
 */
@UntrackedTask(because = "gates a CI-provided patch file, not a build-produced input")
internal abstract class PrCoverageCheckTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    internal abstract val patchFile: RegularFileProperty

    @get:Input internal abstract val minChangedLinesCoverage: Property<Double>

    @get:Input internal abstract val variant: Property<String>

    @get:Internal internal abstract val xmlReport: RegularFileProperty

    /**
     * Wires the task inputs: the patch file from the `patchFile` Gradle property, the threshold
     * from [TestCoverageExtension.minChangedLinesCoverage], and the merged Kover XML report
     * produced by the `koverXmlReport` task this task depends on.
     */
    internal fun configureFrom(
        rootProject: Project,
        extension: TestCoverageExtension,
    ) {
        patchFile.set(
            rootProject.providers.gradleProperty("patchFile").map {
                rootProject.layout.projectDirectory.file(it)
            },
        )
        minChangedLinesCoverage.set(extension.minChangedLinesCoverage)
        variant.set(extension.variant)

        xmlReport.set(rootProject.layout.buildDirectory.file(MERGED_XML_REPORT_PATH))
        dependsOn(rootProject.tasks.named(XML_REPORT_TASK_NAME))
    }

    /**
     * Computes the changed-lines coverage of the patch and logs it; passes when no coverable lines
     * changed, fails the build when the percentage is below [minChangedLinesCoverage].
     */
    @TaskAction
    internal fun verify() {
        val result =
            PatchCoverageCalculator.calculate(
                xmlReport = xmlReport.get().asFile,
                unifiedDiff = patchFile.get().asFile.readText(),
                variant = variant.get(),
            )

        val percentage = result.percentage
        if (percentage == null) {
            logger.lifecycle(
                "Changed-lines coverage: n/a — no coverable lines changed, gate passed")
            return
        }

        val threshold = minChangedLinesCoverage.get()
        val summary =
            "Changed-lines coverage: ${"%.1f%%".format(percentage)} " +
                "(${result.coveredLines}/${result.totalLines} covered), required ≥ ${"%.0f%%".format(threshold)}"
        if (percentage < threshold) {
            throw GradleException(summary)
        }
        logger.lifecycle(summary)
    }
}
