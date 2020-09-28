package io.kuberig.dsl.vanilla.plugin

open class KubeRigDslVanillaPluginExtension{
    var gitHubOwner: String = ""
    var gitHubRepo: String = ""
    var startVersion: SemVersion? = null
    var swaggerLocation: String = ""
}