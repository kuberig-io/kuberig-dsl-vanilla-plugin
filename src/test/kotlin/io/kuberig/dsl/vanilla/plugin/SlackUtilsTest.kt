package io.kuberig.dsl.vanilla.plugin

import kotlin.test.Test

internal class SlackUtilsTest {

    @Test fun newUpStreamVersions() {
        val slackUtils = SlackUtils("test-channel")

        slackUtils.newUpStreamVersions("Kubernetes", listOf(
                NewVersion("kuberig-dsl-kubernetes-v1.19.3"),
                NewVersion("kuberig-dsl-kubernetes-v1.19.4")
        ))

    }
}