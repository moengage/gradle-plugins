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

import com.intellij.rt.coverage.data.LineData
import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.instrumentation.InstrumentationOptions
import com.intellij.rt.coverage.util.CoverageReport
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.CoverageBuilder
import org.jacoco.core.analysis.ICounter
import org.jacoco.core.tools.ExecFileLoader

/**
 * Gradle task to convert JaCoCo androidTest coverage (.ec files) into Kover's native binary format
 * (.ic) so on-device coverage can be merged into the Kover reports; a line is covered when JaCoCo
 * reports any covered instruction on it.
 *
 * @author Abhishek Kumar
 */
@CacheableTask
internal abstract class AndroidTestCoverageToKoverTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:SkipWhenEmpty
    internal abstract val execFiles: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal abstract val classFiles: ConfigurableFileCollection

    /** Variant the class files belong to; only used to diagnose an empty [classFiles]. */
    @get:Input internal abstract val variant: Property<String>

    @get:OutputFile internal abstract val outputFile: RegularFileProperty

    /**
     * Analyzes the [execFiles] against the [classFiles] with JaCoCo and rewrites the line coverage
     * of every class with at least one covered line into [outputFile] in Kover's binary format;
     * branch data is not carried over.
     */
    @TaskAction
    internal fun convert() {
        if (classFiles.isEmpty) {
            logger.warn(
                "$name: on-device execution data exists, but no class files were found for " +
                    "variant '${variant.get()}' — the converted coverage will be empty. If the " +
                    "project uses product flavors, set testCoverage.variant to the full variant " +
                    "name the connected tests run against (e.g. \"defaultDebug\").",
            )
        }

        val loader = ExecFileLoader()
        execFiles.forEach(loader::load)

        val coverageBuilder = CoverageBuilder()
        val analyzer = Analyzer(loader.executionDataStore, coverageBuilder)
        classFiles.forEach(analyzer::analyzeAll)

        if (coverageBuilder.noMatchClasses.isNotEmpty()) {
            logger.warn(
                "$name: ${coverageBuilder.noMatchClasses.size} classes did not match the " +
                    "on-device execution data (stale build?); their coverage was dropped",
            )
        }

        val projectData = ProjectData()
        coverageBuilder.classes
            .filter { it.lineCounter.coveredCount > 0 }
            .forEach { cls ->
                val classData = projectData.getOrCreateClassData(cls.name.replace('/', '.'))
                val lines = arrayOfNulls<LineData>(cls.lastLine + 1)
                cls.methods
                    .filter { it.firstLine >= 0 }
                    .forEach { method ->
                        val signature = method.name + method.desc
                        for (line in method.firstLine..method.lastLine) {
                            val status = method.getLine(line).status
                            if (status == ICounter.EMPTY) continue
                            val lineData =
                                lines[line] ?: LineData(line, signature).also { lines[line] = it }
                            if (status != ICounter.NOT_COVERED) lineData.hits = 1
                        }
                    }
                classData.setLines(lines)
            }

        val output = outputFile.get().asFile.apply { parentFile.mkdirs() }
        CoverageReport.save(
            projectData,
            InstrumentationOptions.Builder().setDataFile(output).build(),
        )
    }
}
