# 01-07-2026

## 2.0.0

- MOEN-44438: Gradle wrapper updated from `8.9` to `9.3.1`
- MOEN-44438: `gradle/libs.versions.toml` updates
  |                       Library / Plugin                      | Current Version     | Next Version       |
  |:-----------------------------------------------------------:|:-------------------:|:------------------:|
  | com.android.tools.build:gradle                              | 8.7.3               | 9.1.1              |
  | org.jetbrains.kotlin:kotlin-gradle-plugin                   | 1.9.23              | 2.3.20             |
  | org.jetbrains.kotlin.jvm (plugin)                           | 1.9.23              | 2.3.20             |
  | com.gradle.plugin-publish (plugin)                          | 1.2.1               | 2.1.1              |
- MOEN-44438: `PublishingConfiguration` update for release variant
- MOEN-44438: JaCoCo version updated from `0.8.8` to `0.8.14`
- MOEN-44438: Removed application of `org.jetbrains.kotlin.android` from native and hybrid config plugins
- MOEN-44438: Removed init template unit, functional tests and suite

# 04-06-2026

## 0.0.5

- Downgrading JDK version to 17 for compatibility with JAVA 17

# 07-04-2026

## 1.0.0

- Support for configuring the module gradle configuration