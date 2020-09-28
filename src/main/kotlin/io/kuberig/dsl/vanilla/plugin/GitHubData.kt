package io.kuberig.dsl.vanilla.plugin

data class GitHubTagRef(val ref: String, val node_id: String, val url: String, val `object`: GitHubGitObject) {
}

data class GitHubGitObject(val sha: String, val type: String, val url: String)