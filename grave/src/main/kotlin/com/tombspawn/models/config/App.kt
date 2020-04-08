package com.tombspawn.models.config

import com.google.gson.annotations.SerializedName
import com.tombspawn.base.common.models.GradleTask
import com.tombspawn.models.slack.Element

data class App constructor(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("repo_id") val repoId: String?,
    @SerializedName("app_dir") var appDir: String? = null,
    @SerializedName("clone_dir") var cloneDir: String? = null,
    @SerializedName("remote_uri") val uri: String? = null,
    @SerializedName("container_uri") val containerUri: String? = null,
    @SerializedName("environment_variables") val env: Map<String, String?>? = null,
    @SerializedName("files") val fileMappings: List<FileMapping>? = null,
    @SerializedName("gradle_tasks") val gradleTasks: List<GradleTask>? = null,
    @SerializedName("build_params") val elements: List<Element>? = null,
    @SerializedName("tag_count") private val _tagCount: Int? = null,
    @SerializedName("branch_count") private val _branchCount: Int? = null,
    @SerializedName("docker_config") val dockerConfig: DockerConfig? = null
) {

    fun dockerEnvVariables() = if(!env.isNullOrEmpty()) {
            env.filter {(key, value) ->
                key.isNotEmpty() && !value.isNullOrEmpty()
            }.map { (key, value) ->
                "ENV $key $value"
            }.joinToString("\n")
        } else {
            ""
        }

    val tagCount: Int
        get() = _tagCount?.coerceAtMost((100 - branchCount).coerceAtLeast(0)) ?: -1

    val branchCount: Int
        get() = _branchCount?.coerceAtMost(100) ?: -1

    data class FileMapping(
        @SerializedName("name") val name: String,
        @SerializedName("path") val path: String
    )

    data class DockerConfig(
        @SerializedName("sysctls") val systemCtls: Map<String, String>? = null,
        @SerializedName("memory") val memory: Long? = null,
        @SerializedName("swap") val swap: Long? = null,
        @SerializedName("cpu_shares") val cpuShares: Int? = null
    )
}