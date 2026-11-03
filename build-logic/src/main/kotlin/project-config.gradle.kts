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

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Common configuration for the Gradle plugin modules published to the Gradle Plugin Portal.
 *
 * Reads GROUP, POM_URL & SCM_URL from the root gradle.properties and VERSION_NAME, NAME &
 * DESCRIPTION from the module's gradle.properties. Modules only need to register their plugins and
 * declare their dependencies.
 */
plugins {
    `java-gradle-plugin`
    id("org.gradle.kotlin.kotlin-dsl")
    `maven-publish`
    signing
    id("org.jetbrains.kotlin.jvm")
    id("com.gradle.plugin-publish")
}

group = project.findProperty("GROUP") as String

version = project.findProperty("VERSION_NAME") as String

// Pin the bytecode level so the published jar doesn't depend on the JDK running the build.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_17 } }

gradlePlugin {
    website = project.findProperty("POM_URL") as String
    vcsUrl = project.findProperty("SCM_URL") as String

    plugins.configureEach {
        displayName = project.findProperty("NAME") as String
        description = project.findProperty("DESCRIPTION") as String
        tags.add("MoEngage")
    }
}

signing {
    // Keys are only available on CI, signing tasks fail only when actually executed without them.
    useInMemoryPgpKeys(
        project.findProperty("signingInMemoryKeyId") as String?,
        project.findProperty("signingInMemoryKey") as String?,
        project.findProperty("signingInMemoryKeyPassword") as String?)
    sign(publishing.publications)
}
