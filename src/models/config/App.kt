package com.ramukaka.models.config

import com.google.gson.annotations.SerializedName

data class App(@SerializedName("id") val id: String,
               @SerializedName("name") val name: String,
               @SerializedName("app_url") val appUrl: String,
               @SerializedName("repo_id") val repoId: String,
               @SerializedName("dir") val dir: String)