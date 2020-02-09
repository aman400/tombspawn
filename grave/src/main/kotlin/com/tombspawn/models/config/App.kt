package com.tombspawn.models.config

import com.google.gson.annotations.SerializedName
import com.tombspawn.base.common.models.GradleTask
import com.tombspawn.models.slack.Element

data class App constructor(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("repo_id") val repoId: String?,
    @SerializedName("dir") var dir: String? = null,
    @SerializedName("remote_uri") val uri: String? = null,
    @SerializedName("memory") val memory: Long? = null,
    @SerializedName("swap") val swap: Long? = null,
    @SerializedName("cpu_shares") val cpuShares: Int? = null,
    @SerializedName("container_uri") val containerUri: String? = null,
    @SerializedName("environment_variables") val env: List<String>? = null,
    @SerializedName("files") val fileMappings: List<FileMapping>? = null,
    @SerializedName("gradle_tasks") val gradleTasks: List<GradleTask>? = null,
    @SerializedName("build_params") val elements: List<Element>? = null,
    @SerializedName("tag_count") private val _tagCount: Int? = null
) {
    val tagCount: Int
        get() = _tagCount ?: 5

    data class FileMapping(
        @SerializedName("name") val name: String,
        @SerializedName("path") val path: String
    )
}