/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package io.kuberig.dsl.vanilla.plugin

import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

class KuberigDslVanillaPluginPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        val bintrayUser = project.properties["bintrayUser"] as String?
        val bintrayApiKey = project.properties["bintrayApiKey"] as String?

        project.extensions.create("kubeRigDslVanilla", KubeRigDslVanillaPluginExtension::class.java)

        project.tasks.register("generateDslProjects", DslProjectsGeneratorTask::class.java)

        project.tasks.register("showMissingFromJCenter", ShowMissingFromJCenterTask::class.java)

        project.tasks.register("removeBadVersion", RemoveBadVersionFromJCenterTask::class.java)

        project.tasks.register(
                "publishMissing",
                PublishMissing::class.java
        ) { task ->
            val version = project.version.toString()
            val subProjects = project.subprojects
            val subProjectsToPublish: MutableList<Project> = ArrayList()
            if (bintrayUser != null && bintrayApiKey != null) {
                for (subProject in subProjects) {
                    if (!jcenterExists("io-kuberig-" + subProject.name, version, bintrayUser, bintrayApiKey)) {
                        subProjectsToPublish.add(subProject)
                    }
                }
            }
            if (subProjectsToPublish.isEmpty()) {
                project.logger.warn("Nothing missing to publish.")
            } else {
                for (subProjectToPublish in subProjectsToPublish) {
                    project.logger.info(subProjectToPublish.name + " needs to be published.")
                    task.dependsOn(
                            subProjectToPublish.tasks.getByName("bintrayUpload")
                    )
                }
            }
        }

        project.run {
            subprojects {
                val subProject = it
                subProject.plugins.apply("com.jfrog.bintray")
                subProject.plugins.apply("maven-publish")
                subProject.plugins.apply("java")
                subProject.plugins.apply("idea")

                subProject.group = "io.kuberig.dsl.kubernetes"
                subProject.version = project.version

                subProject.repositories.jcenter()
                subProject.repositories.maven {
                    it.setUrl("https://dl.bintray.com/teyckmans/rigeldev-oss-maven")
                }

                subProject.extensions.configure(JavaPluginExtension::class.java) {
                    it.sourceCompatibility = JavaVersion.VERSION_1_8
                    it.targetCompatibility = JavaVersion.VERSION_1_8
                }
                subProject.tasks.withType(KotlinCompile::class.java) {
                    it.kotlinOptions.jvmTarget = "1.8"
                }

                val sourcesJar = subProject.tasks.register("sourcesJar", Jar::class.java) {
                    it.archiveClassifier.set("sources")

                    val sourceSets = subProject.properties["sourceSets"] as SourceSetContainer
                    it.from(sourceSets.getByName("main").allSource)
                }

                subProject.extensions.configure(PublishingExtension::class.java) {

                    it.publications.register(subProject.name, MavenPublication::class.java) {
                        it.from(subProject.components.getByName("java"))
                        it.artifact(sourcesJar.get())
                    }

                }

                subProject.extensions.configure(BintrayExtension::class.java) {
                    it.user = bintrayUser
                    it.key = bintrayApiKey
                    it.publish = true

                    it.pkg.repo = "rigeldev-oss-maven"
                    it.pkg.name = "io-kuberig-" + subProject.name
                    it.pkg.setLicenses("Apache-2.0")
                    it.pkg.vcsUrl = "https://github.com/teyckmans/kuberig-dsl-kubernetes"

                    it.setPublications(subProject.name)
                }
            }
        }
    }
}
