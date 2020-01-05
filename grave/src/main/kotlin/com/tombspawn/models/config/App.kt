package com.tombspawn.models.config

import com.google.gson.annotations.SerializedName

data class App constructor(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("app_url") val appUrl: String?,
    @SerializedName("repo_id") val repoId: String?,
    @SerializedName("dir") var dir: String? = null,
    @SerializedName("remote_uri") val uri: String? = null,
    @SerializedName("memory") val memory: Long? = null,
    @SerializedName("swap") val swap: Long? = null,
    @SerializedName("cpu_shares") val cpuShares: Int? = null
)