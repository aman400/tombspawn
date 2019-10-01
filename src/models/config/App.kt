package com.ramukaka.models.config

import com.google.gson.annotations.SerializedName
import com.ramukaka.data.Flavour
import com.ramukaka.models.CommandResponse
import com.ramukaka.models.Reference
import com.ramukaka.network.CommandExecutor
import com.ramukaka.network.GradleExecutor

data class App constructor(@SerializedName("id") var id: String,
               @SerializedName("name") var name: String?,
               @SerializedName("app_url") var appUrl: String?,
               @SerializedName("repo_id") var repoId: String?,
               @SerializedName("dir") var dir: String? = null,
               @SerializedName("remote_uri") var uri: String? = null,
               var gradleExecutor: GradleExecutor? = null)