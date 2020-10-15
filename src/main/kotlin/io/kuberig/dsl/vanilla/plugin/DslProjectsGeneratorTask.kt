package io.kuberig.dsl.vanilla.plugin

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*

open class DslProjectsGeneratorTask : KubeRigTask() {

    private val extension = this.project.extensions.getByType(KubeRigDslVanillaPluginExtension::class.java)

    @TaskAction
    fun generatorDslProjects() {
        configureUnirest()

        val rootProjectName = this.project.rootProject.name

        val tags: HttpResponse<Array<GitHubTagRef>> = Unirest.get("https://api.github.com/repos/{gitHubOwner}/{gitHubRepo}/git/refs/tags")
                .routeParam("gitHubOwner", extension.gitHubOwner)
                .routeParam("gitHubRepo", extension.gitHubRepo)
                .header("Accept", "application/vnd.github.v3+json")
                .asObject<Array<GitHubTagRef>>(Array<GitHubTagRef>::class.java)
        for (tag in tags.body) {
            val tagName: String = tag.ref.substring("refs/tags/".length)
            if (shouldImportTag(tagName)) {
                println("Generating project for tag $tagName")
                val moduleName = "$rootProjectName-$tagName"
                val moduleDir = File(moduleName)
                if (!isModuleValid(moduleDir)) {
                    createDirectoryIfNeeded(moduleDir)
                    createDirectoryIfNeeded(File(moduleDir, "src"))
                    createDirectoryIfNeeded(File(moduleDir, "src/main"))
                    createDirectoryIfNeeded(File(moduleDir, "src/main/resources"))
                    val getRequest = Unirest.get("https://raw.githubusercontent.com/{gitHubOwner}/{gitHubRepo}/{tagName}/${extension.swaggerLocation}")
                            .routeParam("gitHubOwner", extension.gitHubOwner)
                            .routeParam("gitHubRepo", extension.gitHubRepo)
                            .routeParam("tagName", tagName)
                    val swaggerJsonUrl = getRequest.url
                    val swaggerJson = getRequest
                            .asString()
                    if (swaggerJson.status == 200) {
                        val swaggerJsonText = swaggerJson.body
                        if (swaggerJsonText.contains("x-kubernetes-group-version-kind")) {
                            println("$tagName => VALID ")
                            val swaggerJsonFile = File(moduleDir, "src/main/resources/swagger.json")
                            Files.write(swaggerJsonFile.toPath(), swaggerJsonText.toByteArray(StandardCharsets.UTF_8))
                            val buildGradleKtsFile = File(moduleDir, "build.gradle.kts")
                            val buildGradleKtsLines = buildGradlektsLines()
                            Files.write(buildGradleKtsFile.toPath(), buildGradleKtsLines)
                            val readmeFile = File(moduleDir, "README.MD")
                            val readmeLines = Arrays.asList(
                                    "# $moduleName",
                                    "",
                                    "Swagger file downloaded from $swaggerJsonUrl"
                            )
                            Files.write(readmeFile.toPath(), readmeLines, StandardCharsets.UTF_8)
                            updateRootProjectSettingsGradleKtsLines(moduleName)
                        } else {
                            println("$tagName => IN-VALID ( does not have x-kubernetes-group-version-kind info )")
                        }
                    } else {
                        println(tagName + " => " + swaggerJson.statusText)
                    }
                } else {
                    val buildGradleKtsFile = File(moduleDir, "build.gradle.kts")
                    val buildGradleKtsLines = buildGradlektsLines()
                    Files.write(buildGradleKtsFile.toPath(), buildGradleKtsLines)
                    updateRootProjectSettingsGradleKtsLines(moduleName)
                }
            }
        }
    }

    private fun shouldImportTag(tagName: String): Boolean {
        return if (tagName.contains("-")) {
            // -alpha, -beta, -rc
            false
        } else {
            val tagVersion = SemVersion.fromTagName(tagName)
            if (tagVersion == null) {
                false
            } else {
                return if (extension.startVersion == null) {
                    true
                } else {
                    tagVersion == extension.startVersion!! || tagVersion.isHigher(extension.startVersion!!)
                }
            }
        }
    }

    private fun updateRootProjectSettingsGradleKtsLines(moduleName: String) {
        val settingsGradleKts = Paths.get("settings.gradle.kts")
        val settingsGradleKtsLines = Files.readAllLines(settingsGradleKts, StandardCharsets.UTF_8)
        val lineToAdd = "include(\"$moduleName\")"
        if (!settingsGradleKtsLines.contains(lineToAdd)) {
            Files.write(settingsGradleKts, "\n$lineToAdd".toByteArray(StandardCharsets.UTF_8), StandardOpenOption.APPEND)
        }
    }

    private fun buildGradlektsLines(): List<String> {
        return Arrays.asList("plugins {",
                "    id(\"io.kuberig.dsl.generator\") ",
                "}",
                "",
                "repositories {",
                "   jcenter()",
                "   maven(\"https://dl.bintray.com/teyckmans/rigeldev-oss-maven\")",
                "}")
    }

    private fun isModuleValid(moduleDir: File): Boolean {
        val moduleDirectoryExists = moduleDir.exists()
        val buildFile = File(moduleDir, "build.gradle.kts")
        val validBuildFile = buildFile.exists() && buildFile.length() != 0L
        val swaggerFile = File(moduleDir, "src/main/resources/swagger.json")
        val validSwaggerFile = swaggerFile.exists() && swaggerFile.length() != 0L
        return moduleDirectoryExists && validBuildFile && validSwaggerFile
    }

    private fun createDirectoryIfNeeded(directory: File) {
        if (!directory.exists()) {
            check(directory.mkdir()) { "Failed to create directory [" + directory.absolutePath + "]" }
        }
    }
}