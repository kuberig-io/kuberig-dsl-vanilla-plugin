package io.kuberig.dsl.vanilla.plugin

import com.slack.api.Slack
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.model.block.HeaderBlock
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.SectionBlock
import com.slack.api.model.block.composition.MarkdownTextObject
import com.slack.api.model.block.composition.PlainTextObject
import com.slack.api.model.block.element.ButtonElement

data class NewVersion(val versionModuleName: String) {
    fun requestLink(): String {
        return "https://bintray.com/message/addPackageToJCenter?pkgPath=%2Fteyckmans%2Frigeldev-oss-maven%2F" + this.versionModuleName + "&tab=general"
    }
}

class SlackUtils(val channel: String) {
    private val slackToken = System.getenv("SLACK_TOKEN")

    fun newUpStreamVersions(upstreamName: String, newVersions: List<NewVersion>) {
        if (slackToken == null) {
            println("No SLACK_TOKEN environment variable set, sending slack notification [SKIPPED]")
        } else {
            this.sendNewUpStreamVersionsNotification(upstreamName, newVersions)
        }
    }

    private fun sendNewUpStreamVersionsNotification(upstreamName: String, newVersions: List<NewVersion>) {
        val slack = Slack.getInstance()
        val methods = slack.methods(this.slackToken)

        val requestBuilder = ChatPostMessageRequest.builder()
                .channel(this.channel)

        val blocks = mutableListOf<LayoutBlock>()

        blocks.add(
                HeaderBlock.builder()
                        .text(PlainTextObject("New upstream $upstreamName versions detected", true))
                        .build()
        )

        for (newVersion in newVersions) {
            blocks.add(
                    SectionBlock.builder()
                            .text(MarkdownTextObject.builder()
                                    .text(newVersion.versionModuleName)
                                    .build())
                            .accessory(
                                    ButtonElement.builder()
                                            .text(PlainTextObject("Request JCenter inclusion", true))
                                            .url(newVersion.requestLink())
                                            .build()
                            )
                            .build()
            )
        }

        blocks.add(
                SectionBlock.builder()
                        .text(MarkdownTextObject.builder()
                                .text("*Message to use for the JCenter inclusion request*")
                                .build())
                        .build()
        )

        blocks.add(
                SectionBlock.builder()
                        .text(MarkdownTextObject.builder()
                                .text("This package contains a version number in the name that matches the upstream $upstreamName version this package is intended for.")
                                .build())
                        .build()
        )

        requestBuilder.blocks(blocks);

        val chatPostMessage = methods.chatPostMessage(requestBuilder.build())
        println("chatPostMessage = $chatPostMessage")
    }

}