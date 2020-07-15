package com.tombspawn.skeleton.models

import com.google.gson.annotations.SerializedName
import com.tombspawn.base.common.models.GradleTask
import com.tombspawn.skeleton.git.CredentialProvider

data class App constructor(
    @SerializedName("id")
    var id: String,
    @SerializedName("name")
    var name: String?,
    @SerializedName("clone_dir")
    var cloneDir: String? = null,
    @SerializedName("app_dir")
    var appDir: String? = null,
    @SerializedName("remote_uri")
    var uri: String? = null,
    @SerializedName("gradle_tasks")
    val gradleTasks: List<GradleTask>? = null,
    @SerializedName("git_config")
    val gitConfig: GitConfig?
) {

    data class GitConfig(
        @SerializedName("tag_config") val tagConfig: RefConfig? = null,
        @SerializedName("branch_config") val branchConfig: RefConfig? = null,
        @SerializedName("git_credentials") val credentialProvider: CredentialProvider
    )

    data class RefConfig(
        @SerializedName("count")
        val count: Int,
        @SerializedName("regex")
        val regex: String? = null,
        @SerializedName("default")
        val default: String? = null
    )
}