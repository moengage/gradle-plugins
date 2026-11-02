![Logo](.github/logo.png)

MoEngage Gradle Plugins
=======================

Multi-module Gradle build hosting MoEngage's Gradle plugins. Each plugin lives in its own module
with its own README, changelog and release cycle.

## Plugins

| Module | Plugin ID | Latest Version | Docs |
|--------|-----------|----------------|------|
| `config-plugin` | `com.moengage.android.library.config.plugin` | [![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/com.moengage.android.library.config.plugin)](https://plugins.gradle.org/plugin/com.moengage.android.library.config.plugin) | [README](config-plugin/README.md) · [CHANGELOG](config-plugin/CHANGELOG.md) |
| `config-plugin` | `com.moengage.android.hybrid.module.config.plugin` | [![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/com.moengage.android.hybrid.module.config.plugin)](https://plugins.gradle.org/plugin/com.moengage.android.hybrid.module.config.plugin) | [README](config-plugin/README.md) · [CHANGELOG](config-plugin/CHANGELOG.md) |
| `maven-publish-plugin` | `com.moengage.plugin.maven.publish` | [![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/com.moengage.plugin.maven.publish)](https://plugins.gradle.org/plugin/com.moengage.plugin.maven.publish) | [README](maven-publish-plugin/README.md) · [CHANGELOG](maven-publish-plugin/CHANGELOG.md) |
| `test-coverage-plugin` | `com.moengage.plugin.test.coverage` | [![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/com.moengage.plugin.test.coverage)](https://plugins.gradle.org/plugin/com.moengage.plugin.test.coverage) | [README](test-coverage-plugin/README.md) · [CHANGELOG](test-coverage-plugin/CHANGELOG.md) |

### [Config Plugin](config-plugin/README.md)

Standardises Android, Kotlin, lint, test, build type and publishing configuration for native
Android library modules, and Android/Kotlin configuration for hybrid platform (React Native,
Flutter, Cordova) wrapper modules, through a declarative `moduleConfig` / `hybridModuleConfig` DSL.

### [Maven Publish Plugin](maven-publish-plugin/README.md)

Publishes Android, Kotlin, Java libraries and version catalogs to
[Maven Central](https://central.sonatype.com/) and automatically closes and releases the uploaded
repository, so no manual step on the web portal is needed.

### [Test Coverage Plugin](test-coverage-plugin/README.md)

Aggregated test coverage for multi-module Android builds. Wires
[Kover](https://github.com/Kotlin/kotlinx-kover) onto every module, merges unit-test and on-device
(androidTest) coverage into a single report, and gates PRs on changed-lines coverage.

See the module READMEs for integration, configuration, and usage details.

## Project Structure

```
gradle-plugins/
├── build.gradle.kts             # Root build: common config for all modules
├── settings.gradle.kts          # Module includes and build-script naming convention
├── gradle/                      # Version catalog and Gradle wrapper
├── config-plugin/               # Config plugins module (com.moengage:plugin)
├── maven-publish-plugin/        # Maven publish plugin module (com.moengage:maven-publish-plugin)
└── test-coverage-plugin/        # Test coverage plugin module (com.moengage:test-coverage-plugin)
```

## Building

```
./gradlew build                                        # build + checks
./gradlew :<module>:assemble                           # build a single module
./gradlew :<module>:spotlessCheck                      # formatting check for a single module
./gradlew :<module>:publishToMavenLocal                # publish a module locally for testing
```

## Releasing

All modules are released through a single [Publish Plugin](.github/workflows/publish.yml) workflow.
Run it from the Actions tab and select the `module`, the release type (`major` / `minor` / `patch`)
and the release ticket. The workflow bumps `VERSION_NAME` in the module's `gradle.properties`,
updates the module's `CHANGELOG.md`, publishes the module to the Gradle Plugin Portal and tags the
release as `<module>-v<version>`.

When adding a new module, add it to the `module` options in the workflow.
