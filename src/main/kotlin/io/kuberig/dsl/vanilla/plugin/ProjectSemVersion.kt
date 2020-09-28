package io.kuberig.dsl.vanilla.plugin

import org.gradle.api.Project

data class ProjectSemVersion(val project: Project, val semVersion: SemVersion) {
    fun isHigher(otherProjectSemVersion: ProjectSemVersion): Boolean {
        return semVersion.isHigher(otherProjectSemVersion.semVersion)
    }
}