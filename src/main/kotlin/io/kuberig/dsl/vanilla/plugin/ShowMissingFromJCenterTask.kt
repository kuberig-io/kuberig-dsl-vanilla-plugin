package io.kuberig.dsl.vanilla.plugin

import com.mashape.unirest.http.Unirest
import org.gradle.api.tasks.TaskAction
import java.io.BufferedWriter
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files

open class ShowMissingFromJCenterTask : KubeRigTask() {

    private val extension = this.project.extensions.getByType(KubeRigDslVanillaPluginExtension::class.java)

    @TaskAction
    fun showMissingFromJCenter() {

        val projectSemVersions = mutableListOf<ProjectSemVersion>()

        val subProjects = project.subprojects
        for (subProject in subProjects) {
            projectSemVersions.add(
                    ProjectSemVersion(subProject, SemVersion.fromProjectName(project.rootProject.name, subProject.name))
            )
        }

        projectSemVersions.sortWith(Comparator { o1, o2 ->
            if (o1!!.isHigher(o2!!)) {
                1
            } else {
                -1
            }
        })

        configureUnirest()
        val statusFile = project.file("AVAILABILITY.MD")
        val memoryWriter = StringWriter()
        BufferedWriter(memoryWriter).use { writer ->
            writer.append("# Dependency Availability")
            writer.newLine()
            writer.append("| ${extension.gitHubOwner} version | repositories | bintray package |")
            writer.newLine()
            writer.append("| ------------------ | ------------ | --------------- |")
            writer.newLine()
            for (projectSemVersion in projectSemVersions) {
                val subProject = projectSemVersion.project
                val jsonNodeHttpResponse = Unirest.get("https://api.bintray.com/packages/teyckmans/rigeldev-oss-maven/{module}")
                        .routeParam("module", subProject.name)
                        .asJson()
                val x = "[" + subProject.name + "](https://bintray.com/teyckmans/rigeldev-oss-maven/" + subProject.name + ")"
                println(x)
                val upstreamVersion = subProject.name.substring("kuberig-dsl-kubernetes-".length)
                if (jsonNodeHttpResponse.status == 404) {
                    writer.append("|").append(subProject.name).append("|none|").append(x).append("|")
                    writer.newLine()
                } else {
                    val linkedToRepos = jsonNodeHttpResponse.body.getObject().getJSONArray("linked_to_repos")
                    if (linkedToRepos.length() == 0) {
                        writer.append("| ").append(upstreamVersion).append(" | rigeldev-oss-maven | ").append(x).append(" |")
                        writer.newLine()
                        println("available in [rigeldev-oss-maven]")
                        println("\t\tadd to jcenter with: " +
                                "https://bintray.com/message/addPackageToJCenter?pkgPath=%2Fteyckmans%2Frigeldev-oss-maven%2F" + subProject.name + "&tab=general"
                        )
                    } else {
                        writer.append("| ").append(upstreamVersion).append(" | rigeldev-oss-maven, jcenter | ").append(x).append(" |")
                        writer.newLine()
                    }
                }
            }
            println()
            println("This package contains a version number in the name that matches the upstream ${extension.gitHubOwner} version this package is intended for.")
            println()
        }
        val newContent = memoryWriter.toString()

        val writeFile = if (statusFile.exists()) {
            val currentContent = Files.readString(statusFile.toPath(), StandardCharsets.UTF_8)
            currentContent != newContent
        } else {
            true
        }

        if (writeFile) {
            Files.writeString(statusFile.toPath(), newContent, StandardCharsets.UTF_8)
        }
    }
}