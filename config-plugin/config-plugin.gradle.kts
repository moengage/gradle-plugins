plugins { id("project-config") }

testing {
    suites {
        val test by getting(JvmTestSuite::class) { useKotlinTest(libs.versions.kotlin.get()) }
    }
}

kotlin { compilerOptions { freeCompilerArgs.add("-Xexplicit-api=strict") } }

gradlePlugin {
    plugins {
        register("androidModuleConfigPlugin") {
            id = "com.moengage.android.library.config.plugin"
            implementationClass =
                "com.moengage.gradle.android.library.plugin.AndroidModuleConfigPlugin"
            tags.add("Gradle-Config")
        }
        register("hybridModuleConfigPlugin") {
            id = "com.moengage.android.hybrid.module.config.plugin"
            implementationClass =
                "com.moengage.gradle.android.library.plugin.HybridModuleConfigPlugin"
            tags.add("Gradle-Config")
        }
    }
}

dependencies {
    compileOnly(libs.android.tools.build.gradle)
    compileOnly(libs.kotlin.gradle.plugin)
}
