package io.kuberig.dsl.vanilla.plugin

import org.gradle.api.DefaultTask

open class KubeRigTask : DefaultTask() {
    init {
        this.group = "kuberig"
    }
}