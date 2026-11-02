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

package com.moengage.plugin.test.coverage

import com.moengage.plugin.test.coverage.internal.PR_COVERAGE_TASK_NAME
import com.moengage.plugin.test.coverage.internal.coverage.RootCoverageManager
import com.moengage.plugin.test.coverage.internal.verification.PrCoverageCheckTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin class for branch coverage and pr-coverage (patch based) gradle task
 *
 * @author Abhishek Kumar
 */
public class TestCoveragePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        require(project == project.rootProject) {
            "com.moengage.plugin.test.coverage must be applied to the root project"
        }

        val extension = project.extensions.create("testCoverage", TestCoverageExtension::class.java)
        extension.minChangedLinesCoverage.convention(80.0)
        extension.variant.convention("debug")

        RootCoverageManager(project, extension).configure()

        project.tasks.register(PR_COVERAGE_TASK_NAME, PrCoverageCheckTask::class.java).configure {
            group = "verification"
            description =
                "Verifies coverage of the lines changed in the supplied patch (-PpatchFile=...)"
            configureFrom(project, extension)
        }
    }
}
