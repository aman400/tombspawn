package com.tombspawn.models.config

import com.google.gson.annotations.SerializedName

data class Common(@SerializedName("base_url") val baseUrl: String?,
                  @SerializedName("gradle_path") val gradlePath: String?,
                  @SerializedName("base_port") val basePort: Int)