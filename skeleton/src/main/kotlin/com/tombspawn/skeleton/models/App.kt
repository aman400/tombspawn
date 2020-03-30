package com.tombspawn.skeleton.models

import com.google.gson.annotations.SerializedName
import com.tombspawn.base.common.models.GradleTask

data class App constructor(
    @SerializedName("id")
    var id: String,
    @SerializedName("name")
    var name: String?,
    @SerializedName("repo_id")
    var repoId: String?,
    @SerializedName("dir")
    var dir: String? = null,
    @SerializedName("remote_uri")
    var uri: String? = null,
    @SerializedName("gradle_tasks")
    val gradleTasks: List<GradleTask>? = null
)