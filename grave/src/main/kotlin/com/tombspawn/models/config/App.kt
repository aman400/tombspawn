package com.tombspawn.models.config

import com.google.gson.annotations.SerializedName
import com.tombspawn.base.annotations.DoNotDeserialize
import com.tombspawn.base.annotations.DoNotSerialize
import com.tombspawn.network.GradleExecutor
import io.ktor.client.HttpClient

data class App constructor(@SerializedName("id") val id: String,
               @SerializedName("name") val name: String?,
               @SerializedName("app_url") val appUrl: String?,
               @SerializedName("repo_id") val repoId: String?,
               @SerializedName("dir") var dir: String? = null,
               @SerializedName("remote_uri") val uri: String? = null) {
    @DoNotSerialize
    @DoNotDeserialize
    var gradleExecutor: GradleExecutor? = null
    @DoNotSerialize
    @DoNotDeserialize
    var networkClient: HttpClient? = null
    @DoNotSerialize
    @DoNotDeserialize
    var port: Int? = null
}