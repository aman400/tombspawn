package com.tombspawn.skeleton.models

import com.google.gson.annotations.SerializedName

data class App constructor(
    @SerializedName("id")
    var id: String,
    @SerializedName("name")
    var name: String?,
    @SerializedName("app_url")
    var appUrl: String?,
    @SerializedName("repo_id")
    var repoId: String?,
    @SerializedName("dir")
    var dir: String? = null,
    @SerializedName("remote_uri")
    var uri: String? = null,
    @SerializedName("gradle_tasks")
    val gradleTasks: List<GradleTask>? = null
) {
    data class GradleTask(@SerializedName("id") val id: String,
                          @SerializedName("tasks") val tasks: List<String>,
                          @SerializedName("output_dir") val outputDir: String)
}