#!/usr/bin/env kotlin

@file:Import("utils.main.kts")

import java.io.File

/**
 * Finds the Gradle modules affected by the current pull request and writes them as a JSON array
 * to the GITHUB_OUTPUT variable `modules` for use in a workflow matrix.
 *
 * - Modules are discovered from settings.gradle.kts, so no workflow change is needed when a new
 *   module is added.
 * - A change to any file outside a module directory (root build scripts, version catalog,
 *   gradle wrapper etc.) triggers verification of all modules.
 * - When no base ref is available (manual workflow_dispatch run) all modules are verified.
 */

val moduleRegex = Regex("\"(:[A-Za-z0-9:_.-]+)\"")
val modules = moduleRegex.findAll(File("settings.gradle.kts").readText())
    .map { it.groupValues[1].removePrefix(":").replace(':', '/') }
    .toList()

if (modules.isEmpty()) {
    throw IllegalStateException("No modules found in settings.gradle.kts")
}
println("Modules in project: $modules")

val baseRef = System.getenv("BASE_REF").orEmpty()

val changedModules: List<String> = if (baseRef.isBlank()) {
    println("No base ref available, verifying all modules")
    modules
} else {
    val changedFiles = executeCommandForOutput("git diff --name-only origin/$baseRef...HEAD")
        .lines()
        .filter { it.isNotBlank() }
    println("Changed files:\n${changedFiles.joinToString("\n")}")
    val hasNonModuleChange = changedFiles.any { file -> modules.none { file.startsWith("$it/") } }
    if (hasNonModuleChange) {
        println("Files outside module directories changed, verifying all modules")
        modules
    } else {
        modules.filter { module -> changedFiles.any { it.startsWith("$module/") } }
    }
}

val modulesJson = changedModules.joinToString(separator = ",", prefix = "[", postfix = "]") { "\"$it\"" }
println("Modules to verify: $modulesJson")
File(System.getenv("GITHUB_OUTPUT")).appendText("modules=$modulesJson\n")
