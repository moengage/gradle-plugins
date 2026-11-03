import com.diffplug.gradle.spotless.BaseKotlinExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.kotlin.KtfmtStep.TrailingCommaManagementStrategy

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

plugins {
    alias(libs.plugins.plugin.spotless) apply false
    alias(libs.plugins.plugin.kotlin.serialization) apply false
}

val ktfmtVersion = libs.versions.ktfmt.get()

allprojects {
    apply(plugin = "com.diffplug.spotless")
    // build-logic is an included build, so it is formatted from the root project.
    val isRoot = this == rootProject
    configure<SpotlessExtension> {
        ratchetFrom = "origin/development"
        kotlin {
            target(if (isRoot) "build-logic/src/**/*.kt" else "src/**/*.kt")
            targetExclude("**/build/**")
            ktfmt(ktfmtVersion).moEngageStyle()
        }
        kotlinGradle {
            if (isRoot) target("*.gradle.kts", "build-logic/**/*.gradle.kts")
            else target("*.gradle.kts")
            targetExclude("**/build/**", "**/.gradle/**")
            ktfmt(ktfmtVersion).moEngageStyle()
        }
    }
}

fun BaseKotlinExtension.KtfmtConfig.moEngageStyle() {
    kotlinlangStyle().configure {
        it.setMaxWidth(100)
        it.setTrailingCommaManagementStrategy(TrailingCommaManagementStrategy.NONE)
    }
}
