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

import com.moengage.plugin.test.coverage.internal.model.FilePatchCoverage
import com.moengage.plugin.test.coverage.internal.model.PatchCoverageResult
import com.moengage.plugin.test.coverage.internal.variantSourceSets
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

private val sourcePathPattern =
    Regex(""".+?/src/([^/]+)/(?:java|kotlin)/(.*)/([^/]+\.(?:kt|java))$""")

private val hunkHeaderPattern = Regex("""@@ -\d+(?:,\d+)? \+(\d+)(?:,(\d+))? @@""")

/**
 * Computes changed-lines ("patch") coverage of a PR by intersecting the unified diff with the
 * per-line coverage of the merged Kover XML report.
 *
 * @author Abhishek Kumar
 */
internal object PatchCoverageCalculator {
    /**
     * Computes the coverage of the added/modified lines of [unifiedDiff] from the per-line coverage
     * recorded in [xmlReport]; fails when the report is missing or unreadable. Only files in source
     * sets compiled into [variant] are counted — files in other source sets (test, androidTest,
     * other flavors) are never instrumented and are excluded from the changed-lines calculation.
     */
    internal fun calculate(
        xmlReport: File,
        unifiedDiff: String,
        variant: String,
    ): PatchCoverageResult {
        val reportRoot =
            parseXml(xmlReport) ?: error("merged coverage report missing or unreadable: $xmlReport")

        val files =
            patchCoverage(instrumentedLines(reportRoot), unifiedDiff, variantSourceSets(variant))
        return PatchCoverageResult(
            files = files,
            coveredLines = files.sumOf { it.covered },
            totalLines = files.sumOf { it.total },
        )
    }

    /** Parses the report XML and returns its root element, or null when missing/invalid. */
    private fun parseXml(file: File): Element? =
        if (!file.isFile) {
            null
        } else {
            runCatching {
                    DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder()
                        .parse(file)
                        .documentElement
                }
                .getOrNull()
        }

    /** Direct child elements of this element with the given [tag] name. */
    private fun Element.childElements(tag: String): List<Element> {
        val nodes = childNodes
        return (0 until nodes.length)
            .mapNotNull { nodes.item(it) as? Element }
            .filter { it.tagName == tag }
    }

    /** Map (package path, filename) -> (line nr -> covered) from the merged report. */
    private fun instrumentedLines(
        reportRoot: Element
    ): Map<Pair<String, String>, Map<Int, Boolean>> {
        val lines = mutableMapOf<Pair<String, String>, MutableMap<Int, Boolean>>()
        for (pkg in reportRoot.childElements("package")) {
            for (sourceFile in pkg.childElements("sourcefile")) {
                val key = pkg.getAttribute("name") to sourceFile.getAttribute("name")
                val entry = lines.getOrPut(key) { mutableMapOf() }
                for (line in sourceFile.childElements("line")) {
                    val nr = line.getAttribute("nr").toInt()
                    val covered = line.getAttribute("ci").ifEmpty { "0" }.toInt() > 0
                    // merged reports may repeat a sourcefile; keep "covered" if any copy covers it
                    entry[nr] = (entry[nr] ?: false) || covered
                }
            }
        }
        return lines
    }

    /** Map repo-relative file path -> set of added/modified line numbers in the diff. */
    private fun changedLinesByFile(unifiedDiff: String): Map<String, Set<Int>> {
        val files = mutableMapOf<String, MutableSet<Int>>()
        var current: MutableSet<Int>? = null
        for (line in unifiedDiff.lineSequence()) {
            if (line.startsWith("+++ b/")) {
                current = files.getOrPut(line.removePrefix("+++ b/")) { mutableSetOf() }
            } else if (line.startsWith("@@")) {
                val match = hunkHeaderPattern.find(line) ?: continue
                val start = match.groupValues[1].toInt()
                val count = match.groupValues[2].ifEmpty { "1" }.toInt()
                current?.addAll(start until start + count)
            }
        }
        return files.filterValues { it.isNotEmpty() }
    }

    /**
     * Map a repo path to (package path, filename) as they appear in the report, e.g.
     * core/src/main/java/com/moengage/core/Foo.kt -> ("com/moengage/core", "Foo.kt"). Returns null
     * for files outside instrumented source sets.
     */
    private fun sourceLocation(
        path: String,
        coveredSourceSets: Set<String>,
    ): Pair<String, String>? {
        val match = sourcePathPattern.matchEntire(path) ?: return null
        val (sourceSet, pkg, fileName) = match.destructured
        return if (sourceSet in coveredSourceSets) pkg to fileName else null
    }

    /**
     * Per-file coverage of the changed lines: intersects each file's changed line numbers with the
     * executable lines of the report, skipping files without coverage data and lines that are not
     * coverable (comments, blanks, declarations).
     */
    private fun patchCoverage(
        reportLines: Map<Pair<String, String>, Map<Int, Boolean>>,
        unifiedDiff: String,
        coveredSourceSets: Set<String>,
    ): List<FilePatchCoverage> =
        changedLinesByFile(unifiedDiff).toSortedMap().mapNotNull { (path, changed) ->
            val location = sourceLocation(path, coveredSourceSets) ?: return@mapNotNull null
            val fileLines = reportLines[location] ?: return@mapNotNull null
            val measurable = changed.filter { it in fileLines }.sorted()
            if (measurable.isEmpty()) return@mapNotNull null
            FilePatchCoverage(
                path = path,
                covered = measurable.count { fileLines[it] == true },
                total = measurable.size,
                missedLines = measurable.filter { fileLines[it] == false },
            )
        }
}
