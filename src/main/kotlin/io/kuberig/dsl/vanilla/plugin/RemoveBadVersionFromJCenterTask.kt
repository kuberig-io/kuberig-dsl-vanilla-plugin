package io.kuberig.dsl.vanilla.plugin

import com.mashape.unirest.http.Unirest
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

open class RemoveBadVersionFromJCenterTask : KubeRigTask() {

    @Input
    protected var badVersion: String = ""
        @Option(option = "badVersion", description = "Version to remove from JCenter.")
        set

    @TaskAction
    fun removeVersion() {
        val bintrayUser = this.project.properties["bintrayUser"] as String
        val bintrayApiKey = this.project.properties["bintrayApiKey"] as String

        this.project.subprojects { subProject ->

            val checkResponse = Unirest.get("https://api.bintray.com/packages/teyckmans/rigeldev-oss-maven/{package}/versions/{badVersion}")
                    .routeParam("package", subProject.name)
                    .routeParam("badVersion", badVersion)
                    .basicAuth(bintrayUser, bintrayApiKey)
                    .asJson()

            if (checkResponse.status == 200) {
                val deleteResponse = Unirest.delete("https://api.bintray.com/packages/teyckmans/rigeldev-oss-maven/{package}/versions/{badVersion}")
                        .routeParam("package", subProject.name)
                        .routeParam("badVersion", badVersion)
                        .basicAuth(bintrayUser, bintrayApiKey)
                        .asJson()

                if (deleteResponse.status == 200) {
                    println("${subProject.name} version $badVersion has been deleted.")
                } else {
                    println("${subProject.name} version $badVersion delete failed.")
                    println(deleteResponse.body)
                }
            } else {
                println("${subProject.name} version $badVersion does not exist.")
                println(checkResponse.body)
            }
        }
    }

}