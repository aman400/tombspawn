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
    var uri: String? = null
)