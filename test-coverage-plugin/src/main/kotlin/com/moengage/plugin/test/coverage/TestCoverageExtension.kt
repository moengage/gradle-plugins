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

import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

/**
 * Configuration for the MoEngage test coverage plugin, registered as `testCoverage` on the root
 * project.
 *
 * ```
 * testCoverage {
 *     excludedModules.set(listOf("mock-helper", "sampleapp"))
 *     minChangedLinesCoverage.set(80.0)
 *     variant.set("defaultDebug")
 * }
 * ```
 *
 * @author Abhishek Kumar
 */
public abstract class TestCoverageExtension {
    /**
     * Module names excluded from the merged (root) coverage report. Every other module is
     * aggregated automatically.
     */
    public abstract val excludedModules: SetProperty<String>

    /**
     * Build variant measured by coverage, e.g. `debug` or `defaultDebug` for a `default` flavor.
     * Its build type gets androidTest coverage instrumentation and its class files feed the
     * converted on-device coverage; other variants are not instrumented. Default: `debug`.
     */
    public abstract val variant: Property<String>

    /**
     * Threshold (in percent) for the changed-lines gate of the [verifyPrCoverage] task. The task
     * fails when the coverage of the PR's added/modified lines is below this value. Default: 80.0.
     */
    public abstract val minChangedLinesCoverage: Property<Double>
}
