#!/usr/bin/env kotlin

@file:Import("utils.main.kts")

import java.util.Locale

enum class UpgradeType {
    MAJOR,
    MINOR,
    PATCH
}

/**
 * Bump the VERSION_NAME in the given module's gradle.properties as per the upgrade type and
 * return the updated version.
 */
fun updateLibraryVersion(module: String, upgradeInput: String): String {
    if (upgradeInput.isBlank()) {
        throw IllegalStateException("upgrade type argument required. Possible values major, minor, patch")
    }
    val upgradeType = UpgradeType.valueOf(upgradeInput.uppercase(Locale.getDefault()))
    val propertiesPath = "./$module/gradle.properties"
    val versionName = readProperties(propertiesPath).getProperty("VERSION_NAME")
        ?: throw IllegalStateException("VERSION_NAME cannot be empty")
    val updatedVersion = getUpdatedVersion(versionName, upgradeType)
    // in-place text replacement to preserve the license header and other properties
    replaceTextInFile(propertiesPath, "VERSION_NAME=$versionName", "VERSION_NAME=$updatedVersion")
    return updatedVersion
}

fun getUpdatedVersion(versionName: String, upgradeType: UpgradeType): String {
    val versionSplit = versionName.split(".")
    if (versionSplit.size != 3) throw IllegalStateException("Version not in the required semver format")
    val major = versionSplit[0].toInt()
    val minor = versionSplit[1].toInt()
    val patch = versionSplit[2].toInt()
    return when (upgradeType) {
        UpgradeType.MAJOR -> "${major + 1}.0.0"
        UpgradeType.MINOR -> "$major.${minor + 1}.0"
        UpgradeType.PATCH -> "$major.$minor.${patch + 1}"
    }
}
