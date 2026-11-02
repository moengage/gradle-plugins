import java.io.File
import java.io.FileInputStream
import java.util.Properties

/**
 * Executes the provided command in working directory on the bash shell.
 */
fun executeCommandOnShell(command: String): Int {
    val process = ProcessBuilder("/bin/bash", "-c", command).inheritIO().start()
    return process.waitFor()
}

/**
 * Executes the provided command on the bash shell and returns its standard output.
 */
fun executeCommandForOutput(command: String): String {
    val process = ProcessBuilder("/bin/bash", "-c", command).start()
    val output = process.inputStream.bufferedReader().readText()
    process.waitFor()
    return output.trim()
}

/**
 * Merge development branch to master branch
 */
fun mergeDevBranchToMaster() {
    executeCommandOnShell("git checkout master")
    executeCommandOnShell("git pull origin master")
    executeCommandOnShell("git checkout development")
    executeCommandOnShell("git pull origin development")
    executeCommandOnShell("git merge master")
    executeCommandOnShell("git checkout master")
    executeCommandOnShell("git merge development")
    executeCommandOnShell("git push origin master")
}

/**
 * Create and push the module scoped git tag, e.g. test-coverage-plugin-v1.2.3
 */
fun tagModuleWithLatestVersion(module: String, version: String) {
    val tag = "$module-v$version"
    println("module: $module \nversion: $version \ntag: $tag")
    executeCommandOnShell("git tag -a $tag -m \"release $module $version\"")
    executeCommandOnShell("git push origin --tags")
}

/**
 * Fetch the value for VERSION_NAME from the module's gradle.properties.
 */
@Throws(IllegalStateException::class)
fun getVersionNameForModule(module: String): String {
    val properties = Properties()
    properties.load(FileInputStream("./$module/gradle.properties"))
    return properties.getProperty("VERSION_NAME") ?: run {
        println("VERSION_NAME not found")
        throw IllegalStateException("VERSION_NAME not found")
    }
}

fun replaceTextInFile(filePath: String, currentText: String, updatedText: String) {
    val file = File(filePath)
    if (!file.exists()) {
        throw IllegalStateException("File does not exist")
    }
    val fileContent = file.readText()
    val updatedFileContent = fileContent.replaceFirst(currentText, updatedText)
    file.writeText(updatedFileContent)
}

fun readProperties(filePath: String): Properties {
    val properties = Properties()
    properties.load(FileInputStream(filePath))
    return properties
}

fun backMergeMasterToDevelopment() {
    executeCommandOnShell("git checkout master")
    executeCommandOnShell("git pull origin master")
    executeCommandOnShell("git checkout development")
    executeCommandOnShell("git pull origin development")
    executeCommandOnShell("git merge master")
    executeCommandOnShell("git push origin development")
}
