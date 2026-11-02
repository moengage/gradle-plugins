# Release Date

## Release Version

- Module moved from [gradle-maven-publish-plugin](https://github.com/moengage/gradle-maven-publish-plugin) to the `maven-publish-plugin` module of this repository.
- Gradle wrapper updated from `8.6` to `9.3.1`, Kotlin from `2.0.0` to `2.3.20` and `com.gradle.plugin-publish` from `1.2.1` to `2.1.1`.

# 4-3-2025

## 1.1.0
- Improve logging message for missing configuration.
- Support for snapshot builds in Central Portal.

# 18-10-2024

## 1.0.0
- Merging the credentials config flag for OSS Portal
- Adding option to configure retry count for sonatype and timeout duration for all network call
- Logging improvements
- Bugfix
  - Incorrect convertor added for central portal publishing
  - No error shown while the checks are failed in OSS portal during transitioning
- Breaking Changes
  |                       Then                 |                       Now                                      |
  |:------------------------------------------:|:--------------------------------------------------------------:|
  |  s01_oss_mavenCentralUsername              |            oss_mavenCentralUsername                            |
  |  s01_oss_mavenCentralPassword              |            oss_mavenCentralPassword                            |