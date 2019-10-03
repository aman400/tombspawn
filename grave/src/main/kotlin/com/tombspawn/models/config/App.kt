package com.tombspawn.models.config

import com.google.gson.annotations.SerializedName
import com.tombspawn.data.Flavour
import com.tombspawn.models.CommandResponse
import com.tombspawn.models.Reference
import com.tombspawn.network.CommandExecutor
import com.tombspawn.network.GradleExecutor

data class App constructor(@SerializedName("id") var id: String,
               @SerializedName("name") var name: String?,
               @SerializedName("app_url") var appUrl: String?,
               @SerializedName("repo_id") var repoId: String?,
               @SerializedName("dir") var dir: String? = null,
               @SerializedName("remote_uri") var uri: String? = null,
               var gradleExecutor: GradleExecutor? = null)