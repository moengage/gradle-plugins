#!/usr/bin/env kotlin

@file:Import("version-updater.main.kts")
@file:Import("utils.main.kts")

import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import kotlin.system.exitProcess

val publishKey = args[0]
val secretKey = args[1]
val releaseType = args[2]
val ticketNumber = args[3]
val module = args[4]

mergeDevBranchToMaster()

if (executeCommandOnShell("./gradlew :$module:assemble --stacktrace") != 0) {
    println("::error::Failed to assemble $module")
    exitProcess(1)
}

val updatedVersion = updateLibraryVersion(module, releaseType)

val dateFormat = SimpleDateFormat("dd-MM-yyyy")
dateFormat.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
val date = dateFormat.format(Date())

replaceTextInFile("$module/CHANGELOG.md", "Release Date", date)
replaceTextInFile("$module/CHANGELOG.md", "Release Version", updatedVersion)
// commit changes
executeCommandOnShell("git add .")
executeCommandOnShell("git commit -m \"$ticketNumber : release $module $updatedVersion\"")
executeCommandOnShell("git push origin master")

if (executeCommandOnShell(
        "./gradlew :$module:publishPlugins -Pgradle.publish.key=$publishKey -Pgradle.publish.secret=$secretKey"
    ) != 0
) {
    println("::error::Failed to publish plugin")
    exitProcess(1)
}
tagModuleWithLatestVersion(module, getVersionNameForModule(module))
backMergeMasterToDevelopment()
