![Logo](../.github/logo.png)

MoEngage Test Coverage Plugin
=============================

| Plugin ID | Latest Version |
|-----------|----------------|
| `com.moengage.plugin.test.coverage` | [![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/com.moengage.plugin.test.coverage)](https://plugins.gradle.org/plugin/com.moengage.plugin.test.coverage) |

Gradle plugin for aggregated test coverage of multi-module Android SDK builds. It wires
[Kover](https://github.com/Kotlin/kotlinx-kover) onto every module, merges unit-test and
on-device (androidTest) coverage into a single report, and can verify that the lines
added/modified in a PR meet a minimum coverage threshold.

## Features

* **Merged coverage reports** — `koverHtmlReport` / `koverXmlReport` on the root project
  aggregate all modules. Unit tests are measured by Kover's own engine; on-device
  (androidTest) JaCoCo coverage produced by AGP is converted into Kover's format and
  merged into every report.
* **PR changed-lines coverage gate** — `verifyPrCoverage` computes the coverage of only
  the lines a PR adds or modifies and fails the build when it is below the configured
  threshold (default 80%).

## Plugin Integration

Apply the plugin to the **root project** of the multi-module build:

```kotlin
plugins {
    id("com.moengage.plugin.test.coverage").version("0.0.1")
}
```

## Configuration

Optional configuration via the `testCoverage` extension on the root project:

```kotlin
testCoverage {
    // Modules excluded from the merged report and the PR gate
    excludedModules.set(listOf("mock-helper", "sampleapp"))
    // Minimum coverage (in percent) of the changed lines, default 80.0
    minChangedLinesCoverage.set(80.0)
    // Build variant measured by coverage, default "debug"
    variant.set("defaultDebug")
}
```

> **Note:** When the modules use product flavors, `variant` **must** be set to the full
> variant name the connected tests run against (e.g. `defaultDebug` for flavor `default`);
> with the bare `debug` default no class files match and the on-device coverage converts
> to an empty report.

## Generating Coverage Reports

```
./gradlew -Pcoverage testDebugUnitTest connectedDebugAndroidTest koverHtmlReport
```

* `-Pcoverage` opts androidTest builds into coverage instrumentation (skipped otherwise,
  since instrumentation slows on-device builds).
* The merged report is written to `build/reports/kover/` on the root project; every module
  also gets its own report under `<module>/build/reports/kover/`.

## Verifying PR Coverage

The task takes the PR's unified diff as a patch file and checks the coverage of the
added/modified lines against the merged report of the current build:

```
git diff --unified=0 origin/$BASE_BRANCH...HEAD > pr.patch
./gradlew -Pcoverage -PpatchFile=pr.patch connectedDebugAndroidTest verifyPrCoverage
```

The build fails with the current percentage in the error message when the changed-lines
coverage is below the threshold, e.g.

```
Changed-lines coverage: 65.0% (13/20 covered), required ≥ 80%
```

Lines that cannot be covered — comments, blank lines, declarations, and files outside
instrumented source sets (test/androidTest code, excluded modules) — are ignored, so they
never count against the percentage. A PR with no coverable changes passes automatically.

### Reusing coverage data from earlier test runs

With `-PskipTestExecution`, test tasks are skipped and reports are built from coverage data
already present in the build directories. This lets a CI coverage job reuse the results of
earlier test jobs instead of re-running the tests: restore the unit-test Kover binary
reports (`<module>/build/kover/bin-reports/*.ic`) and the on-device JaCoCo execution data
(`<module>/build/outputs/code_coverage/**/*.ec`, produced when the tests ran with
`-Pcoverage`) into the checkout, then run

```
./gradlew -Pcoverage -PskipTestExecution -PpatchFile=pr.patch verifyPrCoverage
```

The classes must be compiled from the same revision (and toolchain) that produced the
coverage data — the on-device converter drops coverage of classes whose bytecode does not
match the execution data, logging a warning when that happens.

## Limitations

* On-device coverage conversion is line-granular — branch coverage in the reports reflects
  unit tests only.
* The patch file must be generated with `--unified=0` against the exact revision being
  built; a stale patch attributes coverage to shifted line numbers.
* Sources are mapped to coverage data via the standard `src/<sourceSet>/java|kotlin/<package>`
  layout; files in non-standard locations are skipped by the PR gate.
* `verifyPrCoverage` relies on the tests having run in the same build (or their outputs
  being restored from cache) — it does not run tests by itself.
